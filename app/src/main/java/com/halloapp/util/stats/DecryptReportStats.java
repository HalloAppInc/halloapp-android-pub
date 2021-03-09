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
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

import java.util.Collections;
import java.util.List;

public class DecryptReportStats {

    private static final String DECRYPT_STATS_WORK_ID = "decrypt-report-stats";

    private static final long MIN_DELAY = DateUtils.DAY_IN_MILLIS;

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
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(DecryptTimelineStatsWorker.class).build();
        WorkManager.getInstance(appContext.get()).enqueueUniqueWork(DECRYPT_STATS_WORK_ID, ExistingWorkPolicy.KEEP, workRequest);
    }

    @WorkerThread
    private ListenableWorker.Result run() {
        Log.i("DecryptReportStats.run");

        long now = System.currentTimeMillis();
        long lastId = preferences.getLastDecryptStatMessageRowId();
        List<DecryptStats> stats = contentDb.getMessageDecryptStats(lastId);

        if (lastId < 0) {
            Log.i("DecryptReportStats.run first time running; setting last id to most recent message");
            for (DecryptStats stat : stats) {
                if (stat.rowId > lastId) {
                    lastId = stat.rowId;
                }
            }
            preferences.setLastDecryptStatMessageRowId(lastId);
            return ListenableWorker.Result.success();
        }

        int reports = 0;
        for (DecryptStats stat : stats) {
            if (now - stat.originalTimestamp > MIN_DELAY) {
                reports++;
                events.sendEvent(stat.toDecryptionReport());
                if (stat.rowId > lastId) {
                    lastId = stat.rowId;
                }
            }
        }
        if (reports > 0) {
            preferences.setLastDecryptStatMessageRowId(lastId);
        }

        Log.i("DecryptReportStats reported for " + reports + " messages");
        return ListenableWorker.Result.success();
    }

    public static class DecryptTimelineStatsWorker extends Worker {

        public DecryptTimelineStatsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
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
