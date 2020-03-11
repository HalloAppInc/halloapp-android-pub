package com.halloapp.ui.mediapicker;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.halloapp.util.Log;

import java.util.ArrayList;
import java.util.List;

public class GalleryDataSource extends ItemKeyedDataSource<Long, GalleryItem> {

    @SuppressLint("InlinedApi")
    public static final String MEDIA_VOLUME = MediaStore.VOLUME_EXTERNAL;

    final private ContentResolver contentResolver;
    final private boolean includeVideos;

    private static final String MEDIA_TYPE_SELECTION = MediaStore.Files.FileColumns.MEDIA_TYPE + " IN ('" +
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + "', '" +
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + "')";
    private static final String MEDIA_TYPE_IMAGES_ONLY = MediaStore.Files.FileColumns.MEDIA_TYPE + " IN ('" +
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + "')";
    private static final String[] MEDIA_PROJECTION = new String[] {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            //MediaStore.Files.FileColumns.DURATION
    };

    private GalleryDataSource(ContentResolver contentResolver, boolean includeVideos) {
        this.contentResolver = contentResolver;
        this.includeVideos = includeVideos;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<GalleryItem> callback) {
        callback.onResult(load(null, true, params.requestedLoadSize));
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<GalleryItem> callback) {
        callback.onResult(load(params.key, true, params.requestedLoadSize));
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<GalleryItem> callback) {
        callback.onResult(load(params.key, false, params.requestedLoadSize));
    }

    private List<GalleryItem> load(Long start, boolean after, int size) {
        final List<GalleryItem> galleryItems = new ArrayList<>();
        try (final Cursor cursor = contentResolver.query(
                MediaStore.Files.getContentUri(MEDIA_VOLUME),
                MEDIA_PROJECTION,
                (includeVideos ? MEDIA_TYPE_SELECTION : MEDIA_TYPE_IMAGES_ONLY) + (start == null ? "" : ((after ? " AND _id<" : " AND _id>") + start)),
                null,
                "_id DESC LIMIT " + size,
                null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    galleryItems.add(new GalleryItem(cursor.getLong(0), cursor.getInt(1)));
                }
            }
        } catch (SecurityException ex) {
            Log.w("GalleryDataSource.load", ex);
        }
        return galleryItems;
    }

    @Override
    public @NonNull Long getKey(@NonNull GalleryItem item) {
        return item.id;
    }


    public static class Factory extends DataSource.Factory<Long, GalleryItem> {

        final ContentResolver contentResolver;
        final boolean includeVideos;

        Factory(ContentResolver contentResolver, boolean includeVideos) {
            this.contentResolver = contentResolver;
            this.includeVideos = includeVideos;
        }

        @Override
        public @NonNull DataSource<Long, GalleryItem> create() {
            return new GalleryDataSource(contentResolver, includeVideos);
        }
    }
}
