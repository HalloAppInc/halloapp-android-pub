package com.halloapp.crypto.keys;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.annotation.Nullable;

import com.google.crypto.tink.subtle.X25519;
import com.halloapp.AppContext;
import com.halloapp.Me;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.id.UserId;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.HashSet;
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
    private static final String PT_PREF_FILE_NAME = "pt_halloapp_keys";

    private static final String PREF_KEY_MY_ED25519_IDENTITY_KEY = "my_ed25519_identity_key";
    private static final String PREF_KEY_MY_PRIVATE_SIGNED_PRE_KEY = "my_private_signed_pre_key";
    private static final String PREF_KEY_LAST_ONE_TIME_PRE_KEY_ID = "last_one_time_pre_key_id";

    private static final String PREF_KEY_ONE_TIME_PRE_KEY_ID_PREFIX = "one_time_pre_key";
    private static final String PREF_KEY_MESSAGE_KEY_PREFIX = "message_key";
    private static final String PREF_KEY_MESSAGE_KEY_SET_PREFIX = "message_key_set";
    private static final String PREF_KEY_LAST_DOWNLOAD_ATTEMPT_SUFFIX = "last_download_attempt";

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

    private static final int CURVE_25519_PRIVATE_KEY_LENGTH = 32;

    private static final int ONE_TIME_PRE_KEY_BATCH_COUNT = 20;

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
            sharedPreferences = getSharedPreferences(appContext.get());
        }
        return sharedPreferences;
    }

    public boolean getSessionAlreadySetUp(UserId peerUserId) {
        return getPreferences().getBoolean(getSessionAlreadySetUpPrefKey(peerUserId), false);
    }

    public void setSessionAlreadySetUp(UserId peerUserId, boolean downloaded) {
        getPreferences().edit().putBoolean(getSessionAlreadySetUpPrefKey(peerUserId), downloaded).apply();
    }

    public void clearSessionAlreadySetUp(UserId peerUserId) {
        getPreferences().edit().remove(getSessionAlreadySetUpPrefKey(peerUserId)).apply();
    }

    private String getSessionAlreadySetUpPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_SESSION_ALREADY_SET_UP_SUFFIX;
    }

    public long getLastDownloadAttempt(UserId peerUserId) {
        return getPreferences().getLong(getLastDownloadAttemptPrefKey(peerUserId), 0);
    }

    public void setLastDownloadAttempt(UserId peerUserId, long lastDownloadAttempt) {
        getPreferences().edit().putLong(getLastDownloadAttemptPrefKey(peerUserId), lastDownloadAttempt).apply();
    }

    public void clearLastDownloadAttempt(UserId peerUserId) {
        getPreferences().edit().remove(getLastDownloadAttemptPrefKey(peerUserId)).apply();
    }

    private String getLastDownloadAttemptPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_LAST_DOWNLOAD_ATTEMPT_SUFFIX;
    }

    public boolean getPeerResponded(UserId peerUserId) {
        return getPreferences().getBoolean(getPeerRespondedPrefKey(peerUserId), false);
    }

    public void setPeerResponded(UserId peerUserId, boolean responded) {
        getPreferences().edit().putBoolean(getPeerRespondedPrefKey(peerUserId), responded).apply();
    }

    public void clearPeerResponded(UserId peerUserId) {
        getPreferences().edit().remove(getPeerRespondedPrefKey(peerUserId)).apply();
    }

    private String getPeerRespondedPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_RESPONDED_SUFFIX;
    }

    public void generateClientPrivateKeys() {
        setMyEd25519IdentityKey(CryptoUtils.generateEd25519KeyPair());
        setMyPrivateSignedPreKey(X25519.generatePrivateKey());
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

    public PrivateXECKey getMyPrivateX25519IdentityKey() {
        return CryptoUtils.convertPrivateEdToX(getMyPrivateEd25519IdentityKey());
    }

    private void setMyPrivateSignedPreKey(byte[] key) {
        storeCurve25519PrivateKey(PREF_KEY_MY_PRIVATE_SIGNED_PRE_KEY, key);
    }

    public PrivateXECKey getMyPrivateSignedPreKey() {
        return new PrivateXECKey(getMyPrivateSignedPreKeyInternal());
    }

    private byte[] getMyPrivateSignedPreKeyInternal() {
        return retrieveCurve25519PrivateKey(PREF_KEY_MY_PRIVATE_SIGNED_PRE_KEY);
    }

    public PublicXECKey getMyPublicSignedPreKey() {
        try {
            return XECKey.publicFromPrivate(getMyPrivateSignedPreKey());
        } catch (InvalidKeyException e) {
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
            } catch (InvalidKeyException e) {
                Log.w("Invalid X25519 private key for conversion", e);
            }
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
        getPreferences().edit().remove(prefKey).apply();
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

        getPreferences().edit().putStringSet(messageKeySetPrefKey, messageKeyPrefKeys).apply();

        return stringToBytes(messageKeyString);
    }

    // TODO(jack): Clear out old keys after some threshold
    public void storeSkippedMessageKey(UserId peerUserId, MessageKey messageKey) {
        Log.i("Storing skipped message key " + messageKey + " for user " + peerUserId);
        String messageKeySetPrefKey = getMessageKeySetPrefKey(peerUserId);
        Set<String> messageKeyPrefKeys = new HashSet<>(Preconditions.checkNotNull(getPreferences().getStringSet(messageKeySetPrefKey, new HashSet<>())));

        String keyPrefKey = getMessageKeyPrefKey(peerUserId, messageKey.getEphemeralKeyId(), messageKey.getCurrentChainIndex());
        messageKeyPrefKeys.add(keyPrefKey);

        getPreferences().edit()
                .putString(keyPrefKey, bytesToString(messageKey.getKeyMaterial()))
                .putStringSet(messageKeySetPrefKey, messageKeyPrefKeys)
                .apply();
    }

    public void clearSkippedMessageKeys(UserId peerUserId) {
        String messageKeySetPrefKey = getMessageKeySetPrefKey(peerUserId);
        Set<String> messageKeyPrefKeys = new HashSet<>(Preconditions.checkNotNull(getPreferences().getStringSet(messageKeySetPrefKey, new HashSet<>())));

        SharedPreferences.Editor editor = getPreferences().edit();
        editor.remove(messageKeySetPrefKey);

        for (String prefKey : messageKeyPrefKeys) {
            editor.remove(prefKey);
        }

        editor.apply();
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

    public PublicXECKey getPeerPublicIdentityKey(UserId peerUserId) {
        return new PublicXECKey(retrieveBytes(getPeerPublicIdentityKeyPrefKey(peerUserId)));
    }

    public void clearPeerPublicIdentityKey(UserId peerUserId) {
        getPreferences().edit().remove(getPeerPublicIdentityKeyPrefKey(peerUserId)).apply();
    }

    public String getPeerPublicIdentityKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_IDENTITY_KEY_SUFFIX;
    }

    public void setPeerSignedPreKey(UserId peerUserId, PublicXECKey key) {
        storeBytes(getPeerSignedPreKeyPrefKey(peerUserId), key.getKeyMaterial());
    }

    public PublicXECKey getPeerSignedPreKey(UserId peerUserId) {
        return new PublicXECKey(retrieveBytes(getPeerSignedPreKeyPrefKey(peerUserId)));
    }

    public void clearPeerSignedPreKey(UserId peerUserId) {
        getPreferences().edit().remove(getPeerSignedPreKeyPrefKey(peerUserId)).apply();
    }

    private String getPeerSignedPreKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_SIGNED_PRE_KEY_SUFFIX;
    }

    public void setPeerOneTimePreKey(UserId peerUserId, PublicXECKey key) {
        storeBytes(getPeerOneTimePreKeyPrefKey(peerUserId), key.getKeyMaterial());
    }

    public PublicXECKey getPeerOneTimePreKey(UserId peerUserId) {
        return new PublicXECKey(retrieveBytes(getPeerOneTimePreKeyPrefKey(peerUserId)));
    }

    public void clearPeerOneTimePreKey(UserId peerUserId) {
        getPreferences().edit().remove(getPeerOneTimePreKeyPrefKey(peerUserId)).apply();
    }

    private String getPeerOneTimePreKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_ONE_TIME_PRE_KEY_SUFFIX;
    }

    public void setPeerOneTimePreKeyId(UserId peerUserId, int id) {
        getPreferences().edit().putInt(getPeerOneTimePreKeyIdPrefKey(peerUserId), id).apply();
    }

    public Integer getPeerOneTimePreKeyId(UserId peerUserId) {
        int ret = getPreferences().getInt(getPeerOneTimePreKeyIdPrefKey(peerUserId), -1);
        if (ret == -1) {
            return null;
        }
        return ret;
    }

    public void clearPeerOneTimePreKeyId(UserId peerUserId) {
        getPreferences().edit().remove(getPeerOneTimePreKeyIdPrefKey(peerUserId)).apply();
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
        getPreferences().edit().remove(getRootKeyPrefKey(peerUserId)).apply();
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
        getPreferences().edit().remove(getOutboundChainKeyPrefKey(peerUserId)).apply();
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
        getPreferences().edit().remove(getInboundChainKeyPrefKey(peerUserId)).apply();
    }

    private String getInboundChainKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_CHAIN_KEY_SUFFIX;
    }

    public void setInboundEphemeralKey(UserId peerUserId, PublicXECKey key) {
        storeCurve25519PrivateKey(getInboundEphemeralKeyPrefKey(peerUserId), key.getKeyMaterial());
    }

    public PublicXECKey getInboundEphemeralKey(UserId peerUserId) {
        return new PublicXECKey(retrieveCurve25519PrivateKey(getInboundEphemeralKeyPrefKey(peerUserId)));
    }

    public void clearInboundEphemeralKey(UserId peerUserId) {
        getPreferences().edit().remove(getInboundEphemeralKeyPrefKey(peerUserId)).apply();
    }

    private String getInboundEphemeralKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_EPHEMERAL_KEY_SUFFIX;
    }

    public void setOutboundEphemeralKey(UserId peerUserId, PrivateXECKey key) {
        storeCurve25519PrivateKey(getOutboundEphemeralKeyPrefKey(peerUserId), key.getKeyMaterial());
    }

    public PrivateXECKey getOutboundEphemeralKey(UserId peerUserId) {
        return new PrivateXECKey(retrieveCurve25519PrivateKey(getOutboundEphemeralKeyPrefKey(peerUserId)));
    }

    public void clearOutboundEphemeralKey(UserId peerUserId) {
        getPreferences().edit().remove(getOutboundEphemeralKeyPrefKey(peerUserId)).apply();
    }

    private String getOutboundEphemeralKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_OUTBOUND_EPHEMERAL_KEY_SUFFIX;
    }

    public void setInboundEphemeralKeyId(UserId peerUserId, int id) {
        getPreferences().edit().putInt(getInboundEphemeralKeyIdPrefKey(peerUserId), id).apply();
    }

    public int getInboundEphemeralKeyId(UserId peerUserId) {
        return getPreferences().getInt(getInboundEphemeralKeyIdPrefKey(peerUserId), -1);
    }

    public void clearInboundEphemeralKeyId(UserId peerUserId) {
        getPreferences().edit().remove(getInboundEphemeralKeyIdPrefKey(peerUserId)).apply();
    }

    private String getInboundEphemeralKeyIdPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_EPHEMERAL_KEY_ID_SUFFIX;
    }

    public void setOutboundEphemeralKeyId(UserId peerUserId, int id) {
        getPreferences().edit().putInt(getOutboundEphemeralKeyIdPrefKey(peerUserId), id).apply();
    }

    public int getOutboundEphemeralKeyId(UserId peerUserId) {
        return getPreferences().getInt(getOutboundEphemeralKeyIdPrefKey(peerUserId), -1);
    }

    public void clearOutboundEphemeralKeyId(UserId peerUserId) {
        getPreferences().edit().remove(getOutboundEphemeralKeyIdPrefKey(peerUserId)).apply();
    }

    private String getOutboundEphemeralKeyIdPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_OUTBOUND_EPHEMERAL_KEY_ID_SUFFIX;
    }

    public void setInboundPreviousChainLength(UserId peerUserId, int len) {
        getPreferences().edit().putInt(getInboundPreviousChainLengthPrefKey(peerUserId), len).apply();
    }

    public int getInboundPreviousChainLength(UserId peerUserId) {
        return getPreferences().getInt(getInboundPreviousChainLengthPrefKey(peerUserId), 0);
    }

    public void clearInboundPreviousChainLength(UserId peerUserId) {
        getPreferences().edit().remove(getInboundPreviousChainLengthPrefKey(peerUserId)).apply();
    }

    private String getInboundPreviousChainLengthPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_PREVIOUS_CHAIN_LENGTH_SUFFIX;
    }

    public void setOutboundPreviousChainLength(UserId peerUserId, int len) {
        getPreferences().edit().putInt(getOutboundPreviousChainLengthPrefKey(peerUserId), len).apply();
    }

    public int getOutboundPreviousChainLength(UserId peerUserId) {
        return getPreferences().getInt(getOutboundPreviousChainLengthPrefKey(peerUserId), 0);
    }

    public void clearOutboundPreviousChainLength(UserId peerUserId) {
        getPreferences().edit().remove(getOutboundPreviousChainLengthPrefKey(peerUserId)).apply();
    }

    private String getOutboundPreviousChainLengthPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_OUTBOUND_PREVIOUS_CHAIN_LENGTH_SUFFIX;
    }

    public void setInboundCurrentChainIndex(UserId peerUserId, int index) {
        getPreferences().edit().putInt(getInboundCurrentChainIndexPrefKey(peerUserId), index).apply();
    }

    public int getInboundCurrentChainIndex(UserId peerUserId) {
        return getPreferences().getInt(getInboundCurrentChainIndexPrefKey(peerUserId), 0);
    }

    public void clearInboundCurrentChainIndex(UserId peerUserId) {
        getPreferences().edit().remove(getInboundCurrentChainIndexPrefKey(peerUserId)).apply();
    }

    private String getInboundCurrentChainIndexPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_CURRENT_CHAIN_INDEX_SUFFIX;
    }

    public void setOutboundCurrentChainIndex(UserId peerUserId, int index) {
        getPreferences().edit().putInt(getOutboundCurrentChainIndexPrefKey(peerUserId), index).apply();
    }

    public int getOutboundCurrentChainIndex(UserId peerUserId) {
        return getPreferences().getInt(getOutboundCurrentChainIndexPrefKey(peerUserId), 0);
    }

    public void clearOutboundCurrentChainIndex(UserId peerUserId) {
        getPreferences().edit().remove(getOutboundCurrentChainIndexPrefKey(peerUserId)).apply();
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
        getPreferences().edit().remove(getOutboundTeardownKeyPrefKey(peerUserId)).apply();
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
        getPreferences().edit().remove(getInboundTeardownKeyPrefKey(peerUserId)).apply();
    }

    private String getInboundTeardownKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_TEARDOWN_KEY;
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
        getPreferences().edit().putString(prefKey, bytesToString(bytes)).apply();
    }

    @Nullable
    private byte[] retrieveBytes(String prefKey) {
        String stored = getPreferences().getString(prefKey, null);
        return stringToBytes(stored);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        // TODO(jack): bring back EncryptedSharedPreferences once Google fixes the androidx security-crypto library
        return context.getSharedPreferences(EncryptedKeyStore.PT_PREF_FILE_NAME, Context.MODE_PRIVATE);
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
