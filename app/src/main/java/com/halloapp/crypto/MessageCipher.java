package com.halloapp.crypto;

import com.halloapp.contacts.UserId;
import com.halloapp.crypto.keys.MessageKey;
import com.halloapp.crypto.keys.PrivateXECKey;
import com.halloapp.crypto.keys.PublicXECKey;
import com.halloapp.crypto.keys.XECKey;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.KeyManager;
import com.halloapp.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class MessageCipher {
    private static final int COUNTER_SIZE_BYTES = 4;

    private final KeyManager keyManager;

    MessageCipher() {
        this.keyManager = KeyManager.getInstance();
    }

    byte[] convertFromWire(byte[] message, UserId peerUserId, PublicXECKey ephemeralKey, Integer ephemeralKeyId) throws Exception {
        byte[] previousChainLengthBytes = Arrays.copyOfRange(message, 0, 4);
        byte[] currentChainIndexBytes = Arrays.copyOfRange(message, 4, 8);
        byte[] encryptedMessage = Arrays.copyOfRange(message, 8, message.length);

        int previousChainLength = ByteBuffer.wrap(previousChainLengthBytes).getInt();
        int currentChainIndex = ByteBuffer.wrap(currentChainIndexBytes).getInt();

        byte[] inboundMessageKey = keyManager.getInboundMessageKey(peerUserId, ephemeralKey, ephemeralKeyId, previousChainLength, currentChainIndex);

        byte[] aesKey = Arrays.copyOfRange(inboundMessageKey, 0, 32);
        byte[] hmacKey = Arrays.copyOfRange(inboundMessageKey, 32, 64);
        byte[] iv = Arrays.copyOfRange(inboundMessageKey, 64, 80);

        Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding"); // NOTE: for AES same as PKCS5
        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        c.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
        byte[] ret =  c.doFinal(encryptedMessage);

        CryptoUtil.nullify(inboundMessageKey, aesKey, hmacKey, iv);

        return ret;
    }

    byte[] convertForWire(byte[] message, UserId peerUserId) throws Exception {
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

        CryptoUtil.nullify(outboundMessageKey, aesKey, hmacKey, iv);

        byte[] previousChainLengthBytes = ByteBuffer.allocate(COUNTER_SIZE_BYTES).putInt(messageKey.getPreviousChainLength()).array();
        byte[] currentChainIndexBytes = ByteBuffer.allocate(COUNTER_SIZE_BYTES).putInt(messageKey.getCurrentChainIndex()).array();

        return CryptoUtil.concat(previousChainLengthBytes, currentChainIndexBytes, encryptedContents);
    }
}
