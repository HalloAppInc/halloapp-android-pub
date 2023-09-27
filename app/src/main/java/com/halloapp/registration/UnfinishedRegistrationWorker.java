package com.halloapp.registration;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.Me;
import com.halloapp.Notifications;
import com.halloapp.Preferences;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class UnfinishedRegistrationWorker extends Worker {

    private static final String UNFINISHED_REGISTRATION_WORKER_ID = "unfinished-registration-worker";
    private static final BgWorkers bgWorkers = BgWorkers.getInstance();

    public static void schedule(@NonNull Context context) {
        Preferences preferences = Preferences.getInstance();
        CheckRegistration.CheckResult result = CheckRegistration.checkRegistration(Me.getInstance(), preferences);
        boolean isRegistered = result.registered;
        boolean hasUsername = result.username != null;
        if (!isRegistered || !hasUsername) {
            bgWorkers.execute(() -> {
                long currentTime = System.currentTimeMillis();
                long previousNotificationTime = !isRegistered ? preferences.getPrevUnfinishedRegistrationNotificationTimeInMillis() : preferences.getPrefPrevFriendModelNotifyTimeInMillis();
                int time1 = !isRegistered ? preferences.getUnfinishedRegistrationNotifyDelayInDaysTimeOne() : preferences.getPrefFriendModelDelayInDaysTimeOne();
                long timeUntilNextNotification = TimeUnit.DAYS.toMillis(time1) - (currentTime - previousNotificationTime);
                Log.i("currentTime = " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.US).format(new Date()) + " timeUntilNextNotification = " + TimeUnit.MILLISECONDS.toHours(timeUntilNextNotification) + "hours " + TimeUnit.MILLISECONDS.toMinutes(timeUntilNextNotification) % 60 + " minutes");
                if (timeUntilNextNotification > 0) {
                    final OneTimeWorkRequest workRequest = (new OneTimeWorkRequest.Builder(UnfinishedRegistrationWorker.class)).setInitialDelay(timeUntilNextNotification, TimeUnit.MILLISECONDS).build();
                    WorkManager.getInstance(context).enqueueUniqueWork(UNFINISHED_REGISTRATION_WORKER_ID, ExistingWorkPolicy.REPLACE, workRequest);
                } else if (!isRegistered) {
                    notifyAndScheduleUnfinishedRegistration(context, preferences);
                } else {
                    notifyAndScheduleFriendModel(context, preferences);
                }
            });
        }
    }
    
    public UnfinishedRegistrationWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @Override
    public @NonNull Result doWork() {
        Preferences preferences = Preferences.getInstance();
        CheckRegistration.CheckResult result = CheckRegistration.checkRegistration(Me.getInstance(), preferences);
        if (!result.registered) {
            Log.i("UnfinishedRegistrationWorker.doWork missing registration");
            Context context = getApplicationContext();
            notifyAndScheduleUnfinishedRegistration(context, preferences);
        } else if (result.username == null) {
            Log.i("UnfinishedRegistrationWorker.doWork missing username");
            Context context = getApplicationContext();
            notifyAndScheduleFriendModel(context, preferences);
        }
        return Result.success();
    }

    private static void notifyAndScheduleUnfinishedRegistration(@NonNull Context context, @NonNull Preferences preferences) {
        Notifications.getInstance(context).showFinishRegistrationNotification();
        Preferences.getInstance().setPrevUnfinishedRegistrationNotificationTimeInMillis(System.currentTimeMillis());
        int time1 = preferences.getUnfinishedRegistrationNotifyDelayInDaysTimeOne();
        int time2 = preferences.getUnfinishedRegistrationNotifyDelayInDaysTimeTwo();
        preferences.setUnfinishedRegistrationNotifyDelayInDaysTimeOne(time2);
        preferences.setUnfinishedRegistrationNotifyDelayInDaysTimeTwo(time1 + time2);
        schedule(context);
    }

    private static void notifyAndScheduleFriendModel(@NonNull Context context, @NonNull Preferences preferences) {
        Notifications.getInstance(context).showFriendModelNotification();
        Preferences.getInstance().setPrefPrevFriendModelNotifyTimeInMillis(System.currentTimeMillis());
        int time1 = preferences.getPrefFriendModelDelayInDaysTimeOne();
        int time2 = preferences.getPrefFriendModelDelayInDaysTimeTwo();
        preferences.setPrefFriendModelDelayInDaysTimeOne(time2);
        preferences.setPrefFriendModelDelayInDaysTimeTwo(time1 + time2);
        schedule(context);
    }
}
