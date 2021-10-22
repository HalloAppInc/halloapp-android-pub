package com.halloapp;

import android.content.Context;
import android.content.SharedPreferences;

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

    private static final String PREF_KEY_LAST_FULL_CONTACTS_SYNC_TIME = "last_sync_time";
    private static final String PREF_KEY_CONTACT_SYNC_BACKOFF_TIME = "contact_sync_backoff_time";
    private static final String PREF_KEY_REQUIRE_FULL_CONTACTS_SYNC = "require_full_sync";
    private static final String PREF_KEY_REQUIRE_SHARE_POSTS = "require_share_posts";

    private static final String PREF_KEY_PENDING_OFFLINE_QUEUE = "pending_offline_queue";

    private static final String PREF_KEY_LAST_PUSH_TOKEN_SYNC_TIME = "last_push_token_sync";
    private static final String PREF_KEY_SYNCED_PUSH_TOKEN = "last_synced_push_token";
    private static final String PREF_KEY_SYNCED_LANGUAGE = "last_synced_language";

    private static final String PREF_KEY_FEED_NOTIFICATION_TIME_CUTOFF = "feed_notification_time_cutoff";
    private static final String PREF_KEY_NOTIFY_POSTS = "notify_posts";
    private static final String PREF_KEY_NOTIFY_COMMENTS = "notify_comments";
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
    private static final String PREF_KEY_NEXT_NOTIF_ID = "next_notif_id";
    private static final String PREF_KEY_NEXT_PRESENCE_ID = "next_presence_id";
    private static final String PREF_KEY_LAST_DECRYPT_MESSAGE_ROW_ID = "last_decrypt_message_row_id";
    private static final String PREF_KEY_LAST_GROUP_POST_DECRYPT_MESSAGE_ROW_ID = "last_group_post_decrypt_message_row_id";
    private static final String PREF_KEY_LAST_GROUP_COMMENT_DECRYPT_MESSAGE_ROW_ID = "last_group_comment_decrypt_message_row_id";
    private static final String PREF_KEY_LAST_SILENT_DECRYPT_MESSAGE_ROW_ID = "last_silent_decrypt_message_row_id";
    private static final String PREF_KEY_VIDEO_BITRATE = "video_bitrate";
    private static final String PREF_KEY_AUDIO_BITRATE = "audio_bitrate";
    private static final String PREF_KEY_H264_RES = "h264_res";
    private static final String PREF_KEY_H265_RES = "h265_res";
    private static final String PREF_KEY_PICKER_LAYOUT = "picker_layout";
    private static final String PREF_KEY_NIGHT_MODE = "night_mode";
    private static final String PREF_KEY_EXPORT_DATA_STATE = "export_data_state";
    private static final String PREF_KEY_LAST_SEEN_POST_TIME = "last_seen_post_time";
    private static final String PREF_KEY_ZERO_ZONE_STATE = "zero_zone_state";
    private static final String PREF_KEY_ZERO_ZONE_GROUP_ID = "zero_zone_group_id";
    private static final String PREF_KEY_FORCED_ZERO_ZONE = "forced_zero_zone";

    private static final String PREF_KEY_REGISTRATION_TIME = "registration_time";
    private static final String PREF_KEY_INVITE_NOTIFICATION_SEEN = "welcome_invite_seen";

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
    private final LongPreference prefContactSyncBackoffTime = createPref(false, PREF_KEY_CONTACT_SYNC_BACKOFF_TIME, 0L);
    private final LongPreference prefLastFullContactSyncTime = createPref(false, PREF_KEY_LAST_FULL_CONTACTS_SYNC_TIME, 0L);
    private final LongPreference prefLastBlockListSyncTime = createPref(false, PREF_KEY_LAST_BLOCK_LIST_SYNC_TIME, 0L);
    private final LongPreference prefRegistrationTime = createPref(false, PREF_KEY_REGISTRATION_TIME, 0L);
    private final LongPreference prefLastPushTokenTime = createPref(false, PREF_KEY_LAST_PUSH_TOKEN_SYNC_TIME, 0L);
    private final StringPreference prefLastPushToken = createPref(false, PREF_KEY_SYNCED_PUSH_TOKEN, null);
    private final StringPreference prefLastLocale = createPref(false, PREF_KEY_SYNCED_LANGUAGE, null);
    private final LongPreference prefLastGroupSyncTime = createPref(false, PREF_KEY_LAST_GROUP_SYNC_TIME, 0L);
    private final IntPreference prefInvitesRemaining = createPref(false, PREF_KEY_INVITES_REMAINING, -1);
    private final BooleanPreference prefRequireFullContactSync = createPref(false, PREF_KEY_REQUIRE_FULL_CONTACTS_SYNC, true);
    private final BooleanPreference prefRequireSharePosts = createPref(false, PREF_KEY_REQUIRE_SHARE_POSTS, false);
    private final LongPreference prefFeedNotificationCutoff = createPref(false, PREF_KEY_FEED_NOTIFICATION_TIME_CUTOFF, 0L);
    private final BooleanPreference prefUseDebugHost = createPref(false, PREF_KEY_USE_DEBUG_HOST, BuildConfig.DEBUG);
    private final StringPreference prefActivePrivacyList = createPref(false, PREF_KEY_FEED_PRIVACY_SETTING, PrivacyList.Type.INVALID);
    private final IntPreference prefNextNotificationId = createPref(false, PREF_KEY_NEXT_NOTIF_ID, Notifications.FIRST_DYNAMIC_NOTIFICATION_ID);
    private final LongPreference prefLastDecryptStatRowId = createPref(false, PREF_KEY_LAST_DECRYPT_MESSAGE_ROW_ID, -1L);
    private final LongPreference prefLastGroupPostDecryptStatRowId = createPref(false, PREF_KEY_LAST_GROUP_POST_DECRYPT_MESSAGE_ROW_ID, -1L);
    private final LongPreference prefLastGroupCommentDecryptStatRowId = createPref(false, PREF_KEY_LAST_GROUP_COMMENT_DECRYPT_MESSAGE_ROW_ID, -1L);
    private final LongPreference prefLastSeenPostTime = createPref(false, PREF_KEY_LAST_SEEN_POST_TIME, 0L);
    private final IntPreference prefVideoBitrate = createPref(false, PREF_KEY_VIDEO_BITRATE, Constants.VIDEO_BITRATE);
    private final IntPreference prefAudioBitrate = createPref(false, PREF_KEY_AUDIO_BITRATE, Constants.AUDIO_BITRATE);
    private final IntPreference prefH264Res = createPref(false, PREF_KEY_H264_RES, Constants.VIDEO_RESOLUTION_H264);
    private final IntPreference prefH265Res = createPref(false, PREF_KEY_H265_RES, Constants.VIDEO_RESOLUTION_H265);
    private final IntPreference prefPickerLayout = createPref(false, PREF_KEY_PICKER_LAYOUT, MediaPickerViewModel.LAYOUT_DAY_SMALL);
    private final IntPreference prefNightMode = createPref(false, PREF_KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    private final IntPreference prefZeroZoneState = createPref(false, PREF_KEY_ZERO_ZONE_STATE, 0);
    private final StringPreference prefZeroZoneGroupId = createPref(false, PREF_KEY_ZERO_ZONE_GROUP_ID, null);
    private final BooleanPreference prefForceZeroZone = createPref(false, PREF_KEY_FORCED_ZERO_ZONE, false);

    private final IntPreference prefExportDataState = createPref(true, PREF_KEY_EXPORT_DATA_STATE, ExportDataActivity.EXPORT_STATE_INITIAL);
    private final BooleanPreference prefNotifyPosts = createPref(true, PREF_KEY_NOTIFY_POSTS, true);
    private final BooleanPreference prefNotifyComments = createPref(true, PREF_KEY_NOTIFY_COMMENTS, true);

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

        public void set(Integer v) {
            if (!getPreferences().edit().putInt(this.prefKey, v).commit()) {
                Log.e("Preferences: failed to set " + this.prefKey);
            }
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
    public String getLastDeviceLocale() {
        return prefLastLocale.get();
    }

    @WorkerThread
    public void setLastDeviceLocale(String languageCode) {
        prefLastLocale.set(languageCode);
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
    public boolean getNotifyPosts() {
        return prefNotifyPosts.get();
    }

    @WorkerThread
    public boolean getNotifyComments() {
        return prefNotifyComments.get();
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
    public long getLastSeenPostTime() {
        return prefLastSeenPostTime.get();
    }

    @WorkerThread
    public void setLastSeenPostTime(long timeStamp) {
        prefLastSeenPostTime.set(timeStamp);
    }

    @WorkerThread
    public void resetVideoOverride() {
        prefVideoBitrate.remove();
        prefAudioBitrate.remove();
        prefH264Res.remove();
        prefH265Res.remove();
    }

    @WorkerThread
    public void saveVideoOverride() {
        prefVideoBitrate.set(Constants.VIDEO_BITRATE);
        prefAudioBitrate.set(Constants.AUDIO_BITRATE);
        prefH264Res.set(Constants.VIDEO_RESOLUTION_H264);
        prefH265Res.set(Constants.VIDEO_RESOLUTION_H265);
    }

    @WorkerThread
    public void loadVideoOverride() {
        Constants.VIDEO_BITRATE = prefVideoBitrate.get();
        Constants.AUDIO_BITRATE = prefAudioBitrate.get();
        Constants.VIDEO_RESOLUTION_H264 = prefH264Res.get();
        Constants.VIDEO_RESOLUTION_H265 = prefH265Res.get();
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
}
