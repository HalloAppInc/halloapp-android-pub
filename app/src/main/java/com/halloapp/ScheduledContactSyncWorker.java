package com.halloapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.contacts.ContactsSync;
import com.halloapp.props.ServerProps;
import com.halloapp.util.logs.Log;

import java.util.concurrent.TimeUnit;

public class ScheduledContactSyncWorker extends Worker {

    private static final String CONTACT_SYNC_WORKER_ID = "scheduled-contact-sync-worker";

    static void schedule(@NonNull Context context) {
        long lastFullSync = Preferences.getInstance().getLastContactsSyncTime();
        long syncDelayMs = ServerProps.getInstance().getContactSyncIntervalSeconds() * 1000L;

        long timeSince = System.currentTimeMillis() - lastFullSync;
        long delay = Math.max(0, syncDelayMs - timeSince);

        Constraints.Builder constraintBuilder = new Constraints.Builder();
        constraintBuilder.setRequiredNetworkType(NetworkType.CONNECTED);
        final PeriodicWorkRequest workRequest = (new PeriodicWorkRequest.Builder(ScheduledContactSyncWorker.class, syncDelayMs, TimeUnit.MILLISECONDS))
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(constraintBuilder.build()).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(CONTACT_SYNC_WORKER_ID, ExistingPeriodicWorkPolicy.REPLACE, workRequest);
    }

    public ScheduledContactSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public @NonNull Result doWork() {
        if (isStopped()) {
            return Result.failure();
        }
        Log.i("ScheduledContactSyncWorker.doWork");
        long lastFullSync = Preferences.getInstance().getLastContactsSyncTime();
        long syncDelayMs = ServerProps.getInstance().getContactSyncIntervalSeconds() * 1000L;

        long timeSince = System.currentTimeMillis() - lastFullSync;
        if (timeSince > syncDelayMs) {
            Log.i("ScheduledContactSyncWorker.doWork starting full sync");
            ContactsSync.getInstance().startContactsSync(true);
        } else {
            schedule(getApplicationContext());
        }
        return Result.success();
    }
}
