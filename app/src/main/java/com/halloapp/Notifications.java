package com.halloapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.ui.MainActivity;
import com.halloapp.util.ListFormatter;
import com.halloapp.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Notifications {

    private static Notifications instance;

    private static final String NEW_POST_NOTIFICATION_CHANNEL_ID = "new_post_notification";
    private static final int NEW_POST_NOTIFICATION_ID = 0;

    private static final int UNSEEN_POSTS_LIMIT = 256;

    private final Context context;
    private final Preferences preferences;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private long lastPostTime;

    public static Notifications getInstance(final @NonNull Context context) {
        if (instance == null) {
            synchronized(Notifications.class) {
                if (instance == null) {
                    instance = new Notifications(context);
                }
            }
        }
        return instance;
    }

    private Notifications(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = Preferences.getInstance(context);
    }

    public void init() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= 26) {
            final CharSequence name = context.getString(R.string.notification_channel_name);
            final String description = context.getString(R.string.notification_channel_description);
            final int importance = NotificationManager.IMPORTANCE_DEFAULT;
            final NotificationChannel channel = new NotificationChannel(NEW_POST_NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void clear() {
        executor.execute(() -> {
            if (lastPostTime != 0) {
                preferences.setLastPostNotificationTime(lastPostTime);
            }
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(NEW_POST_NOTIFICATION_ID);
        });
    }

    public void update() {
        executor.execute(() -> {

            final List<Post> unseenPosts = PostsDb.getInstance(context).getUnseenPosts(preferences.getLastPostNotificationTime(), UNSEEN_POSTS_LIMIT);
            if (unseenPosts.isEmpty()) {
                final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.cancelAll();
                return;
            }
            final Set<UserId> userIds = new HashSet<>();
            for (Post post : unseenPosts) {
                Log.d("Notifications.update: " + post);
                userIds.add(post.senderUserId);
                if (post.timestamp > lastPostTime) {
                    lastPostTime = post.timestamp;
                }
            }
            final List<String> names = new ArrayList<>();
            for (UserId userId : userIds) {
                Contact contact = ContactsDb.getInstance(context).getContact(userId);
                if (contact == null) {
                    contact = new Contact(userId);
                }
                names.add(contact.getDisplayName());
            }
            final String text;
            if (unseenPosts.size() == 1) {
                text = context.getString(R.string.new_post_notification, names.get(0));
            } else {
                text = context.getResources().getQuantityString(R.plurals.new_posts_notification, unseenPosts.size(), unseenPosts.size(), ListFormatter.format(context, names));
            }
            showNotification(context.getString(R.string.app_name), text, lastPostTime);
        });
    }

    private void showNotification(@NonNull String title, @NonNull String body, long lastPostTime) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NEW_POST_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        final Intent contentIntent = new Intent(context, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        final Intent deleteIntent = new Intent(context, DeleteNotificationReceiver.class);
        deleteIntent.putExtra("last_post_time", lastPostTime) ;
        builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0 , deleteIntent, PendingIntent. FLAG_CANCEL_CURRENT));
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // Using the same notification-id to always show the latest notification for now.
        notificationManager.notify(NEW_POST_NOTIFICATION_ID, builder.build());
    }


    static public class DeleteNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive (Context context , Intent intent) {
            Log.i("Notifications.BroadcastReceiver: cancel");
            long lastPostTime = intent.getLongExtra("last_post_time", 0);
            if (lastPostTime > 0) {
                Log.i("Notifications.BroadcastReceiver: cancel, last post is at " + lastPostTime);
                Notifications.getInstance(context).executor.execute(() -> {
                    Notifications.getInstance(context).preferences.setLastPostNotificationTime(lastPostTime);
                });
            }
        }
    }
}
