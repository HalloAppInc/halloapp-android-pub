package com.halloapp.ui.contacts;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;
import com.halloapp.ui.chat.ChatViewModel;
import com.halloapp.util.ComputableLiveData;

import java.util.List;

public class ContactsViewModel extends AndroidViewModel {

    private final ContactsDb contactsDb = ContactsDb.getInstance();

    final ComputableLiveData<List<Contact>> contactList;

    public ContactsViewModel(@NonNull Application application) {
        this(application, false);
    }

    public ContactsViewModel(@NonNull Application application, boolean onlyFriends) {
        super(application);

        contactList = new ComputableLiveData<List<Contact>>() {

            @Override
            protected List<Contact> compute() {
                return Contact.sort(onlyFriends ? contactsDb.getFriends() : contactsDb.getUsers());
            }
        };
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final boolean onlyFriends;

        Factory(@NonNull Application application, boolean onlyFriends) {
            this.application = application;
            this.onlyFriends = onlyFriends;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ContactsViewModel.class)) {
                //noinspection unchecked
                return (T) new ContactsViewModel(application, onlyFriends);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
