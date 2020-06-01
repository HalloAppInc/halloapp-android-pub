package com.halloapp.ui.avatar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.collection.LruCache;

import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.content.Media;
import com.halloapp.media.Downloader;
import com.halloapp.util.Log;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.xmpp.Connection;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class AvatarLoader extends ViewDataLoader<ImageView, Bitmap, String> {

    private static final long AVATAR_DATA_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000; // a week

    private static AvatarLoader instance;

    private final Connection connection;
    private final Context context;
    private final LruCache<String, Bitmap> cache;

    private Bitmap defaultAvatar;

    public static AvatarLoader getInstance(@NonNull Connection connection, @NonNull Context context) {
        if (instance == null) {
            synchronized (AvatarLoader.class) {
                if (instance == null) {
                    instance = new AvatarLoader(connection, context);
                }
            }
        }
        return instance;
    }

    private AvatarLoader(@NonNull Connection connection, @NonNull Context context) {
        this.connection = connection;
        this.context = context.getApplicationContext();

        // Use 1/8th of the available memory for memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        Log.i("AvatarLoader: create " + cacheSize + "KB cache for post images");
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
        final Callable<Bitmap> loader = () -> getAvatarImpl(userId);
        final Displayer<ImageView, Bitmap> displayer = new Displayer<ImageView, Bitmap>() {

            @Override
            public void showResult(@NonNull ImageView view, Bitmap result) {
                if (result != null) {
                    view.setImageBitmap(result);
                }
            }

            @Override
            public void showLoading(@NonNull ImageView view) {
                view.setImageResource(R.drawable.avatar_person);
            }
        };
        load(view, loader, displayer, userId.rawId(), cache);
    }

    @WorkerThread
    public Bitmap getAvatar(@NonNull UserId userId) {
        Bitmap avatar = cache.get(userId.rawId());
        if (avatar != null) {
            return avatar;
        }
        avatar = getAvatarImpl(userId);
        if (avatar != null) {
            cache.put(userId.rawId(), avatar);
        }
        return avatar;
    }

    @WorkerThread
    private Bitmap getAvatarImpl(@NonNull UserId userId) {
        FileStore fileStore = FileStore.getInstance(context);
        File avatarFile = fileStore.getAvatarFile(userId.rawId());

        ContactsDb contactsDb = ContactsDb.getInstance(context);
        ContactsDb.ContactAvatarInfo contactAvatarInfo = contactsDb.getContactAvatarInfo(userId);

        if (contactAvatarInfo == null) {
            Log.i("AvatarLoader: Making new contact avatar info for user " + userId);
            contactAvatarInfo = new ContactsDb.ContactAvatarInfo(userId, 0, null);
        }

        long currentTimeMs = System.currentTimeMillis();
        if (currentTimeMs - contactAvatarInfo.avatarCheckTimestamp > AVATAR_DATA_EXPIRATION_MS) {
            try {
                String avatarId = null;
                Contact contact = contactsDb.getContact(userId);
                if (userId.isMe()) {
                    avatarId = contactAvatarInfo.avatarId;
                } else if (contact.friend) {
                    avatarId = contact.avatarId;
                }

                if (TextUtils.isEmpty(avatarId)) {
                    Log.i("AvatarLoader: no avatar id " + avatarId);
                    return getDefaultAvatar();
                }

                if (!avatarFile.exists() || !avatarId.equals(contactAvatarInfo.avatarId)) {
                    String url = "https://avatar-cdn.halloapp.net/" + avatarId;
                    Downloader.run(url, null, null, Media.MEDIA_TYPE_UNKNOWN, avatarFile, p -> true);
                    contactAvatarInfo.avatarId = contact.avatarId;
                }
            } catch (IOException e) {
                Log.w("Failed getting avatar", e);
            } finally {
                contactAvatarInfo.avatarCheckTimestamp = System.currentTimeMillis();
                contactsDb.updateContactAvatarInfo(contactAvatarInfo);
            }
        }

        if (!avatarFile.exists()) {
            return getDefaultAvatar();
        }
        return BitmapFactory.decodeFile(avatarFile.getAbsolutePath());
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

        final Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void removeMyAvatar() {
        reportMyAvatarChanged(null);
    }

    void reportMyAvatarChanged(String avatarId) {
        ContactsDb contactsDb = ContactsDb.getInstance(context);
        ContactsDb.ContactAvatarInfo contactAvatarInfo = contactsDb.getContactAvatarInfo(UserId.ME);
        contactAvatarInfo.avatarId = avatarId;
        contactAvatarInfo.avatarCheckTimestamp = 0;
        contactsDb.updateContactAvatarInfo(contactAvatarInfo);

        FileStore fileStore = FileStore.getInstance(context);
        File avatarFile = fileStore.getAvatarFile(UserId.ME.rawId());
        if (avatarFile.exists()) {
            if (!avatarFile.delete()) {
                Log.e("failed to remove avatar " + avatarFile.getAbsolutePath());
            }
        }
        cache.remove(UserId.ME.rawId());
    }

    public void reportAvatarUpdate(@NonNull UserId userId, @NonNull String avatarId) {
        FileStore fileStore = FileStore.getInstance(context);
        File avatarFile = fileStore.getAvatarFile(userId.rawId());
        if (avatarFile.exists()) {
            if (!avatarFile.delete()) {
                Log.e("failed to remove avatar " + avatarFile.getAbsolutePath());
            }
        }

        ContactsDb.getInstance(context).updateAvatarId(userId, avatarId);
        cache.remove(userId.rawId());
        try {
            ContactsDb.getInstance(context).updateContactAvatarInfo(new ContactsDb.ContactAvatarInfo(userId, 0, avatarId)).get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e("failed to update avatar", e);
        }
    }
}
