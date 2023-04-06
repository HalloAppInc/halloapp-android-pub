package com.halloapp.katchup.avatar;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.halloapp.FileStore;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Media;
import com.halloapp.id.UserId;
import com.halloapp.katchup.ui.Colors;
import com.halloapp.media.ChunkedMediaParametersException;
import com.halloapp.media.Downloader;
import com.halloapp.media.ForeignRemoteAuthorityException;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class KAvatarLoader extends KBaseAvatarLoader {

    private static KAvatarLoader instance;

    private final FileStore fileStore;
    private final Connection connection;
    private final ContactsDb contactsDb;

    private boolean isDarkMode;

    public static KAvatarLoader getInstance() {
        if (instance == null) {
            synchronized (KAvatarLoader.class) {
                if (instance == null) {
                    instance = new KAvatarLoader(FileStore.getInstance(), Connection.getInstance(), ContactsDb.getInstance());
                }
            }
        }
        return instance;
    }

    private KAvatarLoader(@NonNull FileStore fileStore, @NonNull Connection connection, @NonNull ContactsDb contactsDb) {
        this.fileStore = fileStore;
        this.connection = connection;
        this.contactsDb = contactsDb;
    }

    @MainThread
    public void load(@NonNull ImageView view, @NonNull UserId userId) {
        final Callable<Drawable> loader = () -> {
            Bitmap avatar = getAvatarImpl(userId);
            return avatar != null ? new BitmapDrawable(view.getResources(), avatar) : getDefaultAvatar(view.getContext(), userId);
        };
        final Displayer<ImageView, Drawable> displayer = new Displayer<ImageView, Drawable>() {

            @Override
            public void showResult(@NonNull ImageView view, Drawable result) {
                if (result != null) {
                    view.setImageDrawable(result);
                }
            }

            @Override
            public void showLoading(@NonNull ImageView view) {
            }
        };

        setDefaultBackgroundColor(view, userId);
        load(view, loader, displayer, userId.rawId(), cache);
    }

    @MainThread
    public void load(@NonNull ImageView view, @NonNull UserId userId, String avatarId) {
        final Callable<Drawable> loader = () -> {
            Bitmap avatar = getAvatarImpl(userId, avatarId);
            return avatar != null ? new BitmapDrawable(view.getResources(), avatar) : getDefaultAvatar(view.getContext(), userId);
        };
        final Displayer<ImageView, Drawable> displayer = new Displayer<ImageView, Drawable>() {

            @Override
            public void showResult(@NonNull ImageView view, Drawable result) {
                if (result != null) {
                    view.setImageDrawable(result);
                }
            }

            @Override
            public void showLoading(@NonNull ImageView view) {
            }
        };

        setDefaultBackgroundColor(view, userId);
        load(view, loader, displayer, userId.rawId(), cache);
    }

    @MainThread
    public void loadLarge(@NonNull ImageView view, @NonNull UserId userId, String avatarId) {
        final Callable<Drawable> loader = () -> {
            Bitmap avatar = getAvatarImpl(userId, avatarId, true);
            if (avatar != null) {
                return new BitmapDrawable(view.getResources(), avatar);
            }

            Drawable small = cache.get(userId.rawId());
            if (small != null) {
                return small;
            }

            return getDefaultAvatar(view.getContext(), userId);
        };
        final Displayer<ImageView, Drawable> displayer = new Displayer<ImageView, Drawable>() {

            @Override
            public void showResult(@NonNull ImageView view, Drawable result) {
                if (result != null) {
                    view.setImageDrawable(result);
                }
            }

            @Override
            public void showLoading(@NonNull ImageView view) {
                Drawable small = cache.get(userId.rawId());
                if (small != null) {
                    view.setImageDrawable(small);
                }
            }
        };

        setDefaultBackgroundColor(view, userId);
        load(view, loader, displayer, userId.rawId(), null);
    }

    @MainThread
    public void loadDefaultAvatar(@NonNull ImageView view, @NonNull UserId userId) {
        setDefaultBackgroundColor(view, userId);
        executor.submit(() -> {
            Drawable defaultAvatar = getDefaultAvatar(view.getContext(), userId);
            view.post(() -> view.setImageDrawable(defaultAvatar));
        });
    }

    private void setDefaultBackgroundColor(@NonNull ImageView view, @NonNull UserId userId) {
        if (!userId.isMe()) {
            int colorIndex = (int) (Long.parseLong(userId.rawId()));
            int color = ContextCompat.getColor(view.getContext(), Colors.getAvatarBgColor(colorIndex));
            view.setImageDrawable(new ColorDrawable(color));
        }
    }

    @WorkerThread
    public boolean hasAvatar() {
        return hasAvatar(UserId.ME);
    }

    @WorkerThread
    public boolean hasAvatar(UserId userId) {
        return cache.get(userId.rawId()) != null || getAvatarImpl(userId) != null;
    }

    @WorkerThread
    private Bitmap getAvatarImpl(@NonNull UserId userId) {
        return getAvatarImpl(userId, null);
    }

    @WorkerThread
    private Bitmap getAvatarImpl(@NonNull UserId userId, @Nullable String knownAvatarId) {
        return getAvatarImpl(userId, knownAvatarId, false);
    }

    @WorkerThread
    private Bitmap getAvatarImpl(@NonNull UserId userId, @Nullable String knownAvatarId, boolean large) {
        File avatarFile = fileStore.getAvatarFile(userId.rawId(), large);
        ContactsDb.ContactAvatarInfo contactAvatarInfo = getContactAvatarInfo(userId);

        String avatarId = knownAvatarId;
        if (avatarId == null) {
            avatarId = getAvatarId(userId, contactAvatarInfo);
        }

        try {
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
    private String getAvatarId(@NonNull UserId userId, @NonNull ContactsDb.ContactAvatarInfo contactAvatarInfo) {
        String avatarId;

        if (userId.isMe()) {
            avatarId = contactAvatarInfo.avatarId;

            if (avatarId == null) {
                try {
                    avatarId = connection.getMyAvatarId().await();
                } catch (InterruptedException | ObservableErrorException e) {
                    Log.w("AvatarLoader: Failed getting avatar for " + userId + "; resetting values", e);
                    contactAvatarInfo.avatarCheckTimestamp = 0;
                    contactAvatarInfo.avatarId = null;
                    return null;
                }
                contactAvatarInfo.avatarCheckTimestamp = System.currentTimeMillis();
            }
        } else {
            avatarId = contactAvatarInfo.avatarId;
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

    private @NonNull ContactsDb.ContactAvatarInfo getContactAvatarInfo(UserId userId) {
        ContactsDb.ContactAvatarInfo contactAvatarInfo = contactsDb.getContactAvatarInfo(userId);

        if (contactAvatarInfo == null) {
            Log.i("AvatarLoader: Making new contact avatar info for chat " + userId);
            contactAvatarInfo = new ContactsDb.ContactAvatarInfo(userId, 0, null, null, null);
        }

        return contactAvatarInfo;
    }

    @WorkerThread
    @NonNull
    private Drawable getDefaultAvatar(@NonNull Context context, @NonNull UserId userId) {
        int localMode = AppCompatDelegate.getDefaultNightMode();
        boolean darkMode = localMode == AppCompatDelegate.MODE_NIGHT_YES ||
                ((localMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) && (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES);
        if (darkMode != isDarkMode) {
            isDarkMode = darkMode;
        }

        Contact contact = contactsDb.getContact(userId);
        return getDefaultAvatar(context, contact);
    }

    @WorkerThread
    public void removeMyAvatar() {
        reportMyAvatarChanged(null);
    }

    @WorkerThread
    public void removeAvatar(@NonNull UserId userId) {
        reportAvatarUpdate(userId, null);
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
        File largeFile = fileStore.getAvatarFile(UserId.ME.rawId(), true);
        if (largeFile.exists()) {
            if (!largeFile.delete()) {
                Log.e("failed to remove avatar " + largeFile.getAbsolutePath());
            }
        }
        cache.remove(UserId.ME.rawId());
    }

    public void reportAvatarUpdate(@NonNull UserId userId, @Nullable String avatarId) {
        File smallFile = fileStore.getAvatarFile(userId.rawId());
        if (smallFile.exists()) {
            if (!smallFile.delete()) {
                Log.e("failed to remove avatar " + smallFile.getAbsolutePath());
            }
        }
        File largeFile = fileStore.getAvatarFile(userId.rawId(), true);
        if (largeFile.exists()) {
            if (!largeFile.delete()) {
                Log.e("failed to remove avatar " + largeFile.getAbsolutePath());
            }
        }

        ContactsDb.getInstance().updateAvatarId(userId, avatarId);
        cache.remove(userId.rawId());
        try {
            ContactsDb.getInstance().updateContactAvatarInfo(new ContactsDb.ContactAvatarInfo(userId, 0, avatarId, null, null)).get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e("failed to update avatar", e);
        }
    }
}
