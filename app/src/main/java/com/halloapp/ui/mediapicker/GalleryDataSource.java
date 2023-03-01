package com.halloapp.ui.mediapicker;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.halloapp.AppContext;
import com.halloapp.util.logs.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GalleryDataSource extends ItemKeyedDataSource<Long, GalleryItem> {

    @SuppressLint("InlinedApi")
    public static final String MEDIA_VOLUME = MediaStore.VOLUME_EXTERNAL;

    final private ContentResolver contentResolver;
    final private boolean includeVideos;

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
        try (final Cursor cursor = getCursor(start, after, size)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    int type = cursor.getInt(1);
                    long duration = 0;
                    long date = cursor.getLong(2);

                    if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                        if (Build.VERSION.SDK_INT >= 29) {
                            duration = cursor.getLong(3);
                        } else {
                            duration = getDuration(id);
                        }
                    }

                    galleryItems.add(new GalleryItem(id, type, date, duration));
                }
            }
        } catch (SecurityException ex) {
            Log.w("GalleryDataSource.load", ex);
        }
        return galleryItems;
    }

    private Cursor getCursor(Long start, boolean after, int size) {
        String[] projection;
        if (Build.VERSION.SDK_INT >= 29) {
            projection = Arrays.copyOf(MEDIA_PROJECTION, MEDIA_PROJECTION.length + 1);
            projection[projection.length - 1] = MediaStore.Files.FileColumns.DURATION;
        } else {
            projection = MEDIA_PROJECTION;
        }

        String selection = (includeVideos ? MEDIA_TYPE_SELECTION : MEDIA_TYPE_IMAGES_ONLY) + (start == null ? "" : ((after ? " AND _id<" : " AND _id>") + start));
        Uri uri = MediaStore.Files.getContentUri(MEDIA_VOLUME);
        if (Build.VERSION.SDK_INT >= 30) { // Adding LIMIT in the sortOrder field like below causes a crash starting on API 30
            Bundle queryArgs = new Bundle();
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection);
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, "_id DESC");
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_LIMIT, Integer.toString(size));
            return contentResolver.query(uri, projection, queryArgs, null);
        } else {
            return contentResolver.query(uri, projection, selection, null, "_id DESC LIMIT " + size, null);
        }
    }

    private long getDuration(long id) {
        Uri uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri(MEDIA_VOLUME), id);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try {
            retriever.setDataSource(AppContext.getInstance().get(), uri);
            String durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durationString == null) {
                Log.w("GalleryDataSource.getDuration got null duration");
                return 0;
            }
            return Long.parseLong(durationString);
        } catch (IllegalArgumentException e) {
            Log.e("GalleryDataSource.getDuration invalid uri " + uri + " for id " + id, e);
            return 0;
        } catch (RuntimeException e) {
            Log.e("GalleryDataSource.getDuration other exception with uri " + uri + " for id " + id, e);
            return 0;
        } finally  {
            retriever.release();
        }
    }

    @Override
    public @NonNull Long getKey(@NonNull GalleryItem item) {
        return item.id;
    }


    public static class Factory extends DataSource.Factory<Long, GalleryItem> {

        final ContentResolver contentResolver;
        final boolean includeVideos;
        private GalleryDataSource latestSource;

        public Factory(ContentResolver contentResolver, boolean includeVideos) {
            this.contentResolver = contentResolver;
            this.includeVideos = includeVideos;
            latestSource = new GalleryDataSource(contentResolver, includeVideos);
        }

        @Override
        public @NonNull DataSource<Long, GalleryItem> create() {
            if (latestSource.isInvalid()) {
                Log.i("GalleryDataSource.Factory.create old source was invalidated; creating a new one");
                latestSource = new GalleryDataSource(contentResolver, includeVideos);
            }
            return latestSource;
        }

        public void invalidateLatestDataSource() {
            Log.i("GalleryDataSource.Factory.invalidateLatestDataSource");
            latestSource.invalidate();
        }
    }
}
