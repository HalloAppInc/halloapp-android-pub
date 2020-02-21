package com.halloapp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.util.Log;

public class Preferences {

    private static Preferences instance;

    public static final String PREFS_NAME = "prefs";

    private static final String PREF_KEY_LAST_SYNC_TIME = "last_sync_time";
    private static final String PREF_KEY_LAST_POST_NOTIFICATION_TIME = "last_post_notification_time";

    private final Context context;
    private SharedPreferences preferences;

    public static Preferences getInstance(final @NonNull Context context) {
        if (instance == null) {
            synchronized(Preferences.class) {
                if (instance == null) {
                    instance = new Preferences(context);
                }
            }
        }
        return instance;
    }

    private Preferences(Context context) {
        this.context = context.getApplicationContext();
    }

    @WorkerThread
    private synchronized SharedPreferences getPreferences() {
        if (preferences == null) {
            preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        return preferences;
    }

    @WorkerThread
    public long getLastSyncTime() {
        return getPreferences().getLong(PREF_KEY_LAST_SYNC_TIME, 0);
    }

    @WorkerThread
    public void setLastSyncTime(long time) {
        if (!getPreferences().edit().putLong(PREF_KEY_LAST_SYNC_TIME, time).commit()) {
            Log.e("preferences: failed to set last sync time");
        }
    }

    @WorkerThread
    public long getLastPostNotificationTime() {
        return getPreferences().getLong(PREF_KEY_LAST_POST_NOTIFICATION_TIME, 0);
    }

    @WorkerThread
    public void setLastPostNotificationTime(long time) {
        if (!getPreferences().edit().putLong(PREF_KEY_LAST_POST_NOTIFICATION_TIME, time).commit()) {
            Log.e("preferences: failed to set last post notification time");
        }
    }
}
