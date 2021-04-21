package com.halloapp.xmpp;

import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.EncryptedSessionManager;
import com.halloapp.crypto.SessionSetupInfo;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.id.UserId;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.ChatMessage;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.server.ChatStanza;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Stats;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessageElement {

    private ChatMessage chatMessage;
    private final long timestamp;
    private final String senderName;
    private final UserId recipientUserId;
    private final SessionSetupInfo sessionSetupInfo;
    private final byte[] encryptedBytes;
    private ChatMessage plaintextChatMessage = null; // TODO(jack): Remove before removing s1 XML tag

    private final Stats stats = Stats.getInstance();
    private final ContentDb contentDb = ContentDb.getInstance();
    private final ServerProps serverProps = ServerProps.getInstance();
    private final EncryptedKeyStore encryptedKeyStore = EncryptedKeyStore.getInstance();

    ChatMessageElement(@NonNull Message message, UserId recipientUserId, @Nullable SessionSetupInfo sessionSetupInfo) {
        this.chatMessage = MessageElementHelper.messageToChatMessage(message);
        this.timestamp = 0;
        this.senderName = null;
        this.recipientUserId = recipientUserId;
        this.sessionSetupInfo = sessionSetupInfo;
        this.encryptedBytes = null;
    }

    private ChatMessageElement(byte[] encryptedBytes, SessionSetupInfo sessionSetupInfo, long timestamp, String senderName) {
        this.chatMessage = null;
        this.timestamp = timestamp;
        this.senderName = senderName;
        this.recipientUserId = null;
        this.sessionSetupInfo = sessionSetupInfo;
        this.encryptedBytes = encryptedBytes;
    }

    private ChatMessageElement(@NonNull ChatMessage chatMessage, long timestamp) {
        this.chatMessage = chatMessage;
        this.timestamp = timestamp;
        this.senderName = null;
        this.recipientUserId = null;
        this.sessionSetupInfo = null;
        this.encryptedBytes = null;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSenderName() {
        return senderName;
    }

    @NonNull
    Message getMessage(UserId fromUserId, String id, boolean isSilentMessage, String senderAgent) {
        boolean isFriend = ContactsDb.getInstance().getContact(fromUserId).friend;
        Log.i("Local state relevant to message " + id + " from " + (isFriend ? "friend" : "non-friend") + ": " + getLogInfo(fromUserId));
        String senderPlatform = senderAgent.contains("Android") ? "android" : senderAgent.contains("iOS") ? "ios" : "";
        String senderVersion = senderPlatform.equals("android") ? senderAgent.split("Android")[1] : senderPlatform.equals("ios") ? senderAgent.split("iOS")[1] : "";
        String failureReason = null;
        if (encryptedBytes != null) {
            try {
                final byte[] dec = EncryptedSessionManager.getInstance().decryptMessage(this.encryptedBytes, fromUserId, sessionSetupInfo);
                chatMessage = MessageElementHelper.readEncodedEntry(dec);
                if (plaintextChatMessage != null && !plaintextChatMessage.equals(chatMessage)) {
                    Log.sendErrorReport("Decrypted message does not match plaintext");
                    failureReason = "plaintext_mismatch";
                    stats.reportDecryptError(failureReason, senderPlatform, senderVersion);
                } else {
                    stats.reportDecryptSuccess(senderPlatform, senderVersion);
                }
            } catch (CryptoException | ArrayIndexOutOfBoundsException e) {
                failureReason = e instanceof CryptoException ? e.getMessage() : "aioobe";
                Log.e("Failed to decrypt message: " + failureReason + ", falling back to plaintext", e);
                Log.sendErrorReport("Decryption failure: " + failureReason);
                stats.reportDecryptError(failureReason, senderPlatform, senderVersion);

                // TODO(jack): Remove this block to enable tombstones for all users
                if (!ServerProps.getInstance().getIsInternalUser()) {
                    chatMessage = plaintextChatMessage;
                }

                if (Constants.REREQUEST_SEND_ENABLED) {
                    Log.i("Rerequesting message " + id);
                    int count;
                    if (isSilentMessage) {
                        count = contentDb.getSilentMessageRerequestCount(fromUserId, id);
                        count += 1;
                        contentDb.setSilentMessageRerequestCount(fromUserId, id, count);
                    } else {
                        count = contentDb.getMessageRerequestCount(fromUserId, fromUserId, id);
                        count += 1;
                        contentDb.setMessageRerequestCount(fromUserId, fromUserId, id, count);
                    }
                    byte[] teardownKey = e instanceof CryptoException ? ((CryptoException) e).teardownKey : null;
                    EncryptedSessionManager.getInstance().sendRerequest(fromUserId, id, count, teardownKey);
                }
            }
        }

        final Message message;
        if (chatMessage != null) {
            String rawReplyMessageId = chatMessage.getChatReplyMessageId();
            String rawSenderId = chatMessage.getChatReplyMessageSenderId();
            message = new Message(0,
                    fromUserId,
                    fromUserId,
                    id,
                    timestamp,
                    Message.TYPE_CHAT,
                    Message.USAGE_CHAT,
                    chatMessage.getMediaCount() == 0 ? Message.STATE_INCOMING_RECEIVED : Message.STATE_INITIAL,
                    chatMessage.getText(),
                    chatMessage.getFeedPostId(),
                    chatMessage.getFeedPostMediaIndex(),
                    TextUtils.isEmpty(rawReplyMessageId) ? null : rawReplyMessageId,
                    chatMessage.getChatReplyMessageMediaIndex(),
                    rawSenderId.equals(Me.getInstance().getUser()) ? UserId.ME : new UserId(rawSenderId),
                    0);
            for (com.halloapp.proto.clients.Media item : chatMessage.getMediaList()) {
                message.media.add(Media.createFromUrl(
                        MessageElementHelper.fromProtoMediaType(item.getType()),
                        item.getDownloadUrl(),
                        item.getEncryptionKey().toByteArray(),
                        item.getCiphertextHash().toByteArray(),
                        item.getWidth(),
                        item.getHeight()));
            }
            for (com.halloapp.proto.clients.Mention item : chatMessage.getMentionsList()) {
                message.mentions.add(Mention.parseFromProto(item));
            }
        } else {
            message = new Message(0,
                    fromUserId,
                    fromUserId,
                    id,
                    timestamp,
                    Message.TYPE_CHAT,
                    Message.USAGE_CHAT,
                    Message.STATE_INCOMING_DECRYPT_FAILED,
                    "",
                    "",
                    0,
                    null,
                    0,
                    UserId.ME,
                    0
                    );
        }

        message.senderPlatform = senderPlatform;
        message.senderVersion = senderVersion;
        message.clientVersion = BuildConfig.VERSION_NAME + (BuildConfig.DEBUG ? "D" : "");
        message.failureReason = failureReason;

        return message;
    }

    private byte[] getEncryptedEntry() {
        try {
            byte[] encodedEntry = getEncodedEntry();
            byte[] encryptedEntry = EncryptedSessionManager.getInstance().encryptMessage(encodedEntry, recipientUserId);
            stats.reportEncryptSuccess();
            return encryptedEntry;
        } catch (CryptoException e) {
            String message = e.getMessage();
            Log.e("Failed to encrypt: " + message, e);
            Log.sendErrorReport("Encryption failure: " + message);
            stats.reportEncryptError(message);
        }
        return null;
    }

    private byte[] getEncodedEntry() {
        Container.Builder containerBuilder = Container.newBuilder();
        containerBuilder.setChatMessage(chatMessage);
        return containerBuilder.build().toByteArray();
    }

    private static ChatMessageElement readEncryptedEntryProto(@NonNull byte[] encryptedBytes, byte[] identityKeyBytes, Integer oneTimePreKeyId, long timestamp, String senderName) {
        PublicEdECKey identityKey = identityKeyBytes == null || identityKeyBytes.length == 0 ? null : new PublicEdECKey(identityKeyBytes);
        return new ChatMessageElement(encryptedBytes, new SessionSetupInfo(identityKey, oneTimePreKeyId), timestamp, senderName);
    }

    private String getLogInfo(UserId userId) {
        StringBuilder sb = new StringBuilder();

        sb.append("TS: ");
        DateFormat df = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG, Locale.US);
        sb.append(df.format(new Date()));

        try {
            sb.append("; MIK:");
            sb.append(Base64.encodeToString(encryptedKeyStore.getMyPublicEd25519IdentityKey().getKeyMaterial(), Base64.NO_WRAP));
        } catch (NullPointerException e) {
            sb.append("null");
        }

        try {
            sb.append("; PIK:");
            sb.append(Base64.encodeToString(encryptedKeyStore.getPeerPublicIdentityKey(userId).getKeyMaterial(), Base64.NO_WRAP));
        } catch (CryptoException e) {
            Log.w("Failed to get peer public identity key", e);
            sb.append("CryptoException");
        }

        sb.append("; MICK:").append(CryptoUtils.obfuscate(encryptedKeyStore.getInboundChainKey(userId)));
        sb.append("; MOCK:").append(CryptoUtils.obfuscate(encryptedKeyStore.getOutboundChainKey(userId)));

        return sb.toString();
    }

    public ChatStanza toProto() {
        ChatStanza.Builder builder = ChatStanza.newBuilder();
        builder.setSenderLogInfo(getLogInfo(recipientUserId));

        if (serverProps.getCleartextChatMessagesEnabled()) {
            builder.setPayload(ByteString.copyFrom(getEncodedEntry()));
        }

        if (sessionSetupInfo != null) {
            builder.setPublicKey(ByteString.copyFrom(sessionSetupInfo.identityKey.getKeyMaterial()));
            if (sessionSetupInfo.oneTimePreKeyId != null) {
                builder.setOneTimePreKeyId(sessionSetupInfo.oneTimePreKeyId);
            }
        }

        byte[] encryptedEntry = getEncryptedEntry();
        if (encryptedEntry != null) {
            builder.setEncPayload(ByteString.copyFrom(encryptedEntry));
        }

        return builder.build();
    }

    public static ChatMessageElement fromProto(@NonNull ChatStanza chatStanza) {
        long timestamp = chatStanza.getTimestamp() * 1000L;

        ByteString encrypted = chatStanza.getEncPayload();
        ByteString plaintext = chatStanza.getPayload();

        ChatMessage plaintextChatMessage = null;
        if (plaintext != null && !ServerProps.getInstance().getIsInternalUser()) {
            plaintextChatMessage = MessageElementHelper.readEncodedEntry(plaintext.toByteArray());
        }

        ChatMessageElement ret = null;
        if (encrypted != null && encrypted.size() > 0) {
            ByteString identityKey = chatStanza.getPublicKey();
            ret = readEncryptedEntryProto(encrypted.toByteArray(), identityKey.toByteArray(), (int) chatStanza.getOneTimePreKeyId(), timestamp, chatStanza.getSenderName());
            ret.plaintextChatMessage = plaintextChatMessage;
        }

        if (ret == null) {
            ret = new ChatMessageElement(plaintextChatMessage, timestamp);
        }

        return ret;
    }
}
