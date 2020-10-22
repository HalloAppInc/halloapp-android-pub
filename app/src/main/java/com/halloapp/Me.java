package com.halloapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.util.logs.Log;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Objects;

public class Me {

    private static Me instance;

    private static final String FILE_NAME = "me";

    private static final String PREF_KEY_USER_ID = "user_id";
    private static final String PREF_KEY_PASSWORD = "password";
    private static final String PREF_KEY_PHONE = "phone";
    private static final String PREF_KEY_NAME = "name";

    private AppContext appContext;
    private SharedPreferences preferences;

    public final MutableLiveData<String> name = new MutableLiveData<>();
    public final MutableLiveData<String> user = new MutableLiveData<>();

    public static Me getInstance() {
        if (instance == null) {
            synchronized(Me.class) {
                if (instance == null) {
                    instance = new Me(AppContext.getInstance());
                }
            }
        }
        return instance;
    }

    private Me(final @NonNull AppContext appContext) {
        this.appContext = appContext;
    }

    @WorkerThread
    private synchronized SharedPreferences getPreferences() {
        final Context context = appContext.get();
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
        final Context context = appContext.get();
        if (preferences == null) {
            if (Build.VERSION.SDK_INT >= 23) {
                // EncryptedSharedPreferences.create is very slow call, try to use regular shared prefs to detect if any data is there
                final SharedPreferences tmpPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
                if (tmpPreferences.getAll().isEmpty()) {
                    return false;
                }
            }
        }
        return !TextUtils.isEmpty(getUser()) && !TextUtils.isEmpty(getPassword()) && !TextUtils.isEmpty(getName());
    }

    @WorkerThread
    public synchronized String getUser() {
        final String user = getPreferences().getString(PREF_KEY_USER_ID, null);
        if (!Objects.equals(this.user.getValue(), user)) {
            this.user.postValue(user);
        }
        return user;
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
    public synchronized String getName() {
        final String name = getPreferences().getString(PREF_KEY_NAME, null);
        if (!Objects.equals(this.name.getValue(), name)) {
            this.name.postValue(name);
        }
        return name;
    }

    @WorkerThread
    public synchronized void saveName(@NonNull String name) {
        Log.i("Me.saveName: " + name);
        if (!getPreferences().edit().putString(PREF_KEY_NAME, name).commit()) {
            Log.e("Me.saveName: failed");
        } else {
            this.name.postValue(name);
        }
    }

    @WorkerThread
    public synchronized void saveRegistration(@NonNull String user, @NonNull String password, @NonNull String phone) {
        Log.i("Me.saveRegistration: " + user + " " + password);
        if (!getPreferences().edit().putString(PREF_KEY_USER_ID, user).putString(PREF_KEY_PASSWORD, password).putString(PREF_KEY_PHONE, phone).commit()) {
            Log.e("Me.saveRegistration: failed");
        } else {
            EncryptedKeyStore.getInstance().setKeysUploaded(false);
            this.user.postValue(user);
        }
    }

    @WorkerThread
    public synchronized void resetRegistration() {
        if (!getPreferences().edit().remove(PREF_KEY_PASSWORD).remove(PREF_KEY_PHONE).commit()) {
            Log.e("Me.resetRegistration: failed");
        } else {
            this.user.postValue(null);
        }
    }
}
