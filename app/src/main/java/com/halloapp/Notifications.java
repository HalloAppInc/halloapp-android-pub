package com.halloapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.posts.Comment;
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

    private static final String FEED_NOTIFICATION_CHANNEL_ID = "feed_notifications";
    private static final int FEED_NOTIFICATION_ID = 0;

    private static final int UNSEEN_POSTS_LIMIT = 256;
    private static final int UNSEEN_COMMENTS_LIMIT = 64;

    private static final String EXTRA_FEED_NOTIFICATION_TIME_CUTOFF = "last_feed_notification_time";

    private final Context context;
    private final Preferences preferences;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private long feedNotificationTimeCutoff;

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
            final NotificationChannel channel = new NotificationChannel(FEED_NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.createNotificationChannel(channel);

            notificationManager.deleteNotificationChannel("new_post_notification"); // TODO (ds): remove
        }
    }

    public void clear() {
        executor.execute(() -> {
            if (feedNotificationTimeCutoff != 0) {
                preferences.setFeedNotificationTimeCutoff(feedNotificationTimeCutoff);
            }
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(FEED_NOTIFICATION_ID);
        });
    }

    public void update() {
        executor.execute(() -> {
            final String newPostsNotificationText = getNewPostsNotificationText();
            final String newCommentsNotificationText = getNewCommentsNotificationText();
            if (TextUtils.isEmpty(newPostsNotificationText) && TextUtils.isEmpty(newCommentsNotificationText)) {
                final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.cancelAll();
            } else {
                final String text;
                if (TextUtils.isEmpty(newCommentsNotificationText) && !TextUtils.isEmpty(newPostsNotificationText)) {
                    text = newPostsNotificationText;
                } else if (TextUtils.isEmpty(newPostsNotificationText) && !TextUtils.isEmpty(newCommentsNotificationText)) {
                    text = newCommentsNotificationText;
                } else {
                    text = context.getString(R.string.new_posts_and_comments_notification, newPostsNotificationText, newCommentsNotificationText);
                }
                showNotification(context.getString(R.string.app_name), text);
            }
        });
    }

    private String getNewPostsNotificationText() {
        if (!preferences.getNotifyPosts()) {
            return null;
        }
        final List<Post> unseenPosts = PostsDb.getInstance(context).getUnseenPosts(preferences.getFeedNotificationTimeCutoff(), UNSEEN_POSTS_LIMIT);
        if (unseenPosts.isEmpty()) {
            return null;
        }
        final Set<UserId> userIds = new HashSet<>();
        for (Post post : unseenPosts) {
            Log.d("Notifications.update: " + post);
            userIds.add(post.senderUserId);
            if (post.timestamp > feedNotificationTimeCutoff) {
                feedNotificationTimeCutoff = post.timestamp;
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
        return text;
    }

    private String getNewCommentsNotificationText() {
        if (!preferences.getNotifyComments()) {
            return null;
        }
        final List<Comment> unseenComments = PostsDb.getInstance(context).getUnseenCommentsOnMyPosts(preferences.getFeedNotificationTimeCutoff(), UNSEEN_COMMENTS_LIMIT);
        if (unseenComments.isEmpty()) {
            return null;
        }
        final Set<UserId> userIds = new HashSet<>();
        final Set<String> postIds = new HashSet<>();
        for (Comment comment : unseenComments) {
            Log.d("Notifications.update: " + comment);
            userIds.add(comment.commentSenderUserId);
            postIds.add(comment.postId);
            if (comment.timestamp > feedNotificationTimeCutoff) {
                feedNotificationTimeCutoff = comment.timestamp;
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
        return postIds.size() == 1 ? context.getString(R.string.new_comments_notification, ListFormatter.format(context, names)) :
                context.getString(R.string.new_comments_on_multiple_posts_notification, ListFormatter.format(context, names));
    }

    private void showNotification(@NonNull String title, @NonNull String body) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FEED_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        final Intent contentIntent = new Intent(context, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        final Intent deleteIntent = new Intent(context, DeleteNotificationReceiver.class);
        deleteIntent.putExtra(EXTRA_FEED_NOTIFICATION_TIME_CUTOFF, feedNotificationTimeCutoff) ;
        builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0 , deleteIntent, PendingIntent. FLAG_CANCEL_CURRENT));
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // Using the same notification-id to always show the latest notification for now.
        notificationManager.notify(FEED_NOTIFICATION_ID, builder.build());
    }


    static public class DeleteNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive (Context context , Intent intent) {
            Log.i("Notifications.BroadcastReceiver: cancel");
            final long feedNotificationTimeCutoff = intent.getLongExtra(EXTRA_FEED_NOTIFICATION_TIME_CUTOFF, 0);
            if (feedNotificationTimeCutoff > 0) {
                Log.i("Notifications.BroadcastReceiver: cancel, notification cutoff at " + feedNotificationTimeCutoff);
                Notifications.getInstance(context).executor.execute(() -> {
                    Notifications.getInstance(context).preferences.setFeedNotificationTimeCutoff(feedNotificationTimeCutoff);
                });
            }
        }
    }
}
