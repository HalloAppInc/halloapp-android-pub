package com.halloapp.content;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.ui.markdown.MarkdownUtils;
import com.halloapp.ui.mentions.MentionsLoader;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.PlaceholderDrawable;
import com.halloapp.widget.TextDrawable;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

public class PostThumbnailLoader extends ViewDataLoader<ImageView, Drawable, String> {

    private final Context context;
    private final Me me;
    private final ContentDb contentDb;
    private final ContactsDb contactsDb;
    private final LruCache<String, Drawable> cache;

    private final int dimensionLimit;

    private final int textColor;
    private final int textSizeMax;
    private final int textSizeMin;
    private final int textPadding;
    private final int placeholderColor;

    @MainThread
    public PostThumbnailLoader(@NonNull Context context, int dimensionLimit) {

        this.dimensionLimit = dimensionLimit;
        this.context = context.getApplicationContext();
        me = Me.getInstance();
        contentDb = ContentDb.getInstance();
        contactsDb = ContactsDb.getInstance();

        textSizeMax = context.getResources().getDimensionPixelSize(R.dimen.text_post_thumbnail_max_text_size);
        textSizeMin = context.getResources().getDimensionPixelSize(R.dimen.text_post_thumbnail_min_text_size);
        textPadding = context.getResources().getDimensionPixelSize(R.dimen.text_post_thumbnail_padding);
        textColor = context.getResources().getColor(R.color.text_post_thumbnail_color);
        placeholderColor = context.getResources().getColor(R.color.media_placeholder);

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

            final Post post = contentDb.getPost(postId);
            if (post == null) {
                return null;
            }
            if (post.media.isEmpty()) {
                CharSequence text = post.text;
                if (!post.mentions.isEmpty()) {
                    List<Mention> ret = MentionsLoader.loadMentionNames(me, contactsDb, post.mentions);
                    text = MarkdownUtils.formatMarkdownWithMentions(view.getContext(), post.text, ret);
                }
                return new TextDrawable(text, textSizeMax, textSizeMin, textPadding, textColor);
            } else {
                Drawable drawable = null;
                for (Media media : post.media) {
                    drawable = getDrawable(media);
                    if (drawable != null) {
                        return drawable;
                    }
                }
                return drawable;
            }
        };
        final ViewDataLoader.Displayer<ImageView, Drawable> displayer = new ViewDataLoader.Displayer<ImageView, Drawable>() {

            @Override
            public void showResult(@NonNull ImageView view, Drawable result) {
                view.setImageDrawable(result);
                if (result == null) {
                    view.setVisibility(View.GONE);
                } else {
                    view.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void showLoading(@NonNull ImageView view) {
                view.setImageDrawable(null);
            }
        };
        load(view, loader, displayer, userId.rawId() + "_" + postId, cache);
    }

    @Nullable
    private Drawable getDrawable(Media media) throws IOException {
        switch (media.type) {
            case Media.MEDIA_TYPE_IMAGE:
            case Media.MEDIA_TYPE_VIDEO:
                Bitmap bitmap = null;
                if (media.file != null) {
                    if (media.file.exists()) {
                        bitmap = MediaUtils.decode(media.file, media.type, dimensionLimit);
                    } else {
                        Log.i("MediaThumbnailLoader.load: file " + media.file.getAbsolutePath() + " doesn't exist");
                    }
                }
                if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
                    Log.i("MediaThumbnailLoader.load: cannot decode " + media.file);
                    return new PlaceholderDrawable(media.width, media.height, placeholderColor);
                } else {
                    return new BitmapDrawable(context.getResources(), bitmap);
                }
        }
        return null;
    }
}
