package com.halloapp.xmpp.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.Constants;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.content.Reaction;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.group.GroupFeedSessionManager;
import com.halloapp.crypto.group.GroupSetupInfo;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.crypto.signal.SignalSessionSetupInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.ChatContainer;
import com.halloapp.proto.clients.ChatContext;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.clients.EncryptedPayload;
import com.halloapp.proto.clients.SenderKey;
import com.halloapp.proto.clients.SenderState;
import com.halloapp.proto.server.ChatStanza;
import com.halloapp.proto.server.GroupChat;
import com.halloapp.proto.server.GroupChatStanza;
import com.halloapp.proto.server.SenderStateWithKeyInfo;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Stats;
import com.halloapp.xmpp.MediaCounts;
import com.halloapp.xmpp.MessageElementHelper;
import com.halloapp.xmpp.ProtoPrinter;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ChatMessageProtocol {

    private static ChatMessageProtocol instance;

    public static ChatMessageProtocol getInstance() {
        if (instance == null) {
            synchronized (ChatMessageProtocol.class) {
                if (instance == null) {
                    instance = new ChatMessageProtocol(
                            Stats.getInstance(),
                            ContentDb.getInstance(),
                            EncryptedKeyStore.getInstance(),
                            SignalSessionManager.getInstance());
                }
            }
        }
        return instance;
    }

    private final Stats stats;
    private final ContentDb contentDb;
    private final EncryptedKeyStore encryptedKeyStore;
    private final SignalSessionManager signalSessionManager;

    private ChatMessageProtocol(
            @NonNull Stats stats,
            @NonNull ContentDb contentDb,
            @NonNull EncryptedKeyStore encryptedKeyStore,
            @NonNull SignalSessionManager signalSessionManager) {
        this.stats = stats;
        this.contentDb = contentDb;
        this.encryptedKeyStore = encryptedKeyStore;
        this.signalSessionManager = signalSessionManager;
    }

    public ChatStanza serializeReaction(@NonNull Reaction reaction, UserId recipientUserId, @Nullable SignalSessionSetupInfo signalSessionSetupInfo) {
        ChatStanza.Builder builder = ChatStanza.newBuilder();
        builder.setChatType(ChatStanza.ChatType.CHAT_REACTION);
        builder.setSenderLogInfo(encryptedKeyStore.getLogInfo(recipientUserId));
        builder.setSenderClientVersion(Constants.USER_AGENT);

        if (signalSessionSetupInfo != null) {
            builder.setPublicKey(ByteString.copyFrom(signalSessionSetupInfo.identityKey.getKeyMaterial()));
            if (signalSessionSetupInfo.oneTimePreKeyId != null) {
                builder.setOneTimePreKeyId(signalSessionSetupInfo.oneTimePreKeyId);
            }
        }

        byte[] encryptedEntry = encryptReaction(reaction, recipientUserId);
        if (encryptedEntry != null) {
            builder.setEncPayload(ByteString.copyFrom(encryptedEntry));
        }

        return builder.build();
    }

    private byte[] encryptReaction(@NonNull Reaction reaction, @NonNull UserId recipientUserId) {
        try {
            byte[] encodedEntry = serializeReactionToBytes(reaction);
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

    private byte[] serializeReactionToBytes(@NonNull Reaction reaction) {
        Container.Builder containerBuilder = Container.newBuilder();
        ChatContainer chatContainer = ChatContainer.newBuilder()
                .setReaction(com.halloapp.proto.clients.Reaction.newBuilder().setEmoji(reaction.reactionType))
                .setContext(ChatContext.newBuilder().setChatReplyMessageId(reaction.contentId).setChatReplyMessageSenderId(reaction.senderUserId.rawId()))
                .build();

        containerBuilder.setChatContainer(chatContainer);

        return containerBuilder.build().toByteArray();
    }

    public ChatStanza serializeMessage(@NonNull Message message, UserId recipientUserId, @Nullable SignalSessionSetupInfo signalSessionSetupInfo) {
        ChatStanza.Builder builder = ChatStanza.newBuilder();
        builder.setChatType(ChatStanza.ChatType.CHAT);
        builder.setSenderLogInfo(encryptedKeyStore.getLogInfo(recipientUserId));
        builder.setSenderClientVersion(Constants.USER_AGENT);

        if (signalSessionSetupInfo != null) {
            builder.setPublicKey(ByteString.copyFrom(signalSessionSetupInfo.identityKey.getKeyMaterial()));
            if (signalSessionSetupInfo.oneTimePreKeyId != null) {
                builder.setOneTimePreKeyId(signalSessionSetupInfo.oneTimePreKeyId);
            }
        }

        byte[] encryptedEntry = encryptMessage(message, recipientUserId);
        if (encryptedEntry != null) {
            builder.setEncPayload(ByteString.copyFrom(encryptedEntry));
        }

        builder.setMediaCounters(new MediaCounts(message.media).toProto());

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
        ChatContainer chatContainer = MessageElementHelper.messageToChatContainer(message);

        containerBuilder.setChatContainer(chatContainer);

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

        SignalSessionSetupInfo signalSessionSetupInfo = new SignalSessionSetupInfo(identityKey, (int) chatStanza.getOneTimePreKeyId());
        String senderAgent = chatStanza.getSenderClientVersion();

        Log.i("Local state relevant to message " + id + " from:" + encryptedKeyStore.getLogInfo(fromUserId));
        String senderPlatform = senderAgent == null ? "" : senderAgent.contains("Android") ? "android" : senderAgent.contains("iOS") ? "ios" : "";
        String senderVersion = senderPlatform.equals("android") ? senderAgent.split("Android")[1] : senderPlatform.equals("ios") ? senderAgent.split("iOS")[1] : "";
        String failureReason = null;
        ChatContainer chatContainer = null;
        int rerequestCount = contentDb.getMessageRerequestCount(fromUserId, fromUserId, id);
        if (encryptedBytes != null) {
            try {
                final byte[] dec = signalSessionManager.decryptMessage(encryptedBytes, fromUserId, signalSessionSetupInfo);
                try {
                    Container container = Container.parseFrom(dec);
                    chatContainer = container.getChatContainer();
                    stats.reportDecryptSuccess(senderPlatform, senderVersion);
                } catch (InvalidProtocolBufferException e) {
                    Log.e("Payload not a valid container", e);
                }
            } catch (CryptoException | ArrayIndexOutOfBoundsException e) {
                failureReason = e instanceof CryptoException ? e.getMessage() : "aioobe";
                Log.e("Failed to decrypt message: " + failureReason + ", falling back to plaintext", e);
                Log.sendErrorReport("Decryption failure: " + failureReason);
                stats.reportDecryptError(failureReason, senderPlatform, senderVersion);

                Log.i("Rerequesting message " + id);
                rerequestCount += 1;
                byte[] teardownKey = e instanceof CryptoException ? ((CryptoException) e).teardownKey : null;
                signalSessionManager.sendRerequest(fromUserId, id, ChatStanza.ChatType.CHAT_REACTION.equals(chatStanza.getChatType()), rerequestCount, teardownKey);
            }
        }

        final Message message;
        if (chatContainer != null) {
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
                    rerequestCount
            );
        }

        message.senderPlatform = senderPlatform;
        message.senderVersion = senderVersion;
        message.clientVersion = Constants.FULL_VERSION;
        message.failureReason = failureReason;

        return message;
    }

    private byte[] decryptGroupMessage(byte[] encPayload, GroupId groupId, UserId fromUserId) throws CryptoException {
        byte[] decPayload;
        try {
            EncryptedPayload encryptedPayload = EncryptedPayload.parseFrom(encPayload);
            switch (encryptedPayload.getPayloadCase()) {
                case SENDER_STATE_ENCRYPTED_PAYLOAD: {
                    byte[] toDecrypt = encryptedPayload.getSenderStateEncryptedPayload().toByteArray();
                    decPayload = GroupFeedSessionManager.getInstance().decryptMessage(toDecrypt, groupId, fromUserId);
                    break;
                }
                case ONE_TO_ONE_ENCRYPTED_PAYLOAD: {
                    byte[] toDecrypt = encryptedPayload.getOneToOneEncryptedPayload().toByteArray();
                    decPayload = SignalSessionManager.getInstance().decryptMessage(toDecrypt, fromUserId, null);
                    break;
                }
                default: {
                    throw new CryptoException("no_accepted_enc_payload");
                }
            }
        } catch (InvalidProtocolBufferException e) {
            throw new CryptoException("grp_invalid_proto", e);
        }
        return decPayload;
    }

    private byte[] encryptGroupMessage(@NonNull Message message)  {
        if (!(message.chatId instanceof GroupId)) {
            Log.e("Attempting to encrypt a non-group message");
            return null;
        }
        try {
            GroupId groupId = (GroupId) message.chatId;

            byte[] encryptedPayload = GroupFeedSessionManager.getInstance().encryptMessage(serializeMessageToBytes(message), groupId);
            Stats.getInstance().reportGroupEncryptSuccess(false);
            return encryptedPayload;
        } catch (CryptoException e) {
            String errorMessage = e.getMessage();
            Log.e("Failed to encrypt group message", e);
            Log.sendErrorReport("Group message encrypt failed: " + errorMessage);
            Stats.getInstance().reportGroupEncryptError(errorMessage, false);
        }
        return null;
    }

    public GroupChatStanza serializeGroupMessage(@NonNull Message message) {
        return encryptGroupMessageAsChatStanza(message).build();
    }

    private GroupChatStanza.Builder encryptGroupMessageAsChatStanza(@NonNull Message message) {
        GroupChatStanza.Builder builder = GroupChatStanza.newBuilder();
        builder.setChatType(GroupChatStanza.ChatType.CHAT);
        GroupId groupId = (GroupId) message.chatId;

        builder.setGid(groupId.rawId());
        builder.setSenderClientVersion(Constants.USER_AGENT);

        GroupSetupInfo groupSetupInfo = null;
        try {
            groupSetupInfo = GroupFeedSessionManager.getInstance().ensureGroupSetUp(groupId);
            byte[] payload = encryptGroupMessage(message);

            if (groupSetupInfo.senderStateBundles != null && groupSetupInfo.senderStateBundles.size() > 0) {
                builder.addAllSenderStateBundles(groupSetupInfo.senderStateBundles);
            }
            if (groupSetupInfo.audienceHash != null) {
                builder.setAudienceHash(ByteString.copyFrom(groupSetupInfo.audienceHash));
            }
            if (payload != null) {
                EncryptedPayload encryptedPayload = EncryptedPayload.newBuilder()
                        .setSenderStateEncryptedPayload(ByteString.copyFrom(payload))
                        .build();
                builder.setEncPayload(encryptedPayload.toByteString());
            }
            builder.setMediaCounters(new MediaCounts(message.media).toProto());
        } catch (CryptoException e) {
            String errorMessage = e.getMessage();
            Log.e("Failed to encrypt group message", e);
            Log.sendErrorReport("Group message encrypt failed: " + errorMessage);
            Stats.getInstance().reportGroupEncryptError(errorMessage, false);
        } catch (NoSuchAlgorithmException e) {
            String errorMessage = "no_such_algo";
            Log.e("Failed to calculate audience hash", e);
            Log.sendErrorReport("Group message encrypt failed: " + errorMessage);
            Stats.getInstance().reportGroupEncryptError(errorMessage, false);
        }
        return builder;
    }

    public GroupChatStanza serializeGroupMessageRerequest(@NonNull Message message, @NonNull UserId userId) {
        GroupId groupId = (GroupId) message.chatId;
        SignalSessionSetupInfo signalSessionSetupInfo;
        try {
            signalSessionSetupInfo = SignalSessionManager.getInstance().getSessionSetupInfo(userId);
        } catch (Exception e) {
            Log.e("connection: sendRerequestedGroupComment failed to get setup info", e);
            return null;
        }
        SenderStateWithKeyInfo.Builder senderStateWithKeyInfoBuilder = SenderStateWithKeyInfo.newBuilder();
        try {
            SenderState senderState = GroupFeedSessionManager.getInstance().getSenderState(groupId);
            byte[] encSenderState = SignalSessionManager.getInstance().encryptMessage(senderState.toByteArray(), userId);
            senderStateWithKeyInfoBuilder.setEncSenderState(ByteString.copyFrom(encSenderState));
            if (signalSessionSetupInfo != null) {
                senderStateWithKeyInfoBuilder.setPublicKey(ByteString.copyFrom(signalSessionSetupInfo.identityKey.getKeyMaterial()));
                if (signalSessionSetupInfo.oneTimePreKeyId != null) {
                    senderStateWithKeyInfoBuilder.setOneTimePreKeyId(signalSessionSetupInfo.oneTimePreKeyId);
                }
            }
        } catch (CryptoException e) {
            Log.e("connection: sendRerequestedGroupComment failed to encrypt sender state", e);
        }
        GroupChatStanza.Builder builder = encryptGroupMessageAsChatStanza(message);
        builder.setSenderState(senderStateWithKeyInfoBuilder.build());

        return builder.build();
    }

    private void processGroupMessageSenderState(SenderStateWithKeyInfo senderStateWithKeyInfo, GroupId groupId, UserId publisherUserId) throws CryptoException, InvalidProtocolBufferException {
        byte[] encSenderState = senderStateWithKeyInfo.getEncSenderState().toByteArray();
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
        Log.i("Received sender state with current chain index of " + currentChainIndex + " from " + publisherUserId.rawId());

        EncryptedKeyStore.getInstance().edit()
                .setPeerGroupCurrentChainIndex(groupId, publisherUserId, currentChainIndex)
                .setPeerGroupChainKey(groupId, publisherUserId, chainKey)
                .setPeerGroupSigningKey(groupId, publisherUserId, publicSignatureKey)
                .apply();
    }

    @Nullable
    public Message parseGroupMessage(@NonNull GroupChatStanza groupChatStanza, String id, UserId fromUserId) {
        GroupId groupId = new GroupId(groupChatStanza.getGid());
        byte[] encPayload = groupChatStanza.getEncPayload().toByteArray();
        String errorMessage = null;
        boolean senderStateIssue = false;
        if (groupChatStanza.hasSenderState()) {
            try {
                processGroupMessageSenderState(groupChatStanza.getSenderState(), groupId, fromUserId);
            } catch (CryptoException e) {
                Log.e("Failed to decrypt sender state for " + ProtoPrinter.toString(groupChatStanza), e);
                senderStateIssue = true;
            } catch (InvalidProtocolBufferException e) {
                Log.e("Failed to parse sender state for " + ProtoPrinter.toString(groupChatStanza), e);
            }
        }
        int rerequestCount = contentDb.getMessageRerequestCount(groupId, fromUserId, id);
        ChatContainer chatContainer = null;
        String senderAgent = groupChatStanza.getSenderClientVersion();
        Log.i("Local state relevant to message " + id + " from:" + encryptedKeyStore.getLogInfo(fromUserId));
        String senderPlatform = senderAgent == null ? "" : senderAgent.contains("Android") ? "android" : senderAgent.contains("iOS") ? "ios" : "";
        String senderVersion = senderPlatform.equals("android") ? senderAgent.split("Android")[1] : senderPlatform.equals("ios") ? senderAgent.split("iOS")[1] : "";
        try {
            byte[] payload = decryptGroupMessage(encPayload, groupId, fromUserId);
            try {
                Container container = Container.parseFrom(payload);
                chatContainer = container.getChatContainer();
                stats.reportGroupDecryptSuccess(false, senderPlatform, senderVersion);
            } catch (InvalidProtocolBufferException e) {
                Log.e("Payload not a valid container", e);
            }
        } catch (CryptoException e) {
            Log.e("Failed to decrypt group message", e);
            errorMessage = (senderStateIssue ? "sender_state_" : "") + e.getMessage();
            Log.sendErrorReport("Group message decryption failed: " + errorMessage);
            stats.reportGroupDecryptError(errorMessage, false, senderPlatform, senderVersion);

            Log.i("Rerequesting message " + id);
            rerequestCount += 1;
            if (senderStateIssue) {
                Log.i("Tearing down session because of sender state issue");
                signalSessionManager.tearDownSession(fromUserId);
            }
            GroupFeedSessionManager.getInstance().sendMessageRerequest(fromUserId, groupId, id, rerequestCount, senderStateIssue);
        }

        long timestamp = groupChatStanza.getTimestamp() * 1000L;

        final Message message;
        if (chatContainer != null) {
            message = Message.parseFromProto(groupId, fromUserId, id, timestamp, chatContainer);
        } else {
            message = new Message(0,
                    groupId,
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
                    rerequestCount
            );
        }

        message.senderPlatform = senderPlatform;
        message.senderVersion = senderVersion;
        message.clientVersion = Constants.FULL_VERSION;
        message.failureReason = errorMessage;

        return message;
    }

}
