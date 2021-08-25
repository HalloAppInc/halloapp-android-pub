package com.halloapp.crypto.group;

import com.google.protobuf.ByteString;
import com.goterl.lazysodium.interfaces.Sign;
import com.halloapp.Me;
import com.halloapp.content.ContentDb;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.PrivateEdECKey;
import com.halloapp.crypto.signal.SessionSetupInfo;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.clients.SenderKey;
import com.halloapp.proto.server.SenderStateBundle;
import com.halloapp.proto.server.SenderStateWithKeyInfo;
import com.halloapp.util.logs.Log;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupFeedKeyManager {
    private static GroupFeedKeyManager instance;

    private static final byte[] HKDF_INPUT_MESSAGE_KEY = new byte[]{3};
    private static final byte[] HKDF_INPUT_CHAIN_KEY = new byte[]{4};

    private final Me me;
    private final EncryptedKeyStore encryptedKeyStore;
    private final SignalSessionManager signalSessionManager;

    public static GroupFeedKeyManager getInstance() {
        if (instance == null) {
            synchronized (GroupFeedKeyManager.class) {
                if (instance == null) {
                    instance = new GroupFeedKeyManager(Me.getInstance(), EncryptedKeyStore.getInstance(), SignalSessionManager.getInstance());
                }
            }
        }
        return instance;
    }

    private GroupFeedKeyManager(Me me, EncryptedKeyStore encryptedKeyStore, SignalSessionManager signalSessionManager) {
        this.me = me;
        this.encryptedKeyStore = encryptedKeyStore;
        this.signalSessionManager = signalSessionManager;
    }

    public static class GroupSetupResult {
        public final byte[] audienceHash;
        public final List<SenderStateBundle> senderStateBundles;

        public GroupSetupResult(byte[] audienceHash, List<SenderStateBundle> senderStateBundles) {
            this.audienceHash = audienceHash;
            this.senderStateBundles = senderStateBundles;
        }
    }

    public GroupSetupResult ensureGroupSetUp(GroupId groupId) throws CryptoException, NoSuchAlgorithmException {
        Map<UserId, SessionSetupInfo> setupInfoMap = new HashMap<>();
        List<MemberInfo> members = new ArrayList<>();
        for (MemberInfo memberInfo : ContentDb.getInstance().getGroupMembers(groupId)) {
            UserId userId = memberInfo.userId;
            if (userId.isMe()) {
                members.add(new MemberInfo(-1, new UserId(Me.getInstance().getUser()), memberInfo.type, memberInfo.name));
            } else {
                SessionSetupInfo sessionSetupInfo;
                try {
                    sessionSetupInfo = signalSessionManager.getSessionSetupInfo(userId);
                } catch (Exception e) {
                    throw new CryptoException("failed_get_session_setup_info", e);
                }
                setupInfoMap.put(userId, sessionSetupInfo);

                members.add(memberInfo);
            }
        }

        List<byte[]> identityKeyList = new ArrayList<>();
        for (MemberInfo memberInfo : members) {
            UserId userId = memberInfo.userId;

            byte[] ik;
            if (Me.getInstance().getUser().equals(userId.rawId())) {
                ik = EncryptedKeyStore.getInstance().getMyPublicEd25519IdentityKey().getKeyMaterial();
                identityKeyList.add(ik);
            } else {
                ik = EncryptedKeyStore.getInstance().getPeerPublicIdentityKey(userId).getKeyMaterial();
                identityKeyList.add(ik);
            }
        }

        List<SenderStateBundle> senderStateBundles = new ArrayList<>();
        if (!EncryptedKeyStore.getInstance().getGroupSendAlreadySetUp(groupId)) {
            Log.i("connection: Group send not yet set up for " + groupId + "; setting up now");
            SecureRandom r = new SecureRandom();
            byte[] chainKey = new byte[32];
            r.nextBytes(chainKey);

            byte[] signatureKey = CryptoUtils.generateEd25519KeyPair(); // BOTH PUBLIC AND SECRET
            byte[] publicSignatureKeyBytes = Arrays.copyOfRange(signatureKey, 0, Sign.ED25519_PUBLICKEYBYTES);
            byte[] privateSignatureKeyBytes = Arrays.copyOfRange(signatureKey, Sign.ED25519_PUBLICKEYBYTES, signatureKey.length);
            PrivateEdECKey privateSignatureKey = new PrivateEdECKey(privateSignatureKeyBytes);

            int currentChainIndex = 0;

            SenderKey senderKey = SenderKey.newBuilder()
                    .setChainKey(ByteString.copyFrom(chainKey))
                    .setPublicSignatureKey(ByteString.copyFrom(publicSignatureKeyBytes))
                    .build();

            for (MemberInfo memberInfo : members) {
                if (me.getUser().equals(memberInfo.userId.rawId())) {
                    continue;
                }
                UserId peerUserId = memberInfo.userId;
                byte[] senderKeyBytes = senderKey.toByteArray();
                byte[] encSenderKey = SignalSessionManager.getInstance().encryptMessage(senderKeyBytes, peerUserId);
                SenderStateWithKeyInfo.Builder info = SenderStateWithKeyInfo.newBuilder()
                        .setEncSenderState(ByteString.copyFrom(encSenderKey));
                SessionSetupInfo sessionSetupInfo = setupInfoMap.get(peerUserId);
                if (sessionSetupInfo != null) {
                    info.setPublicKey(ByteString.copyFrom(sessionSetupInfo.identityKey.getKeyMaterial()));
                    if (sessionSetupInfo.oneTimePreKeyId != null) {
                        info.setOneTimePreKeyId(sessionSetupInfo.oneTimePreKeyId);
                    }
                }
                SenderStateBundle senderStateBundle = SenderStateBundle.newBuilder()
                        .setSenderState(info)
                        .setUid(Long.parseLong(peerUserId.rawId()))
                        .build();
                senderStateBundles.add(senderStateBundle);
            }

            encryptedKeyStore.setMyGroupCurrentChainIndex(groupId, currentChainIndex);
            encryptedKeyStore.setMyGroupChainKey(groupId, chainKey);
            encryptedKeyStore.setMyGroupSigningKey(groupId, privateSignatureKey);
            encryptedKeyStore.setGroupSendAlreadySetUp(groupId);
        }

        // Calculate audience hash
        byte[] xor = new byte[32];
        for (byte[] arr : identityKeyList) {
            for (int i = 0; i < arr.length; i++) {
                xor[i] = (byte) (xor[i] ^ arr[i]);
            }
        }

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fullHash = digest.digest(xor);
        byte[] audienceHash = Arrays.copyOfRange(fullHash, 0, 6);

        return new GroupSetupResult(audienceHash, senderStateBundles);
    }

    public byte[] getNextInboundMessageKey(GroupId groupId, UserId peerUserId, int currentChainIndex) throws CryptoException {
        try {
            // TODO(jack): out of order messages
            byte[] chainKey = encryptedKeyStore.getPeerGroupChainKey(groupId, peerUserId);
            byte[] messageKey = CryptoUtils.hkdf(chainKey, null, HKDF_INPUT_MESSAGE_KEY, 80);
            byte[] updatedChainKey = CryptoUtils.hkdf(chainKey, null, HKDF_INPUT_CHAIN_KEY, 32);
            encryptedKeyStore.setPeerGroupChainKey(groupId, peerUserId, updatedChainKey);
            return messageKey;
        } catch (GeneralSecurityException e) {
            throw new CryptoException("group_inbound_key_fail", e);
        }
    }

    public byte[] getNextOutboundMessageKey(GroupId groupId) throws CryptoException {
        byte[] chainKey = encryptedKeyStore.getMyGroupChainKey(groupId);
        try {
            byte[] messageKey = CryptoUtils.hkdf(chainKey, null, HKDF_INPUT_MESSAGE_KEY, 80);
            byte[] updatedChainKey = CryptoUtils.hkdf(chainKey, null, HKDF_INPUT_CHAIN_KEY, 32);
            encryptedKeyStore.setMyGroupChainKey(groupId, updatedChainKey);
            return messageKey;
        } catch (GeneralSecurityException e) {
            throw new CryptoException("group_outbound_key_fail", e);
        }
    }

    public void tearDownOutboundSession(GroupId groupId) {
        Log.i("GroupFeedKeyManager tearing down outbound session for " + groupId);
        encryptedKeyStore.clearMyGroupChainKey(groupId);
        encryptedKeyStore.clearMyGroupCurrentChainIndex(groupId);
        encryptedKeyStore.clearMyGroupSigningKey(groupId); // TODO(jack): Are we sure this should be reset?
    }

    public void tearDownInboundSession(GroupId groupId, UserId peerUserId) {
        Log.i("GroupFeedKeyManager tearing down inbound session with " + peerUserId + " for " + groupId);
        encryptedKeyStore.clearPeerGroupChainKey(groupId, peerUserId);
        encryptedKeyStore.clearPeerGroupCurrentChainIndex(groupId, peerUserId);
        encryptedKeyStore.clearPeerGroupSigningKey(groupId, peerUserId);
    }
}
