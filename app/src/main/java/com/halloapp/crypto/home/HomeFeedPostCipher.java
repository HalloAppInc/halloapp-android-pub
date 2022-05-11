package com.halloapp.crypto.home;

import com.goterl.lazysodium.interfaces.Sign;
import com.halloapp.crypto.CryptoByteUtils;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.PrivateEdECKey;
import com.halloapp.id.UserId;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class HomeFeedPostCipher {

    private static HomeFeedPostCipher instance;

    private static final int COUNTER_SIZE_BYTES = 4;

    private final EncryptedKeyStore encryptedKeyStore;
    private final HomeFeedPostKeyManager homeFeedPostKeyManager;

    public static HomeFeedPostCipher getInstance() {
        if (instance == null) {
            synchronized (HomeFeedPostCipher.class) {
                if (instance == null) {
                    instance = new HomeFeedPostCipher(EncryptedKeyStore.getInstance(), HomeFeedPostKeyManager.getInstance());
                }
            }
        }
        return instance;
    }

    HomeFeedPostCipher(EncryptedKeyStore encryptedKeyStore, HomeFeedPostKeyManager homeFeedPostKeyManager) {
        this.encryptedKeyStore = encryptedKeyStore;
        this.homeFeedPostKeyManager = homeFeedPostKeyManager;
    }

    byte[] convertForWire(byte[] payload, boolean favorites) throws CryptoException {
        int usedChainIndex = encryptedKeyStore.getMyHomeCurrentChainIndex(favorites);
        byte[] messageKey = homeFeedPostKeyManager.getNextOutboundMessageKey(favorites);
        PrivateEdECKey privateSignatureKey = encryptedKeyStore.getMyPrivateHomeSigningKey(favorites);

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

            byte[] currentChainIndexBytes = ByteBuffer.allocate(COUNTER_SIZE_BYTES).putInt(usedChainIndex).array();

            return CryptoByteUtils.concat(currentChainIndexBytes, encryptedContents, hmac);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("cipher_enc_failure", e);
        }
    }

    byte[] convertFromWire(byte[] encPayload, boolean favorites, UserId peerUserId) throws CryptoException {
        byte[] currentChainIndexBytes = Arrays.copyOfRange(encPayload, 0, 4);
        byte[] encryptedMessage = Arrays.copyOfRange(encPayload, 4, encPayload.length - 32);
        byte[] receivedHmac = Arrays.copyOfRange(encPayload, encPayload.length - 32, encPayload.length);

        int currentChainIndex = ByteBuffer.wrap(currentChainIndexBytes).getInt();

        byte[] inboundMessageKey = homeFeedPostKeyManager.getNextInboundMessageKey(favorites, peerUserId, currentChainIndex);

        byte[] aesKey = Arrays.copyOfRange(inboundMessageKey, 0, 32);
        byte[] hmacKey = Arrays.copyOfRange(inboundMessageKey, 32, 64);
        byte[] iv = Arrays.copyOfRange(inboundMessageKey, 64, 80);

        byte[] calculatedHmac = CryptoUtils.hmac(hmacKey, encryptedMessage);
        if (!Arrays.equals(calculatedHmac, receivedHmac)) {
            Log.e("Expected HMAC " + StringUtils.bytesToHexString(receivedHmac) + " but calculated " + StringUtils.bytesToHexString(calculatedHmac));
            HomeFeedPostMessageKey messageKey = new HomeFeedPostMessageKey(currentChainIndex, inboundMessageKey);
            onDecryptFailure("home_hmac_mismatch", favorites, peerUserId, messageKey);
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
                CryptoUtils.verify(signature, decPayload, EncryptedKeyStore.getInstance().getPeerHomeSigningKey(favorites, peerUserId));
            } catch (GeneralSecurityException e) {
                Log.e("Home Feed Encryption signature verification failed", e);
                HomeFeedPostMessageKey messageKey = new HomeFeedPostMessageKey(currentChainIndex, inboundMessageKey);
                onDecryptFailure("home_dec_invalid_signature", favorites, peerUserId, messageKey);
            }

            return decPayload;
        } catch (GeneralSecurityException e) {
            Log.w("Decryption failed, storing message key", e);
            HomeFeedPostMessageKey messageKey = new HomeFeedPostMessageKey(currentChainIndex, inboundMessageKey);
            onDecryptFailure("home_cipher_dec_failure", favorites, peerUserId, messageKey);
        }

        throw new IllegalStateException("Unreachable");
    }

    private void onDecryptFailure(String reason, boolean favorites, UserId peerUserId, HomeFeedPostMessageKey messageKey) throws CryptoException {
        encryptedKeyStore.edit().storeSkippedHomeFeedKey(favorites, peerUserId, messageKey).apply();
        throw new CryptoException(reason);
    }
}
