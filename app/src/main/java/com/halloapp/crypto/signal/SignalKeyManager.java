package com.halloapp.crypto.signal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.halloapp.crypto.CryptoByteUtils;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.OneTimePreKey;
import com.halloapp.crypto.keys.PrivateXECKey;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.keys.PublicXECKey;
import com.halloapp.crypto.keys.XECKey;
import com.halloapp.id.UserId;
import com.halloapp.util.logs.Log;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
public class SignalKeyManager {

    private static SignalKeyManager instance;

    private static final byte[] HKDF_ROOT_KEY_INFO = "HalloApp".getBytes(StandardCharsets.UTF_8);
    private static final byte[] HKDF_INPUT_MESSAGE_KEY = new byte[]{1};
    private static final byte[] HKDF_INPUT_CHAIN_KEY = new byte[]{2};

    private final EncryptedKeyStore encryptedKeyStore;

    public static SignalKeyManager getInstance() {
        if (instance == null) {
            synchronized (SignalKeyManager.class) {
                if (instance == null) {
                    instance = new SignalKeyManager(EncryptedKeyStore.getInstance());
                }
            }
        }
        return instance;
    }

    private SignalKeyManager(EncryptedKeyStore encryptedKeyStore) {
        this.encryptedKeyStore = encryptedKeyStore;
    }

    void tearDownSession(UserId peerUserId) {
        Log.i("SignalKeyManager tearing down session with user " + peerUserId);
        encryptedKeyStore.clearPeerVerified(peerUserId);
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
        // teardown keys intentionally omitted
    }

    void setUpSession(UserId peerUserId, PublicEdECKey recipientPublicIdentityKey, PublicXECKey recipientPublicSignedPreKey, @Nullable OneTimePreKey recipientPublicOneTimePreKey) throws CryptoException {
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
                masterSecret = CryptoByteUtils.concat(a, b, c, d);
                CryptoByteUtils.nullify(d);
            } else {
                masterSecret = CryptoByteUtils.concat(a, b, c);
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

            encryptedKeyStore.setSessionAlreadySetUp(peerUserId, true);

            CryptoByteUtils.nullify(a, b, c, masterSecret, output, rootKey, outboundChainKey, inboundChainKey);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("setup_has_bad_keys", e);
        }
    }

    void receiveSessionSetup(UserId peerUserId, byte[] message, @NonNull SignalSessionSetupInfo signalSessionSetupInfo) throws CryptoException {
        byte[] ephemeralKeyBytes = Arrays.copyOfRange(message, 0, 32);
        byte[] ephemeralKeyIdBytes = Arrays.copyOfRange(message, 32, 36);

        int ephemeralKeyId = ByteBuffer.wrap(ephemeralKeyIdBytes).getInt();
        PublicXECKey publicEphemeralKey = new PublicXECKey(ephemeralKeyBytes);

        receiveSessionSetup(peerUserId, publicEphemeralKey, ephemeralKeyId, signalSessionSetupInfo);
    }

    void receiveSessionSetup(UserId peerUserId, PublicXECKey publicEphemeralKey, int ephemeralKeyId, @NonNull SignalSessionSetupInfo signalSessionSetupInfo) throws CryptoException {
        encryptedKeyStore.setPeerPublicIdentityKey(peerUserId, signalSessionSetupInfo.identityKey);

        try {
            byte[] a = CryptoUtils.ecdh(encryptedKeyStore.getMyPrivateSignedPreKey(), CryptoUtils.convertPublicEdToX(signalSessionSetupInfo.identityKey));
            byte[] b = CryptoUtils.ecdh(encryptedKeyStore.getMyPrivateX25519IdentityKey(), publicEphemeralKey);
            byte[] c = CryptoUtils.ecdh(encryptedKeyStore.getMyPrivateSignedPreKey(), publicEphemeralKey);

            byte[] masterSecret;
            if (signalSessionSetupInfo.oneTimePreKeyId != null) {
                byte[] d;
                try {
                    d = CryptoUtils.ecdh(encryptedKeyStore.removeOneTimePreKeyById(signalSessionSetupInfo.oneTimePreKeyId), publicEphemeralKey);
                } catch (CryptoException e) {
                    tearDownSession(peerUserId);
                    throw e;
                }
                masterSecret = CryptoByteUtils.concat(a, b, c, d);
                CryptoByteUtils.nullify(d);
            } else {
                masterSecret = CryptoByteUtils.concat(a, b, c);
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

            encryptedKeyStore.setSessionAlreadySetUp(peerUserId, true);

            CryptoByteUtils.nullify(a, b, c, masterSecret, output, rootKey, inboundChainKey, outboundChainKey);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("setup_received_bad_keys", e);
        }
    }

    public SignalMessageKey getNextOutboundMessageKey(UserId peerUserId) throws CryptoException {
        int ephemeralKeyId = encryptedKeyStore.getOutboundEphemeralKeyId(peerUserId);
        int previousChainLength = encryptedKeyStore.getOutboundPreviousChainLength(peerUserId);
        int currentChainIndex = encryptedKeyStore.getOutboundCurrentChainIndex(peerUserId);

        byte[] messageKey = getNextMessageKey(peerUserId, true);
        int newIndex = currentChainIndex + 1;
        encryptedKeyStore.setOutboundCurrentChainIndex(peerUserId, newIndex);

        return new SignalMessageKey(ephemeralKeyId, previousChainLength, currentChainIndex, messageKey);
    }

    private byte[] getNextInboundMessageKey(UserId peerUserId) throws CryptoException {
        return getNextMessageKey(peerUserId, false);
    }

    byte[] getInboundMessageKey(UserId peerUserId, PublicXECKey ephemeralKey, int ephemeralKeyId, int previousChainLength, int currentChainIndex) throws CryptoException {
        int latestStoredEphemeralKeyId = encryptedKeyStore.getInboundEphemeralKeyId(peerUserId);
        int latestPreviousChainLength = encryptedKeyStore.getInboundPreviousChainLength(peerUserId);
        int latestStoredChainIndex = encryptedKeyStore.getInboundCurrentChainIndex(peerUserId);

        if (ephemeralKeyId < latestStoredEphemeralKeyId || (ephemeralKeyId == latestStoredEphemeralKeyId && currentChainIndex < latestStoredChainIndex)) {
            Log.i("SignalKeyManager retrieving stored message key");
            byte[] messageKey = encryptedKeyStore.removeSkippedMessageKey(peerUserId, ephemeralKeyId, currentChainIndex);
            if (messageKey == null) {
                throw new CryptoException("old_message_key_not_found");
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
            encryptedKeyStore.setOutboundCurrentChainIndex(peerUserId, 0);
        }

        return messageKey;
    }

    private void skipInboundKeys(UserId peerUserId, int count, int ephemeralKeyId, int previousChainLength, int startIndex) throws CryptoException {
        Log.i("skipping " + count + " inbound keys");
        for (int i=0; i<count; i++) {
            byte[] inboundMessageKey = getNextInboundMessageKey(peerUserId);
            try {
                SignalMessageKey signalMessageKey = new SignalMessageKey(ephemeralKeyId, previousChainLength, startIndex + i, inboundMessageKey);
                encryptedKeyStore.storeSkippedMessageKey(peerUserId, signalMessageKey);
            } catch (CryptoException e) {
                Log.e("Cannot store invalid incoming message key for later use", e);
                throw new CryptoException("skip_msg_key_" + e.getMessage());
            }
        }
    }

    private byte[] getNextMessageKey(UserId peerUserId, boolean isOutbound) throws CryptoException {
        byte[] chainKey = isOutbound ? encryptedKeyStore.getOutboundChainKey(peerUserId) : encryptedKeyStore.getInboundChainKey(peerUserId);
        Log.d("Getting message key for " + peerUserId + "; " + (isOutbound ? "outbound" : "inbound") + " chain key hash: " + CryptoByteUtils.obfuscate(chainKey));

        try {
            byte[] messageKey = CryptoUtils.hkdf(chainKey, null, HKDF_INPUT_MESSAGE_KEY, 80);
            byte[] newChainKey = CryptoUtils.hkdf(chainKey, null, HKDF_INPUT_CHAIN_KEY, 32);

            if (isOutbound) {
                encryptedKeyStore.setOutboundChainKey(peerUserId, newChainKey);
            } else {
                encryptedKeyStore.setInboundChainKey(peerUserId, newChainKey);
            }

            CryptoByteUtils.nullify(chainKey, newChainKey);

            return messageKey;
        } catch (GeneralSecurityException e) {
            throw new CryptoException((isOutbound ? "outbound" : "inbound") + "_sym_ratchet_failure", e);
        }
    }

    private void updateOutboundChainAndRootKey(UserId peerUserId, PrivateXECKey myEphemeral, PublicXECKey peerEphemeral) throws CryptoException {
        updateChainAndRootKey(peerUserId, myEphemeral, peerEphemeral, true);
    }

    private void updateInboundChainAndRootKey(UserId peerUserId, PrivateXECKey myEphemeral, PublicXECKey peerEphemeral) throws CryptoException {
        updateChainAndRootKey(peerUserId, myEphemeral, peerEphemeral, false);
    }

    private void updateChainAndRootKey(UserId peerUserId, PrivateXECKey myEphemeral, PublicXECKey peerEphemeral, boolean isOutbound) throws CryptoException {
        try {
            byte[] ephemeralSecret = CryptoUtils.ecdh(myEphemeral, peerEphemeral);

            byte[] output = CryptoUtils.hkdf(ephemeralSecret, encryptedKeyStore.getRootKey(peerUserId), HKDF_ROOT_KEY_INFO, 64);
            byte[] rootKey = Arrays.copyOfRange(output, 0, 32);
            byte[] chainKey = Arrays.copyOfRange(output, 32, 64);
            Log.d("root key with " + peerUserId + " updated to hash: " + CryptoByteUtils.obfuscate(rootKey));

            encryptedKeyStore.setRootKey(peerUserId, rootKey);
            if (isOutbound) {
                encryptedKeyStore.setOutboundChainKey(peerUserId, chainKey);
            } else {
                encryptedKeyStore.setInboundChainKey(peerUserId, chainKey);
            }

            CryptoByteUtils.nullify(ephemeralSecret, output, rootKey, chainKey);
        } catch (GeneralSecurityException e) {
            throw new CryptoException((isOutbound ? "outbound" : "inbound") + "_asym_ratchet_failure", e);
        }
    }

}
