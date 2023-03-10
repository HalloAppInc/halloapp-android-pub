package com.halloapp.katchup;

import android.content.Context;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.FileStore;
import com.halloapp.content.ContentDb;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.emoji.EmojiManager;
import com.halloapp.util.logs.Log;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

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
        FileStore.getInstance().cleanup();
        EmojiManager.getInstance().checkUpdate();
        schedule(getApplicationContext());
        return Result.success();
    }
}
