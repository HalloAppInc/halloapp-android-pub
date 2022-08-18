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
import androidx.palette.graphics.Palette;

import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.util.logs.Log;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.widget.PlaceholderDrawable;
import com.halloapp.widget.ContentPhotoView;

import java.io.File;
import java.util.concurrent.Callable;

public class MediaPaletteThumbnailLoader extends ViewDataLoader<ImageView, MediaPaletteThumbnailLoader.ThumbnailWithPalette, File> {

    protected final LruCache<File, MediaPaletteThumbnailLoader.ThumbnailWithPalette> cache;
    protected final int placeholderColor;
    private final int dimensionLimit;

    protected static final Bitmap INVALID_BITMAP = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);

    public static class ThumbnailWithPalette {
        public final Bitmap thumbnail;
        public final Palette palette;

        public ThumbnailWithPalette(Bitmap bitmap, Palette palette) {
            this.thumbnail = bitmap;
            this.palette = palette;
        }
    }

    public interface PaletteLoadListener {
        void onPalette(Palette palette);
    }

    @MainThread
    public MediaPaletteThumbnailLoader(@NonNull Context context, int dimensionLimit) {

        this.dimensionLimit = dimensionLimit;
        placeholderColor = ContextCompat.getColor(context, R.color.media_placeholder);

        // Use 1/8th of the available memory for memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        Log.i("MediaThumbnailLoader: create " + cacheSize + "KB cache for post images");
        cache = new LruCache<File, MediaPaletteThumbnailLoader.ThumbnailWithPalette>(cacheSize) {

            @Override
            protected int sizeOf(@NonNull File key, @NonNull MediaPaletteThumbnailLoader.ThumbnailWithPalette bitmap) {
                // The cache size will be measured in kilobytes rather than number of items
                return bitmap.thumbnail.getByteCount() / 1024;
            }
        };
    }

    @MainThread
    public void load(@NonNull ImageView view, @NonNull Media media, @Nullable PaletteLoadListener listener) {
        final ViewDataLoader.Displayer<ImageView, MediaPaletteThumbnailLoader.ThumbnailWithPalette> displayer = new ViewDataLoader.Displayer<ImageView, MediaPaletteThumbnailLoader.ThumbnailWithPalette>() {

            @Override
            public void showResult(@NonNull ImageView view, @NonNull MediaPaletteThumbnailLoader.ThumbnailWithPalette result) {
                if (result.thumbnail == INVALID_BITMAP && media.width != 0 && media.height != 0) {
                    view.setImageDrawable(new PlaceholderDrawable(media.width, media.height, placeholderColor));
                } else {
                    final Drawable oldDrawable = view.getDrawable();
                    view.setImageBitmap(result.thumbnail);
                    if (oldDrawable instanceof PlaceholderDrawable && view instanceof ContentPhotoView) {
                        view.setBackgroundColor(placeholderColor);
                        ((ContentPhotoView)view).playTransition(150);
                    }
                }
                if (listener != null && result.palette != null) {
                    listener.onPalette(result.palette);
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
        load(view, media, displayer);
    }

    @MainThread
    public void load(@NonNull ImageView view, @NonNull Media media, @NonNull ViewDataLoader.Displayer<ImageView, MediaPaletteThumbnailLoader.ThumbnailWithPalette> displayer) {
        if (media.file == null) {
            view.setImageDrawable(new PlaceholderDrawable(media.width, media.height, placeholderColor));
            return;
        }
        if (media.file.equals(view.getTag()) && view.getDrawable() != null) {
            return; // bitmap can be out of cache, but still attached to image view; since media images are stable we can assume the whatever is loaded for current tag would'n change
        }
        final Callable<ThumbnailWithPalette> loader = () -> {
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
                return new ThumbnailWithPalette(INVALID_BITMAP, null);
            } else {
                return new ThumbnailWithPalette(bitmap, Palette.from(bitmap).generate());
            }

        };
        load(view, loader, displayer, media.file, cache);
    }

    public void remove(@NonNull File file) {
        cache.remove(file);
    }
}
