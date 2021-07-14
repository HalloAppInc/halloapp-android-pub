package com.halloapp.crypto.group;

import com.google.crypto.tink.subtle.Hex;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.util.logs.Log;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public class GroupFeedKeyManager {
    private static GroupFeedKeyManager instance;

    private static final byte[] HKDF_INPUT_MESSAGE_KEY = new byte[]{3};
    private static final byte[] HKDF_INPUT_CHAIN_KEY = new byte[]{4};

    private final EncryptedKeyStore encryptedKeyStore;

    public static GroupFeedKeyManager getInstance() {
        if (instance == null) {
            synchronized (GroupFeedKeyManager.class) {
                if (instance == null) {
                    instance = new GroupFeedKeyManager(EncryptedKeyStore.getInstance());
                }
            }
        }
        return instance;
    }

    private GroupFeedKeyManager(EncryptedKeyStore encryptedKeyStore) {
        this.encryptedKeyStore = encryptedKeyStore;
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
