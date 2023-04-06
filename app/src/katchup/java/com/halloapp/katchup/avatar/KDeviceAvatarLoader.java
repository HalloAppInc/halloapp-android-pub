package com.halloapp.katchup.avatar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.halloapp.contacts.Contact;
import com.halloapp.ui.avatar.DeviceAvatarLoader;

import java.util.concurrent.Callable;

public class KDeviceAvatarLoader extends KBaseAvatarLoader {
    private final Context context;

    public KDeviceAvatarLoader(@NonNull Context context) {
        this.context = context;
    }

    public void load(@NonNull ImageView view, @NonNull Contact contact) {
        final Callable<Drawable> loader = () -> {
            Bitmap avatar = DeviceAvatarLoader.getAddressBookPhoto(context, contact.normalizedPhone);
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
}
