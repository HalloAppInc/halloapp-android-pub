package com.halloapp.ui.avatar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.halloapp.contacts.UserId;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.PublishedAvatarData;
import com.halloapp.xmpp.PublishedAvatarMetadata;
import com.halloapp.xmpp.PubsubItem;

import java.util.concurrent.ExecutionException;

public class AvatarLoader {

    private static AvatarLoader instance;

    private final Connection connection;
    private final AvatarCache avatarCache;

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
    }

    private Bitmap getAvatarFor(UserId userId) {
        try {
            PubsubItem item = connection.getMostRecentAvatarMetadata(userId).get();
            if (item == null) {
                // TODO(jack): return the placeholder instead
                Log.i("No avatar metadata for " + userId);
                return null;
            }
            PublishedAvatarMetadata avatarMetadata = PublishedAvatarMetadata.getPublishedItem(item);
            String itemId = avatarMetadata.getId(); // this is hash

            Bitmap cached = avatarCache.getAvatarFor(itemId);
            if (cached != null) {
                return cached;
            }

            PubsubItem avatarData = connection.getAvatarData(userId, itemId).get();
            PublishedAvatarData data = PublishedAvatarData.getPublishedItem(avatarData);
            byte[] bytes = Base64.decode(data.getBase64Data(), Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (ExecutionException | InterruptedException e) {
            Log.e("AvatarLoader", e);
        }

        return null;
    }

    public void loadAvatarFor(UserId userId, @NonNull LifecycleOwner owner, @NonNull Observer<? super Bitmap> observer) {
        LoadAvatarTask loadAvatarTask = new LoadAvatarTask(userId, this);
        loadAvatarTask.avatarBitmap.observe(owner, observer);
        // TODO(jack): Probably ought to have a separate thread pool
        loadAvatarTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static class LoadAvatarTask extends AsyncTask<Void, Void, Void> {

        final MutableLiveData<Bitmap> avatarBitmap = new MutableLiveData<>();
        final UserId userId;
        final AvatarLoader avatarLoader;

        LoadAvatarTask(UserId userId, AvatarLoader avatarLoader) {
            this.userId = userId;
            this.avatarLoader = avatarLoader;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Bitmap avatar = avatarLoader.getAvatarFor(userId);
            if (avatar != null) {
                avatarBitmap.postValue(avatar);
            }
            return null;
        }
    }

}
