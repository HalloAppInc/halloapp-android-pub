package com.halloapp;

import android.content.Context;

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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConnectRetryWorker extends Worker {

    private static final String WORKER_ID = "connect-retry-worker";

    private static final int WAIT_TIMEOUT = 30_000;

    public static void schedule(@NonNull Context context) {
        Constraints.Builder constraintBuilder = new Constraints.Builder();
        constraintBuilder.setRequiredNetworkType(NetworkType.CONNECTED);
        final OneTimeWorkRequest workRequest = (new OneTimeWorkRequest.Builder(ConnectRetryWorker.class))
                .setConstraints(constraintBuilder.build())
                .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS).build();
        WorkManager.getInstance(context).enqueueUniqueWork(WORKER_ID, ExistingWorkPolicy.KEEP, workRequest);
    }

    public ConnectRetryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        final Connection connection = Connection.getInstance();
        Future<Boolean> connectAttempt = connection.connect();
        try {
            if (connectAttempt.get(WAIT_TIMEOUT, TimeUnit.MILLISECONDS)) {
                return Result.success();
            }
            return Result.retry();
        } catch (ExecutionException | InterruptedException e) {
            Log.e("ConnectRetryWorker connectAttempt exception", e);
            return Result.failure();
        } catch (TimeoutException e) {
            Log.e("ConnectRetryWorker connect timeout", e);
            return Result.retry();
        }
    }
}
