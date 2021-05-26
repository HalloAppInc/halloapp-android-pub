package com.halloapp.util.stats;

import android.content.Context;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.AppContext;
import com.halloapp.Preferences;
import com.halloapp.content.ContentDb;
import com.halloapp.proto.log_events.DecryptionReport;
import com.halloapp.proto.server.Comment;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DecryptReportStats {

    private static final String DECRYPT_STATS_WORK_ID = "decrypt-report-stats";

    private static final long MIN_DELAY = DateUtils.DAY_IN_MILLIS;
    private static final int MAX_BATCH_SIZE = 50;

    private static DecryptReportStats instance;

    private final AppContext appContext;
    private final ContentDb contentDb;
    private final Preferences preferences;
    private final Events events;

    public static DecryptReportStats getInstance() {
        if (instance == null) {
            synchronized(DecryptReportStats.class) {
                if (instance == null) {
                    instance = new DecryptReportStats();
                }
            }
        }
        return instance;
    }

    private DecryptReportStats() {
        this.appContext = AppContext.getInstance();
        this.contentDb = ContentDb.getInstance();
        this.preferences = Preferences.getInstance();
        this.events = Events.getInstance();
    }

    public void start() {
        Log.d("DecryptReportStats.start");
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(DecryptReportStatsWorker.class).build();
        WorkManager.getInstance(appContext.get()).enqueueUniqueWork(DECRYPT_STATS_WORK_ID, ExistingWorkPolicy.KEEP, workRequest);
    }

    @WorkerThread
    private ListenableWorker.Result run() {
        Log.i("DecryptReportStats.run");

        long now = System.currentTimeMillis();

        if (!run(now)) {
            Log.i("DecryptReportStats.run normal message failure");
            return ListenableWorker.Result.failure();
        }

        Log.i("DecryptReportStats success");
        return ListenableWorker.Result.success();
    }

    @WorkerThread
    private boolean run(long now) {
        long lastId = preferences.getLastDecryptStatMessageRowId();
        List<DecryptStats> stats = contentDb.getMessageDecryptStats(lastId);
        Log.i("DecryptReportStats.run lastId: " + lastId);

        if (lastId < 0) {
            Log.i("DecryptReportStats.run first time running; setting last id to most recent message");
            for (DecryptStats stat : stats) {
                if (stat.rowId > lastId) {
                    lastId = stat.rowId;
                }
            }

            preferences.setLastDecryptStatMessageRowId(lastId);

            return true;
        }

        Collections.sort(stats, (o1, o2) -> Long.compare(o1.rowId, o2.rowId));
        List<List<DecryptStats>> batches = new ArrayList<>();
        List<DecryptStats> current = new ArrayList<>();
        for (DecryptStats stat : stats) {
            if (now - stat.originalTimestamp < MIN_DELAY) {
                break;
            } else {
                if (current.size() >= MAX_BATCH_SIZE) {
                    batches.add(current);
                    current = new ArrayList<>();
                }

                current.add(stat);
            }
        }
        if (current.size() > 0) {
            batches.add(current);
        }
        Log.i("DecryptReportStats.run made " + batches.size() + " batches");

        for (List<DecryptStats> batch : batches) {
            List<DecryptionReport> reports = new ArrayList<>();
            for (DecryptStats ds : batch) {
                reports.add(ds.toDecryptionReport());
            }

            try {
                events.sendDecryptionReports(reports).await();
                long newLastId = batch.get(batch.size() - 1).rowId;

                preferences.setLastDecryptStatMessageRowId(newLastId);

                Log.i("DecryptReportStats.run batch succeeded; new lastId is " + newLastId);
            } catch (InterruptedException | ObservableErrorException e) {
                Log.e("DecryptReportStats.run batch failed", e);
                return false;
            }
        }

        return true;
    }

    public static class DecryptReportStatsWorker extends Worker {

        public DecryptReportStatsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
            super(context, params);
        }

        @Override
        public @NonNull Result doWork() {
            final Result result = DecryptReportStats.getInstance().run();
            if  (!Result.success().equals(result)) {
                Log.sendErrorReport("DecryptReportStats failed");
            }
            return result;
        }
    }
}
