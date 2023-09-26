package com.halloapp.crypto.home;

import androidx.annotation.VisibleForTesting;

import com.google.protobuf.ByteString;
import com.goterl.lazysodium.interfaces.Sign;
import com.halloapp.Me;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.crypto.CryptoByteUtils;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.PrivateEdECKey;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.crypto.signal.SignalSessionSetupInfo;
import com.halloapp.id.UserId;
import com.halloapp.proto.clients.SenderKey;
import com.halloapp.proto.clients.SenderState;
import com.halloapp.proto.server.SenderStateBundle;
import com.halloapp.proto.server.SenderStateWithKeyInfo;
import com.halloapp.util.logs.Log;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
public class HomeFeedPostKeyManager {
    private static HomeFeedPostKeyManager instance;

    private static final byte[] HKDF_INPUT_MESSAGE_KEY = new byte[]{5};
    private static final byte[] HKDF_INPUT_CHAIN_KEY = new byte[]{6};

    private static final int MAX_HOME_FEED_KEYS_SKIP = 100;

    private final Me me;
    private final EncryptedKeyStore encryptedKeyStore;
    private final SignalSessionManager signalSessionManager;

    public static HomeFeedPostKeyManager getInstance() {
        if (instance == null) {
            synchronized (HomeFeedPostKeyManager.class) {
                if (instance == null) {
                    instance = new HomeFeedPostKeyManager(Me.getInstance(), EncryptedKeyStore.getInstance(), SignalSessionManager.getInstance());
                }
            }
        }
        return instance;
    }

    private HomeFeedPostKeyManager(Me me, EncryptedKeyStore encryptedKeyStore, SignalSessionManager signalSessionManager) {
        this.me = me;
        this.encryptedKeyStore = encryptedKeyStore;
        this.signalSessionManager = signalSessionManager;
    }

    HomePostSetupInfo ensureSetUp(boolean favorites) throws CryptoException {
        Map<UserId, SignalSessionSetupInfo> setupInfoMap = new HashMap<>();
        List<UserId> userIds = new ArrayList<>();

        ContactsDb contactsDb = ContactsDb.getInstance();
        List<Contact> friends;
        if (!favorites) {
            friends = contactsDb.getFriends();
        } else {
            List<UserId> favoritesUserIds = contactsDb.getFeedShareList();
            friends = new ArrayList<>();
            for (UserId userId : favoritesUserIds) {
                friends.add(contactsDb.getContact(userId));
            }
        }
        List<UserId> allUserIds = new ArrayList<>();
        for (Contact friend : friends) {
            allUserIds.add(friend.userId);
        }
        for (UserId userId : allUserIds) {
            if (!userId.isMe()) {
                SignalSessionSetupInfo signalSessionSetupInfo;
                try {
                    signalSessionSetupInfo = signalSessionManager.getSessionSetupInfo(userId);
                } catch (Exception e) {
                    Throwable cause = e.getCause();
                    if (cause != null && cause.getMessage() != null && cause.getMessage().contains("invalid_uid")) {
                        Log.d("HomeFeedPostKeyManager ensureSetUp failed due to invalid uid; forcing contact sync");
                        ContactsSync.getInstance().forceFullContactsSync();
                    }
                    throw new CryptoException("failed_get_session_setup_info", e);
                }
                setupInfoMap.put(userId, signalSessionSetupInfo);
                userIds.add(userId);
            }
        }

        List<SenderStateBundle> senderStateBundles = new ArrayList<>();
        if (!EncryptedKeyStore.getInstance().getHomeSendAlreadySetUp(favorites)) {
            Log.i("HomeFeedPostKeyManager: Home send not yet set up; setting up now");
            SecureRandom r = new SecureRandom();
            byte[] chainKey = new byte[32];
            r.nextBytes(chainKey);

            byte[] signatureKey = CryptoUtils.generateEd25519KeyPair(); // BOTH PUBLIC AND SECRET
            byte[] publicSignatureKeyBytes = Arrays.copyOfRange(signatureKey, 0, Sign.ED25519_PUBLICKEYBYTES);
            byte[] privateSignatureKeyBytes = Arrays.copyOfRange(signatureKey, Sign.ED25519_PUBLICKEYBYTES, signatureKey.length);
            PrivateEdECKey privateSignatureKey = new PrivateEdECKey(privateSignatureKeyBytes);

            int currentChainIndex = 0;

            SenderKey senderKey = SenderKey.newBuilder()
                    .setChainKey(ByteString.copyFrom(chainKey))
                    .setPublicSignatureKey(ByteString.copyFrom(publicSignatureKeyBytes))
                    .build();

            SenderState senderState = SenderState.newBuilder()
                    .setSenderKey(senderKey)
                    .setCurrentChainIndex(currentChainIndex)
                    .build();

            for (UserId userId : userIds) {
                byte[] senderStateBytes = senderState.toByteArray();
                byte[] encSenderKey = SignalSessionManager.getInstance().encryptMessage(senderStateBytes, userId);
                SenderStateWithKeyInfo.Builder info = SenderStateWithKeyInfo.newBuilder()
                        .setEncSenderState(ByteString.copyFrom(encSenderKey));
                SignalSessionSetupInfo signalSessionSetupInfo = setupInfoMap.get(userId);
                if (signalSessionSetupInfo != null) {
                    info.setPublicKey(ByteString.copyFrom(signalSessionSetupInfo.identityKey.getKeyMaterial()));
                    if (signalSessionSetupInfo.oneTimePreKeyId != null) {
                        info.setOneTimePreKeyId(signalSessionSetupInfo.oneTimePreKeyId);
                    }
                }
                SenderStateBundle senderStateBundle = SenderStateBundle.newBuilder()
                        .setSenderState(info)
                        .setUid(Long.parseLong(userId.rawId()))
                        .build();
                senderStateBundles.add(senderStateBundle);
            }

            encryptedKeyStore.edit()
                    .setMyHomeCurrentChainIndex(favorites, currentChainIndex)
                    .setMyHomeChainKey(favorites, chainKey)
                    .setMyHomeSigningKey(favorites, privateSignatureKey)
                    .setHomeSendAlreadySetUp(favorites)
                    .apply();
            encryptedKeyStore.removeAllNeedsStateUids(favorites);
        } else {
            List<UserId> needsSenderState = encryptedKeyStore.removeAllNeedsStateUids(favorites);
            SenderState senderState = getSenderState(favorites);
            for (UserId userId : needsSenderState) {
                if (!allUserIds.contains(userId)) {
                    Log.w("Dropping user needing sender state since not in audience list: " + userId);
                    continue;
                }
                byte[] senderStateBytes = senderState.toByteArray();
                byte[] encSenderKey = SignalSessionManager.getInstance().encryptMessage(senderStateBytes, userId);
                SenderStateWithKeyInfo.Builder info = SenderStateWithKeyInfo.newBuilder()
                        .setEncSenderState(ByteString.copyFrom(encSenderKey));
                SignalSessionSetupInfo signalSessionSetupInfo = setupInfoMap.get(userId);
                if (signalSessionSetupInfo != null) {
                    info.setPublicKey(ByteString.copyFrom(signalSessionSetupInfo.identityKey.getKeyMaterial()));
                    if (signalSessionSetupInfo.oneTimePreKeyId != null) {
                        info.setOneTimePreKeyId(signalSessionSetupInfo.oneTimePreKeyId);
                    }
                }
                SenderStateBundle senderStateBundle = SenderStateBundle.newBuilder()
                        .setSenderState(info)
                        .setUid(Long.parseLong(userId.rawId()))
                        .build();
                senderStateBundles.add(senderStateBundle);
            }
        }

        return new HomePostSetupInfo(senderStateBundles);
    }

    byte[] getNextInboundMessageKey(boolean favorites, UserId peerUserId, int currentChainIndex) throws CryptoException {
        int storedCurrentChainIndex = encryptedKeyStore.getPeerHomeCurrentChainIndex(favorites, peerUserId);
        Log.i("HomeFeedPostKeyManager.getNextInboundMessageKey for user " + peerUserId + " receivedIndex " + currentChainIndex + " storedIndex " + storedCurrentChainIndex);

        if (currentChainIndex < storedCurrentChainIndex) {
            Log.i("HomeFeedPostKeyManager retrieving stored group feed key");
            byte[] messageKey = encryptedKeyStore.removeSkippedHomeFeedKey(favorites, peerUserId, currentChainIndex);
            if (messageKey == null) {
                throw new CryptoException("old_home_key_not_found");
            }
            return messageKey;
        }

        skipInboundKeys(favorites, peerUserId, currentChainIndex - storedCurrentChainIndex - 1, storedCurrentChainIndex);
        encryptedKeyStore.edit().setPeerHomeCurrentChainIndex(favorites, peerUserId, currentChainIndex).apply();

        return getInboundMessageKey(favorites, peerUserId);
    }

    @VisibleForTesting
    public byte[] getInboundMessageKey(boolean favorites, UserId peerUserId) throws CryptoException {
        Log.i("HomeFeedPostKeyManager.getInboundMessageKey for user " + peerUserId);
        try {
            byte[] chainKey = encryptedKeyStore.getPeerHomeChainKey(favorites, peerUserId);
            byte[] messageKey = CryptoUtils.hkdf(chainKey, null, HKDF_INPUT_MESSAGE_KEY, 80);
            byte[] updatedChainKey = CryptoUtils.hkdf(chainKey, null, HKDF_INPUT_CHAIN_KEY, 32);
            encryptedKeyStore.edit().setPeerHomeChainKey(favorites, peerUserId, updatedChainKey).apply();
            Log.i("HomeFeedPostKeyManager.getInboundMessageKey chain key " + CryptoByteUtils.obfuscate(chainKey) + " -> " + CryptoByteUtils.obfuscate(updatedChainKey));
            CryptoByteUtils.nullify(chainKey, updatedChainKey);
            return messageKey;
        } catch (NullPointerException e) {
            throw new CryptoException("home_inbound_key_null", e);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("home_inbound_key_fail", e);
        }
    }

    public byte[] getNextOutboundMessageKey(boolean favorites) throws CryptoException {
        Log.i("HomeFeedPostKeyManager.getNextOutboundMessageKey");
        byte[] chainKey = encryptedKeyStore.getMyHomeChainKey(favorites);
        try {
            int currentChainIndex = encryptedKeyStore.getMyHomeCurrentChainIndex(favorites);
            int nextChainIndex = currentChainIndex + 1;
            byte[] messageKey = CryptoUtils.hkdf(chainKey, null, HKDF_INPUT_MESSAGE_KEY, 80);
            byte[] updatedChainKey = CryptoUtils.hkdf(chainKey, null, HKDF_INPUT_CHAIN_KEY, 32);
            encryptedKeyStore.edit()
                    .setMyHomeChainKey(favorites, updatedChainKey)
                    .setMyHomeCurrentChainIndex(favorites, nextChainIndex)
                    .apply();
            Log.i("HomeFeedPostKeyManager.getOutboundMessageKey chain key " + CryptoByteUtils.obfuscate(chainKey) + " -> " + CryptoByteUtils.obfuscate(updatedChainKey));
            CryptoByteUtils.nullify(chainKey, updatedChainKey);
            return messageKey;
        } catch (NullPointerException e) {
            throw new CryptoException("home_outbound_key_null", e);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("home_outbound_key_fail", e);
        }
    }

    SenderState getSenderState(boolean favorites) throws CryptoException {
        byte[] chainKey = encryptedKeyStore.getMyHomeChainKey(favorites);
        PublicEdECKey publicSignatureKey = encryptedKeyStore.getMyPublicHomeSigningKey(favorites);
        int currentChainIndex = encryptedKeyStore.getMyHomeCurrentChainIndex(favorites);

        SenderKey senderKey = SenderKey.newBuilder()
                .setChainKey(ByteString.copyFrom(chainKey))
                .setPublicSignatureKey(ByteString.copyFrom(publicSignatureKey.getKeyMaterial()))
                .build();

        return SenderState.newBuilder()
                .setSenderKey(senderKey)
                .setCurrentChainIndex(currentChainIndex)
                .build();
    }

    private void skipInboundKeys(boolean favorites, UserId peerUserId, int count, int startIndex) throws CryptoException {
        Log.i("HomeFeedPostKeyManager: skipping " + count + " inbound keys");
        if (count > MAX_HOME_FEED_KEYS_SKIP) {
            Log.e("Attempting to skip too many keys");
            throw new CryptoException("skip_too_many_home_keys");
        }
        for (int i=0; i<count; i++) {
            byte[] inboundMessageKey = getInboundMessageKey(favorites, peerUserId);
            try {
                HomeFeedPostMessageKey messageKey = new HomeFeedPostMessageKey(startIndex + i, inboundMessageKey);
                encryptedKeyStore.edit().storeSkippedHomeFeedKey(favorites, peerUserId, messageKey).apply();
            } catch (CryptoException e) {
                Log.e("Cannot store invalid incoming home feed key for later use", e);
                throw new CryptoException("skip_home_key_" + e.getMessage());
            }
        }
    }

    void tearDownOutboundSession(boolean favorites) {
        Log.i("HomeFeedPostKeyManager tearing down outbound session");
        encryptedKeyStore.edit()
                .clearMyHomeChainKey(favorites)
                .clearMyHomeCurrentChainIndex(favorites)
                .clearMyHomeSigningKey(favorites)
                .clearHomeSendAlreadySetUp(favorites)
                .apply();
    }

    void tearDownInboundSession(boolean favorites, UserId peerUserId) {
        Log.i("HomeFeedPostKeyManager tearing down inbound session with " + peerUserId);
        encryptedKeyStore.edit()
                .clearPeerHomeChainKey(favorites, peerUserId)
                .clearPeerHomeCurrentChainIndex(favorites, peerUserId)
                .clearPeerHomeSigningKey(favorites, peerUserId)
                .apply();
    }
}
