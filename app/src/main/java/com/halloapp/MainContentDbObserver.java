package com.halloapp;

import android.content.Context;

import androidx.annotation.NonNull;

import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.content.SeenReceipt;
import com.halloapp.crypto.EncryptedSessionManager;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
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
    private final EncryptedSessionManager encryptedSessionManager;

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
        this.fileStore = FileStore.getInstance();
        this.contentDb = ContentDb.getInstance();
        this.notifications = Notifications.getInstance(context);
        this.encryptedSessionManager = EncryptedSessionManager.getInstance();
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
    public void onPostRetracted(@NonNull Post post) {
        if (post.senderUserId.isMe()) {
            if (post.getParentGroup() == null) {
                connection.retractPost(post.id);
            } else {
                connection.retractGroupPost(post.getParentGroup(), post.id);
            }
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
            Post parentPost = comment.getParentPost();
            if (parentPost != null) {
                if (parentPost.senderUserId.isMe()) {
                    notifications.updateFeedNotifications();
                }
            }
        }
    }

    @Override
    public void onCommentRetracted(@NonNull Comment comment) {
        if (comment.commentSenderUserId.isMe()) {
            if (comment.getParentPost() == null || comment.getParentPost().getParentGroup() == null) {
                connection.retractComment(comment.getPostSenderUserId(), comment.postId, comment.commentId);
            } else {
                connection.retractGroupComment(comment.getParentPost().getParentGroup(), comment.getParentPost().senderUserId, comment.postId, comment.commentId);
            }
        }
    }

    @Override
    public void onCommentUpdated(@NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
    }

    @Override
    public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
    }

    @Override
    public void onMessageAdded(@NonNull Message message) {
        if (message.isOutgoing()) {
            if (message.media.isEmpty()) {
                encryptedSessionManager.sendMessage(message, true);
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
    public void onMessageRetracted(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        if (senderUserId.isMe()) {
            // TODO (ds): implement
            //connection.retractMessage(chatId, messageId);
        }
    }

    @Override
    public void onMessageUpdated(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {

    }

    @Override
    public void onGroupChatAdded(@NonNull GroupId groupId) {

    }

    @Override
    public void onGroupMetadataChanged(@NonNull GroupId groupId) {

    }

    @Override
    public void onGroupMembersChanged(@NonNull GroupId groupId) {

    }

    @Override
    public void onGroupAdminsChanged(@NonNull GroupId groupId) {

    }

    @Override
    public void onOutgoingMessageDelivered(@NonNull ChatId chatId, @NonNull UserId recipientUserId, @NonNull String messageId) {

    }

    @Override
    public void onOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId seenByUserId, @NonNull String messageId) {

    }

    @Override
    public void onChatSeen(@NonNull ChatId chatId, @NonNull Collection<SeenReceipt> seenReceipts) {
        for (SeenReceipt seenReceipt : seenReceipts) {
            connection.sendMessageSeenReceipt(seenReceipt.chatId, seenReceipt.senderUserId, seenReceipt.itemId);
        }
    }

    @Override
    public void onChatDeleted(@NonNull ChatId chatId) {

    }

    @Override
    public void onFeedCleanup() {
    }

    @Override
    public void onDbCreated() {
        // TODO (ds): restore from backup
    }
}
