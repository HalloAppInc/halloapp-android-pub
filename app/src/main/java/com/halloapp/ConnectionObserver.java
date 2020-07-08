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
import com.halloapp.ui.AppExpirationActivity;
import com.halloapp.ui.RegistrationRequestActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ContactInfo;
import com.halloapp.xmpp.PresenceLoader;
import com.halloapp.xmpp.WhisperKeysMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ConnectionObserver implements Connection.Observer {

    private final Context context;

    ConnectionObserver(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void onConnected() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            try {
                EncryptedSessionManager.getInstance().ensureKeysUploaded(Connection.getInstance());
            } catch (Exception e) {
                Log.e("Failed to ensure keys uploaded", e);
            }
        });

        Connection.getInstance().updatePresence(ForegroundObserver.getInstance().isInForeground());
        new TransferPendingItemsTask(context).execute();
        HalloApp.sendPushTokenFromFirebase();
        new RequestExpirationInfoTask(Connection.getInstance(), context).execute();
        PresenceLoader.getInstance(Connection.getInstance()).onReconnect();
    }

    @Override
    public void onDisconnected() {
        PresenceLoader.getInstance(Connection.getInstance()).onDisconnect();
    }

    @Override
    public void onLoginFailed() {
        Me.getInstance(context).resetRegistration();
        if (ForegroundObserver.getInstance().isInForeground()) {
            RegistrationRequestActivity.reVerify(context);
        } else {
            Notifications.getInstance(context).showLoginFailedNotification();
        }
    }

    @Override
    public void onClientVersionExpired() {
        if (ForegroundObserver.getInstance().isInForeground()) {
            AppExpirationActivity.open(context, 0);
        } else {
            Notifications.getInstance(context).showExpirationNotification(0);
        }
    }

    @Override
    public void onOutgoingPostSent(@NonNull String postId) {
        ContentDb.getInstance(context).setPostTransferred(UserId.ME, postId);
    }

    @Override
    public void onIncomingFeedItemsReceived(@NonNull List<Post> posts, @NonNull List<Comment> comments, @NonNull String ackId) {
        ContentDb.getInstance(context).addFeedItems(posts, comments, () -> Connection.getInstance().sendAck(ackId));
    }

    @Override
    public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId, long timestamp, @NonNull String ackId) {
        ContentDb.getInstance(context).setOutgoingPostSeen(seenByUserId, postId, timestamp, () -> Connection.getInstance().sendAck(ackId));
    }

    @Override
    public void onOutgoingCommentSent(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull String commentId) {
        ContentDb.getInstance(context).setCommentTransferred(postSenderUserId, postId, UserId.ME, commentId);
    }

    @Override
    public void onIncomingPostSeenReceiptSent(@NonNull UserId senderUserId, @NonNull String postId) {
        ContentDb.getInstance(context).setPostSeenReceiptSent(senderUserId, postId);
    }

    @Override
    public void onOutgoingMessageSent(@NonNull String chatId, @NonNull String messageId) {
        ContentDb.getInstance(context).setMessageTransferred(chatId, UserId.ME, messageId);
    }

    @Override
    public void onOutgoingMessageDelivered(@NonNull String chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {
        ContentDb.getInstance(context).setOutgoingMessageDelivered(chatId, userId, id, timestamp, () -> Connection.getInstance().sendAck(stanzaId));
    }

    @Override
    public void onOutgoingMessageSeen(@NonNull String chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {
        ContentDb.getInstance(context).setOutgoingMessageSeen(chatId, userId, id, timestamp, () -> Connection.getInstance().sendAck(stanzaId));
    }

    @Override
    public void onIncomingMessageReceived(@NonNull Message message) {
        final boolean isMessageForForegroundChat = ForegroundChat.getInstance().isForegroundChatId(message.chatId);
        final Runnable completionRunnable = () -> {
            final Connection connection = Connection.getInstance();
            if (isMessageForForegroundChat) {
                connection.sendMessageSeenReceipt(message.chatId, message.senderUserId, message.id);
            }
            connection.sendAck(message.id);
        };
        if (message.isRetracted()) {
            ContentDb.getInstance(context).retractMessage(message, completionRunnable);
        } else {
            ContentDb.getInstance(context).addMessage(message, !isMessageForForegroundChat, completionRunnable);
        }
    }

    @Override
    public void onIncomingMessageSeenReceiptSent(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        ContentDb.getInstance(context).setMessageSeenReceiptSent(chatId, senderUserId, messageId);
    }

    @Override
    public void onMessageRerequest(@NonNull UserId peerUserId, @NonNull String messageId, @NonNull String stanzaId) {
        ContentDb contentDb = ContentDb.getInstance(context);
        Message message = contentDb.getMessage(peerUserId.rawId(), UserId.ME, messageId);
        if (message != null && message.rerequestCount < Constants.MAX_REREQUESTS_PER_MESSAGE) {
            contentDb.setMessageRerequestCount(peerUserId.rawId(), UserId.ME, messageId, message.rerequestCount + 1);
            EncryptedSessionManager.getInstance().sendMessage(message);
        }
        Connection.getInstance().sendAck(stanzaId);
    }

    @Override
    public void onContactsChanged(@NonNull List<ContactInfo> protocolContacts, @NonNull List<String> contactHashes, @NonNull String ackId) {
        final List<ContactsDb.NormalizedPhoneData> normalizedPhoneDataList = new ArrayList<>(protocolContacts.size());
        for (ContactInfo contact : protocolContacts) {
            normalizedPhoneDataList.add(new ContactsDb.NormalizedPhoneData(contact.normalizedPhone, new UserId(contact.userId), "friends".equals(contact.role), contact.avatarId));
        }
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            try {
                ContactsDb.getInstance(context).updateNormalizedPhoneData(normalizedPhoneDataList).get();
                if (!contactHashes.isEmpty()) {
                    ContactsSync.getInstance(context).startContactSync(contactHashes);
                }
                Connection.getInstance().sendAck(ackId);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("ConnectionObserver.onContactsChanged", e);
            }
        });
    }

    @Override
    public void onUserNamesReceived(@NonNull Map<UserId, String> names) {
        ContactsDb.getInstance(context).updateUserNames(names);
    }

    @Override
    public void onWhisperKeysMessage(WhisperKeysMessage message, @NonNull String ackId) {
        if (message.count != null) {
            int count = message.count;
            Log.i("OTPK count down to " + count + "; replenishing");
            List<byte[]> protoKeys = EncryptedSessionManager.getInstance().getFreshOneTimePreKeyProtos();
            Connection.getInstance().uploadMoreOneTimePreKeys(protoKeys);
            Connection.getInstance().sendAck(ackId);
        } else if (message.userId != null) {
            EncryptedSessionManager.getInstance().tearDownSession(message.userId);
            Connection.getInstance().sendAck(ackId);
        }
    }

    @Override
    public void onAvatarChangeMessageReceived(UserId userId, String avatarId, @NonNull String ackId) {
        AvatarLoader.getInstance(Connection.getInstance(), context).reportAvatarUpdate(userId, avatarId);
        Connection.getInstance().sendAck(ackId);
    }
}
