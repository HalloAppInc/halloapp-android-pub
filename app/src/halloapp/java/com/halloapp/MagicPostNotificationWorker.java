package com.halloapp;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.content.ContentDb;
import com.halloapp.ui.mediapicker.GalleryDataSource;
import com.halloapp.ui.mediapicker.GalleryItem;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MagicPostNotificationWorker extends Worker {

    private static final String MAGIC_POST_WORKER_ID = "magic-post-worker";

    private static final int DELAY_IN_MINUTES = 30;
    private static final int GALLERY_DEFAULT_SIZE = 100;
    private static final long ONE_DAY_AGO_IN_SECONDS = (System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS) / 1000;

    private static final String WORKER_PARAM_SIZE = "size";
    private static final String WORKER_PARAM_TIME = "time";
    private static final String WORKER_PARAM_LOCATION = "location";

    public static void schedule(@NonNull Context context, int size, @NonNull String location, long time) {
        Log.i("Scheduling a magic post notification at location " + location + " at " + time);
        Data inputData = new Data.Builder().putInt(WORKER_PARAM_SIZE, size).putString(WORKER_PARAM_LOCATION, location).putLong(WORKER_PARAM_TIME, time).build();
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MagicPostNotificationWorker.class).setInputData(inputData).setInitialDelay(DELAY_IN_MINUTES, TimeUnit.MINUTES).addTag(location).build();
        // Remove any previous scheduled notifications at the same location
        WorkManager.getInstance(context).cancelAllWorkByTag(location);
        WorkManager.getInstance(context).enqueueUniqueWork(MAGIC_POST_WORKER_ID, ExistingWorkPolicy.APPEND_OR_REPLACE, workRequest);
    }

    public MagicPostNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @Override
    public @NonNull Result doWork() {
        Data data = getInputData();
        int size = data.getInt(WORKER_PARAM_SIZE, 0);
        String location = data.getString(WORKER_PARAM_LOCATION);
        long time = data.getLong(WORKER_PARAM_TIME, 0);
        if (size > 0 && !TextUtils.isEmpty(location)) {
            sendMagicPostNotification(getApplicationContext(), size, location);
            Preferences.getInstance().setPrefLastMagicPostNotificationTimeInMillis(time);
        }
        return Result.success();
    }

    private void sendMagicPostNotification(@NonNull Context context, int photoSize, @NonNull String coordinateLocation) {
        Log.i("Sending magic post notification for " + coordinateLocation + " with " + photoSize + " photos");
        BgWorkers.getInstance().execute(() -> {
            String[] latLong = coordinateLocation.split(" ");
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            String location = null;
            try {
                List<Address> addresses = geocoder.getFromLocation(Double.parseDouble(latLong[0]), Double.parseDouble(latLong[1]), 1);
                if (addresses != null && addresses.size() > 0) {
                    location = addresses.get(0).getThoroughfare();
                    location = location == null ? addresses.get(0).getLocality() : location;
                }
            } catch (IOException e) {
                Log.e("MagicPostNotificationWorker/sendMagicPostNotification failed to get location", e);
            }
            // It's unlikely that our gallery listener captured all the media in the background so we reload the gallery
            getRecentGalleryItems();
            Notifications.getInstance(context).showMagicPostNotification(photoSize, location);
        });
    }

    private void getRecentGalleryItems() {
        Log.i("Pulling gallery items from the last day");
        GalleryDataSource source = new GalleryDataSource(getApplicationContext().getContentResolver(), true, ONE_DAY_AGO_IN_SECONDS);
        List<GalleryItem> allPhotos = source.load(null, false, GALLERY_DEFAULT_SIZE);
        for (GalleryItem galleryItem : allPhotos) {
            ContentDb.getInstance().addGalleryItemUri(galleryItem.id, galleryItem.type, galleryItem.date, galleryItem.duration);
        }
    }
}
