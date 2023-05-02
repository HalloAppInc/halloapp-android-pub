package com.halloapp.katchup.avatar;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.util.logs.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

public class KDeviceAvatarLoader extends KBaseAvatarLoader {
    private final Context context;

    public KDeviceAvatarLoader(@NonNull Context context) {
        this.context = context;
    }

    public void load(@NonNull ImageView view, @NonNull Contact contact) {
        final Callable<Drawable> loader = () -> {
            Bitmap avatar = getAddressBookPhoto(context, contact.normalizedPhone);
            return avatar != null ? new BitmapDrawable(view.getResources(), avatar) : getDefaultAvatar(view.getContext(), contact);
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

        if (contact.normalizedPhone != null) {
            load(view, loader, displayer, getKey(contact), cache);
        } else {
            loadDefaultAvatar(view, contact);
        }
    }

    @MainThread
    public void loadDefaultAvatar(@NonNull ImageView view, @NonNull Contact contact) {
        executor.submit(() -> {
            Drawable defaultAvatar = getDefaultAvatar(view.getContext(), contact);;
            view.post(() -> view.setImageDrawable(defaultAvatar));
        });
    }

    @NonNull
    private String getKey(@NonNull Contact contact) {
        if (contact.normalizedPhone != null) {
            return contact.normalizedPhone;
        }

        if (contact.getAddressBookId() > 0) {
            return String.valueOf(contact.getAddressBookId());
        }

        return "";
    }

    @WorkerThread
    private static Bitmap getAddressBookPhoto(@NonNull Context context, String number) {
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
            Log.e("KDeviceAvatarLoader/getAddressBookPhoto failed to get photo", e);
        }
        return photo;
    }

    @WorkerThread
    private static Long fetchContactIdFromPhoneNumber(@NonNull Context context, String phoneNumber) {
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
}
