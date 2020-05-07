package com.halloapp.crypto;

import com.halloapp.contacts.UserId;
import com.halloapp.crypto.keys.PrivateXECKey;
import com.halloapp.crypto.keys.PublicXECKey;
import com.halloapp.crypto.keys.XECKey;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.KeyManager;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class MessageCipher {

    private final EncryptedKeyStore encryptedKeyStore;
    private final KeyManager keyManager;

    MessageCipher() {
        this.encryptedKeyStore = EncryptedKeyStore.getInstance();
        this.keyManager = KeyManager.getInstance();
    }

    byte[] convertFromWire(byte[] message, UserId peerUserId, PublicXECKey ephemeralKey, Integer ephemeralKeyId) throws Exception {
        boolean shouldUpdateChains = ephemeralKeyId != encryptedKeyStore.getInboundEphemeralKeyId(peerUserId);
        if (shouldUpdateChains) {
            keyManager.updateInboundChainAndRootKey(peerUserId, encryptedKeyStore.getOutboundEphemeralKey(peerUserId), ephemeralKey);
            encryptedKeyStore.setInboundEphemeralKeyId(peerUserId, ephemeralKeyId);
            encryptedKeyStore.setInboundEphemeralKey(peerUserId, ephemeralKey);
        }

        byte[] inboundMessageKey = keyManager.getNextInboundMessageKey(peerUserId);
        byte[] aesKey = Arrays.copyOfRange(inboundMessageKey, 0, 32);
        byte[] hmacKey = Arrays.copyOfRange(inboundMessageKey, 32, 64);
        byte[] iv = Arrays.copyOfRange(inboundMessageKey, 64, 80);

        Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding"); // NOTE: for AES same as PKCS5
        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        c.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
        byte[] ret =  c.doFinal(message);

        if (shouldUpdateChains) {
            PrivateXECKey newEphemeralKey = XECKey.generatePrivateKey();
            int lastSentId = encryptedKeyStore.getOutboundEphemeralKeyId(peerUserId);
            encryptedKeyStore.setOutboundEphemeralKeyId(peerUserId, lastSentId + 1);
            encryptedKeyStore.setOutboundEphemeralKey(peerUserId, newEphemeralKey);
            keyManager.updateOutboundChainAndRootKey(peerUserId, newEphemeralKey, ephemeralKey);
        }

        CryptoUtil.nullify(inboundMessageKey, aesKey, hmacKey, iv);

        return ret;
    }

    byte[] convertForWire(byte[] message, UserId peerUserId) throws Exception {
        byte[] outboundMessageKey = keyManager.getNextOutboundMessageKey(peerUserId);
        byte[] aesKey = Arrays.copyOfRange(outboundMessageKey, 0, 32);
        byte[] hmacKey = Arrays.copyOfRange(outboundMessageKey, 32, 64);
        byte[] iv = Arrays.copyOfRange(outboundMessageKey, 64, 80);

        Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding"); // NOTE: for AES same as PKCS5
        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        c.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
        byte[] encryptedContents = c.doFinal(message);

        CryptoUtil.nullify(outboundMessageKey, aesKey, hmacKey, iv);

        return encryptedContents;
    }
}
