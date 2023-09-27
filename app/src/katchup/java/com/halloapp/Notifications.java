package com.halloapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.content.ScreenshotByInfo;
import com.halloapp.id.UserId;
import com.halloapp.katchup.Analytics;
import com.halloapp.katchup.AppExpirationActivity;
import com.halloapp.katchup.SelfiePostComposerActivity;
import com.halloapp.katchup.ViewKatchupCommentsActivity;
import com.halloapp.katchup.ViewKatchupProfileActivity;
import com.halloapp.props.ServerProps;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Notifications {
    private static final int UNSEEN_POSTS_LIMIT = 256;
    private static final int UNSEEN_COMMENTS_LIMIT = 64;

    private static Notifications instance;

    private static final String MOMENTS_NOTIFICATION_CHANNEL_ID = "moments_notifications";
    private static final String DAILY_NOTIFICATION_CHANNEL_ID = "daily_notifications";
    private static final String CRITICAL_NOTIFICATION_CHANNEL_ID = "critical_notifications";
    private static final String NEW_USER_NOTIFICATION_CHANNEL_ID = "new_user_notifications";
    private static final String NEW_FOLLOWER_NOTIFICATION_CHANNEL_ID = "new_follower_notifications";
    private static final String REACTION_NOTIFICATION_CHANNEL_ID = "reaction_notifications";

    private static final String NEW_USER_NOTIFICATION_GROUP_KEY = "new_user_notification";
    private static final String NEW_FOLLOWER_NOTIFICATION_GROUP_KEY = "new_follower_notification";
    private static final String REACTION_NOTIFICATION_GROUP_KEY = "reaction_notification";

    private static final String MOMENTS_NOTIFICATION_TAG = "moments_notification_tag";
    private static final String DAILY_MOMENT_NOTIFICATION_TAG = " daily_moment_notification_tag";

    private static final int EXPIRATION_NOTIFICATION_ID = 1;
    private static final int LOGIN_FAILED_NOTIFICATION_ID = 2;
    private static final int UNFINISHED_REGISTRATION_NOTIFICATION_ID = 3;
    private static final int DAILY_MOMENT_NOTIFICATION_ID = 4;
    private static final int MOMENTS_NOTIFICATION_ID = 5;
    private static final int MOMENT_SCREENSHOT_NOTIFICATION_ID = 6;
    private static final int NEW_USER_NOTIFICATION_ID = 7;
    private static final int NEW_FOLLOWER_NOTIFICATION_ID = 8;
    private static final int REACTION_NOTIFICATION_ID = 9;

    private static final String EXTRA_MOMENT_NOTIFICATION_TIME_CUTOFF = "last_moment_notification_time";
    private static final String EXTRA_SCREENSHOT_NOTIFICATION_TIME_CUTOFF = "last_screenshot_notification_time";
    private static final String EXTRA_REACTION_NOTIFICATION_COMMENT_ID = "reaction_notification_comment_id";

    public static final String EXTRA_IS_NOTIFICATION = "is_notification";
    public static final String EXTRA_NOTIFICATION_TYPE = "notification_type";
    public static final String EXTRA_NOTIFICATION_BODY = "notification_body";
    public static final String EXTRA_IS_DAILY_MOMENT_REMINDER_NOTIFICATION = "is_daily_reminder_notification";

    private static final AudioAttributes AUDIO_ATTRIBUTES = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
    private static final Uri DAILY_NOTIFICATION_SOUND_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE).authority(BuildConfig.APPLICATION_ID).appendPath(Integer.toString(R.raw.discovery)).build();
    private static final Uri NEW_USER_NOTIFICATION_SOUND_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE).authority(BuildConfig.APPLICATION_ID).appendPath(Integer.toString(R.raw.bulb)).build();
    private static final Uri NEW_FOLLOWER_NOTIFICATION_SOUND_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE).authority(BuildConfig.APPLICATION_ID).appendPath(Integer.toString(R.raw.bulb)).build();
    private static final Uri REACTION_NOTIFICATION_SOUND_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE).authority(BuildConfig.APPLICATION_ID).appendPath(Integer.toString(R.raw.flick)).build();

    private final Context context;
    private final Preferences preferences;
    private final ContactsDb contactsDb;
    private final ContentDb contentDb;

    private final Executor executor = Executors.newSingleThreadExecutor();

    private long momentNotificationTimeCutoff;
    private long screenshotNotificationTimeCutoff;

    private final Set<String> localPostIds = new HashSet<>();

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

    public static void openNotificationSettings(final @NonNull Context context) {
        final Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= 26) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        } else {
            // Useful discussion on how to open app specific notification settings prior to API 26:
            // https://stackoverflow.com/questions/32366649/any-way-to-link-to-the-android-notification-settings-for-my-app
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
        }
        context.startActivity(intent);
    }

    private Notifications(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = Preferences.getInstance();
        this.contactsDb = ContactsDb.getInstance();
        this.contentDb = ContentDb.getInstance();
    }

    public void init() {
        Log.d("Notifications init");
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= 26) {
            final NotificationChannel dailyNotificationChannel = new NotificationChannel(DAILY_NOTIFICATION_CHANNEL_ID, context.getString(R.string.daily_notifications_channel_name), NotificationManager.IMPORTANCE_HIGH);
            dailyNotificationChannel.enableLights(true);
            dailyNotificationChannel.enableVibration(true);
            dailyNotificationChannel.setSound(DAILY_NOTIFICATION_SOUND_URI, AUDIO_ATTRIBUTES);

            final NotificationChannel criticalNotificationsChannel = new NotificationChannel(CRITICAL_NOTIFICATION_CHANNEL_ID, context.getString(R.string.critical_notifications_channel_name), NotificationManager.IMPORTANCE_HIGH);
            criticalNotificationsChannel.enableLights(true);
            criticalNotificationsChannel.enableVibration(true);

            final NotificationChannel momentsNotificationChannel = new NotificationChannel(MOMENTS_NOTIFICATION_CHANNEL_ID, context.getString(R.string.moments_notifications_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            momentsNotificationChannel.enableLights(true);
            momentsNotificationChannel.enableVibration(true);

            final NotificationChannel newUserNotificationChannel = new NotificationChannel(NEW_USER_NOTIFICATION_CHANNEL_ID, context.getString(R.string.new_user_notifications_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            newUserNotificationChannel.enableLights(true);
            newUserNotificationChannel.enableVibration(true);
            newUserNotificationChannel.setSound(NEW_USER_NOTIFICATION_SOUND_URI, AUDIO_ATTRIBUTES);

            final NotificationChannel newFollowerNotificationChannel = new NotificationChannel(NEW_FOLLOWER_NOTIFICATION_CHANNEL_ID, context.getString(R.string.new_follower_notifications_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            newFollowerNotificationChannel.enableLights(true);
            newFollowerNotificationChannel.enableVibration(true);
            newFollowerNotificationChannel.setSound(NEW_FOLLOWER_NOTIFICATION_SOUND_URI, AUDIO_ATTRIBUTES);

            final NotificationChannel reactionNotificationChannel = new NotificationChannel(REACTION_NOTIFICATION_CHANNEL_ID, context.getString(R.string.reactions_notifications_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            reactionNotificationChannel.enableLights(true);
            reactionNotificationChannel.enableVibration(true);
            reactionNotificationChannel.setSound(REACTION_NOTIFICATION_SOUND_URI, AUDIO_ATTRIBUTES);

            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.createNotificationChannel(dailyNotificationChannel);
            notificationManager.createNotificationChannel(criticalNotificationsChannel);
            notificationManager.createNotificationChannel(momentsNotificationChannel);
            notificationManager.createNotificationChannel(newUserNotificationChannel);
            notificationManager.createNotificationChannel(newFollowerNotificationChannel);
            notificationManager.createNotificationChannel(reactionNotificationChannel);
        }
    }

    public void updateScreenshotNotifications() {
        executor.execute(() -> {
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            final List<ScreenshotByInfo> screenshotContacts = getScreenshotContacts();
            if (screenshotContacts == null || screenshotContacts.isEmpty()) {
                notificationManager.cancel(MOMENT_SCREENSHOT_NOTIFICATION_ID);
                return;
            }

            final Set<UserId> users = new HashSet<>();
            final ArrayList<String> usernames = new ArrayList<>();
            for (ScreenshotByInfo info : screenshotContacts) {
                if (info.timestamp > screenshotNotificationTimeCutoff) {
                    screenshotNotificationTimeCutoff = info.timestamp;
                }

                final UserId userId = info.userId;
                if (users.contains(userId)) {
                    continue;
                }

                final String username = contactsDb.readUsername(userId);
                if (username != null) {
                    usernames.add(username);
                    users.add(userId);
                }
            }

            if (usernames.isEmpty()) {
                notificationManager.cancel(MOMENT_SCREENSHOT_NOTIFICATION_ID);
                return;
            }

            final String title = context.getString(R.string.notification_screenshot_title);
            final String body = formatNotificationBodyFromUsernames(usernames);

            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MOMENTS_NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(ContextCompat.getColor(context, R.color.color_accent))
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                            NotificationCompat.DEFAULT_SOUND |
                            NotificationCompat.DEFAULT_VIBRATE)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI, AudioManager.STREAM_NOTIFICATION);

            final String unlockingMomentId = Preconditions.checkNotNull(contentDb.getUnlockingMomentId());

            final Intent contentIntent = ViewKatchupCommentsActivity.viewPost(context, unlockingMomentId);
            contentIntent.putExtra(EXTRA_IS_NOTIFICATION, true);
            contentIntent.putExtra(EXTRA_NOTIFICATION_TYPE, Analytics.SCREENSHOT_NOTIFICATION);
            Analytics.getInstance().notificationReceived(Analytics.SCREENSHOT_NOTIFICATION, true);
            final Intent parentIntent = new Intent(context, MainActivity.class);
            parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            parentIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_FEED);
            final TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(parentIntent);
            stackBuilder.addNextIntent(contentIntent);
            builder.setContentIntent(stackBuilder.getPendingIntent(0, getPendingIntentFlags(true)));

            final Intent deleteIntent = new Intent(context, Notifications.DeleteScreenshotNotificationReceiver.class);
            deleteIntent.putExtra(EXTRA_SCREENSHOT_NOTIFICATION_TIME_CUTOFF, screenshotNotificationTimeCutoff);
            builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT | getPendingIntentFlags(false)));

            notificationManager.notify(unlockingMomentId, MOMENT_SCREENSHOT_NOTIFICATION_ID, builder.build());
        });
    }

    public void clearScreenshotNotifications(@NonNull String momentId) {
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(momentId, MOMENT_SCREENSHOT_NOTIFICATION_ID);
    }

    public void updateMomentNotifications() {
        executor.execute(() -> {
            final List<Post> newKatchupMoments = getNewMoments(preferences.getMomentNotificationTimeCutoff());
            Log.i("Notifications/updateFeedNotifications newKatchupMoments=" + newKatchupMoments.size());

            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (newKatchupMoments.isEmpty()) {
                notificationManager.cancel(MOMENTS_NOTIFICATION_TAG, MOMENTS_NOTIFICATION_ID);
                Log.i("Notifications/showNotificationForMoments hiding moments notification group");
                return;
            }

            final HashSet<UserId> users = new HashSet<>();
            final ArrayList<String> usernames = new ArrayList<>();
            localPostIds.clear();
            for (Post post : newKatchupMoments) {
                localPostIds.add(post.id);
                if (post.timestamp > momentNotificationTimeCutoff) {
                    momentNotificationTimeCutoff = post.timestamp;
                }

                final UserId userId = post.senderUserId;
                if (users.contains(userId)) {
                    continue;
                }

                final String username = contactsDb.readUsername(userId);
                if (username != null) {
                    usernames.add(username);
                    users.add(userId);
                }
            }

            if (usernames.isEmpty()) {
                notificationManager.cancel(MOMENTS_NOTIFICATION_TAG, MOMENTS_NOTIFICATION_ID);
                return;
            }

            final String title = context.getString(R.string.notification_new_katchup_title);
            final String body = formatNotificationBodyFromUsernames(usernames);

            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MOMENTS_NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(ContextCompat.getColor(context, R.color.color_accent))
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                            NotificationCompat.DEFAULT_SOUND |
                            NotificationCompat.DEFAULT_VIBRATE)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI, AudioManager.STREAM_NOTIFICATION);
            final Intent contentIntent = new Intent(context, MainActivity.class);
            contentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            contentIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_FEED);
            contentIntent.putExtra(MainActivity.EXTRA_SCROLL_TO_TOP, true);
            contentIntent.putExtra(MainActivity.EXTRA_STACK_TOP_MOMENT_ID, newKatchupMoments.get(0).id);
            contentIntent.putExtra(EXTRA_IS_NOTIFICATION, true);
            contentIntent.putExtra(EXTRA_NOTIFICATION_TYPE, Analytics.FEEDPOST_NOTIFICATION);
            Analytics.getInstance().notificationReceived(Analytics.FEEDPOST_NOTIFICATION, true);

            builder.setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, getPendingIntentFlags(true)));
            final Intent deleteIntent = new Intent(context, DeleteMomentNotificationReceiver.class);
            deleteIntent.putExtra(EXTRA_MOMENT_NOTIFICATION_TIME_CUTOFF, momentNotificationTimeCutoff);
            builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT | getPendingIntentFlags(false)));
            notificationManager.notify(MOMENTS_NOTIFICATION_TAG, MOMENTS_NOTIFICATION_ID, builder.build());
        });
    }

    public void clearMomentNotifications() {
        executor.execute(() -> {
            if (momentNotificationTimeCutoff != 0) {
                preferences.setMomentNotificationTimeCutoff(momentNotificationTimeCutoff);
            }
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(MOMENTS_NOTIFICATION_TAG, MOMENTS_NOTIFICATION_ID);
        });
        localPostIds.clear();
    }

    public void updateMomentNotifications(@NonNull Post post) {
        if (localPostIds.contains(post.id)) {
            updateMomentNotifications();
        }
    }

    public void updateReactionNotifications() {
        executor.execute(() -> {
            if (!preferences.getNotifyReactions()) {
                return;
            }

            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            final String title = context.getString(R.string.notification_reaction_title);

            int notificationsDisplayedCount = 0;
            final List<Comment> newComments = getNewComments();
            for (Comment comment : newComments) {
                final String commentSenderUsername = contactsDb.readUsername(comment.senderUserId);
                final Post parentPost = comment.getParentPost();

                if (commentSenderUsername == null || parentPost == null || parentPost.senderUserId == null) {
                    Log.i("Notifications/updateReactionNotifications hiding moments notification for comment.id=" + comment.id);
                    notificationManager.cancel(comment.id, REACTION_NOTIFICATION_ID);
                    continue;
                }

                final String body;
                if (parentPost.senderUserId.isMe()) {
                    body = context.getString(R.string.notification_reaction_body, commentSenderUsername);
                } else {
                    if (parentPost.senderUserId.equals(comment.senderUserId)) {
                        body = context.getString(R.string.notification_reaction_other_post_author_body, commentSenderUsername);
                    } else {
                        final String postSenderUsername = contactsDb.readUsername(parentPost.senderUserId);
                        if (postSenderUsername == null) {
                            Log.i("Notifications/updateReactionNotifications hiding moments notification for comment.id=" + comment.id);
                            notificationManager.cancel(comment.id, REACTION_NOTIFICATION_ID);
                            continue;
                        } else {
                            body = context.getString(R.string.notification_reaction_other_post_commenter_body, commentSenderUsername, postSenderUsername);
                        }
                    }
                }

                final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, REACTION_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setColor(ContextCompat.getColor(context, R.color.color_accent))
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setGroup(REACTION_NOTIFICATION_GROUP_KEY)
                        .setGroupSummary(false)
                        .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                                NotificationCompat.DEFAULT_SOUND |
                                NotificationCompat.DEFAULT_VIBRATE)
                        .setSound(REACTION_NOTIFICATION_SOUND_URI, AudioManager.STREAM_NOTIFICATION);

                final int requestCode = Objects.hashCode(comment.rowId);
                final Intent contentIntent = ViewKatchupCommentsActivity.viewPost(context, parentPost);
                final Intent parentIntent = new Intent(context, MainActivity.class);
                parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                parentIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_FEED);

                final TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntent(parentIntent);
                stackBuilder.addNextIntent(contentIntent);
                builder.setContentIntent(stackBuilder.getPendingIntent(requestCode, getPendingIntentFlags(false)));

                final Intent deleteIntent = new Intent(context, Notifications.DeleteReactionNotificationReceiver.class);
                deleteIntent.putExtra(EXTRA_REACTION_NOTIFICATION_COMMENT_ID, comment.id);
                builder.setDeleteIntent(PendingIntent.getBroadcast(context, requestCode, deleteIntent, getPendingIntentFlags(false)));

                notificationsDisplayedCount++;
                notificationManager.notify(comment.id, REACTION_NOTIFICATION_ID, builder.build());
            }

            if (notificationsDisplayedCount > 0) {
                final NotificationCompat.Builder groupSummaryBuilder = new NotificationCompat.Builder(context, REACTION_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setColor(ContextCompat.getColor(context, R.color.color_accent))
                        .setContentTitle(title)
                        .setGroup(REACTION_NOTIFICATION_GROUP_KEY)
                        .setGroupSummary(true);

                notificationManager.notify(REACTION_NOTIFICATION_ID, groupSummaryBuilder.build());
            }
        });
    }

    public void updateReactionNotifications(@NonNull Comment comment) {
        if (comment.isRetracted()) {
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(comment.id, REACTION_NOTIFICATION_ID);
            Log.i("Notifications/updateReactionNotifications hiding comments notification group");
        } else {
            updateReactionNotifications();
        }
    }

    public void clearReactionNotifications(@NonNull List<String> commentIds) {
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        for (String commentId : commentIds) {
            notificationManager.cancel(commentId, REACTION_NOTIFICATION_ID);
        }
    }

    public void showDailyMomentNotification(long timestamp, long notificationId, int type, String prompt, String date, boolean reminder) {
        final String title = context.getString(R.string.notification_daily_katchup_title);
        final String body = reminder ? getReminderDailyMomentNotificationText() : getDailyMomentNotificationText(prompt);
        final Intent contentIntent = SelfiePostComposerActivity.startFromNotification(context, notificationId, timestamp, type, prompt, date);
        contentIntent.putExtra(EXTRA_IS_NOTIFICATION, true);
        contentIntent.putExtra(EXTRA_NOTIFICATION_TYPE, Analytics.DAILY_MOMENT_NOTIFICATION);
        contentIntent.putExtra(EXTRA_IS_DAILY_MOMENT_REMINDER_NOTIFICATION, reminder);

        Analytics.getInstance().notificationReceived(Analytics.DAILY_MOMENT_NOTIFICATION, true, notificationId, prompt, body, reminder);

        Intent parentIntent = new Intent(context, MainActivity.class);
        parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        parentIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_FEED);
        contentIntent.putExtra(EXTRA_NOTIFICATION_BODY, body);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(parentIntent);
        stackBuilder.addNextIntent(contentIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DAILY_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.color_accent))
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                        NotificationCompat.DEFAULT_SOUND |
                        NotificationCompat.DEFAULT_VIBRATE)
                .setSound(DAILY_NOTIFICATION_SOUND_URI, AudioManager.STREAM_NOTIFICATION);

        builder.setContentIntent(stackBuilder.getPendingIntent(0, getPendingIntentFlags(true)));

        // TODO: handle clearing of intent
        //Intent deleteIntent = new Intent(context, DeleteMomentNotificationReceiver.class);
        //builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent. FLAG_CANCEL_CURRENT | getPendingIntentFlags(false)));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(DAILY_MOMENT_NOTIFICATION_TAG, DAILY_MOMENT_NOTIFICATION_ID, builder.build());
    }

    private String getReminderDailyMomentNotificationText() {
        final List<Post> newKatchupMoments = getNewMoments(preferences.getMomentNotificationTimestamp());
        final HashSet<UserId> users = new HashSet<>();
        final ArrayList<String> usernames = new ArrayList<>();
        for (Post post : newKatchupMoments) {
            final UserId userId = post.senderUserId;
            if (users.contains(userId)) {
                continue;
            }

            final String username = contactsDb.readUsername(userId);
            if (username != null) {
                usernames.add(username);
                users.add(userId);
            }
        }

        if (usernames.size() < 1) {
            return context.getString(R.string.notification_daily_katchup_reminder_body);
        } else {
            switch (usernames.size()) {
                case 1:
                    return context.getString(R.string.notification_one_daily_katchup_reminder_body, usernames.get(0));
                case 2:
                    return context.getString(R.string.notification_two_daily_katchup_reminder_body, usernames.get(0), usernames.get(1));
                case 3:
                    return context.getString(R.string.notification_three_daily_katchup_reminder_body, usernames.get(0), usernames.get(1), usernames.get(2));
                default:
                    return context.getResources().getQuantityString(R.plurals.notification_many_daily_katchup_reminder_body_plural, usernames.size() - 2, usernames.get(0), usernames.get(1), usernames.size() - 2);
            }
        }
    }

    private String getDailyMomentNotificationText(String prompt) {
        String body = ServerProps.getInstance().getPropDailyKatchupNotificationTemplate();
        if (!TextUtils.isEmpty(body) && !TextUtils.isEmpty(prompt)) {
            try {
                JSONObject jsonObject = new JSONObject(body);

                String language = Locale.getDefault().getLanguage();
                // See https://developer.android.com/reference/java/util/Locale#getLanguage() for why one cannot directly look up the string
                for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                    String key = it.next();
                    if (language.equals(new Locale(key).getLanguage())) {
                        String selected = jsonObject.getString(key);
                        try {
                            return String.format(selected.replace('@', 's'), prompt);
                        } catch (IllegalFormatException e) {
                            Log.e("Failed to format daily notification prompt string", e);
                            Log.sendErrorReport("Bad daily notification prompt string");
                        }
                    }
                }
            } catch (JSONException e) {
                Log.e("Failed to parse daily notification prompt json", e);
                Log.sendErrorReport("Bad daily notification prompt json");
            }
        }
        return context.getString(R.string.notification_daily_katchup_body);
    }

    public void clearDailyMomentNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(DAILY_MOMENT_NOTIFICATION_TAG, DAILY_MOMENT_NOTIFICATION_ID);
    }

    public void showExpirationNotification(int daysLeft) {
        final String title;
        if (daysLeft > 0) {
            title = context.getResources().getQuantityString(R.plurals.katchup_notification_app_expiration_days_left_title, daysLeft, daysLeft);
        } else {
            title = context.getString(R.string.katchup_notification_app_expired_title);
        }
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CRITICAL_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.color_accent))
                .setContentTitle(title)
                .setContentText(context.getString(R.string.notification_app_expiration_body))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        final Intent contentIntent = AppExpirationActivity.open(context, daysLeft);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, getPendingIntentFlags(true)));
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(EXPIRATION_NOTIFICATION_ID, builder.build());
    }

    public void showLoginFailedNotification() {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CRITICAL_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.color_accent))
                .setContentTitle(context.getString(R.string.login_failed))
                .setContentText(context.getString(R.string.login_failed_explanation))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        final Intent contentIntent = new Intent(context, RegistrationRequestActivity.class);
        contentIntent.putExtra(RegistrationRequestActivity.EXTRA_RE_VERIFY, true);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, getPendingIntentFlags(true)));
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(LOGIN_FAILED_NOTIFICATION_ID, builder.build());
    }

    public void clearLoginFailedNotification() {
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(LOGIN_FAILED_NOTIFICATION_ID);
    }

    public void showFinishRegistrationNotification() {
        Intent contentIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, contentIntent, getPendingIntentFlags(false));
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CRITICAL_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.color_accent))
                .setContentTitle(context.getString(R.string.notification_finish_registration_title))
                .setContentText(context.getString(R.string.notification_katchup_finish_registration_body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                        NotificationCompat.DEFAULT_SOUND |
                        NotificationCompat.DEFAULT_VIBRATE)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI, AudioManager.STREAM_NOTIFICATION);
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(UNFINISHED_REGISTRATION_NOTIFICATION_ID, builder.build());
        Log.i("Unfinished Notification at time : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.US).format(new Date()));
    }

    public void clearFinishRegistrationNotification() {
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(UNFINISHED_REGISTRATION_NOTIFICATION_ID);
    }

    public void showNewUserNotification(@NonNull UserId userId, @NonNull String username) {
        executor.execute(() -> {
            Analytics.getInstance().notificationReceived(Analytics.CONTACT_NOTICE_NOTIFICATION, preferences.getNotifyNewUsers());
            if (preferences.getNotifyNewUsers()) {
                final String title = context.getString(R.string.notification_new_user_title);
                final String body = context.getString(R.string.notification_new_user_body, username);

                final Intent contentIntent = ViewKatchupProfileActivity.viewProfile(context, userId);
                contentIntent.putExtra(EXTRA_IS_NOTIFICATION, true);
                contentIntent.putExtra(EXTRA_NOTIFICATION_TYPE, Analytics.CONTACT_NOTICE_NOTIFICATION);
                final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, contentIntent, getPendingIntentFlags(false));
                final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NEW_USER_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setColor(ContextCompat.getColor(context, R.color.color_accent))
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setGroup(NEW_USER_NOTIFICATION_GROUP_KEY)
                        .setGroupSummary(false)
                        .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                                NotificationCompat.DEFAULT_SOUND |
                                NotificationCompat.DEFAULT_VIBRATE)
                        .setSound(NEW_USER_NOTIFICATION_SOUND_URI, AudioManager.STREAM_NOTIFICATION);

                final NotificationCompat.Builder groupSummaryBuilder = new NotificationCompat.Builder(context, NEW_USER_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setColor(ContextCompat.getColor(context, R.color.color_accent))
                        .setContentTitle(title)
                        .setGroup(NEW_USER_NOTIFICATION_GROUP_KEY)
                        .setGroupSummary(true);

                final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(userId.rawId(), NEW_USER_NOTIFICATION_ID, builder.build());
                notificationManager.notify(NEW_USER_NOTIFICATION_ID, groupSummaryBuilder.build());
                Log.i("New User Notification at time : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.US).format(new Date()));
            }
        });
    }

    public void clearNewUserNotification(@NonNull UserId userId) {
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(userId.rawId(), UNFINISHED_REGISTRATION_NOTIFICATION_ID);
    }

    public void showNewFollowerNotification(@NonNull UserId userId, @NonNull String username) {
        executor.execute(() -> {
            Analytics.getInstance().notificationReceived(Analytics.FOLLOWER_NOTICE_NOTIFICATION, preferences.getNotifySomeoneFollowsYou());
            if (preferences.getNotifySomeoneFollowsYou()) {
                final String title = context.getString(R.string.notification_new_follower_title);
                final String body = context.getString(R.string.notification_new_follower_body, username);

                final Intent contentIntent = ViewKatchupProfileActivity.viewProfile(context, userId);
                contentIntent.putExtra(EXTRA_IS_NOTIFICATION, true);
                contentIntent.putExtra(EXTRA_NOTIFICATION_TYPE, Analytics.FOLLOWER_NOTICE_NOTIFICATION);
                final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, contentIntent, getPendingIntentFlags(false));
                final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NEW_FOLLOWER_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setColor(ContextCompat.getColor(context, R.color.color_accent))
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setGroup(NEW_FOLLOWER_NOTIFICATION_GROUP_KEY)
                        .setGroupSummary(false)
                        .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                                NotificationCompat.DEFAULT_SOUND |
                                NotificationCompat.DEFAULT_VIBRATE)
                        .setSound(NEW_FOLLOWER_NOTIFICATION_SOUND_URI, AudioManager.STREAM_NOTIFICATION);

                final NotificationCompat.Builder groupSummaryBuilder = new NotificationCompat.Builder(context, NEW_FOLLOWER_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setColor(ContextCompat.getColor(context, R.color.color_accent))
                        .setContentTitle(title)
                        .setGroup(NEW_FOLLOWER_NOTIFICATION_GROUP_KEY)
                        .setGroupSummary(true);

                final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(userId.rawId(), NEW_FOLLOWER_NOTIFICATION_ID, builder.build());
                notificationManager.notify(NEW_FOLLOWER_NOTIFICATION_ID, groupSummaryBuilder.build());
                Log.i("New Follower Notification at time : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.US).format(new Date()));
            }
        });
    }

    public void clearNewFollowerNotification(@NonNull UserId userId) {
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(userId.rawId(), NEW_FOLLOWER_NOTIFICATION_ID);
    }

    public void showFriendModelNotification() {
        // HalloApp exclusive - do nothing
    }

    @WorkerThread
    @Nullable
    private List<ScreenshotByInfo> getScreenshotContacts() {
        final String unlockingMomentId = contentDb.getUnlockingMomentId();
        if (unlockingMomentId == null) {
            return null;
        }
        return contentDb.getRecentMomentScreenshotInfo(unlockingMomentId, preferences.getScreenshotNotificationTimeCutoff());
    }

    @WorkerThread
    @NonNull
    private List<Post> getNewMoments(long timestamp) {
        final List<Post> newMoments = contentDb.getUnexpiredUnseenPostsAfter(timestamp, UNSEEN_POSTS_LIMIT);

        final ListIterator<Post> iterator = newMoments.listIterator();
        while (iterator.hasNext()) {
            Post post = iterator.next();
            if (post.senderUserId == UserId.ME || post.isRetracted() || post.type != Post.TYPE_KATCHUP) {
                iterator.remove();
            }
        }

        return newMoments;
    }

    @WorkerThread
    @NonNull
    private List<Comment> getNewComments() {
        final List<Comment> unseenComments = contentDb.getNotificationComments(0, UNSEEN_COMMENTS_LIMIT, true);

        final ListIterator<Comment> iterator = unseenComments.listIterator();
        while (iterator.hasNext()) {
            final Comment comment = iterator.next();
            final Post parentPost = comment.getParentPost();
            if (comment.isRetracted() || parentPost == null || parentPost.isRetracted()) {
                iterator.remove();
            }
        }

        return unseenComments;
    }

    private String formatNotificationBodyFromUsernames(@NonNull ArrayList<String> usernames) {
        switch (usernames.size()) {
            case 0:
                throw new IllegalArgumentException("Unexpected names is empty");
            case 1:
                return context.getString(R.string.new_katchup_notification, usernames.get(0));
            case 2:
                return context.getString(R.string.two_new_katchup_notification, usernames.get(0), usernames.get(1));
            case 3:
                return context.getString(R.string.three_new_katchup_notification, usernames.get(0), usernames.get(1), usernames.get(2));
            default:
                return context.getString(R.string.many_new_katchup_notification, usernames.get(0), usernames.get(1), usernames.size() - 2);
        }
    }

    public static int getMutableIntentFlag(boolean mutable) {
        if (Build.VERSION.SDK_INT >= 23) {
            return (mutable
                    ? (Build.VERSION.SDK_INT >= 31
                    ? PendingIntent.FLAG_MUTABLE
                    : 0)
                    : PendingIntent.FLAG_IMMUTABLE);
        } else {
            return 0;
        }
    }

    public static int getPendingIntentFlags(boolean mutable) {
        if (Build.VERSION.SDK_INT >= 23) {
            return PendingIntent.FLAG_UPDATE_CURRENT | getMutableIntentFlag(mutable);
        } else {
            return PendingIntent.FLAG_UPDATE_CURRENT;
        }
    }

    static public class DeleteMomentNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Notifications.DeleteMomentNotificationReceiver: cancel");
            final long momentNotificationTimeCutoff = intent.getLongExtra(EXTRA_MOMENT_NOTIFICATION_TIME_CUTOFF, 0);
            if (momentNotificationTimeCutoff > 0) {
                Log.i("Notifications.DeleteMomentNotificationReceiver: cancel, moment notification cutoff at " + momentNotificationTimeCutoff);
                Notifications.getInstance(context).executor.execute(() -> Notifications.getInstance(context).preferences.setMomentNotificationTimeCutoff(momentNotificationTimeCutoff));
            }
        }
    }

    static public class DeleteScreenshotNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Notifications.DeleteScreenshotNotificationReceiver: cancel");
            final long screenshotNotificationCutoffTime = intent.getLongExtra(EXTRA_SCREENSHOT_NOTIFICATION_TIME_CUTOFF, 0);
            if (screenshotNotificationCutoffTime > 0) {
                Log.i("Notifications.DeleteScreenshotNotificationReceiver: cancel, screenshot notification cutoff at " + screenshotNotificationCutoffTime);
                Notifications.getInstance(context).executor.execute(() -> Notifications.getInstance(context).preferences.setScreenshotNotificationTimeCutoff(screenshotNotificationCutoffTime));
            }
        }
    }

    static public class DeleteReactionNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Notifications.DeleteReactionNotificationReceiver: cancel");
            final String commentId = intent.getStringExtra(EXTRA_REACTION_NOTIFICATION_COMMENT_ID);
            if (commentId != null) {
                Log.i("Notifications.DeleteReactionNotificationReceiver: cancel, comment id = " + commentId);
                Notifications.getInstance(context).contentDb.setCommentShouldNotify(commentId, false);
            }
        }
    }
}
