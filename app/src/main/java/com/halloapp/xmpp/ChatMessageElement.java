package com.halloapp.xmpp;

import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentDb;
import com.halloapp.content.FutureProofMessage;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.content.VoiceNoteMessage;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.EncryptedSessionManager;
import com.halloapp.crypto.SessionSetupInfo;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.id.UserId;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.Album;
import com.halloapp.proto.clients.AlbumMedia;
import com.halloapp.proto.clients.ChatContainer;
import com.halloapp.proto.clients.ChatContext;
import com.halloapp.proto.clients.ChatMessage;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.clients.Text;
import com.halloapp.proto.clients.VoiceNote;
import com.halloapp.proto.server.ChatStanza;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Stats;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessageElement {

    private ChatMessage chatMessage;
    private ChatContainer chatContainer;
    private final long timestamp;
    private final String senderName;
    private final UserId recipientUserId;
    private final SessionSetupInfo sessionSetupInfo;
    private final byte[] encryptedBytes;

    private final Stats stats = Stats.getInstance();
    private final ContentDb contentDb = ContentDb.getInstance();
    private final ServerProps serverProps = ServerProps.getInstance();
    private final EncryptedKeyStore encryptedKeyStore = EncryptedKeyStore.getInstance();

    ChatMessageElement(@NonNull Message message, UserId recipientUserId, @Nullable SessionSetupInfo sessionSetupInfo) {
        this.chatMessage = MessageElementHelper.messageToChatMessage(message);
        this.chatContainer = MessageElementHelper.messageToChatContainer(message);
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
    Message getMessage(UserId fromUserId, String id, String senderAgent) {
        Log.i("Local state relevant to message " + id + " from:" + getLogInfo(fromUserId));
        String senderPlatform = senderAgent.contains("Android") ? "android" : senderAgent.contains("iOS") ? "ios" : "";
        String senderVersion = senderPlatform.equals("android") ? senderAgent.split("Android")[1] : senderPlatform.equals("ios") ? senderAgent.split("iOS")[1] : "";
        String failureReason = null;
        if (encryptedBytes != null) {
            try {
                final byte[] dec = EncryptedSessionManager.getInstance().decryptMessage(this.encryptedBytes, fromUserId, sessionSetupInfo);
                try {
                    Container container = Container.parseFrom(dec);
                    // TODO: (clarkc) remove legacy proto format once clients are all sending new format
                    if (!container.hasChatContainer()) {
                        chatMessage = MessageElementHelper.readEncodedEntry(dec);
                        stats.reportDecryptSuccess(senderPlatform, senderVersion);
                    } else {
                        chatContainer = container.getChatContainer();
                        stats.reportDecryptSuccess(senderPlatform, senderVersion);
                    }
                } catch (InvalidProtocolBufferException e) {
                    chatMessage = null;
                    chatContainer = null;
                    Log.e("Payload not a valid container", e);
                }
            } catch (CryptoException | ArrayIndexOutOfBoundsException e) {
                failureReason = e instanceof CryptoException ? e.getMessage() : "aioobe";
                Log.e("Failed to decrypt message: " + failureReason + ", falling back to plaintext", e);
                Log.sendErrorReport("Decryption failure: " + failureReason);
                stats.reportDecryptError(failureReason, senderPlatform, senderVersion);

                if (Constants.REREQUEST_SEND_ENABLED) {
                    Log.i("Rerequesting message " + id);
                    int count;
                    count = contentDb.getMessageRerequestCount(fromUserId, fromUserId, id);
                    count += 1;
                    contentDb.setMessageRerequestCount(fromUserId, fromUserId, id, count);
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
        } else if (chatContainer != null) {
            message = Message.parseFromProto(fromUserId, id, timestamp, chatContainer);
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
        if (chatMessage != null) {
            containerBuilder.setChatMessage(chatMessage);
        }
        if (serverProps.getNewClientContainerEnabled()) {
            containerBuilder.setChatContainer(chatContainer);
        }
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
        } catch (NullPointerException | CryptoException e) {
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

        ChatMessageElement ret = null;
        if (encrypted != null && encrypted.size() > 0) {
            ByteString identityKey = chatStanza.getPublicKey();
            ret = readEncryptedEntryProto(encrypted.toByteArray(), identityKey.toByteArray(), (int) chatStanza.getOneTimePreKeyId(), timestamp, chatStanza.getSenderName());
        }

        return ret;
    }
}
