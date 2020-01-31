package com.halloapp.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;
import androidx.core.content.ContextCompat;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.posts.Media;
import com.halloapp.util.Log;
import com.halloapp.util.ViewDataLoader;

import java.util.concurrent.Callable;

public class MediaThumbnailLoader extends ViewDataLoader<ImageView, Bitmap, String> {

    private final LruCache<String, Bitmap> cache;
    private final int placeholderColor;
    private final int dimensionLimit;

    @MainThread
    public MediaThumbnailLoader(@NonNull Context context) {
        this(context, Constants.MAX_IMAGE_DIMENSION);
    }

    @MainThread
    public MediaThumbnailLoader(@NonNull Context context, int dimensionLimit) {

        this.dimensionLimit = dimensionLimit;
        placeholderColor = ContextCompat.getColor(context, R.color.media_placeholder);

        // Use 1/8th of the available memory for memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        Log.i("MediaThumbnailLoader: create " + cacheSize + "KB cache for post images");
        cache = new LruCache<String, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(@NonNull String key, @NonNull Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than number of items
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @MainThread
    public void load(@NonNull ImageView view, @NonNull Media media) {
        final Callable<Bitmap> loader = () -> {
            Bitmap bitmap = null;
            if (media.file != null) {
                if (media.file.exists()) {
                    bitmap = MediaUtils.decode(media.file, dimensionLimit);
                } else {
                    Log.i("MediaThumbnailLoader:load file " + media.file.getAbsolutePath() + " doesn't exist");
                }
            }
            if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
                Log.i("MediaThumbnailLoader:load cannot decode " + media.file);
                return null;
            } else {
                return bitmap;
            }

        };
        final ViewDataLoader.Displayer<ImageView, Bitmap> displayer = new ViewDataLoader.Displayer<ImageView, Bitmap>() {

            @Override
            public void showResult(@NonNull ImageView view, Bitmap result) {
                if (result == null && media.width != 0 && media.height != 0) {
                    view.setImageDrawable(new PlaceholderDrawable(media.width, media.height, placeholderColor));
                } else {
                    view.setImageBitmap(result);
                }
            }

            @Override
            public void showLoading(@NonNull ImageView view) {
                if (media.width != 0 && media.height != 0) {
                    view.setImageDrawable(new PlaceholderDrawable(media.width, media.height, placeholderColor));
                } else {
                    view.setImageDrawable(null);
                }
            }
        };
        load(view, loader, displayer, media.id, cache);
    }

    static class PlaceholderDrawable extends Drawable {

        final int width;
        final int height;
        final int color;

        PlaceholderDrawable(int width, int height, int color) {
            this.width = width;
            this.height = height;
            this.color = color;
        }

        public int getIntrinsicWidth() {
            return width;
        }

        public int getIntrinsicHeight() {
            return height;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            canvas.drawColor(color);
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }
    }
}
