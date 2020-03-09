package com.halloapp.ui.avatar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.halloapp.contacts.UserId;
import com.halloapp.media.Downloader;
import com.halloapp.media.MediaStore;
import com.halloapp.posts.Media;
import com.halloapp.util.Log;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.PublishedAvatarData;
import com.halloapp.xmpp.PublishedAvatarMetadata;
import com.halloapp.xmpp.PubsubItem;

import java.io.File;
import java.util.concurrent.Callable;

// String will have to be unique...
public class AvatarLoader extends ViewDataLoader<ImageView, Bitmap, String> {

    private static AvatarLoader instance;

    private final Connection connection;
    private final AvatarCache avatarCache;
    private final LruCache<String, Bitmap> cache;

    public static AvatarLoader getInstance(Connection connection) {
        if (instance == null) {
            synchronized (AvatarLoader.class) {
                if (instance == null) {
                    instance = new AvatarLoader(connection, AvatarCache.getInstance());
                }
            }
        }
        return instance;
    }

    private AvatarLoader(Connection connection, AvatarCache avatarCache) {
        this.connection = connection;
        this.avatarCache = avatarCache;

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
    public void load(@NonNull ImageView view, @NonNull UserId userId, @NonNull String uniqueId) {
        final Callable<Bitmap> loader = () -> {

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
                File localFile = MediaStore.getInstance(view.getContext()).getMediaFile(RandomId.create() + ".png");
                Downloader.run(url, null, hash, Media.MEDIA_TYPE_UNKNOWN, localFile, p -> true);
                return BitmapFactory.decodeFile(localFile.getAbsolutePath());
            }
            return null;
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
                //view.setImageDrawable(null);
            }
        };
        load(view, loader, displayer, userId.rawId() + "_" + uniqueId, cache);
    }
}
