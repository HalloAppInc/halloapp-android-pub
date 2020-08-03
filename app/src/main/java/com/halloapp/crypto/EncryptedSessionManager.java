package com.halloapp.crypto;

import android.content.Context;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.Constants;
import com.halloapp.id.UserId;
import com.halloapp.content.Message;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.KeyManager;
import com.halloapp.crypto.keys.OneTimePreKey;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.keys.PublicXECKey;
import com.halloapp.proto.IdentityKey;
import com.halloapp.proto.SignedPreKey;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.WhisperKeysResponseIq;

import org.jxmpp.jid.Jid;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

/**
 * The public-facing interface for Signal protocol. All production calls to code related to
 * the Signal protocol should be routed through this class.
 */
public class EncryptedSessionManager {
    private static final long MIN_TIME_BETWEEN_KEY_DOWNLOAD_ATTEMPTS = 60 * 60 * 1000; // one hour

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

    public void init(Context context) {
        encryptedKeyStore.init(context);
    }

    // Should be used in a try-with-resources block for auto-release
    private AutoCloseLock acquireLock(UserId userId) throws InterruptedException {
        lockMap.putIfAbsent(userId, new AutoCloseLock());
        return Preconditions.checkNotNull(lockMap.get(userId)).lock();
    }

    public byte[] encryptMessage(byte[] message, UserId peerUserId) throws GeneralSecurityException {
        try (AutoCloseLock autoCloseLock = acquireLock(peerUserId)) {
            return messageCipher.convertForWire(message, peerUserId);
        } catch (InterruptedException e) {
            throw new GeneralSecurityException("Interrupted during encryption", e);
        }
    }

    public byte[] decryptMessage(byte[] message, UserId peerUserId, @Nullable SessionSetupInfo sessionSetupInfo) throws GeneralSecurityException {
        try (AutoCloseLock autoCloseLock = acquireLock(peerUserId)) {
            if (!encryptedKeyStore.getSessionAlreadySetUp(peerUserId)) {
                if (sessionSetupInfo == null || sessionSetupInfo.identityKey == null) {
                    throw new GeneralSecurityException("Cannot set up session without identity key");
                }
                keyManager.receiveSessionSetup(peerUserId, message, sessionSetupInfo);
            }
            encryptedKeyStore.setSessionAlreadySetUp(peerUserId, true);
            encryptedKeyStore.setPeerResponded(peerUserId, true);

            return messageCipher.convertFromWire(message, peerUserId);
        } catch (InterruptedException e) {
            throw new GeneralSecurityException("Interrupted during decryption", e);
        }
    }

    public void sendRerequest(final @NonNull Jid originalSender, final @NonNull String messageId) {
        String encodedIdentityKey = Base64.encodeToString(getPublicIdentityKey().getKeyMaterial(), Base64.NO_WRAP);
        connection.sendRerequest(encodedIdentityKey, originalSender, messageId);
    }

    public void sendMessage(final @NonNull Message message) {
        if (!(message.chatId instanceof UserId)) {
            // TODO(jack): support groups
            throw new IllegalArgumentException("Encryption only supported for 1-1 chats");
        }
        final UserId recipientUserId = (UserId)message.chatId;
        try (AutoCloseLock autoCloseLock = acquireLock(recipientUserId)) {
            SessionSetupInfo sessionSetupInfo = setUpSession(recipientUserId);
            connection.sendMessage(message, sessionSetupInfo);
        } catch (Exception e) {
            Log.e("Failed to set up encryption session", e);
            Log.sendErrorReport("Failed to get session setup info");
            connection.sendMessage(message, null);
        }
    }

    public void tearDownSession(final @NonNull UserId peerUserId) {
        try (AutoCloseLock autoCloseLock = acquireLock(peerUserId)) {
            keyManager.tearDownSession(peerUserId);
        } catch (InterruptedException e) {
            Log.e("Interrupted trying to tear down session", e);
        }
    }

    public void ensureKeysUploaded() {
        keyManager.ensureKeysUploaded(connection);
    }

    public List<byte[]> getFreshOneTimePreKeyProtos() {
        Set<OneTimePreKey> keys = encryptedKeyStore.getNewBatchOfOneTimePreKeys();
        List<byte[]> protoKeys = new ArrayList<>();
        for (OneTimePreKey otpk : keys) {
            com.halloapp.proto.OneTimePreKey protoKey = com.halloapp.proto.OneTimePreKey.newBuilder()
                    .setId(otpk.id)
                    .setPublicKey(ByteString.copyFrom(otpk.publicXECKey.getKeyMaterial()))
                    .build();
            protoKeys.add(protoKey.toByteArray());
        }
        return protoKeys;
    }

    public PublicEdECKey getPublicIdentityKey() {
        return encryptedKeyStore.getMyPublicEd25519IdentityKey();
    }

    private SessionSetupInfo setUpSession(UserId peerUserId) throws GeneralSecurityException, InvalidProtocolBufferException, ExecutionException, InterruptedException {
        if (!Constants.ENCRYPTION_TURNED_ON || encryptedKeyStore.getPeerResponded(peerUserId)) {
            return null;
        }

        if (!encryptedKeyStore.getSessionAlreadySetUp(peerUserId)) {
            // TODO(jack): Reconsider once encryption is fully deployed
            long now = System.currentTimeMillis();
            if (now - encryptedKeyStore.getLastDownloadAttempt(peerUserId) < MIN_TIME_BETWEEN_KEY_DOWNLOAD_ATTEMPTS) {
                Log.i("EncryptedSessionManager last download attempt too recent for " + peerUserId);
                return null;
            }
            encryptedKeyStore.setLastDownloadAttempt(peerUserId, now);

            WhisperKeysResponseIq keysIq = connection.downloadKeys(peerUserId).get();
            if (keysIq == null || keysIq.identityKey == null || keysIq.signedPreKey == null) {
                Log.i("EncryptedSessionManager no whisper keys returned");
                return null;
            }

            IdentityKey identityKeyProto = IdentityKey.parseFrom(keysIq.identityKey);
            SignedPreKey signedPreKeyProto = SignedPreKey.parseFrom(keysIq.signedPreKey);

            byte[] identityKeyBytes = identityKeyProto.getPublicKey().toByteArray();
            byte[] signedPreKeyBytes = signedPreKeyProto.getPublicKey().toByteArray();

            if (identityKeyBytes == null || identityKeyBytes.length == 0 || signedPreKeyBytes == null || signedPreKeyBytes.length == 0) {
                Log.i("Did not get any keys for peer " + peerUserId);

                return null;
            }

            PublicEdECKey peerIdentityKey = new PublicEdECKey(identityKeyBytes);
            PublicXECKey peerSignedPreKey = new PublicXECKey(signedPreKeyBytes);

            byte[] signature = signedPreKeyProto.getSignature().toByteArray();
            CryptoUtils.verify(signature, signedPreKeyBytes, peerIdentityKey);

            OneTimePreKey oneTimePreKey = null;
            if (keysIq.oneTimePreKeys != null && !keysIq.oneTimePreKeys.isEmpty()) {
                com.halloapp.proto.OneTimePreKey otpk = com.halloapp.proto.OneTimePreKey.parseFrom(keysIq.oneTimePreKeys.get(0));
                byte[] bytes = otpk.getPublicKey().toByteArray();
                if (bytes != null && bytes.length > 0) {
                    oneTimePreKey = new OneTimePreKey(new PublicXECKey(bytes), otpk.getId());
                }
            }

            keyManager.setUpSession(peerUserId, peerIdentityKey, peerSignedPreKey, oneTimePreKey);
            encryptedKeyStore.setSessionAlreadySetUp(peerUserId, true);
        }

        return new SessionSetupInfo(
                encryptedKeyStore.getMyPublicEd25519IdentityKey(),
                encryptedKeyStore.getPeerOneTimePreKeyId(peerUserId)
        );
    }
}
