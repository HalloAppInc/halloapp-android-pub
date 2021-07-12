package com.halloapp.content;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.halloapp.FileStore;
import com.halloapp.crypto.EncryptedSessionManager;
import com.halloapp.id.UserId;
import com.halloapp.media.DownloadMediaTask;
import com.halloapp.media.MediaUploadDownloadThreadPool;
import com.halloapp.media.UploadMediaTask;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.xmpp.Connection;

import java.util.List;

public class TransferPendingItemsTask extends AsyncTask<Void, Void, Void> {

    private final Connection connection;
    private final FileStore fileStore;
    private final ContentDb contentDb;
    private final EncryptedSessionManager encryptedSessionManager;

    public TransferPendingItemsTask(@NonNull Context context) {
        this.connection = Connection.getInstance();
        this.fileStore = FileStore.getInstance();
        this.contentDb = ContentDb.getInstance();
        this.encryptedSessionManager = EncryptedSessionManager.getInstance();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        final Handler mainHandler = new Handler(Looper.getMainLooper());

        final List<Post> posts = contentDb.getPendingPosts();
        Log.i("TransferPendingItemsTask: " + posts.size() + " posts");
        for (Post post : posts) {
            if (post.isRetracted()) {
                connection.retractPost(post.id);
                continue;
            }
            if (post.isIncoming()) {
                if (!post.media.isEmpty()) {
                    mainHandler.post(() -> new DownloadMediaTask(post, fileStore, contentDb).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR));
                }
            } else /*post.isOutgoing()*/ {
                if (post.media.isEmpty()) {
                    connection.sendPost(post);
                } else {
                    mainHandler.post(() -> new UploadMediaTask(post, fileStore, contentDb, connection).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR));
                }
            }
        }

        final List<Message> messages = contentDb.getPendingMessages();
        Log.i("TransferPendingItemsTask: " + messages.size() + " messages");
        for (Message message : messages) {
            if (message.isIncoming()) {
                if (!message.media.isEmpty()) {
                    mainHandler.post(() -> new DownloadMediaTask(message, fileStore, contentDb).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR));
                } else {
                    contentDb.setMessageTransferred(message.chatId, message.senderUserId, message.id);
                }
            } else /*post.isOutgoing()*/ {
                if (message.isRetracted()) {
                    if (message.chatId instanceof UserId) {
                        connection.retractMessage((UserId) message.chatId, message.id);
                    }
                } else if (message.media.isEmpty()) {
                    encryptedSessionManager.sendMessage(message);
                } else {
                    mainHandler.post(() -> new UploadMediaTask(message, fileStore, contentDb, connection).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR));
                }
            }
        }

        final List<Comment> comments = contentDb.getPendingComments();
        Log.i("TransferPendingItemsTask: " + comments.size() + " comments");
        for (Comment comment : comments) {
            if (comment.isIncoming()) {
                if (!comment.media.isEmpty()) {
                    mainHandler.post(() -> new DownloadMediaTask(comment, fileStore, contentDb).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR));
                } else {
                    contentDb.setCommentTransferred(comment.postId, comment.senderUserId, comment.id);
                }
            } else /*comment.isOutgoing()*/ {
                if (comment.media.isEmpty()) {
                    connection.sendComment(comment);
                } else {
                    mainHandler.post(() -> new UploadMediaTask(comment, fileStore, contentDb, connection).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR));
                }
            }
        }

        final List<SeenReceipt> postSeenReceipts = contentDb.getPendingPostSeenReceipts();
        Log.i("TransferPendingItemsTask: " + postSeenReceipts.size() + " post seen receipts");
        for (SeenReceipt receipt : postSeenReceipts) {
            connection.sendPostSeenReceipt(receipt.senderUserId, receipt.itemId);
        }

        final List<SeenReceipt> messageSeenReceipts = contentDb.getPendingMessageSeenReceipts();
        Log.i("TransferPendingItemsTask: " + messageSeenReceipts.size() + " message seen receipts");
        for (SeenReceipt receipt : messageSeenReceipts) {
            connection.sendMessageSeenReceipt(receipt.chatId, receipt.senderUserId, receipt.itemId);
        }
        return null;
    }
}
