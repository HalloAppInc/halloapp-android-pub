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
import com.halloapp.proto.log_events.GroupDecryptionReport;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupCommentDecryptReportStats {

    private static final String GROUP_COMMENT_DECRYPT_STATS_WORK_ID = "group-comment-decrypt-report-stats";

    private static final long MIN_DELAY = DateUtils.DAY_IN_MILLIS;
    private static final int MAX_BATCH_SIZE = 50;

    private static GroupCommentDecryptReportStats instance;

    private final AppContext appContext;
    private final ContentDb contentDb;
    private final Preferences preferences;
    private final Events events;

    public static GroupCommentDecryptReportStats getInstance() {
        if (instance == null) {
            synchronized(GroupCommentDecryptReportStats.class) {
                if (instance == null) {
                    instance = new GroupCommentDecryptReportStats();
                }
            }
        }
        return instance;
    }

    private GroupCommentDecryptReportStats() {
        this.appContext = AppContext.getInstance();
        this.contentDb = ContentDb.getInstance();
        this.preferences = Preferences.getInstance();
        this.events = Events.getInstance();
    }

    public void start() {
        Log.d("GroupCommentDecryptReportStats.start");
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(DecryptReportStatsWorker.class).build();
        WorkManager.getInstance(appContext.get()).enqueueUniqueWork(GROUP_COMMENT_DECRYPT_STATS_WORK_ID, ExistingWorkPolicy.KEEP, workRequest);
    }

    @WorkerThread
    private ListenableWorker.Result run() {
        Log.i("GroupCommentDecryptReportStats.run");

        long now = System.currentTimeMillis();

        if (!run(now)) {
            Log.i("GroupCommentDecryptReportStats.run group failure");
            return ListenableWorker.Result.failure();
        }

        Log.i("GroupCommentDecryptReportStats success");
        return ListenableWorker.Result.success();
    }

    @WorkerThread
    private boolean run(long now) {
        long lastId = preferences.getLastGroupCommentDecryptStatMessageRowId();
        List<GroupDecryptStats> stats = contentDb.getGroupCommentDecryptStats(lastId);
        Log.i("GroupCommentDecryptReportStats.run lastId: " + lastId);

        if (lastId < 0) {
            Log.i("GroupCommentDecryptReportStats.run first time running; setting last id to most recent message");
            for (GroupDecryptStats stat : stats) {
                if (stat.rowId > lastId) {
                    lastId = stat.rowId;
                }
            }

            preferences.setLastGroupCommentDecryptStatMessageRowId(lastId);

            return true;
        }

        Collections.sort(stats, (o1, o2) -> Long.compare(o1.rowId, o2.rowId));
        List<List<GroupDecryptStats>> batches = new ArrayList<>();
        List<GroupDecryptStats> current = new ArrayList<>();
        for (GroupDecryptStats stat : stats) {
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
        Log.i("GroupCommentDecryptReportStats.run made " + batches.size() + " batches");

        for (List<GroupDecryptStats> batch : batches) {
            List<GroupDecryptionReport> reports = new ArrayList<>();
            for (GroupDecryptStats ds : batch) {
                reports.add(ds.toDecryptionReport());
            }

            try {
                events.sendGroupDecryptionReports(reports).await();
                long newLastId = batch.get(batch.size() - 1).rowId;

                preferences.setLastGroupCommentDecryptStatMessageRowId(newLastId);

                Log.i("GroupCommentDecryptReportStats.run batch succeeded; new lastId is " + newLastId);
            } catch (InterruptedException | ObservableErrorException e) {
                Log.e("GroupCommentDecryptReportStats.run batch failed", e);
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
            final Result result = GroupCommentDecryptReportStats.getInstance().run();
            if  (!Result.success().equals(result)) {
                Log.sendErrorReport("GroupCommentDecryptReportStats failed");
            }
            return result;
        }
    }
}
