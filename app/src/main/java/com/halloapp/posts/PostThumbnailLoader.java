package com.halloapp.posts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.halloapp.R;
import com.halloapp.contacts.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.Log;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.widget.TextDrawable;

import java.util.concurrent.Callable;

public class PostThumbnailLoader extends ViewDataLoader<ImageView, Drawable, String> {

    private final Context context;
    private final PostsDb postsDb;
    private final LruCache<String, Drawable> cache;

    private final int dimensionLimit;

    private final int textColor;
    private final int textSizeMax;
    private final int textSizeMin;
    private final int textPadding;

    @MainThread
    public PostThumbnailLoader(@NonNull Context context, int dimensionLimit) {

        this.dimensionLimit = dimensionLimit;
        this.context = context.getApplicationContext();
        postsDb = PostsDb.getInstance(context);

        textSizeMax = context.getResources().getDimensionPixelSize(R.dimen.text_post_thumbnail_max_text_size);
        textSizeMin = context.getResources().getDimensionPixelSize(R.dimen.text_post_thumbnail_min_text_size);
        textPadding = context.getResources().getDimensionPixelSize(R.dimen.text_post_thumbnail_padding);
        textColor = context.getResources().getColor(R.color.text_post_thumbnail_color);

        // Use 1/8th of the available memory for memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        Log.i("PostThumbnailLoader: create " + cacheSize + "KB cache for post images");
        cache = new LruCache<String, Drawable>(cacheSize) {

            @Override
            protected int sizeOf(@NonNull String key, @NonNull Drawable drawable) {
                // The cache size will be measured in kilobytes rather than number of items
                if (drawable instanceof BitmapDrawable) {
                    return ((BitmapDrawable)drawable).getBitmap().getByteCount() / 1024;
                } else {
                    return 1;
                }
            }
        };
    }

    @MainThread
    public void load(@NonNull ImageView view, @NonNull UserId userId, @NonNull String postId) {
        final Callable<Drawable> loader = () -> {

            final Post post = postsDb.getPost(userId, postId);
            if (post == null) {
                return null;
            }
            if (post.media.isEmpty()) {
                return new TextDrawable(post.text, textSizeMax, textSizeMin, textPadding, textColor);
            } else {
                Media media = post.media.get(0);
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
                    return null;
                } else {
                    return new BitmapDrawable(context.getResources(), bitmap);
                }
            }
        };
        final ViewDataLoader.Displayer<ImageView, Drawable> displayer = new ViewDataLoader.Displayer<ImageView, Drawable>() {

            @Override
            public void showResult(@NonNull ImageView view, Drawable result) {
                view.setImageDrawable(result);
            }

            @Override
            public void showLoading(@NonNull ImageView view) {
                view.setImageDrawable(null);
            }
        };
        load(view, loader, displayer, userId.rawId() + "_" + postId, cache);
    }

}
