package com.halloapp.crypto.keys;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.halloapp.AppContext;
import com.halloapp.Me;
import com.halloapp.crypto.CryptoByteUtils;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.group.GroupFeedMessageKey;
import com.halloapp.crypto.signal.SignalMessageKey;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Tink's KeysetManager currently does not allow importing of keys (this is by design,
 * to reduce chances of key exposure or improperly generated keys). However, we still need
 * to store keys that are generated in ways that Tink does not support, so we need to
 * handle storing these ourselves.
 *
 * TODO(jack): Signed pre key key rotation
 */
public class EncryptedKeyStore {

    private static final String ENC_PREF_FILE_NAME = "halloapp_keys";
    private static final String PT_PREF_FILE_NAME = "pt_halloapp_keys"; // TODO(jack): delete file

    private static final String PREF_KEY_PT_MIGRATED = "pt_migrated";

    private static final String PREF_KEY_MY_ED25519_IDENTITY_KEY = "my_ed25519_identity_key";
    private static final String PREF_KEY_MY_PRIVATE_SIGNED_PRE_KEY = "my_private_signed_pre_key";
    private static final String PREF_KEY_LAST_ONE_TIME_PRE_KEY_ID = "last_one_time_pre_key_id";
    private static final String PREF_KEY_MY_PREVIOUS_PUBLIC_ED25519_IK = "my_previous_ed25519_public_ik";

    private static final String PREF_KEY_ONE_TIME_PRE_KEY_ID_PREFIX = "one_time_pre_key";
    private static final String PREF_KEY_MESSAGE_KEY_PREFIX = "message_key";
    private static final String PREF_KEY_MESSAGE_KEY_SET_PREFIX = "message_key_set";
    private static final String PREF_KEY_LAST_DOWNLOAD_ATTEMPT_SUFFIX = "last_download_attempt";

    private static final String PREF_KEY_PEER_VERIFIED = "peer_verified";
    private static final String PREF_KEY_SESSION_ALREADY_SET_UP_SUFFIX = "session_already_set_up";
    private static final String PREF_KEY_PEER_RESPONDED_SUFFIX = "peer_responded";
    private static final String PREF_KEY_PEER_IDENTITY_KEY_SUFFIX = "peer_identity_key";
    private static final String PREF_KEY_PEER_SIGNED_PRE_KEY_SUFFIX = "peer_signed_pre_key";
    private static final String PREF_KEY_PEER_ONE_TIME_PRE_KEY_SUFFIX = "peer_one_time_pre_key";
    private static final String PREF_KEY_PEER_ONE_TIME_PRE_KEY_ID_SUFFIX = "peer_one_time_pre_key_id";
    private static final String PREF_KEY_ROOT_KEY_SUFFIX = "root_key";
    private static final String PREF_KEY_OUTBOUND_CHAIN_KEY_SUFFIX = "outbound_chain_key";
    private static final String PREF_KEY_INBOUND_CHAIN_KEY_SUFFIX = "inbound_chain_key";
    private static final String PREF_KEY_INBOUND_EPHEMERAL_KEY_SUFFIX = "last_received_ephemeral_key";
    private static final String PREF_KEY_OUTBOUND_EPHEMERAL_KEY_SUFFIX = "last_sent_ephemeral_key";
    private static final String PREF_KEY_INBOUND_EPHEMERAL_KEY_ID_SUFFIX = "last_received_ephemeral_key_id";
    private static final String PREF_KEY_OUTBOUND_EPHEMERAL_KEY_ID_SUFFIX = "last_sent_ephemeral_key_id";
    private static final String PREF_KEY_INBOUND_PREVIOUS_CHAIN_LENGTH_SUFFIX = "inbound_previous_chain_length";
    private static final String PREF_KEY_OUTBOUND_PREVIOUS_CHAIN_LENGTH_SUFFIX = "outbound_previous_chain_length";
    private static final String PREF_KEY_INBOUND_CURRENT_CHAIN_INDEX_SUFFIX = "inbound_current_chain_index";
    private static final String PREF_KEY_OUTBOUND_CURRENT_CHAIN_INDEX_SUFFIX = "outbound_current_chain_index";
    private static final String PREF_KEY_INBOUND_TEARDOWN_KEY = "inbound_teardown_key";
    private static final String PREF_KEY_OUTBOUND_TEARDOWN_KEY = "outbound_teardown_key";

    private static final String PREF_KEY_GROUP_SEND_ALREADY_SET_UP = "group_send_already_set_up";
    private static final String PREF_KEY_MY_GROUP_CURRENT_CHAIN_INDEX = "my_current_chain_index";
    private static final String PREF_KEY_PEER_GROUP_CURRENT_CHAIN_INDEX = "peer_current_chain_index";
    private static final String PREF_KEY_MY_GROUP_CHAIN_KEY = "my_group_chain_key";
    private static final String PREF_KEY_PEER_GROUP_CHAIN_KEY = "peer_group_chain_key";
    private static final String PREF_KEY_MY_GROUP_SIGNING_KEY = "my_group_signing_key";
    private static final String PREF_KEY_PEER_GROUP_SIGNING_KEY = "peer_group_signing_key";
    private static final String PREF_KEY_SKIPPED_GROUP_FEED_KEYS_SET = "skipped_group_feed_keys_set";
    private static final String PREF_KEY_SKIPPED_GROUP_FEED_KEY = "skipped_group_feed_key";

    private static final int CURVE_25519_PRIVATE_KEY_LENGTH = 32;

    private static final int ONE_TIME_PRE_KEY_BATCH_COUNT = 100;
    private static final int KEY_STORE_RETRY_WAIT_MS = 50;

    private static EncryptedKeyStore instance;

    // TODO(jack): Try moving away from SharedPreferences to avoid Strings in memory with key material
    private SharedPreferences sharedPreferences;

    private final AppContext appContext;

    public static EncryptedKeyStore getInstance() {
        if (instance == null) {
            synchronized (EncryptedKeyStore.class) {
                if (instance == null) {
                    instance = new EncryptedKeyStore(AppContext.getInstance());
                }
            }
        }
        return instance;
    }

    private EncryptedKeyStore(AppContext appContext) {
        this.appContext = appContext;
    }

    private synchronized SharedPreferences getPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences(appContext.get(), true);
        }
        return sharedPreferences;
    }

    public boolean getPeerVerified(UserId peerUserId) {
        return getPreferences().getBoolean(getPeerVerifiedPrefKey(peerUserId), false);
    }

    public void setPeerVerified(UserId peerUserId, boolean verified) {
        if (!getPreferences().edit().putBoolean(getPeerVerifiedPrefKey(peerUserId), verified).commit()) {
            Log.e("EncryptedKeyStore: failed to set peer verification state");
        }
    }

    public void clearPeerVerified(UserId peerUserId) {
        if (!getPreferences().edit().remove(getPeerVerifiedPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear peer verification state");
        }
    }

    private String getPeerVerifiedPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_VERIFIED;
    }

    public boolean getSessionAlreadySetUp(UserId peerUserId) {
        return getPreferences().getBoolean(getSessionAlreadySetUpPrefKey(peerUserId), false);
    }

    public void setSessionAlreadySetUp(UserId peerUserId, boolean downloaded) {
        if (!getPreferences().edit().putBoolean(getSessionAlreadySetUpPrefKey(peerUserId), downloaded).commit()) {
            Log.e("EncryptedKeyStore: failed to set session already set up");
        }
    }

    public void clearSessionAlreadySetUp(UserId peerUserId) {
        if (!getPreferences().edit().remove(getSessionAlreadySetUpPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear session already set up");
        }
    }

    private String getSessionAlreadySetUpPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_SESSION_ALREADY_SET_UP_SUFFIX;
    }

    public long getLastDownloadAttempt(UserId peerUserId) {
        return getPreferences().getLong(getLastDownloadAttemptPrefKey(peerUserId), 0);
    }

    public void setLastDownloadAttempt(UserId peerUserId, long lastDownloadAttempt) {
        if (!getPreferences().edit().putLong(getLastDownloadAttemptPrefKey(peerUserId), lastDownloadAttempt).commit()) {
            Log.e("EncryptedKeyStore: failed to set last download attempt");
        }
    }

    public void clearLastDownloadAttempt(UserId peerUserId) {
        if (!getPreferences().edit().remove(getLastDownloadAttemptPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear last download attempt");
        }
    }

    private String getLastDownloadAttemptPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_LAST_DOWNLOAD_ATTEMPT_SUFFIX;
    }

    public boolean getPeerResponded(UserId peerUserId) {
        return getPreferences().getBoolean(getPeerRespondedPrefKey(peerUserId), false);
    }

    public void setPeerResponded(UserId peerUserId, boolean responded) {
        if (!getPreferences().edit().putBoolean(getPeerRespondedPrefKey(peerUserId), responded).commit()) {
            Log.e("EncryptedKeyStore: failed to set peer responded");
        }
    }

    public void clearPeerResponded(UserId peerUserId) {
        if (!getPreferences().edit().remove(getPeerRespondedPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear peer responded");
        }
    }

    private String getPeerRespondedPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_RESPONDED_SUFFIX;
    }

    public void generateClientPrivateKeys() {
        Log.i("EncryptedKeyStore: Generating new keys");
        clearMyPreviousPublicEd25519IdentityKey();
        setMyEd25519IdentityKey(CryptoUtils.generateEd25519KeyPair());
        setMyPrivateSignedPreKey(CryptoUtils.generateX25519PrivateKey());
    }

    public boolean clientPrivateKeysSet() {
        return getMyEd25519IdentityKey() != null;
    }

    private void setMyEd25519IdentityKey(byte[] key) {
        storeBytes(PREF_KEY_MY_ED25519_IDENTITY_KEY, key);
    }

    private byte[] getMyEd25519IdentityKey() {
        return retrieveBytes(PREF_KEY_MY_ED25519_IDENTITY_KEY);
    }

    public PublicEdECKey getMyPublicEd25519IdentityKey() {
        try {
            return new PublicEdECKey(Arrays.copyOfRange(getMyEd25519IdentityKey(), 0, 32));
        } catch (NullPointerException e) {
            Log.e("Failed to retrieve identity key; resetting key store and registration", e);
            clearAll();
            Me.getInstance().resetRegistration();
            Log.sendErrorReport("Missing own identity key");
            throw e;
        }
    }

    public PrivateEdECKey getMyPrivateEd25519IdentityKey() {
        return new PrivateEdECKey(Arrays.copyOfRange(getMyEd25519IdentityKey(), 32, 96));
    }

    public PrivateXECKey getMyPrivateX25519IdentityKey() throws CryptoException {
        return CryptoUtils.convertPrivateEdToX(getMyPrivateEd25519IdentityKey());
    }

    public void checkIdentityKeyChanges() {
        PublicEdECKey current = getMyPublicEd25519IdentityKey();
        PublicEdECKey previous = getMyPreviousPublicEd25519IdentityKey();
        if (previous != null) {
            Log.i("EncryptedKeyStore: Checking for identity key changes");
            if (!Arrays.equals(current.getKeyMaterial(), previous.getKeyMaterial())) {
                Log.e("EncryptedKeyStore: Previous identity key was " + Base64.encodeToString(previous.getKeyMaterial(), Base64.NO_WRAP)
                        + " but current is " + Base64.encodeToString(current.getKeyMaterial(), Base64.NO_WRAP));
                Log.sendErrorReport("Identity key changed");
            }
        }
        setMyPreviousPublicEd25519IdentityKey(current);
    }

    private void setMyPreviousPublicEd25519IdentityKey(PublicEdECKey key) {
        storeBytes(PREF_KEY_MY_PREVIOUS_PUBLIC_ED25519_IK, key.getKeyMaterial());
    }

    private void clearMyPreviousPublicEd25519IdentityKey() {
        getPreferences().edit().remove(PREF_KEY_MY_PREVIOUS_PUBLIC_ED25519_IK).apply();
    }

    private PublicEdECKey getMyPreviousPublicEd25519IdentityKey() {
        byte[] bytes = retrieveBytes(PREF_KEY_MY_PREVIOUS_PUBLIC_ED25519_IK);
        if (bytes == null) {
            return null;
        }
        return new PublicEdECKey(bytes);
    }

    private void setMyPrivateSignedPreKey(byte[] key) {
        storeCurve25519PrivateKey(PREF_KEY_MY_PRIVATE_SIGNED_PRE_KEY, key);
    }

    public PrivateXECKey getMyPrivateSignedPreKey() throws CryptoException {
        return new PrivateXECKey(getMyPrivateSignedPreKeyInternal());
    }

    private byte[] getMyPrivateSignedPreKeyInternal() {
        return retrieveCurve25519PrivateKey(PREF_KEY_MY_PRIVATE_SIGNED_PRE_KEY);
    }

    public PublicXECKey getMyPublicSignedPreKey() {
        try {
            return XECKey.publicFromPrivate(getMyPrivateSignedPreKey());
        } catch (CryptoException e) {
            Log.w("Failed to get public identity key", e);
        }
        return null;
    }

    public Set<OneTimePreKey> getNewBatchOfOneTimePreKeys() {
        int startId = getPreferences().getInt(PREF_KEY_LAST_ONE_TIME_PRE_KEY_ID, 1);

        Set<OneTimePreKey> ret = new HashSet<>();
        for (int i=0; i<ONE_TIME_PRE_KEY_BATCH_COUNT; i++) {
            int id = startId + i;
            PrivateXECKey privateKey = XECKey.generatePrivateKey();
            try {
                OneTimePreKey otpk = new OneTimePreKey(XECKey.publicFromPrivate(privateKey), id);
                storeCurve25519PrivateKey(getOneTimePreKeyPrefKey(id), privateKey.getKeyMaterial());
                ret.add(otpk);
            } catch (CryptoException e) {
                Log.w("Invalid X25519 private key for conversion", e);
            }
        }

        if (!getPreferences().edit().putInt(PREF_KEY_LAST_ONE_TIME_PRE_KEY_ID, startId + ONE_TIME_PRE_KEY_BATCH_COUNT).commit()) {
            Log.e("Failed to update last otpk id");
        }

        return ret;
    }

    public PrivateXECKey removeOneTimePreKeyById(int id) throws CryptoException {
        String prefKey = getOneTimePreKeyPrefKey(id);
        byte[] rawKey = retrieveCurve25519PrivateKey(prefKey);
        if (rawKey == null) {
            Log.w("Could not find OTPK for id " + id);
            throw new CryptoException("otpk_not_found");
        }
        PrivateXECKey ret = new PrivateXECKey(rawKey);
        if (!getPreferences().edit().remove(prefKey).commit()) {
            Log.e("EncryptedKeyStore: failed to remove one time pre key");
        }
        return ret;
    }

    private String getOneTimePreKeyPrefKey(int id) {
        return PREF_KEY_ONE_TIME_PRE_KEY_ID_PREFIX + "/" + id;
    }

    public byte[] removeSkippedMessageKey(UserId peerUserId, int ephemeralKeyId, int chainIndex) {
        String messageKeySetPrefKey = getMessageKeySetPrefKey(peerUserId);
        Set<String> messageKeyPrefKeys = new HashSet<>(Preconditions.checkNotNull(getPreferences().getStringSet(messageKeySetPrefKey, new HashSet<>())));

        String prefKey = getMessageKeyPrefKey(peerUserId, ephemeralKeyId, chainIndex);
        if (!messageKeyPrefKeys.remove(prefKey)) {
            Log.e("Message key for " + prefKey + " not found in set");
            return null;
        }

        String messageKeyString = getPreferences().getString(prefKey, null);
        if (messageKeyString == null) {
            Log.e("Failed to retrieve message key for " + prefKey);
            return null;
        }

        if (!getPreferences().edit().putStringSet(messageKeySetPrefKey, messageKeyPrefKeys).commit()) {
            Log.e("EncryptedKeyStore: failed to rewrite skipped message key set");
        }

        return stringToBytes(messageKeyString);
    }

    // TODO(jack): Clear out old keys after some threshold
    public void storeSkippedMessageKey(UserId peerUserId, SignalMessageKey signalMessageKey) {
        Log.i("Storing skipped message key " + signalMessageKey + " for user " + peerUserId);
        String messageKeySetPrefKey = getMessageKeySetPrefKey(peerUserId);
        Set<String> messageKeyPrefKeys = new HashSet<>(Preconditions.checkNotNull(getPreferences().getStringSet(messageKeySetPrefKey, new HashSet<>())));

        String keyPrefKey = getMessageKeyPrefKey(peerUserId, signalMessageKey.getEphemeralKeyId(), signalMessageKey.getCurrentChainIndex());
        messageKeyPrefKeys.add(keyPrefKey);

        if (!getPreferences().edit().putString(keyPrefKey, bytesToString(signalMessageKey.getKeyMaterial())).putStringSet(messageKeySetPrefKey, messageKeyPrefKeys).commit()) {
            Log.e("EncryptedKeyStore: failed to store skipped message key");
        }
    }

    public void clearSkippedMessageKeys(UserId peerUserId) {
        String messageKeySetPrefKey = getMessageKeySetPrefKey(peerUserId);
        Set<String> messageKeyPrefKeys = new HashSet<>(Preconditions.checkNotNull(getPreferences().getStringSet(messageKeySetPrefKey, new HashSet<>())));

        SharedPreferences.Editor editor = getPreferences().edit();
        editor.remove(messageKeySetPrefKey);

        for (String prefKey : messageKeyPrefKeys) {
            editor.remove(prefKey);
        }

        if (!editor.commit()) {
            Log.e("EncryptedKeyStore: failed to clear skipped message keys");
        }
    }

    private String getMessageKeySetPrefKey(UserId peerUserId) {
        return PREF_KEY_MESSAGE_KEY_SET_PREFIX + "/" + peerUserId.rawId();
    }

    private String getMessageKeyPrefKey(UserId peerUserId, int ephemeralKeyId, int currentChainIndex) {
        return PREF_KEY_MESSAGE_KEY_PREFIX + "/" + peerUserId.rawId() + "/" + ephemeralKeyId + "/" + currentChainIndex;
    }

    public void setPeerPublicIdentityKey(UserId peerUserId, PublicEdECKey key) {
        storeBytes(getPeerPublicIdentityKeyPrefKey(peerUserId), key.getKeyMaterial());
    }

    public PublicEdECKey getPeerPublicIdentityKey(UserId peerUserId) throws CryptoException {
        try {
            return new PublicEdECKey(retrieveBytes(getPeerPublicIdentityKeyPrefKey(peerUserId)));
        } catch (NullPointerException e) {
            throw new CryptoException("no_key_found", e);
        }
    }

    public void clearPeerPublicIdentityKey(UserId peerUserId) {
        if (!getPreferences().edit().remove(getPeerPublicIdentityKeyPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear peer identity key");
        }
    }

    private String getPeerPublicIdentityKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_IDENTITY_KEY_SUFFIX;
    }

    public void setPeerSignedPreKey(UserId peerUserId, PublicXECKey key) {
        storeBytes(getPeerSignedPreKeyPrefKey(peerUserId), key.getKeyMaterial());
    }

    public PublicXECKey getPeerSignedPreKey(UserId peerUserId) throws CryptoException {
        return new PublicXECKey(retrieveBytes(getPeerSignedPreKeyPrefKey(peerUserId)));
    }

    public void clearPeerSignedPreKey(UserId peerUserId) {
        if (!getPreferences().edit().remove(getPeerSignedPreKeyPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear peer signed pre key");
        }
    }

    private String getPeerSignedPreKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_SIGNED_PRE_KEY_SUFFIX;
    }

    public void setPeerOneTimePreKey(UserId peerUserId, PublicXECKey key) {
        storeBytes(getPeerOneTimePreKeyPrefKey(peerUserId), key.getKeyMaterial());
    }

    public PublicXECKey getPeerOneTimePreKey(UserId peerUserId) throws CryptoException {
        return new PublicXECKey(retrieveBytes(getPeerOneTimePreKeyPrefKey(peerUserId)));
    }

    public void clearPeerOneTimePreKey(UserId peerUserId) {
        if (!getPreferences().edit().remove(getPeerOneTimePreKeyPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear one-time pre key");
        }
    }

    private String getPeerOneTimePreKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_ONE_TIME_PRE_KEY_SUFFIX;
    }

    public void setPeerOneTimePreKeyId(UserId peerUserId, int id) {
        if (!getPreferences().edit().putInt(getPeerOneTimePreKeyIdPrefKey(peerUserId), id).commit()) {
            Log.e("EncryptedKeyStore: failed to set one-time pre key id");
        }
    }

    public Integer getPeerOneTimePreKeyId(UserId peerUserId) {
        int ret = getPreferences().getInt(getPeerOneTimePreKeyIdPrefKey(peerUserId), -1);
        if (ret == -1) {
            return null;
        }
        return ret;
    }

    public void clearPeerOneTimePreKeyId(UserId peerUserId) {
        if (!getPreferences().edit().remove(getPeerOneTimePreKeyIdPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear one-time pre key id");
        }
    }

    private String getPeerOneTimePreKeyIdPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_ONE_TIME_PRE_KEY_ID_SUFFIX;
    }

    public void setRootKey(UserId peerUserId, byte[] key) {
        storeBytes(getRootKeyPrefKey(peerUserId), key);
    }

    public byte[] getRootKey(UserId peerUserId) {
        return retrieveBytes(getRootKeyPrefKey(peerUserId));
    }

    public void clearRootKey(UserId peerUserId) {
        if (!getPreferences().edit().remove(getRootKeyPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear root key");
        }
    }

    private String getRootKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_ROOT_KEY_SUFFIX;
    }

    public void setOutboundChainKey(UserId peerUserId, byte[] key) {
        storeBytes(getOutboundChainKeyPrefKey(peerUserId), key);
    }

    public byte[] getOutboundChainKey(UserId peerUserId) {
        return retrieveBytes(getOutboundChainKeyPrefKey(peerUserId));
    }

    public void clearOutboundChainKey(UserId peerUserId) {
        if (!getPreferences().edit().remove(getOutboundChainKeyPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear outbound chain key");
        }
    }

    private String getOutboundChainKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_OUTBOUND_CHAIN_KEY_SUFFIX;
    }

    public void setInboundChainKey(UserId peerUserId, byte[] key) {
        storeBytes(getInboundChainKeyPrefKey(peerUserId), key);
    }

    public byte[] getInboundChainKey(UserId peerUserId) {
        return retrieveBytes(getInboundChainKeyPrefKey(peerUserId));
    }

    public void clearInboundChainKey(UserId peerUserId) {
        if (!getPreferences().edit().remove(getInboundChainKeyPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear inbound chain key");
        }
    }

    private String getInboundChainKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_CHAIN_KEY_SUFFIX;
    }

    public void setInboundEphemeralKey(UserId peerUserId, PublicXECKey key) {
        storeCurve25519PrivateKey(getInboundEphemeralKeyPrefKey(peerUserId), key.getKeyMaterial());
    }

    public PublicXECKey getInboundEphemeralKey(UserId peerUserId) throws CryptoException {
        return new PublicXECKey(retrieveCurve25519PrivateKey(getInboundEphemeralKeyPrefKey(peerUserId)));
    }

    public void clearInboundEphemeralKey(UserId peerUserId) {
        if (!getPreferences().edit().remove(getInboundEphemeralKeyPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear inbound ephemeral key");
        }
    }

    private String getInboundEphemeralKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_EPHEMERAL_KEY_SUFFIX;
    }

    public void setOutboundEphemeralKey(UserId peerUserId, PrivateXECKey key) {
        storeCurve25519PrivateKey(getOutboundEphemeralKeyPrefKey(peerUserId), key.getKeyMaterial());
    }

    public PrivateXECKey getOutboundEphemeralKey(UserId peerUserId) throws CryptoException {
        return new PrivateXECKey(retrieveCurve25519PrivateKey(getOutboundEphemeralKeyPrefKey(peerUserId)));
    }

    public void clearOutboundEphemeralKey(UserId peerUserId) {
        if (!getPreferences().edit().remove(getOutboundEphemeralKeyPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear outbound ephemeral key");
        }
    }

    private String getOutboundEphemeralKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_OUTBOUND_EPHEMERAL_KEY_SUFFIX;
    }

    public void setInboundEphemeralKeyId(UserId peerUserId, int id) {
        if (!getPreferences().edit().putInt(getInboundEphemeralKeyIdPrefKey(peerUserId), id).commit()) {
            Log.e("EncryptedKeyStore: failed to set inbound ephemeral key id");
        }
    }

    public int getInboundEphemeralKeyId(UserId peerUserId) {
        return getPreferences().getInt(getInboundEphemeralKeyIdPrefKey(peerUserId), -1);
    }

    public void clearInboundEphemeralKeyId(UserId peerUserId) {
        if (!getPreferences().edit().remove(getInboundEphemeralKeyIdPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear inbound ephemeral key id");
        }
    }

    private String getInboundEphemeralKeyIdPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_EPHEMERAL_KEY_ID_SUFFIX;
    }

    public void setOutboundEphemeralKeyId(UserId peerUserId, int id) {
        if (!getPreferences().edit().putInt(getOutboundEphemeralKeyIdPrefKey(peerUserId), id).commit()) {
            Log.e("EncryptedKeyStore: failed to set outbound ephemeral key id");
        }
    }

    public int getOutboundEphemeralKeyId(UserId peerUserId) {
        return getPreferences().getInt(getOutboundEphemeralKeyIdPrefKey(peerUserId), -1);
    }

    public void clearOutboundEphemeralKeyId(UserId peerUserId) {
        if (!getPreferences().edit().remove(getOutboundEphemeralKeyIdPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear outbound ephemeral key id");
        }
    }

    private String getOutboundEphemeralKeyIdPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_OUTBOUND_EPHEMERAL_KEY_ID_SUFFIX;
    }

    public void setInboundPreviousChainLength(UserId peerUserId, int len) {
        if (!getPreferences().edit().putInt(getInboundPreviousChainLengthPrefKey(peerUserId), len).commit()) {
            Log.e("EncryptedKeyStore: failed to set inbound previous chain length");
        }
    }

    public int getInboundPreviousChainLength(UserId peerUserId) {
        return getPreferences().getInt(getInboundPreviousChainLengthPrefKey(peerUserId), 0);
    }

    public void clearInboundPreviousChainLength(UserId peerUserId) {
        if (!getPreferences().edit().remove(getInboundPreviousChainLengthPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear inbound previous chain length");
        }
    }

    private String getInboundPreviousChainLengthPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_PREVIOUS_CHAIN_LENGTH_SUFFIX;
    }

    public void setOutboundPreviousChainLength(UserId peerUserId, int len) {
        if (!getPreferences().edit().putInt(getOutboundPreviousChainLengthPrefKey(peerUserId), len).commit()) {
            Log.e("EncryptedKeyStore: failed to set outbound previous chain length");
        }
    }

    public int getOutboundPreviousChainLength(UserId peerUserId) {
        return getPreferences().getInt(getOutboundPreviousChainLengthPrefKey(peerUserId), 0);
    }

    public void clearOutboundPreviousChainLength(UserId peerUserId) {
        if (!getPreferences().edit().remove(getOutboundPreviousChainLengthPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear outbound previous chain length");
        }
    }

    private String getOutboundPreviousChainLengthPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_OUTBOUND_PREVIOUS_CHAIN_LENGTH_SUFFIX;
    }

    public void setInboundCurrentChainIndex(UserId peerUserId, int index) {
        if (!getPreferences().edit().putInt(getInboundCurrentChainIndexPrefKey(peerUserId), index).commit()) {
            Log.e("EncryptedKeyStore: failed to set inbound current chain index");
        }
    }

    public int getInboundCurrentChainIndex(UserId peerUserId) {
        return getPreferences().getInt(getInboundCurrentChainIndexPrefKey(peerUserId), 0);
    }

    public void clearInboundCurrentChainIndex(UserId peerUserId) {
        if (!getPreferences().edit().remove(getInboundCurrentChainIndexPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear inbound current chain index");
        }
    }

    private String getInboundCurrentChainIndexPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_CURRENT_CHAIN_INDEX_SUFFIX;
    }

    public void setOutboundCurrentChainIndex(UserId peerUserId, int index) {
        if (!getPreferences().edit().putInt(getOutboundCurrentChainIndexPrefKey(peerUserId), index).commit()) {
            Log.e("EncryptedKeyStore: failed to set outbound current chain index");
        }
    }

    public int getOutboundCurrentChainIndex(UserId peerUserId) {
        return getPreferences().getInt(getOutboundCurrentChainIndexPrefKey(peerUserId), 0);
    }

    public void clearOutboundCurrentChainIndex(UserId peerUserId) {
        if (!getPreferences().edit().remove(getOutboundCurrentChainIndexPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear outbound current chain index");
        }
    }

    private String getOutboundCurrentChainIndexPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_OUTBOUND_CURRENT_CHAIN_INDEX_SUFFIX;
    }

    public void setOutboundTeardownKey(UserId peerUserId, byte[] teardownKey) {
        storeBytes(getOutboundTeardownKeyPrefKey(peerUserId), teardownKey);
    }

    public byte[] getOutboundTeardownKey(UserId peerUserId) {
        return retrieveBytes(getOutboundTeardownKeyPrefKey(peerUserId));
    }

    public void clearOutboundTeardownKey(UserId peerUserId) {
        if (!getPreferences().edit().remove(getOutboundTeardownKeyPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear outbound teardown key");
        }
    }

    private String getOutboundTeardownKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_OUTBOUND_TEARDOWN_KEY;
    }

    public void setInboundTeardownKey(UserId peerUserId, byte[] teardownKey) {
        storeBytes(getInboundTeardownKeyPrefKey(peerUserId), teardownKey);
    }

    public byte[] getInboundTeardownKey(UserId peerUserId) {
        return retrieveBytes(getInboundTeardownKeyPrefKey(peerUserId));
    }

    public void clearInboundTeardownKey(UserId peerUserId) {
        if (!getPreferences().edit().remove(getInboundTeardownKeyPrefKey(peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear inbound teardown key");
        }
    }

    private String getInboundTeardownKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_TEARDOWN_KEY;
    }

    public String getLogInfo(@NonNull UserId userId) {
        StringBuilder sb = new StringBuilder();

        sb.append("TS: ");
        try {
            DateFormat df = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG, Locale.US);
            sb.append(df.format(new Date()));
        } catch (IllegalArgumentException e) {
            Log.w("Failed to create date format", e);
            sb.append("IllegalArgumentException");
        }

        try {
            sb.append("; MIK:");
            sb.append(Base64.encodeToString(getMyPublicEd25519IdentityKey().getKeyMaterial(), Base64.NO_WRAP));
        } catch (NullPointerException e) {
            sb.append("null");
        }

        try {
            sb.append("; PIK:");
            sb.append(Base64.encodeToString(getPeerPublicIdentityKey(userId).getKeyMaterial(), Base64.NO_WRAP));
        } catch (NullPointerException | CryptoException e) {
            Log.w("Failed to get peer public identity key", e);
            sb.append("CryptoException");
        }

        sb.append("; MICKH:").append(CryptoByteUtils.obfuscate(getInboundChainKey(userId)));
        sb.append("; MOCKH:").append(CryptoByteUtils.obfuscate(getOutboundChainKey(userId)));

        return sb.toString();
    }



    // GROUPS
    private String getGroupSendAlreadySetUpPrefKey(GroupId groupId) {
        return groupId.rawId() + "/" + PREF_KEY_GROUP_SEND_ALREADY_SET_UP;
    }

    public void setGroupSendAlreadySetUp(GroupId groupId) {
        if (!getPreferences().edit().putBoolean(getGroupSendAlreadySetUpPrefKey(groupId), true).commit()) {
            Log.e("EncryptedKeyStore: failed to set group already set up");
        }
    }

    public boolean getGroupSendAlreadySetUp(GroupId groupId) {
        return getPreferences().getBoolean(getGroupSendAlreadySetUpPrefKey(groupId), false);
    }

    public void clearGroupSendAlreadySetUp(GroupId groupId) {
        if (!getPreferences().edit().remove(getGroupSendAlreadySetUpPrefKey(groupId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear group send already set up");
        }
    }

    private String getMyGroupCurrentChainIndexPrefKey(GroupId groupId) {
        return groupId.rawId() + "/" + PREF_KEY_MY_GROUP_CURRENT_CHAIN_INDEX;
    }

    public void setMyGroupCurrentChainIndex(GroupId groupId, int index) {
        if (!getPreferences().edit().putInt(getMyGroupCurrentChainIndexPrefKey(groupId), index).commit()) {
            Log.e("EncryptedKeyStore: failed to set my group current chain index");
        }
    }

    public int getMyGroupCurrentChainIndex(GroupId groupId) {
        return getPreferences().getInt(getMyGroupCurrentChainIndexPrefKey(groupId), 0);
    }

    public void clearMyGroupCurrentChainIndex(GroupId groupId) {
        if (!getPreferences().edit().remove(getMyGroupCurrentChainIndexPrefKey(groupId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear my group current chain index");
        }
    }

    private String getPeerGroupCurrentChainIndexPrefKey(GroupId groupId, UserId peerUserId) {
        return groupId.rawId() + "/" + PREF_KEY_PEER_GROUP_CURRENT_CHAIN_INDEX + "/" + peerUserId.rawId();
    }

    public void setPeerGroupCurrentChainIndex(GroupId groupId, UserId peerUserId, int index) {
        if (!getPreferences().edit().putInt(getPeerGroupCurrentChainIndexPrefKey(groupId, peerUserId), index).commit()) {
            Log.e("EncryptedKeyStore: failed to set peer group current chain index");
        }
    }

    public int getPeerGroupCurrentChainIndex(GroupId groupId, UserId peerUserId) {
        return getPreferences().getInt(getPeerGroupCurrentChainIndexPrefKey(groupId, peerUserId), 0);
    }

    public void clearPeerGroupCurrentChainIndex(GroupId groupId, UserId peerUserId) {
        if (!getPreferences().edit().remove(getPeerGroupCurrentChainIndexPrefKey(groupId, peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear peer group current chain index");
        }
    }

    private String getMyGroupChainKeyPrefKey(GroupId groupId) {
        return groupId.rawId() + "/" + PREF_KEY_MY_GROUP_CHAIN_KEY;
    }

    public void setMyGroupChainKey(GroupId groupId, byte[] key) {
        storeBytes(getMyGroupChainKeyPrefKey(groupId), key);
    }

    public byte[] getMyGroupChainKey(GroupId groupId) {
        return retrieveBytes(getMyGroupChainKeyPrefKey(groupId));
    }

    public void clearMyGroupChainKey(GroupId groupId) {
        if (!getPreferences().edit().remove(getMyGroupChainKeyPrefKey(groupId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear my group chain key");
        }
    }

    private String getPeerGroupChainKeyPrefKey(GroupId groupId, UserId peerUserId) {
        return groupId.rawId() + "/" + PREF_KEY_PEER_GROUP_CHAIN_KEY + "/" + peerUserId.rawId();
    }

    public void setPeerGroupChainKey(GroupId groupId, UserId peerUserId, byte[] key) {
        storeBytes(getPeerGroupChainKeyPrefKey(groupId, peerUserId), key);
    }

    public byte[] getPeerGroupChainKey(GroupId groupId, UserId peerUserId) {
        return retrieveBytes(getPeerGroupChainKeyPrefKey(groupId, peerUserId));
    }

    public void clearPeerGroupChainKey(GroupId groupId, UserId peerUserId) {
        if (!getPreferences().edit().remove(getPeerGroupChainKeyPrefKey(groupId, peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear peer group chain key");
        }
    }

    private String getMyGroupSigningKeyPrefKey(GroupId groupId) {
        return groupId.rawId() + "/" + PREF_KEY_MY_GROUP_SIGNING_KEY;
    }

    public void setMyGroupSigningKey(GroupId groupId, PrivateEdECKey key) {
        storeBytes(getMyGroupSigningKeyPrefKey(groupId), key.getKeyMaterial());
    }

    public PrivateEdECKey getMyPrivateGroupSigningKey(GroupId groupId) throws CryptoException {
        try {
            return new PrivateEdECKey(retrieveBytes(getMyGroupSigningKeyPrefKey(groupId)));
        } catch (NullPointerException e) {
            throw new CryptoException("my_group_signing_key_not_found", e);
        }
    }

    public PublicEdECKey getMyPublicGroupSigningKey(GroupId groupId) throws CryptoException {
        return CryptoUtils.publicEdECKeyFromPrivate(getMyPrivateGroupSigningKey(groupId));
    }

    public void clearMyGroupSigningKey(GroupId groupId) {
        if (!getPreferences().edit().remove(getMyGroupSigningKeyPrefKey(groupId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear my group signing key");
        }
    }

    private String getPeerGroupSigningKeyPrefKey(GroupId groupId, UserId peerUserId) {
        return groupId.rawId() + "/" + PREF_KEY_PEER_GROUP_SIGNING_KEY + "/" + peerUserId.rawId();
    }

    public void setPeerGroupSigningKey(GroupId groupId, UserId peerUserId, PublicEdECKey key) {
        storeBytes(getPeerGroupSigningKeyPrefKey(groupId, peerUserId), key.getKeyMaterial());
    }

    public PublicEdECKey getPeerGroupSigningKey(GroupId groupId, UserId peerUserId) throws CryptoException {
        try {
            return new PublicEdECKey(retrieveBytes(getPeerGroupSigningKeyPrefKey(groupId, peerUserId)));
        } catch (NullPointerException e) {
            throw new CryptoException("peer_group_signing_key_not_found", e);
        }
    }

    public void clearPeerGroupSigningKey(GroupId groupId, UserId peerUserId) {
        if (!getPreferences().edit().remove(getPeerGroupSigningKeyPrefKey(groupId, peerUserId)).commit()) {
            Log.e("EncryptedKeyStore: failed to clear peer group signing key");
        }
    }

    private String getGroupFeedKeySetPrefKey(GroupId groupId, UserId peerUserId) {
        return groupId.rawId() + "/" + PREF_KEY_SKIPPED_GROUP_FEED_KEYS_SET + "/" + peerUserId.rawId();
    }

    private String getGroupFeedKeyPrefKey(GroupId groupId, UserId peerUserId, int currentChainIndex) {
        return groupId.rawId() + "/" + PREF_KEY_SKIPPED_GROUP_FEED_KEY + "/" + peerUserId.rawId() + "/" + currentChainIndex;
    }

    public byte[] removeSkippedGroupFeedKey(GroupId groupId, UserId peerUserId, int chainIndex) {
        String messageKeySetPrefKey = getGroupFeedKeySetPrefKey(groupId, peerUserId);
        Set<String> messageKeyPrefKeys = new HashSet<>(Preconditions.checkNotNull(getPreferences().getStringSet(messageKeySetPrefKey, new HashSet<>())));

        String prefKey = getGroupFeedKeyPrefKey(groupId, peerUserId, chainIndex);
        if (!messageKeyPrefKeys.remove(prefKey)) {
            Log.e("Group feed key for " + prefKey + " not found in set");
            return null;
        }

        String messageKeyString = getPreferences().getString(prefKey, null);
        if (messageKeyString == null) {
            Log.e("Failed to retrieve group feed key for " + prefKey);
            return null;
        }

        if (!getPreferences().edit().putStringSet(messageKeySetPrefKey, messageKeyPrefKeys).commit()) {
            Log.e("EncryptedKeyStore: failed to rewrite skipped group feed key set");
        }

        return stringToBytes(messageKeyString);
    }

    // TODO(jack): Clear out old keys after some threshold
    public void storeSkippedGroupFeedKey(GroupId groupId, UserId peerUserId, GroupFeedMessageKey messageKey) {
        Log.i("Storing skipped group feed key " + messageKey + " for " + groupId + " member " + peerUserId);
        String messageKeySetPrefKey = getGroupFeedKeySetPrefKey(groupId, peerUserId);
        Set<String> messageKeyPrefKeys = new HashSet<>(Preconditions.checkNotNull(getPreferences().getStringSet(messageKeySetPrefKey, new HashSet<>())));

        String keyPrefKey = getGroupFeedKeyPrefKey(groupId, peerUserId, messageKey.getCurrentChainIndex());
        messageKeyPrefKeys.add(keyPrefKey);

        if (!getPreferences().edit().putString(keyPrefKey, bytesToString(messageKey.getKeyMaterial())).putStringSet(messageKeySetPrefKey, messageKeyPrefKeys).commit()) {
            Log.e("EncryptedKeyStore: failed to store skipped group feed key");
        }
    }

    public void clearSkippedGroupFeedKeys(GroupId groupId, UserId peerUserId) {
        String messageKeySetPrefKey = getGroupFeedKeySetPrefKey(groupId, peerUserId);
        Set<String> messageKeyPrefKeys = new HashSet<>(Preconditions.checkNotNull(getPreferences().getStringSet(messageKeySetPrefKey, new HashSet<>())));

        SharedPreferences.Editor editor = getPreferences().edit();
        editor.remove(messageKeySetPrefKey);

        for (String prefKey : messageKeyPrefKeys) {
            editor.remove(prefKey);
        }

        if (!editor.commit()) {
            Log.e("EncryptedKeyStore: failed to clear skipped group feed keys");
        }
    }



    // Only private key is stored; public key can be generated from it
    private void storeCurve25519PrivateKey(String prefKey, byte[] privateKey) {
        Preconditions.checkArgument(privateKey.length == CURVE_25519_PRIVATE_KEY_LENGTH);
        storeBytes(prefKey, privateKey);
    }

    private byte[] retrieveCurve25519PrivateKey(String prefKey) {
        byte[] ret = retrieveBytes(prefKey);
        if (ret == null) {
            return null;
        }
        Preconditions.checkState(ret.length == CURVE_25519_PRIVATE_KEY_LENGTH);
        return ret;
    }

    private void storeBytes(String prefKey, byte[] bytes) {
        if (!getPreferences().edit().putString(prefKey, bytesToString(bytes)).commit()) {
            Log.e("EncryptedKeyStore: failed to store bytes");
        }
    }

    @Nullable
    private byte[] retrieveBytes(String prefKey) {
        String stored = getPreferences().getString(prefKey, null);
        return stringToBytes(stored);
    }

    private static SharedPreferences getSharedPreferences(Context context, boolean allowRecurse) {
        SharedPreferences encrypted;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            encrypted = EncryptedSharedPreferences.create(
                    context,
                    EncryptedKeyStore.ENC_PREF_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e("EncryptedKeyStore failed to get shared preferences", e);
            return null;
        } catch (IllegalArgumentException e) {
            if (allowRecurse) {
                try {
                    Thread.sleep(KEY_STORE_RETRY_WAIT_MS);
                } catch (InterruptedException ex) {
                    Log.e("EncryptedKeyStore.getSharedPreferences keystore wait interrupted");
                }
                Log.e("EncryptedKeyStore.getSharedPreferences hit illegal argument exception; retrying", e);
                encrypted = getSharedPreferences(context, false);
            } else {
                throw e;
            }
        }

        return encrypted;
    }

    private static String bytesToString(byte[] bytes) {
        Preconditions.checkArgument(bytes != null);
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private static byte[] stringToBytes(String string) {
        if (string == null) {
            return null;
        }
        return Base64.decode(string, Base64.NO_WRAP);
    }


    @SuppressLint("ApplySharedPref")
    public void clearAll() {
        appContext.get().getSharedPreferences(EncryptedKeyStore.ENC_PREF_FILE_NAME, Context.MODE_PRIVATE).edit().clear().commit();
        appContext.get().getSharedPreferences(EncryptedKeyStore.PT_PREF_FILE_NAME, Context.MODE_PRIVATE).edit().clear().commit();
    }
}
