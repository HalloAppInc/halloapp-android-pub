package com.halloapp;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.StrictMode;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.common.GoogleApiAvailability;
import com.halloapp.autodownload.DownloadableAssetManager;
import com.halloapp.calling.calling.CallManager;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.content.ContentDb;
import com.halloapp.emoji.EmojiManager;
import com.halloapp.nux.ZeroZoneManager;
import com.halloapp.permissions.PermissionObserver;
import com.halloapp.permissions.PermissionWatcher;
import com.halloapp.props.ServerProps;
import com.halloapp.registration.UnfinishedRegistrationWorker;
import com.halloapp.ui.BlurManager;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.HAThreadPolicyListener;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.PresenceManager;

import io.sentry.android.core.SentryAndroid;

public class App extends Application {

    private final AppContext appContext = AppContext.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();
        initSync();
        Log.i("halloapp: onCreate");
        // https://firebase.google.com/docs/test-lab/android/android-studio#modify_instrumented_test_behavior_for
        String runningInFirebaseTestLab = Settings.System.getString(getContentResolver(), "firebase.test.lab");
        Log.i("halloapp: running in firebase test lab? " + runningInFirebaseTestLab);

        if (ServerProps.getInstance().getIsInternalUser() || BuildConfig.DEBUG) {
            Preferences.getInstance().loadVideoOverride();
        }

        BgWorkers.getInstance().execute(() -> {
            AppCompatDelegate.setDefaultNightMode(Preferences.getInstance().getNightMode());
        });

        if (!BuildConfig.DEBUG) {
            Log.uploadUnsentReports();
            SentryAndroid.init(this);
        } else {
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyDeathOnNetwork();
            if (Build.VERSION.SDK_INT >= 28) {
                threadPolicyBuilder.penaltyListener(BgWorkers.getInstance().getExecutor(), new HAThreadPolicyListener());
            } else {
                if (AndroidHallOfShame.deviceDoesWorkOnUIThread()) {
                    threadPolicyBuilder.penaltyFlashScreen();
                } else {
                    threadPolicyBuilder.penaltyDeath();
                }
            }
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }
        CallManager.getInstance().init();
        ContentDraftManager.getInstance().init();
        ConnectionObservers.getInstance().addObserver(MainConnectionObserver.getInstance(this));
        ContentDb.getInstance().addObserver(MainContentDbObserver.getInstance(this));
        ContactsDb.getInstance().addObserver(new ContactsDb.BaseObserver() {
            @Override
            public void onContactsReset() {
                Preferences.getInstance().setLastFullContactSyncTime(0);
            }
        });

        Notifications.getInstance(this).init();

        connect();

        PermissionWatcher permissionWatcher = PermissionWatcher.getInstance();
        permissionWatcher.addObserver(new PermissionObserver() {

            @Override
            public void onWatchedPermissionGranted(@NonNull String permission) {
                if (Manifest.permission.READ_CONTACTS.equals(permission)) {
                    BgWorkers.getInstance().execute(() -> {
                        if (Preferences.getInstance().getLastFullContactSyncTime() > 0) {
                            ContactsSync.getInstance().startAddressBookListener();
                            ContactsSync.getInstance().forceFullContactsSync();
                        }
                    });
                }
            }
        });

        Lifecycle lifecycle = ProcessLifecycleOwner.get().getLifecycle();
        lifecycle.addObserver(new AppLifecycleObserver());
        lifecycle.addObserver(ForegroundObserver.getInstance());
        lifecycle.addObserver(permissionWatcher);

        DailyWorker.schedule(this);
        ScheduledContactSyncWorker.schedule(this);
        UnfinishedRegistrationWorker.schedule(this);
        if (Build.VERSION.SDK_INT >= 24 && ServerProps.getInstance().getMagicPostsEnabled()) {
            GalleryWorker.schedule(getApplicationContext());
        }

        new StartContactSyncTask(Preferences.getInstance(), ContactsSync.getInstance()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        BgWorkers.getInstance().execute(() -> {
            ContentDb.getInstance().processFutureProofContent();
            ContentDb.getInstance().checkIndexes();
            ContactsDb.getInstance().checkIndexes();
            ApkHasher.getInstance().run(this);
            ContentDb.getInstance().addMomentEntryPost();
            DownloadableAssetManager.getInstance().init(this);
            FriendshipSyncWorker.schedule(this);

            // 0 indicates success; see com.google.android.gms.common.ConnectionResult
            int playServicesConnectionResult = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
            Log.d("Play Services Connection Result " + playServicesConnectionResult);
        });
    }

    /**
     * Synchronous init, try to do as little work here as possible
     */
    private void initSync() {
        appContext.setApplicationContext(this);
        Log.init(FileStore.getInstance());
        Log.i("HalloApp init " + BuildConfig.VERSION_NAME + " " + BuildConfig.GIT_HASH);

        Log.wrapCrashlytics();

        // Init server props synchronously so we have the correct values loaded
        ServerProps.getInstance().init();
        Preferences.getInstance().init();
        Preferences.getInstance().ensureMigrated();

        EmojiManager.getInstance().init(this);
        BlurManager.getInstance().init();
        ZeroZoneManager.getInstance().init();
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
            Notifications notifications = Notifications.getInstance(App.this);
            notifications.setEnabled(true);
            notifications.updateFeedNotifications();
            mainHandler.postDelayed(disconnectOnBackgroundedRunnable, 20000);
            PresenceManager.getInstance().onBackground();
        }

        @SuppressWarnings("unused")
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        void onForeground() {
            Log.i("halloapp: onForeground");
            Notifications.getInstance(App.this).setEnabled(false);
            Connection.getInstance().resetConnectionBackoff();
            connect();
            registerReceiver(receiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            registerReceiver(airplaneModeChangeReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
            mainHandler.removeCallbacks(disconnectOnBackgroundedRunnable);
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            Log.i("halloapp: device power saving mode on? " + powerManager.isPowerSaveMode());
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
            return preferences.getLastFullContactSyncTime() > 0;
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
