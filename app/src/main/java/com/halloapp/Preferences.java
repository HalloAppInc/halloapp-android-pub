package com.halloapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatDelegate;

import com.halloapp.id.GroupId;
import com.halloapp.nux.ZeroZoneManager;
import com.halloapp.ui.ExportDataActivity;
import com.halloapp.ui.mediapicker.MediaPickerViewModel;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.util.ArrayList;
import java.util.List;

public class Preferences {

    private static Preferences instance;

    public static final String BACKED_UP_PREFS_NAME = "prefs_backed_up";
    public static final String DEVICE_LOCAL_PREFS_NAME = "prefs_device_local";

    private static final String PREF_KEY_MIGRATED_PREFS = "migrated_prefs";
    private static final String PREF_KEY_MIGRATED_GROUP_TIMESTAMPS = "updated_group_timestamps";

    private static final String PREF_KEY_CONTACTS_PERMISSION_REQUESTED = "contacts_permission_requested";
    private static final String PREF_KEY_LOCATION_PERMISSION_REQUESTED = "location_permission_requested";
    private static final String PREF_KEY_PROFILE_SETUP = "profile_setup";
    private static final String PREF_KEY_OBOARDING_FOLLOWING_SETUP = "onboarding_following_setup";
    private static final String PREF_KEY_OBOARDING_GET_STARTED_SHOWN = "onboarding_get_started_shown";
    private static final String PREF_KEY_COMPLETED_FIRST_POST_ONBOARDING = "completed_first_post_onboarding";
    private static final String PREF_KEY_LAST_FULL_CONTACTS_SYNC_TIME = "last_sync_time";
    private static final String PREF_KEY_CONTACT_SYNC_BACKOFF_TIME = "contact_sync_backoff_time";
    private static final String PREF_KEY_REQUIRE_FULL_CONTACTS_SYNC = "require_full_sync";
    private static final String PREF_KEY_REQUIRE_SHARE_POSTS = "require_share_posts";

    private static final String PREF_KEY_PENDING_OFFLINE_QUEUE = "pending_offline_queue";

    private static final String PREF_KEY_LAST_PUSH_TOKEN_SYNC_TIME = "last_push_token_sync";
    private static final String PREF_KEY_SYNCED_PUSH_TOKEN = "last_synced_push_token";
    private static final String PREF_KEY_SYNCED_LANGUAGE = "last_synced_language";
    private static final String PREF_KEY_SYNCED_TIME_ZONE_OFFSET = "last_synced_time_zone_offset";
    private static final String PREF_KEY_LAST_HUAWEI_PUSH_TOKEN_SYNC_TIME = "last_huawei_push_token_sync";
    private static final String PREF_KEY_SYNCED_HUAWEI_PUSH_TOKEN = "last_synced_huawei_push_token";

    private static final String PREF_KEY_FEED_NOTIFICATION_TIME_CUTOFF = "feed_notification_time_cutoff";
    private static final String PREF_KEY_MOMENT_NOTIFICATION_TIME_CUTOFF = "moment_notification_time_cutoff";
    private static final String PREF_KEY_SCREENSHOT_NOTIFICATION_TIME_CUTOFF = "screenshot_notification_time_cutoff";
    private static final String PREF_KEY_NOTIFY_POSTS = "notify_posts";
    private static final String PREF_KEY_NOTIFY_COMMENTS = "notify_comments";
    private static final String PREF_KEY_NOTIFY_MOMENTS = "notify_moments";
    private static final String PREF_KEY_USE_DEBUG_HOST = "use_debug_host";
    private static final String PREF_KEY_INVITES_REMAINING = "invites_remaining";
    private static final String PREF_KEY_FEED_PRIVACY_SETTING = "feed_privacy_setting";
    private static final String PREF_KEY_LAST_BLOCK_LIST_SYNC_TIME = "last_block_list_sync_time";
    private static final String PREF_KEY_LAST_GROUP_SYNC_TIME = "last_group_sync_time";
    private static final String PREF_KEY_SHOWED_FEED_NUX = "showed_feed_nux";
    private static final String PREF_KEY_SHOWED_MESSAGES_NUX = "showed_messages_nux";
    private static final String PREF_KEY_SHOWED_PROFILE_NUX = "showed_profile_nux";
    private static final String PREF_KEY_SHOWED_MAKE_POST_NUX = "showed_make_post_nux";
    private static final String PREF_KEY_SHOWED_ACTIVITY_CENTER_NUX = "showed_activity_center_nux";
    private static final String PREF_KEY_SHOWED_WELCOME_NUX = "showed_welcome_nux";
    private static final String PREF_KEY_SHOWED_FAVORITES_NUX = "showed_favorites_nux";
    private static final String PREF_KEY_SHOWED_MOMENTS_NUX = "showed_moments_nux";
    private static final String PREF_KEY_NEXT_NOTIF_ID = "next_notif_id";
    private static final String PREF_KEY_NEXT_PRESENCE_ID = "next_presence_id";
    private static final String PREF_KEY_LAST_DECRYPT_MESSAGE_ROW_ID = "last_decrypt_message_row_id";
    private static final String PREF_KEY_LAST_GROUP_POST_DECRYPT_MESSAGE_ROW_ID = "last_group_post_decrypt_message_row_id";
    private static final String PREF_KEY_LAST_GROUP_COMMENT_DECRYPT_MESSAGE_ROW_ID = "last_group_comment_decrypt_message_row_id";
    private static final String PREF_KEY_LAST_GROUP_HISTORY_DECRYPT_MESSAGE_ROW_ID = "last_group_history_decrypt_message_row_id";
    private static final String PREF_KEY_LAST_HOME_POST_DECRYPT_MESSAGE_ROW_ID = "last_home_post_decrypt_message_row_id";
    private static final String PREF_KEY_LAST_HOME_COMMENT_DECRYPT_MESSAGE_ROW_ID = "last_home_comment_decrypt_message_row_id";
    private static final String PREF_KEY_LAST_SILENT_DECRYPT_MESSAGE_ROW_ID = "last_silent_decrypt_message_row_id";
    private static final String PREF_KEY_VIDEO_BITRATE = "video_bitrate";
    private static final String PREF_KEY_VIDEO_BITRATE_OVERRIDE = "video_bitrate_override";
    private static final String PREF_KEY_AUDIO_BITRATE = "audio_bitrate";
    private static final String PREF_KEY_H264_RES = "h264_res";
    private static final String PREF_KEY_H265_RES = "h265_res";
    private static final String PREF_KEY_PICKER_LAYOUT = "picker_layout";
    private static final String PREF_KEY_NIGHT_MODE = "night_mode";
    private static final String PREF_KEY_EXPORT_DATA_STATE = "export_data_state";
    private static final String PREF_KEY_LAST_SEEN_POST_TIME = "last_seen_post_time";
    private static final String PREF_KEY_LAST_SEEN_ACTIVITY_TIME = "last_seen_activity_time";
    private static final String PREF_KEY_ZERO_ZONE_STATE = "zero_zone_state";
    private static final String PREF_KEY_ZERO_ZONE_GROUP_ID = "zero_zone_group_id";
    private static final String PREF_KEY_FORCED_ZERO_ZONE = "forced_zero_zone";
    private static final String PREF_KEY_MOMENT_NOTIFICATION_TIMESTAMP = "moment_notification_timestamp";
    private static final String PREF_KEY_MOMENT_NOTIFICATION_ID = "moment_notification_id";
    private static final String PREF_KEY_MOMENT_NOTIFICATION_TYPE = "moment_notification_type";
    private static final String PREF_KEY_MOMENT_NOTIFICATION_PROMPT = "moment_notification_prompt";
    private static final String PREF_KEY_MOMENT_NOTIFICATION_DATE = "moment_notification_date";

    private static final String PREF_KEY_REGISTRATION_TIME = "registration_time";
    private static final String PREF_KEY_INVITE_NOTIFICATION_SEEN = "welcome_invite_seen";

    private static final String PREF_KEY_FAVORITES_NOTIFICATION_TIME = "favorites_notification_time";
    private static final String PREF_KEY_FAVORITES_NOTIFICATION_SEEN = "favorites_notification_seen";
    private static final String PREF_KEY_WELCOME_NOTIFICATION_TIME = "welcome_notification_time";

    private static final String PREF_KEY_KEYBOARD_HEIGHT_PORTRAIT = "keyboard_height_portrait";
    private static final String PREF_KEY_KEYBOARD_HEIGHT_LANDSCAPE = "keyboard_height_landscape";

    private static final String PREF_KEY_LOCAL_EMOJI_VERSION = "local_emoji_version";
    private static final String PREF_KEY_RECENT_EMOJIS = "recent_emojis";
    private static final String PREF_KEY_EMOJI_VARIANTS = "emoji_variants";

    private static final String PREF_KEY_VIDEO_CALL_LOCAL_QUADRANT = "video_call_local_quadrant";
    private static final String PREF_KEY_KRISP_NOISE_SUPPRESSION_OLD = "krisp_noise_suppression";
    private static final String PREF_KEY_KRISP_NOISE_SUPPRESSION = "krisp_noise_suppression_new";
    private static final String PREF_KEY_KRISP_NOISE_SUPPRESSION_SAVED = "krisp_noise_suppression_saved";

    private static final String PREF_KEY_WARNED_ABOUT_MOMENT_REPLACE = "warned_about_moment_replace";

    private static final String PREF_KEY_FORCE_COMPACT_SHARE = "force_compact_share";

    private static final String PREF_UNFINISHED_REGISTRATION_DELAY_IN_DAYS_TIME_ONE = "unfinished_registration_delay_in_days_time_one";
    private static final String PREF_UNFINISHED_REGISTRATION_DELAY_IN_DAYS_TIME_TWO = "unfinished_registration_delay_in_days_time_two";
    private static final String PREF_PREV_UNFINISHED_REGISTRATION_NOTIFY_TIME_IN_MILLIS = "prev_unfinished_registration_notify_time_in_millis";

    private static final String PREF_IS_CONNECTED_TO_WEB_CLIENT = "is_connected_to_web_client";

    private static final String PREF_KEY_LAST_FULL_RELATIONSHIP_SYNC_TIME = "last_relationship_sync_time";
    private static final String PREF_KEY_LAST_FULL_FRIENDSHIP_SYNC_TIME = "last_friendship_sync_time";

    private static final String PREF_KEY_NOTIFY_REACTIONS = "notify_reactions";
    private static final String PREF_KEY_NOTIFY_MENTIONS = "notify_mentions";
    private static final String PREF_KEY_NOTIFY_ON_FIRE = "notify_on_fire";
    private static final String PREF_KEY_NOTIFY_NEW_USERS = "notify_new_users";
    private static final String PREF_KEY_NOTIFY_SOMEONE_FOLLOWS_YOU = "notify_someone_follows";

    private static final String PREF_KEY_SHOW_SERVER_SCORE = "show_server_score";
    private static final String PREF_KEY_SHOW_DEV_CONTENT = "show_dev_content";
    private static final String PREF_KEY_GEOTAG = "geotag";

    private static final String PREF_FRIEND_MODEL_DELAY_IN_DAYS_TIME_ONE = "friend_model_delay_in_days_time_one";
    private static final String PREF_FRIEND_MODEL_DELAY_IN_DAYS_TIME_TWO = "friend_model_delay_in_days_time_two";
    private static final String PREF_PREV_FRIEND_MODEL_NOTIFY_TIME_IN_MILLIS = "prev_friend_model_notify_time_in_millis";

    private static final String PREF_LAST_MAGIC_POST_NOTIFICATION_TIME_IN_MILLIS = "last_magic_post_notification_time_in_millis";
    private static final String PREF_KEY_SHOWED_MAGIC_POSTS_NUX = "showed_magic_posts_nux";

    private static final String PREF_KEY_SOCIAL_MEDIA_NOTIFICATION_TIME = "social_media_notification_time";
    private static final String PREF_KEY_SOCIAL_MEDIA_NOTIFICATION_SEEN = "social_media_notification_seen";

    private final AppContext appContext;
    private SharedPreferences backedUpPreferences;
    private SharedPreferences deviceLocalPreferences;

    public static Preferences getInstance() {
        if (instance == null) {
            synchronized(Preferences.class) {
                if (instance == null) {
                    instance = new Preferences(AppContext.getInstance());
                }
            }
        }
        return instance;
    }

    private Preferences(@NonNull AppContext appContext) {
        this.appContext = appContext;
    }

    public synchronized void init() {
        getKeyboardHeightLandscape(0);
        getKeyboardHeightPortrait(0);
        getLocalVideoViewQuadrant();
        ensureMigrated();
    }

    public synchronized void ensureMigrated() {
        SharedPreferences backedUpPreferences = getPreferences(true);
        boolean migrated = backedUpPreferences.getBoolean(PREF_KEY_MIGRATED_PREFS, false);
        Log.d("Prefences migrated? " + migrated);
        if (migrated) {
            return;
        }

        for (Preference<?> pref : prefs) {
            pref.migrate();
        }

        if (!backedUpPreferences.edit().putBoolean(PREF_KEY_MIGRATED_PREFS, true).commit()) {
            Log.w("Preferences failed to save migration state");
        }
    }

    private final List<Preference<?>> prefs = new ArrayList<>();

    private final BooleanPreference prefMigratedGroupTimestamp = createPref(false, PREF_KEY_MIGRATED_GROUP_TIMESTAMPS, false);
    private final BooleanPreference prefInviteNotificationSeen = createPref(false, PREF_KEY_INVITE_NOTIFICATION_SEEN, false);
    private final BooleanPreference prefPendingOfflineQueue = createPref(false, PREF_KEY_PENDING_OFFLINE_QUEUE, false);
    private final BooleanPreference prefProfileSetup = createPref(false, PREF_KEY_PROFILE_SETUP, true);
    private final BooleanPreference prefOnboardingFollowingSetup = createPref(false, PREF_KEY_OBOARDING_FOLLOWING_SETUP, false);
    private final BooleanPreference prefOnboardingGetStartedShown = createPref(false, PREF_KEY_OBOARDING_GET_STARTED_SHOWN, false);
    private final BooleanPreference prefContactsPermissionRequested = createPref(false, PREF_KEY_CONTACTS_PERMISSION_REQUESTED, false);
    private final BooleanPreference prefLocationPermissionRequested = createPref(false, PREF_KEY_LOCATION_PERMISSION_REQUESTED, false);
    private final BooleanPreference prefCompletedFirstPostOnboarding = createPref(false, PREF_KEY_COMPLETED_FIRST_POST_ONBOARDING, true);
    private final LongPreference prefContactSyncBackoffTime = createPref(false, PREF_KEY_CONTACT_SYNC_BACKOFF_TIME, 0L);
    private final LongPreference prefLastFullContactSyncTime = createPref(false, PREF_KEY_LAST_FULL_CONTACTS_SYNC_TIME, 0L);
    private final LongPreference prefLastBlockListSyncTime = createPref(false, PREF_KEY_LAST_BLOCK_LIST_SYNC_TIME, 0L);
    private final LongPreference prefRegistrationTime = createPref(false, PREF_KEY_REGISTRATION_TIME, 0L);
    private final LongPreference prefLastPushTokenTime = createPref(false, PREF_KEY_LAST_PUSH_TOKEN_SYNC_TIME, 0L);
    private final StringPreference prefLastPushToken = createPref(false, PREF_KEY_SYNCED_PUSH_TOKEN, null);
    private final LongPreference prefLastHuaweiPushTokenTime = createPref(false, PREF_KEY_LAST_HUAWEI_PUSH_TOKEN_SYNC_TIME, 0L);
    private final StringPreference prefLastHuaweiPushToken = createPref(false, PREF_KEY_SYNCED_HUAWEI_PUSH_TOKEN, null);
    private final StringPreference prefLastLocale = createPref(false, PREF_KEY_SYNCED_LANGUAGE, null);
    private final LongPreference prefLastTimeZoneOffset = createPref(false, PREF_KEY_SYNCED_TIME_ZONE_OFFSET, 0L);
    private final LongPreference prefLastGroupSyncTime = createPref(false, PREF_KEY_LAST_GROUP_SYNC_TIME, 0L);
    private final IntPreference prefInvitesRemaining = createPref(false, PREF_KEY_INVITES_REMAINING, -1);
    private final BooleanPreference prefRequireFullContactSync = createPref(false, PREF_KEY_REQUIRE_FULL_CONTACTS_SYNC, true);
    private final BooleanPreference prefRequireSharePosts = createPref(false, PREF_KEY_REQUIRE_SHARE_POSTS, false);
    private final LongPreference prefFeedNotificationCutoff = createPref(false, PREF_KEY_FEED_NOTIFICATION_TIME_CUTOFF, 0L);
    private final LongPreference prefMomentNotificationCutoff = createPref(false, PREF_KEY_MOMENT_NOTIFICATION_TIME_CUTOFF, 0L);
    private final LongPreference prefScreenshotNotificationCutoff = createPref(false, PREF_KEY_SCREENSHOT_NOTIFICATION_TIME_CUTOFF, 0L);
    private final BooleanPreference prefUseDebugHost = createPref(false, PREF_KEY_USE_DEBUG_HOST, BuildConfig.DEBUG);
    private final StringPreference prefActivePrivacyList = createPref(false, PREF_KEY_FEED_PRIVACY_SETTING, PrivacyList.Type.INVALID);
    private final IntPreference prefNextNotificationId = createPref(false, PREF_KEY_NEXT_NOTIF_ID, Constants.FIRST_DYNAMIC_NOTIFICATION_ID);
    private final LongPreference prefLastDecryptStatRowId = createPref(false, PREF_KEY_LAST_DECRYPT_MESSAGE_ROW_ID, -1L);
    private final LongPreference prefLastGroupPostDecryptStatRowId = createPref(false, PREF_KEY_LAST_GROUP_POST_DECRYPT_MESSAGE_ROW_ID, -1L);
    private final LongPreference prefLastGroupCommentDecryptStatRowId = createPref(false, PREF_KEY_LAST_GROUP_COMMENT_DECRYPT_MESSAGE_ROW_ID, -1L);
    private final LongPreference prefLastGroupHistoryDecryptStatRowId = createPref(false, PREF_KEY_LAST_GROUP_HISTORY_DECRYPT_MESSAGE_ROW_ID, -1L);
    private final LongPreference prefLastHomePostDecryptStatRowId = createPref(false, PREF_KEY_LAST_HOME_POST_DECRYPT_MESSAGE_ROW_ID, -1L);
    private final LongPreference prefLastHomeCommentDecryptStatRowId = createPref(false, PREF_KEY_LAST_HOME_COMMENT_DECRYPT_MESSAGE_ROW_ID, -1L);
    private final LongPreference prefLastSeenPostTime = createPref(false, PREF_KEY_LAST_SEEN_POST_TIME, 0L);
    private final LongPreference prefLastSeenActivityTime = createPref(false, PREF_KEY_LAST_SEEN_ACTIVITY_TIME, 0L);
    private final IntPreference prefVideoBitrateOverride = createPref(false, PREF_KEY_VIDEO_BITRATE_OVERRIDE, Constants.VIDEO_BITRATE_OVERRIDE);
    private final IntPreference prefAudioBitrate = createPref(false, PREF_KEY_AUDIO_BITRATE, Constants.AUDIO_BITRATE);
    private final IntPreference prefH264Res = createPref(false, PREF_KEY_H264_RES, Constants.VIDEO_RESOLUTION_H264);
    private final IntPreference prefPickerLayout = createPref(false, PREF_KEY_PICKER_LAYOUT, MediaPickerViewModel.LAYOUT_DAY_SMALL);
    private final IntPreference prefNightMode = createPref(false, PREF_KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    private final IntPreference prefZeroZoneState = createPref(false, PREF_KEY_ZERO_ZONE_STATE, 0);
    private final StringPreference prefZeroZoneGroupId = createPref(false, PREF_KEY_ZERO_ZONE_GROUP_ID, null);
    private final BooleanPreference prefForceZeroZone = createPref(false, PREF_KEY_FORCED_ZERO_ZONE, false);
    private final LongPreference prefMomentNotificationTimestamp = createPref(false, PREF_KEY_MOMENT_NOTIFICATION_TIMESTAMP, 0L);
    private final LongPreference prefMomentNotificationId = createPref(false, PREF_KEY_MOMENT_NOTIFICATION_ID, 0L);
    private final IntPreference prefMomentNotificationType = createPref(false, PREF_KEY_MOMENT_NOTIFICATION_TYPE, 0);
    private final StringPreference prefMomentNotificationPrompt = createPref(false, PREF_KEY_MOMENT_NOTIFICATION_PROMPT, null);
    private final StringPreference prefMomentNotificationDate = createPref(false, PREF_KEY_MOMENT_NOTIFICATION_DATE, null);

    private final IntPreference prefExportDataState = createPref(true, PREF_KEY_EXPORT_DATA_STATE, ExportDataActivity.EXPORT_STATE_INITIAL);
    private final BooleanPreference prefNotifyPosts = createPref(true, PREF_KEY_NOTIFY_POSTS, true);
    private final BooleanPreference prefNotifyComments = createPref(true, PREF_KEY_NOTIFY_COMMENTS, true);
    private final BooleanPreference prefNotifyMoments = createPref(true, PREF_KEY_NOTIFY_MOMENTS, true);

    private final IntPreference prefKeyboardHeightPortrait = createPref(false, PREF_KEY_KEYBOARD_HEIGHT_PORTRAIT, 0);
    private final IntPreference prefKeyboardHeightLandscape = createPref(false, PREF_KEY_KEYBOARD_HEIGHT_LANDSCAPE, 0);

    private final IntPreference prefLocalEmojiVersion = createPref(true, PREF_KEY_LOCAL_EMOJI_VERSION, 0);
    private final StringPreference prefRecentEmojis = createPref(true, PREF_KEY_RECENT_EMOJIS, null);
    private final StringPreference prefEmojiVariants = createPref(true, PREF_KEY_EMOJI_VARIANTS, null);
    private final IntPreference prefVideoCallLocalViewQuadrant = createPref(true, PREF_KEY_VIDEO_CALL_LOCAL_QUADRANT, Constants.Quadrant.TOP_RIGHT);

    private final LongPreference prefFavoritesNotificationTime = createPref(false, PREF_KEY_FAVORITES_NOTIFICATION_TIME, 0L);
    private final BooleanPreference prefFavoritesNotificationSeen = createPref(false, PREF_KEY_FAVORITES_NOTIFICATION_SEEN, false);
    private final LongPreference prefWelcomeNotificationTime = createPref(false, PREF_KEY_WELCOME_NOTIFICATION_TIME, 0L);
    private final BooleanPreference prefWarnedAboutMomentReplace = createPref(false, PREF_KEY_WARNED_ABOUT_MOMENT_REPLACE, false);

    private final BooleanPreference prefForceCompactShare = createPref(false, PREF_KEY_FORCE_COMPACT_SHARE, false);

    private final IntPreference prefUnfinishedRegistrationNotifyDelayInDaysTimeOne = createPref(false, PREF_UNFINISHED_REGISTRATION_DELAY_IN_DAYS_TIME_ONE, 1);
    private final IntPreference prefUnfinishedRegistrationNotifyDelayInDaysTimeTwo = createPref(false, PREF_UNFINISHED_REGISTRATION_DELAY_IN_DAYS_TIME_TWO, 1);
    private final LongPreference prefPrevUnfinishedRegistrationNotificationTimeInMillis = createPref(false, PREF_PREV_UNFINISHED_REGISTRATION_NOTIFY_TIME_IN_MILLIS, System.currentTimeMillis());

    private final BooleanPreference prefIsConnectedToWebClient = createPref(false, PREF_IS_CONNECTED_TO_WEB_CLIENT, false);

    private final LongPreference prefLastFullRelationshipSyncTime = createPref(false, PREF_KEY_LAST_FULL_RELATIONSHIP_SYNC_TIME, 0L);
    private final LongPreference prefLastFullFriendshipSyncTime = createPref(false, PREF_KEY_LAST_FULL_FRIENDSHIP_SYNC_TIME, 0L);

    private final BooleanPreference prefNotifyReactions = createPref(true, PREF_KEY_NOTIFY_REACTIONS, true);
    private final BooleanPreference prefNotifyMentions = createPref(true, PREF_KEY_NOTIFY_MENTIONS, true);
    private final BooleanPreference prefNotifyOnFire = createPref(true, PREF_KEY_NOTIFY_ON_FIRE, true);
    private final BooleanPreference prefNotifyNewUsers = createPref(true, PREF_KEY_NOTIFY_NEW_USERS, true);
    private final BooleanPreference prefNotifySomeoneFollowsYou = createPref(true, PREF_KEY_NOTIFY_SOMEONE_FOLLOWS_YOU, true);

    private final BooleanPreference prefShowServerScore = createPref(false, PREF_KEY_SHOW_SERVER_SCORE, false);
    private final BooleanPreference prefShowDevContent = createPref(false, PREF_KEY_SHOW_DEV_CONTENT, false);
    private final StringPreference prefGeotag = createPref(true, PREF_KEY_GEOTAG, null);

    private final IntPreference prefFriendModelDelayInDaysTimeOne = createPref(false, PREF_FRIEND_MODEL_DELAY_IN_DAYS_TIME_ONE, 1);
    private final IntPreference prefFriendModelDelayInDaysTimeTwo = createPref(false, PREF_FRIEND_MODEL_DELAY_IN_DAYS_TIME_TWO, 1);
    private final LongPreference prefPrevFriendModelNotifyTimeInMillis = createPref(false, PREF_PREV_FRIEND_MODEL_NOTIFY_TIME_IN_MILLIS, System.currentTimeMillis());

    private final LongPreference prefLastMagicPostNotificationTimeInMillis = createPref(false, PREF_LAST_MAGIC_POST_NOTIFICATION_TIME_IN_MILLIS, System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS);
    private final BooleanPreference prefShowedMagicPostNux = createPref(false, PREF_KEY_SHOWED_MAGIC_POSTS_NUX, false);

    private final LongPreference prefSocialMediaNotificationTime = createPref(false, PREF_KEY_SOCIAL_MEDIA_NOTIFICATION_TIME, 0L);
    private final BooleanPreference prefSocialMediaNotificationSeen = createPref(false, PREF_KEY_SOCIAL_MEDIA_NOTIFICATION_SEEN, false);

    private BooleanPreference createPref(boolean backedUp, String prefKey, boolean defaultValue) {
        BooleanPreference pref = new BooleanPreference(backedUp, prefKey, defaultValue);
        prefs.add(pref);
        return pref;
    }

    private IntPreference createPref(boolean backedUp, String prefKey, int defaultValue) {
        IntPreference pref = new IntPreference(backedUp, prefKey, defaultValue);
        prefs.add(pref);
        return pref;
    }

    private LongPreference createPref(boolean backedUp, String prefKey, long defaultValue) {
        LongPreference pref = new LongPreference(backedUp, prefKey, defaultValue);
        prefs.add(pref);
        return pref;
    }

    private StringPreference createPref(boolean backedUp, String prefKey, String defaultValue) {
        StringPreference pref = new StringPreference(backedUp, prefKey, defaultValue);
        prefs.add(pref);
        return pref;
    }

    private final String[] deletedPrefs = new String[] {
            PREF_KEY_LAST_SILENT_DECRYPT_MESSAGE_ROW_ID, // TODO(jack): Remove after August 30
            PREF_KEY_NEXT_PRESENCE_ID, // TODO(jack): Remove after August 30
            PREF_KEY_SHOWED_FEED_NUX, // TODO(clark): Remove after October 1
            PREF_KEY_SHOWED_ACTIVITY_CENTER_NUX,
            PREF_KEY_SHOWED_MESSAGES_NUX,
            PREF_KEY_SHOWED_PROFILE_NUX,
            PREF_KEY_SHOWED_MAKE_POST_NUX,
            PREF_KEY_SHOWED_WELCOME_NUX, // TODO(clark): Remove after October 30
            PREF_KEY_VIDEO_BITRATE, // TODO(vasil): Remove after July 31
            PREF_KEY_KRISP_NOISE_SUPPRESSION_OLD,  // TODO(vipin): Remove after Dec 31, 2022
            PREF_KEY_H265_RES, // TODO(jack): Remove after Sept 29
            PREF_KEY_KRISP_NOISE_SUPPRESSION,  // TODO(vipin): Remove after Dec 31, 2022
            PREF_KEY_KRISP_NOISE_SUPPRESSION_SAVED,  // TODO(vipin): Remove after Dec 31, 2022
            PREF_KEY_SHOWED_MOMENTS_NUX, // TODO(jack): Remove after December 31
            PREF_KEY_SHOWED_FAVORITES_NUX, // TODO(jack): Remove after December 31
    };

    private abstract class Preference<T> {
        final boolean backedUp;
        final String prefKey;
        final T defaultValue;

        public Preference(boolean backedUp, String prefKey, T defaultValue) {
            this.backedUp = backedUp;
            this.prefKey = prefKey;
            this.defaultValue = defaultValue;
        }

        protected SharedPreferences getPreferences() {
            return Preferences.this.getPreferences(this.backedUp);
        }

        public abstract T get();
        public abstract void set(T value);

        public boolean remove() {
            return getPreferences().edit().remove(this.prefKey).commit();
        }

        public abstract void migrate(); // TODO(jack): Remove once migration complete
    }

    private class BooleanPreference extends Preference<Boolean> {
        public BooleanPreference(boolean backedUp, String prefKey, Boolean defaultValue) {
            super(backedUp, prefKey, defaultValue);
        }

        public Boolean get() {
            return getPreferences().getBoolean(this.prefKey, this.defaultValue);
        }

        public void set(Boolean v) {
            if (!getPreferences().edit().putBoolean(this.prefKey, v).commit()) {
                Log.e("Preferences: failed to set " + this.prefKey);
            }
        }

        public void apply(Boolean v) {
            getPreferences().edit().putBoolean(this.prefKey, v).apply();
        }

        public void migrate() {
            SharedPreferences originalPreferences = appContext.get().getSharedPreferences("prefs", Context.MODE_PRIVATE);
            set(originalPreferences.getBoolean(this.prefKey, this.defaultValue));
        }
    }

    private class IntPreference extends Preference<Integer> {
        public IntPreference(boolean backedUp, String prefKey, Integer defaultValue) {
            super(backedUp, prefKey, defaultValue);
        }

        public Integer get() {
            return getPreferences().getInt(this.prefKey, this.defaultValue);
        }

        public Integer get(int def) {
            return getPreferences().getInt(this.prefKey, def);
        }

        public void set(Integer v) {
            if (!getPreferences().edit().putInt(this.prefKey, v).commit()) {
                Log.e("Preferences: failed to set " + this.prefKey);
            }
        }

        public void apply(Integer v) {
            getPreferences().edit().putInt(this.prefKey, v).apply();
        }

        public void migrate() {
            SharedPreferences originalPreferences = appContext.get().getSharedPreferences("prefs", Context.MODE_PRIVATE);
            set(originalPreferences.getInt(this.prefKey, this.defaultValue));
        }
    }

    private class LongPreference extends Preference<Long> {
        public LongPreference(boolean backedUp, String prefKey, Long defaultValue) {
            super(backedUp, prefKey, defaultValue);
        }

        public Long get() {
            return getPreferences().getLong(this.prefKey, this.defaultValue);
        }

        public void set(Long v) {
            if (!getPreferences().edit().putLong(this.prefKey, v).commit()) {
                Log.e("Preferences: failed to set " + this.prefKey);
            }
        }

        public void apply(Long v) {
            getPreferences().edit().putLong(this.prefKey, v).apply();
        }

        public void migrate() {
            SharedPreferences originalPreferences = appContext.get().getSharedPreferences("prefs", Context.MODE_PRIVATE);
            set(originalPreferences.getLong(this.prefKey, this.defaultValue));
        }
    }

    private class StringPreference extends Preference<String> {
        public StringPreference(boolean backedUp, String prefKey, String defaultValue) {
            super(backedUp, prefKey, defaultValue);
        }

        public String get() {
            return getPreferences().getString(this.prefKey, this.defaultValue);
        }

        public void set(String v) {
            if (!getPreferences().edit().putString(this.prefKey, v).commit()) {
                Log.e("Preferences: failed to set " + this.prefKey);
            }
        }

        public void apply(String v) {
            getPreferences().edit().putString(this.prefKey, v).apply();
        }

        public void migrate() {
            SharedPreferences originalPreferences = appContext.get().getSharedPreferences("prefs", Context.MODE_PRIVATE);
            set(originalPreferences.getString(this.prefKey, this.defaultValue));
        }
    }

    @WorkerThread
    private synchronized SharedPreferences getPreferences(boolean backedUp) {
        if (backedUp) {
            if (backedUpPreferences == null) {
                backedUpPreferences = appContext.get().getSharedPreferences(BACKED_UP_PREFS_NAME, Context.MODE_PRIVATE);
                removeDeletedPrefs(backedUpPreferences);
            }
            return backedUpPreferences;
        } else {
            if (deviceLocalPreferences == null) {
                deviceLocalPreferences = appContext.get().getSharedPreferences(DEVICE_LOCAL_PREFS_NAME, Context.MODE_PRIVATE);
                removeDeletedPrefs(deviceLocalPreferences);
            }
            return deviceLocalPreferences;
        }
    }

    @WorkerThread
    public void wipePreferences() {
        getPreferences(false).edit().clear().apply();
        getPreferences(true).edit().clear().apply();
    }

    private void removeDeletedPrefs(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        for (String prefKey : deletedPrefs) {
            editor.remove(prefKey);
        }
        if (!editor.commit()) {
            Log.w("Preferences: Failed to remove deleted prefs");
        }
    }

    @WorkerThread
    public boolean getProfileSetup() {
        return prefProfileSetup.get();
    }

    @WorkerThread
    public void setProfileSetup(boolean setup) {
        prefProfileSetup.set(setup);
    }

    @WorkerThread
    public boolean getOnboardingFollowingSetup() {
        return prefOnboardingFollowingSetup.get();
    }

    @WorkerThread
    public void setOnboardingFollowingSetup(boolean setup) {
        prefOnboardingFollowingSetup.set(setup);
    }

    @WorkerThread
    public boolean getOnboardingGetStartedShown() {
        return prefOnboardingGetStartedShown.get();
    }

    @WorkerThread
    public void setOnboardingGetStartedShown(boolean setup) {
        prefOnboardingGetStartedShown.set(setup);
    }

    @WorkerThread
    public boolean getContactsPermissionRequested() {
        return prefContactsPermissionRequested.get();
    }

    @WorkerThread
    public void setContactsPermissionRequested(boolean setup) {
        prefContactsPermissionRequested.set(setup);
    }

    @WorkerThread
    public boolean getLocationPermissionRequested() {
        return prefLocationPermissionRequested.get();
    }

    @WorkerThread
    public void setLocationPermissionRequested(boolean setup) {
        prefLocationPermissionRequested.set(setup);
    }

    @WorkerThread
    public boolean getCompletedFirstPostOnboarding() {
        return prefCompletedFirstPostOnboarding.get();
    }

    @WorkerThread
    public void setCompletedFirstPostOnboarding(boolean completed) {
        prefCompletedFirstPostOnboarding.set(completed);
    }

    @WorkerThread
    public void applyCompletedFirstPostOnboarding(boolean completed) {
        prefCompletedFirstPostOnboarding.apply(completed);
    }

    @WorkerThread
    public long getContactSyncBackoffTime() {
        return prefContactSyncBackoffTime.get();
    }

    @WorkerThread
    public void setContactSyncBackoffTime(long time) {
        prefContactSyncBackoffTime.set(time);
    }

    @AnyThread
    public void clearContactSyncBackoffTime() {
        prefContactSyncBackoffTime.apply(0L);
    }

    @WorkerThread
    public long getLastFullContactSyncTime() {
        return prefLastFullContactSyncTime.get();
    }

    @WorkerThread
    public void setLastFullContactSyncTime(long time) {
        prefLastFullContactSyncTime.set(time);
    }

    @WorkerThread
    public long getLastBlockListSyncTime() {
        return prefLastBlockListSyncTime.get();
    }

    @WorkerThread
    public void setLastBlockListSyncTime(long time) {
        prefLastBlockListSyncTime.set(time);
    }

    @WorkerThread
    public boolean getWelcomeInviteNotificationSeen() {
        return prefInviteNotificationSeen.get();
    }

    @WorkerThread
    public void setWelcomeInviteNotificationSeen(boolean seen) {
        prefInviteNotificationSeen.set(seen);
    }

    @WorkerThread
    public boolean getMigratedGroupTimestamps() {
        return prefMigratedGroupTimestamp.get();
    }

    @WorkerThread
    public void setMigratedGroupTimestamps(boolean migrated) {
        prefMigratedGroupTimestamp.set(migrated);
    }

    @WorkerThread
    public long getInitialRegistrationTime() {
        return prefRegistrationTime.get();
    }

    @WorkerThread
    public void setInitialRegistrationTime(long time) {
        prefRegistrationTime.set(time);
    }

    @WorkerThread
    public long getLastPushTokenSyncTime() {
        return prefLastPushTokenTime.get();
    }

    @WorkerThread
    public void setLastPushTokenSyncTime(long time) {
        prefLastPushTokenTime.set(time);
    }

    @WorkerThread
    public String getLastPushToken() {
        return prefLastPushToken.get();
    }

    @WorkerThread
    public void setLastPushToken(String token) {
        prefLastPushToken.set(token);
    }

    @WorkerThread
    public long getLastHuaweiPushTokenSyncTime() {
        return prefLastHuaweiPushTokenTime.get();
    }

    @WorkerThread
    public void setLastHuaweiPushTokenSyncTime(long time) {
        prefLastHuaweiPushTokenTime.set(time);
    }

    @WorkerThread
    public String getLastHuaweiPushToken() {
        return prefLastHuaweiPushToken.get();
    }

    @WorkerThread
    public void setLastHuaweiPushToken(String token) {
        prefLastHuaweiPushToken.set(token);
    }

    @WorkerThread
    public String getLastDeviceLocale() {
        return prefLastLocale.get();
    }

    @WorkerThread
    public void setLastDeviceLocale(String languageCode) {
        prefLastLocale.set(languageCode);
    }

    @WorkerThread
    public long getLastTimeZoneOffset() {
        return prefLastTimeZoneOffset.get();
    }

    @WorkerThread
    public void setLastTimeZoneOffset(long timeZoneOffset) {
        prefLastTimeZoneOffset.set(timeZoneOffset);
    }

    @WorkerThread
    public long getLastGroupSyncTime() {
        return prefLastGroupSyncTime.get();
    }

    @WorkerThread
    public void setLastGroupSyncTime(long time) {
        prefLastGroupSyncTime.set(time);
    }

    @WorkerThread
    public void setInvitesRemaining(int invitesRemaining) {
        prefInvitesRemaining.set(invitesRemaining);
    }

    @WorkerThread
    public int getInvitesRemaining() {
        return prefInvitesRemaining.get();
    }

    @WorkerThread
    public boolean getRequireFullContactsSync() {
        return prefRequireFullContactSync.get();
    }

    @AnyThread
    public void applyRequireFullContactsSync(boolean requireFullContactsSync) {
        prefRequireFullContactSync.apply(requireFullContactsSync);
    }

    @WorkerThread
    public void setRequireFullContactsSync(boolean requireFullContactsSync) {
        prefRequireFullContactSync.set(requireFullContactsSync);
    }

    @WorkerThread
    public boolean getRequireSharePosts() {
        return prefRequireSharePosts.get();
    }

    @WorkerThread
    public void setRequireSharePosts(boolean requireSharePosts) {
        prefRequireSharePosts.set(requireSharePosts);
    }

    @WorkerThread
    public long getFeedNotificationTimeCutoff() {
        return prefFeedNotificationCutoff.get();
    }

    @WorkerThread
    public void setFeedNotificationTimeCutoff(long time) {
        prefFeedNotificationCutoff.set(time);
    }

    @WorkerThread
    public long getMomentNotificationTimeCutoff() {
        return prefMomentNotificationCutoff.get();
    }

    @WorkerThread
    public void setMomentNotificationTimeCutoff(long time) {
        prefMomentNotificationCutoff.set(time);
    }

    @WorkerThread
    public long getScreenshotNotificationTimeCutoff() {
        return prefScreenshotNotificationCutoff.get();
    }

    @WorkerThread
    public void setScreenshotNotificationTimeCutoff(long time) {
        prefScreenshotNotificationCutoff.set(time);
    }

    @WorkerThread
    public boolean getNotifyPosts() {
        return prefNotifyPosts.get();
    }

    @WorkerThread
    public boolean getNotifyComments() {
        return prefNotifyComments.get();
    }

    @WorkerThread
    public boolean getNotifyMoments() {
        return prefNotifyMoments.get();
    }

    @WorkerThread
    public boolean getUseDebugHost() {
        return prefUseDebugHost.get();
    }

    @WorkerThread
    public void setUseDebugHost(boolean useDebugHost) {
        prefUseDebugHost.set(useDebugHost);
    }

    @WorkerThread
    public @PrivacyList.Type String getFeedPrivacyActiveList() {
        return prefActivePrivacyList.get();
    }

    @WorkerThread
    public void setFeedPrivacyActiveList(@PrivacyList.Type String activeList) {
        prefActivePrivacyList.set(activeList);
    }

    @WorkerThread
    public int getAndIncrementNotificationId() {
        int id = prefNextNotificationId.get();
        prefNextNotificationId.set(id + 1);
        return id;
    }

    @WorkerThread
    public long getLastDecryptStatMessageRowId() {
        return prefLastDecryptStatRowId.get();
    }

    @WorkerThread
    public void setLastDecryptStatMessageRowId(long id) {
        prefLastDecryptStatRowId.set(id);
    }

    @WorkerThread
    public long getLastGroupPostDecryptStatMessageRowId() {
        return prefLastGroupPostDecryptStatRowId.get();
    }

    @WorkerThread
    public void setLastGroupPostDecryptStatMessageRowId(long id) {
        prefLastGroupPostDecryptStatRowId.set(id);
    }

    @WorkerThread
    public long getLastGroupCommentDecryptStatMessageRowId() {
        return prefLastGroupCommentDecryptStatRowId.get();
    }

    @WorkerThread
    public void setLastGroupCommentDecryptStatMessageRowId(long id) {
        prefLastGroupCommentDecryptStatRowId.set(id);
    }

    @WorkerThread
    public long getLastGroupHistoryDecryptStatMessageRowId() {
        return prefLastGroupHistoryDecryptStatRowId.get();
    }

    @WorkerThread
    public void setLastGroupHistoryDecryptStatMessageRowId(long id) {
        prefLastGroupHistoryDecryptStatRowId.set(id);
    }

    @WorkerThread
    public long getLastHomePostDecryptStatMessageRowId() {
        return prefLastHomePostDecryptStatRowId.get();
    }

    @WorkerThread
    public void setLastHomePostDecryptStatMessageRowId(long id) {
        prefLastHomePostDecryptStatRowId.set(id);
    }

    @WorkerThread
    public long getLastHomeCommentDecryptStatMessageRowId() {
        return prefLastHomeCommentDecryptStatRowId.get();
    }

    @WorkerThread
    public void setLastHomeCommentDecryptStatMessageRowId(long id) {
        prefLastHomeCommentDecryptStatRowId.set(id);
    }

    @WorkerThread
    public long getLastSeenPostTime() {
        return prefLastSeenPostTime.get();
    }

    @WorkerThread
    public void setLastSeenPostTime(long timeStamp) {
        prefLastSeenPostTime.set(timeStamp);
    }

    @WorkerThread
    public long getLastSeenActivityTime() {
        return prefLastSeenActivityTime.get();
    }

    @WorkerThread
    public void setLastSeenActivityTime(long timeStamp) {
        prefLastSeenActivityTime.set(timeStamp);
    }

    @WorkerThread
    public void resetVideoOverride() {
        prefVideoBitrateOverride.remove();
        prefAudioBitrate.remove();
        prefH264Res.remove();
    }

    @WorkerThread
    public void saveVideoOverride() {
        prefVideoBitrateOverride.set(Constants.VIDEO_BITRATE_OVERRIDE);
        prefAudioBitrate.set(Constants.AUDIO_BITRATE);
        prefH264Res.set(Constants.VIDEO_RESOLUTION_H264);
    }

    @WorkerThread
    public void loadVideoOverride() {
        Constants.VIDEO_BITRATE_OVERRIDE = prefVideoBitrateOverride.get();
        Constants.AUDIO_BITRATE = prefAudioBitrate.get();
        Constants.VIDEO_RESOLUTION_H264 = prefH264Res.get();
    }

    @WorkerThread
    public int getPickerLayout() {
        return prefPickerLayout.get();
    }

    @WorkerThread
    public void setPickerLayout(int layout) {
        prefPickerLayout.set(layout);
    }

    @WorkerThread
    public int getExportDataState() {
        return prefExportDataState.get();
    }

    @WorkerThread
    public void setExportDataState(int state) {
        prefExportDataState.set(state);
    }

    @WorkerThread
    public int getNightMode() {
        return prefNightMode.get();
    }

    @WorkerThread
    public void setNightMode(int nightMode) {
        prefNightMode.set(nightMode);
    }

    @WorkerThread
    public boolean getPendingOfflineQueue() {
        return prefPendingOfflineQueue.get();
    }

    @WorkerThread
    public void setPendingOfflineQueue(boolean hasQueue) {
        prefPendingOfflineQueue.set(hasQueue);
    }

    @WorkerThread
    public @ZeroZoneManager.ZeroZoneState int getZeroZoneState() {
        return prefZeroZoneState.get();
    }

    @WorkerThread
    public void setZeroZoneState(@ZeroZoneManager.ZeroZoneState int state) {
        prefZeroZoneState.set(state);
    }

    @WorkerThread
    @Nullable
    public GroupId getZeroZoneGroupId() {
        return GroupId.fromNullable(prefZeroZoneGroupId.get());
    }

    @WorkerThread
    public void setZeroZoneGroupId(@Nullable GroupId groupId) {
        prefZeroZoneGroupId.set(groupId == null ? null : groupId.rawId());
    }

    @WorkerThread
    public void setForcedZeroZone(boolean inZeroZone) {
        prefForceZeroZone.set(inZeroZone);
    }

    @WorkerThread
    public boolean isForcedZeroZone() {
        return prefForceZeroZone.get();
    }

    @WorkerThread
    public int getKeyboardHeightPortrait(int defaultValue) {
        return prefKeyboardHeightPortrait.get(defaultValue);
    }

    @WorkerThread
    public int getKeyboardHeightLandscape(int defaultValue) {
        return prefKeyboardHeightLandscape.get(defaultValue);
    }

    public void setKeyboardHeightPortrait(int height) {
        prefKeyboardHeightPortrait.apply(height);
    }

    public void setKeyboardHeightLandscape(int height) {
        prefKeyboardHeightLandscape.apply(height);
    }

    @WorkerThread
    public int getLocalEmojiVersion() {
        return prefLocalEmojiVersion.get();
    }

    @WorkerThread
    public void setLocalEmojiVersion(int version) {
        prefLocalEmojiVersion.set(version);
    }

    @WorkerThread
    public String getRecentEmojis() {
        return prefRecentEmojis.get();
    }

    public void setRecentEmojis(String recentEmojis) {
        prefRecentEmojis.apply(recentEmojis);
    }

    @WorkerThread
    public String getEmojiVariants() {
        return prefEmojiVariants.get();
    }

    public void setEmojiVariants(String variants) {
        prefEmojiVariants.apply(variants);
    }

    @AnyThread
    public void applyLocalVideoViewQuadrant(int quadrant) {
        prefVideoCallLocalViewQuadrant.apply(quadrant);
    }

    public int getLocalVideoViewQuadrant() {
        return prefVideoCallLocalViewQuadrant.get();
    }

    @WorkerThread
    public long getFavoritesNotificationTime() {
        return prefFavoritesNotificationTime.get();
    }

    @WorkerThread
    public void setFavoritesNotificationTime(long time) {
        prefFavoritesNotificationTime.set(time);
    }

    @WorkerThread
    public long getWelcomeNotificationTime() {
        return prefWelcomeNotificationTime.get();
    }

    @WorkerThread
    public void setWelcomeNotificationTime(long time) {
        prefWelcomeNotificationTime.set(time);
    }

    @WorkerThread
    public boolean getFavoritesNotificationSeen() {
        return prefFavoritesNotificationSeen.get();
    }

    @WorkerThread
    public void setFavoritesNotificationSeen() {
        prefFavoritesNotificationSeen.set(true);
    }

    @AnyThread
    public void applyMomentsReplaceWarned() {
        prefWarnedAboutMomentReplace.apply(true);
    }

    @WorkerThread
    public boolean getWarnedMomentsReplace() {
        return prefWarnedAboutMomentReplace.get();
    }

    @WorkerThread
    public boolean getForceCompactShare() {
        return prefForceCompactShare.get();
    }

    @WorkerThread
    public void setForceCompactShare(boolean force) {
        prefForceCompactShare.set(force);
    }

    @WorkerThread
    public int getUnfinishedRegistrationNotifyDelayInDaysTimeOne() {
        return prefUnfinishedRegistrationNotifyDelayInDaysTimeOne.get();
    }

    @WorkerThread
    public void setUnfinishedRegistrationNotifyDelayInDaysTimeOne(int time1) {
         prefUnfinishedRegistrationNotifyDelayInDaysTimeOne.set(time1);
    }

    @WorkerThread
    public int getUnfinishedRegistrationNotifyDelayInDaysTimeTwo() {
        return prefUnfinishedRegistrationNotifyDelayInDaysTimeTwo.get();
    }

    @WorkerThread
    public void setUnfinishedRegistrationNotifyDelayInDaysTimeTwo(int time2) {
        prefUnfinishedRegistrationNotifyDelayInDaysTimeTwo.set(time2);
    }

    @WorkerThread
    public void setPrevUnfinishedRegistrationNotificationTimeInMillis(long time) {
        prefPrevUnfinishedRegistrationNotificationTimeInMillis.set(time);
    }

    @WorkerThread
    public long getPrevUnfinishedRegistrationNotificationTimeInMillis() {
        return prefPrevUnfinishedRegistrationNotificationTimeInMillis.get();
    }

    @WorkerThread
    public void setIsConnectedToWebClient(boolean isConnected) {
        prefIsConnectedToWebClient.set(isConnected);
    }

    @WorkerThread
    public boolean getIsConnectedToWebClient() {
        return prefIsConnectedToWebClient.get();
    }

    @WorkerThread
    public long getMomentNotificationTimestamp() {
        return prefMomentNotificationTimestamp.get();
    }

    @WorkerThread
    public void setMomentNotificationTimestamp(long timestamp) {
        prefMomentNotificationTimestamp.set(timestamp);
    }

    @WorkerThread
    public int getMomentNotificationType() {
        return prefMomentNotificationType.get();
    }

    @WorkerThread
    public void setMomentNotificationType(int type) {
        prefMomentNotificationType.set(type);
    }

    @WorkerThread
    @Nullable
    public String getMomentNotificationPrompt() {
        return prefMomentNotificationPrompt.get();
    }

    @WorkerThread
    public void setMomentNotificationPrompt(String prompt) {
        prefMomentNotificationPrompt.set(prompt);
    }

    @WorkerThread
    @Nullable
    public String getMomentNotificationDate() {
        return prefMomentNotificationDate.get();
    }

    @WorkerThread
    public void setMomentNotificationDate(String date) {
        prefMomentNotificationDate.set(date);
    }

    @WorkerThread
    public long getMomentNotificationId() {
        return prefMomentNotificationId.get();
    }

    @WorkerThread
    public void setMomentNotificationId(long id) {
        prefMomentNotificationId.set(id);
    }

    @WorkerThread
    public long getLastFullRelationshipSyncTime() {
        return prefLastFullRelationshipSyncTime.get();
    }

    @WorkerThread
    public void setLastFullRelationshipSyncTime(long timestamp) {
        prefLastFullRelationshipSyncTime.set(timestamp);
    }

    @WorkerThread
    public long getLastFullFriendshipSyncTime() {
        return prefLastFullFriendshipSyncTime.get();
    }

    @WorkerThread
    public void setLastFullFriendshipSyncTime(long timestamp) {
        prefLastFullFriendshipSyncTime.set(timestamp);
    }

    @WorkerThread
    public boolean getNotifyReactions() {
        return prefNotifyReactions.get();
    }

    @WorkerThread
    public boolean getNotifyMentions() {
        return prefNotifyMentions.get();
    }

    @WorkerThread
    public boolean getNotifyOnFire() {
        return prefNotifyOnFire.get();
    }

    @WorkerThread
    public boolean getNotifyNewUsers() {
        return prefNotifyNewUsers.get();
    }

    @WorkerThread
    public boolean getNotifySomeoneFollowsYou() {
        return prefNotifySomeoneFollowsYou.get();
    }

    @WorkerThread
    public boolean getShowServerScore() {
        return prefShowServerScore.get();
    }

    @WorkerThread
    public void setShowServerScore(boolean show) {
        prefShowServerScore.set(show);
    }

    @WorkerThread
    public boolean getShowDevContent() {
        return prefShowDevContent.get();
    }

    public String getGeotag() {
        return prefGeotag.get();
    }

    public void setGeotag(String geotag) {
        prefGeotag.set(geotag);
    }
    
    @WorkerThread
    public int getPrefFriendModelDelayInDaysTimeOne() {
        return prefFriendModelDelayInDaysTimeOne.get();
    }

    @WorkerThread
    public void setPrefFriendModelDelayInDaysTimeOne(int time) {
        prefFriendModelDelayInDaysTimeOne.set(time);
    }

    @WorkerThread
    public int getPrefFriendModelDelayInDaysTimeTwo() {
        return prefFriendModelDelayInDaysTimeTwo.get();
    }

    @WorkerThread
    public void setPrefFriendModelDelayInDaysTimeTwo(int time) {
        prefFriendModelDelayInDaysTimeTwo.set(time);
    }
    
    @WorkerThread
    public long getPrefPrevFriendModelNotifyTimeInMillis() {
        return prefPrevFriendModelNotifyTimeInMillis.get();
    }

    @WorkerThread
    public void setPrefPrevFriendModelNotifyTimeInMillis(long time) {
        prefPrevFriendModelNotifyTimeInMillis.set(time);
    }

    @WorkerThread
    public long getPrefLastMagicPostNotificationTimeInMillis() {
        return prefLastMagicPostNotificationTimeInMillis.get();
    }

    @WorkerThread
    public void setPrefLastMagicPostNotificationTimeInMillis(long time) {
        prefLastMagicPostNotificationTimeInMillis.set(time);
    }

    @WorkerThread
    public boolean getPrefShowedMagicPostNux() {
        return prefShowedMagicPostNux.get();
    }

    @WorkerThread
    public void setPrefShowedMagicPostNux(boolean showed) {
        prefShowedMagicPostNux.set(showed);
    }

    @WorkerThread
    public long getSocialMediaNotificationTime() {
        return prefSocialMediaNotificationTime.get();
    }

    @WorkerThread
    public void setSocialMediaNotificationTime(long time) {
        prefSocialMediaNotificationTime.set(time);
    }

    @WorkerThread
    public boolean getSocialMediaNotificationSeen() {
        return prefSocialMediaNotificationSeen.get();
    }

    @WorkerThread
    public void setSocialMediaNotificationSeen() {
        prefSocialMediaNotificationSeen.set(true);
    }
}
