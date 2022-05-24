package com.halloapp.crypto.group;

import androidx.annotation.VisibleForTesting;

import com.google.protobuf.ByteString;
import com.goterl.lazysodium.interfaces.Sign;
import com.halloapp.Me;
import com.halloapp.content.ContentDb;
import com.halloapp.crypto.CryptoByteUtils;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.PrivateEdECKey;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.crypto.signal.SignalSessionSetupInfo;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.clients.SenderKey;
import com.halloapp.proto.clients.SenderState;
import com.halloapp.proto.server.SenderStateBundle;
import com.halloapp.proto.server.SenderStateWithKeyInfo;
import com.halloapp.util.StringUtils;
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

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
public class GroupFeedKeyManager {
    private static GroupFeedKeyManager instance;

    private static final byte[] HKDF_INPUT_MESSAGE_KEY = new byte[]{3};
    private static final byte[] HKDF_INPUT_CHAIN_KEY = new byte[]{4};

    private static final int MAX_GROUP_FEED_KEYS_SKIP = 100;

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

    GroupSetupInfo ensureGroupSetUp(GroupId groupId) throws CryptoException, NoSuchAlgorithmException {
        Map<UserId, SignalSessionSetupInfo> setupInfoMap = new HashMap<>();
        List<MemberInfo> members = new ArrayList<>();
        for (MemberInfo memberInfo : ContentDb.getInstance().getGroupMembers(groupId)) {
            UserId userId = memberInfo.userId;
            if (userId.isMe()) {
                members.add(new MemberInfo(-1, new UserId(Me.getInstance().getUser()), memberInfo.type, memberInfo.name));
            } else {
                SignalSessionSetupInfo signalSessionSetupInfo;
                try {
                    signalSessionSetupInfo = signalSessionManager.getSessionSetupInfo(userId);
                } catch (Exception e) {
                    throw new CryptoException("failed_get_session_setup_info", e);
                }
                setupInfoMap.put(userId, signalSessionSetupInfo);

                members.add(memberInfo);
            }
        }

        List<byte[]> identityKeyList = new ArrayList<>();
        for (MemberInfo memberInfo : members) {
            UserId userId = memberInfo.userId;

            byte[] ik;
            if (Me.getInstance().getUser().equals(userId.rawId())) {
                ik = encryptedKeyStore.getMyPublicEd25519IdentityKey().getKeyMaterial();
                identityKeyList.add(ik);
            } else {
                ik = encryptedKeyStore.getPeerPublicIdentityKey(userId).getKeyMaterial();
                identityKeyList.add(ik);
            }
        }

        List<SenderStateBundle> senderStateBundles = new ArrayList<>();
        if (!encryptedKeyStore.getGroupSendAlreadySetUp(groupId)) {
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

            SenderState senderState = SenderState.newBuilder()
                    .setSenderKey(senderKey)
                    .setCurrentChainIndex(currentChainIndex)
                    .build();

            for (MemberInfo memberInfo : members) {
                if (me.getUser().equals(memberInfo.userId.rawId())) {
                    continue;
                }
                UserId peerUserId = memberInfo.userId;
                byte[] senderStateBytes = senderState.toByteArray();
                byte[] encSenderKey = SignalSessionManager.getInstance().encryptMessage(senderStateBytes, peerUserId);
                SenderStateWithKeyInfo.Builder info = SenderStateWithKeyInfo.newBuilder()
                        .setEncSenderState(ByteString.copyFrom(encSenderKey));
                SignalSessionSetupInfo signalSessionSetupInfo = setupInfoMap.get(peerUserId);
                if (signalSessionSetupInfo != null) {
                    info.setPublicKey(ByteString.copyFrom(signalSessionSetupInfo.identityKey.getKeyMaterial()));
                    if (signalSessionSetupInfo.oneTimePreKeyId != null) {
                        info.setOneTimePreKeyId(signalSessionSetupInfo.oneTimePreKeyId);
                    }
                }
                SenderStateBundle senderStateBundle = SenderStateBundle.newBuilder()
                        .setSenderState(info)
                        .setUid(Long.parseLong(peerUserId.rawId()))
                        .build();
                senderStateBundles.add(senderStateBundle);
            }

            encryptedKeyStore.edit()
                    .setMyGroupCurrentChainIndex(groupId, currentChainIndex)
                    .setMyGroupChainKey(groupId, chainKey)
                    .setMyGroupSigningKey(groupId, privateSignatureKey)
                    .setGroupSendAlreadySetUp(groupId)
                    .apply();
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

        return new GroupSetupInfo(audienceHash, senderStateBundles);
    }

    byte[] getNextInboundMessageKey(GroupId groupId, UserId peerUserId, int currentChainIndex) throws CryptoException {
        int storedCurrentChainIndex = encryptedKeyStore.getPeerGroupCurrentChainIndex(groupId, peerUserId);
        Log.i("GroupFeedKeyManager.getNextInboundMessageKey for " + groupId + " member " + peerUserId + " receivedIndex " + currentChainIndex + " storedIndex " + storedCurrentChainIndex);

        if (currentChainIndex < storedCurrentChainIndex) {
            Log.i("GroupFeedKeyManager retrieving stored group feed key");
            byte[] messageKey = encryptedKeyStore.removeSkippedGroupFeedKey(groupId, peerUserId, currentChainIndex);
            if (messageKey == null) {
                throw new CryptoException("old_grp_key_not_found");
            }
            return messageKey;
        }

        skipInboundKeys(groupId, peerUserId, currentChainIndex - storedCurrentChainIndex - 1, storedCurrentChainIndex);
        encryptedKeyStore.edit().setPeerGroupCurrentChainIndex(groupId, peerUserId, currentChainIndex).apply();

        return getInboundMessageKey(groupId, peerUserId);
    }

    @VisibleForTesting
    public byte[] getInboundMessageKey(GroupId groupId, UserId peerUserId) throws CryptoException {
        Log.i("GroupFeedKeyManager.getInboundMessageKey for " + groupId + " member " + peerUserId);
        try {
            byte[] chainKey = encryptedKeyStore.getPeerGroupChainKey(groupId, peerUserId);
            byte[] messageKey = CryptoUtils.hkdf(chainKey, null, HKDF_INPUT_MESSAGE_KEY, 80);
            byte[] updatedChainKey = CryptoUtils.hkdf(chainKey, null, HKDF_INPUT_CHAIN_KEY, 32);
            encryptedKeyStore.edit().setPeerGroupChainKey(groupId, peerUserId, updatedChainKey).apply();
            Log.i("GroupFeedKeyManager.getInboundMessageKey chain key " + CryptoByteUtils.obfuscate(chainKey) + " -> " + CryptoByteUtils.obfuscate(updatedChainKey));
            CryptoByteUtils.nullify(chainKey, updatedChainKey);
            return messageKey;
        } catch (NullPointerException e) {
            throw new CryptoException("group_inbound_key_null", e);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("group_inbound_key_fail", e);
        }
    }

    public byte[] getNextOutboundMessageKey(GroupId groupId) throws CryptoException {
        Log.i("GroupFeedKeyManager.getNextOutboundMessageKey for " + groupId);
        byte[] chainKey = encryptedKeyStore.getMyGroupChainKey(groupId);
        try {
            int currentChainIndex = encryptedKeyStore.getMyGroupCurrentChainIndex(groupId);
            int nextChainIndex = currentChainIndex + 1;
            byte[] messageKey = CryptoUtils.hkdf(chainKey, null, HKDF_INPUT_MESSAGE_KEY, 80);
            byte[] updatedChainKey = CryptoUtils.hkdf(chainKey, null, HKDF_INPUT_CHAIN_KEY, 32);
            encryptedKeyStore.edit()
                    .setMyGroupChainKey(groupId, updatedChainKey)
                    .setMyGroupCurrentChainIndex(groupId, nextChainIndex)
                    .apply();
            Log.i("GroupFeedKeyManager.getOutboundMessageKey chain key " + CryptoByteUtils.obfuscate(chainKey) + " -> " + CryptoByteUtils.obfuscate(updatedChainKey));
            CryptoByteUtils.nullify(chainKey, updatedChainKey);
            return messageKey;
        } catch (NullPointerException e) {
            throw new CryptoException("group_outbound_key_null", e);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("group_outbound_key_fail", e);
        }
    }

    SenderState getSenderState(GroupId groupId) throws CryptoException {
        byte[] chainKey = encryptedKeyStore.getMyGroupChainKey(groupId);
        PublicEdECKey publicSignatureKey = encryptedKeyStore.getMyPublicGroupSigningKey(groupId);
        int currentChainIndex = encryptedKeyStore.getMyGroupCurrentChainIndex(groupId);

        SenderKey senderKey = SenderKey.newBuilder()
                .setChainKey(ByteString.copyFrom(chainKey))
                .setPublicSignatureKey(ByteString.copyFrom(publicSignatureKey.getKeyMaterial()))
                .build();

        return SenderState.newBuilder()
                .setSenderKey(senderKey)
                .setCurrentChainIndex(currentChainIndex)
                .build();
    }

    private void skipInboundKeys(GroupId groupId, UserId peerUserId, int count, int startIndex) throws CryptoException {
        Log.i("GroupFeedKeyManager: skipping " + count + " inbound keys");
        if (count > MAX_GROUP_FEED_KEYS_SKIP) {
            Log.e("Attempting to skip too many keys");
            throw new CryptoException("skip_too_many_grp_keys");
        }
        for (int i=0; i<count; i++) {
            byte[] inboundMessageKey = getInboundMessageKey(groupId, peerUserId);
            try {
                GroupFeedMessageKey messageKey = new GroupFeedMessageKey(startIndex + i, inboundMessageKey);
                encryptedKeyStore.edit().storeSkippedGroupFeedKey(groupId, peerUserId, messageKey).apply();
            } catch (CryptoException e) {
                Log.e("Cannot store invalid incoming group feed key for later use", e);
                throw new CryptoException("skip_grp_key_" + e.getMessage());
            }
        }
    }

    void tearDownOutboundSession(GroupId groupId) {
        Log.i("GroupFeedKeyManager tearing down outbound session for " + groupId);
        encryptedKeyStore.edit()
                .clearMyGroupChainKey(groupId)
                .clearMyGroupCurrentChainIndex(groupId)
                .clearMyGroupSigningKey(groupId)
                .clearGroupSendAlreadySetUp(groupId)
                .apply();
    }

    void tearDownInboundSession(GroupId groupId, UserId peerUserId) {
        Log.i("GroupFeedKeyManager tearing down inbound session with " + peerUserId + " for " + groupId);
        encryptedKeyStore.edit()
                .clearPeerGroupChainKey(groupId, peerUserId)
                .clearPeerGroupCurrentChainIndex(groupId, peerUserId)
                .clearPeerGroupSigningKey(groupId, peerUserId)
                .apply();
    }
}
