package com.halloapp;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.iid.FirebaseInstanceId;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.content.ContentDb;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.props.ServerProps;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.LanguageUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

public class HalloApp extends Application {

    private final AppContext appContext = AppContext.getInstance();

    private boolean hasContactPermission;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("halloapp: onCreate");
        initSync();

        if (ServerProps.getInstance().getIsInternalUser() || BuildConfig.DEBUG) {
            Preferences.getInstance().loadVideoOverride();
        }

        if (!BuildConfig.DEBUG) {
            Log.uploadUnsentReports();
        } else {
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyDeathOnNetwork();
            if (AndroidHallOfShame.deviceDoesWorkOnUIThread()) {
                threadPolicyBuilder.penaltyFlashScreen();
            } else {
                threadPolicyBuilder.penaltyDeath();
            }
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }

        ConnectionObservers.getInstance().addObserver(MainConnectionObserver.getInstance(this));
        ContentDb.getInstance().addObserver(MainContentDbObserver.getInstance(this));
        ContactsDb.getInstance().addObserver(new ContactsDb.BaseObserver() {
            @Override
            public void onContactsReset() {
                Preferences.getInstance().setLastContactsSyncTime(0);
            }
        });

        Notifications.getInstance(this).init();

        connect();

        checkContactsPermissionChanged();

        Lifecycle lifecycle = ProcessLifecycleOwner.get().getLifecycle();
        lifecycle.addObserver(new AppLifecycleObserver());
        lifecycle.addObserver(ForegroundObserver.getInstance());

        DailyWorker.schedule(this);

        new StartContactSyncTask(Preferences.getInstance(), ContactsSync.getInstance()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        BgWorkers.getInstance().execute(() -> {
            ContentDb.getInstance().fixGroupMembership();
            ContentDb.getInstance().processFutureProofContent();
            EncryptedKeyStore.getInstance().ensureMigrated(); // TODO(jack): Remove after May 1
        });
    }

    private boolean checkContactsPermissionChanged() {
        if (!hasContactPermission) {
            hasContactPermission = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(HalloApp.this, Manifest.permission.READ_CONTACTS);
            return hasContactPermission;
        } else {
            return false;
        }
    }

    /**
     * Synchronous init, try to do as little work here as possible
     */
    private void initSync() {
        appContext.setApplicationContext(this);
        Log.init(FileStore.getInstance());
        Log.i("HalloApp init " + BuildConfig.VERSION_NAME);

        Log.wrapCrashlytics();

        // Init server props synchronously so we have the correct values loaded
        ServerProps.getInstance().init();

        Preferences.getInstance().ensureMigrated();
    }

    private void connect() {
        Connection.getInstance().connect();
        Log.setUser(Me.getInstance());
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w("halloapp: low memory");
    }

    public static void updateFirebasePushTokenIfNeeded() {
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

                        String locale = LanguageUtils.getLocaleIdentifier();

                        String savedLocale = Preferences.getInstance().getLastDeviceLocale();
                        String savedToken = Preferences.getInstance().getLastPushToken();
                        long lastUpdateTime = Preferences.getInstance().getLastPushTokenSyncTime();
                        if (!Preconditions.checkNotNull(pushToken).equals(savedToken)
                                || !locale.equals(savedLocale)
                                || System.currentTimeMillis() - lastUpdateTime > Constants.PUSH_TOKEN_RESYNC_TIME) {
                            Connection.getInstance().sendPushToken(pushToken, locale);
                        } else {
                            Log.i("halloapp: no need to sync push token");
                        }
                    }
                });
    }

    class AppLifecycleObserver implements LifecycleObserver {
        private final Runnable disconnectOnBackgroundedRunnable = () -> Connection.getInstance().disconnect();
        private final Handler mainHandler = new Handler(Looper.getMainLooper());

        private final AirplaneModeChangeReceiver airplaneModeChangeReceiver = new AirplaneModeChangeReceiver();
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
            unregisterReceiver(airplaneModeChangeReceiver);
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
            registerReceiver(airplaneModeChangeReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
            mainHandler.removeCallbacks(disconnectOnBackgroundedRunnable);
            Connection.getInstance().updatePresence(true);

            if (checkContactsPermissionChanged()) {
                // We got contacts permission
                ContactsSync.getInstance().startAddressBookListener();
                ContactsSync.getInstance().startContactsSync(true);
            }
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
