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
import com.halloapp.contacts.FriendshipInfo;
import com.halloapp.id.UserId;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactsViewModel extends AndroidViewModel {

    private final ContactsDb contactsDb = ContactsDb.getInstance();

    final ComputableLiveData<List<Contact>> contactList;
    private final ComputableLiveData<List<Contact>> friendsList;


    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsChanged() {
            contactList.invalidate();
        }

        @Override
        public void onFriendshipsChanged(@NonNull FriendshipInfo friendshipInfo) {
            friendsList.invalidate();
        }
    };

    public ContactsViewModel(@NonNull Application application) {
        this(application, null);
    }

    public ContactsViewModel(@NonNull Application application, @Nullable Set<UserId> initialSelection) {
        super(application);

        contactList = new ComputableLiveData<List<Contact>>() {

            @SuppressLint("RestrictedApi")
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

        friendsList = new ComputableLiveData<List<Contact>>() {

            @SuppressLint("RestrictedApi")
            @Override
            protected List<Contact> compute() {
                List<Contact> contacts = contactsDb.getFriends();
                return Contact.sort(contacts);
            }
        };

        contactsDb.addObserver(contactsObserver);
    }

    public void sendFriendRequests(@NonNull HashSet<UserId> userIds) {
        for (UserId userId : userIds) {
            Connection.getInstance().sendFriendRequest(userId).onResponse(response -> {
                if (!response.success) {
                    Log.e("Unable to send a friend request to " + userId);
                } else {
                    ContactsDb.getInstance().addFriendship(response.info);
                }
            }).onError(e -> {
                Log.e("Unable to send friend request", e);
            });
        }
    }

    public ComputableLiveData<List<Contact>> getFriendsList() {
        return friendsList;
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
