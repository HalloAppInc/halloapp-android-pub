package com.halloapp;

import android.content.Context;

import androidx.annotation.NonNull;

import com.halloapp.contacts.UserId;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.LoadPostsHistoryWorker;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.content.SeenReceipt;
import com.halloapp.media.DownloadMediaTask;
import com.halloapp.media.MediaUploadDownloadThreadPool;
import com.halloapp.media.UploadMediaTask;
import com.halloapp.xmpp.Connection;

import java.util.Collection;

public class MainContentDbObserver implements ContentDb.Observer {

    private static MainContentDbObserver instance;

    private final Context context;
    private final Connection connection;
    private final FileStore fileStore;
    private final ContentDb contentDb;
    private final Notifications notifications;

    public static MainContentDbObserver getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized(MainContentDbObserver.class) {
                if (instance == null) {
                    instance = new MainContentDbObserver(context);
                }
            }
        }
        return instance;
    }

    private MainContentDbObserver(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.connection = Connection.getInstance();
        this.fileStore = FileStore.getInstance(context);
        this.contentDb = ContentDb.getInstance(context);
        this.notifications = Notifications.getInstance(context);
    }

    @Override
    public void onPostAdded(@NonNull Post post) {
        if (post.isOutgoing()) {
            if (post.media.isEmpty()) {
                connection.sendPost(post);
            } else {
                new UploadMediaTask(post, fileStore, contentDb, connection).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
            }
        } else { // if (post.isIncoming())
            if (!post.media.isEmpty()) {
                new DownloadMediaTask(post, fileStore, contentDb).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
            }
            notifications.updateFeedNotifications();
        }
    }

    @Override
    public void onPostRetracted(@NonNull UserId senderUserId, @NonNull String postId) {
        if (senderUserId.isMe()) {
            connection.retractPost(postId);
        }
    }

    @Override
    public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
    }

    @Override
    public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId) {
        connection.sendPostSeenReceipt(senderUserId, postId);
    }

    @Override
    public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
    }

    @Override
    public void onCommentAdded(@NonNull Comment comment) {
        if (comment.isOutgoing()) {
            connection.sendComment(comment);
        } else { // if (comment.isIncoming())
            if (comment.postSenderUserId.isMe()) {
                notifications.updateFeedNotifications();
            }
        }
    }

    @Override
    public void onCommentRetracted(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
        if (commentSenderUserId.isMe()) {
            connection.retractComment(postSenderUserId, postId, commentId);
        }
    }

    @Override
    public void onCommentUpdated(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
    }

    @Override
    public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
    }

    @Override
    public void onMessageAdded(@NonNull Message message) {
        if (message.isOutgoing()) {
            if (message.media.isEmpty()) {
                connection.sendMessage(message);
            } else {
                new UploadMediaTask(message, fileStore, contentDb, connection).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
            }
        } else { // if (message.isIncoming())
            if (!message.media.isEmpty()) {
                new DownloadMediaTask(message, fileStore, contentDb).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
            }
            notifications.updateMessageNotifications();
        }
    }

    @Override
    public void onMessageRetracted(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        if (senderUserId.isMe()) {
            // TODO (ds): implement
            //connection.retractMessage(chatId, messageId);
        }
    }

    @Override
    public void onMessageUpdated(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {

    }

    @Override
    public void onOutgoingMessageDelivered(@NonNull String chatId, @NonNull UserId recipientUserId, @NonNull String messageId) {

    }

    @Override
    public void onOutgoingMessageSeen(@NonNull String chatId, @NonNull UserId seenByUserId, @NonNull String messageId) {

    }

    @Override
    public void onChatSeen(@NonNull String chatId, @NonNull Collection<SeenReceipt> seenReceipts) {
        for (SeenReceipt seenReceipt : seenReceipts) {
            connection.sendMessageSeenReceipt(seenReceipt.chatId, seenReceipt.senderUserId, seenReceipt.itemId);
        }
    }

    @Override
    public void onChatDeleted(@NonNull String chatId) {

    }

    @Override
    public void onFeedHistoryAdded(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments) {
        for (Post post : historyPosts) {
            if (!post.media.isEmpty()) {
                new DownloadMediaTask(post, fileStore, contentDb).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
            }
        }
    }

    @Override
    public void onFeedCleanup() {
    }

    @Override
    public void onDbCreated() {
        // TODO (ds): restore from backup
        LoadPostsHistoryWorker.loadPostsHistory(context);
    }
}
