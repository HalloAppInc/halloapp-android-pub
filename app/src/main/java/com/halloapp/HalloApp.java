package com.halloapp;

import android.app.Application;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.content.ContentDb;
import com.halloapp.crypto.EncryptedSessionManager;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;

public class HalloApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("halloapp: onCreate");

        if (!BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        } else {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
//                    .penaltyDeath()
                    .penaltyDeathOnNetwork()
                    .build());
        }

        Connection.getInstance().setObserver(new ConnectionObserver(this));
        ContentDb.getInstance(this).addObserver(MainContentDbObserver.getInstance(this));
        ContactsDb.getInstance(this).addObserver(new ContactsDb.Observer() {

            @Override
            public void onContactsChanged() {
            }

            @Override
            public void onContactsReset() {
                Preferences.getInstance(HalloApp.this).setLastContactsSyncTime(0);
            }
        });

        EncryptedSessionManager.getInstance().init(this);

        Notifications.getInstance(this).init();

        connect();

        Lifecycle lifecycle = ProcessLifecycleOwner.get().getLifecycle();
        lifecycle.addObserver(new AppLifecycleObserver());
        lifecycle.addObserver(ForegroundObserver.getInstance());

        DailyWorker.schedule(this);

        new StartContactSyncTask(Preferences.getInstance(this), ContactsSync.getInstance(this)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void connect() {
        Connection.getInstance().connect(this);
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

    class AppLifecycleObserver implements LifecycleObserver {
        private final Runnable disconnectOnBackgroundedRunnable = () -> Connection.getInstance().disconnect();
        private final Handler mainHandler = new Handler(Looper.getMainLooper());

        private final NetworkChangeReceiver receiver = new NetworkChangeReceiver() {

            @Override
            public void onConnected(int type) {
                Log.i("halloapp: network connected, type=" + type);
                connect();
            }

            @Override
            public void onDisconnected() {
                Log.i("halloapp: network disconnected");
            }
        };

        @SuppressWarnings("unused")
        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        void onBackground() {
            Log.i("halloapp: onBackground");
            unregisterReceiver(receiver);
            Notifications.getInstance(HalloApp.this).setEnabled(true);
            mainHandler.postDelayed(disconnectOnBackgroundedRunnable, 20000);
            Connection.getInstance().updatePresence(false);
        }

        @SuppressWarnings("unused")
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        void onForeground() {
            Log.i("halloapp: onForeground");
            Notifications.getInstance(HalloApp.this).setEnabled(false);
            connect();
            registerReceiver(receiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            mainHandler.removeCallbacks(disconnectOnBackgroundedRunnable);
            Connection.getInstance().updatePresence(true);
        }
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
            return preferences.getLastContactsSyncTime() > 0;
        }

        @Override
        protected void onPostExecute(Boolean hadContactSync) {
            if (hadContactSync) {
                contactsSync.startAddressBookListener();
                contactsSync.startContactsSync(false);
            }
        }
    }
}
