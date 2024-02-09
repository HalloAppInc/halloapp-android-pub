package com.halloapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MagicPostPsaWorker extends Worker {

    private static final String MAGIC_POST_PSA_WORKER_ID = "magic-post-psa-worker";

    // PSA should be sent for four weeks alternating between Sunday and Saturday
    private static final int[] DAY_FOR_NOTIFICATION = new int[]{Calendar.SUNDAY, Calendar.SATURDAY, Calendar.SUNDAY, Calendar.SATURDAY};
    private static final int MAX_NUM_NOTIFICATIONS = 4;

    static void schedule(@NonNull Context context) {
        BgWorkers.getInstance().execute(() -> {
            Preferences preferences = Preferences.getInstance();
            int numNotificationsShown = preferences.getPrefMagicPostPsaNotificationNum();
            if (numNotificationsShown >= MAX_NUM_NOTIFICATIONS) {
                return;
            }
            Log.i("MagicPostPsaWorker.schedule number of notifications previously shown: " + numNotificationsShown);
            Calendar currentDate = Calendar.getInstance();
            Calendar dueDate = Calendar.getInstance();
            // Set Execution around 05:00:00 PM on either Sun/Sat (based on DAY_FOR_NOTIFICATION)
            dueDate.set(Calendar.HOUR_OF_DAY, 17);
            dueDate.set(Calendar.MINUTE, 0);
            dueDate.set(Calendar.SECOND, 0);

            int dayDiff = DAY_FOR_NOTIFICATION[numNotificationsShown] - dueDate.get(Calendar.DAY_OF_WEEK);
            if (dayDiff < 0) {
                dayDiff += 7;
            }
            dueDate.add(Calendar.DAY_OF_MONTH, dayDiff);

            long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
            final OneTimeWorkRequest dailyWorkRequest = (new OneTimeWorkRequest.Builder(MagicPostPsaWorker.class)).setInitialDelay(timeDiff, TimeUnit.MILLISECONDS).build();
            WorkManager.getInstance(context).enqueueUniqueWork(MAGIC_POST_PSA_WORKER_ID, ExistingWorkPolicy.KEEP, dailyWorkRequest);
        });
    }

    public MagicPostPsaWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public @NonNull Result doWork() {
        Log.i("MagicPostPsaWorker.doWork");
        notifyAndSchedulePsa(getApplicationContext());
        return Result.success();
    }

    private static void notifyAndSchedulePsa(@NonNull Context context) {
        Notifications.getInstance(context).showMagicPostPsaNotification();
        Preferences.getInstance().incrementPrefMagicPostPsaNotificationNum();
        schedule(context);
    }
}
