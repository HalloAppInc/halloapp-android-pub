package com.halloapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.halloapp.util.Log;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Me {

    private static Me instance;

    private static final String FILE_NAME = "me";

    private static final String PREF_KEY_USER_ID = "user_id";
    private static final String PREF_KEY_PASSWORD = "password";
    private static final String PREF_KEY_PHONE = "phone";

    private final Context context;
    private SharedPreferences preferences;

    public static Me getInstance(final @NonNull Context context) {
        if (instance == null) {
            synchronized(Me.class) {
                if (instance == null) {
                    instance = new Me(context);
                }
            }
        }
        return instance;
    }

    private Me(final @NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @WorkerThread
    private synchronized SharedPreferences getPreferences() {
        if (preferences == null) {
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    preferences = EncryptedSharedPreferences.create(
                            FILE_NAME,
                            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                            context,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    );
                } catch (GeneralSecurityException | IOException e) {
                    Log.e("Me.getPreferences", e);
                }
            } else {
                preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            }
        }
        return preferences;
    }

    @WorkerThread
    public synchronized boolean isRegistered() {
        if (preferences == null) {
            if (Build.VERSION.SDK_INT >= 23) {
                // EncryptedSharedPreferences.create is very slow call, try to use regular chared prefs to detect if any data is there
                final SharedPreferences tmpPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
                if (tmpPreferences.getAll().isEmpty()) {
                    return false;
                }
            }
        }
        return !TextUtils.isEmpty(getUser()) && !TextUtils.isEmpty(getPassword());
    }

    @WorkerThread
    public synchronized String getUser() {
        return getPreferences().getString(PREF_KEY_USER_ID, null);
    }

    @WorkerThread
    public synchronized String getPassword() {
        return getPreferences().getString(PREF_KEY_PASSWORD, null);
    }

    @WorkerThread
    public synchronized String getPhone() {
        return getPreferences().getString(PREF_KEY_PHONE, null);
    }

    @WorkerThread
    public synchronized void saveRegistration(@NonNull String user, @NonNull String password, @NonNull String phone) {
        Log.i("Me.saveRegistration: " + user + " " + password);
        if (!getPreferences().edit().putString(PREF_KEY_USER_ID, user).putString(PREF_KEY_PASSWORD, password).putString(PREF_KEY_PHONE, phone).commit()) {
            Log.e("Me.saveRegistration: failed");
        }
    }

    @WorkerThread
    public synchronized void resetRegistration() {
        if (!getPreferences().edit().remove(PREF_KEY_USER_ID).remove(PREF_KEY_PASSWORD).remove(PREF_KEY_PHONE).commit()) {
            Log.e("Me.resetRegistration: failed");
        }
    }
}
