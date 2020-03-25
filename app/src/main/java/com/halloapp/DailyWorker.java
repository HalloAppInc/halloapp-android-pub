package com.halloapp;

import android.content.Context;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.contacts.ContactsSync;
import com.halloapp.content.ContentDb;
import com.halloapp.util.Log;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class DailyWorker extends Worker {

    private static final String DAILY_WORKER_ID = "daily-worker";

    static void schedule(@NonNull Context context) {
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
        final OneTimeWorkRequest dailyWorkRequest = (new OneTimeWorkRequest.Builder(DailyWorker.class)).setInitialDelay(timeDiff, TimeUnit.MILLISECONDS).build();
        WorkManager.getInstance(context).enqueueUniqueWork(DAILY_WORKER_ID, ExistingWorkPolicy.KEEP, dailyWorkRequest);
    }

    public DailyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public @NonNull Result doWork() {
        Log.i("DailyWorker.doWork");
        ContentDb.getInstance(getApplicationContext()).cleanup();
        FileStore.getInstance(getApplicationContext()).cleanup();
        ContactsSync.getInstance(getApplicationContext()).startContactsSync(true);
        schedule(getApplicationContext());
        return Result.success();
    }
}
