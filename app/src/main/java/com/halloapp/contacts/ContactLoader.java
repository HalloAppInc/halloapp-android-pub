package com.halloapp.contacts;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import com.halloapp.R;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.ViewDataLoader;

import java.util.concurrent.Callable;

public class ContactLoader extends ViewDataLoader<TextView, Contact, UserId> {

    private final LruCache<UserId, Contact> cache = new LruCache<>(512);
    private final ContactsDb contactsDb;

    public ContactLoader(@NonNull Context context) {
        contactsDb = ContactsDb.getInstance(context);
    }

    @MainThread
    public void load(@NonNull TextView view, @NonNull UserId userId) {
        load(view, userId, true);
    }

    @MainThread
    public void load(@NonNull TextView view, @NonNull UserId userId, boolean openProfileOnTap) {
        if (userId.isMe()) {
            view.setText(view.getContext().getString(R.string.me));
            view.setClickable(false);
            view.setOnClickListener(null);
            return;
        }
        final Callable<Contact> loader = () -> contactsDb.getContact(userId);
        final ViewDataLoader.Displayer<TextView, Contact> displayer = new ViewDataLoader.Displayer<TextView, Contact>() {

            @Override
            public void showResult(@NonNull TextView view, Contact contact) {
                if (contact == null) {
                    view.setText(R.string.unknown_contact);
                    return;
                }
                final String name = contact.getDisplayName();
                if (TextUtils.isEmpty(name)) {
                    view.setText(R.string.unknown_contact);
                } else {
                    view.setText(contact.getDisplayName());
                    if (openProfileOnTap && !userId.isMe() && contact.addressBookName != null) {
                        view.setOnClickListener(v -> {
                            v.getContext().startActivity(ViewProfileActivity.viewProfile(v.getContext(), userId));
                        });
                    }
                }
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                view.setText("");
                view.setOnClickListener(null);
                view.setClickable(false);
            }
        };
        load(view, loader, displayer, userId, cache);
    }

    public void resetCache() {
        cache.evictAll();
    }
}
