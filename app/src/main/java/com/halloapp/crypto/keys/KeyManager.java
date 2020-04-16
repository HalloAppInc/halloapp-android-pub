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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class KeyManager {

    private static KeyManager instance;

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

            IdentityKey identityKey = IdentityKey.newBuilder()
                    .setPublicKey(ByteString.copyFrom(encryptedKeyStore.getMyPublicIdentityKey().getKeyMaterial()))
                    .build();
            SignedPreKey signedPreKey = SignedPreKey.newBuilder()
                    .setPublicKey(ByteString.copyFrom(encryptedKeyStore.getMyPublicSignedPreKey().getKeyMaterial()))
                    // TODO(jack): ID, signature
                    .build();
            List<byte[]> oneTimePreKeys = new ArrayList<>();
            for (OneTimePreKey otpk : encryptedKeyStore.getNewBatchOfOneTimePreKeys()) {
                com.halloapp.proto.OneTimePreKey toAdd = com.halloapp.proto.OneTimePreKey.newBuilder()
                        .setId(otpk.id)
                        .setPublicKey(ByteString.copyFrom(otpk.publicECKey.getKeyMaterial()))
                        .build();
                oneTimePreKeys.add(toAdd.toByteArray());
            }

            // TODO(jack): Check for success
            connection.uploadKeys(identityKey.toByteArray(), signedPreKey.toByteArray(), oneTimePreKeys);

            encryptedKeyStore.setKeysUploaded(true);
        } else {
            Log.i("Keys were already uploaded");
        }
    }

    public void setUpSession(UserId peerUserId, PublicECKey recipientPublicIdentityKey, PublicECKey recipientPublicSignedPreKey, @Nullable OneTimePreKey recipientPublicOneTimePreKey) throws Exception {
        encryptedKeyStore.setPeerPublicIdentityKey(peerUserId, recipientPublicIdentityKey);
        encryptedKeyStore.setPeerSignedPreKey(peerUserId, recipientPublicSignedPreKey);
        if (recipientPublicOneTimePreKey != null) {
            encryptedKeyStore.setPeerOneTimePreKey(peerUserId, recipientPublicOneTimePreKey.publicECKey);
            encryptedKeyStore.setPeerOneTimePreKeyId(peerUserId, recipientPublicOneTimePreKey.id);
        }

        PrivateECKey privateEphemeralKey = ECKey.generatePrivateKey();
        PrivateECKey myPrivateIdentityKey = encryptedKeyStore.getMyPrivateIdentityKey();

        // TODO(jack): Null out all byte arrays once no longer needed
        byte[] a = CryptoUtil.ecdh(myPrivateIdentityKey, recipientPublicSignedPreKey);
        byte[] b = CryptoUtil.ecdh(privateEphemeralKey, recipientPublicIdentityKey);
        byte[] c = CryptoUtil.ecdh(privateEphemeralKey, recipientPublicSignedPreKey);

        byte[] masterSecret;
        if (recipientPublicOneTimePreKey != null) {
            byte[] d = CryptoUtil.ecdh(privateEphemeralKey, recipientPublicOneTimePreKey.publicECKey);
            masterSecret = CryptoUtil.concat(a, b, c, d);
        } else {
            masterSecret = CryptoUtil.concat(a, b, c);
        }

        byte[] output = CryptoUtil.hkdf(masterSecret, null, 96);
        byte[] rootKey = Arrays.copyOfRange(output, 0, 32);
        byte[] outboundChainKey = Arrays.copyOfRange(output, 32, 64);
        byte[] inboundChainKey = Arrays.copyOfRange(output, 64, 96);

        int firstId = 1;

        encryptedKeyStore.setRootKey(peerUserId, rootKey);
        encryptedKeyStore.setOutboundChainKey(peerUserId, outboundChainKey);
        encryptedKeyStore.setInboundChainKey(peerUserId, inboundChainKey);
        encryptedKeyStore.setLastSentEphemeralKey(peerUserId, privateEphemeralKey);
        encryptedKeyStore.setLastSentEphemeralKeyId(peerUserId, firstId);
    }

    public void receiveSessionSetup(UserId peerUserId, PublicECKey publicEphemeralKey, int ephemeralKeyId, PublicECKey initiatorPublicIdentityKey, @Nullable Integer oneTimePreKeyId) throws Exception {
        encryptedKeyStore.setPeerPublicIdentityKey(peerUserId, initiatorPublicIdentityKey);

        byte[] a = CryptoUtil.ecdh(encryptedKeyStore.getMyPrivateSignedPreKey(), initiatorPublicIdentityKey);
        byte[] b = CryptoUtil.ecdh(encryptedKeyStore.getMyPrivateIdentityKey(), publicEphemeralKey);
        byte[] c = CryptoUtil.ecdh(encryptedKeyStore.getMyPrivateSignedPreKey(), publicEphemeralKey);

        byte[] masterSecret;
        if (oneTimePreKeyId != null) {
            byte[] d = CryptoUtil.ecdh(encryptedKeyStore.removeOneTimePreKeyById(oneTimePreKeyId), publicEphemeralKey);
            masterSecret = CryptoUtil.concat(a, b, c, d);
        } else {
            masterSecret = CryptoUtil.concat(a, b, c);
        }

        byte[] output = CryptoUtil.hkdf(masterSecret, null, 96);
        byte[] rootKey = Arrays.copyOfRange(output, 0, 32);

        // NOTE: Order switched so that keys match appropriately
        byte[] inboundChainKey = Arrays.copyOfRange(output, 32, 64);
        byte[] outboundChainKey = Arrays.copyOfRange(output, 64, 96);

        encryptedKeyStore.setRootKey(peerUserId, rootKey);
        encryptedKeyStore.setOutboundChainKey(peerUserId, outboundChainKey);
        encryptedKeyStore.setInboundChainKey(peerUserId, inboundChainKey);
        encryptedKeyStore.setLastReceivedEphemeralKey(peerUserId, publicEphemeralKey);
        encryptedKeyStore.setLastReceivedEphemeralKeyId(peerUserId, ephemeralKeyId);

        PrivateECKey myEphemeralKey = ECKey.generatePrivateKey();
        encryptedKeyStore.setLastSentEphemeralKey(peerUserId, myEphemeralKey);
        encryptedKeyStore.setLastSentEphemeralKeyId(peerUserId, 1);

        updateOutboundChainAndRootKey(peerUserId, myEphemeralKey, publicEphemeralKey);
    }

    public byte[] getNextOutboundMessageKey(UserId peerUserId) throws Exception {
        return getNextMessageKey(peerUserId, true);
    }

    public byte[] getNextInboundMessageKey(UserId peerUserId) throws Exception {
        return getNextMessageKey(peerUserId, false);
    }

    private byte[] getNextMessageKey(UserId peerUserId, boolean isOutbound) throws Exception {
        byte[] chainKey = isOutbound ? encryptedKeyStore.getOutboundChainKey(peerUserId) : encryptedKeyStore.getInboundChainKey(peerUserId);

        byte[] messageKey = CryptoUtil.hkdf(chainKey, new byte[]{1}, 80);
        byte[] newChainKey = CryptoUtil.hkdf(chainKey, new byte[]{2}, 32);

        if (isOutbound) {
            encryptedKeyStore.setOutboundChainKey(peerUserId, newChainKey);
        } else {
            encryptedKeyStore.setInboundChainKey(peerUserId, newChainKey);
        }

        return messageKey;
    }

    public void updateOutboundChainAndRootKey(UserId peerUserId, PrivateECKey myEphemeral, PublicECKey peerEphemeral) throws Exception {
        updateChainAndRootKey(peerUserId, myEphemeral, peerEphemeral, true);
    }

    public void updateInboundChainAndRootKey(UserId peerUserId, PrivateECKey myEphemeral, PublicECKey peerEphemeral) throws Exception {
        updateChainAndRootKey(peerUserId, myEphemeral, peerEphemeral, false);
    }

    private void updateChainAndRootKey(UserId peerUserId, PrivateECKey myEphemeral, PublicECKey peerEphemeral, boolean isOutbound) throws Exception {
        byte[] ephemeralSecret = CryptoUtil.ecdh(myEphemeral, peerEphemeral);

        byte[] output = CryptoUtil.hkdf(encryptedKeyStore.getRootKey(peerUserId), ephemeralSecret, 64);
        byte[] rootKey = Arrays.copyOfRange(output, 0, 32);
        byte[] chainKey = Arrays.copyOfRange(output, 32, 64);

        encryptedKeyStore.setRootKey(peerUserId, rootKey);
        if (isOutbound) {
            encryptedKeyStore.setOutboundChainKey(peerUserId, chainKey);
        } else {
            encryptedKeyStore.setInboundChainKey(peerUserId, chainKey);
        }
    }

}
