package com.halloapp.ui.avatar;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.collection.LruCache;

import com.halloapp.R;
import com.halloapp.util.logs.Log;
import com.halloapp.util.ViewDataLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

public class DeviceAvatarLoader extends ViewDataLoader<ImageView, Bitmap, String> {

    private final LruCache<String, Bitmap> cache;

    private final Context context;

    public DeviceAvatarLoader(@NonNull Context context) {
        this.context = context.getApplicationContext();
        // Use 1/8th of the available memory for memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        Log.i("DeviceAvatarLoader: create " + cacheSize + "KB cache for images");
        cache = new LruCache<String, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(@NonNull String key, @NonNull Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than number of items
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @MainThread
    public void load(@NonNull ImageView view, @Nullable String number) {
        final Callable<Bitmap> loader = () -> getAddressBookPhoto(context, number);
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
        if (number != null) {
            load(view, loader, displayer, number, cache);
        } else {
            view.setImageResource(R.drawable.avatar_person);
        }
    }

    @WorkerThread
    public static Bitmap getAddressBookPhoto(@NonNull Context context, String number) {
        Bitmap photo = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.avatar_person);
        Long id = fetchContactIdFromPhoneNumber(context, number);
        if (id == null) {
            return null;
        }
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        try (InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), contactUri)) {
            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);
            }
        } catch (IOException e) {
            Log.e("DeviceAvatarLoader/getAddressBookPhoto failed to get photo", e);
        }
        return photo;
    }

    @WorkerThread
    public static Long fetchContactIdFromPhoneNumber(@NonNull Context context, String phoneNumber) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Long contactId = null;
        try (Cursor cursor = context.getContentResolver().query(uri,
                new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID },
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    //noinspection Range
                    contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
                } while (cursor.moveToNext());
            }
        }
        return contactId;
    }

    @Override
    public void destroy() {
        super.destroy();
        cache.evictAll();
    }
}
