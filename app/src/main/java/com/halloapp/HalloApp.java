package com.halloapp;

import android.app.Application;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.posts.PostsDb;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;

import io.fabric.sdk.android.Fabric;

public class HalloApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("halloapp: onCreate");

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        } else {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyDeath()
                    .penaltyDeathOnNetwork()
                    .build());
        }

        Connection.getInstance().setObserver(new ConnectionObserver(this));
        PostsDb.getInstance(this).addObserver(MainPostsObserver.getInstance(this));
        ContactsDb.getInstance(this).addObserver(new ContactsDb.Observer() {

            @Override
            public void onContactsChanged() {
            }

            @Override
            public void onContactsReset() {
                Preferences.getInstance(HalloApp.this).setLastSyncTime(0);
            }
        });

        Notifications.getInstance(this).init();

        connect();

        //noinspection unused
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new LifecycleObserver() {

            final NetworkChangeReceiver receiver = new NetworkChangeReceiver() {
                public void onConnected(int type) {
                    connect();
                }

                public void onDisconnected() {
                    // do nothing
                }
            };

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            void onBackground() {
                Log.i("halloapp: onBackground");
                unregisterReceiver(receiver);
                Notifications.getInstance(HalloApp.this).setEnabled(true);
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            void onForeground() {
                Log.i("halloapp: onForeground");
                Notifications.getInstance(HalloApp.this).setEnabled(false);
                connect();
                registerReceiver(receiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            }
        });

        DailyWorker.schedule(this);

        new StartContactSyncTask(Preferences.getInstance(this), ContactsSync.getInstance(this)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void connect() {
        Connection.getInstance().connect(Me.getInstance(this));
        Log.setUser(Me.getInstance(this));
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w("halloapp: low memory");
    }

    public static void sendPushTokenFromFirebase() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e( "halloapp: getInstanceId failed", task.getException());
                        return;
                    }
                    // Get the Instance ID token.
                    final String pushToken = task.getResult() == null ? null : task.getResult().getToken();
                    if (TextUtils.isEmpty(pushToken)) {
                        Log.e("halloapp: error getting push token");
                    } else {
                        Log.d("halloapp: obtained the push token!");
                        Connection.getInstance().sendPushToken(pushToken);
                    }
                });
    }

    static class StartContactSyncTask extends AsyncTask<Void, Void, Boolean> {

        private final Preferences preferences;
        private final ContactsSync contactsSync;

        StartContactSyncTask(@NonNull Preferences preferences, @NonNull ContactsSync contactsSync) {
            this.preferences = preferences;
            this.contactsSync = contactsSync;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return preferences.getLastSyncTime() > 0;
        }

        @Override
        protected void onPostExecute(Boolean hadContactSync) {
            if (hadContactSync) {
                contactsSync.startAddressBookListener();
                contactsSync.startAddressBookSync();
            }
        }
    }
}
