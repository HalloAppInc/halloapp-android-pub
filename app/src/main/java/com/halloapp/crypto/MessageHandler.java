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

public class MessageHandler {

    private final EncryptedKeyStore myEncryptedKeyStore;
    private final KeyManager myKeyManager;

    public MessageHandler() {
        this.myEncryptedKeyStore = EncryptedKeyStore.getInstance();
        this.myKeyManager = KeyManager.getInstance();
    }

    // TODO(jack): Probably move this up to SessionManager
    public byte[] receiveFirstMessage(byte[] message, UserId peerUserId, byte[] identityKey, PublicXECKey ephemeralKey, Integer ephemeralKeyId, Integer oneTimePreKeyId) throws Exception {
        if (!myEncryptedKeyStore.getPeerResponded(peerUserId)) {
            myKeyManager.receiveSessionSetup(peerUserId, ephemeralKey, ephemeralKeyId, identityKey, oneTimePreKeyId);
            myEncryptedKeyStore.setPeerResponded(peerUserId, true);
        }

        return convertFromWire(message, peerUserId, ephemeralKey, ephemeralKeyId);
    }

    public byte[] convertFromWire(byte[] message, UserId peerUserId, PublicXECKey ephemeralKey, Integer ephemeralKeyId) throws Exception {
        boolean shouldUpdateChains = ephemeralKeyId != myEncryptedKeyStore.getInboundEphemeralKeyId(peerUserId);
        if (shouldUpdateChains) {
            myKeyManager.updateInboundChainAndRootKey(peerUserId, myEncryptedKeyStore.getOutboundEphemeralKey(peerUserId), ephemeralKey);
            myEncryptedKeyStore.setInboundEphemeralKeyId(peerUserId, ephemeralKeyId);
            myEncryptedKeyStore.setInboundEphemeralKey(peerUserId, ephemeralKey);
        }

        byte[] inboundMessageKey = myKeyManager.getNextInboundMessageKey(peerUserId);
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
            int lastSentId = myEncryptedKeyStore.getOutboundEphemeralKeyId(peerUserId);
            myEncryptedKeyStore.setOutboundEphemeralKeyId(peerUserId, lastSentId + 1);
            myEncryptedKeyStore.setOutboundEphemeralKey(peerUserId, newEphemeralKey);
            myKeyManager.updateOutboundChainAndRootKey(peerUserId, newEphemeralKey, ephemeralKey);
        }

        CryptoUtil.nullify(inboundMessageKey, aesKey, hmacKey, iv);

        return ret;
    }

    public byte[] convertForWire(byte[] message, UserId peerUserId) throws Exception {
        byte[] outboundMessageKey = myKeyManager.getNextOutboundMessageKey(peerUserId);
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
