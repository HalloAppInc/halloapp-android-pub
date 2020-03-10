package com.halloapp.ui.avatar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.halloapp.contacts.UserId;
import com.halloapp.media.Downloader;
import com.halloapp.media.MediaStore;
import com.halloapp.posts.Media;
import com.halloapp.util.Log;
import com.halloapp.util.StringUtils;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.PublishedAvatarMetadata;
import com.halloapp.xmpp.PubsubItem;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

public class AvatarLoader extends ViewDataLoader<ImageView, Bitmap, String> {

    private static AvatarLoader instance;

    private final Connection connection;
    private final Context context;
    private final LruCache<String, Bitmap> cache;

    public static AvatarLoader getInstance(Connection connection, Context context) {
        if (instance == null) {
            synchronized (AvatarLoader.class) {
                if (instance == null) {
                    instance = new AvatarLoader(connection, context);
                }
            }
        }
        return instance;
    }

    private AvatarLoader(Connection connection, Context context) {
        this.connection = connection;
        this.context = context;

        // Use 1/8th of the available memory for memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        Log.i("PostThumbnailLoader: create " + cacheSize + "KB cache for post images");
        cache = new LruCache<String, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(@NonNull String key, @NonNull Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than number of items
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @MainThread
    public void load(@NonNull ImageView view, @NonNull UserId userId) {
        final Callable<Bitmap> loader = () -> {
            MediaStore mediaStore = MediaStore.getInstance(context);
            File avatarFile = mediaStore.getAvatarFile(userId.rawId());

            if (!avatarFile.exists()) {
                PubsubItem item = connection.getMostRecentAvatarMetadata(userId).get();
                if (item == null) {
                    Log.i("No avatar metadata for " + userId);
                    return null;
                }
                PublishedAvatarMetadata avatarMetadata = PublishedAvatarMetadata.getPublishedItem(item);
                String itemId = avatarMetadata.getId();
                byte[] hash = StringUtils.bytesFromHexString(itemId);

                String url = avatarMetadata.getUrl();
                if (url != null) {
                    Downloader.run(url, null, hash, Media.MEDIA_TYPE_UNKNOWN, avatarFile, p -> true);
                }
            }

            return BitmapFactory.decodeFile(avatarFile.getAbsolutePath());
        };
        final Displayer<ImageView, Bitmap> displayer = new Displayer<ImageView, Bitmap>() {

            @Override
            public void showResult(@NonNull ImageView view, Bitmap result) {
                if (result != null) {
                    view.setImageBitmap(result);
                }
            }

            @Override
            public void showLoading(@NonNull ImageView view) {
            }
        };
        load(view, loader, displayer, userId.rawId(), cache);
    }

    public void reportAvatarMetadataUpdate(@NonNull UserId userId, @NonNull String hash, @NonNull String url) {
        MediaStore mediaStore = MediaStore.getInstance(context);
        File avatarFile = mediaStore.getAvatarFile(userId.rawId());
        try {
            Downloader.run(url, null, StringUtils.bytesFromHexString(hash), Media.MEDIA_TYPE_UNKNOWN, avatarFile, p -> true);
            cache.remove(userId.rawId());
        } catch (IOException e) {
            Log.w("avatar metadata update", e);
        }
    }
}
