package com.halloapp.posts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.halloapp.R;
import com.halloapp.util.Log;
import com.halloapp.util.ViewDataLoader;

import java.io.File;
import java.util.concurrent.Callable;

public class PostsImageLoader extends ViewDataLoader<ImageView, Bitmap, Long> {

    private final Context context;
    private final LruCache<Long, Bitmap> cache;

    @MainThread
    public PostsImageLoader(@NonNull Context context) {
        this.context = context;

        // Use 1/8th of the available memory for memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        Log.i("create " + cacheSize + "KB cache for post imeges");
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
                File file = new File(context.getFilesDir(), post.file);
                if (file.exists()) {
                    bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                }
            }
            if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.test0);
            } else {
                return bitmap;
            }

        };
        final ViewDataLoader.Displayer<ImageView, Bitmap> displayer = new ViewDataLoader.Displayer<ImageView, Bitmap>() {

            @Override
            public void showResult(ImageView view, Bitmap result) {
                view.setImageBitmap(result);
            }

            @Override
            public void showLoading(ImageView view) {
                view.setImageDrawable(null);
            }
        };
        load(view, loader, displayer, post.rowId, cache);
    }
}
