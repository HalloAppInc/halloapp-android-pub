package com.halloapp.ui.avatar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.media.Downloader;
import com.halloapp.media.MediaStore;
import com.halloapp.posts.Media;
import com.halloapp.util.Log;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.PubSubItem;
import com.halloapp.xmpp.PublishedAvatarMetadata;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

public class AvatarLoader extends ViewDataLoader<ImageView, Bitmap, String> {

    private static AvatarLoader instance;

    private final Connection connection;
    private final Context context;
    private final LruCache<String, Bitmap> cache;

    private Bitmap defaultAvatar;

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
        this.context = context.getApplicationContext();

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
                PubSubItem item = connection.getMostRecentAvatarMetadata(userId).get();
                if (item == null) {
                    Log.i("No avatar metadata item for " + userId);
                    return getDefaultAvatar();
                }
                PublishedAvatarMetadata avatarMetadata = PublishedAvatarMetadata.getPublishedItem(item);
                if (avatarMetadata == null) {
                    Log.i("Empty avatar metadata item for " + userId);
                    return getDefaultAvatar();
                }

                String itemId = avatarMetadata.getId();
                byte[] hash = StringUtils.bytesFromHexString(itemId);

                // Do not permanently save avatars if we won't get updates
                if (!userId.isMe()) {
                    ContactsDb contactsDb = ContactsDb.getInstance(context);
                    Contact contact = contactsDb.getContact(userId);
                    if (contact == null || !contact.friend) {
                        avatarFile = mediaStore.getTmpFile(userId.rawId());
                    }
                }

                String url = avatarMetadata.getUrl();
                if (url != null) {
                    Downloader.run(url, null, hash, Media.MEDIA_TYPE_UNKNOWN, avatarFile, p -> true);
                } else {
                    return getDefaultAvatar();
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

    private Bitmap getDefaultAvatar() {
        if (defaultAvatar == null) {
            Drawable drawable = context.getDrawable(R.drawable.avatar_person);
            defaultAvatar = drawableToBitmap(drawable);
        }
        return defaultAvatar;
    }

    // From https://stackoverflow.com/a/24389104/11817085
    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void removeMyAvatar() {
        Connection connection = Connection.getInstance();
        connection.publishAvatarMetadata(RandomId.create(), null, 0, 0, 0);

        MediaStore mediaStore = MediaStore.getInstance(context);
        File avatarFile = mediaStore.getAvatarFile(UserId.ME.rawId());
        if (avatarFile.exists()) {
            avatarFile.delete();
        }

        reportMyAvatarChanged();
    }

    public void reportMyAvatarChanged() {
        cache.remove(UserId.ME.rawId());
    }

    public void reportAvatarMetadataUpdate(@NonNull UserId userId, @NonNull String hash, @Nullable String url) {
        MediaStore mediaStore = MediaStore.getInstance(context);
        File avatarFile = mediaStore.getAvatarFile(userId.rawId());
        if (url != null) {
            try {
                Downloader.run(url, null, StringUtils.bytesFromHexString(hash), Media.MEDIA_TYPE_UNKNOWN, avatarFile, p -> true);
            } catch (IOException e) {
                Log.w("avatar metadata update", e);
            }
        } else if (avatarFile.exists()) {
            avatarFile.delete();
        }
        cache.remove(userId.rawId());
    }
}
