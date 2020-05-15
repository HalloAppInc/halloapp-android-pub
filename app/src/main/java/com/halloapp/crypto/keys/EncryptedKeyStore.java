package com.halloapp.crypto.keys;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Base64;

import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.crypto.tink.subtle.X25519;
import com.halloapp.contacts.UserId;
import com.halloapp.crypto.CryptoUtil;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;

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

    private static final String PREF_KEY_MY_ED25519_IDENTITY_KEY = "my_ed25519_identity_key";
    private static final String PREF_KEY_MY_PRIVATE_SIGNED_PRE_KEY = "my_private_signed_pre_key";
    private static final String PREF_KEY_LAST_ONE_TIME_PRE_KEY_ID = "last_one_time_pre_key_id";
    private static final String PREF_KEY_KEYS_UPLOADED = "keys_uploaded";
    private static final String PREF_KEY_MESSAGE_KEY_SET = "message_key_set";

    private static final String PREF_KEY_ONE_TIME_PRE_KEY_ID_PREFIX = "one_time_pre_key";
    private static final String PREF_KEY_MESSAGE_KEY_PREFIX = "message_key";

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

    private static final int CURVE_25519_PRIVATE_KEY_LENGTH = 32;

    private static final int ONE_TIME_PRE_KEY_BATCH_COUNT = 20;

    // TODO(jack): Try moving away from SharedPreferences to avoid Strings in memory with key material
    private SharedPreferences sharedPreferences;

    private static EncryptedKeyStore instance;

    public static EncryptedKeyStore getInstance() {
        if (instance == null) {
            synchronized (EncryptedKeyStore.class) {
                if (instance == null) {
                    instance = new EncryptedKeyStore();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            try {
                sharedPreferences = getSharedPreferences(context, ENC_PREF_FILE_NAME);
            } catch (Exception e) {
                Log.e("Failed to init encrypted key store", e);
            }
        });
    }

    public boolean getKeysUploaded() {
        return sharedPreferences.getBoolean(PREF_KEY_KEYS_UPLOADED, false);
    }

    public void setKeysUploaded(boolean uploaded) {
        sharedPreferences.edit().putBoolean(PREF_KEY_KEYS_UPLOADED, uploaded).apply();
    }

    public boolean getSessionAlreadySetUp(UserId peerUserId) {
        return sharedPreferences.getBoolean(getSessionAlreadySetUpPrefKey(peerUserId), false);
    }

    public void setSessionAlreadySetUp(UserId peerUserId, boolean downloaded) {
        sharedPreferences.edit().putBoolean(getSessionAlreadySetUpPrefKey(peerUserId), downloaded).apply();
    }

    private String getSessionAlreadySetUpPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_SESSION_ALREADY_SET_UP_SUFFIX;
    }

    public boolean getPeerResponded(UserId peerUserId) {
        return sharedPreferences.getBoolean(getPeerRespondedPrefKey(peerUserId), false);
    }

    public void setPeerResponded(UserId peerUserId, boolean responded) {
        sharedPreferences.edit().putBoolean(getPeerRespondedPrefKey(peerUserId), responded).apply();
    }

    private String getPeerRespondedPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_RESPONDED_SUFFIX;
    }

    public void generateClientPrivateKeys() {
        setMyEd25519IdentityKey(CryptoUtil.generateEd25519KeyPair());
        setMyPrivateSignedPreKey(X25519.generatePrivateKey());
    }



    private void setMyEd25519IdentityKey(byte[] key) {
        storeBytes(PREF_KEY_MY_ED25519_IDENTITY_KEY, key);
    }

    private byte[] getMyEd25519IdentityKey() {
        return retrieveBytes(PREF_KEY_MY_ED25519_IDENTITY_KEY);
    }

    public PublicEdECKey getMyPublicEd25519IdentityKey() {
        return new PublicEdECKey(Arrays.copyOfRange(getMyEd25519IdentityKey(), 0, 32));
    }

    public PrivateEdECKey getMyPrivateEd25519IdentityKey() {
        return new PrivateEdECKey(Arrays.copyOfRange(getMyEd25519IdentityKey(), 32, 96));
    }

    public PrivateXECKey getMyPrivateX25519IdentityKey() {
        return CryptoUtil.convertPrivateEdToX(getMyPrivateEd25519IdentityKey());
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
        int startId = sharedPreferences.getInt(PREF_KEY_LAST_ONE_TIME_PRE_KEY_ID, 1);

        Set<OneTimePreKey> ret = new HashSet<>();
        for (int i=0; i<ONE_TIME_PRE_KEY_BATCH_COUNT; i++) {
            int id = startId + i;
            PrivateXECKey privateKey = XECKey.generatePrivateKey();
            storeCurve25519PrivateKey(getOneTimePreKeyPrefKey(id), privateKey.getKeyMaterial());
            try {
                OneTimePreKey otpk = new OneTimePreKey(XECKey.publicFromPrivate(privateKey), id);
                ret.add(otpk);
            } catch (InvalidKeyException e) {
                Log.w("Invalid X25519 private key for conversion", e);
            }
        }
        return ret;
    }

    public PrivateXECKey removeOneTimePreKeyById(int id) {
        String prefKey = getOneTimePreKeyPrefKey(id);
        PrivateXECKey ret = new PrivateXECKey(retrieveCurve25519PrivateKey(prefKey));
        sharedPreferences.edit().remove(prefKey).apply();
        return ret;
    }

    private String getOneTimePreKeyPrefKey(int id) {
        return PREF_KEY_ONE_TIME_PRE_KEY_ID_PREFIX + "/" + id;
    }

    public byte[] removeSkippedMessageKey(UserId peerUserId, int ephemeralKeyId, int chainIndex) {
        Set<String> messageKeyPrefKeys = sharedPreferences.getStringSet(PREF_KEY_MESSAGE_KEY_SET, new HashSet<>());

        String prefKey = getMessageKeyPrefKey(peerUserId, ephemeralKeyId, chainIndex);
        if (!messageKeyPrefKeys.remove(prefKey)) {
            Log.e("Message key for " + prefKey + " not found in set");
            return null;
        }

        String messageKeyString = sharedPreferences.getString(prefKey, null);
        if (messageKeyString == null) {
            Log.e("Failed to retrieve message key for " + prefKey);
            return null;
        }

        sharedPreferences.edit().putStringSet(PREF_KEY_MESSAGE_KEY_SET, messageKeyPrefKeys).apply();

        return stringToBytes(messageKeyString);
    }

    // TODO(jack): Clear out old keys after some threshold
    public void storeSkippedMessageKey(UserId peerUserId, MessageKey messageKey) {
        Log.i("Storing skipped message key " + messageKey + " for user " + peerUserId);
        Set<String> messageKeyPrefKeys = sharedPreferences.getStringSet(PREF_KEY_MESSAGE_KEY_SET, new HashSet<>());

        String keyPrefKey = getMessageKeyPrefKey(peerUserId, messageKey.getEphemeralKeyId(), messageKey.getCurrentChainIndex());
        messageKeyPrefKeys.add(keyPrefKey);

        sharedPreferences.edit()
                .putString(keyPrefKey, bytesToString(messageKey.getKeyMaterial()))
                .putStringSet(PREF_KEY_MESSAGE_KEY_SET, messageKeyPrefKeys)
                .apply();
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

    public String getPeerPublicIdentityKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_IDENTITY_KEY_SUFFIX;
    }

    public void setPeerSignedPreKey(UserId peerUserId, PublicXECKey key) {
        storeBytes(getPeerSignedPreKeyPrefKey(peerUserId), key.getKeyMaterial());
    }

    public PublicXECKey getPeerSignedPreKey(UserId peerUserId) {
        return new PublicXECKey(retrieveBytes(getPeerSignedPreKeyPrefKey(peerUserId)));
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

    private String getPeerOneTimePreKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_ONE_TIME_PRE_KEY_SUFFIX;
    }

    public void setPeerOneTimePreKeyId(UserId peerUserId, int id) {
        sharedPreferences.edit().putInt(getPeerOneTimePreKeyIdPrefKey(peerUserId), id).apply();
    }

    public Integer getPeerOneTimePreKeyId(UserId peerUserId) {
        int ret = sharedPreferences.getInt(getPeerOneTimePreKeyIdPrefKey(peerUserId), -1);
        if (ret == -1) {
            return null;
        }
        return ret;
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

    private String getRootKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_ROOT_KEY_SUFFIX;
    }

    public void setOutboundChainKey(UserId peerUserId, byte[] key) {
        storeBytes(getOutboundChainKeyPrefKey(peerUserId), key);
    }

    public byte[] getOutboundChainKey(UserId peerUserId) {
        return retrieveBytes(getOutboundChainKeyPrefKey(peerUserId));
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

    private String getInboundChainKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_CHAIN_KEY_SUFFIX;
    }

    public void setInboundEphemeralKey(UserId peerUserId, PublicXECKey key) {
        storeCurve25519PrivateKey(getInboundEphemeralKeyPrefKey(peerUserId), key.getKeyMaterial());
    }

    public PublicXECKey getInboundEphemeralKey(UserId peerUserId) {
        return new PublicXECKey(retrieveCurve25519PrivateKey(getInboundEphemeralKeyPrefKey(peerUserId)));
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

    private String getOutboundEphemeralKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_OUTBOUND_EPHEMERAL_KEY_SUFFIX;
    }

    public void setInboundEphemeralKeyId(UserId peerUserId, int id) {
        sharedPreferences.edit().putInt(getInboundEphemeralKeyIdPrefKey(peerUserId), id).apply();
    }

    public int getInboundEphemeralKeyId(UserId peerUserId) {
        return sharedPreferences.getInt(getInboundEphemeralKeyIdPrefKey(peerUserId), -1);
    }

    private String getInboundEphemeralKeyIdPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_EPHEMERAL_KEY_ID_SUFFIX;
    }

    public void setOutboundEphemeralKeyId(UserId peerUserId, int id) {
        sharedPreferences.edit().putInt(getOutboundEphemeralKeyIdPrefKey(peerUserId), id).apply();
    }

    public int getOutboundEphemeralKeyId(UserId peerUserId) {
        return sharedPreferences.getInt(getOutboundEphemeralKeyIdPrefKey(peerUserId), -1);
    }

    private String getOutboundEphemeralKeyIdPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_OUTBOUND_EPHEMERAL_KEY_ID_SUFFIX;
    }

    public void setInboundPreviousChainLength(UserId peerUserId, int len) {
        sharedPreferences.edit().putInt(getInboundPreviousChainLengthPrefKey(peerUserId), len).apply();
    }

    public int getInboundPreviousChainLength(UserId peerUserId) {
        return sharedPreferences.getInt(getInboundPreviousChainLengthPrefKey(peerUserId), 0);
    }

    private String getInboundPreviousChainLengthPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_PREVIOUS_CHAIN_LENGTH_SUFFIX;
    }

    public void setOutboundPreviousChainLength(UserId peerUserId, int len) {
        sharedPreferences.edit().putInt(getOutboundPreviousChainLengthPrefKey(peerUserId), len).apply();
    }

    public int getOutboundPreviousChainLength(UserId peerUserId) {
        return sharedPreferences.getInt(getOutboundPreviousChainLengthPrefKey(peerUserId), 0);
    }

    private String getOutboundPreviousChainLengthPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_OUTBOUND_PREVIOUS_CHAIN_LENGTH_SUFFIX;
    }

    public void setInboundCurrentChainIndex(UserId peerUserId, int index) {
        sharedPreferences.edit().putInt(getInboundCurrentChainIndexPrefKey(peerUserId), index).apply();
    }

    public int getInboundCurrentChainIndex(UserId peerUserId) {
        return sharedPreferences.getInt(getInboundCurrentChainIndexPrefKey(peerUserId), 0);
    }

    private String getInboundCurrentChainIndexPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_CURRENT_CHAIN_INDEX_SUFFIX;
    }

    public void setOutboundCurrentChainIndex(UserId peerUserId, int index) {
        sharedPreferences.edit().putInt(getOutboundCurrentChainIndexPrefKey(peerUserId), index).apply();
    }

    public int getOutboundCurrentChainIndex(UserId peerUserId) {
        return sharedPreferences.getInt(getOutboundCurrentChainIndexPrefKey(peerUserId), 0);
    }

    private String getOutboundCurrentChainIndexPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_OUTBOUND_CURRENT_CHAIN_INDEX_SUFFIX;
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
        sharedPreferences.edit().putString(prefKey, bytesToString(bytes)).apply();
    }

    @Nullable
    private byte[] retrieveBytes(String prefKey) {
        String stored = sharedPreferences.getString(prefKey, null);
        return stringToBytes(stored);
    }

    private static SharedPreferences getSharedPreferences(Context context, String fileName) throws Exception {
        if (Build.VERSION.SDK_INT >= 23) {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC); // TODO(jack): Is default master key okay?
            return EncryptedSharedPreferences.create(
                    fileName,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } else {
            // TODO(jack): remove once androidx.security supports back to API 21
            return context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        }
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


    public void clearAll() {
        sharedPreferences.edit().clear().commit();
    }
}
