package com.halloapp.crypto.signal;

import android.text.format.DateUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.content.Message;
import com.halloapp.crypto.AutoCloseLock;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.OneTimePreKey;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.keys.PublicXECKey;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.IdentityKey;
import com.halloapp.proto.clients.SignedPreKey;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Stats;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.WhisperKeysResponseIq;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The public-facing interface for Signal protocol. All production calls to code related to
 * the Signal protocol should be routed through this class.
 */
public class SignalSessionManager {
    private static final long MIN_TIME_BETWEEN_KEY_DOWNLOAD_ATTEMPTS = 5 * DateUtils.SECOND_IN_MILLIS;

    private final Stats stats;
    private final Connection connection;
    private final SignalKeyManager signalKeyManager;
    private final EncryptedKeyStore encryptedKeyStore;
    private final SignalMessageCipher signalMessageCipher;
    private final ConcurrentMap<UserId, AutoCloseLock> lockMap = new ConcurrentHashMap<>();

    private static SignalSessionManager instance = null;

    public static SignalSessionManager getInstance() {
        if (instance == null) {
            synchronized (SignalSessionManager.class) {
                if (instance == null) {
                    instance = new SignalSessionManager(Stats.getInstance(), Connection.getInstance(), SignalKeyManager.getInstance(), EncryptedKeyStore.getInstance(), SignalMessageCipher.getInstance());
                }
            }
        }
        return instance;
    }

    private SignalSessionManager(Stats stats, Connection connection, SignalKeyManager signalKeyManager, EncryptedKeyStore encryptedKeyStore, SignalMessageCipher signalMessageCipher) {
        this.stats = stats;
        this.connection = connection;
        this.signalKeyManager = signalKeyManager;
        this.encryptedKeyStore = encryptedKeyStore;
        this.signalMessageCipher = signalMessageCipher;
    }

    // Should be used in a try-with-resources block for auto-release
    private AutoCloseLock acquireLock(@NonNull UserId userId) throws InterruptedException {
        lockMap.putIfAbsent(userId, new AutoCloseLock());
        return Preconditions.checkNotNull(lockMap.get(userId)).lock();
    }

    public byte[] encryptMessage(@NonNull byte[] message, @NonNull UserId peerUserId) throws CryptoException {
        try (AutoCloseLock autoCloseLock = acquireLock(peerUserId)) {
            return signalMessageCipher.convertForWire(message, peerUserId);
        } catch (InterruptedException e) {
            throw new CryptoException("enc_interrupted", e);
        }
    }

    public byte[] decryptMessage(@NonNull byte[] message, @NonNull UserId peerUserId, @Nullable SignalSessionSetupInfo signalSessionSetupInfo) throws CryptoException {
        try (AutoCloseLock autoCloseLock = acquireLock(peerUserId)) {
            try {
                if (message.length < SignalMessageCipher.MINIMUM_MESSAGE_SIZE_BYTES) {
                    Log.e("Input message bytes too short");
                    throw new CryptoException("ciphertext_too_short");
                }
                byte[] ephemeralKeyBytes = Arrays.copyOfRange(message, 0, 32);
                if (Arrays.equals(encryptedKeyStore.getOutboundTeardownKey(peerUserId), ephemeralKeyBytes)) {
                    throw new CryptoException("matching_teardown_key", true, ephemeralKeyBytes);
                }
                if (!encryptedKeyStore.getSessionAlreadySetUp(peerUserId)) {
                    if (signalSessionSetupInfo == null || signalSessionSetupInfo.identityKey == null) {
                        encryptedKeyStore.edit().setOutboundTeardownKey(peerUserId, ephemeralKeyBytes).apply();
                        throw new CryptoException("no_identity_key");
                    }
                    signalKeyManager.receiveSessionSetup(peerUserId, message, signalSessionSetupInfo);
                    encryptedKeyStore.edit().setPeerResponded(peerUserId, true).apply();
                } else if (signalSessionSetupInfo != null && signalSessionSetupInfo.identityKey != null) {
                    PublicEdECKey peerIdentityKey = encryptedKeyStore.getPeerPublicIdentityKey(peerUserId);
                    if (!Arrays.equals(peerIdentityKey.getKeyMaterial(), signalSessionSetupInfo.identityKey.getKeyMaterial())) {
                        Log.w("Session already set up but received session setup info with new identity key;"
                                + " stored: " + Base64.encodeToString(peerIdentityKey.getKeyMaterial(), Base64.NO_WRAP)
                                + " received: " + Base64.encodeToString(signalSessionSetupInfo.identityKey.getKeyMaterial(), Base64.NO_WRAP));
                    }
                    encryptedKeyStore.edit().setPeerResponded(peerUserId, true).apply();
                }
                encryptedKeyStore.edit().setSessionAlreadySetUp(peerUserId, true).apply();

                return signalMessageCipher.convertFromWire(message, peerUserId);
            } catch (CryptoException e) {
                if (e.teardownKeyMatched) {
                    Log.i("Teardown key matched; skipping session reset");
                } else {
                    Log.i("Resetting session because teardown key did not match", e);
                    signalKeyManager.tearDownSession(peerUserId);
                    setUpSession(peerUserId, true);
                }
                throw e;
            }
        } catch (InterruptedException e) {
            throw new CryptoException("dec_interrupted", e);
        }
    }

    public void sendRerequest(final @NonNull UserId senderUserId, final @NonNull String messageId, boolean isReaction, int rerequestCount, @Nullable byte[] teardownKey) {
        connection.sendRerequest(senderUserId, messageId, isReaction, rerequestCount, teardownKey);
    }

    public void sendMessage(final @NonNull Message message) {
        if (message.chatId instanceof GroupId) {
            connection.sendGroupMessage(message);
            return;
        }

        final UserId recipientUserId = (UserId)message.chatId;
        try (AutoCloseLock autoCloseLock = acquireLock(recipientUserId)) {
            SignalSessionSetupInfo signalSessionSetupInfo = setUpSession(recipientUserId, false);
            connection.sendMessage(message, signalSessionSetupInfo);
        } catch (Exception e) {
            Log.e("Failed to set up encryption session", e);
            Log.sendErrorReport("Failed to get session setup info");
        }
    }

    public SignalSessionSetupInfo getSessionSetupInfo(final @NonNull UserId peerUserId) throws Exception {
        try (AutoCloseLock autoCloseLock = acquireLock(peerUserId)) {
            return setUpSession(peerUserId, false);
        } catch (Exception e) {
            Log.e("Failed to set up encryption session", e);
            Log.sendErrorReport("Failed to get session setup info");
            throw e;
        }
    }

    public void tearDownSession(final @NonNull UserId peerUserId) {
        try (AutoCloseLock autoCloseLock = acquireLock(peerUserId)) {
            signalKeyManager.tearDownSession(peerUserId);
        } catch (InterruptedException e) {
            Log.e("Interrupted trying to tear down session", e);
        }
    }

    public List<byte[]> getFreshOneTimePreKeyProtos() {
        Set<OneTimePreKey> keys = encryptedKeyStore.getNewBatchOfOneTimePreKeys();
        List<byte[]> protoKeys = new ArrayList<>();
        for (OneTimePreKey otpk : keys) {
            com.halloapp.proto.clients.OneTimePreKey protoKey = com.halloapp.proto.clients.OneTimePreKey.newBuilder()
                    .setId(otpk.id)
                    .setPublicKey(ByteString.copyFrom(otpk.publicXECKey.getKeyMaterial()))
                    .build();
            protoKeys.add(protoKey.toByteArray());
        }
        return protoKeys;
    }

    public void receiveRerequestSetup(UserId peerUserId, PublicXECKey publicEphemeralKey, int ephemeralKeyId, @NonNull SignalSessionSetupInfo signalSessionSetupInfo) throws CryptoException {
        try (AutoCloseLock autoCloseLock = acquireLock(peerUserId)) {
            signalKeyManager.receiveSessionSetup(peerUserId, publicEphemeralKey, ephemeralKeyId, signalSessionSetupInfo);
        } catch (InterruptedException e) {
            throw new CryptoException("recv_setup_interrupted", e);
        }
    }

    private SignalSessionSetupInfo setUpSession(UserId peerUserId, boolean isReset) throws CryptoException {
        boolean missingOutboundKeyId = encryptedKeyStore.getOutboundEphemeralKeyId(peerUserId) == -1;

        if (!missingOutboundKeyId && encryptedKeyStore.getPeerResponded(peerUserId)) {
            Log.d("EncryptedSessionManager.setUpSession: peer already responded!");
            return null;
        }

        if (missingOutboundKeyId || !encryptedKeyStore.getSessionAlreadySetUp(peerUserId)) {
            // TODO: Reconsider once encryption is fully deployed
            long now = System.currentTimeMillis();
            if (now - encryptedKeyStore.getLastDownloadAttempt(peerUserId) < MIN_TIME_BETWEEN_KEY_DOWNLOAD_ATTEMPTS) {
                Log.i("EncryptedSessionManager.setUpSession: last download attempt too recent for " + peerUserId);
                throw new CryptoException("last_key_dl_too_recent");
            }

            try {
                WhisperKeysResponseIq keysIq = connection.downloadKeys(peerUserId).await();
                encryptedKeyStore.edit().setLastDownloadAttempt(peerUserId, now).apply();
                if (keysIq == null || keysIq.identityKey == null || keysIq.signedPreKey == null) {
                    Log.i("EncryptedSessionManager.setUpSession: no whisper keys returned");
                    throw new CryptoException("no_whisper_keys_returned");
                }
                IdentityKey identityKeyProto = IdentityKey.parseFrom(keysIq.identityKey);
                SignedPreKey signedPreKeyProto = SignedPreKey.parseFrom(keysIq.signedPreKey);

                byte[] identityKeyBytes = identityKeyProto.getPublicKey().toByteArray();
                byte[] signedPreKeyBytes = signedPreKeyProto.getPublicKey().toByteArray();

                if (identityKeyBytes == null || identityKeyBytes.length == 0 || signedPreKeyBytes == null || signedPreKeyBytes.length == 0) {
                    Log.i("EncryptedSessionManager.setUpSession: Did not get any keys for peer " + peerUserId);
                    throw new CryptoException("empty_whisper_keys");
                }

                PublicEdECKey peerIdentityKey = new PublicEdECKey(identityKeyBytes);
                PublicXECKey peerSignedPreKey = new PublicXECKey(signedPreKeyBytes);

                byte[] signature = signedPreKeyProto.getSignature().toByteArray();
                CryptoUtils.verify(signature, signedPreKeyBytes, peerIdentityKey);

                OneTimePreKey oneTimePreKey = null;
                if (keysIq.oneTimePreKeys != null && !keysIq.oneTimePreKeys.isEmpty()) {
                    com.halloapp.proto.clients.OneTimePreKey otpk = com.halloapp.proto.clients.OneTimePreKey.parseFrom(keysIq.oneTimePreKeys.get(0));
                    byte[] bytes = otpk.getPublicKey().toByteArray();
                    if (bytes != null && bytes.length > 0) {
                        oneTimePreKey = new OneTimePreKey(new PublicXECKey(bytes), otpk.getId());
                    }
                }

                signalKeyManager.setUpSession(peerUserId, peerIdentityKey, peerSignedPreKey, oneTimePreKey);

                stats.reportSignalSessionEstablished(isReset);
            } catch (InvalidProtocolBufferException e) {
                throw new CryptoException("invalid_protobuf", e);
            } catch (InterruptedException | ObservableErrorException e) {
                throw new CryptoException("execution_interrupted", e);
            } catch (GeneralSecurityException e) {
                throw new CryptoException("spk_sig_mismatch", e);
            }
        } else {
            Log.i("EncryptedSessionManager.setUpSession: Session has already been set up!");
        }

        return new SignalSessionSetupInfo(
                encryptedKeyStore.getMyPublicEd25519IdentityKey(),
                encryptedKeyStore.getPeerOneTimePreKeyId(peerUserId)
        );
    }
}
