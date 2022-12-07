package com.halloapp.katchup;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.Preferences;
import com.halloapp.props.ServerProps;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.util.concurrent.TimeUnit;

public class RelationshipSyncWorker extends Worker {

    private static final String CONTACT_SYNC_WORKER_ID = "relationship-sync-worker";

    public static void schedule(@NonNull Context context) {
        long lastFullSync = Preferences.getInstance().getLastFullRelationshipSyncTime();
        long syncDelayMs = ServerProps.getInstance().getRelationshipSyncIntervalSeconds() * 1000L;

        long timeSince = System.currentTimeMillis() - lastFullSync;
        long delay = Math.max(0, syncDelayMs - timeSince);

        Constraints.Builder constraintBuilder = new Constraints.Builder();
        constraintBuilder.setRequiredNetworkType(NetworkType.CONNECTED);
        final PeriodicWorkRequest workRequest = (new PeriodicWorkRequest.Builder(RelationshipSyncWorker.class, syncDelayMs, TimeUnit.MILLISECONDS))
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(constraintBuilder.build()).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(CONTACT_SYNC_WORKER_ID, ExistingPeriodicWorkPolicy.REPLACE, workRequest);
    }

    public RelationshipSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public @NonNull Result doWork() {
        if (isStopped()) {
            return Result.failure();
        }
        Log.i("RelationshipSyncWorker.doWork");
        long lastFullSync = Preferences.getInstance().getLastFullRelationshipSyncTime();
        long syncDelayMs = ServerProps.getInstance().getRelationshipSyncIntervalSeconds() * 1000L;

        long timeSince = System.currentTimeMillis() - lastFullSync;
        if (timeSince > syncDelayMs) {
            Log.i("RelationshipSyncWorker.doWork starting full sync");
            return performSync();
        } else {
            schedule(getApplicationContext());
        }
        return Result.success();
    }

    private Result performSync() {
        Connection connection = Connection.getInstance();
        try {
            connection.requestRelationshipList().await();
        } catch (ObservableErrorException | InterruptedException e) {
            Log.e("Connection failed during relationship sync", e);
            return Result.failure();
        }

        return Result.success();
    }
}
