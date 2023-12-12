package com.halloapp;

import android.content.Context;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.content.ContentDb;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.emoji.EmojiManager;
import com.halloapp.ui.mediapicker.GalleryDataSource;
import com.halloapp.ui.mediapicker.GalleryItem;
import com.halloapp.util.logs.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DailyWorker extends Worker {

    private static final String DAILY_WORKER_ID = "daily-worker";
    private static final long CUTOFF_TIME_IN_SECONDS = (System.currentTimeMillis() - (2 * DateUtils.WEEK_IN_MILLIS)) / 1000;
    private static final int GALLERY_SIZE = 100;

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

    static void scheduleDebug(@NonNull Context context) {
        final OneTimeWorkRequest dailyWorkRequest = (new OneTimeWorkRequest.Builder(DailyWorker.class)).setInitialDelay(100, TimeUnit.MILLISECONDS).build();
        WorkManager.getInstance(context).enqueueUniqueWork(DAILY_WORKER_ID, ExistingWorkPolicy.REPLACE, dailyWorkRequest);
    }

    public DailyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public @NonNull Result doWork() {
        Log.i("DailyWorker.doWork");
        ContentDb.getInstance().cleanup();
        FileStore.getInstance().cleanup();
        EncryptedKeyStore.getInstance().checkIdentityKeyChanges();
        resetGalleryItems();
        schedule(getApplicationContext());
        EmojiManager.getInstance().checkUpdate();
        return Result.success();
    }

    private void resetGalleryItems() {
        ContentDb contentDb = ContentDb.getInstance();
        contentDb.deleteAllGalleryItems();
        GalleryDataSource source = new GalleryDataSource(getApplicationContext().getContentResolver(), true, CUTOFF_TIME_IN_SECONDS);
        List<GalleryItem> allGalleryItems = source.load(null, false, GALLERY_SIZE);
        for (GalleryItem galleryItem : allGalleryItems) {
            contentDb.addGalleryItemUri(galleryItem.id, galleryItem.type, galleryItem.date, galleryItem.duration);
        }
        ArrayList<Suggestion> existingSuggestions = contentDb.getAllSuggestions();
        for (Suggestion suggestion : existingSuggestions) {
            if (suggestion.size != 0) {
                suggestion.size = 0;
            }
        }
        contentDb.addAllSuggestions(existingSuggestions);
    }
}
