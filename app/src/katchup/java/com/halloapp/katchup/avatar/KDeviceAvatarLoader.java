package com.halloapp.katchup.avatar;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.halloapp.contacts.Contact;
import com.halloapp.ui.avatar.DeviceAvatarLoader;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;

import java.util.concurrent.Callable;

public class KDeviceAvatarLoader extends ViewDataLoader<ImageView, Bitmap, String> {
    private final Context context;
    private final LruCache<String, Bitmap> cache;

    public KDeviceAvatarLoader(@NonNull Context context) {
        this.context = context;
        // Use 1/8th of the available memory for memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        Log.i("KDeviceAvatarLoader: create " + cacheSize + "KB cache for images");
        cache = new LruCache<String, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(@NonNull String key, @NonNull Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than number of items
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void load(@NonNull ImageView view, @NonNull Contact contact) {
        final Callable<Bitmap> loader = () -> DeviceAvatarLoader.getAddressBookPhoto(context, contact.normalizedPhone);

        final Displayer<ImageView, Bitmap> displayer = new Displayer<ImageView, Bitmap>() {
            @Override
            public void showResult(@NonNull ImageView view, Bitmap result) {
                if (result == null) {
                    view.setImageDrawable(KAvatarLoader.getDefaultAvatar(context, contact));
                } else {
                    view.setImageBitmap(result);
                }
            }

            @Override
            public void showLoading(@NonNull ImageView view) {
                view.setImageDrawable(KAvatarLoader.getDefaultAvatar(context, contact));
            }
        };

        if (contact.normalizedPhone != null) {
            load(view, loader, displayer, contact.normalizedPhone, cache);
        } else {
            view.setImageDrawable(KAvatarLoader.getDefaultAvatar(context, contact));
        }
    }
}
