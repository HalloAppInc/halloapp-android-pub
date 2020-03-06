package com.halloapp.posts;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.core.util.Preconditions;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

// TODO (ds): remove
public class LoadPostsHistoryWorker extends Worker {

    private static final String LOAD_POSTS_HISTORY_WORK_ID = "load-posts-history";

    public static void loadPostsHistory(@NonNull Context context) {
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(LoadPostsHistoryWorker.class).build();
        WorkManager.getInstance(context).enqueueUniqueWork(LOAD_POSTS_HISTORY_WORK_ID, ExistingWorkPolicy.REPLACE, workRequest);
    }

    public LoadPostsHistoryWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public @NonNull Result doWork() {
        try {
            final Pair<Collection<Post>, Collection<Comment>> result = Connection.getInstance().getFeedHistory().get();
            if (result == null) {
                Log.e("LoadPostsHistoryWorker: failed retrieve feed history");
                return ListenableWorker.Result.failure();
            }
            PostsDb.getInstance(getApplicationContext()).addHistory(Preconditions.checkNotNull(result.first), Preconditions.checkNotNull(result.second));
            return ListenableWorker.Result.success();
        } catch (ExecutionException | InterruptedException e) {
            Log.e("LoadPostsHistoryWorker: failed retrieve feed history", e);
            return ListenableWorker.Result.failure();
        }
    }
}
