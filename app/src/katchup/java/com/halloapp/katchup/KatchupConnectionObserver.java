package com.halloapp.katchup;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.ForegroundObserver;
import com.halloapp.HuaweiMessagingService;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.PushMessagingService;
import com.halloapp.RegistrationRequestActivity;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.content.TransferPendingItemsTask;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.katchup.avatar.KAvatarLoader;
import com.halloapp.proto.server.ContentMissing;
import com.halloapp.proto.server.ExpiryInfo;
import com.halloapp.proto.server.GroupFeedRerequest;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.HistoryResend;
import com.halloapp.proto.server.HomeFeedRerequest;
import com.halloapp.proto.server.MomentNotification;
import com.halloapp.proto.server.Rerequest;
import com.halloapp.ui.AppExpirationActivity;
import com.halloapp.ui.DeleteAccountActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.ChatState;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ContactInfo;
import com.halloapp.xmpp.WhisperKeysMessage;
import com.halloapp.xmpp.groups.MemberElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class KatchupConnectionObserver extends Connection.Observer {

    private static KatchupConnectionObserver instance;

    public static KatchupConnectionObserver getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (KatchupConnectionObserver.class) {
                if (instance == null) {
                    instance = new KatchupConnectionObserver(
                            context,
                            Me.getInstance(),
                            BgWorkers.getInstance(),
                            ContentDb.getInstance(),
                            Connection.getInstance(),
                            ContactsDb.getInstance(),
                            Preferences.getInstance(),
                            KAvatarLoader.getInstance(),
                            Notifications.getInstance(context),
                            ForegroundObserver.getInstance()
                    );
                }
            }
        }
        return instance;
    }

    private final Context context;

    private final Me me;
    private final BgWorkers bgWorkers;
    private final ContentDb contentDb;
    private final Connection connection;
    private final ContactsDb contactsDb;
    private final Preferences preferences;
    private final KAvatarLoader avatarLoader;
    private final Notifications notifications;
    private final ForegroundObserver foregroundObserver;

    private KatchupConnectionObserver(
            @NonNull Context context,
            @NonNull Me me,
            @NonNull BgWorkers bgWorkers,
            @NonNull ContentDb contentDb,
            @NonNull Connection connection,
            @NonNull ContactsDb contactsDb,
            @NonNull Preferences preferences,
            @NonNull KAvatarLoader avatarLoader,
            @NonNull Notifications notifications,
            @NonNull ForegroundObserver foregroundObserver
    ) {
        this.context = context.getApplicationContext();

        this.me = me;
        this.bgWorkers = bgWorkers;
        this.contentDb = contentDb;
        this.connection = connection;
        this.contactsDb = contactsDb;
        this.preferences = preferences;
        this.avatarLoader = avatarLoader;
        this.notifications = notifications;
        this.foregroundObserver = foregroundObserver;
    }

    @Override
    public void onConnected() {
        new TransferPendingItemsTask(context).execute();
        PushMessagingService.updateFirebasePushTokenIfNeeded();
        HuaweiMessagingService.updateHuaweiPushTokenIfNeeded();
        RelationshipSyncWorker.schedule(context);
    }

    @Override
    public void onDisconnected() {
        if (foregroundObserver.isInForeground()) {
            Log.i("MainConnectionObserver/onDisconnected still in foreground, reconnecting...");
            connection.connect();
        }
    }

    @Override
    public void onLoginFailed(boolean accountDeleted) {
        me.resetRegistration();

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
        Log.w("Katchup received unsupported audience hash mismatch for " + contentItem);
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
        Log.w("Katchup received unsupported message sent notification for " + messageId);
    }

    @Override
    public void onOutgoingMessageDelivered(@NonNull ChatId chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {
        Log.w("Katchup received unsupported message delivered notification for " + id);
    }

    @Override
    public void onOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {
        Log.w("Katchup received unsupported message seen notification for " + id);
    }

    @Override
    public void onOutgoingMessagePlayed(@NonNull ChatId chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {
        Log.w("Katchup received unsupported message played notification for " + id);
    }

    @Override
    public void onIncomingMessageReceived(@NonNull Message message) {
        Log.w("Katchup received unsupported message received notification for " + message);
    }

    @Override
    public void onIncomingMessageSeenReceiptSent(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        Log.w("Katchup received unsupported message seen receipt sent notification for " + messageId);
    }

    @Override
    public void onIncomingMessagePlayedReceiptSent(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        Log.w("Katchup received unsupported message played receipt sent notification for " + messageId);
    }

    @Override
    public void onMessageRerequest(@NonNull Rerequest.ContentType contentType, @NonNull UserId peerUserId, @NonNull String messageId, @NonNull PublicEdECKey peerIdentityKey, @Nullable Integer otpkId, @NonNull byte[] sessionSetupKey, @NonNull byte[] messageEphemeralKey, @NonNull String stanzaId) {
        Log.w("Katchup received unsupported message rerequest notification for " + messageId);
    }

    @Override
    public void onGroupFeedRerequest(@NonNull GroupFeedRerequest.ContentType contentType, @NonNull UserId senderUserId, @NonNull GroupId groupId, @NonNull String contentId, boolean senderStateIssue, @NonNull String stanzaId) {
        Log.w("Katchup received unsupported group feed rerequest notification for " + contentId);
    }

    @Override
    public void onGroupFeedHistoryRerequest(@NonNull UserId senderUserId, @NonNull GroupId groupId, @NonNull String historyId, boolean senderStateIssue, @NonNull String stanzaId) {
        Log.w("Katchup received unsupported group feed history rerequest notification for " + historyId);
    }

    @Override
    public void onHomeFeedRerequest(@NonNull HomeFeedRerequest.ContentType contentType, @NonNull UserId senderUserId, @NonNull String contentId, boolean senderStateIssue, @NonNull String stanzaId) {
        Log.w("Katchup received unsupported home feed rerequest notification for " + contentId);
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
        Log.w("Katchup received unsupported invites accepted notification");
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
        Log.w("Katchup received unsupported present notification");
    }

    @Override
    public void onChatStateReceived(UserId user, ChatState chatState) {
        Log.w("Katchup received unsupported chat state notification");
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
        Log.w("Katchup recieved unsupported message revoked notification for " + messageId);
    }

    @Override
    public void onWhisperKeysMessage(@NonNull WhisperKeysMessage message, @NonNull String ackId) {
        Log.w("Katchup received unsupported whisper keys message");
    }

    @Override
    public void onAvatarChangeMessageReceived(UserId userId, String avatarId, @NonNull String ackId) {
        avatarLoader.reportAvatarUpdate(userId, avatarId);
        connection.sendAck(ackId);
    }

    @Override
    public void onGroupFeedCreated(@NonNull GroupId groupId, @NonNull String name, @Nullable String avatarId, @NonNull List<MemberElement> memberElements, @NonNull UserId sender, @NonNull String senderName, @Nullable ExpiryInfo expiryInfo, @NonNull String ackId) {
        Log.w("Katchup received unsupported group feed created notification");
    }

    @Override
    public void onGroupChatCreated(@NonNull GroupId groupId, @NonNull String name, @Nullable String avatarId, @NonNull List<MemberElement> memberElements, @NonNull UserId sender, @NonNull String senderName, @Nullable ExpiryInfo expiryInfo, @NonNull String ackId) {
        Log.w("Katchup received unsupported group chat created notification");
    }

    @Override
    public void onGroupMemberChangeReceived(@NonNull GroupId groupId, @Nullable String groupName, @Nullable String avatarId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @Nullable HistoryResend historyResend, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        Log.w("Katchup received unsupported group member change notification");
    }

    @Override
    public void onGroupMemberJoinReceived(@NonNull GroupId groupId, @Nullable String groupName, @Nullable String avatarId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        Log.w("Katchup received unsupported group member joined notification");
    }

    @Override
    public void onGroupMemberLeftReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        Log.w("Katchup received unsupported group member left notification");
    }
    @Override
    public void onGroupBackgroundChangeReceived(@NonNull GroupId groupId, int theme, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        Log.w("Katchup received unsupported group background changed notification");
    }

    @Override
    public void onGroupAdminChangeReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        Log.w("Katchup received unsupported group admin changed notification");
    }

    @Override
    public void onGroupNameChangeReceived(@NonNull GroupId groupId, @NonNull String name, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        Log.w("Katchup received unsupported group name changed notification");
    }

    @Override
    public void onGroupAvatarChangeReceived(@NonNull GroupId groupId, @NonNull String avatarId, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        Log.w("Katchup received unsupported group avatar changed notification");
    }

    @Override
    public void onGroupDescriptionChanged(@NonNull GroupId groupId, @NonNull String description, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        Log.w("Katchup received unsupported group description changed notification");
    }

    @Override
    public void onGroupExpiryChanged(@NonNull GroupId groupId, @NonNull ExpiryInfo expiryInfo, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        Log.w("Katchup received unsupported group expiry changed notification");
    }

    @Override
    public void onGroupAdminAutoPromoteReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        Log.w("Katchup received unsupported group admin autopromoted notification");
    }

    @Override
    public void onGroupDeleteReceived(@NonNull GroupId groupId, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        Log.w("Katchup received unsupported group delete notification");
    }

    @Override
    public void onHistoryResend(@NonNull HistoryResend historyResend, @NonNull UserId peerUserId, @NonNull String ackId) {
        Log.w("Katchup received unsupported history resend notification");
    }

    @Override
    public void onContentMissing(@NonNull ContentMissing.ContentType contentType, @NonNull UserId peerUserId, @NonNull String contentId, @NonNull String ackId) {
        Log.w("Katchup received unsupported content missing notification");
    }

    @Override
    public void onMomentNotificationReceived(@NonNull MomentNotification momentNotification, @NonNull String ackId) {
        long timestamp = momentNotification.getTimestamp() * 1000;
        long oldTimestamp = preferences.getMomentNotificationTimestamp();

        if (oldTimestamp > timestamp) {
            Log.e("onMomentNotificationReceived: " + timestamp + " is older than the current " + oldTimestamp);
        } else {
            preferences.setMomentNotificationId(momentNotification.getNotificationId());
            preferences.setMomentNotificationTimestamp(timestamp);
            preferences.setMomentNotificationType(momentNotification.getTypeValue());
            notifications.showKatchupDailyMomentNotification(timestamp, momentNotification.getNotificationId(), momentNotification.getTypeValue(), momentNotification.getPrompt());
            connection.sendAck(ackId);
        }
    }
}
