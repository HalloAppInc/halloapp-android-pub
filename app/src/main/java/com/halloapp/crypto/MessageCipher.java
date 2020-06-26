package com.halloapp.crypto;

import com.halloapp.contacts.UserId;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.KeyManager;
import com.halloapp.crypto.keys.MessageKey;
import com.halloapp.crypto.keys.PublicXECKey;
import com.halloapp.crypto.keys.XECKey;
import com.halloapp.util.Log;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class MessageCipher {
    private static final int COUNTER_SIZE_BYTES = 4;

    private final KeyManager keyManager;
    private final EncryptedKeyStore encryptedKeyStore;

    MessageCipher() {
        this.keyManager = KeyManager.getInstance();
        this.encryptedKeyStore = EncryptedKeyStore.getInstance();
    }

    byte[] convertFromWire(byte[] message, UserId peerUserId) throws GeneralSecurityException {
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

        byte[] inboundMessageKey = keyManager.getInboundMessageKey(peerUserId, ephemeralKey, ephemeralKeyId, previousChainLength, currentChainIndex);

        byte[] aesKey = Arrays.copyOfRange(inboundMessageKey, 0, 32);
        byte[] hmacKey = Arrays.copyOfRange(inboundMessageKey, 32, 64);
        byte[] iv = Arrays.copyOfRange(inboundMessageKey, 64, 80);

        byte[] calculatedHmac = CryptoUtils.hmac(hmacKey, encryptedMessage);
        if (!Arrays.equals(calculatedHmac, receivedHmac)) {
            Log.e("HMAC does not match; rejecting");
            throw new GeneralSecurityException("HMAC mismatch");
        }

        Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding"); // NOTE: for AES same as PKCS5
        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        c.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
        byte[] ret =  c.doFinal(encryptedMessage);

        CryptoUtils.nullify(inboundMessageKey, aesKey, hmacKey, iv);

        return ret;
    }

    byte[] convertForWire(byte[] message, UserId peerUserId) throws GeneralSecurityException {
        MessageKey messageKey = keyManager.getNextOutboundMessageKey(peerUserId);
        byte[] outboundMessageKey = messageKey.getKeyMaterial();

        byte[] aesKey = Arrays.copyOfRange(outboundMessageKey, 0, 32);
        byte[] hmacKey = Arrays.copyOfRange(outboundMessageKey, 32, 64);
        byte[] iv = Arrays.copyOfRange(outboundMessageKey, 64, 80);

        Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding"); // NOTE: for AES same as PKCS5
        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        c.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
        byte[] encryptedContents = c.doFinal(message);

        byte[] hmac = CryptoUtils.hmac(hmacKey, encryptedContents);

        CryptoUtils.nullify(outboundMessageKey, aesKey, hmacKey, iv);

        byte[] ephemeralKeyBytes = XECKey.publicFromPrivate(encryptedKeyStore.getOutboundEphemeralKey(peerUserId)).getKeyMaterial();
        byte[] ephemeralKeyIdBytes = ByteBuffer.allocate(COUNTER_SIZE_BYTES).putInt(encryptedKeyStore.getOutboundEphemeralKeyId(peerUserId)).array();
        byte[] previousChainLengthBytes = ByteBuffer.allocate(COUNTER_SIZE_BYTES).putInt(messageKey.getPreviousChainLength()).array();
        byte[] currentChainIndexBytes = ByteBuffer.allocate(COUNTER_SIZE_BYTES).putInt(messageKey.getCurrentChainIndex()).array();

        return CryptoUtils.concat(ephemeralKeyBytes, ephemeralKeyIdBytes, previousChainLengthBytes, currentChainIndexBytes, encryptedContents, hmac);
    }
}
