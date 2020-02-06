package com.halloapp.contacts;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;

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
        final Callable<Contact> loader = () -> {
            final Contact contact = contactsDb.getContact(userId);
            return contact == null ? new Contact(userId) : contact;
        };
        final ViewDataLoader.Displayer<TextView, Contact> displayer = new ViewDataLoader.Displayer<TextView, Contact>() {

            @Override
            public void showResult(@NonNull TextView view, Contact contact) {
                if (contact != null) {
                    view.setText(contact.getDisplayName());
                } else {
                    view.setText(userId.formatPhoneNumber());
                }
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                view.setText(userId.formatPhoneNumber());
            }
        };
        load(view, loader, displayer, userId, cache);
    }

    public void resetCache() {
        cache.evictAll();
    }
}
