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

import com.halloapp.AppContext;
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.Downloader;
import com.halloapp.ui.groups.ViewGroupFeedActivity;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.xmpp.Connection;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class AvatarLoader extends ViewDataLoader<ImageView, Bitmap, String> {

    private static final long AVATAR_DATA_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000; // a week

    private static AvatarLoader instance;

    private final Context context;
    private final Connection connection;
    private final ContactsDb contactsDb;
    private final LruCache<String, Bitmap> cache;

    private Bitmap defaultUserAvatar;
    private Bitmap defaultGroupAvatar;

    public static AvatarLoader getInstance() {
        if (instance == null) {
            synchronized (AvatarLoader.class) {
                if (instance == null) {
                    instance = new AvatarLoader(AppContext.getInstance().get(), Connection.getInstance(), ContactsDb.getInstance());
                }
            }
        }
        return instance;
    }

    private AvatarLoader(@NonNull Context context, @NonNull Connection connection, @NonNull ContactsDb contactsDb) {
        this.context = context.getApplicationContext();
        this.connection = connection;
        this.contactsDb = contactsDb;

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
    public void load(@NonNull ImageView view, @NonNull ChatId chatId) {
        load(view, chatId, true);
    }

    @MainThread
    public void load(@NonNull ImageView view, @NonNull ChatId chatId, boolean openProfileOnTap) {
        if (openProfileOnTap) {
            if (chatId instanceof UserId) {
                UserId userId = (UserId) chatId;
                if (userId.isMe()) {
                    view.setOnClickListener(null);
                } else {
                    view.setOnClickListener(v -> v.getContext().startActivity(ViewProfileActivity.viewProfile(v.getContext(), userId)));
                }
            } else if (chatId instanceof GroupId) {
                GroupId groupId = (GroupId) chatId;
                view.setOnClickListener(v -> v.getContext().startActivity(ViewGroupFeedActivity.viewFeed(v.getContext(), groupId)));
            }
        }
        final Callable<Bitmap> loader = () -> getAvatarImpl(chatId);
        final Displayer<ImageView, Bitmap> displayer = new Displayer<ImageView, Bitmap>() {

            @Override
            public void showResult(@NonNull ImageView view, Bitmap result) {
                view.setImageBitmap(result != null ? result : getDefaultAvatar(chatId));
            }

            @Override
            public void showLoading(@NonNull ImageView view) {
                view.setImageResource(chatId instanceof GroupId ? R.drawable.avatar_group : R.drawable.avatar_person);
            }
        };
        load(view, loader, displayer, chatId.rawId(), cache);
    }

    @WorkerThread
    @NonNull public Bitmap getAvatar(@NonNull ChatId chatId) {
        Bitmap avatar = cache.get(chatId.rawId());
        if (avatar != null) {
            return avatar;
        }

        avatar = getAvatarImpl(chatId);
        if (avatar != null) {
            cache.put(chatId.rawId(), avatar);
        }

        return avatar != null ? avatar : getDefaultAvatar(chatId);
    }

    @WorkerThread
    private Bitmap getAvatarImpl(@NonNull ChatId chatId) {
        FileStore fileStore = FileStore.getInstance();
        File avatarFile = fileStore.getAvatarFile(chatId.rawId());

        ContactsDb.ContactAvatarInfo contactAvatarInfo = getContactAvatarInfo(chatId);

        long currentTimeMs = System.currentTimeMillis();
        if (currentTimeMs - contactAvatarInfo.avatarCheckTimestamp > AVATAR_DATA_EXPIRATION_MS) {
            try {
                String avatarId = null;

                if (chatId instanceof UserId) {
                    UserId userId = (UserId) chatId;

                    Contact contact = contactsDb.getContact(userId);
                    if (userId.isMe()) {
                        avatarId = contactAvatarInfo.avatarId;
                        if (avatarId == null) {
                            avatarId = connection.getMyAvatarId().get();
                            contactAvatarInfo.avatarCheckTimestamp = System.currentTimeMillis();
                        }
                    } else if (contact.friend) {
                        avatarId = contact.avatarId;
                    }

                    if (TextUtils.isEmpty(avatarId)) {
                        Log.i("AvatarLoader: no avatar id " + avatarId);
                        return null;
                    }

                    if (!avatarFile.exists() || !Preconditions.checkNotNull(avatarId).equals(contactAvatarInfo.avatarId)) {
                        String url = "https://avatar-cdn.halloapp.net/" + avatarId;
                        Downloader.run(url, null, null, Media.MEDIA_TYPE_UNKNOWN, null, avatarFile, p -> true);
                        contactAvatarInfo.avatarId = contact.avatarId;
                    }
                } else {
                    Chat chat = Preconditions.checkNotNull(ContentDb.getInstance().getChat(chatId));
                    avatarId = chat.groupAvatarId;

                    if (TextUtils.isEmpty(avatarId)) {
                        Log.i("AvatarLoader: no group avatar id " + avatarId);
                        return null;
                    }

                    if (!avatarFile.exists() || !avatarId.equals(contactAvatarInfo.avatarId)) {
                        String url = "https://avatar-cdn.halloapp.net/" + avatarId;
                        Downloader.run(url, null, null, Media.MEDIA_TYPE_UNKNOWN, null, avatarFile, p -> true);
                        contactAvatarInfo.avatarId = avatarId;
                    }
                }

                contactAvatarInfo.avatarCheckTimestamp = System.currentTimeMillis();
            } catch (InterruptedException | ExecutionException | IOException e) {
                Log.w("AvatarLoader: Failed getting avatar; resetting values", e);
                contactAvatarInfo.avatarCheckTimestamp = 0;
                contactAvatarInfo.avatarId = null;
                return null;
            } finally {
                contactsDb.updateContactAvatarInfo(contactAvatarInfo);
            }
        }

        if (!avatarFile.exists()) {
            return null;
        }
        return BitmapFactory.decodeFile(avatarFile.getAbsolutePath());
    }

    private @NonNull ContactsDb.ContactAvatarInfo getContactAvatarInfo(ChatId chatId) {
        ContactsDb.ContactAvatarInfo contactAvatarInfo = contactsDb.getContactAvatarInfo(chatId);

        if (contactAvatarInfo == null) {
            Log.i("AvatarLoader: Making new contact avatar info for chat " + chatId);
            contactAvatarInfo = new ContactsDb.ContactAvatarInfo(chatId, 0, null);
        }

        return contactAvatarInfo;
    }

    @NonNull private Bitmap getDefaultAvatar(@NonNull ChatId chatId) {
        return chatId instanceof GroupId ? getDefaultGroupAvatar() : getDefaultUserAvatar();
    }

    @NonNull private Bitmap getDefaultUserAvatar() {
        if (defaultUserAvatar == null) {
            Drawable drawable = context.getDrawable(R.drawable.avatar_person);
            defaultUserAvatar = drawableToBitmap(drawable);
        }
        return defaultUserAvatar;
    }

    @NonNull private Bitmap getDefaultGroupAvatar() {
        if (defaultGroupAvatar == null) {
            Drawable drawable = context.getDrawable(R.drawable.avatar_group);
            defaultGroupAvatar = drawableToBitmap(drawable);
        }
        return defaultGroupAvatar;
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

    @WorkerThread
    public void removeMyAvatar() {
        reportMyAvatarChanged(null);
    }

    @WorkerThread
    public void reportMyAvatarChanged(String avatarId) {
        ContactsDb.ContactAvatarInfo contactAvatarInfo = getContactAvatarInfo(UserId.ME);
        contactAvatarInfo.avatarId = avatarId;
        contactAvatarInfo.avatarCheckTimestamp = 0;
        contactsDb.updateContactAvatarInfo(contactAvatarInfo);

        FileStore fileStore = FileStore.getInstance();
        File avatarFile = fileStore.getAvatarFile(UserId.ME.rawId());
        if (avatarFile.exists()) {
            if (!avatarFile.delete()) {
                Log.e("failed to remove avatar " + avatarFile.getAbsolutePath());
            }
        }
        cache.remove(UserId.ME.rawId());
    }

    public void reportAvatarUpdate(@NonNull ChatId chatId, @NonNull String avatarId) {
        FileStore fileStore = FileStore.getInstance();
        File avatarFile = fileStore.getAvatarFile(chatId.rawId());
        if (avatarFile.exists()) {
            if (!avatarFile.delete()) {
                Log.e("failed to remove avatar " + avatarFile.getAbsolutePath());
            }
        }

        ContactsDb.getInstance().updateAvatarId(chatId, avatarId);
        cache.remove(chatId.rawId());
        try {
            ContactsDb.getInstance().updateContactAvatarInfo(new ContactsDb.ContactAvatarInfo(chatId, 0, avatarId)).get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e("failed to update avatar", e);
        }
    }
}
