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
import com.halloapp.proto.log_events.HomeDecryptionReport;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomePostDecryptReportStats {

    private static final String HOME_POST_DECRYPT_STATS_WORK_ID = "home-post-decrypt-report-stats";

    private static final long MIN_DELAY = DateUtils.DAY_IN_MILLIS;
    private static final int MAX_BATCH_SIZE = 50;

    private static HomePostDecryptReportStats instance;

    private final AppContext appContext;
    private final ContentDb contentDb;
    private final Preferences preferences;
    private final Events events;

    public static HomePostDecryptReportStats getInstance() {
        if (instance == null) {
            synchronized(HomePostDecryptReportStats.class) {
                if (instance == null) {
                    instance = new HomePostDecryptReportStats();
                }
            }
        }
        return instance;
    }

    private HomePostDecryptReportStats() {
        this.appContext = AppContext.getInstance();
        this.contentDb = ContentDb.getInstance();
        this.preferences = Preferences.getInstance();
        this.events = Events.getInstance();
    }

    public void start() {
        Log.d("HomePostDecryptReportStats.start");
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(DecryptReportStatsWorker.class).build();
        WorkManager.getInstance(appContext.get()).enqueueUniqueWork(HOME_POST_DECRYPT_STATS_WORK_ID, ExistingWorkPolicy.KEEP, workRequest);
    }

    @WorkerThread
    private ListenableWorker.Result run() {
        Log.i("HomePostDecryptReportStats.run");

        long now = System.currentTimeMillis();

        if (!run(now)) {
            Log.i("HomePostDecryptReportStats.run home failure");
            return ListenableWorker.Result.failure();
        }

        Log.i("HomePostDecryptReportStats success");
        return ListenableWorker.Result.success();
    }

    @WorkerThread
    private boolean run(long now) {
        long lastId = preferences.getLastHomePostDecryptStatMessageRowId();
        List<HomeDecryptStats> stats = contentDb.getHomePostDecryptStats(lastId);
        Log.i("HomePostDecryptReportStats.run lastId: " + lastId);

        if (lastId < 0) {
            Log.i("HomePostDecryptReportStats.run first time running; setting last id to most recent message");
            for (HomeDecryptStats stat : stats) {
                if (stat.rowId > lastId) {
                    lastId = stat.rowId;
                }
            }

            preferences.setLastHomePostDecryptStatMessageRowId(lastId);

            return true;
        }

        Collections.sort(stats, (o1, o2) -> Long.compare(o1.rowId, o2.rowId));
        List<List<HomeDecryptStats>> batches = new ArrayList<>();
        List<HomeDecryptStats> current = new ArrayList<>();
        for (HomeDecryptStats stat : stats) {
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
        Log.i("HomePostDecryptReportStats.run made " + batches.size() + " batches");

        for (List<HomeDecryptStats> batch : batches) {
            List<HomeDecryptionReport> reports = new ArrayList<>();
            for (HomeDecryptStats ds : batch) {
                reports.add(ds.toDecryptionReport());
            }

            try {
                events.sendHomeDecryptionReports(reports).await();
                long newLastId = batch.get(batch.size() - 1).rowId;

                preferences.setLastHomePostDecryptStatMessageRowId(newLastId);

                Log.i("HomePostDecryptReportStats.run batch succeeded; new lastId is " + newLastId);
            } catch (InterruptedException | ObservableErrorException e) {
                Log.e("HomePostDecryptReportStats.run batch failed", e);
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
            final Result result = HomePostDecryptReportStats.getInstance().run();
            if  (!Result.success().equals(result)) {
                Log.sendErrorReport("HomePostDecryptReportStats failed");
            }
            return result;
        }
    }
}
