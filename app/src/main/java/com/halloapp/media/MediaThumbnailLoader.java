package com.halloapp.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;
import androidx.core.content.ContextCompat;

import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ContentPhotoView;
import com.halloapp.widget.PlaceholderDrawable;

import java.io.File;
import java.util.concurrent.Callable;

public class MediaThumbnailLoader extends ViewDataLoader<ImageView, Bitmap, File> {

    protected final LruCache<File, Bitmap> cache;
    protected final int placeholderColor;
    protected final int dimensionLimit;

    protected static final Bitmap INVALID_BITMAP = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);

    @MainThread
    public MediaThumbnailLoader(@NonNull Context context, int dimensionLimit) {

        this.dimensionLimit = dimensionLimit;
        placeholderColor = ContextCompat.getColor(context, R.color.media_placeholder);

        // Use 1/8th of the available memory for memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        Log.i("MediaThumbnailLoader: create " + cacheSize + "KB cache for post images");
        cache = new LruCache<File, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(@NonNull File key, @NonNull Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than number of items
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @MainThread
    public void load(@NonNull ImageView view, @NonNull Media media) {
        load(view, media, (Runnable) null);
    }

    @MainThread
    public void load(@NonNull ImageView view, @NonNull Media media, @Nullable Runnable completion) {
        final ViewDataLoader.Displayer<ImageView, Bitmap> displayer = new ViewDataLoader.Displayer<ImageView, Bitmap>() {

            @Override
            public void showResult(@NonNull ImageView view, Bitmap result) {
                if (result == INVALID_BITMAP && media.width != 0 && media.height != 0) {
                    view.setImageDrawable(new PlaceholderDrawable(media.width, media.height, placeholderColor));
                } else {
                    final Drawable oldDrawable = view.getDrawable();
                    view.setImageBitmap(result);
                    if (oldDrawable instanceof PlaceholderDrawable && view instanceof ContentPhotoView) {
                        view.setBackgroundColor(placeholderColor);
                        ((ContentPhotoView)view).playTransition(150);
                    }
                }

                if (completion != null) {
                    completion.run();
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
        load(view, media, displayer, completion);
    }

    @MainThread
    public void load(@NonNull ImageView view, @NonNull Media media, @NonNull ViewDataLoader.Displayer<ImageView, Bitmap> displayer) {
        load(view, media, displayer, null);
    }

    @MainThread
    public void load(@NonNull ImageView view, @NonNull Media media, @NonNull ViewDataLoader.Displayer<ImageView, Bitmap> displayer, @Nullable Runnable completion) {
        if (media.file == null) {
            view.setImageDrawable(new PlaceholderDrawable(media.width, media.height, placeholderColor));
            if (completion != null) {
                completion.run();
            }
            return;
        }
        if (media.file.equals(view.getTag()) && view.getDrawable() != null) {
            if (completion != null) {
                completion.run();
            }
            return; // bitmap can be out of cache, but still attached to image view; since media images are stable we can assume the whatever is loaded for current tag would'n change
        }
        final Callable<Bitmap> loader = () -> {
            Bitmap bitmap = null;
            if (media.file != null) {
                if (media.file.exists()) {
                    bitmap = MediaUtils.decode(media.file, media.type, dimensionLimit);
                } else {
                    Log.i("MediaThumbnailLoader:load file " + media.file.getAbsolutePath() + " doesn't exist");
                }
            }
            if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
                Log.i("MediaThumbnailLoader:load cannot decode " + media.file);
                return INVALID_BITMAP;
            } else {
                return bitmap;
            }

        };
        load(view, loader, displayer, media.file, cache);
    }

    public void remove(@NonNull File file) {
        cache.remove(file);
    }

    public Bitmap getCached(@NonNull File file) {
        return cache.get(file);
    }
}
