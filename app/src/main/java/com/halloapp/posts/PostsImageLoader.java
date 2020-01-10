package com.halloapp.posts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;
import androidx.core.content.ContextCompat;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.media.MediaStore;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.Log;
import com.halloapp.util.ViewDataLoader;

import java.io.File;
import java.util.concurrent.Callable;

public class PostsImageLoader extends ViewDataLoader<ImageView, Bitmap, Long> {

    private final MediaStore mediaStore;
    private final LruCache<Long, Bitmap> cache;
    private final int placeholderColor;

    @MainThread
    public PostsImageLoader(@NonNull Context context) {

        mediaStore = MediaStore.getInstance(context);
        placeholderColor = ContextCompat.getColor(context, R.color.media_placeholder);

        // Use 1/8th of the available memory for memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        Log.i("PostsImageLoader: create " + cacheSize + "KB cache for post imeges");
        cache = new LruCache<Long, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(@NonNull Long key, @NonNull Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than number of items
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @MainThread
    public void load(@NonNull ImageView view, @NonNull Post post) {
        final Callable<Bitmap> loader = () -> {
            Bitmap bitmap = null;
            if (post.file != null) {
                final File file = mediaStore.getMediaFile(post.file);
                if (file.exists()) {
                    bitmap = MediaUtils.decode(file, Constants.MAX_IMAGE_DIMENSION);
                } else {
                    Log.i("PostsImageLoader:load file " + file.getAbsolutePath() + " doesn't exist");
                }
            }
            if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
                Log.i("PostsImageLoader:load cannot decode " + post.file);
                return null;
            } else {
                return bitmap;
            }

        };
        final ViewDataLoader.Displayer<ImageView, Bitmap> displayer = new ViewDataLoader.Displayer<ImageView, Bitmap>() {

            @Override
            public void showResult(@NonNull ImageView view, Bitmap result) {
                view.setImageBitmap(result);
            }

            @Override
            public void showLoading(@NonNull ImageView view) {
                if (post.width != 0 && post.height != 0) {
                    view.setImageDrawable(new PlaceholderDrawable(post.width, post.height, placeholderColor));
                } else {
                    view.setImageDrawable(null);
                }
            }
        };
        load(view, loader, displayer, post.rowId, cache);
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
