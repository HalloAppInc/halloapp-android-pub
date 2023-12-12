package com.halloapp;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.exifinterface.media.ExifInterface;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.content.ContentDb;
import com.halloapp.ui.mediapicker.GalleryDataSource;
import com.halloapp.ui.mediapicker.GalleryItem;
import com.halloapp.util.logs.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@RequiresApi(api = 24)
public class GalleryWorker extends Worker {

    private static final String GALLERY_WORKER_ID = "gallery-worker";

    private static final long CUTOFF_TIME_IN_SECONDS = (System.currentTimeMillis() - (2 * DateUtils.WEEK_IN_MILLIS)) / 1000;
    private static final int MIN_GALLERY_ITEMS_FOR_SUGGESTION = 2;
    private static final int GALLERY_SIZE = 100;
    private static final int MAX_DELAY_TIME = 50;

    public static void schedule(@NonNull Context context) {
        Log.i("Scheduling gallery worker");
        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(GalleryWorker.class);
        builder.setConstraints(new Constraints.Builder()
                .setTriggerContentMaxDelay(MAX_DELAY_TIME, TimeUnit.MILLISECONDS)
                .addContentUriTrigger(MediaStore.Images.Media.INTERNAL_CONTENT_URI, true)
                .addContentUriTrigger(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true)
                .addContentUriTrigger(MediaStore.Video.Media.INTERNAL_CONTENT_URI, true)
                .addContentUriTrigger(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true)
                .build());

        OneTimeWorkRequest workRequest = builder.build();
        WorkManager.getInstance(context).enqueueUniqueWork(GALLERY_WORKER_ID, ExistingWorkPolicy.REPLACE, workRequest);
    }

    public static long getUriId(@NonNull String uri) {
        String uriId = uri.substring(uri.lastIndexOf("/") + 1);
        return TextUtils.isDigitsOnly(uriId) ? Long.parseLong(uriId) : -1;
    }

    public GalleryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public @NonNull Result doWork() {
        Preferences preferences = Preferences.getInstance();
        List<Uri> uris = getTriggeredContentUris();
        long lastPostNotificationTime = preferences.getPrefLastMagicPostNotificationTimeInMillis();
        ArrayList<GalleryItem> pendingGalleryItems = ContentDb.getInstance().getPendingGalleryItems(lastPostNotificationTime);
        for (Uri uri : uris) {
            Log.i("GalleryWorker.doWork received uri: " + uri);
            long uriId = getUriId(uri.toString());
            if (uriId == -1) {
                Log.i("Received invalid uri id - resetting suggestions and gallery items");
                resetGalleryItems();
                resetSuggestions();
            } else if (uri.toString().startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())) {
                try {
                    ExifInterface exif = new ExifInterface(getApplicationContext().getContentResolver().openInputStream(uri));
                    double[] latLong = exif.getLatLong();
                    latLong = latLong == null ? new double[]{0,0} : latLong;
                    GalleryItem galleryItem = new GalleryItem(uriId, MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, System.currentTimeMillis(), 0, latLong[0] , latLong[1], null);
                    ContentDb.getInstance().addGalleryItem(galleryItem, null);
                    pendingGalleryItems.add(galleryItem);
                    processPendingGalleryItems(pendingGalleryItems);
                } catch (FileNotFoundException | IllegalStateException e) {
                    Log.i("GalleryWorker.doWork missing file - removing item", e);
                    deleteGalleryItem(uriId);
                } catch (IOException e) {
                    Log.w("GalleryWorker.doWork IOException - removing item", e);
                    deleteGalleryItem(uriId);
                } catch (SecurityException e) {
                    Log.w("GalleryWorker.doWork SecurityException - removing item", e);
                    deleteGalleryItem(uriId);
                }
            } else if (uri.toString().startsWith(MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString())) {
                long duration = getDuration(uri);
                if (duration > 0) {
                    GalleryItem galleryItem = new GalleryItem(uriId, MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO, System.currentTimeMillis() / 1000, getDuration(uri));
                    ContentDb.getInstance().addGalleryItem(galleryItem, null);
                } else {
                    deleteGalleryItem(uriId);
                }
            }
        }
        schedule(getApplicationContext());
        return Result.success();
    }

    private long getDuration(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(AppContext.getInstance().get(), uri);
            String durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durationString == null) {
                Log.w("GalleryWorker.getDuration got null duration");
                return 0;
            }
            return Long.parseLong(durationString);
        } catch (RuntimeException e) {
            Log.w("GalleryWorker.getDuration retriever failed", e);
            return -1;
        } finally  {
            try {
                retriever.release();
            } catch (IOException e) {
                Log.e("GalleryWorker.getDuration retriever release failed", e);
            }
        }
    }

    private void deleteGalleryItem(long uriId) {
        ContentDb.getInstance().deleteGalleryItemFromSuggestion(uriId);
        ContentDb.getInstance().deleteGalleryItem(uriId);
    }

    private void resetGalleryItems() {
        ContentDb.getInstance().deleteAllGalleryItems();
        GalleryDataSource source = new GalleryDataSource(getApplicationContext().getContentResolver(), true, CUTOFF_TIME_IN_SECONDS);
        List<GalleryItem> allGalleryItems = source.load(null, false, GALLERY_SIZE);
        for (GalleryItem galleryItem : allGalleryItems) {
            ContentDb.getInstance().addGalleryItemUri(galleryItem.id, galleryItem.type, galleryItem.date, galleryItem.duration);
        }
    }

    private void resetSuggestions() {
        ContentDb contentDb = ContentDb.getInstance();
        ArrayList<Suggestion> existingSuggestions = contentDb.getAllSuggestions();
        ArrayList<Suggestion> updatedSuggestions = new ArrayList<>();
        for (Suggestion suggestion : existingSuggestions) {
            if (suggestion.size != 0) {
                suggestion.size = 0;
                updatedSuggestions.add(suggestion);
            }
        }
        contentDb.addAllSuggestions(updatedSuggestions);
    }

    private void processPendingGalleryItems(@NonNull ArrayList<GalleryItem> pendingGalleryItems) {
        HashMap<String, Integer> locationCount = new HashMap<>();
        if (pendingGalleryItems.size() > MIN_GALLERY_ITEMS_FOR_SUGGESTION) {
            for (GalleryItem galleryItem : pendingGalleryItems) {
                // Weak clustering is done by rounding latitude & longitude to the thousandths place
                // since this information is only used to potentially send a notification
                // The actual clustering will happen after the app gets opened in MagicPostViewModel
                if (galleryItem.latitude != 0 || galleryItem.longitude != 0) {
                    String key = String.format(Locale.US, "%.3f %.3f", galleryItem.latitude, galleryItem.longitude);
                    locationCount.merge(key, 1, Integer::sum);
                }
            }
        }
        for (String location : locationCount.keySet()) {
            Integer galleryItemSize = locationCount.get(location);
            if (galleryItemSize != null && galleryItemSize > MIN_GALLERY_ITEMS_FOR_SUGGESTION && !TextUtils.isEmpty(location)) {
                MagicPostNotificationWorker.schedule(getApplicationContext(), galleryItemSize, location, System.currentTimeMillis());
            }
        }
    }
}
