package com.halloapp.ui.mediapicker;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;
import androidx.core.content.ContextCompat;

import com.halloapp.R;
import com.halloapp.util.ViewDataLoader;

import java.util.concurrent.Callable;

public class GalleryThumbnailLoader extends ViewDataLoader<ImageView, Bitmap, Long>  {

    private final LruCache<Long, Bitmap> cache;
    private final int placeholderColor;
    private final int dimensionLimit;
    private final ContentResolver contentResolver;

    private static final Bitmap INVALID_BITMAP = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);

    @MainThread
    public GalleryThumbnailLoader(@NonNull Context context, int dimensionLimit) {

        this.dimensionLimit = dimensionLimit;
        contentResolver = context.getContentResolver();
        placeholderColor = ContextCompat.getColor(context, R.color.gallery_placeholder);

        final long cacheSize = Runtime.getRuntime().maxMemory() / 8;
        cache = new LruCache<Long, Bitmap>((int)cacheSize) {

            @Override
            protected int sizeOf(@NonNull Long key, @NonNull Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
    }

    @MainThread
    public void load(@NonNull ImageView view, @NonNull GalleryItem galleryItem) {
        final Callable<Bitmap> loader = () -> {
            Bitmap bitmap = null;
            if (galleryItem.type == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                bitmap = MediaStore.Images.Thumbnails.getThumbnail(contentResolver, galleryItem.id, MediaStore.Images.Thumbnails.MINI_KIND, null);
            } else {
                bitmap = MediaStore.Video.Thumbnails.getThumbnail(contentResolver, galleryItem.id, MediaStore.Video.Thumbnails.MINI_KIND, null);
            }

            if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
                return INVALID_BITMAP;
            } else {
                final float scale = Math.min(1f * dimensionLimit / bitmap.getWidth(), 1f * dimensionLimit / bitmap.getHeight());
                final Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scale), (int) (bitmap.getHeight() * scale), true);
                if (scaledBitmap != bitmap) {
                    bitmap.recycle();;
                    bitmap = scaledBitmap;
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
