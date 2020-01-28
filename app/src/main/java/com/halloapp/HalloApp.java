package com.halloapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.posts.PostsDb;
import com.halloapp.ui.MainActivity;
import com.halloapp.util.Log;

import io.fabric.sdk.android.Fabric;

public class HalloApp extends Application {

    public static HalloApp instance;
    public boolean appActiveStatus;

    private static String NEW_POST_NOTIFICATION_CHANNEL_ID = "new_post_notification";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("halloapp: onCreate");

        instance = this;

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
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

        createNotificationChannel();

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
                appActiveStatus = false;
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            void onForeground() {
                Log.i("halloapp: onForeground");
                appActiveStatus = true;
                connect();
                registerReceiver(receiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            }
        });

        if (Preferences.getInstance(this).getLastSyncTime() > 0) {
            ContactsSync.getInstance(this).startAddressBookListener();
            ContactsSync.getInstance(this).startAddressBookSync();
        }
    }

    private void connect() {
        if (!Preferences.getInstance(this).isRegistered()) {
            Log.i("halloapp: not registered");
            return;
        }
        Connection.getInstance().connect(Preferences.getInstance(this).getUser(), Preferences.getInstance(this).getPassword());

        if (Fabric.isInitialized()) {
            Crashlytics.setString("user", Preferences.getInstance(this).getUser());
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w("halloapp: low memory");
    }

    public void sendPushTokenFromFirebase() {
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

    public void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final CharSequence name = getString(R.string.notification_channel_name);
            final String description = getString(R.string.notification_channel_description);
            final int importance = NotificationManager.IMPORTANCE_DEFAULT;
            final NotificationChannel channel = new NotificationChannel(NEW_POST_NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showNotification(String title, String body) {
        if (Build.VERSION.SDK_INT >= 26) {
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NEW_POST_NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            final Intent intent = new Intent(this, MainActivity.class);
            final PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pi);
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            // Using the same notification-id to always show the latest notification for now.
            notificationManager.notify(0, builder.build());
        }
    }

    public void cancelAllNotifications() {
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancelAll();
    }
}
