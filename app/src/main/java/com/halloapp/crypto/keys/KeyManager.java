package com.halloapp.crypto.keys;

import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.halloapp.Constants;
import com.halloapp.contacts.UserId;
import com.halloapp.crypto.CryptoUtil;
import com.halloapp.proto.IdentityKey;
import com.halloapp.proto.SignedPreKey;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeyManager {

    private static KeyManager instance;

    private static final byte[] HKDF_ROOT_KEY_INFO = "HalloApp".getBytes(StandardCharsets.UTF_8);
    private static final byte[] HKDF_INPUT_MESSAGE_KEY = new byte[]{1};
    private static final byte[] HKDF_INPUT_CHAIN_KEY = new byte[]{2};

    private EncryptedKeyStore encryptedKeyStore;

    public static KeyManager getInstance() {
        if (instance == null) {
            synchronized (KeyManager.class) {
                if (instance == null) {
                    instance = new KeyManager(EncryptedKeyStore.getInstance());
                }
            }
        }
        return instance;
    }

    private KeyManager(EncryptedKeyStore encryptedKeyStore) {
        this.encryptedKeyStore = encryptedKeyStore;
    }

    public void ensureKeysUploaded(Connection connection) {
        if (!Constants.ENCRYPTION_TURNED_ON) {
            return;
        }

        if (!encryptedKeyStore.getKeysUploaded()) {
            encryptedKeyStore.generateClientPrivateKeys();

            IdentityKey identityKeyProto = IdentityKey.newBuilder()
                    .setPublicKey(ByteString.copyFrom(encryptedKeyStore.getMyPublicEd25519IdentityKey().getKeyMaterial()))
                    .build();

            PublicXECKey signedPreKey = encryptedKeyStore.getMyPublicSignedPreKey();
            byte[] signature = CryptoUtil.sign(signedPreKey.getKeyMaterial(), encryptedKeyStore.getMyPrivateEd25519IdentityKey());

            SignedPreKey signedPreKeyProto = SignedPreKey.newBuilder()
                    .setPublicKey(ByteString.copyFrom(signedPreKey.getKeyMaterial()))
                    .setSignature(ByteString.copyFrom(signature))
                    // TODO(jack): ID
                    .build();

            List<byte[]> oneTimePreKeys = new ArrayList<>();
            for (OneTimePreKey otpk : encryptedKeyStore.getNewBatchOfOneTimePreKeys()) {
                com.halloapp.proto.OneTimePreKey toAdd = com.halloapp.proto.OneTimePreKey.newBuilder()
                        .setId(otpk.id)
                        .setPublicKey(ByteString.copyFrom(otpk.publicXECKey.getKeyMaterial()))
                        .build();
                oneTimePreKeys.add(toAdd.toByteArray());
            }

            // TODO(jack): Check for success
            connection.uploadKeys(identityKeyProto.toByteArray(), signedPreKeyProto.toByteArray(), oneTimePreKeys);

            encryptedKeyStore.setKeysUploaded(true);
        } else {
            Log.i("Keys were already uploaded");
        }
    }

    public void setUpSession(UserId peerUserId, PublicEdECKey recipientPublicIdentityKey, PublicXECKey recipientPublicSignedPreKey, @Nullable OneTimePreKey recipientPublicOneTimePreKey) throws Exception {
        encryptedKeyStore.setPeerPublicIdentityKey(peerUserId, recipientPublicIdentityKey);
        encryptedKeyStore.setPeerSignedPreKey(peerUserId, recipientPublicSignedPreKey);
        if (recipientPublicOneTimePreKey != null) {
            encryptedKeyStore.setPeerOneTimePreKey(peerUserId, recipientPublicOneTimePreKey.publicXECKey);
            encryptedKeyStore.setPeerOneTimePreKeyId(peerUserId, recipientPublicOneTimePreKey.id);
        }

        PrivateXECKey privateEphemeralKey = XECKey.generatePrivateKey();
        PrivateXECKey myPrivateIdentityKey = encryptedKeyStore.getMyPrivateX25519IdentityKey();

        byte[] a = CryptoUtil.ecdh(myPrivateIdentityKey, recipientPublicSignedPreKey);
        byte[] b = CryptoUtil.ecdh(privateEphemeralKey, CryptoUtil.convertPublicEdToX(recipientPublicIdentityKey));
        byte[] c = CryptoUtil.ecdh(privateEphemeralKey, recipientPublicSignedPreKey);

        byte[] masterSecret;
        if (recipientPublicOneTimePreKey != null) {
            byte[] d = CryptoUtil.ecdh(privateEphemeralKey, recipientPublicOneTimePreKey.publicXECKey);
            masterSecret = CryptoUtil.concat(a, b, c, d);
            CryptoUtil.nullify(d);
        } else {
            masterSecret = CryptoUtil.concat(a, b, c);
        }

        byte[] output = CryptoUtil.hkdf(masterSecret, null, HKDF_ROOT_KEY_INFO, 96);
        byte[] rootKey = Arrays.copyOfRange(output, 0, 32);
        byte[] outboundChainKey = Arrays.copyOfRange(output, 32, 64);
        byte[] inboundChainKey = Arrays.copyOfRange(output, 64, 96);

        int firstId = 1;

        encryptedKeyStore.setRootKey(peerUserId, rootKey);
        encryptedKeyStore.setOutboundChainKey(peerUserId, outboundChainKey);
        encryptedKeyStore.setInboundChainKey(peerUserId, inboundChainKey);
        encryptedKeyStore.setOutboundEphemeralKey(peerUserId, privateEphemeralKey);
        encryptedKeyStore.setOutboundEphemeralKeyId(peerUserId, firstId);

        CryptoUtil.nullify(a, b, c, masterSecret, output, rootKey, outboundChainKey, inboundChainKey);
    }

    public void receiveSessionSetup(UserId peerUserId, PublicXECKey publicEphemeralKey, int ephemeralKeyId, PublicEdECKey initiatorPublicIdentityKey, @Nullable Integer oneTimePreKeyId) throws Exception {
        encryptedKeyStore.setPeerPublicIdentityKey(peerUserId, initiatorPublicIdentityKey);

        byte[] a = CryptoUtil.ecdh(encryptedKeyStore.getMyPrivateSignedPreKey(), CryptoUtil.convertPublicEdToX(initiatorPublicIdentityKey));
        byte[] b = CryptoUtil.ecdh(encryptedKeyStore.getMyPrivateX25519IdentityKey(), publicEphemeralKey);
        byte[] c = CryptoUtil.ecdh(encryptedKeyStore.getMyPrivateSignedPreKey(), publicEphemeralKey);

        byte[] masterSecret;
        if (oneTimePreKeyId != null) {
            byte[] d = CryptoUtil.ecdh(encryptedKeyStore.removeOneTimePreKeyById(oneTimePreKeyId), publicEphemeralKey);
            masterSecret = CryptoUtil.concat(a, b, c, d);
            CryptoUtil.nullify(d);
        } else {
            masterSecret = CryptoUtil.concat(a, b, c);
        }

        byte[] output = CryptoUtil.hkdf(masterSecret, null, HKDF_ROOT_KEY_INFO, 96);
        byte[] rootKey = Arrays.copyOfRange(output, 0, 32);

        // NOTE: Order switched so that keys match appropriately
        byte[] inboundChainKey = Arrays.copyOfRange(output, 32, 64);
        byte[] outboundChainKey = Arrays.copyOfRange(output, 64, 96);

        encryptedKeyStore.setRootKey(peerUserId, rootKey);
        encryptedKeyStore.setOutboundChainKey(peerUserId, outboundChainKey);
        encryptedKeyStore.setInboundChainKey(peerUserId, inboundChainKey);
        encryptedKeyStore.setInboundEphemeralKey(peerUserId, publicEphemeralKey);
        encryptedKeyStore.setInboundEphemeralKeyId(peerUserId, ephemeralKeyId);

        PrivateXECKey myEphemeralKey = XECKey.generatePrivateKey();
        encryptedKeyStore.setOutboundEphemeralKey(peerUserId, myEphemeralKey);
        encryptedKeyStore.setOutboundEphemeralKeyId(peerUserId, 0);

        updateOutboundChainAndRootKey(peerUserId, myEphemeralKey, publicEphemeralKey);

        CryptoUtil.nullify(a, b, c, masterSecret, output, rootKey, inboundChainKey, outboundChainKey);
    }

    public MessageKey getNextOutboundMessageKey(UserId peerUserId) throws Exception {
        int ephemeralKeyId = encryptedKeyStore.getOutboundEphemeralKeyId(peerUserId);
        int previousChainLength = encryptedKeyStore.getOutboundPreviousChainLength(peerUserId);
        int currentChainIndex = encryptedKeyStore.getOutboundCurrentChainIndex(peerUserId);

        byte[] messageKey = getNextMessageKey(peerUserId, true);
        int newIndex = currentChainIndex + 1;
        encryptedKeyStore.setOutboundCurrentChainIndex(peerUserId, newIndex);

        return new MessageKey(ephemeralKeyId, previousChainLength, currentChainIndex, messageKey);
    }

    private byte[] getNextInboundMessageKey(UserId peerUserId) throws Exception {
        return getNextMessageKey(peerUserId, false);
    }

    public byte[] getInboundMessageKey(UserId peerUserId, PublicXECKey ephemeralKey, int ephemeralKeyId, int previousChainLength, int currentChainIndex) throws Exception {
        int latestStoredEphemeralKeyId = encryptedKeyStore.getInboundEphemeralKeyId(peerUserId);
        int latestPreviousChainLength = encryptedKeyStore.getInboundPreviousChainLength(peerUserId);
        int latestStoredChainIndex = encryptedKeyStore.getInboundCurrentChainIndex(peerUserId);

        if (ephemeralKeyId < latestStoredEphemeralKeyId || (ephemeralKeyId == latestStoredEphemeralKeyId && currentChainIndex < latestStoredChainIndex)) {
            Log.i("KeyManager retrieving stored message key");
            return encryptedKeyStore.removeSkippedMessageKey(peerUserId, ephemeralKeyId, currentChainIndex);
        }

        boolean shouldUpdateChains = ephemeralKeyId != latestStoredEphemeralKeyId;
        if (shouldUpdateChains) {
            skipInboundKeys(peerUserId, previousChainLength - latestStoredChainIndex, latestStoredEphemeralKeyId, latestPreviousChainLength, latestStoredChainIndex);

            updateInboundChainAndRootKey(peerUserId, encryptedKeyStore.getOutboundEphemeralKey(peerUserId), ephemeralKey);
            encryptedKeyStore.setInboundEphemeralKeyId(peerUserId, ephemeralKeyId);
            encryptedKeyStore.setInboundEphemeralKey(peerUserId, ephemeralKey);

            encryptedKeyStore.setInboundPreviousChainLength(peerUserId, previousChainLength);
            encryptedKeyStore.setInboundCurrentChainIndex(peerUserId, 0);
        }

        int currentStoredIndex = encryptedKeyStore.getInboundCurrentChainIndex(peerUserId); // NOTE: could have been updated in if statement above
        skipInboundKeys(peerUserId, currentChainIndex - currentStoredIndex, ephemeralKeyId, previousChainLength, currentStoredIndex);

        byte[] messageKey = getNextMessageKey(peerUserId, false);

        encryptedKeyStore.setInboundCurrentChainIndex(peerUserId, currentChainIndex + 1);

        if (shouldUpdateChains) {
            PrivateXECKey newEphemeralKey = XECKey.generatePrivateKey();
            int lastSentId = encryptedKeyStore.getOutboundEphemeralKeyId(peerUserId);
            encryptedKeyStore.setOutboundEphemeralKeyId(peerUserId, lastSentId + 1);
            encryptedKeyStore.setOutboundEphemeralKey(peerUserId, newEphemeralKey);
            updateOutboundChainAndRootKey(peerUserId, newEphemeralKey, ephemeralKey);

            encryptedKeyStore.setOutboundPreviousChainLength(peerUserId, encryptedKeyStore.getOutboundCurrentChainIndex(peerUserId));
            encryptedKeyStore.setOutboundCurrentChainIndex(peerUserId, 0); // TODO(jack): synchronization (per user would work?)
        }

        return messageKey;
    }

    private void skipInboundKeys(UserId peerUserId, int count, int ephemeralKeyId, int previousChainLength, int startIndex) throws Exception {
        Log.i("skipping " + count + " inbound keys");
        for (int i=0; i<count; i++) {
            byte[] inboundMessageKey = getNextInboundMessageKey(peerUserId);
            MessageKey messageKey = new MessageKey(ephemeralKeyId, previousChainLength, startIndex + i, inboundMessageKey);
            encryptedKeyStore.storeSkippedMessageKey(peerUserId, messageKey);
        }
    }

    private byte[] getNextMessageKey(UserId peerUserId, boolean isOutbound) throws Exception {
        byte[] chainKey = isOutbound ? encryptedKeyStore.getOutboundChainKey(peerUserId) : encryptedKeyStore.getInboundChainKey(peerUserId);

        byte[] messageKey = CryptoUtil.hkdf(chainKey, null, HKDF_INPUT_MESSAGE_KEY, 80);
        byte[] newChainKey = CryptoUtil.hkdf(chainKey, null, HKDF_INPUT_CHAIN_KEY, 32);

        if (isOutbound) {
            encryptedKeyStore.setOutboundChainKey(peerUserId, newChainKey);
        } else {
            encryptedKeyStore.setInboundChainKey(peerUserId, newChainKey);
        }

        CryptoUtil.nullify(chainKey, newChainKey);

        return messageKey;
    }

    public void updateOutboundChainAndRootKey(UserId peerUserId, PrivateXECKey myEphemeral, PublicXECKey peerEphemeral) throws Exception {
        updateChainAndRootKey(peerUserId, myEphemeral, peerEphemeral, true);
    }

    public void updateInboundChainAndRootKey(UserId peerUserId, PrivateXECKey myEphemeral, PublicXECKey peerEphemeral) throws Exception {
        updateChainAndRootKey(peerUserId, myEphemeral, peerEphemeral, false);
    }

    private void updateChainAndRootKey(UserId peerUserId, PrivateXECKey myEphemeral, PublicXECKey peerEphemeral, boolean isOutbound) throws Exception {
        byte[] ephemeralSecret = CryptoUtil.ecdh(myEphemeral, peerEphemeral);

        byte[] output = CryptoUtil.hkdf(ephemeralSecret, encryptedKeyStore.getRootKey(peerUserId), HKDF_ROOT_KEY_INFO, 64);
        byte[] rootKey = Arrays.copyOfRange(output, 0, 32);
        byte[] chainKey = Arrays.copyOfRange(output, 32, 64);

        encryptedKeyStore.setRootKey(peerUserId, rootKey);
        if (isOutbound) {
            encryptedKeyStore.setOutboundChainKey(peerUserId, chainKey);
        } else {
            encryptedKeyStore.setInboundChainKey(peerUserId, chainKey);
        }

        CryptoUtil.nullify(ephemeralSecret, output, rootKey, chainKey);
    }

}
