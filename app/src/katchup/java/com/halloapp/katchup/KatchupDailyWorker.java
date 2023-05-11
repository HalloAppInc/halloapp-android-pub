package com.halloapp.katchup;

import android.Manifest;
import android.content.Context;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.FileStore;
import com.halloapp.content.ContentDb;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.emoji.EmojiManager;
import com.halloapp.proto.log_events.Permissions;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Events;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.EasyPermissions;

public class KatchupDailyWorker extends Worker {

    private static final String DAILY_WORKER_ID = "daily-worker";

    public static void schedule(@NonNull Context context) {
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        // Set Execution around 04:00:00 AM
        dueDate.set(Calendar.HOUR_OF_DAY, 4);
        dueDate.set(Calendar.MINUTE, 0);
        dueDate.set(Calendar.SECOND, 0);
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24);
        }
        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
        if (timeDiff < DateUtils.HOUR_IN_MILLIS) {
            timeDiff += DateUtils.DAY_IN_MILLIS;
        }
        final OneTimeWorkRequest dailyWorkRequest = (new OneTimeWorkRequest.Builder(KatchupDailyWorker.class)).setInitialDelay(timeDiff, TimeUnit.MILLISECONDS).build();
        WorkManager.getInstance(context).enqueueUniqueWork(DAILY_WORKER_ID, ExistingWorkPolicy.KEEP, dailyWorkRequest);
    }

    static void scheduleDebug(@NonNull Context context) {
        final OneTimeWorkRequest dailyWorkRequest = (new OneTimeWorkRequest.Builder(KatchupDailyWorker.class)).setInitialDelay(100, TimeUnit.MILLISECONDS).build();
        WorkManager.getInstance(context).enqueueUniqueWork(DAILY_WORKER_ID, ExistingWorkPolicy.REPLACE, dailyWorkRequest);
    }

    public KatchupDailyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public @NonNull Result doWork() {
        Log.i("KatchupDailyWorker.doWork");
        // TODO(jack): Clean up ContentDb as well
//        ContentDb.getInstance().cleanup();
        ContentDb.getInstance().deleteOldSeenReceipts();
        FileStore.getInstance().cleanup();
        EmojiManager.getInstance().checkUpdate();
        reportPermissionStats();
        schedule(getApplicationContext());
        return Result.success();
    }

    private void reportPermissionStats() {
        Events events = Events.getInstance();
        boolean hasContactsPermissions = EasyPermissions.hasPermissions(getApplicationContext(), Manifest.permission.READ_CONTACTS);
        events.sendEvent(Permissions.newBuilder().setType(Permissions.Type.CONTACTS).setStatus(hasContactsPermissions ? Permissions.Status.ALLOWED : Permissions.Status.DENIED).build());
        boolean hasLocationPermissions = EasyPermissions.hasPermissions(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) ||
                EasyPermissions.hasPermissions(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        events.sendEvent(Permissions.newBuilder().setType(Permissions.Type.LOCATION).setStatus(hasLocationPermissions ? Permissions.Status.ALLOWED : Permissions.Status.DENIED).build());

        boolean areNotificationsEnabled = NotificationManagerCompat.from(getApplicationContext()).areNotificationsEnabled();
        events.sendEvent(Permissions.newBuilder().setType(Permissions.Type.NOTIFICATIONS).setStatus(areNotificationsEnabled ? Permissions.Status.ALLOWED : Permissions.Status.DENIED).build());
    }
}
