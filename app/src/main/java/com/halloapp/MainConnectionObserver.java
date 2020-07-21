package com.halloapp;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.contacts.UserId;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.content.TransferPendingItemsTask;
import com.halloapp.crypto.EncryptedSessionManager;
import com.halloapp.groups.GroupsSync;
import com.halloapp.ui.AppExpirationActivity;
import com.halloapp.ui.RegistrationRequestActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ContactInfo;
import com.halloapp.xmpp.PresenceLoader;
import com.halloapp.xmpp.WhisperKeysMessage;
import com.halloapp.xmpp.groups.MemberElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainConnectionObserver extends Connection.Observer {

    private static MainConnectionObserver instance;

    private final Context context;

    private Me me;
    private BgWorkers bgWorkers;
    private ContentDb contentDb;
    private Connection connection;
    private ContactsDb contactsDb;
    private GroupsSync groupsSync;
    private AvatarLoader avatarLoader;
    private Notifications notifications;
    private ForegroundChat foregroundChat;
    private PresenceLoader presenceLoader;
    private ForegroundObserver foregroundObserver;
    private EncryptedSessionManager encryptedSessionManager;

    public static MainConnectionObserver getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (MainConnectionObserver.class) {
                if (instance == null) {
                    instance = new MainConnectionObserver(
                            context,
                            Me.getInstance(context),
                            BgWorkers.getInstance(),
                            ContentDb.getInstance(context),
                            Connection.getInstance(),
                            ContactsDb.getInstance(context),
                            GroupsSync.getInstance(context),
                            AvatarLoader.getInstance(context),
                            Notifications.getInstance(context),
                            ForegroundChat.getInstance(),
                            PresenceLoader.getInstance(),
                            ForegroundObserver.getInstance(),
                            EncryptedSessionManager.getInstance());
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
            @NonNull Connection connection,
            @NonNull ContactsDb contactsDb,
            @NonNull GroupsSync groupsSync,
            @NonNull AvatarLoader avatarLoader,
            @NonNull Notifications notifications,
            @NonNull ForegroundChat foregroundChat,
            @NonNull PresenceLoader presenceLoader,
            @NonNull ForegroundObserver foregroundObserver,
            @NonNull EncryptedSessionManager encryptedSessionManager) {
        this.context = context.getApplicationContext();

        this.me = me;
        this.bgWorkers = bgWorkers;
        this.contentDb = contentDb;
        this.connection = connection;
        this.contactsDb = contactsDb;
        this.groupsSync = groupsSync;
        this.avatarLoader = avatarLoader;
        this.notifications = notifications;
        this.foregroundChat = foregroundChat;
        this.presenceLoader = presenceLoader;
        this.foregroundObserver = foregroundObserver;
        this.encryptedSessionManager = encryptedSessionManager;
    }

    @Override
    public void onConnected() {
        bgWorkers.execute(() -> {
            try {
                encryptedSessionManager.ensureKeysUploaded();
            } catch (Exception e) {
                Log.e("Failed to ensure keys uploaded", e);
            }
        });

        connection.updatePresence(foregroundObserver.isInForeground());
        new TransferPendingItemsTask(context).execute();
        HalloApp.sendPushTokenFromFirebase();
        new RequestExpirationInfoTask(connection, context).execute();
        presenceLoader.onReconnect();
        groupsSync.startGroupsSync();
    }

    @Override
    public void onDisconnected() {
        presenceLoader.onDisconnect();
    }

    @Override
    public void onLoginFailed() {
        me.resetRegistration();
        if (foregroundObserver.isInForeground()) {
            RegistrationRequestActivity.reVerify(context);
        } else {
            notifications.showLoginFailedNotification();
        }
    }

    @Override
    public void onClientVersionExpired() {
        if (foregroundObserver.isInForeground()) {
            AppExpirationActivity.open(context, 0);
        } else {
            notifications.showExpirationNotification(0);
        }
    }

    @Override
    public void onOutgoingPostSent(@NonNull String postId) {
        contentDb.setPostTransferred(UserId.ME, postId);
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
    public void onOutgoingCommentSent(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull String commentId) {
        contentDb.setCommentTransferred(postSenderUserId, postId, UserId.ME, commentId);
    }

    @Override
    public void onIncomingPostSeenReceiptSent(@NonNull UserId senderUserId, @NonNull String postId) {
        contentDb.setPostSeenReceiptSent(senderUserId, postId);
    }

    @Override
    public void onOutgoingMessageSent(@NonNull String chatId, @NonNull String messageId) {
        contentDb.setMessageTransferred(chatId, UserId.ME, messageId);
    }

    @Override
    public void onOutgoingMessageDelivered(@NonNull String chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {
        contentDb.setOutgoingMessageDelivered(chatId, userId, id, timestamp, () -> connection.sendAck(stanzaId));
    }

    @Override
    public void onOutgoingMessageSeen(@NonNull String chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {
        contentDb.setOutgoingMessageSeen(chatId, userId, id, timestamp, () -> connection.sendAck(stanzaId));
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
        if (message.isRetracted()) {
            contentDb.retractMessage(message, completionRunnable);
        } else {
            contentDb.addMessage(message, !isMessageForForegroundChat, completionRunnable);
        }
    }

    @Override
    public void onIncomingMessageSeenReceiptSent(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        contentDb.setMessageSeenReceiptSent(chatId, senderUserId, messageId);
    }

    @Override
    public void onMessageRerequest(@NonNull UserId peerUserId, @NonNull String messageId, @NonNull String stanzaId) {
        Message message = contentDb.getMessage(peerUserId.rawId(), UserId.ME, messageId);
        if (message != null && message.rerequestCount < Constants.MAX_REREQUESTS_PER_MESSAGE) {
            contentDb.setMessageRerequestCount(peerUserId.rawId(), UserId.ME, messageId, message.rerequestCount + 1);
            encryptedSessionManager.sendMessage(message);
        }
        connection.sendAck(stanzaId);
    }

    @Override
    public void onContactsChanged(@NonNull List<ContactInfo> protocolContacts, @NonNull List<String> contactHashes, @NonNull String ackId) {
        final List<ContactsDb.NormalizedPhoneData> normalizedPhoneDataList = new ArrayList<>(protocolContacts.size());
        for (ContactInfo contact : protocolContacts) {
            normalizedPhoneDataList.add(new ContactsDb.NormalizedPhoneData(contact.normalizedPhone, new UserId(contact.userId), "friends".equals(contact.role), contact.avatarId));
        }
        bgWorkers.execute(() -> {
            try {
                contactsDb.updateNormalizedPhoneData(normalizedPhoneDataList).get();
                if (!contactHashes.isEmpty()) {
                    ContactsSync.getInstance(context).startContactSync(contactHashes);
                }
                connection.sendAck(ackId);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("ConnectionObserver.onContactsChanged", e);
            }
        });
    }

    @Override
    public void onUserNamesReceived(@NonNull Map<UserId, String> names) {
        contactsDb.updateUserNames(names);
    }

    @Override
    public void onPresenceReceived(UserId user, Long lastSeen) {
        presenceLoader.reportPresence(user, lastSeen);
    }

    @Override
    public void onWhisperKeysMessage(WhisperKeysMessage message, @NonNull String ackId) {
        if (message.count != null) {
            int count = message.count;
            Log.i("OTPK count down to " + count + "; replenishing");
            List<byte[]> protoKeys = encryptedSessionManager.getFreshOneTimePreKeyProtos();
            connection.uploadMoreOneTimePreKeys(protoKeys);
            connection.sendAck(ackId);
        } else if (message.userId != null) {
            encryptedSessionManager.tearDownSession(message.userId);
            connection.sendAck(ackId);
        }
    }

    @Override
    public void onAvatarChangeMessageReceived(UserId userId, String avatarId, @NonNull String ackId) {
        avatarLoader.reportAvatarUpdate(userId, avatarId);
        connection.sendAck(ackId);
    }

    // TODO(jack): Make GroupsDb to store these changes
    @Override
    public void onGroupMemberChangeReceived(@NonNull String groupId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        connection.sendAck(ackId);
    }

    @Override
    public void onGroupMemberLeftReceived(@NonNull String groupId, @NonNull List<MemberElement> members, @NonNull String ackId) {
        connection.sendAck(ackId);
    }

    @Override
    public void onGroupAdminChangeReceived(@NonNull String groupId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        connection.sendAck(ackId);
    }

    @Override
    public void onGroupNameChangeReceived(@NonNull String groupId, @NonNull String name, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        contentDb.setGroupName(groupId, name, () -> connection.sendAck(ackId));
    }

    @Override
    public void onGroupAvatarChangeReceived(@NonNull String groupId, @NonNull String avatarId, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        contentDb.setGroupAvatar(groupId, avatarId, () -> connection.sendAck(ackId));
    }

    @Override
    public void onGroupAdminAutoPromoteReceived(@NonNull String groupId, @NonNull List<MemberElement> members, @NonNull String ackId) {
        connection.sendAck(ackId);
    }
}
