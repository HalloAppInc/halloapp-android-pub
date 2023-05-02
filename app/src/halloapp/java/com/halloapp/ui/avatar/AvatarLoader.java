package com.halloapp.ui.avatar;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.collection.LruCache;
import androidx.core.content.ContextCompat;

import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.content.Media;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.ChunkedMediaParametersException;
import com.halloapp.media.Downloader;
import com.halloapp.media.ForeignRemoteAuthorityException;
import com.halloapp.ui.groups.ViewGroupFeedActivity;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.DrawableUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class AvatarLoader extends ViewDataLoader<ImageView, Bitmap, String> {

    private static AvatarLoader instance;

    private final FileStore fileStore;
    private final Connection connection;
    private final ContactsDb contactsDb;
    private final LruCache<String, Bitmap> cache;

    private Drawable defaultUserAvatar;
    private Drawable defaultGroupAvatar;

    private boolean isDarkMode;

    public static AvatarLoader getInstance() {
        if (instance == null) {
            synchronized (AvatarLoader.class) {
                if (instance == null) {
                    instance = new AvatarLoader(FileStore.getInstance(), Connection.getInstance(), ContactsDb.getInstance());
                }
            }
        }
        return instance;
    }

    private AvatarLoader(@NonNull FileStore fileStore, @NonNull Connection connection, @NonNull ContactsDb contactsDb) {
        this.fileStore = fileStore;
        this.connection = connection;
        this.contactsDb = contactsDb;

        // Use 1/8th of the available memory for memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        Log.i("AvatarLoader: create " + cacheSize + "KB cache for avatars");
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
                view.setOnClickListener(v -> v.getContext().startActivity(ViewProfileActivity.viewProfile(v.getContext(), userId)));
            } else if (chatId instanceof GroupId) {
                GroupId groupId = (GroupId) chatId;
                view.setOnClickListener(v -> v.getContext().startActivity(ViewGroupFeedActivity.viewFeed(v.getContext(), groupId)));
            } else {
                view.setOnClickListener(null);
            }
        }
        final Callable<Bitmap> loader = () -> getAvatarImpl(chatId);
        final Displayer<ImageView, Bitmap> displayer = new Displayer<ImageView, Bitmap>() {

            @Override
            public void showResult(@NonNull ImageView view, Bitmap result) {
                if (result == null) {
                    view.setImageDrawable(getDefaultAvatar(view.getContext(), chatId));
                } else {
                    view.setImageBitmap(result);
                }
            }

            @Override
            public void showLoading(@NonNull ImageView view) {
                view.setImageDrawable(getDefaultAvatar(view.getContext(), chatId));
            }
        };
        load(view, loader, displayer, chatId.rawId(), cache);
    }

    @MainThread
    public void load(@NonNull ImageView view, @NonNull ChatId chatId, String avatarId) {
        final Callable<Bitmap> loader = () -> getAvatarImpl(chatId, avatarId);
        final Displayer<ImageView, Bitmap> displayer = new Displayer<ImageView, Bitmap>() {

            @Override
            public void showResult(@NonNull ImageView view, Bitmap result) {
                if (result == null) {
                    view.setImageDrawable(getDefaultAvatar(view.getContext(), chatId));
                } else {
                    view.setImageBitmap(result);
                }
            }

            @Override
            public void showLoading(@NonNull ImageView view) {
                view.setImageDrawable(getDefaultAvatar(view.getContext(), chatId));
            }
        };
        load(view, loader, displayer, chatId.rawId(), cache);
    }

    @MainThread
    public void loadLarge(@NonNull ImageView view, @NonNull ChatId chatId, String avatarId) {
        final Callable<Bitmap> loader = () -> getAvatarImpl(chatId, avatarId, true);
        final Displayer<ImageView, Bitmap> displayer = new Displayer<ImageView, Bitmap>() {

            @Override
            public void showResult(@NonNull ImageView view, Bitmap result) {
                if (result == null) {
                    Bitmap small = cache.get(chatId.rawId());
                    if (small != null) {
                        view.setImageBitmap(small);
                    } else {
                        view.setImageDrawable(getDefaultAvatar(view.getContext(), chatId));
                    }
                } else {
                    view.setImageBitmap(result);
                }
            }

            @Override
            public void showLoading(@NonNull ImageView view) {
                Bitmap small = cache.get(chatId.rawId());
                if (small != null) {
                    view.setImageBitmap(small);
                } else {
                    view.setImageDrawable(getDefaultAvatar(view.getContext(), chatId));
                }
            }
        };
        load(view, loader, displayer, chatId.rawId(), null);
    }

    @WorkerThread
    public boolean hasAvatar() {
        return hasAvatar(UserId.ME);
    }

    @WorkerThread
    public boolean hasAvatar(ChatId chatId) {
        return cache.get(chatId.rawId()) != null || getAvatarImpl(chatId) != null;
    }

    @WorkerThread
    @NonNull public Bitmap getAvatar(@NonNull Context context, @NonNull ChatId chatId) {
        Bitmap avatar = cache.get(chatId.rawId());
        if (avatar != null) {
            return avatar;
        }

        avatar = getAvatarImpl(chatId);
        if (avatar != null) {
            cache.put(chatId.rawId(), avatar);
        }

        return avatar != null ? avatar : DrawableUtils.drawableToBitmap(getDefaultAvatar(context, chatId));
    }

    @WorkerThread
    private Bitmap getAvatarImpl(@NonNull ChatId chatId) {
        return getAvatarImpl(chatId, null);
    }

    @WorkerThread
    private Bitmap getAvatarImpl(@NonNull ChatId chatId, @Nullable String knownAvatarId) {
        return getAvatarImpl(chatId, knownAvatarId, false);
    }

    @WorkerThread
    private Bitmap getAvatarImpl(@NonNull ChatId chatId, @Nullable String knownAvatarId, boolean large) {
        File avatarFile = fileStore.getAvatarFile(chatId.rawId(), large);
        ContactsDb.ContactAvatarInfo contactAvatarInfo = getContactAvatarInfo(chatId);

         try {
            String avatarId = getAvatarId(knownAvatarId, chatId, contactAvatarInfo);
            if (!downloadAvatar(avatarId, avatarFile, contactAvatarInfo, large)) {
                return null;
            }
        } finally {
            contactsDb.updateContactAvatarInfo(contactAvatarInfo);
        }

        if (!avatarFile.exists()) {
            return null;
        }
        return BitmapFactory.decodeFile(avatarFile.getAbsolutePath());
    }

    @WorkerThread
    private String getAvatarId(@Nullable String knownAvatarId, @NonNull ChatId chatId, @NonNull ContactsDb.ContactAvatarInfo contactAvatarInfo) {
        String avatarId = knownAvatarId;

        if (chatId instanceof UserId) {
            UserId userId = (UserId) chatId;

            Contact contact = contactsDb.getContact(userId);
            if (userId.isMe()) {
                avatarId = contactAvatarInfo.avatarId;
                if (avatarId == null) {
                    try {
                        avatarId = connection.getMyAvatarId().await();
                    } catch (InterruptedException | ObservableErrorException e) {
                        Log.w("AvatarLoader: Failed getting avatar for " + chatId + "; resetting values", e);
                        contactAvatarInfo.avatarCheckTimestamp = 0;
                        contactAvatarInfo.avatarId = null;
                        return null;
                    }
                    contactAvatarInfo.avatarCheckTimestamp = System.currentTimeMillis();
                }
            } else {
                avatarId = contact.avatarId;
            }
        } else if (chatId instanceof GroupId){
            Group group = ContentDb.getInstance().getGroupFeedOrChat((GroupId) chatId);
            if (group != null) {
                avatarId = group.groupAvatarId;
            } else {
                Log.i("AvatarLoader: group no chat");
            }
        }

        return avatarId;
    }

    @WorkerThread
    private boolean downloadAvatar(@Nullable String avatarId, @NonNull File avatarFile, @NonNull ContactsDb.ContactAvatarInfo contactAvatarInfo, boolean large) {
        if (TextUtils.isEmpty(avatarId)) {
            Log.i("AvatarLoader: no group avatar id " + avatarId);
            return false;
        }

        try {
            if (shouldDownloadAvatar(Preconditions.checkNotNull(avatarId), contactAvatarInfo, large)) {
                String url = "https://avatar-cdn.halloapp.net/" + avatarId + (large ? "-full" : "");
                Downloader.run(url, null, null, Media.MEDIA_TYPE_UNKNOWN, null, avatarFile, new Downloader.DownloadListener() {
                    @Override
                    public boolean onProgress(long bytesWritten) {
                        return true;
                    }
                }, "avatar-" + avatarId + (large ? "-full" : ""));
                contactAvatarInfo.avatarId = avatarId;
            }
            return true;
        } catch (Downloader.DownloadException e) {
            Log.i("AvatarLoader: avatar not found on server");
            return false;
        } catch (IOException | GeneralSecurityException | ChunkedMediaParametersException | ForeignRemoteAuthorityException e) {
            Log.w("AvatarLoader: Failed getting avatar " + avatarId + "; resetting values", e);
            if (large) {
                contactAvatarInfo.largeCurrentId = null;
            } else {
                contactAvatarInfo.regularCurrentId = null;
            }
            return false;
        }
    }

    private static boolean shouldDownloadAvatar(@NonNull String avatarId, ContactsDb.ContactAvatarInfo avatarInfo, boolean large) {
        boolean ret = !avatarId.equals(large ? avatarInfo.largeCurrentId : avatarInfo.regularCurrentId);
        if (large) {
            avatarInfo.largeCurrentId = avatarId;
        } else {
            avatarInfo.regularCurrentId = avatarId;
        }
        return ret;
    }

    private @NonNull ContactsDb.ContactAvatarInfo getContactAvatarInfo(ChatId chatId) {
        ContactsDb.ContactAvatarInfo contactAvatarInfo = contactsDb.getContactAvatarInfo(chatId);

        if (contactAvatarInfo == null) {
            Log.i("AvatarLoader: Making new contact avatar info for chat " + chatId);
            contactAvatarInfo = new ContactsDb.ContactAvatarInfo(chatId, 0, null, null, null);
        }

        return contactAvatarInfo;
    }

    @NonNull private Drawable getDefaultAvatar(@NonNull Context context, @NonNull ChatId chatId) {
        int localMode = AppCompatDelegate.getDefaultNightMode();
        boolean darkMode = localMode == AppCompatDelegate.MODE_NIGHT_YES ||
                ((localMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) && (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES);
        if (darkMode != isDarkMode) {
            isDarkMode = darkMode;
            defaultGroupAvatar = null;
            defaultUserAvatar = null;
        }
        return chatId instanceof GroupId ? getDefaultGroupAvatar(context) : getDefaultUserAvatar(context);
    }

    @NonNull private Drawable getDefaultUserAvatar(@NonNull Context context) {
        if (defaultUserAvatar == null) {
            defaultUserAvatar = ContextCompat.getDrawable(context, R.drawable.avatar_person);
        }
        return defaultUserAvatar;
    }

    @NonNull private Drawable getDefaultGroupAvatar(@NonNull Context context) {
        if (defaultGroupAvatar == null) {
            defaultGroupAvatar = ContextCompat.getDrawable(context, R.drawable.avatar_groups_placeholder);
        }
        return defaultGroupAvatar;
    }

    @WorkerThread
    public void removeMyAvatar() {
        reportMyAvatarChanged(null);
    }

    @WorkerThread
    public void removeAvatar(@NonNull ChatId chatId) {
        reportAvatarUpdate(chatId, null);
    }

    @WorkerThread
    public void reportMyAvatarChanged(String avatarId) {
        ContactsDb.ContactAvatarInfo contactAvatarInfo = getContactAvatarInfo(UserId.ME);
        contactAvatarInfo.avatarId = avatarId;
        contactAvatarInfo.avatarCheckTimestamp = 0;
        contactAvatarInfo.regularCurrentId = null;
        contactAvatarInfo.largeCurrentId = null;
        contactsDb.updateContactAvatarInfo(contactAvatarInfo);

        File smallFile = fileStore.getAvatarFile(UserId.ME.rawId());
        if (smallFile.exists()) {
            if (!smallFile.delete()) {
                Log.e("failed to remove avatar " + smallFile.getAbsolutePath());
            }
        }
        File largeFile = fileStore.getAvatarFile(UserId.ME.rawId());
        if (largeFile.exists()) {
            if (!largeFile.delete()) {
                Log.e("failed to remove avatar " + largeFile.getAbsolutePath());
            }
        }
        cache.remove(UserId.ME.rawId());
    }

    public void reportAvatarUpdate(@NonNull ChatId chatId, @Nullable String avatarId) {
        File smallFile = fileStore.getAvatarFile(chatId.rawId());
        if (smallFile.exists()) {
            if (!smallFile.delete()) {
                Log.e("failed to remove avatar " + smallFile.getAbsolutePath());
            }
        }
        File largeFile = fileStore.getAvatarFile(chatId.rawId());
        if (largeFile.exists()) {
            if (!largeFile.delete()) {
                Log.e("failed to remove avatar " + largeFile.getAbsolutePath());
            }
        }

        ContactsDb.getInstance().updateAvatarId(chatId, avatarId);
        cache.remove(chatId.rawId());
        try {
            ContactsDb.getInstance().updateContactAvatarInfo(new ContactsDb.ContactAvatarInfo(chatId, 0, avatarId, null, null)).get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e("failed to update avatar", e);
        }
    }
}
