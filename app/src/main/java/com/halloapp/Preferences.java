package com.halloapp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.privacy.PrivacyList;

public class Preferences {

    private static Preferences instance;

    public static final String PREFS_NAME = "prefs";

    private static final String PREF_KEY_LAST_CONTACTS_SYNC_TIME = "last_sync_time";
    private static final String PREF_KEY_REQUIRE_FULL_CONTACTS_SYNC = "require_full_sync";
    private static final String PREF_KEY_REQUIRE_SHARE_POSTS = "require_share_posts";
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
    private static final String PREF_KEY_NEXT_NOTIF_ID = "next_notif_id";
    private static final String PREF_KEY_VIDEO_BITRATE = "video_bitrate";
    private static final String PREF_KEY_AUDIO_BITRATE = "audio_bitrate";
    private static final String PREF_KEY_H264_RES = "h264_res";
    private static final String PREF_KEY_H265_RES = "h265_res";

    private AppContext appContext;
    private SharedPreferences preferences;

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

    @WorkerThread
    private synchronized SharedPreferences getPreferences() {
        if (preferences == null) {
            preferences = appContext.get().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        return preferences;
    }

    @WorkerThread
    public long getLastContactsSyncTime() {
        return getPreferences().getLong(PREF_KEY_LAST_CONTACTS_SYNC_TIME, 0);
    }

    @WorkerThread
    public void setLastContactsSyncTime(long time) {
        if (!getPreferences().edit().putLong(PREF_KEY_LAST_CONTACTS_SYNC_TIME, time).commit()) {
            Log.e("preferences: failed to set last contacts sync time");
        }
    }

    @WorkerThread
    public long getLastBlockListSyncTime() {
        return getPreferences().getLong(PREF_KEY_LAST_BLOCK_LIST_SYNC_TIME, 0);
    }

    @WorkerThread
    public void setLastBlockListSyncTime(long time) {
        if (!getPreferences().edit().putLong(PREF_KEY_LAST_BLOCK_LIST_SYNC_TIME, time).commit()) {
            Log.e("preferences: failed to set last block list sync time");
        }
    }

    @WorkerThread
    public long getLastGroupSyncTime() {
        return getPreferences().getLong(PREF_KEY_LAST_GROUP_SYNC_TIME, 0);
    }

    @WorkerThread
    public void setLastGroupSyncTime(long time) {
        if (!getPreferences().edit().putLong(PREF_KEY_LAST_GROUP_SYNC_TIME, time).commit()) {
            Log.e("preferences: failed to set last group sync time");
        }
    }

    @WorkerThread
    public void setInvitesRemaining(int invitesRemaining) {
        if (!getPreferences().edit().putInt(PREF_KEY_INVITES_REMAINING, invitesRemaining).commit()) {
            Log.e("preferences: failed to set invites remaining");
        }
    }

    @WorkerThread
    public int getInvitesRemaining() {
        return getPreferences().getInt(PREF_KEY_INVITES_REMAINING, -1);
    }

    @WorkerThread
    public boolean getRequireFullContactsSync() {
        return getPreferences().getBoolean(PREF_KEY_REQUIRE_FULL_CONTACTS_SYNC, true);
    }

    @WorkerThread
    public void setRequireFullContactsSync(boolean requireFullContactsSync) {
        if (!getPreferences().edit().putBoolean(PREF_KEY_REQUIRE_FULL_CONTACTS_SYNC, requireFullContactsSync).commit()) {
            Log.e("preferences: failed to set required full contacts sync");
        }
    }

    @WorkerThread
    public boolean getRequireSharePosts() {
        return getPreferences().getBoolean(PREF_KEY_REQUIRE_SHARE_POSTS, false);
    }

    @WorkerThread
    public void setRequireSharePosts(boolean requireSharePosts) {
        if (!getPreferences().edit().putBoolean(PREF_KEY_REQUIRE_SHARE_POSTS, requireSharePosts).commit()) {
            Log.e("preferences: failed to set required share posts");
        }
    }

    @WorkerThread
    public long getFeedNotificationTimeCutoff() {
        return getPreferences().getLong(PREF_KEY_FEED_NOTIFICATION_TIME_CUTOFF, 0);
    }

    @WorkerThread
    public void setFeedNotificationTimeCutoff(long time) {
        if (!getPreferences().edit().putLong(PREF_KEY_FEED_NOTIFICATION_TIME_CUTOFF, time).commit()) {
            Log.e("preferences: failed to set feed notification time cutoff");
        }
    }

    @WorkerThread
    public boolean getNotifyPosts() {
        return getPreferences().getBoolean(PREF_KEY_NOTIFY_POSTS, true);
    }

    @WorkerThread
    public boolean getNotifyComments() {
        return getPreferences().getBoolean(PREF_KEY_NOTIFY_COMMENTS, true);
    }

    @WorkerThread
    public boolean getUseDebugHost() {
        return getPreferences().getBoolean(PREF_KEY_USE_DEBUG_HOST, false);
    }

    @WorkerThread
    public void setFeedPrivacyActiveList(@PrivacyList.Type String activeList) {
        if (!getPreferences().edit().putString(PREF_KEY_FEED_PRIVACY_SETTING, activeList).commit()) {
            Log.e("preferences: failed to set feed privacy active list");
        }
    }

    @WorkerThread
    public boolean getShowedFeedNux() {
        return getPreferences().getBoolean(PREF_KEY_SHOWED_FEED_NUX, false);
    }

    @WorkerThread
    public void markFeedNuxShown() {
        if (!getPreferences().edit().putBoolean(PREF_KEY_SHOWED_FEED_NUX, true).commit()) {
            Log.e("preferences: failed to mark feed nux shown");
        }
    }

    @WorkerThread
    public boolean getShowedMessagesNux() {
        return getPreferences().getBoolean(PREF_KEY_SHOWED_MESSAGES_NUX, false);
    }

    @WorkerThread
    public void markMessagesNuxShown() {
        if (!getPreferences().edit().putBoolean(PREF_KEY_SHOWED_MESSAGES_NUX, true).commit()) {
            Log.e("preferences: failed to mark messages nux shown");
        }
    }

    @WorkerThread
    public boolean getShowedProfileNux() {
        return getPreferences().getBoolean(PREF_KEY_SHOWED_PROFILE_NUX, false);
    }

    @WorkerThread
    public void markProfileNuxShown() {
        if (!getPreferences().edit().putBoolean(PREF_KEY_SHOWED_PROFILE_NUX, true).commit()) {
            Log.e("preferences: failed to mark profile nux shown");
        }
    }

    @WorkerThread
    public boolean getShowedMakePostNux() {
        return getPreferences().getBoolean(PREF_KEY_SHOWED_MAKE_POST_NUX, false);
    }

    @WorkerThread
    public void markMakePostNuxShown() {
        if (!getPreferences().edit().putBoolean(PREF_KEY_SHOWED_MAKE_POST_NUX, true).commit()) {
            Log.e("preferences: failed to mark make post nux shown");
        }
    }

    @WorkerThread
    public boolean getShowedActivityCenterNux() {
        return getPreferences().getBoolean(PREF_KEY_SHOWED_ACTIVITY_CENTER_NUX, false);
    }

    @WorkerThread
    public void markActivityCenterNuxShown() {
        if (!getPreferences().edit().putBoolean(PREF_KEY_SHOWED_ACTIVITY_CENTER_NUX, true).commit()) {
            Log.e("preferences: failed to mark activity center nux shown");
        }
    }

    @WorkerThread
    public int getAndIncrementNotificationId() {
        int id = getPreferences().getInt(PREF_KEY_NEXT_NOTIF_ID, Notifications.FIRST_DYNAMIC_NOTIFICATION_ID);
        if (!getPreferences().edit().putInt(PREF_KEY_NEXT_NOTIF_ID, id + 1).commit()) {
            Log.e("preferences: failed to increment notif id");
        }
        return id;
    }

    @WorkerThread
    public @PrivacyList.Type String getFeedPrivacyActiveList() {
        return getPreferences().getString(PREF_KEY_FEED_PRIVACY_SETTING, PrivacyList.Type.INVALID);
    }

    @WorkerThread
    public void resetVideoOverride() {
        getPreferences().edit().remove(PREF_KEY_VIDEO_BITRATE).remove(PREF_KEY_AUDIO_BITRATE).remove(PREF_KEY_H265_RES).remove(PREF_KEY_H264_RES).apply();
    }

    @WorkerThread
    public void saveVideoOverride() {
        getPreferences().edit()
                .putInt(PREF_KEY_VIDEO_BITRATE, Constants.VIDEO_BITRATE)
                .putInt(PREF_KEY_AUDIO_BITRATE, Constants.AUDIO_BITRATE)
                .putInt(PREF_KEY_H264_RES, Constants.VIDEO_RESOLUTION_H264)
                .putInt(PREF_KEY_H265_RES, Constants.VIDEO_RESOLUTION_H265).apply();
    }

    @WorkerThread
    public void loadVideoOverride() {
        Constants.VIDEO_BITRATE = getPreferences().getInt(PREF_KEY_VIDEO_BITRATE, Constants.VIDEO_BITRATE);
        Constants.AUDIO_BITRATE = getPreferences().getInt(PREF_KEY_AUDIO_BITRATE, Constants.AUDIO_BITRATE);
        Constants.VIDEO_RESOLUTION_H264 = getPreferences().getInt(PREF_KEY_H264_RES, Constants.VIDEO_RESOLUTION_H264);
        Constants.VIDEO_RESOLUTION_H265 = getPreferences().getInt(PREF_KEY_H265_RES, Constants.VIDEO_RESOLUTION_H265);
    }
}
