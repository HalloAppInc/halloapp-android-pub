package com.halloapp;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.contacts.FriendshipInfo;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.content.PostsManager;
import com.halloapp.content.Reaction;
import com.halloapp.content.ReactionComment;
import com.halloapp.content.ReactionMessage;
import com.halloapp.content.TransferPendingItemsTask;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.group.GroupFeedSessionManager;
import com.halloapp.crypto.home.HomeFeedSessionManager;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.keys.PublicXECKey;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.crypto.signal.SignalSessionSetupInfo;
import com.halloapp.crypto.web.WebClientManager;
import com.halloapp.groups.GroupInfo;
import com.halloapp.groups.GroupsSync;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.privacy.BlockListManager;
import com.halloapp.privacy.FeedPrivacyManager;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.Background;
import com.halloapp.proto.clients.EncryptedPayload;
import com.halloapp.proto.clients.GroupHistoryPayload;
import com.halloapp.proto.clients.SenderKey;
import com.halloapp.proto.clients.SenderState;
import com.halloapp.proto.server.ContentMissing;
import com.halloapp.proto.server.ExpiryInfo;
import com.halloapp.proto.server.FriendListRequest;
import com.halloapp.proto.server.GroupFeedItems;
import com.halloapp.proto.server.GroupFeedRerequest;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.HalloappProfileUpdate;
import com.halloapp.proto.server.HalloappUserProfile;
import com.halloapp.proto.server.HistoryResend;
import com.halloapp.proto.server.HomeFeedRerequest;
import com.halloapp.proto.server.IdentityKey;
import com.halloapp.proto.server.MomentNotification;
import com.halloapp.proto.server.Rerequest;
import com.halloapp.proto.server.SenderStateWithKeyInfo;
import com.halloapp.ui.AppExpirationActivity;
import com.halloapp.ui.DeleteAccountActivity;
import com.halloapp.RegistrationRequestActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.util.TimeUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.DecryptReportStats;
import com.halloapp.util.stats.GroupCommentDecryptReportStats;
import com.halloapp.util.stats.GroupHistoryDecryptReportStats;
import com.halloapp.util.stats.GroupPostDecryptReportStats;
import com.halloapp.util.stats.HomeCommentDecryptReportStats;
import com.halloapp.util.stats.HomePostDecryptReportStats;
import com.halloapp.xmpp.ChatState;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ContactInfo;
import com.halloapp.xmpp.PresenceManager;
import com.halloapp.xmpp.ProtoPrinter;
import com.halloapp.xmpp.WhisperKeysMessage;
import com.halloapp.xmpp.groups.GroupsApi;
import com.halloapp.xmpp.groups.MemberElement;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainConnectionObserver extends Connection.Observer {

    private static MainConnectionObserver instance;

    private final Context context;

    private final Me me;
    private final BgWorkers bgWorkers;
    private final ContentDb contentDb;
    private final GroupsApi groupsApi;
    private final Connection connection;
    private final ContactsDb contactsDb;
    private final GroupsSync groupsSync;
    private final Preferences preferences;
    private final AvatarLoader avatarLoader;
    private final PostsManager postsManager;
    private final Notifications notifications;
    private final ForegroundChat foregroundChat;
    private final PresenceManager presenceManager;
    private final BlockListManager blockListManager;
    private final WebClientManager webClientManager;
    private final EncryptedKeyStore encryptedKeyStore;
    private final FeedPrivacyManager feedPrivacyManager;
    private final ForegroundObserver foregroundObserver;
    private final DecryptReportStats decryptReportStats;
    private final SignalSessionManager signalSessionManager;
    private final HomeFeedSessionManager homeFeedSessionManager;
    private final GroupFeedSessionManager groupFeedSessionManager;
    private final HomePostDecryptReportStats homePostDecryptReportStats;
    private final GroupPostDecryptReportStats groupPostDecryptReportStats;
    private final HomeCommentDecryptReportStats homeCommentDecryptReportStats;
    private final GroupCommentDecryptReportStats groupCommentDecryptReportStats;
    private final GroupHistoryDecryptReportStats groupHistoryDecryptReportStats;

    public static MainConnectionObserver getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (MainConnectionObserver.class) {
                if (instance == null) {
                    instance = new MainConnectionObserver(
                            context,
                            Me.getInstance(),
                            BgWorkers.getInstance(),
                            ContentDb.getInstance(),
                            GroupsApi.getInstance(),
                            Connection.getInstance(),
                            ContactsDb.getInstance(),
                            GroupsSync.getInstance(context),
                            Preferences.getInstance(),
                            AvatarLoader.getInstance(),
                            PostsManager.getInstance(),
                            Notifications.getInstance(context),
                            ForegroundChat.getInstance(),
                            PresenceManager.getInstance(),
                            BlockListManager.getInstance(),
                            WebClientManager.getInstance(),
                            EncryptedKeyStore.getInstance(),
                            FeedPrivacyManager.getInstance(),
                            ForegroundObserver.getInstance(),
                            DecryptReportStats.getInstance(),
                            SignalSessionManager.getInstance(),
                            HomeFeedSessionManager.getInstance(),
                            GroupFeedSessionManager.getInstance(),
                            HomePostDecryptReportStats.getInstance(),
                            GroupPostDecryptReportStats.getInstance(),
                            HomeCommentDecryptReportStats.getInstance(),
                            GroupCommentDecryptReportStats.getInstance(),
                            GroupHistoryDecryptReportStats.getInstance());
                }
            }
        }
        return instance;
    }


    MainConnectionObserver(
            @NonNull Context context,
            @NonNull Me me,
            @NonNull BgWorkers bgWorkers,
            @NonNull ContentDb contentDb,
            @NonNull GroupsApi groupsApi,
            @NonNull Connection connection,
            @NonNull ContactsDb contactsDb,
            @NonNull GroupsSync groupsSync,
            @NonNull Preferences preferences,
            @NonNull AvatarLoader avatarLoader,
            @NonNull PostsManager postsManager,
            @NonNull Notifications notifications,
            @NonNull ForegroundChat foregroundChat,
            @NonNull PresenceManager presenceManager,
            @NonNull BlockListManager blockListManager,
            @NonNull WebClientManager webClientManager,
            @NonNull EncryptedKeyStore encryptedKeyStore,
            @NonNull FeedPrivacyManager feedPrivacyManager,
            @NonNull ForegroundObserver foregroundObserver,
            @NonNull DecryptReportStats decryptReportStats,
            @NonNull SignalSessionManager signalSessionManager,
            @NonNull HomeFeedSessionManager homeFeedSessionManager,
            @NonNull GroupFeedSessionManager groupFeedSessionManager,
            @NonNull HomePostDecryptReportStats homePostDecryptReportStats,
            @NonNull GroupPostDecryptReportStats groupPostDecryptReportStats,
            @NonNull HomeCommentDecryptReportStats homeCommentDecryptReportStats,
            @NonNull GroupCommentDecryptReportStats groupCommentDecryptReportStats,
            @NonNull GroupHistoryDecryptReportStats groupHistoryDecryptReportStats) {
        this.context = context.getApplicationContext();

        this.me = me;
        this.bgWorkers = bgWorkers;
        this.contentDb = contentDb;
        this.groupsApi = groupsApi;
        this.connection = connection;
        this.contactsDb = contactsDb;
        this.groupsSync = groupsSync;
        this.preferences = preferences;
        this.avatarLoader = avatarLoader;
        this.postsManager = postsManager;
        this.notifications = notifications;
        this.foregroundChat = foregroundChat;
        this.presenceManager = presenceManager;
        this.blockListManager = blockListManager;
        this.webClientManager = webClientManager;
        this.encryptedKeyStore = encryptedKeyStore;
        this.feedPrivacyManager = feedPrivacyManager;
        this.foregroundObserver = foregroundObserver;
        this.decryptReportStats = decryptReportStats;
        this.signalSessionManager = signalSessionManager;
        this.homeFeedSessionManager = homeFeedSessionManager;
        this.groupFeedSessionManager = groupFeedSessionManager;
        this.homePostDecryptReportStats = homePostDecryptReportStats;
        this.groupPostDecryptReportStats = groupPostDecryptReportStats;
        this.homeCommentDecryptReportStats = homeCommentDecryptReportStats;
        this.groupCommentDecryptReportStats = groupCommentDecryptReportStats;
        this.groupHistoryDecryptReportStats = groupHistoryDecryptReportStats;
    }

    @Override
    public void onConnected() {
        bgWorkers.execute(blockListManager::fetchInitialBlockList);
        bgWorkers.execute(feedPrivacyManager::fetchInitialFeedPrivacy);
        bgWorkers.execute(postsManager::ensurePostsShared);

        new TransferPendingItemsTask(context).execute();
        PushMessagingService.updateFirebasePushTokenIfNeeded();
        HuaweiMessagingService.updateHuaweiPushTokenIfNeeded();
        presenceManager.onReconnect();
        groupsSync.startGroupsSync();
        decryptReportStats.start();
        groupPostDecryptReportStats.start();
        groupCommentDecryptReportStats.start();
        groupHistoryDecryptReportStats.start();
        homePostDecryptReportStats.start();
        homeCommentDecryptReportStats.start();
        webClientManager.reconnect();
    }

    @Override
    public void onDisconnected() {
        presenceManager.onDisconnect();
        if (foregroundObserver.isInForeground()) {
            Log.i("MainConnectionObserver/onDisconnected still in foreground, reconnecting...");
            connection.connect();
        }
    }

    @Override
    public void onOfflineQueueComplete(String ackId) {
        connection.sendAck(ackId);
    }

    @Override
    public void onLoginFailed(boolean accountDeleted) {
        me.resetRegistration();
        blockListManager.onLoginFailed();
        feedPrivacyManager.onLoginFailed();

        boolean isInForeground = foregroundObserver.isInForeground();
        if (!accountDeleted) {
            if (isInForeground) {
                RegistrationRequestActivity.reVerify(context);
            } else {
                notifications.showLoginFailedNotification();
            }
        } else {
            if (isInForeground) {
                context.startActivity(new Intent(context, RegistrationRequestActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            } else {
                DeleteAccountActivity.deleteAllUserData();
            }
        }
    }

    @Override
    public void onClientVersionExpiringSoon(int daysLeft) {
        if (foregroundObserver.isInForeground()) {
            AppExpirationActivity.open(context, daysLeft);
        } else {
            notifications.showExpirationNotification(daysLeft);
        }
    }

    @Override
    public void onOutgoingPostSent(@NonNull String postId, @Nullable byte[] protoHash) {
        contentDb.setPostTransferred(UserId.ME, postId);
        contentDb.setPostProtoHash(UserId.ME, postId, protoHash);
    }

    @Override
    public void onIncomingFeedItemsReceived(@NonNull List<Post> posts, @NonNull List<Comment> comments, @NonNull String ackId) {
        contentDb.addFeedItems(posts, comments, () -> connection.sendAck(ackId));
    }

    @Override
    public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId, long timestamp, @NonNull String ackId) {
        contentDb.setOutgoingPostSeen(seenByUserId, postId, timestamp, () -> connection.sendAck(ackId));
    }

    @Override
    public void onOutgoingMomentScreenshotted(@NonNull UserId seenByUserId, @NonNull String postId, long timestamp, @NonNull String ackId) {
        contentDb.setOutgoingMomentScreenshotted(seenByUserId, postId, timestamp, () -> connection.sendAck(ackId));
    }

    @Override
    public void onOutgoingCommentSent(@NonNull String postId, @NonNull String commentId, @Nullable byte[] protoHash) {
        contentDb.setCommentTransferred(postId, UserId.ME, commentId);
        contentDb.setCommentProtoHash(postId, UserId.ME, commentId, protoHash);
    }

    @Override
    public void onAudienceHashMismatch(@NonNull ContentItem contentItem) {
        GroupId groupId = contentItem instanceof Post
                ? ((Post)contentItem).getParentGroup()
                : contentItem instanceof Comment ? ((Comment) contentItem).getParentPost().getParentGroup() : null;

        if (groupId == null) {
            Log.e("Audience hash mismatch could not determine group for item " + contentItem);
            return;
        }

        bgWorkers.execute(() -> {
            try {
                Map<UserId, PublicEdECKey> keys = groupsApi.getGroupKeys(groupId).await();
                List<MemberInfo> localMembers = contentDb.getGroupMembers(groupId);

                boolean foundMismatch = false;
                Set<UserId> remoteUids = keys.keySet();
                Set<UserId> localUids = new HashSet<>();
                for (MemberInfo memberInfo : localMembers) {
                    localUids.add(memberInfo.userId);
                }

                if (remoteUids.size() != localMembers.size() || !remoteUids.containsAll(localUids)) {
                    Log.i("Found member list mismatch; syncing group");
                    foundMismatch = GroupsSync.getInstance(context).performSingleGroupSync(Preconditions.checkNotNull(groupId));
                }

                for (UserId userId : remoteUids) {
                    if (userId.isMe()) {
                        continue;
                    }
                    byte[] remoteIdentityKey = Preconditions.checkNotNull(keys.get(userId)).getKeyMaterial();
                    byte[] localIdentityKey = null;
                    try {
                        localIdentityKey = encryptedKeyStore.getPeerPublicIdentityKey(userId).getKeyMaterial();
                    } catch (CryptoException e) {
                        Log.w("Failed to get local copy of peer identity key for " + userId, e);
                    }

                    if (!Arrays.equals(remoteIdentityKey, localIdentityKey)) {
                        Log.i("Remote and local identity key do not match for " + userId + "; remote: " + StringUtils.bytesToHexString(remoteIdentityKey) + "; local: " + StringUtils.bytesToHexString(localIdentityKey) + "; overwriting");
                        encryptedKeyStore.edit().setPeerPublicIdentityKey(userId, new PublicEdECKey(remoteIdentityKey)).apply();
                        foundMismatch = true;
                    }
                }

                if (foundMismatch) {
                    groupFeedSessionManager.tearDownOutboundSession(groupId);
                    if (contentItem instanceof Post) {
                        connection.sendPost((Post)contentItem);
                    } else if (contentItem instanceof Comment) {
                        connection.sendComment((Comment)contentItem);
                    } else {
                        Log.e("Found mismatch for unknown content item " + contentItem);
                    }
                } else {
                    Log.w("Server said audience does not match but failed to find mismatch; not re-sending");
                    Log.sendErrorReport("no_local_audience_mismatch");
                }
            } catch (ObservableErrorException | InterruptedException e) {
                Log.e("Failed to get keys for group", e);
            }
        });
    }

    @Override
    public void onIncomingPostSeenReceiptSent(@NonNull UserId senderUserId, @NonNull String postId) {
        contentDb.setPostSeenReceiptSent(senderUserId, postId);
    }

    @Override
    public void onIncomingMomentScreenshotReceiptSent(@NonNull UserId senderUserId, @NonNull String postId) {
        contentDb.setMomentScreenshotReceiptSent(senderUserId, postId);
    }

    @Override
    public void onOutgoingMessageSent(@NonNull ChatId chatId, @NonNull String messageId) {
        contentDb.setMessageTransferred(chatId, UserId.ME, messageId);
    }

    @Override
    public void onOutgoingMessageDelivered(@NonNull ChatId chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {
        contentDb.setOutgoingMessageDelivered(chatId, userId, id, timestamp, () -> connection.sendAck(stanzaId));
    }

    @Override
    public void onOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {
        contentDb.setOutgoingMessageSeen(chatId, userId, id, timestamp, () -> connection.sendAck(stanzaId));
    }

    @Override
    public void onOutgoingMessagePlayed(@NonNull ChatId chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {
        contentDb.setOutgoingMessagePlayed(chatId, userId, id, timestamp, () -> connection.sendAck(stanzaId));
    }

    @Override
    public void onIncomingMessageReceived(@NonNull Message message) {
        final boolean isMessageForForegroundChat = foregroundChat.isForegroundChatId(message.chatId);
        final Runnable completionRunnable = () -> {
            if (isMessageForForegroundChat) {
                connection.sendMessageSeenReceipt(message.chatId, message.senderUserId, message.id);
            }
            connection.sendAck(message.id);
        };
        if (message instanceof ReactionMessage) {
            contentDb.addReaction((ReactionMessage)message, completionRunnable);
        } else if (message.isTombstone() || !message.isRetracted()) {
            contentDb.addMessage(message, !isMessageForForegroundChat, completionRunnable);
        } else {
            contentDb.retractMessage(message, completionRunnable);
        }
    }

    @Override
    public void onIncomingMessageSeenReceiptSent(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        contentDb.setMessageSeenReceiptSent(chatId, senderUserId, messageId);
    }

    @Override
    public void onIncomingMessagePlayedReceiptSent(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        contentDb.setMessagePlayedReceiptSent(chatId, senderUserId, messageId);
    }

    @Override
    public void onMessageRerequest(@NonNull Rerequest.ContentType contentType, @NonNull UserId peerUserId, @NonNull String messageId, @NonNull PublicEdECKey peerIdentityKey, @Nullable Integer otpkId, @NonNull byte[] sessionSetupKey, @NonNull byte[] messageEphemeralKey, @NonNull String stanzaId) {
        bgWorkers.execute(() -> {
             byte[] lastMessageEphemeralKey = encryptedKeyStore.getInboundTeardownKey(peerUserId);
             if (!Arrays.equals(lastMessageEphemeralKey, messageEphemeralKey)) {
                 encryptedKeyStore.edit().setInboundTeardownKey(peerUserId, messageEphemeralKey).apply();
                 signalSessionManager.tearDownSession(peerUserId);
                 if (sessionSetupKey.length == 0) {
                    Log.i("Got empty session setup key; cannot process rereq session setup from older client");
                 } else {
                     try {
                         signalSessionManager.receiveRerequestSetup(peerUserId, new PublicXECKey(sessionSetupKey), 1, new SignalSessionSetupInfo(peerIdentityKey, otpkId));
                         encryptedKeyStore.edit().setPeerResponded(peerUserId, true).apply();
                     } catch (CryptoException e) {
                         Log.e("Failed to reset session on message rerequest", e);
                         Log.sendErrorReport("Rerequest reset failed");
                     }
                 }
             }
             if (Rerequest.ContentType.GROUP_HISTORY.equals(contentType)) {
                 int rerequestCount = contentDb.getHistoryResendRerequestCount(peerUserId, messageId);
                 if (rerequestCount >= Constants.MAX_REREQUESTS_PER_MESSAGE) {
                     Log.w("Reached rerequest limit for group history " + messageId);
                     checkIdentityKey();
                 } else {
                     contentDb.setHistoryResendRerequestCount(peerUserId, messageId, rerequestCount + 1);
                     byte[] payload = contentDb.getHistoryResendPayload(messageId);

                     if (payload == null) {
                         Log.w("Could not find payload matching " + messageId);
                         connection.sendMissingContentNotice(ContentMissing.ContentType.HISTORY_RESEND, messageId, peerUserId);
                     } else {
                         try {
                             GroupFeedItems groupFeedItems = GroupFeedItems.parseFrom(payload);
                             groupsApi.sendGroupHistoryResend(new GroupId(groupFeedItems.getGid()), peerUserId, messageId, payload);
                         } catch (InvalidProtocolBufferException e) {
                             Log.e("Failed to encrypt group history payload", e);
                         }
                     }
                 }
             } else if (Rerequest.ContentType.CHAT_REACTION.equals(contentType)) {
                 Reaction reaction = contentDb.getReaction(messageId);
                 if (reaction == null) {
                     Log.w("Could not find chat reaction matching " + messageId);
                     connection.sendMissingContentNotice(ContentMissing.ContentType.CHAT_REACTION, messageId, peerUserId);
                 } else {
                     int rerequestCount = contentDb.getOutboundMessageRerequestCount(peerUserId, messageId);
                     if (rerequestCount >= Constants.MAX_REREQUESTS_PER_MESSAGE) {
                         Log.w("Reached rerequest limit for chat reaction " + messageId + " for user " + peerUserId);
                         checkIdentityKey();
                     } else {
                         contentDb.setOutboundMessageRerequestCount(peerUserId, messageId, rerequestCount + 1);
                         Message message = contentDb.getMessage(reaction.contentId);
                         try {
                             SignalSessionSetupInfo signalSessionSetupInfo = signalSessionManager.getSessionSetupInfo((UserId) message.chatId);
                             connection.sendChatReaction(reaction, message, signalSessionSetupInfo);
                         } catch (Exception e) {
                             Log.e("Failed to encrypt chat reaction", e);
                         }
                     }
                 }
             } else {
                 Message message = contentDb.getMessage(peerUserId, UserId.ME, messageId);
                 if (message == null) {
                     ContentMissing.ContentType contentMissingType = contentType.equals(Rerequest.ContentType.CHAT) ? ContentMissing.ContentType.CHAT : ContentMissing.ContentType.CALL;
                     connection.sendMissingContentNotice(contentMissingType, messageId, peerUserId);
                 } else {
                     int rerequestCount = contentDb.getOutboundMessageRerequestCount(peerUserId, messageId);
                     if (rerequestCount >= Constants.MAX_REREQUESTS_PER_MESSAGE) {
                         Log.w("Reached rerequest limit for message " + messageId + " for user " + peerUserId);
                         checkIdentityKey();
                     } else {
                         contentDb.setOutboundMessageRerequestCount(peerUserId, messageId, rerequestCount + 1);
                         signalSessionManager.sendMessage(message);
                     }
                 }
             }
             connection.sendAck(stanzaId);
        });
    }

    @Override
    public void onGroupFeedRerequest(@NonNull GroupFeedRerequest.ContentType contentType, @NonNull UserId senderUserId, @NonNull GroupId groupId, @NonNull String contentId, boolean senderStateIssue, @NonNull String stanzaId) {
        bgWorkers.execute(() -> {
            if (senderStateIssue) {
                signalSessionManager.tearDownSession(senderUserId);
            }
            if (GroupFeedRerequest.ContentType.POST.equals(contentType)) {
                Post post = contentDb.getPost(contentId);
                if (post != null && groupId.equals(post.getParentGroup())) {
                    if (post.isRetracted()) {
                        Log.i("Rerequested post " + contentId + " has been retracted; sending another retract");
                        connection.retractRerequestedGroupPost(groupId, post.id, senderUserId);
                        connection.sendAck(stanzaId);
                        return;
                    }
                    int rerequestCount = contentDb.getOutboundPostRerequestCount(senderUserId, contentId);
                    if (rerequestCount >= Constants.MAX_REREQUESTS_PER_MESSAGE) {
                        Log.w("Reached rerequest limit for post " + contentId + " for user " + senderUserId);
                        checkIdentityKey();
                    } else {
                        contentDb.setOutboundPostRerequestCount(senderUserId, contentId, rerequestCount + 1);
                        connection.sendRerequestedGroupPost(post, senderUserId);
                    }
                } else {
                    Log.e("Could not find group feed comment " + contentId + " to satisfy rerequest");
                    connection.sendMissingContentNotice(ContentMissing.ContentType.GROUP_FEED_POST, contentId, senderUserId);
                }
            } else if (GroupFeedRerequest.ContentType.COMMENT.equals(contentType)) {
                Comment comment = contentDb.getComment(contentId);
                if (comment != null) {
                    if (comment.isRetracted()) {
                        Log.i("Rerequested comment " + contentId + " has been retracted; sending another retract");
                        connection.retractRerequestedGroupComment(groupId, comment.postId, comment.id, senderUserId);
                        connection.sendAck(stanzaId);
                        return;
                    }
                    int rerequestCount = contentDb.getOutboundCommentRerequestCount(senderUserId, contentId);
                    if (rerequestCount >= Constants.MAX_REREQUESTS_PER_MESSAGE) {
                        Log.w("Reached rerequest limit for comment " + contentId);
                        checkIdentityKey();
                    } else {
                        contentDb.setOutboundCommentRerequestCount(senderUserId, contentId, rerequestCount + 1);
                        connection.sendRerequestedGroupComment(comment, senderUserId);
                    }
                } else {
                    Log.e("Could not find group feed post " + contentId + " to satisfy rerequest");
                    connection.sendMissingContentNotice(ContentMissing.ContentType.GROUP_FEED_COMMENT, contentId, senderUserId);
                }
            } else if (GroupFeedRerequest.ContentType.COMMENT_REACTION.equals(contentType)) {
                Reaction reaction = contentDb.getReaction(contentId);
                if (reaction != null) {
                    int rerequestCount = contentDb.getOutboundCommentRerequestCount(senderUserId, contentId);
                    if (rerequestCount >= Constants.MAX_REREQUESTS_PER_MESSAGE) {
                        Log.w("Reached rerequest limit for comment reaction " + contentId);
                        checkIdentityKey();
                    } else {
                        contentDb.setOutboundCommentRerequestCount(senderUserId, contentId, rerequestCount + 1);
                        Comment reactedComment = contentDb.getComment(reaction.contentId);
                        if (reactedComment == null) {
                            Log.e("Could not find parent comment " + reaction.contentId + " of group feed comment reaction " + contentId + " to satisfy rerequest");
                            connection.sendMissingContentNotice(ContentMissing.ContentType.GROUP_COMMENT_REACTION, contentId, senderUserId);
                        } else {
                            ReactionComment reactionComment = new ReactionComment(reaction, 0, reactedComment.postId, reaction.senderUserId, reaction.reactionId, reactedComment.id, reaction.timestamp, Comment.TRANSFERRED_NO, true, null);
                            Post parentPost = contentDb.getPost(reactionComment.postId);
                            reactionComment.setParentPost(parentPost);
                            connection.sendRerequestedGroupComment(reactionComment, senderUserId);
                        }
                    }
                } else {
                    Log.e("Could not find group feed comment reaction " + contentId + " to satisfy rerequest");
                    connection.sendMissingContentNotice(ContentMissing.ContentType.GROUP_COMMENT_REACTION, contentId, senderUserId);
                }
            } else if (GroupFeedRerequest.ContentType.POST_REACTION.equals(contentType)) {
                Reaction reaction = contentDb.getReaction(contentId);
                if (reaction != null) {
                    int rerequestCount = contentDb.getOutboundCommentRerequestCount(senderUserId, contentId);
                    if (rerequestCount >= Constants.MAX_REREQUESTS_PER_MESSAGE) {
                        Log.w("Reached rerequest limit for post reaction " + contentId);
                        checkIdentityKey();
                    } else {
                        contentDb.setOutboundCommentRerequestCount(senderUserId, contentId, rerequestCount + 1);
                        Post reactedPost = contentDb.getPost(reaction.contentId);
                        if (reactedPost == null) {
                            Log.e("Could not find parent comment " + reaction.contentId + " of group feed post reaction " + contentId + " to satisfy rerequest");
                            connection.sendMissingContentNotice(ContentMissing.ContentType.GROUP_POST_REACTION, contentId, senderUserId);
                        } else {
                            ReactionComment reactionComment = new ReactionComment(reaction, 0, reactedPost.id, reaction.senderUserId, reaction.reactionId, null, reaction.timestamp, Comment.TRANSFERRED_NO, true, null);
                            reactionComment.setParentPost(reactedPost);
                            connection.sendRerequestedGroupComment(reactionComment, senderUserId);
                        }
                    }
                } else {
                    Log.e("Could not find group feed post reaction " + contentId + " to satisfy rerequest");
                    connection.sendMissingContentNotice(ContentMissing.ContentType.GROUP_POST_REACTION, contentId, senderUserId);
                }
            } else if (GroupFeedRerequest.ContentType.MESSAGE.equals(contentType)) {
                Message message = contentDb.getMessage(contentId);
                if (message != null) {
                    int rerequestCount = contentDb.getOutboundMessageRerequestCount(senderUserId, contentId);
                    if (rerequestCount >= Constants.MAX_REREQUESTS_PER_MESSAGE) {
                        Log.w("Reached rerequest limit for message " + contentId);
                        checkIdentityKey();
                    } else {
                        contentDb.setOutboundMessageRerequestCount(senderUserId, contentId, rerequestCount + 1);
                        connection.sendRerequestedGroupMessage(message, senderUserId);
                    }
                } else {
                    Log.e("Could not find group chat message " + contentId + " to satisfy rerequest");
                    connection.sendMissingContentNotice(ContentMissing.ContentType.GROUP_CHAT, contentId, senderUserId);
                }
            }
            connection.sendAck(stanzaId);
        });
    }

    @Override
    public void onGroupFeedHistoryRerequest(@NonNull UserId senderUserId, @NonNull GroupId groupId, @NonNull String historyId, boolean senderStateIssue, @NonNull String stanzaId) {
        bgWorkers.execute(() -> {
            signalSessionManager.tearDownSession(senderUserId);
            int rerequestCount = contentDb.getHistoryResendRerequestCount(senderUserId, historyId);
            if (rerequestCount >= Constants.MAX_REREQUESTS_PER_MESSAGE) {
                Log.w("Reached rerequest limit for comment " + historyId);
                checkIdentityKey();
            } else {
                contentDb.setHistoryResendRerequestCount(senderUserId, historyId, rerequestCount + 1);
                byte[] payload = contentDb.getHistoryResendPayload(historyId);

                if (payload == null) {
                    Log.w("Could not find payload matching " + historyId + " for group " + groupId);
                    connection.sendMissingContentNotice(ContentMissing.ContentType.HISTORY_RESEND, historyId, senderUserId);
                } else {
                    try {
                        try {
                            SignalSessionSetupInfo signalSessionSetupInfo = signalSessionManager.getSessionSetupInfo(senderUserId);
                        } catch (Exception e) {
                            throw new CryptoException("Failed to get session setup info for group history rerequest", e);
                        }
                        byte[] rawEncPayload = SignalSessionManager.getInstance().encryptMessage(payload, senderUserId);
                        byte[] encPayload = EncryptedPayload.newBuilder().setOneToOneEncryptedPayload(ByteString.copyFrom(rawEncPayload)).build().toByteArray();
                        HistoryResend.Builder builder = HistoryResend.newBuilder()
                                .setSenderClientVersion(Constants.USER_AGENT)
                                .setGid(groupId.rawId())
                                .setId(historyId)
                                .setEncPayload(ByteString.copyFrom(encPayload));
                        if (ServerProps.getInstance().getSendPlaintextGroupFeed()) {
                            builder.setPayload(ByteString.copyFrom(payload)); // TODO(jack): Remove once plaintext sending is off
                        }
                        connection.sendRerequestedHistoryResend(builder, senderUserId);
                    } catch (CryptoException e) {
                        Log.e("Failed to encrypt group history payload", e);
                    }
                }
            }

            connection.sendAck(stanzaId);
        });
    }

    private void checkIdentityKey() {
        Log.i("Verifying identity key matches");
        connection.downloadKeys(new UserId(me.getUser()))
                .onResponse(response -> {
                    try {
                        IdentityKey identityKeyProto = IdentityKey.parseFrom(response.identityKey);
                        byte[] remote = identityKeyProto.getPublicKey().toByteArray();
                        byte[] local = encryptedKeyStore.getMyPublicEd25519IdentityKey().getKeyMaterial();
                        Log.i("Remote identity key: " + StringUtils.bytesToHexString(remote));
                        Log.i("Local identity key: " + StringUtils.bytesToHexString(local));
                        if (!Arrays.equals(remote, local)) {
                            Log.e("Remote and local identity key do not match; resetting registration");
                            me.resetRegistration();
                        }
                    } catch (InvalidProtocolBufferException e) {
                        Log.e("Failed to parse own identity key proto", e);
                    }
                }).onError(err -> {
                    Log.w("Failed to fetch own identity key for verification", err);
                });
    }

    @Override
    public void onHomeFeedRerequest(@NonNull HomeFeedRerequest.ContentType contentType, @NonNull UserId senderUserId, @NonNull String contentId, boolean senderStateIssue, @NonNull String stanzaId) {
        bgWorkers.execute(() -> {
            if (senderStateIssue) {
                signalSessionManager.tearDownSession(senderUserId);
            }
            if (HomeFeedRerequest.ContentType.POST.equals(contentType)) {
                Post post = contentDb.getPost(contentId);
                if (post != null) {
                    if (post.isRetracted()) {
                        Log.i("Rerequested post " + contentId + " has been retracted; sending another retract");
                        connection.retractRerequestedPost(post.id, senderUserId);
                        connection.sendAck(stanzaId);
                        return;
                    }
                    int rerequestCount = contentDb.getOutboundPostRerequestCount(senderUserId, contentId);
                    if (rerequestCount >= Constants.MAX_REREQUESTS_PER_MESSAGE) {
                        Log.w("Reached rerequest limit for post " + contentId + " for user " + senderUserId);
                        checkIdentityKey();
                    } else {
                        contentDb.setOutboundPostRerequestCount(senderUserId, contentId, rerequestCount + 1);
                        connection.sendRerequestedHomePost(post, senderUserId);
                    }
                } else {
                    Log.e("Could not find group feed post " + contentId + " to satisfy rerequest");
                    connection.sendMissingContentNotice(ContentMissing.ContentType.HOME_FEED_POST, contentId, senderUserId);
                }
            } else if (HomeFeedRerequest.ContentType.COMMENT.equals(contentType)) {
                Comment comment = contentDb.getComment(contentId);
                if (comment != null) {
                    if (comment.isRetracted()) {
                        Log.i("Rerequested comment " + contentId + " has been retracted; sending another retract");
                        connection.retractRerequestedComment(comment.postId, comment.id, senderUserId);
                        connection.sendAck(stanzaId);
                        return;
                    }
                    int rerequestCount = contentDb.getOutboundCommentRerequestCount(senderUserId, contentId);
                    if (rerequestCount >= Constants.MAX_REREQUESTS_PER_MESSAGE) {
                        Log.w("Reached rerequest limit for comment " + contentId);
                        checkIdentityKey();
                    } else {
                        contentDb.setOutboundCommentRerequestCount(senderUserId, contentId, rerequestCount + 1);
                        connection.sendRerequestedHomeComment(comment, senderUserId);
                    }
                } else {
                    Log.e("Could not find home feed comment " + contentId + " to satisfy rerequest");
                    connection.sendMissingContentNotice(ContentMissing.ContentType.HOME_FEED_COMMENT, contentId, senderUserId);
                }
            } else if (HomeFeedRerequest.ContentType.COMMENT_REACTION.equals(contentType)) {
                Reaction reaction = contentDb.getReaction(contentId);
                if (reaction != null) {
                    int rerequestCount = contentDb.getOutboundCommentRerequestCount(senderUserId, contentId);
                    if (rerequestCount >= Constants.MAX_REREQUESTS_PER_MESSAGE) {
                        Log.w("Reached rerequest limit for comment reaction " + contentId);
                        checkIdentityKey();
                    } else {
                        contentDb.setOutboundCommentRerequestCount(senderUserId, contentId, rerequestCount + 1);
                        Comment reactedComment = contentDb.getComment(reaction.contentId);
                        if (reactedComment == null) {
                            Log.e("Could not find parent comment " + reaction.contentId + " of home feed comment reaction " + contentId + " to satisfy rerequest");
                            connection.sendMissingContentNotice(ContentMissing.ContentType.HOME_COMMENT_REACTION, contentId, senderUserId);
                        } else {
                            ReactionComment reactionComment = new ReactionComment(reaction, 0, reactedComment.postId, reaction.senderUserId, reaction.reactionId, reactedComment.id, reaction.timestamp, Comment.TRANSFERRED_NO, true, null);
                            Post parentPost = contentDb.getPost(reactionComment.postId);
                            reactionComment.setParentPost(parentPost);
                            connection.sendRerequestedHomeComment(reactionComment, senderUserId);
                        }
                    }
                } else {
                    Log.e("Could not find home feed comment reaction " + contentId + " to satisfy rerequest");
                    connection.sendMissingContentNotice(ContentMissing.ContentType.HOME_COMMENT_REACTION, contentId, senderUserId);
                }
            } else if (HomeFeedRerequest.ContentType.POST_REACTION.equals(contentType)) {
                Reaction reaction = contentDb.getReaction(contentId);
                if (reaction != null) {
                    int rerequestCount = contentDb.getOutboundCommentRerequestCount(senderUserId, contentId);
                    if (rerequestCount >= Constants.MAX_REREQUESTS_PER_MESSAGE) {
                        Log.w("Reached rerequest limit for post reaction " + contentId);
                        checkIdentityKey();
                    } else {
                        contentDb.setOutboundCommentRerequestCount(senderUserId, contentId, rerequestCount + 1);
                        Post reactedPost = contentDb.getPost(reaction.contentId);
                        if (reactedPost == null) {
                            Log.e("Could not find parent comment " + reaction.contentId + " of home feed post reaction " + contentId + " to satisfy rerequest");
                            connection.sendMissingContentNotice(ContentMissing.ContentType.HOME_POST_REACTION, contentId, senderUserId);
                        } else {
                            ReactionComment reactionComment = new ReactionComment(reaction, 0, reactedPost.id, reaction.senderUserId, reaction.reactionId, null, reaction.timestamp, Comment.TRANSFERRED_NO, true, null);
                            reactionComment.setParentPost(reactedPost);
                            connection.sendRerequestedGroupComment(reactionComment, senderUserId);
                        }
                    }
                } else {
                    Log.e("Could not find home feed post reaction " + contentId + " to satisfy rerequest");
                    connection.sendMissingContentNotice(ContentMissing.ContentType.HOME_POST_REACTION, contentId, senderUserId);
                }
            }
            connection.sendAck(stanzaId);
        });
    }

    @Override
    public void onContactsChanged(@NonNull List<ContactInfo> protocolContacts, @NonNull List<String> contactHashes, @NonNull String ackId) {
        final List<ContactsDb.NormalizedPhoneData> normalizedPhoneDataList = new ArrayList<>(protocolContacts.size());
        for (ContactInfo contact : protocolContacts) {
            normalizedPhoneDataList.add(new ContactsDb.NormalizedPhoneData(contact.normalizedPhone, new UserId(contact.userId), contact.avatarId));
        }
        bgWorkers.execute(() -> {
            try {
                contactsDb.updateNormalizedPhoneData(normalizedPhoneDataList).get();
                if (!contactHashes.isEmpty()) {
                    ContactsSync.getInstance().startContactSync(contactHashes);
                }
                connection.sendAck(ackId);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("ConnectionObserver.onContactsChanged", e);
            }
        });
    }

    @Override
    public void onInvitesAccepted(@NonNull List<ContactInfo> contacts, @NonNull String ackId) {
        bgWorkers.execute(() -> {
            for (ContactInfo contactInfo : contacts) {
                Contact contact = contactsDb.getContact(new UserId(contactInfo.userId));
                notifications.showInviteAcceptedNotification(contact);
            }
            connection.sendAck(ackId);
        });
    }

    @Override
    public void onUserNamesReceived(@NonNull Map<UserId, String> names) {
        contactsDb.updateUserNames(names);
    }

    @Override
    public void onUserPhonesReceived(@NonNull Map<UserId, String> phones) {
        contactsDb.updateUserPhones(phones);
    }

    @Override
    public void onPresenceReceived(UserId user, Long lastSeen) {
        presenceManager.reportPresence(user, lastSeen);
    }

    @Override
    public void onChatStateReceived(UserId user, ChatState chatState) {
        presenceManager.reportChatState(user, chatState);
    }

    @Override
    public void onPostRevoked(@NonNull UserId senderUserId, @NonNull String postId, @Nullable GroupId groupId, long timestamp) {
        Post post = new Post(0, senderUserId, postId, timestamp, Post.TRANSFERRED_NO, Post.SEEN_NO, null);
        if (groupId != null) {
            post.setParentGroup(groupId);
        }
        contentDb.retractPost(post);
    }

    @Override
    public void onCommentRevoked(@NonNull String id, @NonNull UserId commentSenderId, @NonNull String postId, long timestamp) {
        Comment comment = new Comment(0, postId, commentSenderId, id, null, timestamp, !commentSenderId.isMe() ? Comment.TRANSFERRED_YES : Comment.TRANSFERRED_NO, true, null);
        contentDb.retractComment(comment);
    }

    @Override
    public void onMessageRevoked(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId, @NonNull String ackId) {
        contentDb.retractMessage(chatId, senderUserId, messageId, () -> {
            connection.sendAck(ackId);
        });
    }

    @Override
    public void onWhisperKeysMessage(@NonNull WhisperKeysMessage message, @NonNull String ackId) {
        if (message.count != null) {
            int count = message.count;
            Log.i("OTPK count down to " + count + "; replenishing");
            List<byte[]> protoKeys = signalSessionManager.getFreshOneTimePreKeyProtos();
            connection.uploadMoreOneTimePreKeys(protoKeys);
            connection.sendAck(ackId);
        } else if (message.userId != null) {
            signalSessionManager.tearDownSession(message.userId);
            addSystemMessage(message.userId, message.userId, Message.USAGE_KEYS_CHANGED, null, () -> connection.sendAck(ackId));
            homeFeedSessionManager.tearDownInboundSession(true, message.userId);
            homeFeedSessionManager.tearDownInboundSession(false, message.userId);
            for (GroupId groupId : contentDb.getGroupsInCommon(message.userId)) {
                groupFeedSessionManager.tearDownOutboundSession(groupId);
                groupFeedSessionManager.tearDownInboundSession(groupId, message.userId);
            }
            Contact contact = contactsDb.getContact(message.userId);
            if (contact.inAddressBook()) {
                encryptedKeyStore.edit().storeNeedsStateUid(false, message.userId).apply();
                if (contactsDb.getFeedShareList().contains(message.userId)) {
                    encryptedKeyStore.edit().storeNeedsStateUid(true, message.userId).apply();
                }
            }
        }
    }

    @Override
    public void onAvatarChangeMessageReceived(UserId userId, String avatarId, @NonNull String ackId) {
        avatarLoader.reportAvatarUpdate(userId, avatarId);
        connection.sendAck(ackId);
    }

    @Override
    public void onGroupFeedCreated(@NonNull GroupId groupId, @NonNull String name, @Nullable String avatarId, @NonNull List<MemberElement> memberElements, @NonNull UserId sender, @NonNull String senderName, @Nullable ExpiryInfo expiryInfo, @NonNull String ackId) {
        if (!sender.isMe()) {
            notifications.showNewGroupNotification(groupId, senderName, name, true);
        }
        List<MemberInfo> members = new ArrayList<>();
        for (MemberElement memberElement : memberElements) {
            members.add(new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name));
        }
        members.add(new MemberInfo(-1, sender, MemberElement.Type.ADMIN, senderName));

        contentDb.addFeedGroup(new GroupInfo(GroupStanza.GroupType.FEED, groupId, name, null, avatarId, Background.getDefaultInstance(), members, expiryInfo), () -> {
            GroupId zeroZoneGroup = preferences.getZeroZoneGroupId();
            if (zeroZoneGroup == null || !zeroZoneGroup.equals(groupId)) {
                addSystemPost(groupId, sender, Post.USAGE_CREATE_GROUP, null, true, () -> connection.sendAck(ackId));
            } else {
                connection.sendAck(ackId);
            }
        });
    }

    @Override
    public void onGroupChatCreated(@NonNull GroupId groupId, @NonNull String name, @Nullable String avatarId, @NonNull List<MemberElement> memberElements, @NonNull UserId sender, @NonNull String senderName, @Nullable ExpiryInfo expiryInfo, @NonNull String ackId) {
        if (!sender.isMe()) {
            notifications.showNewGroupNotification(groupId, senderName, name, false);
        }
        List<MemberInfo> members = new ArrayList<>();
        for (MemberElement memberElement : memberElements) {
            members.add(new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name));
        }
        members.add(new MemberInfo(-1, sender, MemberElement.Type.ADMIN, senderName));

        contentDb.addGroupChat(new GroupInfo(GroupStanza.GroupType.CHAT, groupId, name, null, avatarId, Background.getDefaultInstance(), members, expiryInfo), () -> {
            addSystemMessage(groupId, sender, Message.USAGE_CREATE_GROUP, null, () -> connection.sendAck(ackId));
            connection.sendAck(ackId);
        });
    }

    @Override
    public void onGroupMemberChangeReceived(@NonNull GroupId groupId, @Nullable String groupName, @Nullable String avatarId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @Nullable HistoryResend historyResend, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        List<MemberInfo> added = new ArrayList<>();
        List<MemberInfo> removed = new ArrayList<>();
        boolean isFeed = GroupStanza.GroupType.FEED.equals(groupType);
        boolean affectsMe = false;
        for (MemberElement memberElement : members) {
            MemberInfo memberInfo = new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name);
            if (MemberElement.Action.ADD.equals(memberElement.action)) {
                added.add(memberInfo);
                if (memberInfo.userId.isMe()) {
                    affectsMe = true;
                    notifications.showNewGroupNotification(groupId, senderName, groupName, isFeed);
                }
            } else if (MemberElement.Action.REMOVE.equals(memberElement.action)) {
                removed.add(memberInfo);
                if (memberInfo.userId.isMe()) {
                    affectsMe = true;
                    notifications.showRemovedFromGroupNotification(groupId, senderName, groupName, isFeed);
                }
            }
        }

        final boolean affectedMe = affectsMe;
        contentDb.addRemoveGroupMembers(groupId, groupName, avatarId, added, removed, () -> {
            if (!added.isEmpty()) {
                String idList = toUserIdList(added);
                if (isFeed) {
                    addSystemPost(groupId, sender, Post.USAGE_ADD_MEMBERS, idList, affectedMe, null);
                } else {
                    addSystemMessage(groupId, sender, Message.USAGE_ADD_MEMBERS, idList, null);
                }
                encryptedKeyStore.edit().clearGroupSendAlreadySetUp(groupId).apply();
            }

            if (!removed.isEmpty()) {
                String idList = toUserIdList(removed);
                if (isFeed) {
                    addSystemPost(groupId, sender, Post.USAGE_REMOVE_MEMBER, idList, affectedMe, null);
                } else {
                    addSystemMessage(groupId, sender, Message.USAGE_REMOVE_MEMBER, idList, null);
                }
                groupFeedSessionManager.tearDownOutboundSession(groupId);
            }

            for (MemberInfo member : removed) {
                if (member.userId.isMe()) {
                    if (isFeed) {
                        contentDb.setGroupFeedInactive(groupId, null);
                    } else {
                        contentDb.setGroupChatInactive(groupId, null);
                    }
                    break;
                }
            }

            for (MemberInfo member : added) {
                if (member.userId.isMe()) {
                    if (isFeed) {
                        contentDb.setGroupFeedActive(groupId, null);
                    } else {
                        contentDb.setGroupChatActive(groupId, null);
                    }
                    break;
                }
            }

            if (historyResend == null) {
                connection.sendAck(ackId);
            } else if (sender.isMe()) {
                Log.i("User is originator; ignoring history resend");
                connection.sendAck(ackId);
            } else {
                handleHistoryResend(historyResend, sender, ackId);
            }
        });
    }

    @Override
    public void onGroupMemberJoinReceived(@NonNull GroupId groupId, @Nullable String groupName, @Nullable String avatarId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        List<MemberInfo> joined = new ArrayList<>();
        for (MemberElement memberElement : members) {
            MemberInfo memberInfo = new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name);
            if (MemberElement.Action.JOIN.equals(memberElement.action)) {
                joined.add(memberInfo);
            }
        }
        boolean isChat = GroupStanza.GroupType.CHAT.equals(groupType);
        contentDb.addRemoveGroupMembers(groupId, groupName, avatarId, joined, new ArrayList<>(), () -> {
            if (!joined.isEmpty()) {
                encryptedKeyStore.edit().clearGroupSendAlreadySetUp(groupId).apply();
            }
            for (MemberInfo member : joined) {
                final boolean isMe = member.userId.isMe();
                if (isChat) {
                    addSystemMessage(groupId, member.userId, Message.USAGE_MEMBER_JOINED, null, () -> {
                        if (isMe) {
                            contentDb.setGroupChatActive(groupId, null);
                        }
                    });
                } else {
                    addSystemPost(groupId, member.userId, Post.USAGE_MEMBER_JOINED, null, isMe, () -> {
                        if (isMe) {
                            contentDb.setGroupFeedActive(groupId, null);
                        }
                    });
                }
            }

            connection.sendAck(ackId);
        });
    }

    @Override
    public void onGroupMemberLeftReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        List<MemberInfo> left = new ArrayList<>();
        for (MemberElement memberElement : members) {
            MemberInfo memberInfo = new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name);
            if (MemberElement.Action.LEAVE.equals(memberElement.action)) {
                left.add(memberInfo);
            }
        }
        boolean isChat = GroupStanza.GroupType.CHAT.equals(groupType);
        contentDb.addRemoveGroupMembers(groupId, null, null, new ArrayList<>(), left, () -> {
            if (!left.isEmpty()) {
                groupFeedSessionManager.tearDownOutboundSession(groupId);
            }
            for (MemberInfo member : left) {
                if (isChat) {
                    addSystemMessage(groupId, member.userId, Message.USAGE_MEMBER_LEFT, null, () -> {
                        if (member.userId.isMe()) {
                            contentDb.setGroupChatInactive(groupId, null);
                        }
                    });
                } else {
                    addSystemPost(groupId, member.userId, Post.USAGE_MEMBER_LEFT, null, () -> {
                        if (member.userId.isMe()) {
                            contentDb.setGroupFeedInactive(groupId, null);
                        }
                    });
                }
            }

            connection.sendAck(ackId);
        });
    }

    @Override
    public void onGroupBackgroundChangeReceived(@NonNull GroupId groupId, int theme, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        contentDb.setGroupTheme(groupId, theme, () -> {
            addSystemPost(groupId, sender, Post.USAGE_GROUP_THEME_CHANGED, null, () -> connection.sendAck(ackId));
        });
    }

    @Override
    public void onGroupAdminChangeReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        List<MemberInfo> promoted = new ArrayList<>();
        List<MemberInfo> demoted = new ArrayList<>();
        boolean affectsMe = false;
        for (MemberElement memberElement : members) {
            MemberInfo memberInfo = new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name);
            if (memberElement.uid.isMe()) {
                affectsMe = true;
            }
            if (MemberElement.Action.PROMOTE.equals(memberElement.action)) {
                promoted.add(memberInfo);
            } else if (MemberElement.Action.DEMOTE.equals(memberElement.action)) {
                demoted.add(memberInfo);
            }
        }
        final boolean affectedMe = affectsMe;
        boolean isChat = GroupStanza.GroupType.CHAT.equals(groupType);
        contentDb.promoteDemoteGroupAdmins(groupId, promoted, demoted, () -> {
            if (!promoted.isEmpty()) {
                String idList = toUserIdList(promoted);
                if (isChat) {
                    addSystemMessage(groupId, sender, Message.USAGE_PROMOTE, idList, null);
                } else {
                    addSystemPost(groupId, sender, Post.USAGE_PROMOTE, idList, affectedMe, null);
                }
            }

            if (!demoted.isEmpty()) {
                String idList = toUserIdList(demoted);
                if (isChat) {
                    addSystemMessage(groupId, sender, Message.USAGE_DEMOTE, idList, null);
                } else {
                    addSystemPost(groupId, sender, Post.USAGE_DEMOTE, idList, affectedMe, null);
                }
            }

            connection.sendAck(ackId);
        });
    }

    @Override
    public void onGroupNameChangeReceived(@NonNull GroupId groupId, @NonNull String name, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        if (groupType.equals(GroupStanza.GroupType.CHAT)) {
            contentDb.setGroupChatName(groupId, name, () -> {
                addSystemMessage(groupId, sender, Message.USAGE_NAME_CHANGE, name, () -> connection.sendAck(ackId));
            });
        } else {
            contentDb.setGroupName(groupId, name, () -> {
                addSystemPost(groupId, sender, Post.USAGE_NAME_CHANGE, name, () -> connection.sendAck(ackId));
            });
        }
    }

    @Override
    public void onGroupAvatarChangeReceived(@NonNull GroupId groupId, @NonNull String avatarId, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        if (groupType.equals(GroupStanza.GroupType.CHAT)) {
            contentDb.setGroupChatAvatar(groupId, avatarId, () -> {
                avatarLoader.reportAvatarUpdate(groupId, avatarId);
                addSystemMessage(groupId, sender, Message.USAGE_AVATAR_CHANGE, null, () -> connection.sendAck(ackId));
            });
        } else {
            contentDb.setGroupAvatar(groupId, avatarId, () -> {
                avatarLoader.reportAvatarUpdate(groupId, avatarId);
                addSystemPost(groupId, sender, Post.USAGE_AVATAR_CHANGE, null, () -> connection.sendAck(ackId));
            });
        }
    }

    @Override
    public void onGroupDescriptionChanged(@NonNull GroupId groupId, @NonNull String description, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        if (groupType.equals(GroupStanza.GroupType.CHAT)) {
            contentDb.setGroupChatDescription(groupId, description, () -> {
                addSystemMessage(groupId, sender, Message.USAGE_GROUP_DESCRIPTION_CHANGED, description, () -> connection.sendAck(ackId));
            });
        } else {
            contentDb.setGroupDescription(groupId, description, () -> {
                addSystemPost(groupId, sender, Post.USAGE_GROUP_DESCRIPTION_CHANGED, description, () -> connection.sendAck(ackId));
            });
        }
    }

    @Override
    public void onGroupExpiryChanged(@NonNull GroupId groupId, @NonNull ExpiryInfo expiryInfo, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        contentDb.setGroupExpiry(groupId, expiryInfo, () -> {
            addSystemPost(groupId, sender, Post.USAGE_GROUP_EXPIRY_CHANGED, Base64.encodeToString(expiryInfo.toByteArray(), Base64.NO_WRAP), () -> connection.sendAck(ackId));
        });
    }

    @Override
    public void onGroupAdminAutoPromoteReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        List<MemberInfo> promoted = new ArrayList<>();
        for (MemberElement memberElement : members) {
            MemberInfo memberInfo = new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name);
            if (MemberElement.Action.PROMOTE.equals(memberElement.action)) {
                promoted.add(memberInfo);
            }
        }
        boolean isChat = groupType.equals(GroupStanza.GroupType.CHAT);
        contentDb.promoteDemoteGroupAdmins(groupId, promoted, new ArrayList<>(), () -> {
            for (MemberInfo member : promoted) {
                if (isChat) {
                    addSystemMessage(groupId, member.userId, Message.USAGE_AUTO_PROMOTE, null, null);
                } else {
                    addSystemPost(groupId, member.userId, Post.USAGE_AUTO_PROMOTE, null, member.userId.isMe(), null);
                }
            }

            connection.sendAck(ackId);
        });
    }

    @Override
    public void onGroupDeleteReceived(@NonNull GroupId groupId, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        addSystemPost(groupId, sender, Post.USAGE_GROUP_DELETED, null, () -> {
            if (groupType.equals(GroupStanza.GroupType.CHAT)) {
                contentDb.setGroupChatInactive(groupId, () -> {
                    connection.sendAck(ackId);
                });
            } else {
                contentDb.setGroupFeedInactive(groupId, () -> {
                    connection.sendAck(ackId);
                });
            }
        });
    }

    @Override
    public void onHistoryResend(@NonNull HistoryResend historyResend, @NonNull UserId peerUserId, @NonNull String ackId) {
        handleHistoryResend(historyResend, peerUserId, ackId);
    }

    @Override
    public void onContentMissing(@NonNull ContentMissing.ContentType contentType, @NonNull UserId peerUserId, @NonNull String contentId, @NonNull String ackId) {
        if (ContentMissing.ContentType.CHAT.equals(contentType)) {
            Message message = contentDb.getMessage(peerUserId, peerUserId, contentId);
            if (message != null) {
                message.failureReason = "content_missing";
                contentDb.updateMessageDecrypt(message, null);
            }
        } else if (ContentMissing.ContentType.GROUP_FEED_POST.equals(contentType) || ContentMissing.ContentType.HOME_FEED_POST.equals(contentType)) {
            contentDb.setPostMissing(contentId);
        } else if (ContentMissing.ContentType.GROUP_FEED_COMMENT.equals(contentType) || ContentMissing.ContentType.HOME_FEED_COMMENT.equals(contentType)) {
            contentDb.setCommentMissing(contentId);
        } else if (ContentMissing.ContentType.HISTORY_RESEND.equals(contentType) || ContentMissing.ContentType.GROUP_HISTORY.equals(contentType)) {
            // TODO(jack): set history resend objects as missing for stats
        } else if (ContentMissing.ContentType.GROUP_CHAT.equals(contentType)) {
            Message message = contentDb.getMessage(null, peerUserId, contentId);
            if (message != null) {
                message.failureReason = "content_missing";
                contentDb.updateMessageDecrypt(message, null);
            }
        }
        connection.sendAck(ackId);
    }

    @Override
    public void onMomentNotificationReceived(@NonNull MomentNotification momentNotification, @NonNull String ackId) {
        long timestamp = momentNotification.getTimestamp() * 1000;
        long oldTimestamp = preferences.getMomentNotificationTimestamp();

        if (oldTimestamp > timestamp) {
            Log.e("onMomentNotificationReceived: " + timestamp + " is older than the current " + oldTimestamp);
        } else if (TimeUtils.isSameDay(oldTimestamp, timestamp)) {
            Log.e("onMomentNotificationReceived: duplicate moment notification for the day " + timestamp);
        } else {
            preferences.setMomentNotificationTimestamp(timestamp);
            notifications.showDailyMomentNotification(timestamp);
            connection.sendAck(ackId);
        }
    }

    private void handleHistoryResend(@NonNull HistoryResend historyResend, @NonNull UserId publisherUserId, @NonNull String ackId) {
        bgWorkers.execute(() -> {
            ByteString encrypted = historyResend.getEncPayload();
            GroupId groupId = new GroupId(historyResend.getGid());
            if (encrypted != null && encrypted.size() > 0) {
                boolean senderStateIssue = false;
                String errorMessage;
                if (historyResend.hasSenderState()) {
                    SenderStateWithKeyInfo senderStateWithKeyInfo = historyResend.getSenderState();

                    byte[] encSenderState = senderStateWithKeyInfo.getEncSenderState().toByteArray();
                    try {
                        byte[] peerPublicIdentityKey = senderStateWithKeyInfo.getPublicKey().toByteArray();
                        long oneTimePreKeyId = senderStateWithKeyInfo.getOneTimePreKeyId();
                        SignalSessionSetupInfo signalSessionSetupInfo = peerPublicIdentityKey == null || peerPublicIdentityKey.length == 0 ? null : new SignalSessionSetupInfo(new PublicEdECKey(peerPublicIdentityKey), (int) oneTimePreKeyId);
                        byte[] senderStateDec = SignalSessionManager.getInstance().decryptMessage(encSenderState, publisherUserId, signalSessionSetupInfo);
                        SenderState senderState = SenderState.parseFrom(senderStateDec);
                        SenderKey senderKey = senderState.getSenderKey();
                        int currentChainIndex = senderState.getCurrentChainIndex();
                        byte[] chainKey = senderKey.getChainKey().toByteArray();
                        byte[] publicSignatureKeyBytes = senderKey.getPublicSignatureKey().toByteArray();
                        PublicEdECKey publicSignatureKey = new PublicEdECKey(publicSignatureKeyBytes);
                        Log.i("Received sender state with current chain index of " + currentChainIndex + " from " + publisherUserId);

                        encryptedKeyStore.edit()
                                .setPeerGroupCurrentChainIndex(groupId, publisherUserId, currentChainIndex)
                                .setPeerGroupChainKey(groupId, publisherUserId, chainKey)
                                .setPeerGroupSigningKey(groupId, publisherUserId, publicSignatureKey)
                                .apply();
                    } catch (CryptoException e) {
                        Log.e("Failed to decrypt sender state for " + ProtoPrinter.toString(historyResend), e);
                        senderStateIssue = true;
                    } catch (InvalidProtocolBufferException e) {
                        Log.e("Failed to parse sender state for " + ProtoPrinter.toString(historyResend), e);
                    }
                }

                try {
                    byte[] decrypted;
                    try {
                        EncryptedPayload encryptedPayload = EncryptedPayload.parseFrom(encrypted.toByteArray());
                        switch (encryptedPayload.getPayloadCase()) {
                            case SENDER_STATE_ENCRYPTED_PAYLOAD: {
                                byte[] toDecrypt = encryptedPayload.getSenderStateEncryptedPayload().toByteArray();
                                decrypted = GroupFeedSessionManager.getInstance().decryptMessage(toDecrypt, groupId, publisherUserId);
                                break;
                            }
                            case ONE_TO_ONE_ENCRYPTED_PAYLOAD: {
                                byte[] toDecrypt = encryptedPayload.getOneToOneEncryptedPayload().toByteArray();
                                decrypted = SignalSessionManager.getInstance().decryptMessage(toDecrypt, publisherUserId, null);
                                break;
                            }
                            default: {
                                throw new CryptoException("no_accepted_enc_payload");
                            }
                        }
                    } catch (InvalidProtocolBufferException e) {
                        throw new CryptoException("history_resend_invalid_proto", e);
                    }

                    byte[] payload = historyResend.getPayload().toByteArray();
                    if (payload.length > 0 && !Arrays.equals(payload, decrypted)) {
                        Log.e("History Resend Encryption plaintext and decrypted differ");
                        throw new CryptoException("history_resend_payload_differs");
                    }
                    GroupHistoryPayload groupHistoryPayload = GroupHistoryPayload.parseFrom(decrypted);
                    groupsApi.handleGroupHistoryPayload(groupHistoryPayload, groupId);
                } catch (CryptoException e) {
                    Log.e("Failed to decrypt history resend", e);

                    errorMessage = e.getMessage();
                    Log.sendErrorReport("Group history decryption failed: " + errorMessage);
                    // TODO(jack): Stats
//                    stats.reportGroupDecryptError(errorMessage, true, senderPlatform, senderVersion);

                    String historyResendId = historyResend.getId();
                    Log.i("Rerequesting history resend " + historyResendId);
                    ContentDb contentDb = ContentDb.getInstance();
                    int count;
                    count = contentDb.getHistoryResendRerequestCount(publisherUserId, historyResendId);
                    count += 1;
                    contentDb.setHistoryResendRerequestCount(publisherUserId, historyResendId, count);
                    if (senderStateIssue) {
                        Log.i("Tearing down session because of sender state issue");
                        SignalSessionManager.getInstance().tearDownSession(publisherUserId);
                    }
                    GroupFeedSessionManager.getInstance().sendHistoryRerequest(publisherUserId, groupId, historyResendId, senderStateIssue);

                } catch (InvalidProtocolBufferException e) {
                    Log.e("Failed to parse history resend", e);
                }
            }
            connection.sendAck(ackId);
        });

        // TODO(jack): handle history resend rerequests
    }

    private String toUserIdList(@NonNull List<MemberInfo> members) {
        Preconditions.checkArgument(!members.isEmpty());
        StringBuilder sb = new StringBuilder(userIdToString(members.get(0).userId));
        for (int i=1; i<members.size(); i++) {
            sb.append(",").append(userIdToString(members.get(i).userId));
        }
        return sb.toString();
    }

    private String userIdToString(@NonNull UserId userId) {
        return userId.isMe() ? me.getUser() : userId.rawId();
    }

    private void addSystemPost(@NonNull GroupId groupId, @NonNull UserId sender, @Post.Usage int usage, @Nullable String text, @Nullable Runnable completionRunnable) {
        addSystemPost(groupId, sender, usage, text, false, completionRunnable);
    }

    private void addSystemPost(@NonNull GroupId groupId, @NonNull UserId sender, @Post.Usage int usage, @Nullable String text, boolean updateTimestamp, @Nullable Runnable completionRunnable) {
        long timestamp = System.currentTimeMillis();
        Post systemPost = new Post(0,
                sender,
                RandomId.create(),
                timestamp,
                Post.TRANSFERRED_YES,
                Post.SEEN_YES,
                Post.TYPE_SYSTEM,
                text);
        systemPost.usage = usage;
        systemPost.setParentGroup(groupId);
        contentDb.addPost(systemPost, completionRunnable);
        if (updateTimestamp) {
            contentDb.updateFeedGroupTimestamp(groupId, timestamp);
        }
    }

    private void addSystemMessage(@NonNull ChatId chatId, @NonNull UserId sender, @Message.Usage int usage, @Nullable String text, @Nullable Runnable completionRunnable) {
        if (chatId instanceof UserId) {
            if (contentDb.getChat(sender) == null) {
                Log.i("Skipping adding system message because chat with " + sender + " does not already exist");
                if (completionRunnable != null) {
                    completionRunnable.run();
                }
                return;
            }
        }
        Message message = new Message(0,
                chatId,
                sender,
                RandomId.create(),
                System.currentTimeMillis(),
                Message.TYPE_SYSTEM,
                usage,
                Message.STATE_OUTGOING_DELIVERED,
                text,
                null,
                -1,
                null,
                -1,
                null,
                0);
        contentDb.addMessage(message, false, completionRunnable);
    }

    @Override
    public void onHalloappProfileUpdateReceived(@NonNull HalloappProfileUpdate profileUpdate, @NonNull String ackId) {
        UserId userId = new UserId(Long.toString(profileUpdate.getProfile().getUid()));
        HalloappUserProfile profile = profileUpdate.getProfile();
        FriendshipInfo friendshipProfileInfo = new FriendshipInfo(
                userId,
                profile.getUsername(),
                profile.getName(),
                profile.getAvatarId(),
                FriendshipInfo.fromProtoType(profile.getStatus(), profile.getBlocked()),
                System.currentTimeMillis());
        contactsDb.addFriendship(friendshipProfileInfo);
        contactsDb.updateUserNames(Collections.singletonMap(userId, profile.getName()));
        contactsDb.updateUserUsernames(Collections.singletonMap(userId, profile.getUsername()));
        contactsDb.updateUserAvatars(Collections.singletonMap(userId, profile.getAvatarId()));
        connection.sendAck(ackId);
    }

    @Override
    public void onFriendListRequestReceived(@NonNull FriendListRequest friendList, @NonNull String ackId) {
        if (friendList.getAction() == FriendListRequest.Action.SYNC_ALL) {
            Log.i("Running server initiated friendship sync");
            FriendshipSyncWorker.startFriendshipSync(context);
        } else {
            Log.w("Unrecognized friendList request received " + friendList.getAction());
        }
        connection.sendAck(ackId);
    }
}
