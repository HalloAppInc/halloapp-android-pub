package com.halloapp.contacts;

import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.ViewDataLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ContactLoader extends ViewDataLoader<TextView, Contact, UserId> {

    private final LruCache<UserId, Contact> cache = new LruCache<>(512);
    private final ContactsDb contactsDb;

    public ContactLoader() {
        contactsDb = ContactsDb.getInstance();
    }

    @MainThread
    public void load(@NonNull TextView view, @NonNull UserId userId) {
        load(view, userId, true);
    }

    @MainThread
    public void load(@NonNull TextView view, @NonNull UserId userId, boolean openProfileOnTap) {
        load(view, userId, openProfileOnTap, null);
    }

    @MainThread
    public void load(@NonNull TextView view, @NonNull UserId userId, boolean openProfileOnTap, @Nullable String backupName) {
        if (userId.isMe()) {
            view.setText(view.getContext().getString(R.string.me));
            view.setClickable(false);
            if (openProfileOnTap) {
                view.setOnClickListener(null);
            }
            return;
        }
        final Callable<Contact> loader = () -> contactsDb.getContact(userId);
        final ViewDataLoader.Displayer<TextView, Contact> displayer = new ViewDataLoader.Displayer<TextView, Contact>() {

            @Override
            public void showResult(@NonNull TextView view, Contact contact) {
                String backupText = backupName != null ? backupName : view.getResources().getString(R.string.unknown_contact);
                if (contact == null) {
                    view.setText(backupText);
                    return;
                }
                contact.fallbackName = backupText;
                final String name = contact.getDisplayName();
                if (TextUtils.isEmpty(name)) {
                    view.setText(backupText);
                } else {
                    view.setText(contact.getDisplayName());
                    if (openProfileOnTap && !userId.isMe()) {
                        view.setOnClickListener(v -> {
                            v.getContext().startActivity(ViewProfileActivity.viewProfile(v.getContext(), userId));
                        });
                    }
                }
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                view.setText("");
                view.setClickable(false);
                if (openProfileOnTap) {
                    view.setOnClickListener(null);
                }
            }
        };
        load(view, loader, displayer, userId, cache);
    }

    @MainThread
    public void load(@NonNull TextView view, @NonNull UserId userId, @NonNull ViewDataLoader.Displayer<TextView, Contact> displayer) {
        final Callable<Contact> loader = () -> contactsDb.getContact(userId);
        load(view, loader, displayer, userId, cache);
    }

    @MainThread
    public void loadMultiple(@NonNull TextView view, @NonNull List<UserId> userIds, @NonNull ViewDataLoader.Displayer<TextView, List<Contact>> displayer) {
        final List<Callable<Contact>> loaders = new ArrayList<>();
        for (UserId userId : userIds) {
            loaders.add(() -> contactsDb.getContact(userId));
        }
        loadMultiple(view, loaders, displayer, userIds, cache);
    }

    public void resetCache() {
        cache.evictAll();
    }
}
