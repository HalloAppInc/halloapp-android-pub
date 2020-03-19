package com.halloapp.crypto;

import com.halloapp.contacts.UserId;
import com.halloapp.crypto.keys.ECKey;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.KeyManager;
import com.halloapp.crypto.keys.PrivateECKey;
import com.halloapp.crypto.keys.PublicECKey;

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

    public byte[] receiveFirstMessage(byte[] message, UserId peerUserId, PublicECKey identityKey, PublicECKey ephemeralKey, Integer ephemeralKeyId, Integer oneTimePreKeyId) throws Exception {
        if (!myEncryptedKeyStore.getPeerResponded(peerUserId)) {
            myKeyManager.receiveSessionSetup(peerUserId, ephemeralKey, ephemeralKeyId, identityKey, oneTimePreKeyId);
            myEncryptedKeyStore.setPeerResponded(peerUserId, true);
        }

        return convertFromWire(message, peerUserId, ephemeralKey, ephemeralKeyId);
    }

    public byte[] convertFromWire(byte[] message, UserId peerUserId, PublicECKey ephemeralKey, Integer ephemeralKeyId) throws Exception {
        boolean shouldUpdateChains = ephemeralKeyId != myEncryptedKeyStore.getLastReceivedEphemeralKeyId(peerUserId);
        if (shouldUpdateChains) {
            myKeyManager.updateInboundChainAndRootKey(peerUserId, myEncryptedKeyStore.getLastSentEphemeralKey(peerUserId), ephemeralKey);
            myEncryptedKeyStore.setLastReceivedEphemeralKeyId(peerUserId, ephemeralKeyId);
            myEncryptedKeyStore.setLastReceivedEphemeralKey(peerUserId, ephemeralKey);
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
            PrivateECKey newEphemeralKey = ECKey.generatePrivateKey();
            int lastSentId = myEncryptedKeyStore.getLastSentEphemeralKeyId(peerUserId);
            myEncryptedKeyStore.setLastSentEphemeralKeyId(peerUserId, lastSentId + 1);
            myEncryptedKeyStore.setLastSentEphemeralKey(peerUserId, newEphemeralKey); // TODO(jack): probably should be "outbound ephemeral key" not last sent
            myKeyManager.updateOutboundChainAndRootKey(peerUserId, newEphemeralKey, ephemeralKey);
        }

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

        return encryptedContents;
    }
}
