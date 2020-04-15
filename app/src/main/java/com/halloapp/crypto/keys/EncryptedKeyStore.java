package com.halloapp.crypto.keys;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;

import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.crypto.tink.subtle.X25519;
import com.halloapp.contacts.UserId;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;

import java.security.InvalidKeyException;
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

    private static final String PREF_KEY_MY_PRIVATE_IDENTITY_KEY = "my_private_identity_key";
    private static final String PREF_KEY_MY_PRIVATE_SIGNED_PRE_KEY = "my_private_signed_pre_key";
    private static final String PREF_KEY_LAST_ONE_TIME_PRE_KEY_ID = "last_one_time_pre_key_id";
    private static final String PREF_KEY_KEYS_UPLOADED = "keys_uploaded";

    private static final String PREF_KEY_ONE_TIME_PRE_KEY_ID_PREFIX = "one_time_pre_key";

    private static final String PREF_KEY_SESSION_ALREADY_SET_UP_SUFFIX = "session_already_set_up";
    private static final String PREF_KEY_PEER_RESPONDED_SUFFIX = "peer_responded";
    private static final String PREF_KEY_PEER_IDENTITY_KEY_SUFFIX = "peer_identity_key";
    private static final String PREF_KEY_PEER_SIGNED_PRE_KEY_SUFFIX = "peer_signed_pre_key";
    private static final String PREF_KEY_PEER_ONE_TIME_PRE_KEY_SUFFIX = "peer_one_time_pre_key";
    private static final String PREF_KEY_PEER_ONE_TIME_PRE_KEY_ID_SUFFIX = "peer_one_time_pre_key_id";
    private static final String PREF_KEY_ROOT_KEY_SUFFIX = "root_key";
    private static final String PREF_KEY_OUTBOUND_CHAIN_KEY_SUFFIX = "outbound_chain_key";
    private static final String PREF_KEY_INBOUND_CHAIN_KEY_SUFFIX = "inbound_chain_key";
    private static final String PREF_KEY_LAST_RECEIVED_EPHEMERAL_KEY_SUFFIX = "last_received_ephemeral_key";
    private static final String PREF_KEY_LAST_SENT_EPHEMERAL_KEY_SUFFIX = "last_sent_ephemeral_key";
    private static final String PREF_KEY_LAST_RECEIVED_EPHEMERAL_KEY_ID_SUFFIX = "last_received_ephemeral_key_id";
    private static final String PREF_KEY_LAST_SENT_EPHEMERAL_KEY_ID_SUFFIX = "last_sent_ephemeral_key_id";

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
        setMyPrivateIdentityKey(X25519.generatePrivateKey());
        setMyPrivateSignedPreKey(X25519.generatePrivateKey());
    }



    private void setMyPrivateIdentityKey(byte[] key) {
        storeCurve25519PrivateKey(PREF_KEY_MY_PRIVATE_IDENTITY_KEY, key);
    }

    public PrivateECKey getMyPrivateIdentityKey() {
        return new PrivateECKey(getMyPrivateIdentityKeyInternal());
    }

    private byte[] getMyPrivateIdentityKeyInternal() {
        return retrieveCurve25519PrivateKey(PREF_KEY_MY_PRIVATE_IDENTITY_KEY);
    }

    public PublicECKey getMyPublicIdentityKey() {
        return new PublicECKey(getMyPublicIdentityKeyInternal());
    }

    private byte[] getMyPublicIdentityKeyInternal() {
        try {
            return X25519.publicFromPrivate(getMyPrivateIdentityKeyInternal());
        } catch (InvalidKeyException e) {
            Log.w("Failed to get public identity key", e);
        }
        return null;
    }

    private void setMyPrivateSignedPreKey(byte[] key) {
        storeCurve25519PrivateKey(PREF_KEY_MY_PRIVATE_SIGNED_PRE_KEY, key);
    }

    public PrivateECKey getMyPrivateSignedPreKey() {
        return new PrivateECKey(getMyPrivateSignedPreKeyInternal());
    }

    private byte[] getMyPrivateSignedPreKeyInternal() {
        return retrieveCurve25519PrivateKey(PREF_KEY_MY_PRIVATE_SIGNED_PRE_KEY);
    }

    public PublicECKey getMyPublicSignedPreKey() {
        return new PublicECKey(getMyPublicSignedPreKeyInternal());
    }

    // TODO(jack): Switch to ECKey.publicFromPrivate
    private byte[] getMyPublicSignedPreKeyInternal() {
        try {
            return X25519.publicFromPrivate(getMyPrivateSignedPreKeyInternal());
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
            PrivateECKey privateKey = ECKey.generatePrivateKey();
            storeCurve25519PrivateKey(getOneTimePreKeyPrefKey(id), privateKey.getKeyMaterial());
            try {
                OneTimePreKey otpk = new OneTimePreKey(ECKey.publicFromPrivate(privateKey), id);
                ret.add(otpk);
            } catch (InvalidKeyException e) {
                Log.w("Invalid X25519 private key for conversion", e);
            }
        }
        return ret;
    }

    public PrivateECKey removeOneTimePreKeyById(int id) {
        String prefKey = getOneTimePreKeyPrefKey(id);
        PrivateECKey ret = new PrivateECKey(retrieveCurve25519PrivateKey(prefKey));
        sharedPreferences.edit().remove(prefKey).apply();
        return ret;
    }

    private String getOneTimePreKeyPrefKey(int id) {
        return PREF_KEY_ONE_TIME_PRE_KEY_ID_PREFIX + "/" + id;
    }

    public void setPeerPublicIdentityKey(UserId peerUserId, PublicECKey key) {
        storeBytes(getPeerPublicIdentityKeyPrefKey(peerUserId), key.getKeyMaterial());
    }

    public PublicECKey getPeerPublicIdentityKey(UserId peerUserId) {
        return new PublicECKey(retrieveBytes(getPeerPublicIdentityKeyPrefKey(peerUserId)));
    }

    public String getPeerPublicIdentityKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_IDENTITY_KEY_SUFFIX;
    }

    public void setPeerSignedPreKey(UserId peerUserId, PublicECKey key) {
        storeBytes(getPeerSignedPreKeyPrefKey(peerUserId), key.getKeyMaterial());
    }

    public PublicECKey getPeerSignedPreKey(UserId peerUserId) {
        return new PublicECKey(retrieveBytes(getPeerSignedPreKeyPrefKey(peerUserId)));
    }

    private String getPeerSignedPreKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_SIGNED_PRE_KEY_SUFFIX;
    }

    public void setPeerOneTimePreKey(UserId peerUserId, PublicECKey key) {
        storeBytes(getPeerOneTimePreKeyPrefKey(peerUserId), key.getKeyMaterial());
    }

    public PublicECKey getPeerOneTimePreKey(UserId peerUserId) {
        return new PublicECKey(retrieveBytes(getPeerOneTimePreKeyPrefKey(peerUserId)));
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

    public void setLastReceivedEphemeralKey(UserId peerUserId, PublicECKey key) {
        storeCurve25519PrivateKey(getLastReceivedEphemeralKeyPrefKey(peerUserId), key.getKeyMaterial());
    }

    public PublicECKey getLastReceivedEphemeralKey(UserId peerUserId) {
        return new PublicECKey(retrieveCurve25519PrivateKey(getLastReceivedEphemeralKeyPrefKey(peerUserId)));
    }

    private String getLastReceivedEphemeralKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_LAST_RECEIVED_EPHEMERAL_KEY_SUFFIX;
    }

    public void setLastSentEphemeralKey(UserId peerUserId, PrivateECKey key) {
        storeCurve25519PrivateKey(getLastSentEphemeralKeyPrefKey(peerUserId), key.getKeyMaterial());
    }

    public PrivateECKey getLastSentEphemeralKey(UserId peerUserId) {
        return new PrivateECKey(retrieveCurve25519PrivateKey(getLastSentEphemeralKeyPrefKey(peerUserId)));
    }

    private String getLastSentEphemeralKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_LAST_SENT_EPHEMERAL_KEY_SUFFIX;
    }

    public void setLastReceivedEphemeralKeyId(UserId peerUserId, int id) {
        sharedPreferences.edit().putInt(getLastReceivedEphemeralKeyIdPrefKey(peerUserId), id).apply();
    }

    public int getLastReceivedEphemeralKeyId(UserId peerUserId) {
        return sharedPreferences.getInt(getLastReceivedEphemeralKeyIdPrefKey(peerUserId), -1);
    }

    private String getLastReceivedEphemeralKeyIdPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_LAST_RECEIVED_EPHEMERAL_KEY_ID_SUFFIX;
    }

    public void setLastSentEphemeralKeyId(UserId peerUserId, int id) {
        sharedPreferences.edit().putInt(getLastSentEphemeralKeyIdPrefKey(peerUserId), id).apply();
    }

    public int getLastSentEphemeralKeyId(UserId peerUserId) {
        return sharedPreferences.getInt(getLastSentEphemeralKeyIdPrefKey(peerUserId), -1);
    }

    private String getLastSentEphemeralKeyIdPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_LAST_SENT_EPHEMERAL_KEY_ID_SUFFIX;
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
        String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC); // TODO(jack): Is default master key okay?
        return EncryptedSharedPreferences.create(
                fileName,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
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
