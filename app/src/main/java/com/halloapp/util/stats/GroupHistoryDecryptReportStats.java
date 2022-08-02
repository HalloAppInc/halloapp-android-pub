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
import com.halloapp.proto.log_events.GroupHistoryReport;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupHistoryDecryptReportStats {

    private static final String GROUP_HISTORY_DECRYPT_STATS_WORK_ID = "group-history-decrypt-report-stats";

    private static final long MIN_DELAY = DateUtils.DAY_IN_MILLIS;
    private static final int MAX_BATCH_SIZE = 50;

    private static GroupHistoryDecryptReportStats instance;

    private final AppContext appContext;
    private final ContentDb contentDb;
    private final Preferences preferences;
    private final Events events;

    public static GroupHistoryDecryptReportStats getInstance() {
        if (instance == null) {
            synchronized(GroupHistoryDecryptReportStats.class) {
                if (instance == null) {
                    instance = new GroupHistoryDecryptReportStats();
                }
            }
        }
        return instance;
    }

    private GroupHistoryDecryptReportStats() {
        this.appContext = AppContext.getInstance();
        this.contentDb = ContentDb.getInstance();
        this.preferences = Preferences.getInstance();
        this.events = Events.getInstance();
    }

    public void start() {
        Log.d("GroupHistoryDecryptReportStats.start");
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(DecryptReportStatsWorker.class).build();
        WorkManager.getInstance(appContext.get()).enqueueUniqueWork(GROUP_HISTORY_DECRYPT_STATS_WORK_ID, ExistingWorkPolicy.KEEP, workRequest);
    }

    @WorkerThread
    private ListenableWorker.Result run() {
        Log.i("GroupHistoryDecryptReportStats.run");

        long now = System.currentTimeMillis();

        if (!run(now)) {
            Log.i("GroupHistoryDecryptReportStats.run group failure");
            return ListenableWorker.Result.failure();
        }

        Log.i("GroupHistoryDecryptReportStats success");
        return ListenableWorker.Result.success();
    }

    @WorkerThread
    private boolean run(long now) {
        long lastId = preferences.getLastGroupHistoryDecryptStatMessageRowId();
        List<GroupHistoryDecryptStats> stats = contentDb.getGroupHistoryDecryptStats(lastId);
        Log.i("GroupHistoryDecryptReportStats.run lastId: " + lastId);

        if (lastId < 0) {
            Log.i("GroupHistoryDecryptReportStats.run first time running; setting last id to most recent message");
            for (GroupHistoryDecryptStats stat : stats) {
                if (stat.rowId > lastId) {
                    lastId = stat.rowId;
                }
            }

            preferences.setLastGroupHistoryDecryptStatMessageRowId(lastId);

            return true;
        }

        Collections.sort(stats, (o1, o2) -> Long.compare(o1.rowId, o2.rowId));
        List<List<GroupHistoryDecryptStats>> batches = new ArrayList<>();
        List<GroupHistoryDecryptStats> current = new ArrayList<>();
        for (GroupHistoryDecryptStats stat : stats) {
            if (now - stat.addedTimestamp < MIN_DELAY) {
                break;
            } else if (stat.addedTimestamp > 0) {
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
        Log.i("GroupHistoryDecryptReportStats.run made " + batches.size() + " batches");

        for (List<GroupHistoryDecryptStats> batch : batches) {
            List<GroupHistoryReport> reports = new ArrayList<>();
            for (GroupHistoryDecryptStats ds : batch) {
                reports.add(ds.toDecryptionReport());
            }

            try {
                events.sendGroupHistoryDecryptionReports(reports).await();
                long newLastId = batch.get(batch.size() - 1).rowId;

                preferences.setLastGroupHistoryDecryptStatMessageRowId(newLastId);

                Log.i("GroupHistoryDecryptReportStats.run batch succeeded; new lastId is " + newLastId);
            } catch (InterruptedException | ObservableErrorException e) {
                Log.e("GroupHistoryDecryptReportStats.run batch failed", e);
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
            final Result result = GroupHistoryDecryptReportStats.getInstance().run();
            if  (!Result.success().equals(result)) {
                Log.sendErrorReport("GroupHistoryDecryptReportStats failed");
            }
            return result;
        }
    }
}
