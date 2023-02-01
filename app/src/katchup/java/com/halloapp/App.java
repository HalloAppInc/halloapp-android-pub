package com.halloapp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;

import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.halloapp.content.ContentDb;
import com.halloapp.emoji.EmojiManager;
import com.halloapp.katchup.Analytics;
import com.halloapp.katchup.KatchupConnectionObserver;
import com.halloapp.katchup.RelationshipSyncWorker;
import com.halloapp.katchup.Notifications;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.BlurManager;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

public class App extends Application {
    private final AppContext appContext = AppContext.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();
        initSync();

        BgWorkers.getInstance().execute(() -> {
            RelationshipSyncWorker.schedule(this);
            Analytics.getInstance().setUid(Me.getInstance().getUser());
        });
    }

    /**
     * Synchronous init, try to do as little work here as possible
     */
    private void initSync() {
        appContext.setApplicationContext(this);
        Log.init(FileStore.getInstance());
        Log.i("Katchup init " + BuildConfig.VERSION_NAME + " " + BuildConfig.GIT_HASH);

        Log.wrapCrashlytics();

        // Init server props synchronously so we have the correct values loaded
        ServerProps.getInstance().init();
        final Preferences preferences = Preferences.getInstance();
        preferences.init();

        EmojiManager.getInstance().init(this);
        BlurManager.getInstance().init();

        if (Build.VERSION.SDK_INT < 33 || NotificationManagerCompat.from(this).areNotificationsEnabled() || preferences.getOnboardingGetStartedShown()) {
            Notifications.getInstance(this).init();
        }

        Lifecycle lifecycle = ProcessLifecycleOwner.get().getLifecycle();
        lifecycle.addObserver(ForegroundObserver.getInstance());
        lifecycle.addObserver(new AppLifecycleObserver());

        ConnectionObservers.getInstance().addObserver(KatchupConnectionObserver.getInstance(this));
        ContentDb.getInstance().addObserver(MainContentDbObserver.getInstance(this));

        Analytics.getInstance().init(this);

        connect();
    }

    private void connect() {
        Connection.getInstance().connect();
        Log.setUser(Me.getInstance());
    }

    class AppLifecycleObserver implements LifecycleObserver {
        private final Runnable disconnectOnBackgroundedRunnable = () -> Connection.getInstance().disconnect();
        private final Handler mainHandler = new Handler(Looper.getMainLooper());

        private final AirplaneModeChangeReceiver airplaneModeChangeReceiver = new AirplaneModeChangeReceiver();
        private final NetworkChangeReceiver receiver = new NetworkChangeReceiver() {

            @Override
            public void onConnected(int type) {
                Log.i("katchup: network connected, type=" + type);
                connect();
            }

            @Override
            public void onDisconnected() {
                Log.i("katchup: network disconnected");
            }
        };

        @SuppressWarnings("unused")
        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        void onBackground() {
            Log.i("katchup: onBackground");
            Analytics.getInstance().logAppBackgrounded();
            unregisterReceiver(receiver);
            unregisterReceiver(airplaneModeChangeReceiver);
            mainHandler.postDelayed(disconnectOnBackgroundedRunnable, 20000);
        }

        @SuppressWarnings("unused")
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        void onForeground() {
            Log.i("katchup: onForeground");
            Analytics.getInstance().logAppForegrounded();
            Connection.getInstance().resetConnectionBackoff();
            connect();
            registerReceiver(receiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            registerReceiver(airplaneModeChangeReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
            mainHandler.removeCallbacks(disconnectOnBackgroundedRunnable);
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            Log.i("katchup: device power saving mode on? " + powerManager.isPowerSaveMode());
        }
    }
}
