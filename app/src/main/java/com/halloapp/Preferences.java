package com.halloapp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.util.Log;
import com.halloapp.xmpp.privacy.PrivacyList;

public class Preferences {

    private static Preferences instance;

    public static final String PREFS_NAME = "prefs";

    private static final String PREF_KEY_LAST_CONTACTS_SYNC_TIME = "last_sync_time";
    private static final String PREF_KEY_REQUIRE_FULL_CONTACTS_SYNC = "require_full_sync";
    private static final String PREF_KEY_FEED_NOTIFICATION_TIME_CUTOFF = "feed_notification_time_cutoff";
    private static final String PREF_KEY_NOTIFY_POSTS = "notify_posts";
    private static final String PREF_KEY_NOTIFY_COMMENTS = "notify_comments";
    private static final String PREF_KEY_USE_DEBUG_HOST = "use_debug_host";
    private static final String PREF_KEY_INVITES_REMAINING = "invites_remaining";
    private static final String PREF_KEY_FEED_PRIVACY_SETTING = "feed_privacy_setting";
    private static final String PREF_KEY_LAST_BLOCK_LIST_SYNC_TIME = "last_block_list_sync_time";

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
    public @PrivacyList.Type String getFeedPrivacyActiveList() {
        return getPreferences().getString(PREF_KEY_FEED_PRIVACY_SETTING, PrivacyList.Type.INVALID);
    }
}
