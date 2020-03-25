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
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.ui.AppExpirationActivity;
import com.halloapp.ui.MainActivity;
import com.halloapp.ui.RegistrationRequestActivity;
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

    public static final String ACTION_NOTIFY_FEED = "halloapp.intent.action.NOTIFY_FEED";

    private static final String FEED_NOTIFICATION_CHANNEL_ID = "feed_notifications";
    private static final String CRITICAL_NOTIFICATION_CHANNEL_ID = "critical_notifications";

    private static final int FEED_NOTIFICATION_ID = 0;
    private static final int EXPIRATION_NOTIFICATION_ID = 1;
    private static final int LOGIN_FAILED_NOTIFICATION_ID = 2;

    private static final int UNSEEN_POSTS_LIMIT = 256;
    private static final int UNSEEN_COMMENTS_LIMIT = 64;

    private static final String EXTRA_FEED_NOTIFICATION_TIME_CUTOFF = "last_feed_notification_time";

    private final Context context;
    private final Preferences preferences;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private long feedNotificationTimeCutoff;

    private boolean enabled = true;

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
            final NotificationChannel feedNotificationsChannel = new NotificationChannel(FEED_NOTIFICATION_CHANNEL_ID, context.getString(R.string.feed_notifications_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            final NotificationChannel criticalNotificationsChannel = new NotificationChannel(CRITICAL_NOTIFICATION_CHANNEL_ID, context.getString(R.string.critical_notifications_channel_name), NotificationManager.IMPORTANCE_HIGH);

            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.createNotificationChannel(feedNotificationsChannel);
            notificationManager.createNotificationChannel(criticalNotificationsChannel);

            notificationManager.deleteNotificationChannel("new_post_notification"); // TODO (ds): remove
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
        if (!enabled) {
            return;
        }
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
                showFeedNotification(context.getString(R.string.app_name), text);
            }
        });
    }

    private String getNewPostsNotificationText() {
        if (!preferences.getNotifyPosts()) {
            return null;
        }
        final List<Post> unseenPosts = ContentDb.getInstance(context).getUnseenPosts(preferences.getFeedNotificationTimeCutoff(), UNSEEN_POSTS_LIMIT);
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
        final List<Comment> unseenComments = ContentDb.getInstance(context).getUnseenCommentsOnMyPosts(preferences.getFeedNotificationTimeCutoff(), UNSEEN_COMMENTS_LIMIT);
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

    private void showFeedNotification(@NonNull String title, @NonNull String body) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FEED_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        final Intent contentIntent = new Intent(context, MainActivity.class);
        contentIntent.setAction(ACTION_NOTIFY_FEED);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        final Intent deleteIntent = new Intent(context, DeleteNotificationReceiver.class);
        deleteIntent.putExtra(EXTRA_FEED_NOTIFICATION_TIME_CUTOFF, feedNotificationTimeCutoff) ;
        builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0 , deleteIntent, PendingIntent. FLAG_CANCEL_CURRENT));
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(FEED_NOTIFICATION_ID, builder.build());
    }

    public void showExpirationNotification(int daysLeft) {
        final String title;
        if (daysLeft > 0) {
            title = context.getResources().getQuantityString(R.plurals.notification_app_expiration_days_left_title, daysLeft, daysLeft);
        } else {
            title = context.getString(R.string.notification_app_expired_title);
        }
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CRITICAL_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(context.getString(R.string.notification_app_expiration_body))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        final Intent contentIntent = new Intent(context, AppExpirationActivity.class);
        contentIntent.putExtra(AppExpirationActivity.EXTRA_DAYS_LEFT, daysLeft);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(EXPIRATION_NOTIFICATION_ID, builder.build());
    }

    public void showLoginFailedNotification() {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CRITICAL_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.login_failed))
                .setContentText(context.getString(R.string.login_failed_explanation))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        final Intent contentIntent = new Intent(context, RegistrationRequestActivity.class);
        contentIntent.putExtra(RegistrationRequestActivity.EXTRA_RE_VERIFY, true);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(LOGIN_FAILED_NOTIFICATION_ID, builder.build());
    }

    public void clearLoginFailedNotification() {
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(LOGIN_FAILED_NOTIFICATION_ID);
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
