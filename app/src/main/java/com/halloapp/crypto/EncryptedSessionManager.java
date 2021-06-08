package com.halloapp.crypto;

import android.text.format.DateUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Message;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.KeyManager;
import com.halloapp.crypto.keys.OneTimePreKey;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.keys.PublicXECKey;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.IdentityKey;
import com.halloapp.proto.clients.SignedPreKey;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ConnectionImpl;
import com.halloapp.xmpp.WhisperKeysResponseIq;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The public-facing interface for Signal protocol. All production calls to code related to
 * the Signal protocol should be routed through this class.
 */
public class EncryptedSessionManager {
    private static final long MIN_TIME_BETWEEN_KEY_DOWNLOAD_ATTEMPTS = 5 * DateUtils.SECOND_IN_MILLIS;

    private final Connection connection;
    private final KeyManager keyManager;
    private final EncryptedKeyStore encryptedKeyStore;
    private final MessageCipher messageCipher;
    private final ConcurrentMap<UserId, AutoCloseLock> lockMap = new ConcurrentHashMap<>();

    private static EncryptedSessionManager instance = null;

    public static EncryptedSessionManager getInstance() {
        if (instance == null) {
            synchronized (EncryptedSessionManager.class) {
                if (instance == null) {
                    instance = new EncryptedSessionManager(Connection.getInstance(), KeyManager.getInstance(), EncryptedKeyStore.getInstance());
                }
            }
        }
        return instance;
    }

    private EncryptedSessionManager(Connection connection, KeyManager keyManager, EncryptedKeyStore encryptedKeyStore) {
        this.connection = connection;
        this.keyManager = keyManager;
        this.encryptedKeyStore = encryptedKeyStore;

        this.messageCipher = new MessageCipher(keyManager, encryptedKeyStore);
    }

    // Should be used in a try-with-resources block for auto-release
    private AutoCloseLock acquireLock(@NonNull UserId userId) throws InterruptedException {
        lockMap.putIfAbsent(userId, new AutoCloseLock());
        return Preconditions.checkNotNull(lockMap.get(userId)).lock();
    }

    public byte[] encryptMessage(@NonNull byte[] message, @NonNull UserId peerUserId) throws CryptoException {
        try (AutoCloseLock autoCloseLock = acquireLock(peerUserId)) {
            return messageCipher.convertForWire(message, peerUserId);
        } catch (InterruptedException e) {
            throw new CryptoException("enc_interrupted", e);
        }
    }

    public byte[] decryptMessage(@NonNull byte[] message, @NonNull UserId peerUserId, @Nullable SessionSetupInfo sessionSetupInfo) throws CryptoException {
        try (AutoCloseLock autoCloseLock = acquireLock(peerUserId)) {
            if (!encryptedKeyStore.getSessionAlreadySetUp(peerUserId)) {
                if (sessionSetupInfo == null || sessionSetupInfo.identityKey == null) {
                    throw new CryptoException("no_identity_key");
                }
                keyManager.receiveSessionSetup(peerUserId, message, sessionSetupInfo);
                encryptedKeyStore.setPeerResponded(peerUserId, true);
            } else if (sessionSetupInfo != null && sessionSetupInfo.identityKey != null) {
                PublicEdECKey peerIdentityKey = encryptedKeyStore.getPeerPublicIdentityKey(peerUserId);
                if (!Arrays.equals(peerIdentityKey.getKeyMaterial(), sessionSetupInfo.identityKey.getKeyMaterial())) {
                    Log.w("Session already set up but received session setup info with new identity key;"
                            + " stored: " + Base64.encodeToString(peerIdentityKey.getKeyMaterial(), Base64.NO_WRAP)
                            + " received: " + Base64.encodeToString(sessionSetupInfo.identityKey.getKeyMaterial(), Base64.NO_WRAP));
                }
                encryptedKeyStore.setPeerResponded(peerUserId, true);
            }
            encryptedKeyStore.setSessionAlreadySetUp(peerUserId, true);

            return messageCipher.convertFromWire(message, peerUserId);
        } catch (CryptoException e) {
            if (e.teardownKeyMatched) {
                Log.i("Teardown key matched; skipping session reset");
            } else {
                Log.i("Resetting session because teardown key did not match");
                keyManager.tearDownSession(peerUserId);
                setUpSession(peerUserId);
            }
            throw e;
        } catch (InterruptedException e) {
            throw new CryptoException("dec_interrupted", e);
        }
    }

    public void sendRerequest(final @NonNull UserId senderUserId, final @NonNull String messageId, int rerequestCount, @Nullable byte[] teardownKey) {
        connection.sendRerequest(senderUserId, messageId, rerequestCount, teardownKey);
    }

    public void sendMessage(final @NonNull Message message) {
        if (message.chatId instanceof GroupId) {
            // TODO(jack): support groups encryption
            connection.sendGroupMessage(message, null);
            return;
        }

        final UserId recipientUserId = (UserId)message.chatId;
        try (AutoCloseLock autoCloseLock = acquireLock(recipientUserId)) {
            SessionSetupInfo sessionSetupInfo = setUpSession(recipientUserId);
            connection.sendMessage(message, sessionSetupInfo);
        } catch (Exception e) {
            Log.e("Failed to set up encryption session", e);
            Log.sendErrorReport("Failed to get session setup info");
        }
    }

    public void tearDownSession(final @NonNull UserId peerUserId) {
        try (AutoCloseLock autoCloseLock = acquireLock(peerUserId)) {
            keyManager.tearDownSession(peerUserId);
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

    private SessionSetupInfo setUpSession(UserId peerUserId) throws CryptoException {
        boolean missingOutboundKeyId = encryptedKeyStore.getOutboundEphemeralKeyId(peerUserId) == -1;

        if (!missingOutboundKeyId && encryptedKeyStore.getPeerResponded(peerUserId)) {
            Log.d("EncryptedSessionManager.setUpSession: peer already responded!");
            return null;
        }

        if (missingOutboundKeyId || !encryptedKeyStore.getSessionAlreadySetUp(peerUserId)) {
            // TODO(jack): Reconsider once encryption is fully deployed
            long now = System.currentTimeMillis();
            if (now - encryptedKeyStore.getLastDownloadAttempt(peerUserId) < MIN_TIME_BETWEEN_KEY_DOWNLOAD_ATTEMPTS) {
                Log.i("EncryptedSessionManager.setUpSession: last download attempt too recent for " + peerUserId);
                throw new CryptoException("last_key_dl_too_recent");
            }

            try {
                WhisperKeysResponseIq keysIq = connection.downloadKeys(peerUserId).await();
                encryptedKeyStore.setLastDownloadAttempt(peerUserId, now);
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

                keyManager.setUpSession(peerUserId, peerIdentityKey, peerSignedPreKey, oneTimePreKey);
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

        return new SessionSetupInfo(
                encryptedKeyStore.getMyPublicEd25519IdentityKey(),
                encryptedKeyStore.getPeerOneTimePreKeyId(peerUserId)
        );
    }
}
