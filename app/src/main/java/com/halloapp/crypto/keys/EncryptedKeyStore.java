package com.halloapp.crypto.keys;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.AppContext;
import com.halloapp.Me;
import com.halloapp.crypto.CryptoByteUtils;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.group.GroupFeedMessageKey;
import com.halloapp.crypto.home.HomeFeedPostMessageKey;
import com.halloapp.crypto.signal.SignalMessageKey;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.IdentityKey;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Tink's KeysetManager currently does not allow importing of keys (this is by design,
 * to reduce chances of key exposure or improperly generated keys). However, we still need
 * to store keys that are generated in ways that Tink does not support, so we need to
 * handle storing these ourselves.
 *
 * TODO: Signed pre key key rotation
 */
public class EncryptedKeyStore {

    private static final String ENC_PREF_FILE_NAME = "halloapp_keys";
    private static final String PT_PREF_FILE_NAME = "pt_halloapp_keys"; // TODO: delete file

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

    private static final String PREF_KEY_HOME_SEND_ALREADY_SET_UP = "home_send_already_set_up";
    private static final String PREF_KEY_MY_HOME_CURRENT_CHAIN_INDEX = "my_home_current_chain_index";
    private static final String PREF_KEY_PEER_HOME_CURRENT_CHAIN_INDEX = "peer_home_current_chain_index";
    private static final String PREF_KEY_MY_HOME_CHAIN_KEY = "my_home_chain_key";
    private static final String PREF_KEY_PEER_HOME_CHAIN_KEY = "peer_home_chain_key";
    private static final String PREF_KEY_MY_HOME_SIGNING_KEY = "my_home_signing_key";
    private static final String PREF_KEY_PEER_HOME_SIGNING_KEY = "peer_home_signing_key";
    private static final String PREF_KEY_SKIPPED_HOME_FEED_KEYS_SET = "skipped_home_feed_keys_set";
    private static final String PREF_KEY_SKIPPED_HOME_FEED_KEY = "skipped_home_feed_key";
    private static final String PREF_KEY_NEEDS_STATE_UID_SET = "needs_state_uid_set";

    private static final String PREF_KEY_HOME_AUDIENCE_ALL_PREFIX = "home_all";
    private static final String PREF_KEY_HOME_AUDIENCE_FAVORITES_PREFIX = "home_favorites";

    private static final int CURVE_25519_PRIVATE_KEY_LENGTH = 32;

    private static final int ONE_TIME_PRE_KEY_BATCH_COUNT = 100;
    private static final int KEY_STORE_RETRY_WAIT_MS = 50;

    private static EncryptedKeyStore instance;

    // TODO: Try moving away from SharedPreferences to avoid Strings in memory with key material
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

    public Editor edit() {
        return new Editor(getPreferences().edit());
    }

    public boolean getPeerVerified(UserId peerUserId) {
        return getPreferences().getBoolean(getPeerVerifiedPrefKey(peerUserId), false);
    }

    private String getPeerVerifiedPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_VERIFIED;
    }

    public boolean getSessionAlreadySetUp(UserId peerUserId) {
        return getPreferences().getBoolean(getSessionAlreadySetUpPrefKey(peerUserId), false);
    }

    private String getSessionAlreadySetUpPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_SESSION_ALREADY_SET_UP_SUFFIX;
    }

    public long getLastDownloadAttempt(UserId peerUserId) {
        return getPreferences().getLong(getLastDownloadAttemptPrefKey(peerUserId), 0);
    }

    private String getLastDownloadAttemptPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_LAST_DOWNLOAD_ATTEMPT_SUFFIX;
    }

    public boolean getPeerResponded(UserId peerUserId) {
        return getPreferences().getBoolean(getPeerRespondedPrefKey(peerUserId), false);
    }

    private String getPeerRespondedPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_RESPONDED_SUFFIX;
    }

    public boolean clientPrivateKeysSet() {
        return getMyEd25519IdentityKey() != null;
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
        if (!clientPrivateKeysSet()) {
            Log.i("EncryptedKeyStore: Client private keys currently unset");
            return;
        }
        PublicEdECKey current = getMyPublicEd25519IdentityKey();
        PublicEdECKey previous = getMyPreviousPublicEd25519IdentityKey();
        if (previous != null) {
            Log.i("EncryptedKeyStore: Checking for identity key changes");
            if (!Arrays.equals(current.getKeyMaterial(), previous.getKeyMaterial())) {
                Log.e("EncryptedKeyStore: Previous identity key was " + Base64.encodeToString(previous.getKeyMaterial(), Base64.NO_WRAP)
                        + " but current is " + Base64.encodeToString(current.getKeyMaterial(), Base64.NO_WRAP));
                Log.sendErrorReport("Local identity key changed");
            }
        }
        edit().setMyPreviousPublicEd25519IdentityKey(current).apply();

        Me me = Me.getInstance();
        Connection.getInstance().downloadKeys(new UserId(me.getUser()))
                .onResponse(response -> {
                    try {
                        IdentityKey identityKeyProto = IdentityKey.parseFrom(response.identityKey);
                        byte[] remote = identityKeyProto.getPublicKey().toByteArray();
                        byte[] local = current.getKeyMaterial();
                        Log.i("Remote identity key: " + StringUtils.bytesToHexString(remote));
                        Log.i("Local identity key: " + StringUtils.bytesToHexString(local));
                        if (!Arrays.equals(remote, local)) {
                            Log.e("Remote and local identity key do not match");
                            Log.sendErrorReport("Local identity key does not match remote");
                        }
                    } catch (InvalidProtocolBufferException e) {
                        Log.e("Failed to parse own identity key proto", e);
                    }
                }).onError(err -> {
                    Log.w("Failed to fetch own identity key for verification", err);
                });
    }

    private PublicEdECKey getMyPreviousPublicEd25519IdentityKey() {
        byte[] bytes = retrieveBytes(PREF_KEY_MY_PREVIOUS_PUBLIC_ED25519_IK);
        if (bytes == null) {
            return null;
        }
        return new PublicEdECKey(bytes);
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
                edit().storeCurve25519PrivateKey(getOneTimePreKeyPrefKey(id), privateKey.getKeyMaterial()).apply();
                ret.add(otpk);
            } catch (CryptoException e) {
                Log.w("Invalid X25519 private key for conversion", e);
            }
        }

        getPreferences().edit().putInt(PREF_KEY_LAST_ONE_TIME_PRE_KEY_ID, startId + ONE_TIME_PRE_KEY_BATCH_COUNT).apply();

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

        edit().editor.putStringSet(messageKeySetPrefKey, messageKeyPrefKeys).apply();

        return stringToBytes(messageKeyString);
    }

    public byte[] removeSkippedHomeFeedKey(boolean favorites, UserId peerUserId, int chainIndex) {
        String messageKeySetPrefKey = getHomeFeedKeySetPrefKey(favorites, peerUserId);
        Set<String> messageKeyPrefKeys = new HashSet<>(Preconditions.checkNotNull(getPreferences().getStringSet(messageKeySetPrefKey, new HashSet<>())));

        String prefKey = getHomeFeedKeyPrefKey(favorites, peerUserId, chainIndex);
        if (!messageKeyPrefKeys.remove(prefKey)) {
            Log.e("Home feed key for " + prefKey + " not found in set");
            return null;
        }

        String messageKeyString = getPreferences().getString(prefKey, null);
        if (messageKeyString == null) {
            Log.e("Failed to retrieve home feed key for " + prefKey);
            return null;
        }

        edit().editor.putStringSet(messageKeySetPrefKey, messageKeyPrefKeys).apply();

        return stringToBytes(messageKeyString);
    }

    public List<UserId> removeAllNeedsStateUids(boolean favorites) {
        String prefKey = getNeedsStateUidSetPrefKey(favorites);
        Set<String> uidStrings = new HashSet<>(Preconditions.checkNotNull(getPreferences().getStringSet(prefKey, new HashSet<>())));
        List<UserId> ret = new ArrayList<>();
        for (String s : uidStrings) {
            ret.add(new UserId(s));
        }

        edit().editor.putStringSet(prefKey, new HashSet<>()).apply();

        return ret;
    }

    private String getMessageKeySetPrefKey(UserId peerUserId) {
        return PREF_KEY_MESSAGE_KEY_SET_PREFIX + "/" + peerUserId.rawId();
    }

    private String getMessageKeyPrefKey(UserId peerUserId, int ephemeralKeyId, int currentChainIndex) {
        return PREF_KEY_MESSAGE_KEY_PREFIX + "/" + peerUserId.rawId() + "/" + ephemeralKeyId + "/" + currentChainIndex;
    }

    public PublicEdECKey getPeerPublicIdentityKey(UserId peerUserId) throws CryptoException {
        try {
            return new PublicEdECKey(retrieveBytes(getPeerPublicIdentityKeyPrefKey(peerUserId)));
        } catch (NullPointerException e) {
            throw new CryptoException("no_key_found", e);
        }
    }

    private String getPeerPublicIdentityKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_IDENTITY_KEY_SUFFIX;
    }

    public PublicXECKey getPeerSignedPreKey(UserId peerUserId) throws CryptoException {
        return new PublicXECKey(retrieveBytes(getPeerSignedPreKeyPrefKey(peerUserId)));
    }

    private String getPeerSignedPreKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_SIGNED_PRE_KEY_SUFFIX;
    }

    public PublicXECKey getPeerOneTimePreKey(UserId peerUserId) throws CryptoException {
        return new PublicXECKey(retrieveBytes(getPeerOneTimePreKeyPrefKey(peerUserId)));
    }

    private String getPeerOneTimePreKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_ONE_TIME_PRE_KEY_SUFFIX;
    }

    public Integer getPeerOneTimePreKeyId(UserId peerUserId) {
        int ret = getPreferences().getInt(getPeerOneTimePreKeyIdPrefKey(peerUserId), -1);
        if (ret == -1) {
            return null;
        }
        return ret;
    }

    private String getPeerOneTimePreKeyIdPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_PEER_ONE_TIME_PRE_KEY_ID_SUFFIX;
    }

    public byte[] getRootKey(UserId peerUserId) {
        return retrieveBytes(getRootKeyPrefKey(peerUserId));
    }

    private String getRootKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_ROOT_KEY_SUFFIX;
    }

    public byte[] getOutboundChainKey(UserId peerUserId) {
        return retrieveBytes(getOutboundChainKeyPrefKey(peerUserId));
    }

    private String getOutboundChainKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_OUTBOUND_CHAIN_KEY_SUFFIX;
    }

    public byte[] getInboundChainKey(UserId peerUserId) {
        return retrieveBytes(getInboundChainKeyPrefKey(peerUserId));
    }

    private String getInboundChainKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_CHAIN_KEY_SUFFIX;
    }

    public PublicXECKey getInboundEphemeralKey(UserId peerUserId) throws CryptoException {
        return new PublicXECKey(retrieveCurve25519PrivateKey(getInboundEphemeralKeyPrefKey(peerUserId)));
    }

    private String getInboundEphemeralKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_EPHEMERAL_KEY_SUFFIX;
    }

    public PrivateXECKey getOutboundEphemeralKey(UserId peerUserId) throws CryptoException {
        return new PrivateXECKey(retrieveCurve25519PrivateKey(getOutboundEphemeralKeyPrefKey(peerUserId)));
    }

    private String getOutboundEphemeralKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_OUTBOUND_EPHEMERAL_KEY_SUFFIX;
    }

    public int getInboundEphemeralKeyId(UserId peerUserId) {
        return getPreferences().getInt(getInboundEphemeralKeyIdPrefKey(peerUserId), -1);
    }

    private String getInboundEphemeralKeyIdPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_EPHEMERAL_KEY_ID_SUFFIX;
    }

    public int getOutboundEphemeralKeyId(UserId peerUserId) {
        return getPreferences().getInt(getOutboundEphemeralKeyIdPrefKey(peerUserId), -1);
    }

    private String getOutboundEphemeralKeyIdPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_OUTBOUND_EPHEMERAL_KEY_ID_SUFFIX;
    }

    public int getInboundPreviousChainLength(UserId peerUserId) {
        return getPreferences().getInt(getInboundPreviousChainLengthPrefKey(peerUserId), 0);
    }

    private String getInboundPreviousChainLengthPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_PREVIOUS_CHAIN_LENGTH_SUFFIX;
    }

    public int getOutboundPreviousChainLength(UserId peerUserId) {
        return getPreferences().getInt(getOutboundPreviousChainLengthPrefKey(peerUserId), 0);
    }

    private String getOutboundPreviousChainLengthPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_OUTBOUND_PREVIOUS_CHAIN_LENGTH_SUFFIX;
    }

    public int getInboundCurrentChainIndex(UserId peerUserId) {
        return getPreferences().getInt(getInboundCurrentChainIndexPrefKey(peerUserId), 0);
    }

    private String getInboundCurrentChainIndexPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_INBOUND_CURRENT_CHAIN_INDEX_SUFFIX;
    }

    public int getOutboundCurrentChainIndex(UserId peerUserId) {
        return getPreferences().getInt(getOutboundCurrentChainIndexPrefKey(peerUserId), 0);
    }

    private String getOutboundCurrentChainIndexPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_OUTBOUND_CURRENT_CHAIN_INDEX_SUFFIX;
    }

    public byte[] getOutboundTeardownKey(UserId peerUserId) {
        return retrieveBytes(getOutboundTeardownKeyPrefKey(peerUserId));
    }

    private String getOutboundTeardownKeyPrefKey(UserId peerUserId) {
        return peerUserId.rawId() + "/" + PREF_KEY_OUTBOUND_TEARDOWN_KEY;
    }

    public byte[] getInboundTeardownKey(UserId peerUserId) {
        return retrieveBytes(getInboundTeardownKeyPrefKey(peerUserId));
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

    public boolean getGroupSendAlreadySetUp(GroupId groupId) {
        return getPreferences().getBoolean(getGroupSendAlreadySetUpPrefKey(groupId), false);
    }

    private String getMyGroupCurrentChainIndexPrefKey(GroupId groupId) {
        return groupId.rawId() + "/" + PREF_KEY_MY_GROUP_CURRENT_CHAIN_INDEX;
    }

    public int getMyGroupCurrentChainIndex(GroupId groupId) {
        return getPreferences().getInt(getMyGroupCurrentChainIndexPrefKey(groupId), 0);
    }

    private String getPeerGroupCurrentChainIndexPrefKey(GroupId groupId, UserId peerUserId) {
        return groupId.rawId() + "/" + PREF_KEY_PEER_GROUP_CURRENT_CHAIN_INDEX + "/" + peerUserId.rawId();
    }

    public int getPeerGroupCurrentChainIndex(GroupId groupId, UserId peerUserId) {
        return getPreferences().getInt(getPeerGroupCurrentChainIndexPrefKey(groupId, peerUserId), 0);
    }

    private String getMyGroupChainKeyPrefKey(GroupId groupId) {
        return groupId.rawId() + "/" + PREF_KEY_MY_GROUP_CHAIN_KEY;
    }

    public byte[] getMyGroupChainKey(GroupId groupId) {
        return retrieveBytes(getMyGroupChainKeyPrefKey(groupId));
    }

    private String getPeerGroupChainKeyPrefKey(GroupId groupId, UserId peerUserId) {
        return groupId.rawId() + "/" + PREF_KEY_PEER_GROUP_CHAIN_KEY + "/" + peerUserId.rawId();
    }

    public byte[] getPeerGroupChainKey(GroupId groupId, UserId peerUserId) {
        return retrieveBytes(getPeerGroupChainKeyPrefKey(groupId, peerUserId));
    }

    private String getMyGroupSigningKeyPrefKey(GroupId groupId) {
        return groupId.rawId() + "/" + PREF_KEY_MY_GROUP_SIGNING_KEY;
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

    public PublicEdECKey getPeerGroupSigningKey(GroupId groupId, UserId peerUserId) throws CryptoException {
        try {
            return new PublicEdECKey(retrieveBytes(getPeerGroupSigningKeyPrefKey(groupId, peerUserId)));
        } catch (NullPointerException e) {
            throw new CryptoException("peer_group_signing_key_not_found", e);
        }
    }

    private String getPeerGroupSigningKeyPrefKey(GroupId groupId, UserId peerUserId) {
        return groupId.rawId() + "/" + PREF_KEY_PEER_GROUP_SIGNING_KEY + "/" + peerUserId.rawId();
    }

    private String getGroupFeedKeySetPrefKey(GroupId groupId, UserId peerUserId) {
        return groupId.rawId() + "/" + PREF_KEY_SKIPPED_GROUP_FEED_KEYS_SET + "/" + peerUserId.rawId();
    }

    private String getGroupFeedKeyPrefKey(GroupId groupId, UserId peerUserId, int currentChainIndex) {
        return groupId.rawId() + "/" + PREF_KEY_SKIPPED_GROUP_FEED_KEY + "/" + peerUserId.rawId() + "/" + currentChainIndex;
    }


    //HOME
    private String getHomeAudiencePrefix(boolean favorites) {
        return favorites ? PREF_KEY_HOME_AUDIENCE_FAVORITES_PREFIX : PREF_KEY_HOME_AUDIENCE_ALL_PREFIX;
    }

    private String getHomeSendAlreadySetUpPrefKey(boolean favorites) {
        return getHomeAudiencePrefix(favorites) + "/" + PREF_KEY_HOME_SEND_ALREADY_SET_UP;
    }

    public boolean getHomeSendAlreadySetUp(boolean favorites) {
        return getPreferences().getBoolean(getHomeSendAlreadySetUpPrefKey(favorites), false);
    }

    private String getMyHomeCurrentChainIndexPrefKey(boolean favorites) {
        return getHomeAudiencePrefix(favorites) + "/" + PREF_KEY_MY_HOME_CURRENT_CHAIN_INDEX;
    }

    public int getMyHomeCurrentChainIndex(boolean favorites) {
        return getPreferences().getInt(getMyHomeCurrentChainIndexPrefKey(favorites), 0);
    }

    private String getPeerHomeCurrentChainIndexPrefKey(boolean favorites, UserId peerUserId) {
        return getHomeAudiencePrefix(favorites) + "/" + PREF_KEY_PEER_HOME_CURRENT_CHAIN_INDEX + "/" + peerUserId.rawId();
    }

    public int getPeerHomeCurrentChainIndex(boolean favorites, UserId peerUserId) {
        return getPreferences().getInt(getPeerHomeCurrentChainIndexPrefKey(favorites, peerUserId), 0);
    }

    private String getMyHomeChainKeyPrefKey(boolean favorites) {
        return getHomeAudiencePrefix(favorites) + "/" + PREF_KEY_MY_HOME_CHAIN_KEY;
    }

    public byte[] getMyHomeChainKey(boolean favorites) {
        return retrieveBytes(getMyHomeChainKeyPrefKey(favorites));
    }

    private String getPeerHomeChainKeyPrefKey(boolean favorites, UserId peerUserId) {
        return getHomeAudiencePrefix(favorites) + "/" + PREF_KEY_PEER_HOME_CHAIN_KEY + "/" + peerUserId.rawId();
    }

    public byte[] getPeerHomeChainKey(boolean favorites, UserId peerUserId) {
        return retrieveBytes(getPeerHomeChainKeyPrefKey(favorites, peerUserId));
    }

    private String getMyHomeSigningKeyPrefKey(boolean favorites) {
        return getHomeAudiencePrefix(favorites) + "/" + PREF_KEY_MY_HOME_SIGNING_KEY;
    }

    public PrivateEdECKey getMyPrivateHomeSigningKey(boolean favorites) throws CryptoException {
        try {
            return new PrivateEdECKey(retrieveBytes(getMyHomeSigningKeyPrefKey(favorites)));
        } catch (NullPointerException e) {
            throw new CryptoException("my_home_signing_key_not_found", e);
        }
    }

    public PublicEdECKey getMyPublicHomeSigningKey(boolean favorites) throws CryptoException {
        return CryptoUtils.publicEdECKeyFromPrivate(getMyPrivateHomeSigningKey(favorites));
    }

    public PublicEdECKey getPeerHomeSigningKey(boolean favorites, UserId peerUserId) throws CryptoException {
        try {
            return new PublicEdECKey(retrieveBytes(getPeerHomeSigningKeyPrefKey(favorites, peerUserId)));
        } catch (NullPointerException e) {
            throw new CryptoException("peer_home_signing_key_not_found", e);
        }
    }

    private String getPeerHomeSigningKeyPrefKey(boolean favorites, UserId peerUserId) {
        return getHomeAudiencePrefix(favorites) + "/" + PREF_KEY_PEER_HOME_SIGNING_KEY + "/" + peerUserId.rawId();
    }

    private String getHomeFeedKeySetPrefKey(boolean favorites, UserId peerUserId) {
        return getHomeAudiencePrefix(favorites) + "/" + PREF_KEY_SKIPPED_HOME_FEED_KEYS_SET + "/" + peerUserId.rawId();
    }

    private String getHomeFeedKeyPrefKey(boolean favorites, UserId peerUserId, int currentChainIndex) {
        return getHomeAudiencePrefix(favorites) + "/" + PREF_KEY_SKIPPED_HOME_FEED_KEY + "/" + peerUserId.rawId() + "/" + currentChainIndex;
    }

    private String getNeedsStateUidSetPrefKey(boolean favorites) {
        return getHomeAudiencePrefix(favorites) + "/" + PREF_KEY_NEEDS_STATE_UID_SET;
    }



    private byte[] retrieveCurve25519PrivateKey(String prefKey) {
        byte[] ret = retrieveBytes(prefKey);
        if (ret == null) {
            return null;
        }
        Preconditions.checkState(ret.length == CURVE_25519_PRIVATE_KEY_LENGTH);
        return ret;
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

    public class Editor {
        private final SharedPreferences.Editor editor;

        public Editor(SharedPreferences.Editor editor) {
            this.editor = editor;
        }

        public void apply() {
            editor.apply();
        }

        public Editor generateClientPrivateKeys() {
            Log.i("EncryptedKeyStore: Generating new keys");
            clearMyPreviousPublicEd25519IdentityKey();
            setMyEd25519IdentityKey(CryptoUtils.generateEd25519KeyPair());
            setMyPrivateSignedPreKey(CryptoUtils.generateX25519PrivateKey());
            return this;
        }

        private Editor storeBytes(String prefKey, byte[] bytes) {
            editor.putString(prefKey, bytesToString(bytes));
            return this;
        }

        // Only private key is stored; public key can be generated from it
        private Editor storeCurve25519PrivateKey(String prefKey, byte[] privateKey) {
            Preconditions.checkArgument(privateKey.length == CURVE_25519_PRIVATE_KEY_LENGTH);
            storeBytes(prefKey, privateKey);
            return this;
        }

        private Editor setMyEd25519IdentityKey(byte[] key) {
            storeBytes(PREF_KEY_MY_ED25519_IDENTITY_KEY, key);
            return this;
        }

        public Editor setPeerVerified(UserId peerUserId, boolean verified) {
            editor.putBoolean(getPeerVerifiedPrefKey(peerUserId), verified);
            return this;
        }

        public Editor clearPeerVerified(UserId peerUserId) {
            editor.remove(getPeerVerifiedPrefKey(peerUserId));
            return this;
        }

        public Editor setSessionAlreadySetUp(UserId peerUserId, boolean downloaded) {
            editor.putBoolean(getSessionAlreadySetUpPrefKey(peerUserId), downloaded);
            return this;
        }

        public Editor clearSessionAlreadySetUp(UserId peerUserId) {
            editor.remove(getSessionAlreadySetUpPrefKey(peerUserId));
            return this;
        }

        public Editor setLastDownloadAttempt(UserId peerUserId, long lastDownloadAttempt) {
            editor.putLong(getLastDownloadAttemptPrefKey(peerUserId), lastDownloadAttempt);
            return this;
        }

        public Editor clearLastDownloadAttempt(UserId peerUserId) {
            editor.remove(getLastDownloadAttemptPrefKey(peerUserId));
            return this;
        }

        public Editor setPeerResponded(UserId peerUserId, boolean responded) {
            editor.putBoolean(getPeerRespondedPrefKey(peerUserId), responded);
            return this;
        }

        public Editor clearPeerResponded(UserId peerUserId) {
            editor.remove(getPeerRespondedPrefKey(peerUserId));
            return this;
        }

        private Editor setMyPreviousPublicEd25519IdentityKey(PublicEdECKey key) {
            storeBytes(PREF_KEY_MY_PREVIOUS_PUBLIC_ED25519_IK, key.getKeyMaterial());
            return this;
        }

        private Editor clearMyPreviousPublicEd25519IdentityKey() {
            editor.remove(PREF_KEY_MY_PREVIOUS_PUBLIC_ED25519_IK);
            return this;
        }

        private Editor setMyPrivateSignedPreKey(byte[] key) {
            storeCurve25519PrivateKey(PREF_KEY_MY_PRIVATE_SIGNED_PRE_KEY, key);
            return this;
        }

        public Editor clearInboundTeardownKey(UserId peerUserId) {
            editor.remove(getInboundTeardownKeyPrefKey(peerUserId));
            return this;
        }

        public Editor setInboundTeardownKey(UserId peerUserId, byte[] teardownKey) {
            storeBytes(getInboundTeardownKeyPrefKey(peerUserId), teardownKey);
            return this;
        }

        public Editor clearOutboundTeardownKey(UserId peerUserId) {
            editor.remove(getOutboundTeardownKeyPrefKey(peerUserId));
            return this;
        }

        public Editor setOutboundTeardownKey(UserId peerUserId, byte[] teardownKey) {
            storeBytes(getOutboundTeardownKeyPrefKey(peerUserId), teardownKey);
            return this;
        }

        public Editor clearOutboundCurrentChainIndex(UserId peerUserId) {
            editor.remove(getOutboundCurrentChainIndexPrefKey(peerUserId));
            return this;
        }

        public Editor setOutboundCurrentChainIndex(UserId peerUserId, int index) {
            editor.putInt(getOutboundCurrentChainIndexPrefKey(peerUserId), index);
            return this;
        }

        public Editor clearInboundCurrentChainIndex(UserId peerUserId) {
            editor.remove(getInboundCurrentChainIndexPrefKey(peerUserId));
            return this;
        }

        public Editor setInboundCurrentChainIndex(UserId peerUserId, int index) {
            editor.putInt(getInboundCurrentChainIndexPrefKey(peerUserId), index);
            return this;
        }

        public Editor clearOutboundPreviousChainLength(UserId peerUserId) {
            editor.remove(getOutboundPreviousChainLengthPrefKey(peerUserId));
            return this;
        }

        public Editor setOutboundPreviousChainLength(UserId peerUserId, int len) {
            editor.putInt(getOutboundPreviousChainLengthPrefKey(peerUserId), len);
            return this;
        }

        public Editor clearInboundPreviousChainLength(UserId peerUserId) {
            editor.remove(getInboundPreviousChainLengthPrefKey(peerUserId));
            return this;
        }

        public Editor setInboundPreviousChainLength(UserId peerUserId, int len) {
            editor.putInt(getInboundPreviousChainLengthPrefKey(peerUserId), len);
            return this;
        }

        public Editor clearOutboundEphemeralKeyId(UserId peerUserId) {
            editor.remove(getOutboundEphemeralKeyIdPrefKey(peerUserId));
            return this;
        }

        public Editor setOutboundEphemeralKeyId(UserId peerUserId, int id) {
            editor.putInt(getOutboundEphemeralKeyIdPrefKey(peerUserId), id);
            return this;
        }

        public Editor clearInboundEphemeralKeyId(UserId peerUserId) {
            editor.remove(getInboundEphemeralKeyIdPrefKey(peerUserId));
            return this;
        }

        public Editor setInboundEphemeralKeyId(UserId peerUserId, int id) {
            editor.putInt(getInboundEphemeralKeyIdPrefKey(peerUserId), id);
            return this;
        }

        public Editor clearOutboundEphemeralKey(UserId peerUserId) {
            editor.remove(getOutboundEphemeralKeyPrefKey(peerUserId));
            return this;
        }

        public Editor setOutboundEphemeralKey(UserId peerUserId, PrivateXECKey key) {
            storeCurve25519PrivateKey(getOutboundEphemeralKeyPrefKey(peerUserId), key.getKeyMaterial());
            return this;
        }

        public Editor clearInboundEphemeralKey(UserId peerUserId) {
            editor.remove(getInboundEphemeralKeyPrefKey(peerUserId));
            return this;
        }

        public Editor setInboundEphemeralKey(UserId peerUserId, PublicXECKey key) {
            storeCurve25519PrivateKey(getInboundEphemeralKeyPrefKey(peerUserId), key.getKeyMaterial());
            return this;
        }

        public Editor clearInboundChainKey(UserId peerUserId) {
            editor.remove(getInboundChainKeyPrefKey(peerUserId));
            return this;
        }

        public Editor setInboundChainKey(UserId peerUserId, byte[] key) {
            storeBytes(getInboundChainKeyPrefKey(peerUserId), key);
            return this;
        }

        public Editor clearOutboundChainKey(UserId peerUserId) {
            editor.remove(getOutboundChainKeyPrefKey(peerUserId));
            return this;
        }

        public Editor setOutboundChainKey(UserId peerUserId, byte[] key) {
            storeBytes(getOutboundChainKeyPrefKey(peerUserId), key);
            return this;
        }

        public Editor clearRootKey(UserId peerUserId) {
            editor.remove(getRootKeyPrefKey(peerUserId));
            return this;
        }

        public Editor setRootKey(UserId peerUserId, byte[] key) {
            storeBytes(getRootKeyPrefKey(peerUserId), key);
            return this;
        }

        public Editor clearPeerOneTimePreKeyId(UserId peerUserId) {
            editor.remove(getPeerOneTimePreKeyIdPrefKey(peerUserId));
            return this;
        }

        public Editor setPeerOneTimePreKeyId(UserId peerUserId, int id) {
            editor.putInt(getPeerOneTimePreKeyIdPrefKey(peerUserId), id);
            return this;
        }

        public Editor clearPeerOneTimePreKey(UserId peerUserId) {
            editor.remove(getPeerOneTimePreKeyPrefKey(peerUserId));
            return this;
        }

        public Editor setPeerOneTimePreKey(UserId peerUserId, PublicXECKey key) {
            storeBytes(getPeerOneTimePreKeyPrefKey(peerUserId), key.getKeyMaterial());
            return this;
        }

        public Editor clearPeerSignedPreKey(UserId peerUserId) {
            editor.remove(getPeerSignedPreKeyPrefKey(peerUserId));
            return this;
        }

        public Editor setPeerSignedPreKey(UserId peerUserId, PublicXECKey key) {
            storeBytes(getPeerSignedPreKeyPrefKey(peerUserId), key.getKeyMaterial());
            return this;
        }

        public Editor clearPeerPublicIdentityKey(UserId peerUserId) {
            editor.remove(getPeerPublicIdentityKeyPrefKey(peerUserId));
            return this;
        }

        public Editor setPeerPublicIdentityKey(UserId peerUserId, PublicEdECKey key) {
            storeBytes(getPeerPublicIdentityKeyPrefKey(peerUserId), key.getKeyMaterial());
            return this;
        }


        //GROUPS
        public Editor clearPeerGroupSigningKey(GroupId groupId, UserId peerUserId) {
            editor.remove(getPeerGroupSigningKeyPrefKey(groupId, peerUserId)).apply();
            return this;
        }

        public Editor setPeerGroupSigningKey(GroupId groupId, UserId peerUserId, PublicEdECKey key) {
            storeBytes(getPeerGroupSigningKeyPrefKey(groupId, peerUserId), key.getKeyMaterial());
            return this;
        }

        public Editor clearMyGroupSigningKey(GroupId groupId) {
            editor.remove(getMyGroupSigningKeyPrefKey(groupId));
            return this;
        }

        public Editor setMyGroupSigningKey(GroupId groupId, PrivateEdECKey key) {
            storeBytes(getMyGroupSigningKeyPrefKey(groupId), key.getKeyMaterial());
            return this;
        }

        public Editor clearPeerGroupChainKey(GroupId groupId, UserId peerUserId) {
            editor.remove(getPeerGroupChainKeyPrefKey(groupId, peerUserId));
            return this;
        }

        public Editor setPeerGroupChainKey(GroupId groupId, UserId peerUserId, byte[] key) {
            storeBytes(getPeerGroupChainKeyPrefKey(groupId, peerUserId), key);
            return this;
        }

        public Editor clearMyGroupChainKey(GroupId groupId) {
            editor.remove(getMyGroupChainKeyPrefKey(groupId));
            return this;
        }

        public Editor setMyGroupChainKey(GroupId groupId, byte[] key) {
            storeBytes(getMyGroupChainKeyPrefKey(groupId), key);
            return this;
        }

        public Editor clearPeerGroupCurrentChainIndex(GroupId groupId, UserId peerUserId) {
            editor.remove(getPeerGroupCurrentChainIndexPrefKey(groupId, peerUserId));
            return this;
        }

        public Editor setPeerGroupCurrentChainIndex(GroupId groupId, UserId peerUserId, int index) {
            editor.putInt(getPeerGroupCurrentChainIndexPrefKey(groupId, peerUserId), index);
            return this;
        }

        public Editor clearMyGroupCurrentChainIndex(GroupId groupId) {
            editor.remove(getMyGroupCurrentChainIndexPrefKey(groupId));
            return this;
        }

        public Editor setMyGroupCurrentChainIndex(GroupId groupId, int index) {
            editor.putInt(getMyGroupCurrentChainIndexPrefKey(groupId), index);
            return this;
        }

        public Editor clearGroupSendAlreadySetUp(GroupId groupId) {
            editor.remove(getGroupSendAlreadySetUpPrefKey(groupId));
            return this;
        }

        public Editor setGroupSendAlreadySetUp(GroupId groupId) {
            editor.putBoolean(getGroupSendAlreadySetUpPrefKey(groupId), true);
            return this;
        }



        //HOME
        public Editor clearPeerHomeSigningKey(boolean favorites, UserId peerUserId) {
            editor.remove(getPeerHomeSigningKeyPrefKey(favorites, peerUserId)).apply();
            return this;
        }

        public Editor setPeerHomeSigningKey(boolean favorites, UserId peerUserId, PublicEdECKey key) {
            storeBytes(getPeerHomeSigningKeyPrefKey(favorites, peerUserId), key.getKeyMaterial());
            return this;
        }

        public Editor clearMyHomeSigningKey(boolean favorites) {
            editor.remove(getMyHomeSigningKeyPrefKey(favorites));
            return this;
        }

        public Editor setMyHomeSigningKey(boolean favorites, PrivateEdECKey key) {
            storeBytes(getMyHomeSigningKeyPrefKey(favorites), key.getKeyMaterial());
            return this;
        }

        public Editor clearPeerHomeChainKey(boolean favorites, UserId peerUserId) {
            editor.remove(getPeerHomeChainKeyPrefKey(favorites, peerUserId));
            return this;
        }

        public Editor setPeerHomeChainKey(boolean favorites, UserId peerUserId, byte[] key) {
            storeBytes(getPeerHomeChainKeyPrefKey(favorites, peerUserId), key);
            return this;
        }

        public Editor clearMyHomeChainKey(boolean favorites) {
            editor.remove(getMyHomeChainKeyPrefKey(favorites));
            return this;
        }

        public Editor setMyHomeChainKey(boolean favorites, byte[] key) {
            storeBytes(getMyHomeChainKeyPrefKey(favorites), key);
            return this;
        }

        public Editor clearPeerHomeCurrentChainIndex(boolean favorites, UserId peerUserId) {
            editor.remove(getPeerHomeCurrentChainIndexPrefKey(favorites, peerUserId));
            return this;
        }

        public Editor setPeerHomeCurrentChainIndex(boolean favorites, UserId peerUserId, int index) {
            editor.putInt(getPeerHomeCurrentChainIndexPrefKey(favorites, peerUserId), index);
            return this;
        }

        public Editor clearMyHomeCurrentChainIndex(boolean favorites) {
            editor.remove(getMyHomeCurrentChainIndexPrefKey(favorites));
            return this;
        }

        public Editor setMyHomeCurrentChainIndex(boolean favorites, int index) {
            editor.putInt(getMyHomeCurrentChainIndexPrefKey(favorites), index);
            return this;
        }

        public Editor clearHomeSendAlreadySetUp(boolean favorites) {
            editor.remove(getHomeSendAlreadySetUpPrefKey(favorites));
            return this;
        }

        public Editor setHomeSendAlreadySetUp(boolean favorites) {
            editor.putBoolean(getHomeSendAlreadySetUpPrefKey(favorites), true);
            return this;
        }

        public Editor storeNeedsStateUid(boolean favorites, UserId userId) {
            String prefKey = getNeedsStateUidSetPrefKey(favorites);
            Set<String> set = new HashSet<>(getPreferences().getStringSet(prefKey, new HashSet<>()));
            set.add(userId.rawId());

            editor.putStringSet(prefKey, set);

            return this;
        }



        // TODO: Clear out old keys after some threshold
        public Editor storeSkippedHomeFeedKey(boolean favorites, UserId peerUserId, HomeFeedPostMessageKey messageKey) {
            Log.i("Storing skipped home feed key " + messageKey + " for user " + peerUserId);
            String messageKeySetPrefKey = getHomeFeedKeySetPrefKey(favorites, peerUserId);
            Set<String> messageKeyPrefKeys = new HashSet<>(Preconditions.checkNotNull(getPreferences().getStringSet(messageKeySetPrefKey, new HashSet<>())));

            String keyPrefKey = getHomeFeedKeyPrefKey(favorites, peerUserId, messageKey.getCurrentChainIndex());
            messageKeyPrefKeys.add(keyPrefKey);

            editor.putString(keyPrefKey, bytesToString(messageKey.getKeyMaterial())).putStringSet(messageKeySetPrefKey, messageKeyPrefKeys);

            return this;
        }

        public Editor clearSkippedHomeFeedKeys(boolean favorites, UserId peerUserId) {
            String messageKeySetPrefKey = getHomeFeedKeySetPrefKey(favorites, peerUserId);
            Set<String> messageKeyPrefKeys = new HashSet<>(Preconditions.checkNotNull(getPreferences().getStringSet(messageKeySetPrefKey, new HashSet<>())));

            editor.remove(messageKeySetPrefKey);

            for (String prefKey : messageKeyPrefKeys) {
                editor.remove(prefKey);
            }

            return this;
        }

        // TODO: Clear out old keys after some threshold
        public Editor storeSkippedGroupFeedKey(GroupId groupId, UserId peerUserId, GroupFeedMessageKey messageKey) {
            Log.i("Storing skipped group feed key " + messageKey + " for " + groupId + " member " + peerUserId);
            String messageKeySetPrefKey = getGroupFeedKeySetPrefKey(groupId, peerUserId);
            Set<String> messageKeyPrefKeys = new HashSet<>(Preconditions.checkNotNull(getPreferences().getStringSet(messageKeySetPrefKey, new HashSet<>())));

            String keyPrefKey = getGroupFeedKeyPrefKey(groupId, peerUserId, messageKey.getCurrentChainIndex());
            messageKeyPrefKeys.add(keyPrefKey);

            editor.putString(keyPrefKey, bytesToString(messageKey.getKeyMaterial())).putStringSet(messageKeySetPrefKey, messageKeyPrefKeys);

            return this;
        }

        public Editor clearSkippedGroupFeedKeys(GroupId groupId, UserId peerUserId) {
            String messageKeySetPrefKey = getGroupFeedKeySetPrefKey(groupId, peerUserId);
            Set<String> messageKeyPrefKeys = new HashSet<>(Preconditions.checkNotNull(getPreferences().getStringSet(messageKeySetPrefKey, new HashSet<>())));

            editor.remove(messageKeySetPrefKey);

            for (String prefKey : messageKeyPrefKeys) {
                editor.remove(prefKey);
            }

            return this;
        }

        // TODO: Clear out old keys after some threshold
        public Editor storeSkippedMessageKey(UserId peerUserId, SignalMessageKey signalMessageKey) {
            Log.i("Storing skipped message key " + signalMessageKey + " for user " + peerUserId);
            String messageKeySetPrefKey = getMessageKeySetPrefKey(peerUserId);
            Set<String> messageKeyPrefKeys = new HashSet<>(Preconditions.checkNotNull(getPreferences().getStringSet(messageKeySetPrefKey, new HashSet<>())));

            String keyPrefKey = getMessageKeyPrefKey(peerUserId, signalMessageKey.getEphemeralKeyId(), signalMessageKey.getCurrentChainIndex());
            messageKeyPrefKeys.add(keyPrefKey);

            editor.putString(keyPrefKey, bytesToString(signalMessageKey.getKeyMaterial())).putStringSet(messageKeySetPrefKey, messageKeyPrefKeys);
            return this;
        }

        public Editor clearSkippedMessageKeys(UserId peerUserId) {
            String messageKeySetPrefKey = getMessageKeySetPrefKey(peerUserId);
            Set<String> messageKeyPrefKeys = new HashSet<>(Preconditions.checkNotNull(getPreferences().getStringSet(messageKeySetPrefKey, new HashSet<>())));

            editor.remove(messageKeySetPrefKey);

            for (String prefKey : messageKeyPrefKeys) {
                editor.remove(prefKey);
            }

            return this;
        }
    }
}
