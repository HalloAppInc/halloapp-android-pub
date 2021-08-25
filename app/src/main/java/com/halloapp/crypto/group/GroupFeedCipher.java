package com.halloapp.crypto.group;

import com.google.crypto.tink.subtle.Hex;
import com.goterl.lazysodium.interfaces.Sign;
import com.halloapp.crypto.CryptoByteUtils;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.MessageKey;
import com.halloapp.crypto.keys.PrivateEdECKey;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.util.logs.Log;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class GroupFeedCipher {
    private static final int COUNTER_SIZE_BYTES = 4;

    private final EncryptedKeyStore encryptedKeyStore;
    private final GroupFeedKeyManager groupFeedKeyManager;

    GroupFeedCipher(EncryptedKeyStore encryptedKeyStore, GroupFeedKeyManager groupFeedKeyManager) {
        this.encryptedKeyStore = encryptedKeyStore;
        this.groupFeedKeyManager = groupFeedKeyManager;
    }

    public byte[] convertForWire(byte[] payload, GroupId groupId) throws CryptoException {
        byte[] messageKey = GroupFeedKeyManager.getInstance().getNextOutboundMessageKey(groupId);
        PrivateEdECKey privateSignatureKey = encryptedKeyStore.getMyPrivateGroupSigningKey(groupId);
        int nextChainIndex = encryptedKeyStore.getMyGroupCurrentChainIndex(groupId);

        byte[] signature = CryptoUtils.verifyDetached(payload, privateSignatureKey);
        byte[] signedPayload = CryptoByteUtils.concat(payload, signature);

        byte[] aesKey = Arrays.copyOfRange(messageKey, 0, 32);
        byte[] hmacKey = Arrays.copyOfRange(messageKey, 32, 64);
        byte[] iv = Arrays.copyOfRange(messageKey, 64, 80);

        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding"); // NOTE: for AES same as PKCS5
            SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            c.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
            byte[] encryptedContents = c.doFinal(signedPayload);

            byte[] hmac = CryptoUtils.hmac(hmacKey, encryptedContents);

            CryptoByteUtils.nullify(messageKey, aesKey, hmacKey, iv);

            byte[] currentChainIndexBytes = ByteBuffer.allocate(COUNTER_SIZE_BYTES).putInt(nextChainIndex).array();

            return CryptoByteUtils.concat(currentChainIndexBytes, encryptedContents, hmac);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("cipher_enc_failure", e);
        }
    }

    public byte[] convertFromWire(byte[] encPayload, GroupId groupId, UserId peerUserId) throws CryptoException {
        byte[] currentChainIndexBytes = Arrays.copyOfRange(encPayload, 0, 4);
        byte[] encryptedMessage = Arrays.copyOfRange(encPayload, 4, encPayload.length - 32);
        byte[] receivedHmac = Arrays.copyOfRange(encPayload, encPayload.length - 32, encPayload.length);

        int currentChainIndex = ByteBuffer.wrap(currentChainIndexBytes).getInt();

        byte[] inboundMessageKey = groupFeedKeyManager.getNextInboundMessageKey(groupId, peerUserId, currentChainIndex);

        byte[] aesKey = Arrays.copyOfRange(inboundMessageKey, 0, 32);
        byte[] hmacKey = Arrays.copyOfRange(inboundMessageKey, 32, 64);
        byte[] iv = Arrays.copyOfRange(inboundMessageKey, 64, 80);

        byte[] calculatedHmac = CryptoUtils.hmac(hmacKey, encryptedMessage);
        if (!Arrays.equals(calculatedHmac, receivedHmac)) {
            Log.e("Expected HMAC " + Hex.encode(receivedHmac) + " but calculated " + Hex.encode(calculatedHmac));
            GroupFeedMessageKey messageKey = new GroupFeedMessageKey(currentChainIndex, inboundMessageKey);
            onDecryptFailure("group_hmac_mismatch", groupId, peerUserId, messageKey);
        }

        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding"); // NOTE: for AES same as PKCS5
            SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            c.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
            byte[] signedPayload = c.doFinal(encryptedMessage);

            CryptoByteUtils.nullify(inboundMessageKey, aesKey, hmacKey, iv);

            byte[] decPayload = Arrays.copyOfRange(signedPayload, 0, signedPayload.length - Sign.ED25519_BYTES);
            byte[] signature = Arrays.copyOfRange(signedPayload, signedPayload.length - Sign.ED25519_BYTES, signedPayload.length);
            try {
                CryptoUtils.verify(signature, decPayload, EncryptedKeyStore.getInstance().getPeerGroupSigningKey(groupId, peerUserId));
            } catch (GeneralSecurityException e) {
                Log.e("Group Feed Encryption signature verification failed", e);
                throw new CryptoException("group_dec_invalid_signature", e);
            }


            return decPayload;
        } catch (GeneralSecurityException e) {
            Log.w("Decryption failed, storing message key", e);
            GroupFeedMessageKey messageKey = new GroupFeedMessageKey(currentChainIndex, inboundMessageKey);
            onDecryptFailure("grp_cipher_dec_failure", groupId, peerUserId, messageKey);
        }

        throw new IllegalStateException("Unreachable");
    }

    void onDecryptFailure(String reason, GroupId groupId, UserId peerUserId, GroupFeedMessageKey messageKey) throws CryptoException {
        encryptedKeyStore.storeSkippedGroupFeedKey(groupId, peerUserId, messageKey);
//        byte[] lastTeardownKey = encryptedKeyStore.getOutboundTeardownKey(peerUserId);
//        byte[] newTeardownKey = messageKey.getKeyMaterial();
//        boolean match = Arrays.equals(lastTeardownKey, newTeardownKey);
//        if (!match) {
//            encryptedKeyStore.setOutboundTeardownKey(peerUserId, newTeardownKey);
//            signalKeyManager.tearDownSession(peerUserId);
//        }
//        throw new CryptoException(reason, match, newTeardownKey);
        throw new CryptoException(reason);
    }
}
