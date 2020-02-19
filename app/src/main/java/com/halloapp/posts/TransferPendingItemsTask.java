package com.halloapp.posts;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Preconditions;

import com.halloapp.media.DownloadPostTask;
import com.halloapp.media.MediaStore;
import com.halloapp.media.MediaUploadDownloadThreadPool;
import com.halloapp.media.UploadPostTask;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;

import java.util.List;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

public class TransferPendingItemsTask extends AsyncTask<Void, Void, Void> {

    private final Connection connection;
    private final MediaStore mediaStore;
    private final PostsDb postsDb;

    public TransferPendingItemsTask(@NonNull Context context) {
        this.connection = Connection.getInstance();
        this.mediaStore = MediaStore.getInstance(context);
        this.postsDb = PostsDb.getInstance(context);
    }

    @Override
    protected Void doInBackground(Void... voids) {

        final List<Post> posts = postsDb.getPendingPosts();
        Log.i("TransferPendingItemsTask: " + posts.size() + " posts");
        for (Post post : posts) {
            if (post.isIncoming()) {
                if (!post.media.isEmpty()) {
                    new DownloadPostTask(post, mediaStore, postsDb).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
                }
            } else /*post.isOutgoing()*/ {
                if (post.media.isEmpty()) {
                    connection.sendPost(post);
                } else {
                    new UploadPostTask(post, mediaStore, postsDb, connection).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
                }
            }
        }

        final List<Comment> comments = postsDb.getPendingComments();
        Log.i("TransferPendingItemsTask: " + comments.size() + " comments");
        for (Comment comment : comments) {
            Preconditions.checkArgument(comment.isOutgoing());
            Preconditions.checkArgument(!comment.transferred);
            connection.sendComment(comment);
        }
        return null;
    }
}
