package com.halloapp.crypto;

import androidx.annotation.NonNull;

import com.halloapp.Constants;
import com.halloapp.contacts.UserId;
import com.halloapp.content.Message;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.keys.PublicXECKey;
import com.halloapp.crypto.keys.XECKey;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.KeyManager;
import com.halloapp.crypto.keys.OneTimePreKey;
import com.halloapp.proto.IdentityKey;
import com.halloapp.proto.SignedPreKey;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.WhisperKeysResponseIq;

public class SessionManager {
    private final Connection connection;
    private final KeyManager keyManager;
    private final EncryptedKeyStore encryptedKeyStore;
    private final MessageHandler messageHandler;

    private static final SessionSetupInfo nullInfo = new SessionSetupInfo(null, null, null, null);

    private static SessionManager instance = null;

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager(Connection.getInstance(), KeyManager.getInstance(), EncryptedKeyStore.getInstance(), new MessageHandler());
                }
            }
        }
        return instance;
    }

    public SessionManager(Connection connection, KeyManager keyManager, EncryptedKeyStore encryptedKeyStore, MessageHandler messageHandler) {
        this.connection = connection;
        this.keyManager = keyManager;
        this.encryptedKeyStore = encryptedKeyStore;
        this.messageHandler = messageHandler;
    }

    public byte[] encryptMessage(byte[] message, UserId peerUserId) throws Exception {
        return messageHandler.convertForWire(message, peerUserId);
    }

    public byte[] decryptMessage(byte[] message, UserId peerUserId, PublicEdECKey identityKey, PublicXECKey ephemeralKey, Integer ephemeralKeyId, Integer oneTimePreKeyId) throws Exception {
        if (!encryptedKeyStore.getSessionAlreadySetUp(peerUserId)) {
            byte[] ret = messageHandler.receiveFirstMessage(message, peerUserId, identityKey, ephemeralKey, ephemeralKeyId, oneTimePreKeyId);
            encryptedKeyStore.setSessionAlreadySetUp(peerUserId, true);
            return ret;
        }
        encryptedKeyStore.setPeerResponded(peerUserId, true);

        return messageHandler.convertFromWire(message, peerUserId, ephemeralKey, ephemeralKeyId);
    }

    public void sendMessage(final @NonNull Message message) {
        final UserId recipientUserId = new UserId(message.chatId);
        final SessionSetupInfo sessionSetupInfo;
        try {
            sessionSetupInfo = SessionManager.getInstance().setUpSession(recipientUserId);
        } catch (Exception e) {
            Log.e("Failed to set up encryption session", e);
            return;
        }
        connection.sendMessage(message, sessionSetupInfo);
    }

    public SessionSetupInfo setUpSession(UserId peerUserId) throws Exception {
        if (!Constants.ENCRYPTION_TURNED_ON) {
            return nullInfo;
        }

        if (encryptedKeyStore.getPeerResponded(peerUserId)) {
            return new SessionSetupInfo(
                    XECKey.publicFromPrivate(encryptedKeyStore.getOutboundEphemeralKey(peerUserId)),
                    encryptedKeyStore.getOutboundEphemeralKeyId(peerUserId),
                    null,
                    null
            );
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

                return nullInfo;
            }

            PublicEdECKey peerIdentityKey = new PublicEdECKey(identityKeyBytes);
            PublicXECKey peerSignedPreKey = new PublicXECKey(signedPreKeyBytes);

            // TODO(jack): Log signature verification failures
            byte[] signature = signedPreKeyProto.getSignature().toByteArray();
            SodiumWrapper.getInstance().verify(signature, signedPreKeyBytes, peerIdentityKey);

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
                XECKey.publicFromPrivate(encryptedKeyStore.getOutboundEphemeralKey(peerUserId)),
                encryptedKeyStore.getOutboundEphemeralKeyId(peerUserId),
                encryptedKeyStore.getMyPublicEd25519IdentityKey(),
                encryptedKeyStore.getPeerOneTimePreKeyId(peerUserId)
        );
    }
}
