package com.halloapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.ExternalShareInfo;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.content.Reaction;
import com.halloapp.content.SeenReceipt;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.crypto.signal.SignalSessionSetupInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.DownloadMediaTask;
import com.halloapp.media.MediaUploadDownloadThreadPool;
import com.halloapp.media.UploadMediaTask;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

import java.util.Collection;

public class MainContentDbObserver implements ContentDb.Observer {

    private static MainContentDbObserver instance;

    private final Context context;
    private final BgWorkers bgWorkers;
    private final Connection connection;
    private final FileStore fileStore;
    private final ContentDb contentDb;
    private final Notifications notifications;
    private final SignalSessionManager signalSessionManager;

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
        this.bgWorkers = BgWorkers.getInstance();
        this.connection = Connection.getInstance();
        this.fileStore = FileStore.getInstance();
        this.contentDb = ContentDb.getInstance();
        this.notifications = Notifications.getInstance(context);
        this.signalSessionManager = SignalSessionManager.getInstance();
    }

    @Override
    public void onPostAdded(@NonNull Post post) {
        if (post.shouldSend()) {
            if (!post.hasMedia()) {
                connection.sendPost(post);
            } else {
                new UploadMediaTask(post, fileStore, contentDb, connection).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
            }
        } else { // if (post.isIncoming())
            if (post.hasMedia()) {
                DownloadMediaTask.download(post, fileStore, contentDb);
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
            if (post.type == Post.TYPE_MOMENT) {
                contentDb.addMomentEntryPost();
            }
        }
        notifications.updateFeedNotifications(post);
        ExternalShareInfo externalShareInfo = contentDb.getExternalShareInfo(post.id);
        if (externalShareInfo != null && externalShareInfo.shareId != null) {
            connection.revokeSharedPost(externalShareInfo.shareId).onError(e -> {
                Log.w("External share revoke failed", e);
            }).onResponse(res -> {
                contentDb.setExternalShareInfo(post.id, null, null);
            });
        }
    }

    @Override
    public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
    }

    @Override
    public void onPostDeleted(@NonNull Post post) {

    }

    @Override
    public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId, @Nullable GroupId groupId) {
        connection.sendPostSeenReceipt(senderUserId, postId);
    }

    @Override
    public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
    }

    @Override
    public void onIncomingMomentScreenshot(@NonNull UserId senderUserId, @NonNull String postId) {
        connection.sendMomentScreenshotReceipt(senderUserId, postId);
    }

    @Override
    public void onOutgoingMomentScreenshot(@NonNull UserId senderUserId, @NonNull String postId) {
        notifications.updateScreenshotNotifications();
    }

    @Override
    public void onCommentAdded(@NonNull Comment comment) {
        bgWorkers.execute(() -> {
            if (comment.isOutgoing()) {
                if (comment.shouldSend()) {
                    if (!comment.hasMedia()) {
                        connection.sendComment(comment);
                    } else {
                        new UploadMediaTask(comment, fileStore, contentDb, connection).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
                    }
                }
            } else { // if (comment.isIncoming())
                if (comment.hasMedia()) {
                    DownloadMediaTask.download(comment, fileStore, contentDb);
                }
                if (comment.shouldNotify) {
                    notifications.updateFeedNotifications();
                } else {
                    Post parentPost = comment.getParentPost();
                    if (parentPost != null) {
                        if (parentPost.senderUserId.isMe()) {
                            notifications.updateFeedNotifications();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onCommentRetracted(@NonNull Comment comment) {
        bgWorkers.execute(() -> {
            if (comment.senderUserId.isMe()) {
                if (comment.getParentPost() == null || comment.getParentPost().getParentGroup() == null) {
                    connection.retractComment(comment.postId, comment.id);
                } else {
                    connection.retractGroupComment(comment.getParentPost().getParentGroup(), comment.postId, comment.id);
                }
            }
            notifications.updateFeedNotifications(comment);
        });
    }

    @Override
    public void onCommentUpdated(@NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
    }

    @Override
    public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
    }

    @Override
    public void onReactionAdded(@NonNull Reaction reaction, @NonNull ContentItem contentItem) {
        if (contentItem instanceof Message && reaction.senderUserId.isMe()) {
            Message message = (Message)contentItem;
            try {
                SignalSessionSetupInfo signalSessionSetupInfo = signalSessionManager.getSessionSetupInfo((UserId) message.chatId);
                connection.sendChatReaction(reaction, message, signalSessionSetupInfo);
            } catch (Exception e) {
                Log.e("Failed to encrypt chat reaction", e);
            }
        }
    }

    @Override
    public void onReactionRetracted(@NonNull Reaction reaction, @NonNull ContentItem contentItem) {
        if (contentItem instanceof Message && reaction.senderUserId.isMe()) {
            Message message = (Message)contentItem;
            try {
                SignalSessionSetupInfo signalSessionSetupInfo = signalSessionManager.getSessionSetupInfo((UserId) message.chatId);
                Reaction retractReaction = new Reaction(reaction.contentId, reaction.senderUserId, "", reaction.timestamp);
                connection.sendChatReaction(retractReaction, message, signalSessionSetupInfo);
            } catch (Exception e) {
                Log.e("Failed to encrypt chat reaction", e);
            }
        }
    }

    @Override
    public void onMessageAdded(@NonNull Message message) {
        if (message.shouldSend()) {
            if (!message.hasMedia()) {
                bgWorkers.execute(() -> signalSessionManager.sendMessage(message));
            } else {
                new UploadMediaTask(message, fileStore, contentDb, connection).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
            }
        } else { // if (message.isIncoming())
            if (message.hasMedia()) {
                DownloadMediaTask.download(message, fileStore, contentDb);
            }
            notifications.updateMessageNotifications();
        }
    }

    @Override
    public void onMessageRetracted(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        if (senderUserId.isMe()) {
            if (chatId instanceof UserId) {
                connection.retractMessage((UserId) chatId, messageId);
            }
        }
    }

    @Override
    public void onMessageDeleted(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {

    }

    @Override
    public void onMessageUpdated(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {

    }

    @Override
    public void onIncomingMessagePlayed(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        connection.sendMessagePlayedReceipt(chatId, senderUserId, messageId);
    }

    @Override
    public void onGroupFeedAdded(@NonNull GroupId groupId) {

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
    public void onGroupBackgroundChanged(@NonNull GroupId groupId) {
        
    }

    @Override
    public void onOutgoingMessageDelivered(@NonNull ChatId chatId, @NonNull UserId recipientUserId, @NonNull String messageId) {

    }

    @Override
    public void onOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId seenByUserId, @NonNull String messageId) {

    }

    @Override
    public void onMediaPercentTransferred(@NonNull ContentItem contentItem, @NonNull Media media, int percent) {

    }

    @Override
    public void onChatSeen(@NonNull ChatId chatId, @NonNull Collection<SeenReceipt> seenReceipts) {
        for (SeenReceipt seenReceipt : seenReceipts) {
            connection.sendMessageSeenReceipt(seenReceipt.chatId, seenReceipt.senderUserId, seenReceipt.itemId);
        }
    }

    @Override
    public void onGroupSeen(@NonNull GroupId groupId) {

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

    @Override
    public void onArchivedPostRemoved(@NonNull Post post) {

    }

    @Override
    public void onLocalPostSeen(@NonNull String postId) {

    }
}
