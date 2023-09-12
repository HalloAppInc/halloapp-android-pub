package com.halloapp.ui.contacts;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.privacy.FeedPrivacyManager;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditFavoritesViewModel extends AndroidViewModel {

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final ContactsDb contactsDb = ContactsDb.getInstance();
    private final FeedPrivacyManager feedPrivacyManager = FeedPrivacyManager.getInstance();

    final ComputableLiveData<List<Contact>> contactList;

    final ComputableLiveData<FeedPrivacy> favoritesList;

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsChanged() {
            contactList.invalidate();
        }
    };

    public EditFavoritesViewModel(@NonNull Application application, @Nullable Set<UserId> initialSelection) {
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

        favoritesList = new ComputableLiveData<FeedPrivacy>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected FeedPrivacy compute() {
                return feedPrivacyManager.getFeedPrivacy();
            }
        };

        contactsDb.addObserver(contactsObserver);
    }

    @NonNull
    public LiveData<Boolean> saveFavorites(@NonNull List<UserId> userIds) {
        MutableLiveData<Boolean> savingLiveData = new MutableLiveData<>();
        bgWorkers.execute(() -> savingLiveData.postValue(feedPrivacyManager.updateFeedPrivacy(PrivacyList.Type.ONLY, new ArrayList<>(userIds))));
        return savingLiveData;
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
            if (modelClass.isAssignableFrom(EditFavoritesViewModel.class)) {
                //noinspection unchecked
                return (T) new EditFavoritesViewModel(application, initialSelection);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
