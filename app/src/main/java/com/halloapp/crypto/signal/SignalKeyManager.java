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
    private static final int MAX_SIGNAL_KEYS_SKIP = 100;

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
        encryptedKeyStore.edit().clearPeerVerified(peerUserId)
                .clearSessionAlreadySetUp(peerUserId)
                .clearLastDownloadAttempt(peerUserId)
                .clearPeerResponded(peerUserId)
                .clearSkippedMessageKeys(peerUserId)
                .clearPeerPublicIdentityKey(peerUserId)
                .clearPeerSignedPreKey(peerUserId)
                .clearPeerOneTimePreKey(peerUserId)
                .clearPeerOneTimePreKeyId(peerUserId)
                .clearRootKey(peerUserId)
                .clearOutboundChainKey(peerUserId)
                .clearInboundChainKey(peerUserId)
                .clearInboundEphemeralKey(peerUserId)
                .clearOutboundEphemeralKey(peerUserId)
                .clearInboundEphemeralKeyId(peerUserId)
                .clearOutboundEphemeralKeyId(peerUserId)
                .clearInboundPreviousChainLength(peerUserId)
                .clearOutboundPreviousChainLength(peerUserId)
                .clearInboundCurrentChainIndex(peerUserId)
                .clearOutboundCurrentChainIndex(peerUserId)
                .apply();
        // teardown keys intentionally omitted
    }

    void setUpSession(UserId peerUserId, PublicEdECKey recipientPublicIdentityKey, PublicXECKey recipientPublicSignedPreKey, @Nullable OneTimePreKey recipientPublicOneTimePreKey) throws CryptoException {
        EncryptedKeyStore.Editor editor = encryptedKeyStore.edit();
        editor.setPeerPublicIdentityKey(peerUserId, recipientPublicIdentityKey);
        editor.setPeerSignedPreKey(peerUserId, recipientPublicSignedPreKey);
        if (recipientPublicOneTimePreKey != null) {
            editor.setPeerOneTimePreKey(peerUserId, recipientPublicOneTimePreKey.publicXECKey);
            editor.setPeerOneTimePreKeyId(peerUserId, recipientPublicOneTimePreKey.id);
        }
        editor.apply();

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

            encryptedKeyStore.edit()
                    .setRootKey(peerUserId, rootKey)
                    .setOutboundChainKey(peerUserId, outboundChainKey)
                    .setInboundChainKey(peerUserId, inboundChainKey)
                    .setOutboundEphemeralKey(peerUserId, privateEphemeralKey)
                    .setOutboundEphemeralKeyId(peerUserId, firstId)
                    .setSessionAlreadySetUp(peerUserId, true)
                    .apply();

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
        encryptedKeyStore.edit().setPeerPublicIdentityKey(peerUserId, signalSessionSetupInfo.identityKey).apply();

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

            EncryptedKeyStore.Editor editor = encryptedKeyStore.edit()
                    .setRootKey(peerUserId, rootKey)
                    .setOutboundChainKey(peerUserId, outboundChainKey)
                    .setInboundChainKey(peerUserId, inboundChainKey)
                    .setInboundEphemeralKey(peerUserId, publicEphemeralKey)
                    .setInboundEphemeralKeyId(peerUserId, ephemeralKeyId);

            PrivateXECKey myEphemeralKey = XECKey.generatePrivateKey();
            editor.setOutboundEphemeralKey(peerUserId, myEphemeralKey);
            editor.setOutboundEphemeralKeyId(peerUserId, 0);

            editor.apply();
            editor = encryptedKeyStore.edit();
            updateOutboundChainAndRootKey(editor, peerUserId, myEphemeralKey, publicEphemeralKey);

            editor.setSessionAlreadySetUp(peerUserId, true);
            editor.apply();

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
        encryptedKeyStore.edit().setOutboundCurrentChainIndex(peerUserId, newIndex).apply();

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
            Log.i("SignalKeyManager retrieving stored message key cci " + currentChainIndex + " pcl " + previousChainLength);
            byte[] messageKey = encryptedKeyStore.removeSkippedMessageKey(peerUserId, ephemeralKeyId, currentChainIndex);
            if (messageKey == null) {
                throw new CryptoException("old_message_key_not_found");
            }
            return messageKey;
        }

        boolean shouldUpdateChains = ephemeralKeyId != latestStoredEphemeralKeyId;
        if (shouldUpdateChains) {
            skipInboundKeys(peerUserId, previousChainLength - latestStoredChainIndex, latestStoredEphemeralKeyId, latestPreviousChainLength, latestStoredChainIndex);

            EncryptedKeyStore.Editor editor = encryptedKeyStore.edit();
            updateInboundChainAndRootKey(editor, peerUserId, encryptedKeyStore.getOutboundEphemeralKey(peerUserId), ephemeralKey);
            editor.setInboundEphemeralKeyId(peerUserId, ephemeralKeyId);
            editor.setInboundEphemeralKey(peerUserId, ephemeralKey);

            editor.setInboundPreviousChainLength(peerUserId, previousChainLength);
            editor.setInboundCurrentChainIndex(peerUserId, 0);
            editor.apply();
        }

        int currentStoredIndex = encryptedKeyStore.getInboundCurrentChainIndex(peerUserId); // NOTE: could have been updated in if statement above
        skipInboundKeys(peerUserId, currentChainIndex - currentStoredIndex, ephemeralKeyId, previousChainLength, currentStoredIndex);

        byte[] messageKey = getNextMessageKey(peerUserId, false);

        encryptedKeyStore.edit().setInboundCurrentChainIndex(peerUserId, currentChainIndex + 1).apply();

        if (shouldUpdateChains) {
            PrivateXECKey newEphemeralKey = XECKey.generatePrivateKey();
            int lastSentId = encryptedKeyStore.getOutboundEphemeralKeyId(peerUserId);

            EncryptedKeyStore.Editor editor = encryptedKeyStore.edit();
            editor.setOutboundEphemeralKeyId(peerUserId, lastSentId + 1);
            editor.setOutboundEphemeralKey(peerUserId, newEphemeralKey);
            updateOutboundChainAndRootKey(editor, peerUserId, newEphemeralKey, ephemeralKey);

            editor.setOutboundPreviousChainLength(peerUserId, encryptedKeyStore.getOutboundCurrentChainIndex(peerUserId));
            editor.setOutboundCurrentChainIndex(peerUserId, 0);
            editor.apply();
        }

        return messageKey;
    }

    private void skipInboundKeys(UserId peerUserId, int count, int ephemeralKeyId, int previousChainLength, int startIndex) throws CryptoException {
        Log.i("skipping " + count + " inbound keys");
        if (count > MAX_SIGNAL_KEYS_SKIP) {
            Log.e("Attempting to skip too many keys");
            throw new CryptoException("skip_too_many_keys");
        }
        for (int i=0; i<count; i++) {
            byte[] inboundMessageKey = getNextInboundMessageKey(peerUserId);
            try {
                SignalMessageKey signalMessageKey = new SignalMessageKey(ephemeralKeyId, previousChainLength, startIndex + i, inboundMessageKey);
                encryptedKeyStore.edit().storeSkippedMessageKey(peerUserId, signalMessageKey).apply();
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
                encryptedKeyStore.edit().setOutboundChainKey(peerUserId, newChainKey).apply();
            } else {
                encryptedKeyStore.edit().setInboundChainKey(peerUserId, newChainKey).apply();
            }

            CryptoByteUtils.nullify(chainKey, newChainKey);

            return messageKey;
        } catch (GeneralSecurityException e) {
            throw new CryptoException((isOutbound ? "outbound" : "inbound") + "_sym_ratchet_failure", e);
        }
    }

    private void updateOutboundChainAndRootKey(EncryptedKeyStore.Editor editor, UserId peerUserId, PrivateXECKey myEphemeral, PublicXECKey peerEphemeral) throws CryptoException {
        updateChainAndRootKey(editor, peerUserId, myEphemeral, peerEphemeral, true);
    }

    private void updateInboundChainAndRootKey(EncryptedKeyStore.Editor editor, UserId peerUserId, PrivateXECKey myEphemeral, PublicXECKey peerEphemeral) throws CryptoException {
        updateChainAndRootKey(editor, peerUserId, myEphemeral, peerEphemeral, false);
    }

    private void updateChainAndRootKey(EncryptedKeyStore.Editor editor, UserId peerUserId, PrivateXECKey myEphemeral, PublicXECKey peerEphemeral, boolean isOutbound) throws CryptoException {
        try {
            byte[] ephemeralSecret = CryptoUtils.ecdh(myEphemeral, peerEphemeral);

            byte[] output = CryptoUtils.hkdf(ephemeralSecret, encryptedKeyStore.getRootKey(peerUserId), HKDF_ROOT_KEY_INFO, 64);
            byte[] rootKey = Arrays.copyOfRange(output, 0, 32);
            byte[] chainKey = Arrays.copyOfRange(output, 32, 64);
            Log.d("root key with " + peerUserId + " updated to hash: " + CryptoByteUtils.obfuscate(rootKey));

            editor.setRootKey(peerUserId, rootKey);
            if (isOutbound) {
                editor.setOutboundChainKey(peerUserId, chainKey);
            } else {
                editor.setInboundChainKey(peerUserId, chainKey);
            }

            CryptoByteUtils.nullify(ephemeralSecret, output, rootKey, chainKey);
        } catch (GeneralSecurityException e) {
            throw new CryptoException((isOutbound ? "outbound" : "inbound") + "_asym_ratchet_failure", e);
        }
    }

}
