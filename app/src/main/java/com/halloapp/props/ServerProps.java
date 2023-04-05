package com.halloapp.props;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.AppContext;
import com.halloapp.BuildConfig;
import com.halloapp.ConnectionObservers;
import com.halloapp.Constants;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerProps {

    private static final String PREFS_NAME = "props";

    private static final String PREF_KEY_APP_VERSION = "props:app_version";
    private static final String PREF_KEY_PROPS_HASH = "props:version_hash";
    private static final String PREF_KEY_LAST_PROPS_FETCH = "props:last_fetch";

    private static final long PROPS_EXPIRATION_MS = DateUtils.DAY_IN_MILLIS;

    private static final String PROP_INTERNAL_USER = "dev";
    private static final String PROP_GROUP_CHATS_ENABLED = "group_chat";
    private static final String PROP_MAX_GROUP_SIZE = "max_group_size";
    private static final String PROP_MAX_FEED_VIDEO_DURATION = "max_feed_video_duration";
    private static final String PROP_MAX_CHAT_VIDEO_DURATION = "max_chat_video_duration";
    private static final String PROP_MIN_GROUP_SYNC_INTERVAL = "group_sync_time";
    private static final String PROP_MAX_VIDEO_BITRATE = "max_video_bit_rate";
    private static final String PROP_TARGET_VIDEO_BITRATE = "target_video_bit_rate";
    private static final String PROP_TARGET_VIDEO_RESOLUTION = "target_video_resolution";
    private static final String PROP_CONTACT_SYNC_INTERVAL = "contact_sync_frequency";
    private static final String PROP_STREAMING_UPLOAD_CHUNK_SIZE = "streaming_upload_chunk_size";
    private static final String PROP_STREAMING_INITIAL_DOWNLOAD_SIZE = "streaming_initial_download_size";
    private static final String PROP_STREAMING_SENDING_ENABLED = "streaming_sending_enabled";
    private static final String PROP_EMOJI_VERSION = "emoji_version";
    private static final String PROP_MAX_MEMBER_FOR_SHEET = "group_max_for_showing_invite_sheet";
    private static final String PROP_SEND_PLAINTEXT_GROUP_FEED = "cleartext_group_feed";
    private static final String PROP_USE_PLAINTEXT_GROUP_FEED = "use_cleartext_group_feed";
    private static final String PROP_SEND_PLAINTEXT_HOME_FEED = "cleartext_home_feed";
    private static final String PROP_USE_PLAINTEXT_HOME_FEED = "use_cleartext_home_feed";
    private static final String PROP_GROUP_COMMENTS_NOTIFICATION = "group_comments_notification";
    private static final String PROP_CALL_HOLD_ENABLED = "call_hold";
    private static final String PROP_MEDIA_DRAWING_ENABLED = "draw_media";
    private static final String PROP_INVITE_STRINGS = "invite_strings";
    private static final String PROP_IS_PSA_ADMIN = "is_psa_admin";
    private static final String PROP_MAX_POST_MEDIA_ITEMS = "max_post_media_items";
    private static final String PROP_MAX_CHAT_MEDIA_ITEMS = "max_chat_media_items";
    private static final String PROP_GROUP_EXPIRY = "group_expiry";
    private static final String PROP_CHAT_REACTIONS = "chat_reactions";
    private static final String PROP_COMMENT_REACTIONS = "comment_reactions";
    private static final String PROP_POST_REACTIONS = "post_reactions";
    private static final String PROP_MOMENT_REACTIONS = "moment_reactions";
    private static final String PROP_FILE_SHARING = "file_sharing";
    private static final String PROP_CONTACT_SHARING = "contact_sharing";
    private static final String PROP_USE_CLOSE_FRIENDS_RECOMMENDATION = "close_friends_recos";
    private static final String PROP_RELATIONSHIP_SYNC_INTERVAL = "relationship_sync_frequency";
    private static final String PROP_PUBLIC_FEED_REFRESH_INTERNVAL_SECS = "refresh_public_feed_interval_secs";
    private static final String PROP_MAX_SELFIE_VIDEO_BITRATE = "max_selfie_video_bit_rate";
    private static final String PROP_AI_IMAGE_GENERATION_ENABLED = "ai_generated_images";
    private static final String PROP_FEED_COMMENT_NOTIFICATIONS_ENABLED = "feed_comment_notifications";

    private static final int WEEK_IN_SECONDS = (int) (DateUtils.WEEK_IN_MILLIS / DateUtils.SECOND_IN_MILLIS);

    private static ServerProps instance;

    public static ServerProps getInstance() {
        if (instance == null) {
            synchronized (ServerProps.class) {
                if (instance == null) {
                    instance = new ServerProps(AppContext.getInstance(), Connection.getInstance(), ConnectionObservers.getInstance());
                }
            }
        }
        return instance;
    }

    private final List<Prop> serverProps = new ArrayList<>();

    private final AppContext appContext;
    private final Connection connection;

    private SharedPreferences preferences;

    private final BooleanProp propInternalUser = createProp(PROP_INTERNAL_USER, false);
    private final IntegerProp propMaxGroupSize = createProp(PROP_MAX_GROUP_SIZE, 25);
    private final BooleanProp propGroupChatsEnabled = createProp(PROP_GROUP_CHATS_ENABLED, false);
    private final IntegerProp propMaxFeedVideoDuration = createProp(PROP_MAX_FEED_VIDEO_DURATION, 60);
    private final IntegerProp propMaxChatVideoDuration = createProp(PROP_MAX_CHAT_VIDEO_DURATION, 120);
    private final IntegerProp propMinGroupSyncInterval = createProp(PROP_MIN_GROUP_SYNC_INTERVAL, WEEK_IN_SECONDS);
    private final IntegerProp propMaxVideoBitrate = createProp(PROP_MAX_VIDEO_BITRATE, 6000000);
    private final IntegerProp propTargetVideoBitrate = createProp(PROP_TARGET_VIDEO_BITRATE, 4000000);
    private final IntegerProp propTargetVideoResolution = createProp(PROP_TARGET_VIDEO_RESOLUTION, 720);
    private final IntegerProp propContactSyncIntervalSeconds = createProp(PROP_CONTACT_SYNC_INTERVAL, Constants.SECONDS_PER_DAY);
    private final IntegerProp propStreamingUploadChunkSize = createProp(PROP_STREAMING_UPLOAD_CHUNK_SIZE, Constants.DEFAULT_STREAMING_UPLOAD_CHUNK_SIZE);
    private final IntegerProp propStreamingInitialDownloadSize = createProp(PROP_STREAMING_INITIAL_DOWNLOAD_SIZE, Constants.DEFAULT_STREAMING_INITIAL_DOWNLOAD_SIZE);
    private final BooleanProp propStreamingSendingEnabled = createProp(PROP_STREAMING_SENDING_ENABLED, false);
    private final IntegerProp propEmojiVersion = createProp(PROP_EMOJI_VERSION, 2);
    private final IntegerProp propMaxMemberForInviteSheet = createProp(PROP_MAX_MEMBER_FOR_SHEET, 5);
    private final BooleanProp propSendPlaintextGroupFeed = createProp(PROP_SEND_PLAINTEXT_GROUP_FEED, true);
    private final BooleanProp propUsePlaintextGroupFeed = createProp(PROP_USE_PLAINTEXT_GROUP_FEED, true);
    private final BooleanProp propSendPlaintextHomeFeed = createProp(PROP_SEND_PLAINTEXT_HOME_FEED, true);
    private final BooleanProp propUsePlaintextHomeFeed = createProp(PROP_USE_PLAINTEXT_HOME_FEED, true);
    private final BooleanProp propGroupCommentsNotification = createProp(PROP_GROUP_COMMENTS_NOTIFICATION, false);
    private final BooleanProp propCallHoldEnabled = createProp(PROP_CALL_HOLD_ENABLED, false);
    private final BooleanProp propMediaDrawingEnabled = createProp(PROP_MEDIA_DRAWING_ENABLED, false);
    private final StringProp propInviteStrings = createProp(PROP_INVITE_STRINGS, "");
    private final BooleanProp propIsPsaAdmin = createProp(PROP_IS_PSA_ADMIN, false);
    private final IntegerProp propMaxPostMediaItems = createProp(PROP_MAX_POST_MEDIA_ITEMS, 10);
    private final IntegerProp propMaxChatMediaItems = createProp(PROP_MAX_CHAT_MEDIA_ITEMS, 30);
    private final BooleanProp groupExpiryEnabled = createProp(PROP_GROUP_EXPIRY, false);
    private final BooleanProp propChatReactionsEnabled = createProp(PROP_CHAT_REACTIONS, false);
    private final BooleanProp propCommentReactionsEnabled = createProp(PROP_COMMENT_REACTIONS, false);
    private final BooleanProp propPostReactionsEnabled = createProp(PROP_POST_REACTIONS, false);
    private final BooleanProp propMomentReactionsEnabled = createProp(PROP_MOMENT_REACTIONS, false);
    private final BooleanProp propFileSharingEnabled = createProp(PROP_FILE_SHARING, false);
    private final BooleanProp propContactSharingEnabled = createProp(PROP_CONTACT_SHARING, false);
    private final BooleanProp propUseCloseFriendsRec = createProp(PROP_USE_CLOSE_FRIENDS_RECOMMENDATION, false);
    private final IntegerProp propRelationshipSyncIntervalSeconds = createProp(PROP_RELATIONSHIP_SYNC_INTERVAL, Constants.SECONDS_PER_DAY);
    private final IntegerProp propPublicFeedRefreshIntervalSeconds = createProp(PROP_PUBLIC_FEED_REFRESH_INTERNVAL_SECS, 10 * 60);
    private final IntegerProp propMaxSelfieVideoBitrate = createProp(PROP_MAX_SELFIE_VIDEO_BITRATE, 1000000);
    private final BooleanProp propAiImageGenerationEnabled = createProp(PROP_AI_IMAGE_GENERATION_ENABLED, false);
    private final BooleanProp propFeedCommentNotificationsEnabled = createProp(PROP_FEED_COMMENT_NOTIFICATIONS_ENABLED, false);

    private final Connection.Observer connectionObserver = new Connection.Observer() {
        @Override
        public void onServerPropsReceived(@NonNull Map<String, String> props, @NonNull String hash) {
            onReceiveServerProps(props, hash);
        }
    };

    private ServerProps(@NonNull AppContext appContext, @NonNull Connection connection, @NonNull ConnectionObservers connectionObservers) {
        this.appContext = appContext;
        this.connection = connection;

        connectionObservers.addObserver(connectionObserver);
    }

    public void init() {
        SharedPreferences preferences = getPreferences();
        int savedVersionCode = preferences.getInt(PREF_KEY_APP_VERSION, 0);
        if (savedVersionCode != BuildConfig.VERSION_CODE) {
            clearPropHash();
            Log.i("ServerProps/init app upgraded hash cleared");
            if (!preferences.edit().putInt(PREF_KEY_APP_VERSION, BuildConfig.VERSION_CODE).commit()) {
                Log.e("ServerProps/init failed to update app version");
            }
        }

        preferences.edit()
                .remove("moment_external_share") // TODO(clark): Remove after March 1
                .apply();

        loadProps();
    }

    @WorkerThread
    private synchronized SharedPreferences getPreferences() {
        if (preferences == null) {
            preferences = appContext.get().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        return preferences;
    }

    @WorkerThread
    public synchronized void onReceiveServerPropsHash(@NonNull String hash) {
        SharedPreferences preferences = getPreferences();
        String oldHash = preferences.getString(PREF_KEY_PROPS_HASH, null);
        if (oldHash != null && oldHash.equals(hash) && !propsExpired()) {
            return;
        }
        connection.requestServerProps();
    }

    @WorkerThread
    public synchronized void onReceiveServerProps(@NonNull Map<String, String> propMap, @NonNull String propsHash) {
        for (Prop prop : serverProps) {
            String value = propMap.get(prop.getKey());
            if (value != null) {
                prop.parse(value);
            }
        }
        saveProps(propsHash);
        setLastFetchTime();
    }

    public synchronized Map<String, ?> getProps() {
        SharedPreferences preferences = getPreferences();
        return preferences.getAll();
    }

    private boolean propsExpired() {
        return System.currentTimeMillis() - getLastFetchTime() > PROPS_EXPIRATION_MS;
    }

    private long getLastFetchTime() {
        return getPreferences().getLong(PREF_KEY_LAST_PROPS_FETCH, 0);
    }

    private void setLastFetchTime() {
        getPreferences().edit().putLong(PREF_KEY_LAST_PROPS_FETCH, System.currentTimeMillis()).apply();
    }

    private void loadProps() {
        SharedPreferences preferences = getPreferences();
        for (Prop prop : serverProps) {
            prop.load(preferences);
        }
    }

    private void saveProps(@NonNull String propHash) {
        SharedPreferences preferences = getPreferences();
        for (Prop prop : serverProps) {
            prop.save(preferences);
        }
        if (!preferences.edit().putString(PREF_KEY_PROPS_HASH, propHash).commit()) {
            Log.e("ServerProps/saveProps: failed to save hash");
        }
    }

    private void clearPropHash() {
        if (!getPreferences().edit().remove(PREF_KEY_PROPS_HASH).commit()) {
            Log.e("ServerProps/saveProps: failed to clear hash");
        }
    }

    private BooleanProp createProp(@NonNull String key, boolean defaultValue) {
        BooleanProp prop = new BooleanProp(key, defaultValue);
        serverProps.add(prop);
        return prop;
    }

    private IntegerProp createProp(@NonNull String key, int defaultValue) {
        IntegerProp prop = new IntegerProp(key, defaultValue);
        serverProps.add(prop);
        return prop;
    }

    private StringProp createProp(@NonNull String key, String defaultValue) {
        StringProp prop = new StringProp(key, defaultValue);
        serverProps.add(prop);
        return prop;
    }

    public synchronized boolean getIsInternalUser() {
        return propInternalUser.getValue();
    }

    public synchronized void forceExternalUser() {
        propInternalUser.parse("false");
    }

    public synchronized int getMaxGroupSize() {
        return propMaxGroupSize.getValue();
    }

    public synchronized boolean getGroupChatsEnabled() {
        return propGroupChatsEnabled.getValue() || BuildConfig.DEBUG;
    }

    public synchronized int getMaxFeedVideoDuration() {
        return propMaxFeedVideoDuration.getValue();
    }

    public synchronized int getMaxChatVideoDuration() {
        return propMaxChatVideoDuration.getValue();
    }

    public synchronized int getMinGroupSyncIntervalSeconds() {
        return propMinGroupSyncInterval.getValue();
    }

    public synchronized int getMaxVideoBitrate() {
        return propMaxVideoBitrate.getValue();
    }

    public synchronized int getTargetVideoBitrate() {
        return propTargetVideoBitrate.getValue();
    }

    public synchronized int getTargetVideoResolution() {
        return propTargetVideoResolution.getValue();
    }

    public synchronized int getContactSyncIntervalSeconds() {
        return propContactSyncIntervalSeconds.getValue();
    }

    public synchronized int getStreamingUploadChunkSize() {
        return propStreamingUploadChunkSize.getValue();
    }

    public synchronized int getStreamingInitialDownloadSize() {
        return propStreamingInitialDownloadSize.getValue();
    }

    public synchronized boolean getStreamingSendingEnabled() {
        return propStreamingSendingEnabled.getValue();
    }

    public synchronized int getEmojiVersion() {
        return propEmojiVersion.getValue();
    }

    public synchronized int getMaxMemberForInviteSheet() {
        return propMaxMemberForInviteSheet.getValue();
    }

    public synchronized boolean getSendPlaintextGroupFeed() {
        return propSendPlaintextGroupFeed.getValue();
    }

    public synchronized boolean getUsePlaintextGroupFeed() {
        return propUsePlaintextGroupFeed.getValue() && !BuildConfig.DEBUG;
    }

    public synchronized boolean getSendPlaintextHomeFeed() {
        return propSendPlaintextHomeFeed.getValue();
    }

    public synchronized boolean getUsePlaintextHomeFeed() {
        return BuildConfig.IS_KATCHUP || (propUsePlaintextHomeFeed.getValue() && !BuildConfig.DEBUG);
    }

    public synchronized boolean getGroupCommentsNotification() {
        return propGroupCommentsNotification.getValue() || BuildConfig.DEBUG;
    }

    public synchronized boolean getCallHoldEnabled() {
        return propCallHoldEnabled.getValue();
    }

    public synchronized boolean getMediaDrawingEnabled() {
        return BuildConfig.DEBUG || getIsInternalUser() || propMediaDrawingEnabled.getValue();
    }

    public synchronized String getInviteStrings() {
        return propInviteStrings.getValue();
    }

    public synchronized boolean isPsaAdmin() {
        return propIsPsaAdmin.getValue();
    }

    public synchronized boolean isGroupExpiryEnabled() {
        return groupExpiryEnabled.getValue();
    }

    public synchronized boolean getChatReactionsEnabled() {
        return propChatReactionsEnabled.getValue();
    }

    public synchronized boolean getCommentReactionsEnabled() {
        return propCommentReactionsEnabled.getValue();
    }

    public synchronized boolean getPostReactionsEnabled() {
        return propPostReactionsEnabled.getValue();
    }

    public synchronized boolean getMomentReactionsEnabled() {
        return propMomentReactionsEnabled.getValue();
    }

    public synchronized boolean getFileSharingEnabled() {
        return propFileSharingEnabled.getValue();
    }

    public synchronized boolean getContactSharingEnabled() {
        return propContactSharingEnabled.getValue();
    }

    public synchronized boolean getUseCloseFriendsRec() {
        return propUseCloseFriendsRec.getValue();
    }

    public synchronized int getRelationshipSyncIntervalSeconds() {
        return propRelationshipSyncIntervalSeconds.getValue();
    }

    public synchronized int getPublicFeedRefreshIntervalSeconds() {
        return propPublicFeedRefreshIntervalSeconds.getValue();
    }

    public synchronized int getMaxSelfieVideoBitrate() {
        return propMaxSelfieVideoBitrate.getValue();
    }

    public synchronized boolean getAiImageGenerationEnabled() {
        return propAiImageGenerationEnabled.getValue();
    }

    public synchronized boolean getFeedCommentNotificationsEnabled() {
        return propFeedCommentNotificationsEnabled.getValue();
    }

    public boolean useNewAttachmentPicker() {
        return propFileSharingEnabled.getValue() || propContactSharingEnabled.getValue();
    }

    public synchronized int getMaxPostMediaItems() {
        return propMaxPostMediaItems.getValue();
    }

    public synchronized int getMaxChatMediaItems() {
        return propMaxChatMediaItems.getValue();
    }
}
