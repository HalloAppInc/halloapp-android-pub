package com.halloapp.crypto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.Constants;
import com.halloapp.contacts.UserId;
import com.halloapp.content.Message;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.KeyManager;
import com.halloapp.crypto.keys.OneTimePreKey;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.keys.PublicXECKey;
import com.halloapp.proto.IdentityKey;
import com.halloapp.proto.SignedPreKey;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.WhisperKeysResponseIq;

import java.security.GeneralSecurityException;

public class EncryptedSessionManager {
    private final Connection connection;
    private final KeyManager keyManager;
    private final EncryptedKeyStore encryptedKeyStore;
    private final MessageCipher messageCipher;

    private static EncryptedSessionManager instance = null;

    public static EncryptedSessionManager getInstance() {
        if (instance == null) {
            synchronized (EncryptedSessionManager.class) {
                if (instance == null) {
                    instance = new EncryptedSessionManager(Connection.getInstance(), KeyManager.getInstance(), EncryptedKeyStore.getInstance(), new MessageCipher());
                }
            }
        }
        return instance;
    }

    private EncryptedSessionManager(Connection connection, KeyManager keyManager, EncryptedKeyStore encryptedKeyStore, MessageCipher messageCipher) {
        this.connection = connection;
        this.keyManager = keyManager;
        this.encryptedKeyStore = encryptedKeyStore;
        this.messageCipher = messageCipher;
    }

    public byte[] encryptMessage(byte[] message, UserId peerUserId) throws Exception {
        return messageCipher.convertForWire(message, peerUserId);
    }

    public byte[] decryptMessage(byte[] message, UserId peerUserId, @Nullable SessionSetupInfo sessionSetupInfo) throws Exception {
        if (!encryptedKeyStore.getSessionAlreadySetUp(peerUserId)) {
            if (sessionSetupInfo == null) {
                throw new GeneralSecurityException("Cannot set up session without identity key");
            }
            keyManager.receiveSessionSetup(peerUserId, message, sessionSetupInfo);
        }
        encryptedKeyStore.setSessionAlreadySetUp(peerUserId, true);
        encryptedKeyStore.setPeerResponded(peerUserId, true);

        return messageCipher.convertFromWire(message, peerUserId);
    }

    public void sendMessage(final @NonNull Message message) {
        final UserId recipientUserId = new UserId(message.chatId);
        try {
            SessionSetupInfo sessionSetupInfo = setUpSession(recipientUserId);
            connection.sendMessage(message, sessionSetupInfo);
        } catch (Exception e) {
            Log.e("Failed to set up encryption session", e);
            Log.sendErrorReport("Failed to get session setup info");
            connection.sendMessage(message, null);
        }
    }

    private SessionSetupInfo setUpSession(UserId peerUserId) throws Exception {
        if (!Constants.ENCRYPTION_TURNED_ON || encryptedKeyStore.getPeerResponded(peerUserId)) {
            return null;
        }

        if (!encryptedKeyStore.getSessionAlreadySetUp(peerUserId)) {
            WhisperKeysResponseIq keysIq = connection.downloadKeys(peerUserId).get();
            IdentityKey identityKeyProto = IdentityKey.parseFrom(keysIq.identityKey);
            SignedPreKey signedPreKeyProto = SignedPreKey.parseFrom(keysIq.signedPreKey);

            byte[] identityKeyBytes = identityKeyProto.getPublicKey().toByteArray();
            byte[] signedPreKeyBytes = signedPreKeyProto.getPublicKey().toByteArray();

            if (identityKeyBytes == null || identityKeyBytes.length == 0 || signedPreKeyBytes == null || signedPreKeyBytes.length == 0) {
                Log.i("Did not get any keys for peer " + peerUserId);

                // TODO(jack): Once encryption is turned on for all clients this probably should be removed [stops repeated key requests]
                encryptedKeyStore.setSessionAlreadySetUp(peerUserId, true);

                return null;
            }

            PublicEdECKey peerIdentityKey = new PublicEdECKey(identityKeyBytes);
            PublicXECKey peerSignedPreKey = new PublicXECKey(signedPreKeyBytes);

            byte[] signature = signedPreKeyProto.getSignature().toByteArray();
            CryptoUtil.verify(signature, signedPreKeyBytes, peerIdentityKey);

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
