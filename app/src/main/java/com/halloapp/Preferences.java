package com.halloapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.util.Log;

public class Preferences {

    private static Preferences instance;

    private static final String PREF_KEY_USER_ID = "user_id";
    private static final String PREF_KEY_PASSWORD = "password";
    private static final String PREF_KEY_LAST_SYNC_TIME = "last_sync_time";

    private final SharedPreferences preferences;

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
        final StrictMode.ThreadPolicy threadPolicy = StrictMode.allowThreadDiskReads();
        try {
            preferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        } finally {
            StrictMode.setThreadPolicy(threadPolicy);
        }
    }

    public boolean isRegistered() {
        return !TextUtils.isEmpty(getUser()) && !TextUtils.isEmpty(getPassword());
    }

    public String getUser() {
        return getString(PREF_KEY_USER_ID, null);
    }

    public String getPassword() {
        return getString(PREF_KEY_PASSWORD, null);
    }

    public void saveRegistration(@NonNull String user, @NonNull String password) {
        if (!preferences.edit().putString(PREF_KEY_USER_ID, user).putString(PREF_KEY_PASSWORD, password).commit()) {
            Log.e("preferences: failed to save registration");
        }
    }

    public void resetRegistration() {
        if (!preferences.edit().remove(PREF_KEY_USER_ID).remove(PREF_KEY_PASSWORD).commit()) {
            Log.e("preferences: failed to reset registration");
        }
    }

    public long getLastSyncTime() {
        return getLong(PREF_KEY_LAST_SYNC_TIME, 0);
    }

    public void setLastSyncTime(long time) {
        if (!preferences.edit().putLong(PREF_KEY_LAST_SYNC_TIME, time).commit()) {
            Log.e("preferences: failed to set last sync time");
        }
    }

    private String getString(@NonNull String key, @Nullable String defValue) {
        final StrictMode.ThreadPolicy threadPolicy = StrictMode.allowThreadDiskReads();
        try {
            return preferences.getString(key, defValue);
        } finally {
            StrictMode.setThreadPolicy(threadPolicy);
        }
    }

    private long getLong(@NonNull String key, long defValue) {
        final StrictMode.ThreadPolicy threadPolicy = StrictMode.allowThreadDiskReads();
        try {
            return preferences.getLong(key, defValue);
        } finally {
            StrictMode.setThreadPolicy(threadPolicy);
        }
    }
}
