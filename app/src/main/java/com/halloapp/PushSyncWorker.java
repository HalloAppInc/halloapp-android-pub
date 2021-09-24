package com.halloapp;

import android.content.Context;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PushSyncWorker extends Worker {

    private static final String PUSH_SYNC_WORKER_ID = "push-connection-worker";

    private static final long MAX_QUEUE_WAIT_TIME = DateUtils.MINUTE_IN_MILLIS * 5;

    static void schedule(@NonNull Context context) {
        Constraints.Builder constraintBuilder = new Constraints.Builder();
        constraintBuilder.setRequiredNetworkType(NetworkType.CONNECTED);
        final OneTimeWorkRequest workRequest = (new OneTimeWorkRequest.Builder(PushSyncWorker.class))
                .setConstraints(constraintBuilder.build())
                .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS).build();
        WorkManager.getInstance(context).enqueueUniqueWork(PUSH_SYNC_WORKER_ID, ExistingWorkPolicy.KEEP, workRequest);
    }

    public PushSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public @NonNull Result doWork() {
        if (isStopped()) {
            return Result.retry();
        }
        Log.i("PushSyncWorker.doWork");
        if (!Preferences.getInstance().getPendingOfflineQueue()) {
            Log.i("PushSyncWorker.doWork no pending queue done!");
            return Result.success();
        }
        final CountDownLatch latch = new CountDownLatch(1);
        final Connection.Observer observer = new Connection.Observer() {
            @Override
            public void onDisconnected() {
                latch.countDown();
            }

            @Override
            public void onOfflineQueueComplete() {
                latch.countDown();
            }
        };
        ConnectionObservers.getInstance().addObserver(observer);
        if (!Preferences.getInstance().getPendingOfflineQueue()) {
            Log.i("PushSyncWorker/doWork no pending queue after attaching observer, done!");
            ConnectionObservers.getInstance().removeObserver(observer);
            return Result.success();
        }
        Log.i("PushSyncWorker/doWork have pending queue, trying to connect");
        Connection.getInstance().connect();
        try {
            latch.await(MAX_QUEUE_WAIT_TIME, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e("PushSyncWorker/doWork interrupted", e);
        }
        ConnectionObservers.getInstance().removeObserver(observer);
        if (!Preferences.getInstance().getPendingOfflineQueue()) {
            return Result.success();
        }
        return Result.retry();
    }
}
