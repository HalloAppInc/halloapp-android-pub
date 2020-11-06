package com.halloapp.crypto.keys;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.protobuf.ByteString;
import com.halloapp.Constants;
import com.halloapp.crypto.CryptoException;
import com.halloapp.id.UserId;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.SessionSetupInfo;
import com.halloapp.proto.clients.IdentityKey;
import com.halloapp.proto.clients.SignedPreKey;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class KeyManager {

    private static KeyManager instance;

    private static final byte[] HKDF_ROOT_KEY_INFO = "HalloApp".getBytes(StandardCharsets.UTF_8);
    private static final byte[] HKDF_INPUT_MESSAGE_KEY = new byte[]{1};
    private static final byte[] HKDF_INPUT_CHAIN_KEY = new byte[]{2};

    private static final int KEYS_VERSION = 1;

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

    @WorkerThread
    public void ensureKeysUploaded(Connection connection) {
        if (!Constants.ENCRYPTION_TURNED_ON) {
            return;
        }

        if (encryptedKeyStore.getKeysVersion() < KEYS_VERSION) {
            Log.i("KeyManager keys version outdated; clearing key store");
            encryptedKeyStore.clearAll();
        }

        if (!encryptedKeyStore.getKeysUploaded()) {
            encryptedKeyStore.generateClientPrivateKeys();

            IdentityKey identityKeyProto = IdentityKey.newBuilder()
                    .setPublicKey(ByteString.copyFrom(encryptedKeyStore.getMyPublicEd25519IdentityKey().getKeyMaterial()))
                    .build();

            PublicXECKey signedPreKey = encryptedKeyStore.getMyPublicSignedPreKey();
            byte[] signature = CryptoUtils.verifyDetached(signedPreKey.getKeyMaterial(), encryptedKeyStore.getMyPrivateEd25519IdentityKey());

            SignedPreKey signedPreKeyProto = SignedPreKey.newBuilder()
                    .setPublicKey(ByteString.copyFrom(signedPreKey.getKeyMaterial()))
                    .setSignature(ByteString.copyFrom(signature))
                    // TODO(jack): ID
                    .build();

            List<byte[]> oneTimePreKeys = new ArrayList<>();
            for (OneTimePreKey otpk : encryptedKeyStore.getNewBatchOfOneTimePreKeys()) {
                com.halloapp.proto.clients.OneTimePreKey toAdd = com.halloapp.proto.clients.OneTimePreKey.newBuilder()
                        .setId(otpk.id)
                        .setPublicKey(ByteString.copyFrom(otpk.publicXECKey.getKeyMaterial()))
                        .build();
                oneTimePreKeys.add(toAdd.toByteArray());
            }

            Future<Boolean> success = connection.uploadKeys(identityKeyProto.toByteArray(), signedPreKeyProto.toByteArray(), oneTimePreKeys);
            try {
                if (success.get()) {
                    encryptedKeyStore.setKeysUploaded(true);
                    encryptedKeyStore.setKeysVersion(KEYS_VERSION);
                } else {
                    Log.e("Key upload failed");
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.e("Exception awaiting key upload result", e);
            }
        } else {
            Log.i("Keys were already uploaded");
        }
    }

    public void tearDownSession(UserId peerUserId) {
        Log.i("KeyManager tearing down session with user " + peerUserId);
        encryptedKeyStore.clearSessionAlreadySetUp(peerUserId);
        encryptedKeyStore.clearLastDownloadAttempt(peerUserId);
        encryptedKeyStore.clearPeerResponded(peerUserId);
        encryptedKeyStore.clearSkippedMessageKeys(peerUserId);
        encryptedKeyStore.clearPeerPublicIdentityKey(peerUserId);
        encryptedKeyStore.clearPeerSignedPreKey(peerUserId);
        encryptedKeyStore.clearPeerOneTimePreKey(peerUserId);
        encryptedKeyStore.clearPeerOneTimePreKeyId(peerUserId);
        encryptedKeyStore.clearRootKey(peerUserId);
        encryptedKeyStore.clearOutboundChainKey(peerUserId);
        encryptedKeyStore.clearInboundChainKey(peerUserId);
        encryptedKeyStore.clearInboundEphemeralKey(peerUserId);
        encryptedKeyStore.clearOutboundEphemeralKey(peerUserId);
        encryptedKeyStore.clearInboundEphemeralKeyId(peerUserId);
        encryptedKeyStore.clearOutboundEphemeralKeyId(peerUserId);
        encryptedKeyStore.clearInboundPreviousChainLength(peerUserId);
        encryptedKeyStore.clearOutboundPreviousChainLength(peerUserId);
        encryptedKeyStore.clearInboundCurrentChainIndex(peerUserId);
        encryptedKeyStore.clearOutboundCurrentChainIndex(peerUserId);
    }

    public void setUpSession(UserId peerUserId, PublicEdECKey recipientPublicIdentityKey, PublicXECKey recipientPublicSignedPreKey, @Nullable OneTimePreKey recipientPublicOneTimePreKey) throws CryptoException {
        encryptedKeyStore.setPeerPublicIdentityKey(peerUserId, recipientPublicIdentityKey);
        encryptedKeyStore.setPeerSignedPreKey(peerUserId, recipientPublicSignedPreKey);
        if (recipientPublicOneTimePreKey != null) {
            encryptedKeyStore.setPeerOneTimePreKey(peerUserId, recipientPublicOneTimePreKey.publicXECKey);
            encryptedKeyStore.setPeerOneTimePreKeyId(peerUserId, recipientPublicOneTimePreKey.id);
        }

        PrivateXECKey privateEphemeralKey = XECKey.generatePrivateKey();
        PrivateXECKey myPrivateIdentityKey = encryptedKeyStore.getMyPrivateX25519IdentityKey();

        try {
            byte[] a = CryptoUtils.ecdh(myPrivateIdentityKey, recipientPublicSignedPreKey);
            byte[] b = CryptoUtils.ecdh(privateEphemeralKey, CryptoUtils.convertPublicEdToX(recipientPublicIdentityKey));
            byte[] c = CryptoUtils.ecdh(privateEphemeralKey, recipientPublicSignedPreKey);

            byte[] masterSecret;
            if (recipientPublicOneTimePreKey != null) {
                byte[] d = CryptoUtils.ecdh(privateEphemeralKey, recipientPublicOneTimePreKey.publicXECKey);
                masterSecret = CryptoUtils.concat(a, b, c, d);
                CryptoUtils.nullify(d);
            } else {
                masterSecret = CryptoUtils.concat(a, b, c);
            }

            byte[] output = CryptoUtils.hkdf(masterSecret, null, HKDF_ROOT_KEY_INFO, 96);
            byte[] rootKey = Arrays.copyOfRange(output, 0, 32);
            byte[] outboundChainKey = Arrays.copyOfRange(output, 32, 64);
            byte[] inboundChainKey = Arrays.copyOfRange(output, 64, 96);

            int firstId = 1;

            encryptedKeyStore.setRootKey(peerUserId, rootKey);
            encryptedKeyStore.setOutboundChainKey(peerUserId, outboundChainKey);
            encryptedKeyStore.setInboundChainKey(peerUserId, inboundChainKey);
            encryptedKeyStore.setOutboundEphemeralKey(peerUserId, privateEphemeralKey);
            encryptedKeyStore.setOutboundEphemeralKeyId(peerUserId, firstId);

            CryptoUtils.nullify(a, b, c, masterSecret, output, rootKey, outboundChainKey, inboundChainKey);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("setup has bad keys", e);
        }
    }

    public void receiveSessionSetup(UserId peerUserId, byte[] message, @NonNull SessionSetupInfo sessionSetupInfo) throws CryptoException {
        byte[] ephemeralKeyBytes = Arrays.copyOfRange(message, 0, 32);
        byte[] ephemeralKeyIdBytes = Arrays.copyOfRange(message, 32, 36);

        int ephemeralKeyId = ByteBuffer.wrap(ephemeralKeyIdBytes).getInt();
        PublicXECKey publicEphemeralKey = new PublicXECKey(ephemeralKeyBytes);

        encryptedKeyStore.setPeerPublicIdentityKey(peerUserId, sessionSetupInfo.identityKey);

        try {
            byte[] a = CryptoUtils.ecdh(encryptedKeyStore.getMyPrivateSignedPreKey(), CryptoUtils.convertPublicEdToX(sessionSetupInfo.identityKey));
            byte[] b = CryptoUtils.ecdh(encryptedKeyStore.getMyPrivateX25519IdentityKey(), publicEphemeralKey);
            byte[] c = CryptoUtils.ecdh(encryptedKeyStore.getMyPrivateSignedPreKey(), publicEphemeralKey);

            byte[] masterSecret;
            if (sessionSetupInfo.oneTimePreKeyId != null) {
                byte[] d = CryptoUtils.ecdh(encryptedKeyStore.removeOneTimePreKeyById(sessionSetupInfo.oneTimePreKeyId), publicEphemeralKey);
                masterSecret = CryptoUtils.concat(a, b, c, d);
                CryptoUtils.nullify(d);
            } else {
                masterSecret = CryptoUtils.concat(a, b, c);
            }

            byte[] output = CryptoUtils.hkdf(masterSecret, null, HKDF_ROOT_KEY_INFO, 96);
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

            CryptoUtils.nullify(a, b, c, masterSecret, output, rootKey, inboundChainKey, outboundChainKey);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("setup received bad keys", e);
        }
    }

    public MessageKey getNextOutboundMessageKey(UserId peerUserId) throws CryptoException {
        int ephemeralKeyId = encryptedKeyStore.getOutboundEphemeralKeyId(peerUserId);
        int previousChainLength = encryptedKeyStore.getOutboundPreviousChainLength(peerUserId);
        int currentChainIndex = encryptedKeyStore.getOutboundCurrentChainIndex(peerUserId);

        byte[] messageKey = getNextMessageKey(peerUserId, true);
        int newIndex = currentChainIndex + 1;
        encryptedKeyStore.setOutboundCurrentChainIndex(peerUserId, newIndex);

        try {
            return new MessageKey(ephemeralKeyId, previousChainLength, currentChainIndex, messageKey);
        } catch (InvalidKeyException e) {
            throw new CryptoException("invalid message key", e);
        }
    }

    private byte[] getNextInboundMessageKey(UserId peerUserId) throws CryptoException {
        return getNextMessageKey(peerUserId, false);
    }

    public byte[] getInboundMessageKey(UserId peerUserId, PublicXECKey ephemeralKey, int ephemeralKeyId, int previousChainLength, int currentChainIndex) throws CryptoException {
        int latestStoredEphemeralKeyId = encryptedKeyStore.getInboundEphemeralKeyId(peerUserId);
        int latestPreviousChainLength = encryptedKeyStore.getInboundPreviousChainLength(peerUserId);
        int latestStoredChainIndex = encryptedKeyStore.getInboundCurrentChainIndex(peerUserId);

        if (ephemeralKeyId < latestStoredEphemeralKeyId || (ephemeralKeyId == latestStoredEphemeralKeyId && currentChainIndex < latestStoredChainIndex)) {
            Log.i("KeyManager retrieving stored message key");
            byte[] messageKey = encryptedKeyStore.removeSkippedMessageKey(peerUserId, ephemeralKeyId, currentChainIndex);
            if (messageKey == null) {
                throw new CryptoException("old message key not found");
            }
            return messageKey;
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

    private void skipInboundKeys(UserId peerUserId, int count, int ephemeralKeyId, int previousChainLength, int startIndex) throws CryptoException {
        Log.i("skipping " + count + " inbound keys");
        for (int i=0; i<count; i++) {
            byte[] inboundMessageKey = getNextInboundMessageKey(peerUserId);
            try {
                MessageKey messageKey = new MessageKey(ephemeralKeyId, previousChainLength, startIndex + i, inboundMessageKey);
                encryptedKeyStore.storeSkippedMessageKey(peerUserId, messageKey);
            } catch (InvalidKeyException e) {
                Log.w("Cannot store invalid incoming message key for later use", e);
            }
        }
    }

    private byte[] getNextMessageKey(UserId peerUserId, boolean isOutbound) throws CryptoException {
        byte[] chainKey = isOutbound ? encryptedKeyStore.getOutboundChainKey(peerUserId) : encryptedKeyStore.getInboundChainKey(peerUserId);

        try {
            byte[] messageKey = CryptoUtils.hkdf(chainKey, null, HKDF_INPUT_MESSAGE_KEY, 80);
            byte[] newChainKey = CryptoUtils.hkdf(chainKey, null, HKDF_INPUT_CHAIN_KEY, 32);

            if (isOutbound) {
                encryptedKeyStore.setOutboundChainKey(peerUserId, newChainKey);
            } else {
                encryptedKeyStore.setInboundChainKey(peerUserId, newChainKey);
            }

            CryptoUtils.nullify(chainKey, newChainKey);

            return messageKey;
        } catch (GeneralSecurityException e) {
            throw new CryptoException((isOutbound ? "outbound" : "inbound") + " sym ratchet failure", e);
        }
    }

    public void updateOutboundChainAndRootKey(UserId peerUserId, PrivateXECKey myEphemeral, PublicXECKey peerEphemeral) throws CryptoException {
        updateChainAndRootKey(peerUserId, myEphemeral, peerEphemeral, true);
    }

    public void updateInboundChainAndRootKey(UserId peerUserId, PrivateXECKey myEphemeral, PublicXECKey peerEphemeral) throws CryptoException {
        updateChainAndRootKey(peerUserId, myEphemeral, peerEphemeral, false);
    }

    private void updateChainAndRootKey(UserId peerUserId, PrivateXECKey myEphemeral, PublicXECKey peerEphemeral, boolean isOutbound) throws CryptoException {
        try {
            byte[] ephemeralSecret = CryptoUtils.ecdh(myEphemeral, peerEphemeral);

            byte[] output = CryptoUtils.hkdf(ephemeralSecret, encryptedKeyStore.getRootKey(peerUserId), HKDF_ROOT_KEY_INFO, 64);
            byte[] rootKey = Arrays.copyOfRange(output, 0, 32);
            byte[] chainKey = Arrays.copyOfRange(output, 32, 64);

            encryptedKeyStore.setRootKey(peerUserId, rootKey);
            if (isOutbound) {
                encryptedKeyStore.setOutboundChainKey(peerUserId, chainKey);
            } else {
                encryptedKeyStore.setInboundChainKey(peerUserId, chainKey);
            }

            CryptoUtils.nullify(ephemeralSecret, output, rootKey, chainKey);
        } catch (GeneralSecurityException e) {
            throw new CryptoException((isOutbound ? "outbound" : "inbound") + " asym ratchet failure", e);
        }
    }

}
