package com.halloapp.ui.contacts;

import android.annotation.SuppressLint;
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

public class SelectContactsViewModel extends AndroidViewModel {

    private final ContactsDb contactsDb = ContactsDb.getInstance();

    final ComputableLiveData<List<Contact>> contactList;

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @SuppressLint("RestrictedApi")
        @Override
        public void onContactsChanged() {
            contactList.invalidate();
        }
    };

    public SelectContactsViewModel(@NonNull Application application) {
        super(application);

        contactList = new ComputableLiveData<List<Contact>>() {

            @SuppressLint("RestrictedApi")
            @Override
            protected List<Contact> compute() {
                List<Contact> contacts = contactsDb.getUniqueContactsWithPhones();
                return Contact.sort(contacts);
            }
        };

        contactsDb.addObserver(contactsObserver);
    }

    @Override
    protected void onCleared() {
        contactsDb.removeObserver(contactsObserver);
    }
}
