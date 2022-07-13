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

public class HomeCommentDecryptReportStats {

    private static final String HOME_COMMENT_DECRYPT_STATS_WORK_ID = "home-comment-decrypt-report-stats";

    private static final long MIN_DELAY = DateUtils.DAY_IN_MILLIS;
    private static final int MAX_BATCH_SIZE = 50;

    private static HomeCommentDecryptReportStats instance;

    private final AppContext appContext;
    private final ContentDb contentDb;
    private final Preferences preferences;
    private final Events events;

    public static HomeCommentDecryptReportStats getInstance() {
        if (instance == null) {
            synchronized(HomeCommentDecryptReportStats.class) {
                if (instance == null) {
                    instance = new HomeCommentDecryptReportStats();
                }
            }
        }
        return instance;
    }

    private HomeCommentDecryptReportStats() {
        this.appContext = AppContext.getInstance();
        this.contentDb = ContentDb.getInstance();
        this.preferences = Preferences.getInstance();
        this.events = Events.getInstance();
    }

    public void start() {
        Log.d("HomeCommentDecryptReportStats.start");
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(DecryptReportStatsWorker.class).build();
        WorkManager.getInstance(appContext.get()).enqueueUniqueWork(HOME_COMMENT_DECRYPT_STATS_WORK_ID, ExistingWorkPolicy.KEEP, workRequest);
    }

    @WorkerThread
    private ListenableWorker.Result run() {
        Log.i("HomeCommentDecryptReportStats.run");

        long now = System.currentTimeMillis();

        if (!run(now)) {
            Log.i("HomeCommentDecryptReportStats.run home failure");
            return ListenableWorker.Result.failure();
        }

        Log.i("HomeCommentDecryptReportStats success");
        return ListenableWorker.Result.success();
    }

    @WorkerThread
    private boolean run(long now) {
        long lastId = preferences.getLastHomeCommentDecryptStatMessageRowId();
        List<HomeDecryptStats> stats = contentDb.getHomeCommentDecryptStats(lastId);
        Log.i("HomeCommentDecryptReportStats.run lastId: " + lastId);

        if (lastId < 0) {
            Log.i("HomeCommentDecryptReportStats.run first time running; setting last id to most recent message");
            for (HomeDecryptStats stat : stats) {
                if (stat.rowId > lastId) {
                    lastId = stat.rowId;
                }
            }

            preferences.setLastHomeCommentDecryptStatMessageRowId(lastId);

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
        Log.i("HomeCommentDecryptReportStats.run made " + batches.size() + " batches");

        for (List<HomeDecryptStats> batch : batches) {
            List<HomeDecryptionReport> reports = new ArrayList<>();
            for (HomeDecryptStats ds : batch) {
                reports.add(ds.toDecryptionReport());
            }

            try {
                events.sendHomeDecryptionReports(reports).await();
                long newLastId = batch.get(batch.size() - 1).rowId;

                preferences.setLastHomeCommentDecryptStatMessageRowId(newLastId);

                Log.i("HomeCommentDecryptReportStats.run batch succeeded; new lastId is " + newLastId);
            } catch (InterruptedException | ObservableErrorException e) {
                Log.e("HomeCommentDecryptReportStats.run batch failed", e);
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
            final Result result = HomeCommentDecryptReportStats.getInstance().run();
            if  (!Result.success().equals(result)) {
                Log.sendErrorReport("HomeCommentDecryptReportStats failed");
            }
            return result;
        }
    }
}
