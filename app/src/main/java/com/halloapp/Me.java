package com.halloapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;

import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.id.UserId;
import com.halloapp.props.ServerProps;
import com.halloapp.util.Preconditions;
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

    private static final String PREF_KEY_MY_ED25519_NOISE_KEY = "my_ed25519_noise_key";
    private static final String PREF_KEY_SERVER_PUBLIC_STATIC_KEY = "server_public_static_key";

    private final AppContext appContext;
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
    private SharedPreferences getPreferences() {
        return getPreferences(true);
    }

    @WorkerThread
    private synchronized SharedPreferences getPreferences(boolean allowRecurse) {
        final Context context = appContext.get();
        if (preferences == null) {
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    MasterKey masterKey = new MasterKey.Builder(context)
                            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                            .build();

                    preferences = EncryptedSharedPreferences.create(
                            context,
                            FILE_NAME,
                            masterKey,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    );
                } catch (GeneralSecurityException | IOException e) {
                    Log.e("Me.getPreferences failed to create", e);

                    // Handle case where keys are lost (i.e. restore happened using backup from before backup rules were in place)
                    if (allowRecurse) {
                        reset(context);
                        preferences = getPreferences(false);
                    } else {
                        Log.sendErrorReport("Me prefs reset failure");
                    }
                }

            } else {
                preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            }
        }
        return preferences;
    }

    @SuppressLint("ApplySharedPref")
    @WorkerThread
    private void reset(Context context) {
        Log.i("Me.getPreferences resetting preferences");
        SharedPreferences tmp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        tmp.edit().clear().commit();
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
        // TODO (clarkc) Remove getPassword() when migration is finished.
        return !TextUtils.isEmpty(getUser()) && !TextUtils.isEmpty(getName()) && (getMyEd25519NoiseKey() != null || !TextUtils.isEmpty(getPassword()));
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
    public synchronized void saveRegistrationNoise(@NonNull String user, @NonNull String phone) {
        if (!getPreferences().edit().putString(PREF_KEY_USER_ID, user).putString(PREF_KEY_PHONE, phone).commit()) {
            Log.e("Me.saveRegistration: failed");
        } else {
            this.user.postValue(user);
        }
    }


    @WorkerThread
    public synchronized void resetRegistration() {
        if (!getPreferences().edit().remove(PREF_KEY_PASSWORD).remove(PREF_KEY_PHONE).remove(PREF_KEY_MY_ED25519_NOISE_KEY).commit()) {
            Log.e("Me.resetRegistration: failed");
        } else {

            this.user.postValue(null);
        }
    }

    public void saveNoiseKey(byte[] noiseKeyPair) {
        setMyEd25519NoiseKey(noiseKeyPair);
    }

    public void setServerStaticKey(byte[] publicServerStaticKey) {
        storeBytes(PREF_KEY_SERVER_PUBLIC_STATIC_KEY, publicServerStaticKey);
    }

    @Nullable
    public PublicEdECKey getServerStaticKey() {
        byte[] serverStaticKey = retrieveBytes(PREF_KEY_SERVER_PUBLIC_STATIC_KEY);
        if (serverStaticKey == null) {
            return null;
        }
        return new PublicEdECKey(serverStaticKey);
    }

    private void setMyEd25519NoiseKey(byte[] key) {
        storeBytes(PREF_KEY_MY_ED25519_NOISE_KEY, key);
    }

    public byte[] getMyEd25519NoiseKey() {
        return retrieveBytes(PREF_KEY_MY_ED25519_NOISE_KEY);
    }

    private void storeBytes(String prefKey, byte[] bytes) {
        getPreferences().edit().putString(prefKey, bytesToString(bytes)).apply();
    }

    @Nullable
    private byte[] retrieveBytes(String prefKey) {
        String stored = getPreferences().getString(prefKey, null);
        return stringToBytes(stored);
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
}
