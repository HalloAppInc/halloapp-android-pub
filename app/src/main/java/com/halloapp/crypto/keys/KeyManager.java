package com.halloapp.crypto.keys;

import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.halloapp.Constants;
import com.halloapp.contacts.UserId;
import com.halloapp.crypto.CryptoUtil;
import com.halloapp.crypto.SodiumWrapper;
import com.halloapp.proto.IdentityKey;
import com.halloapp.proto.SignedPreKey;
import com.halloapp.util.Log;
import com.halloapp.util.StringUtils;
import com.halloapp.xmpp.Connection;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

            IdentityKey identityKeyProto = IdentityKey.newBuilder()
                    .setPublicKey(ByteString.copyFrom(encryptedKeyStore.getMyPublicEd25519IdentityKey()))
                    .build();

            PublicECKey signedPreKey = encryptedKeyStore.getMyPublicSignedPreKey();
            byte[] signature = SodiumWrapper.getInstance().sign(signedPreKey.getKeyMaterial(), encryptedKeyStore.getMyPrivateEd25519IdentityKey());

            SignedPreKey signedPreKeyProto = SignedPreKey.newBuilder()
                    .setPublicKey(ByteString.copyFrom(signedPreKey.getKeyMaterial()))
                    .setSignature(ByteString.copyFrom(signature))
                    // TODO(jack): ID
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
            connection.uploadKeys(identityKeyProto.toByteArray(), signedPreKeyProto.toByteArray(), oneTimePreKeys);

            encryptedKeyStore.setKeysUploaded(true);
        } else {
            Log.i("Keys were already uploaded");
        }
    }

    public void setUpSession(UserId peerUserId, byte[] recipientPublicIdentityKey, PublicECKey recipientPublicSignedPreKey, @Nullable OneTimePreKey recipientPublicOneTimePreKey) throws Exception {
        encryptedKeyStore.setPeerPublicIdentityKey(peerUserId, recipientPublicIdentityKey);
        encryptedKeyStore.setPeerSignedPreKey(peerUserId, recipientPublicSignedPreKey);
        if (recipientPublicOneTimePreKey != null) {
            encryptedKeyStore.setPeerOneTimePreKey(peerUserId, recipientPublicOneTimePreKey.publicECKey);
            encryptedKeyStore.setPeerOneTimePreKeyId(peerUserId, recipientPublicOneTimePreKey.id);
        }

        PrivateECKey privateEphemeralKey = ECKey.generatePrivateKey();
        PrivateECKey myPrivateIdentityKey = encryptedKeyStore.getMyPrivateX25519IdentityKey();

        byte[] a = CryptoUtil.ecdh(myPrivateIdentityKey, recipientPublicSignedPreKey);
        byte[] b = CryptoUtil.ecdh(privateEphemeralKey, new PublicECKey(SodiumWrapper.getInstance().convertPublicEdToX(recipientPublicIdentityKey)));
        byte[] c = CryptoUtil.ecdh(privateEphemeralKey, recipientPublicSignedPreKey);

        byte[] masterSecret;
        if (recipientPublicOneTimePreKey != null) {
            byte[] d = CryptoUtil.ecdh(privateEphemeralKey, recipientPublicOneTimePreKey.publicECKey);
            masterSecret = CryptoUtil.concat(a, b, c, d);
            CryptoUtil.nullify(d);
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
        encryptedKeyStore.setOutboundEphemeralKey(peerUserId, privateEphemeralKey);
        encryptedKeyStore.setOutboundEphemeralKeyId(peerUserId, firstId);

        CryptoUtil.nullify(a, b, c, masterSecret, output, rootKey, outboundChainKey, inboundChainKey);
    }

    public void receiveSessionSetup(UserId peerUserId, PublicECKey publicEphemeralKey, int ephemeralKeyId, byte[] initiatorPublicIdentityKey, @Nullable Integer oneTimePreKeyId) throws Exception {
        encryptedKeyStore.setPeerPublicIdentityKey(peerUserId, initiatorPublicIdentityKey);

        byte[] a = CryptoUtil.ecdh(encryptedKeyStore.getMyPrivateSignedPreKey(), new PublicECKey(SodiumWrapper.getInstance().convertPublicEdToX(initiatorPublicIdentityKey)));
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

        byte[] output = CryptoUtil.hkdf(masterSecret, null, 96);
        byte[] rootKey = Arrays.copyOfRange(output, 0, 32);

        // NOTE: Order switched so that keys match appropriately
        byte[] inboundChainKey = Arrays.copyOfRange(output, 32, 64);
        byte[] outboundChainKey = Arrays.copyOfRange(output, 64, 96);

        encryptedKeyStore.setRootKey(peerUserId, rootKey);
        encryptedKeyStore.setOutboundChainKey(peerUserId, outboundChainKey);
        encryptedKeyStore.setInboundChainKey(peerUserId, inboundChainKey);
        encryptedKeyStore.setInboundEphemeralKey(peerUserId, publicEphemeralKey);
        encryptedKeyStore.setInboundEphemeralKeyId(peerUserId, ephemeralKeyId);

        PrivateECKey myEphemeralKey = ECKey.generatePrivateKey();
        encryptedKeyStore.setOutboundEphemeralKey(peerUserId, myEphemeralKey);
        encryptedKeyStore.setOutboundEphemeralKeyId(peerUserId, 1);

        updateOutboundChainAndRootKey(peerUserId, myEphemeralKey, publicEphemeralKey);

        CryptoUtil.nullify(a, b, c, masterSecret, output, rootKey, inboundChainKey, outboundChainKey);
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

        CryptoUtil.nullify(chainKey, newChainKey);

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

        CryptoUtil.nullify(ephemeralSecret, output, rootKey, chainKey);
    }

}
