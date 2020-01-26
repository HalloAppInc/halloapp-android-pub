package com.halloapp.posts;

import android.content.Context;

import androidx.annotation.NonNull;

import com.halloapp.Connection;
import com.halloapp.media.DownloadPostTask;
import com.halloapp.media.MediaStore;
import com.halloapp.media.MediaUploadDownloadThreadPool;
import com.halloapp.media.UploadPostTask;
import com.halloapp.util.Log;

import java.util.List;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

public class SendPendingPostsTask extends AsyncTask<Void, Void, Void> {

    private final Connection connection;
    private final MediaStore mediaStore;
    private final PostsDb postsDb;

    public SendPendingPostsTask(@NonNull Context context) {
        this.connection = Connection.getInstance();
        this.mediaStore = MediaStore.getInstance(context);
        this.postsDb = PostsDb.getInstance(context);
    }

    @Override
    protected Void doInBackground(Void... voids) {

        final List<Post> posts = postsDb.getPendingPosts();
        Log.i("SendPendingPostsTask: " + posts.size() + " posts");
        for (Post post : posts) {
            if (post.isIncoming()) {
                if (!post.media.isEmpty()) {
                    new DownloadPostTask(post, mediaStore, postsDb).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
                }
            } else /*post.isOutgoing()*/ {
                if (post.media.isEmpty()) {
                    connection.sendPost(post);
                } else {
                    new UploadPostTask(post, connection, postsDb).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
                }
            }
        }

        return null;
    }
}
