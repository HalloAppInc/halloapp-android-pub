package com.halloapp.ui.mediapicker;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Size;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import com.halloapp.R;
import com.halloapp.util.ViewDataLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class GalleryThumbnailLoader extends ViewDataLoader<ImageView, Bitmap, Long> {

    private final LruCache<Long, Bitmap> cache;
    private final int placeholderColor;
    private final int dimensionLimit;
    private final ContentResolver contentResolver;

    private static final Bitmap INVALID_BITMAP = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);

    @MainThread
    GalleryThumbnailLoader(@NonNull Context context, int dimensionLimit) {
        super(Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1)));

        this.dimensionLimit = dimensionLimit;
        contentResolver = context.getContentResolver();
        placeholderColor = ContextCompat.getColor(context, R.color.gallery_placeholder);

        final long cacheSize = Runtime.getRuntime().maxMemory() / 8;
        cache = new LruCache<Long, Bitmap>((int) cacheSize) {

            @Override
            protected int sizeOf(@NonNull Long key, @NonNull Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
    }

    @MainThread
    public void load(@NonNull ImageView view, @NonNull GalleryItem galleryItem) {
        final Callable<Bitmap> loader = () -> {
            Bitmap bitmap;
            int rotation = 0;
            if (Build.VERSION.SDK_INT >= 29) {
                // ContentResolver.loadThumbnail takes care of orientation
                if (galleryItem.type == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                    bitmap = contentResolver.loadThumbnail(ContentUris.withAppendedId(MediaStore.Images.Media.getContentUri(GalleryDataSource.MEDIA_VOLUME), galleryItem.id), new Size(dimensionLimit, dimensionLimit), null);
                } else {
                    bitmap = contentResolver.loadThumbnail(ContentUris.withAppendedId(MediaStore.Video.Media.getContentUri(GalleryDataSource.MEDIA_VOLUME), galleryItem.id), new Size(dimensionLimit, dimensionLimit), null);
                }
            } else {
                if (galleryItem.type == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                    //noinspection deprecation
                    bitmap = MediaStore.Images.Thumbnails.getThumbnail(contentResolver, galleryItem.id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                    try (InputStream inputStream = contentResolver.openInputStream(ContentUris.withAppendedId(MediaStore.Files.getContentUri(GalleryDataSource.MEDIA_VOLUME), galleryItem.id))) {
                        if (inputStream != null) {
                            final ExifInterface exifInterface = new ExifInterface(inputStream);
                            final int exifRotation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                            if (exifRotation != ExifInterface.ORIENTATION_UNDEFINED) {
                                switch (exifRotation) {
                                    case ExifInterface.ORIENTATION_ROTATE_180:
                                        rotation = 180;
                                        break;
                                    case ExifInterface.ORIENTATION_ROTATE_270:
                                        rotation = 270;
                                        break;
                                    case ExifInterface.ORIENTATION_ROTATE_90:
                                        rotation = 90;
                                        break;
                                }
                            }
                        }
                    } catch (IOException ignore) { }
                } else {
                    //noinspection deprecation
                    bitmap = MediaStore.Video.Thumbnails.getThumbnail(contentResolver, galleryItem.id, MediaStore.Video.Thumbnails.MINI_KIND, null);
                }
            }
            if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
                return INVALID_BITMAP;
            } else {
                final float scale = Math.max(1f * dimensionLimit / bitmap.getWidth(), 1f * dimensionLimit / bitmap.getHeight());
                if (scale < .5f || rotation != 0) {
                    final Matrix matrix = new Matrix();
                    if (scale < 1f) {
                        matrix.setScale(scale, scale);
                    }
                    matrix.setRotate(rotation);
                    final Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    if (scaledBitmap != bitmap) {
                        bitmap.recycle();
                        bitmap = scaledBitmap;
                    }
                }
                return bitmap;
            }

        };
        final ViewDataLoader.Displayer<ImageView, Bitmap> displayer = new ViewDataLoader.Displayer<ImageView, Bitmap>() {

            @Override
            public void showResult(@NonNull ImageView view, Bitmap result) {
                if (result == INVALID_BITMAP) {
                    view.setImageResource(R.drawable.ic_bad_media);
                } else {
                    view.setImageBitmap(result);
                }
                view.setBackgroundColor(0);
            }

            @Override
            public void showLoading(@NonNull ImageView view) {
                view.setImageDrawable(null);
                view.setBackgroundColor(placeholderColor);
            }
        };
        load(view, loader, displayer, galleryItem.id, cache);
    }
}
