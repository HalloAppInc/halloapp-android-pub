package com.halloapp.katchup;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.halloapp.MainActivity;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.RegistrationRequestActivity;
import com.halloapp.proto.server.MomentNotification;
import com.halloapp.util.logs.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Notifications {

    private static Notifications instance;

    private static final String DAILY_NOTIFICATION_CHANNEL_ID = "daily_notifications";
    private static final String CRITICAL_NOTIFICATION_CHANNEL_ID = "critical_notifications";

    private static final String DAILY_MOMENT_NOTIFICATION_TAG = " daily_moment_notification_tag";

    private static final int EXPIRATION_NOTIFICATION_ID = 1;
    private static final int LOGIN_FAILED_NOTIFICATION_ID = 2;
    private static final int UNFINISHED_REGISTRATION_NOTIFICATION_ID = 3;
    private static final int DAILY_MOMENT_NOTIFICATION_ID = 4;

    private static final String EXTRA_MOMENT_NOTIFICATION_TIME_CUTOFF = "last_moment_notification_time";

    private final Context context;
    private final Preferences preferences;

    private final Executor executor = Executors.newSingleThreadExecutor();

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
    }

    public void init() {
        Log.d("Notifications init");
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= 26) {
            final NotificationChannel momentsNotificationChannel = new NotificationChannel(DAILY_NOTIFICATION_CHANNEL_ID, context.getString(R.string.moments_notifications_channel_name), NotificationManager.IMPORTANCE_HIGH);
            momentsNotificationChannel.enableLights(true);
            momentsNotificationChannel.enableVibration(true);
            momentsNotificationChannel.setShowBadge(false);
            final NotificationChannel criticalNotificationsChannel = new NotificationChannel(CRITICAL_NOTIFICATION_CHANNEL_ID, context.getString(R.string.critical_notifications_channel_name), NotificationManager.IMPORTANCE_HIGH);
            criticalNotificationsChannel.enableLights(true);
            criticalNotificationsChannel.enableVibration(true);

            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.createNotificationChannel(momentsNotificationChannel);
            notificationManager.createNotificationChannel(criticalNotificationsChannel);
        }
    }

    public void showKatchupDailyMomentNotification(long timestamp, long notificationId, int type, String prompt) {
        String title = context.getString(R.string.notification_daily_moment_title);
        Intent contentIntent = SelfiePostComposerActivity.startFromNotification(context, notificationId, timestamp, type, prompt);

        String body = context.getString(R.string.notification_daily_moment_body);
        if (type == MomentNotification.Type.LIVE_CAMERA_VALUE) {
            body = context.getString(R.string.notification_daily_moment_live_body);
        } else if (type == MomentNotification.Type.TEXT_POST_VALUE) {
            body = context.getString(R.string.notification_daily_moment_live_text);
        } else if (type == MomentNotification.Type.PROMPT_POST_VALUE) {
            body = prompt;
        } else {
            contentIntent = new Intent(context, MainActivity.class);
            context.getString(R.string.notification_daily_moment_body);
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        final Intent parentIntent = new Intent(context, MainActivity.class);
        parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        parentIntent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_FEED);
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
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI, AudioManager.STREAM_NOTIFICATION);

        builder.setContentIntent(stackBuilder.getPendingIntent(0, getPendingIntentFlags(true)));

        // TODO: handle clearing of intent
        //Intent deleteIntent = new Intent(context, DeleteMomentNotificationReceiver.class);
        //builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0 , deleteIntent, PendingIntent. FLAG_CANCEL_CURRENT | getPendingIntentFlags(false)));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(DAILY_MOMENT_NOTIFICATION_TAG, DAILY_MOMENT_NOTIFICATION_ID, builder.build());
    }

    public void clearKatchupDailyMomentNotification() {
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
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.finish_registration_notification_text))
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

    static public class DeleteMomentNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive (Context context , Intent intent) {
            Log.i("Notifications.BroadcastReceiver: cancel");
            final long momentNotificationTimeCutoff = intent.getLongExtra(EXTRA_MOMENT_NOTIFICATION_TIME_CUTOFF, 0);
            if (momentNotificationTimeCutoff > 0) {
                Log.i("Notifications.BroadcastReceiver: cancel, moment notification cutoff at " + momentNotificationTimeCutoff);
                Notifications.getInstance(context).executor.execute(() -> Notifications.getInstance(context).preferences.setMomentNotificationTimeCutoff(momentNotificationTimeCutoff));
            }
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
}
