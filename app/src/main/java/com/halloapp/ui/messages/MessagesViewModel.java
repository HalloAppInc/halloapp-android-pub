package com.halloapp.ui.messages;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.util.ComputableLiveData;

import java.text.Collator;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MessagesViewModel extends AndroidViewModel {

    final ComputableLiveData<List<Contact>> contactsList;
    private final ContactsDb contactsDb;

    private final ContactsDb.Observer contactsObserver = new ContactsDb.Observer() {

        @Override
        public void onContactsChanged() {
            contactsList.invalidate();
        }

        @Override
        public void onContactsReset() {
        }
    };

    public MessagesViewModel(@NonNull Application application) {
        super(application);

        contactsDb = ContactsDb.getInstance(application);
        contactsDb.addObserver(contactsObserver);

        contactsList = new ComputableLiveData<List<Contact>>() {
            @Override
            protected List<Contact> compute() {
                final List<Contact> friends = ContactsDb.getInstance(application).getFriends();
                final Collator collator = java.text.Collator.getInstance(Locale.getDefault());
                Collections.sort(friends, (obj1, obj2) -> collator.compare(obj1.getDisplayName(), obj2.getDisplayName()));
                return friends;
            }
        };
    }

    @Override
    protected void onCleared() {
        contactsDb.removeObserver(contactsObserver);
    }
}