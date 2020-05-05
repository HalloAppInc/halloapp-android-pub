package com.halloapp.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;
import androidx.collection.LruCache;
import androidx.core.content.ContextCompat;

import com.halloapp.BuildConfig;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.util.Log;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.widget.PlaceholderDrawable;
import com.halloapp.widget.PostImageView;

import java.io.File;
import java.util.concurrent.Callable;

public class MediaThumbnailLoader extends ViewDataLoader<ImageView, Bitmap, File> {

    private final LruCache<File, Bitmap> cache;
    private final int placeholderColor;
    private final int dimensionLimit;

    private static final Bitmap INVALID_BITMAP = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);

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
        if (media.file == null) {
            view.setImageDrawable(new PlaceholderDrawable(media.width, media.height, placeholderColor));
            return;
        }
        if (media.file.equals(view.getTag()) && view.getDrawable() != null) {
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
        final ViewDataLoader.Displayer<ImageView, Bitmap> displayer = new ViewDataLoader.Displayer<ImageView, Bitmap>() {

            @Override
            public void showResult(@NonNull ImageView view, Bitmap result) {
                if (result == INVALID_BITMAP && media.width != 0 && media.height != 0) {
                    view.setImageDrawable(new PlaceholderDrawable(media.width, media.height, placeholderColor));
                } else {
                    final Drawable oldDrawable = view.getDrawable();
                    view.setImageBitmap(result);
                    if (oldDrawable instanceof PlaceholderDrawable && view instanceof PostImageView) {
                        view.setBackgroundColor(placeholderColor);
                        ((PostImageView)view).playTransition(150);
                    }
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
        load(view, loader, displayer, media.file, cache);
    }
}
