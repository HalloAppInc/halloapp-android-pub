package com.halloapp.crypto.signal;

import com.halloapp.crypto.CryptoByteUtils;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.PublicXECKey;
import com.halloapp.crypto.keys.XECKey;
import com.halloapp.id.UserId;
import com.halloapp.util.logs.Log;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class SignalMessageCipher {

    private static SignalMessageCipher instance;

    private static final int COUNTER_SIZE_BYTES = 4;
    private static final int MINIMUM_MESSAGE_SIZE_BYTES = 76;

    private final SignalKeyManager signalKeyManager;
    private final EncryptedKeyStore encryptedKeyStore;

    public static SignalMessageCipher getInstance() {
        if (instance == null) {
            synchronized (SignalMessageCipher.class) {
                if (instance == null) {
                    instance = new SignalMessageCipher(SignalKeyManager.getInstance(), EncryptedKeyStore.getInstance());
                }
            }
        }
        return instance;
    }

    private SignalMessageCipher(SignalKeyManager signalKeyManager, EncryptedKeyStore encryptedKeyStore) {
        this.signalKeyManager = signalKeyManager;
        this.encryptedKeyStore = encryptedKeyStore;
    }

    byte[] convertFromWire(byte[] message, UserId peerUserId) throws CryptoException {
        if (message.length < MINIMUM_MESSAGE_SIZE_BYTES) {
            Log.e("Input message bytes too short");
            throw new CryptoException("ciphertext_too_short");
        }
        byte[] ephemeralKeyBytes = Arrays.copyOfRange(message, 0, 32);
        byte[] ephemeralKeyIdBytes = Arrays.copyOfRange(message, 32, 36);
        byte[] previousChainLengthBytes = Arrays.copyOfRange(message, 36, 40);
        byte[] currentChainIndexBytes = Arrays.copyOfRange(message, 40, 44);
        byte[] encryptedMessage = Arrays.copyOfRange(message, 44, message.length - 32);
        byte[] receivedHmac = Arrays.copyOfRange(message, message.length - 32, message.length);

        PublicXECKey ephemeralKey = new PublicXECKey(ephemeralKeyBytes);
        int ephemeralKeyId = ByteBuffer.wrap(ephemeralKeyIdBytes).getInt();
        int previousChainLength = ByteBuffer.wrap(previousChainLengthBytes).getInt();
        int currentChainIndex = ByteBuffer.wrap(currentChainIndexBytes).getInt();

        byte[] inboundMessageKey = signalKeyManager.getInboundMessageKey(peerUserId, ephemeralKey, ephemeralKeyId, previousChainLength, currentChainIndex);

        byte[] aesKey = Arrays.copyOfRange(inboundMessageKey, 0, 32);
        byte[] hmacKey = Arrays.copyOfRange(inboundMessageKey, 32, 64);
        byte[] iv = Arrays.copyOfRange(inboundMessageKey, 64, 80);

        byte[] calculatedHmac = CryptoUtils.hmac(hmacKey, encryptedMessage);
        if (!Arrays.equals(calculatedHmac, receivedHmac)) {
            Log.e("HMAC does not match; rejecting and storing message key");
            SignalMessageKey signalMessageKey = new SignalMessageKey(ephemeralKeyId, previousChainLength, currentChainIndex, inboundMessageKey);
            onDecryptFailure("hmac_mismatch", peerUserId, signalMessageKey);
        }

        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding"); // NOTE: for AES same as PKCS5
            SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            c.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
            byte[] ret = c.doFinal(encryptedMessage);

            CryptoByteUtils.nullify(inboundMessageKey, aesKey, hmacKey, iv);

            return ret;
        } catch (GeneralSecurityException e) {
            Log.w("Decryption failed, storing message key", e);
            SignalMessageKey signalMessageKey = new SignalMessageKey(ephemeralKeyId, previousChainLength, currentChainIndex, inboundMessageKey);
            onDecryptFailure("cipher_dec_failure", peerUserId, signalMessageKey);
        }

        throw new IllegalStateException("Unreachable");
    }

    private void onDecryptFailure(String reason, UserId peerUserId, SignalMessageKey signalMessageKey) throws CryptoException {
        encryptedKeyStore.storeSkippedMessageKey(peerUserId, signalMessageKey);
        byte[] lastTeardownKey = encryptedKeyStore.getOutboundTeardownKey(peerUserId);
        byte[] newTeardownKey = signalMessageKey.getKeyMaterial();
        boolean match = Arrays.equals(lastTeardownKey, newTeardownKey);
        if (!match) {
            encryptedKeyStore.setOutboundTeardownKey(peerUserId, newTeardownKey);
            signalKeyManager.tearDownSession(peerUserId);
        }
        throw new CryptoException(reason, match, newTeardownKey);
    }

    byte[] convertForWire(byte[] message, UserId peerUserId) throws CryptoException {
        SignalMessageKey signalMessageKey = signalKeyManager.getNextOutboundMessageKey(peerUserId);
        byte[] outboundMessageKey = signalMessageKey.getKeyMaterial();

        byte[] aesKey = Arrays.copyOfRange(outboundMessageKey, 0, 32);
        byte[] hmacKey = Arrays.copyOfRange(outboundMessageKey, 32, 64);
        byte[] iv = Arrays.copyOfRange(outboundMessageKey, 64, 80);

        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding"); // NOTE: for AES same as PKCS5
            SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            c.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
            byte[] encryptedContents = c.doFinal(message);

            byte[] hmac = CryptoUtils.hmac(hmacKey, encryptedContents);

            CryptoByteUtils.nullify(outboundMessageKey, aesKey, hmacKey, iv);

            byte[] ephemeralKeyBytes = XECKey.publicFromPrivate(encryptedKeyStore.getOutboundEphemeralKey(peerUserId)).getKeyMaterial();
            byte[] ephemeralKeyIdBytes = ByteBuffer.allocate(COUNTER_SIZE_BYTES).putInt(encryptedKeyStore.getOutboundEphemeralKeyId(peerUserId)).array();
            byte[] previousChainLengthBytes = ByteBuffer.allocate(COUNTER_SIZE_BYTES).putInt(signalMessageKey.getPreviousChainLength()).array();
            byte[] currentChainIndexBytes = ByteBuffer.allocate(COUNTER_SIZE_BYTES).putInt(signalMessageKey.getCurrentChainIndex()).array();

            return CryptoByteUtils.concat(ephemeralKeyBytes, ephemeralKeyIdBytes, previousChainLengthBytes, currentChainIndexBytes, encryptedContents, hmac);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("cipher_enc_failure", e);
        }
    }
}
