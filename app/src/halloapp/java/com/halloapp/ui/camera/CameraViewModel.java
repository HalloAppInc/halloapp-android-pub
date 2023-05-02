package com.halloapp.ui.camera;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.ui.mediapicker.GalleryItem;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.logs.Log;

import java.util.Arrays;

public class CameraViewModel extends AndroidViewModel {

    @SuppressLint("InlinedApi")
    public static final String MEDIA_VOLUME = MediaStore.VOLUME_EXTERNAL;

    private static final String MEDIA_TYPE_SELECTION = MediaStore.Files.FileColumns.MEDIA_TYPE + " IN ('" +
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + "', '" +
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + "') AND " +
            MediaStore.Files.FileColumns.MIME_TYPE + " NOT IN ('image/svg+xml')";
    private static final String MEDIA_TYPE_IMAGES_ONLY = MediaStore.Files.FileColumns.MEDIA_TYPE + " IN ('" +
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + "') AND " +
            MediaStore.Files.FileColumns.MIME_TYPE + " NOT IN ('image/svg+xml')";
    private static final String[] MEDIA_PROJECTION = new String[] {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.DATE_ADDED,
    };

    private ComputableLiveData<GalleryItem> lastGalleryItemLiveData;

    private boolean includeVideos;

    public CameraViewModel(@NonNull Application application, boolean includeVideo) {
        super(application);
        this.includeVideos = includeVideo;
        lastGalleryItemLiveData = new ComputableLiveData<GalleryItem>() {
            @Override
            protected GalleryItem compute() {
                try (final Cursor cursor = getCursor(1)) {
                    if (cursor != null) {
                        if (cursor.moveToNext()) {
                            long id = cursor.getLong(0);
                            int type = cursor.getInt(1);
                            long duration = 0;
                            long date = cursor.getLong(2);

                            return new GalleryItem(id, type, date, duration);
                        }
                    }
                } catch (SecurityException ex) {
                    Log.w("CameraViewModel/galleryItem/load", ex);
                }
                return null;
            }
        };
    }

    private Cursor getCursor(int size) {
        String[] projection;
        if (Build.VERSION.SDK_INT >= 29) {
            projection = Arrays.copyOf(MEDIA_PROJECTION, MEDIA_PROJECTION.length + 1);
            projection[projection.length - 1] = MediaStore.Files.FileColumns.DURATION;
        } else {
            projection = MEDIA_PROJECTION;
        }

        String selection = (includeVideos ? MEDIA_TYPE_SELECTION : MEDIA_TYPE_IMAGES_ONLY);
        Uri uri = MediaStore.Files.getContentUri(MEDIA_VOLUME);
        if (Build.VERSION.SDK_INT >= 30) { // Adding LIMIT in the sortOrder field like below causes a crash starting on API 30
            Bundle queryArgs = new Bundle();
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection);
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, "_id DESC");
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_LIMIT, Integer.toString(size));
            return getApplication().getContentResolver().query(uri, projection, queryArgs, null);
        } else {
            return getApplication().getContentResolver().query(uri, projection, selection, null, "_id DESC LIMIT " + size, null);
        }
    }

    public LiveData<GalleryItem> getLastGalleryItem() {
        return lastGalleryItemLiveData.getLiveData();
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final boolean includeVideos;

        Factory(@NonNull Application application, boolean includeVideos) {
            this.application = application;
            this.includeVideos = includeVideos;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CameraViewModel.class)) {
                //noinspection unchecked
                return (T) new CameraViewModel(application, includeVideos);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
