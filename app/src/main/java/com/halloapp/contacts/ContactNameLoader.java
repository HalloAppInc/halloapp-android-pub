package com.halloapp.contacts;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.halloapp.util.ViewDataLoader;

import java.util.concurrent.Callable;

public class ContactNameLoader extends ViewDataLoader<TextView, Contact, UserId> {

    private final LruCache<UserId, Contact> cache = new LruCache<>(512);
    private final ContactsDb contactsDb;

    public ContactNameLoader(@NonNull Context context) {
        contactsDb = ContactsDb.getInstance(context);
    }

    @MainThread
    public void load(@NonNull TextView view, @NonNull UserId userId) {
        final Callable<Contact> loader = () -> contactsDb.getContact(userId);
        final ViewDataLoader.Displayer<TextView, Contact> displayer = new ViewDataLoader.Displayer<TextView, Contact>() {

            @Override
            public void showResult(@NonNull TextView view, Contact contact) {
                if (contact != null) {
                    view.setText(contact.getDisplayName());
                }
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                view.setText("");
            }
        };
        load(view, loader, displayer, userId, cache);
    }

}
