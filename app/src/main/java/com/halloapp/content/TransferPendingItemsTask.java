package com.halloapp.content;

import android.content.Context;

import androidx.annotation.NonNull;

import com.halloapp.FileStore;
import com.halloapp.media.DownloadMediaTask;
import com.halloapp.media.MediaUploadDownloadThreadPool;
import com.halloapp.media.UploadMediaTask;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.xmpp.Connection;

import java.util.List;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

public class TransferPendingItemsTask extends AsyncTask<Void, Void, Void> {

    private final Connection connection;
    private final FileStore fileStore;
    private final ContentDb contentDb;

    public TransferPendingItemsTask(@NonNull Context context) {
        this.connection = Connection.getInstance();
        this.fileStore = FileStore.getInstance(context);
        this.contentDb = ContentDb.getInstance(context);
    }

    @Override
    protected Void doInBackground(Void... voids) {

        final List<Post> posts = contentDb.getPendingPosts();
        Log.i("TransferPendingItemsTask: " + posts.size() + " posts");
        for (Post post : posts) {
            if (post.isIncoming()) {
                if (!post.media.isEmpty()) {
                    new DownloadMediaTask(post, fileStore, contentDb).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
                }
            } else /*post.isOutgoing()*/ {
                if (post.media.isEmpty()) {
                    connection.sendPost(post);
                } else {
                    new UploadMediaTask(post, fileStore, contentDb, connection).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
                }
            }
        }

        final List<Message> messages = contentDb.getPendingMessages();
        Log.i("TransferPendingItemsTask: " + messages.size() + " messages");
        for (Message message : messages) {
            if (message.isIncoming()) {
                if (!message.media.isEmpty()) {
                    new DownloadMediaTask(message, fileStore, contentDb).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
                }
            } else /*post.isOutgoing()*/ {
                if (message.media.isEmpty()) {
                    connection.sendMessage(message);
                } else {
                    new UploadMediaTask(message, fileStore, contentDb, connection).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
                }
            }
        }

        final List<Comment> comments = contentDb.getPendingComments();
        Log.i("TransferPendingItemsTask: " + comments.size() + " comments");
        for (Comment comment : comments) {
            Preconditions.checkArgument(comment.isOutgoing());
            Preconditions.checkArgument(!comment.transferred);
            connection.sendComment(comment);
        }

        final List<Receipt> seenReceipts = contentDb.getPendingPostSeenReceipts();
        Log.i("TransferPendingItemsTask: " + seenReceipts.size() + " seen receipts");
        for (Receipt receipt : seenReceipts) {
            connection.sendSeenReceipt(receipt.senderUserId, receipt.postId);
        }
        return null;
    }
}
