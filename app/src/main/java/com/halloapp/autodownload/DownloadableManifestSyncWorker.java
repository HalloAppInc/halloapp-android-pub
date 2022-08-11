package com.halloapp.autodownload;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.AppContext;
import com.halloapp.FileStore;
import java.util.concurrent.TimeUnit;

public class DownloadableManifestSyncWorker extends Worker {

    private static final String AUTO_DOWNLOAD_WORKER_ID = "auto-download-worker";

    public static void schedule(@NonNull Context context) {
        Constraints.Builder constraintBuilder = new Constraints.Builder();
        constraintBuilder.setRequiredNetworkType(NetworkType.CONNECTED);
        final OneTimeWorkRequest workRequest = (new OneTimeWorkRequest.Builder(DownloadableManifestSyncWorker.class))
                .setConstraints(constraintBuilder.build())
                .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        WorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
                        TimeUnit.MILLISECONDS).build();
        WorkManager.getInstance(context).enqueueUniqueWork(AUTO_DOWNLOAD_WORKER_ID, ExistingWorkPolicy.KEEP, workRequest);
    }

    private final FileStore fileStore;
    private final DownloadableAssetManager manager;
    private final AppContext appContext;

    public DownloadableManifestSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        fileStore = FileStore.getInstance();
        manager = DownloadableAssetManager.getInstance();
        appContext = AppContext.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        return manager.downloadManifest();
    }

}
