package com.halloapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.crashlytics.android.Crashlytics;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.media.MediaStore;
import com.halloapp.posts.PostsDb;
import com.halloapp.util.Log;

public class HalloApp extends Application {

    public static HalloApp instance;

    public Connection connection;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("halloapp: onCreate");

        instance = this;

        Crashlytics.setBool("debug", BuildConfig.DEBUG);

        final PostsDb postsDb = PostsDb.getInstance(this);
        connection = Connection.getInstance(new ConnectionObserver(this, postsDb));
        final MainPostsObserver mainPostsObserver = MainPostsObserver.getInstance(connection, MediaStore.getInstance(this), postsDb);
        postsDb.addObserver(mainPostsObserver);

        connect();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new LifecycleObserver() {

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            void onBackground() {
                Log.i("halloapp: onBackground");
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            void onForeground() {
                Log.i("halloapp: onForeground");
                connect();
            }
        });

        final ContactsDb contactsDb = ContactsDb.getInstance(this);
        contactsDb.syncAddressBook();
        getContentResolver().registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, true, new ContentObserver(null) {

            public void onChange(boolean selfChange, Uri uri) {
                Log.i("halloapp: changed " + uri);
                contactsDb.syncAddressBook();
            }
        });
    }

    private void connect() {
        if (!isRegistered()) {
            Log.i("halloapp: not registered");
            return;
        }
        connection.connect(getUser(), getPassword());

        Crashlytics.setString("user", getUser());
    }

    public void disconnect() {
        connection.disconnect();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w("low memory");
    }

    private static final String PREF_KEY_USER_ID = "user_id";
    private static final String PREF_KEY_PASSWORD = "password";

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
}
