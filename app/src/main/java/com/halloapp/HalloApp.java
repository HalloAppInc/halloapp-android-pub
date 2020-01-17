package com.halloapp;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.crashlytics.android.Crashlytics;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.posts.PostsDb;
import com.halloapp.util.Log;

public class HalloApp extends Application {

    public static HalloApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("halloapp: onCreate");

        instance = this;

        Crashlytics.setBool("debug", BuildConfig.DEBUG);

        Connection.getInstance().setObserver(new ConnectionObserver(this));
        PostsDb.getInstance(this).addObserver(MainPostsObserver.getInstance(this));

        connect();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new LifecycleObserver() {

            NetworkChangeReceiver receiver = new NetworkChangeReceiver() {
                public void onConnected(int type) {
                    connect();
                }

                public void onDisconnected() {
                }
            };

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            void onBackground() {
                Log.i("halloapp: onBackground");
                unregisterReceiver(receiver);
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            void onForeground() {
                Log.i("halloapp: onForeground");
                connect();
                registerReceiver(receiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            }
        });

        if (getLastSyncTime() > 0) {
            ContactsSync.getInstance(this).startAddressBookListener();
            ContactsSync.getInstance(this).startAddressBookSync();
        }
    }

    private void connect() {
        if (!isRegistered()) {
            Log.i("halloapp: not registered");
            return;
        }
        Connection.getInstance().connect(getUser(), getPassword());

        Crashlytics.setString("user", getUser());
    }

    public void disconnect() {
        Connection.getInstance().disconnect();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w("low memory");
    }

    private static final String PREF_KEY_USER_ID = "user_id";
    private static final String PREF_KEY_PASSWORD = "password";
    private static final String PREF_KEY_LAST_SYNC_TIME = "last_sync_time";

    public SharedPreferences getPreferences() {
        return getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    public boolean isRegistered() {
        return !TextUtils.isEmpty(getUser()) && !TextUtils.isEmpty(getPassword());
    }

    public String getUser() {
        return getPreferences().getString(PREF_KEY_USER_ID, null);
    }

    public String getPassword() {
        return getPreferences().getString(PREF_KEY_PASSWORD, null);
    }

    public void saveRegistration(@NonNull String user, @NonNull String password) {
        if (!getPreferences().edit().putString(PREF_KEY_USER_ID, user).putString(PREF_KEY_PASSWORD, password).commit()) {
            Log.e("failed to save registration");
        }
        connect();
    }

    public void resetRegistration() {
        if (!getPreferences().edit().remove(PREF_KEY_USER_ID).remove(PREF_KEY_PASSWORD).commit()) {
            Log.e("failed to reset registration");
        }
    }

    public long getLastSyncTime() {
        return getPreferences().getLong(PREF_KEY_LAST_SYNC_TIME, 0);
    }

    public void setLastSyncTime(long time) {
        if (!getPreferences().edit().putLong(PREF_KEY_LAST_SYNC_TIME, time).commit()) {
            Log.e("failed to set last sync time");
        }
    }
}
