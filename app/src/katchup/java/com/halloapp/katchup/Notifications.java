package com.halloapp.katchup;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.halloapp.BuildConfig;
import com.halloapp.MainActivity;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.RegistrationRequestActivity;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.content.ScreenshotByInfo;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.MomentNotification;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Notifications {
    private static final int UNSEEN_POSTS_LIMIT = 256;

    private static Notifications instance;

    private static final String MOMENTS_NOTIFICATION_CHANNEL_ID = "moments_notifications";
    private static final String DAILY_NOTIFICATION_CHANNEL_ID = "daily_notifications";
    private static final String CRITICAL_NOTIFICATION_CHANNEL_ID = "critical_notifications";
    private static final String NEW_USER_NOTIFICATION_CHANNEL_ID = "new_user_notifications";
    private static final String NEW_FOLLOWER_NOTIFICATION_CHANNEL_ID = "follower_notifications";

    private static final String NEW_USER_NOTIFICATION_GROUP_KEY = "new_user_notification";
    private static final String NEW_FOLLOWER_NOTIFICATION_GROUP_KEY = "new_follower_notification";

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

    private static final String EXTRA_MOMENT_NOTIFICATION_TIME_CUTOFF = "last_moment_notification_time";
    private static final String EXTRA_SCREENSHOT_NOTIFICATION_TIME_CUTOFF = "last_screenshot_notification_time";

    private final static AudioAttributes AUDIO_ATTRIBUTES = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
    private final static Uri DAILY_NOTIFICATION_SOUND_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE).authority(BuildConfig.APPLICATION_ID).appendPath(Integer.toString(R.raw.discovery)).build();
    private final static Uri NEW_USER_NOTIFICATION_SOUND_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE).authority(BuildConfig.APPLICATION_ID).appendPath(Integer.toString(R.raw.bulb)).build();
    private final static Uri NEW_FOLLOWER_NOTIFICATION_SOUND_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE).authority(BuildConfig.APPLICATION_ID).appendPath(Integer.toString(R.raw.bulb)).build();

    private final String NOTIFICATION_DAILY_KATCHUP_BODY = "✨✔️✨";
    private final String NOTIFICATION_DAILY_KATCHUP_LIVE_BODY = "🤍📸🤍";
    private final String NOTIFICATION_DAILY_KATCHUP_LIVE_TEXT = "🔍👌🧐";

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

            final NotificationChannel newUserNotificationChannel = new NotificationChannel(NEW_USER_NOTIFICATION_CHANNEL_ID, context.getString(R.string.new_user_notifications_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            newUserNotificationChannel.enableLights(true);
            newUserNotificationChannel.enableVibration(true);
            newUserNotificationChannel.setSound(NEW_USER_NOTIFICATION_SOUND_URI, AUDIO_ATTRIBUTES);

            final NotificationChannel newFollowerNotificationChannel = new NotificationChannel(NEW_FOLLOWER_NOTIFICATION_CHANNEL_ID, context.getString(R.string.new_follower_notifications_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            newFollowerNotificationChannel.enableLights(true);
            newFollowerNotificationChannel.enableVibration(true);
            newFollowerNotificationChannel.setSound(NEW_FOLLOWER_NOTIFICATION_SOUND_URI, AUDIO_ATTRIBUTES);

            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.createNotificationChannel(dailyNotificationChannel);
            notificationManager.createNotificationChannel(criticalNotificationsChannel);
            notificationManager.createNotificationChannel(momentsNotificationChannel);
            notificationManager.createNotificationChannel(newUserNotificationChannel);
            notificationManager.createNotificationChannel(newFollowerNotificationChannel);
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

            final Intent contentIntent = ViewKatchupCommentsActivity.viewPost(context, unlockingMomentId, false);
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
            final List<Post> newKatchupMoments = getNewMoments();
            Log.i("Notifications/updateFeedNotifications newKatchupMoments=" + newKatchupMoments.size());
            showNotificationForMoments(newKatchupMoments);
        });
    }

    public void updateMomentNotifications(@NonNull Post post) {
        if (localPostIds.contains(post.id)) {
            updateMomentNotifications();
        }
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

    private void showNotificationForMoments(@NonNull List<Post> newKatchupMoments) {
        executor.execute(() -> {
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

            builder.setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, getPendingIntentFlags(true)));
            final Intent deleteIntent = new Intent(context, DeleteMomentNotificationReceiver.class);
            deleteIntent.putExtra(EXTRA_MOMENT_NOTIFICATION_TIME_CUTOFF, momentNotificationTimeCutoff);
            builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT | getPendingIntentFlags(false)));
            notificationManager.notify(MOMENTS_NOTIFICATION_TAG, MOMENTS_NOTIFICATION_ID, builder.build());
        });
    }

    public void showDailyMomentNotification(long timestamp, long notificationId, int type, String prompt) {
        String title = context.getString(R.string.notification_daily_katchup_title);
        Intent contentIntent = SelfiePostComposerActivity.startFromNotification(context, notificationId, timestamp, type, prompt);

        String body = NOTIFICATION_DAILY_KATCHUP_BODY;
        if (type == MomentNotification.Type.LIVE_CAMERA_VALUE) {
            body = NOTIFICATION_DAILY_KATCHUP_LIVE_BODY;
        } else if (type == MomentNotification.Type.TEXT_POST_VALUE) {
            body = NOTIFICATION_DAILY_KATCHUP_LIVE_TEXT;
        } else if (type == MomentNotification.Type.PROMPT_POST_VALUE) {
            body = prompt;
        } else {
            contentIntent = new Intent(context, MainActivity.class);
        }

        Intent parentIntent = new Intent(context, MainActivity.class);
        parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        parentIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_FEED);

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

    public void clearDailyMomentNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(DAILY_MOMENT_NOTIFICATION_TAG, DAILY_MOMENT_NOTIFICATION_ID);
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
            if (preferences.getNotifyNewUsers()) {
                final Intent contentIntent = ViewKatchupProfileActivity.viewProfile(context, userId);
                final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, contentIntent, getPendingIntentFlags(false));
                final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NEW_USER_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setColor(ContextCompat.getColor(context, R.color.color_accent))
                        .setContentTitle(context.getString(R.string.notification_new_user_title))
                        .setContentText(context.getString(R.string.notification_new_user_body, username))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setGroup(NEW_USER_NOTIFICATION_GROUP_KEY)
                        .setGroupSummary(false)
                        .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                                NotificationCompat.DEFAULT_SOUND |
                                NotificationCompat.DEFAULT_VIBRATE)
                        .setSound(NEW_USER_NOTIFICATION_SOUND_URI, AudioManager.STREAM_NOTIFICATION);

                final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(userId.rawId(), NEW_USER_NOTIFICATION_ID, builder.build());
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
            if (preferences.getNotifySomeoneFollowsYou()) {
                final Intent contentIntent = ViewKatchupProfileActivity.viewProfile(context, userId);
                final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, contentIntent, getPendingIntentFlags(false));
                final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NEW_FOLLOWER_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setColor(ContextCompat.getColor(context, R.color.color_accent))
                        .setContentTitle(context.getString(R.string.notification_new_follower_title))
                        .setContentText(context.getString(R.string.notification_new_follower_body, username))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setGroup(NEW_FOLLOWER_NOTIFICATION_GROUP_KEY)
                        .setGroupSummary(false)
                        .setDefaults(NotificationCompat.DEFAULT_LIGHTS |
                                NotificationCompat.DEFAULT_SOUND |
                                NotificationCompat.DEFAULT_VIBRATE)
                        .setSound(NEW_FOLLOWER_NOTIFICATION_SOUND_URI, AudioManager.STREAM_NOTIFICATION);

                final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(userId.rawId(), NEW_FOLLOWER_NOTIFICATION_ID, builder.build());
                Log.i("New User Notification at time : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.US).format(new Date()));
            }
        });
    }

    public void clearNewFollowerNotification(@NonNull UserId userId) {
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(userId.rawId(), NEW_FOLLOWER_NOTIFICATION_ID);
    }

    @Nullable
    private List<ScreenshotByInfo> getScreenshotContacts() {
        if (!preferences.getNotifyMoments()) {
            return null;
        }
        final String unlockingMomentId = contentDb.getUnlockingMomentId();
        if (unlockingMomentId == null) {
            return null;
        }
        return contentDb.getRecentMomentScreenshotInfo(unlockingMomentId, preferences.getScreenshotNotificationTimeCutoff());
    }

    @NonNull
    private List<Post> getNewMoments() {
        final List<Post> newMoments = contentDb.getUnexpiredPostsAfter(preferences.getMomentNotificationTimeCutoff(), UNSEEN_POSTS_LIMIT);

        final ListIterator<Post> iterator = newMoments.listIterator();
        while (iterator.hasNext()) {
            Post post = iterator.next();
            if (post.senderUserId == UserId.ME || post.isRetracted() || post.type != Post.TYPE_KATCHUP) {
                iterator.remove();
            }
        }

        return newMoments;
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
                return context.getString(R.string.many_new_katchup_notification, usernames.get(0), usernames.get(1), usernames.size());
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
            Log.i("Notifications.BroadcastReceiver: cancel");
            final long momentNotificationTimeCutoff = intent.getLongExtra(EXTRA_MOMENT_NOTIFICATION_TIME_CUTOFF, 0);
            if (momentNotificationTimeCutoff > 0) {
                Log.i("Notifications.BroadcastReceiver: cancel, moment notification cutoff at " + momentNotificationTimeCutoff);
                Notifications.getInstance(context).executor.execute(() -> Notifications.getInstance(context).preferences.setMomentNotificationTimeCutoff(momentNotificationTimeCutoff));
            }
        }
    }

    static public class DeleteScreenshotNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Notifications.BroadcastReceiver: cancel");
            final long screenshotNotificationCutoffTime = intent.getLongExtra(EXTRA_SCREENSHOT_NOTIFICATION_TIME_CUTOFF, 0);
            if (screenshotNotificationCutoffTime > 0) {
                Log.i("Notifications.BroadcastReceiver: cancel, moment notification cutoff at " + screenshotNotificationCutoffTime);
                Notifications.getInstance(context).executor.execute(() -> Notifications.getInstance(context).preferences.setScreenshotNotificationTimeCutoff(screenshotNotificationCutoffTime));
            }
        }
    }
}
