package com.halloapp.xmpp.chat;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.crypto.signal.SessionSetupInfo;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.id.UserId;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.ChatContainer;
import com.halloapp.proto.clients.ChatMessage;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.server.ChatStanza;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Stats;
import com.halloapp.xmpp.MessageElementHelper;

public class ChatMessageProtocol {

    private static ChatMessageProtocol instance;

    public static ChatMessageProtocol getInstance() {
        if (instance == null) {
            synchronized (ChatMessageProtocol.class) {
                if (instance == null) {
                    instance = new ChatMessageProtocol(
                            Stats.getInstance(),
                            ContentDb.getInstance(),
                            ServerProps.getInstance(),
                            EncryptedKeyStore.getInstance(),
                            SignalSessionManager.getInstance());
                }
            }
        }
        return instance;
    }

    private final Stats stats;
    private final ContentDb contentDb;
    private final ServerProps serverProps;
    private final EncryptedKeyStore encryptedKeyStore;
    private final SignalSessionManager signalSessionManager;

    private ChatMessageProtocol(
            @NonNull Stats stats,
            @NonNull ContentDb contentDb,
            @NonNull ServerProps serverProps,
            @NonNull EncryptedKeyStore encryptedKeyStore,
            @NonNull SignalSessionManager signalSessionManager) {
        this.stats = stats;
        this.contentDb = contentDb;
        this.serverProps = serverProps;
        this.encryptedKeyStore = encryptedKeyStore;
        this.signalSessionManager = signalSessionManager;
    }

    public ChatStanza serializeMessage(@NonNull Message message, UserId recipientUserId, @Nullable SessionSetupInfo sessionSetupInfo) {
        ChatStanza.Builder builder = ChatStanza.newBuilder();
        builder.setSenderLogInfo(encryptedKeyStore.getLogInfo(recipientUserId));

        if (sessionSetupInfo != null) {
            builder.setPublicKey(ByteString.copyFrom(sessionSetupInfo.identityKey.getKeyMaterial()));
            if (sessionSetupInfo.oneTimePreKeyId != null) {
                builder.setOneTimePreKeyId(sessionSetupInfo.oneTimePreKeyId);
            }
        }

        byte[] encryptedEntry = encryptMessage(message, recipientUserId);
        if (encryptedEntry != null) {
            builder.setEncPayload(ByteString.copyFrom(encryptedEntry));
        }

        return builder.build();
    }

    private byte[] encryptMessage(@NonNull Message message, @NonNull UserId recipientUserId) {
        try {
            byte[] encodedEntry = serializeMessageToBytes(message);
            byte[] encryptedEntry = signalSessionManager.encryptMessage(encodedEntry, recipientUserId);
            stats.reportEncryptSuccess();
            return encryptedEntry;
        } catch (CryptoException e) {
            String errorMessage = e.getMessage();
            Log.e("Failed to encrypt: " + errorMessage, e);
            Log.sendErrorReport("Encryption failure: " + errorMessage);
            stats.reportEncryptError(errorMessage);
        }
        return null;
    }

    private byte[] serializeMessageToBytes(@NonNull Message message) {
        Container.Builder containerBuilder = Container.newBuilder();
        ChatMessage chatMessage = MessageElementHelper.messageToChatMessage(message);
        ChatContainer chatContainer = MessageElementHelper.messageToChatContainer(message);

        if (chatMessage != null) {
            containerBuilder.setChatMessage(chatMessage);
        }
        if (serverProps.getNewClientContainerEnabled()) {
            containerBuilder.setChatContainer(chatContainer);
        }

        return containerBuilder.build().toByteArray();
    }

    @Nullable
    public Message parseMessage(@NonNull ChatStanza chatStanza, String id, UserId fromUserId) {
        long timestamp = chatStanza.getTimestamp() * 1000L;
        ByteString encrypted = chatStanza.getEncPayload();
        if (encrypted == null || encrypted.size() <= 0) {
            return null;
        }
        byte[] encryptedBytes = encrypted.toByteArray();
        byte[] identityKeyBytes = chatStanza.getPublicKey().toByteArray();
        PublicEdECKey identityKey = identityKeyBytes == null || identityKeyBytes.length == 0 ? null : new PublicEdECKey(identityKeyBytes);

        SessionSetupInfo sessionSetupInfo = new SessionSetupInfo(identityKey, (int) chatStanza.getOneTimePreKeyId());
        String senderAgent = chatStanza.getSenderClientVersion();

        Log.i("Local state relevant to message " + id + " from:" + encryptedKeyStore.getLogInfo(fromUserId));
        String senderPlatform = senderAgent.contains("Android") ? "android" : senderAgent.contains("iOS") ? "ios" : "";
        String senderVersion = senderPlatform.equals("android") ? senderAgent.split("Android")[1] : senderPlatform.equals("ios") ? senderAgent.split("iOS")[1] : "";
        String failureReason = null;
        ChatMessage chatMessage = null;
        ChatContainer chatContainer = null;
        if (encryptedBytes != null) {
            try {
                final byte[] dec = signalSessionManager.decryptMessage(encryptedBytes, fromUserId, sessionSetupInfo);
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
                    signalSessionManager.sendRerequest(fromUserId, id, count, teardownKey);
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
}
