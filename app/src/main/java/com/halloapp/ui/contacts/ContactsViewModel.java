package com.halloapp.ui.contacts;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.util.ComputableLiveData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactsViewModel extends AndroidViewModel {

    private final ContactsDb contactsDb = ContactsDb.getInstance();

    final ComputableLiveData<List<Contact>> contactList;

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsChanged() {
            contactList.invalidate();
        }
    };

    public ContactsViewModel(@NonNull Application application) {
        this(application, null);
    }

    public ContactsViewModel(@NonNull Application application, @Nullable Set<UserId> initialSelection) {
        super(application);

        contactList = new ComputableLiveData<List<Contact>>() {

            @Override
            protected List<Contact> compute() {
                List<Contact> contacts = contactsDb.getUsers();
                if (initialSelection != null) {
                    Set<UserId> initialSelectionCopy = new HashSet<>(initialSelection);
                    for (Contact contact : contacts) {
                        initialSelectionCopy.remove(contact.userId);
                    }
                    for (UserId userId : initialSelectionCopy) {
                        contacts.add(contactsDb.getContact(userId));
                    }
                }
                return Contact.sort(contacts);
            }
        };

        contactsDb.addObserver(contactsObserver);
    }

    @Override
    protected void onCleared() {
        contactsDb.removeObserver(contactsObserver);
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final Set<UserId> initialSelection;

        Factory(@NonNull Application application, @Nullable Set<UserId> initialSelection) {
            this.application = application;
            this.initialSelection = initialSelection;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ContactsViewModel.class)) {
                //noinspection unchecked
                return (T) new ContactsViewModel(application, initialSelection);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
