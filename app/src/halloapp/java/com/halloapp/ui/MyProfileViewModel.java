package com.halloapp.ui;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.halloapp.Me;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.FriendshipInfo;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.StringUtils;

public class MyProfileViewModel extends ViewModel {

    private final Me me = Me.getInstance();

    private final ComputableLiveData<String> phoneNumberLiveData;
    private final ComputableLiveData<Integer> friendRequestsCount;

    private final ContactsDb contactsDb;

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onFriendshipsChanged(@NonNull FriendshipInfo friendshipInfo) {
            friendRequestsCount.invalidate();
        }
    };

    public MyProfileViewModel() {
        contactsDb = ContactsDb.getInstance();
        contactsDb.addObserver(contactsObserver);
        phoneNumberLiveData = new ComputableLiveData<String>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected String compute() {
                return StringUtils.formatPhoneNumber(me.getPhone());
            }
        };
        friendRequestsCount = new ComputableLiveData<Integer>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected Integer compute() {
                return ContactsDb.getInstance().getFriendRequestCount();
            }
        };
    }

    @Override
    protected void onCleared() {
        contactsDb.removeObserver(contactsObserver);
    }

    public LiveData<String> getPhone() {
        return phoneNumberLiveData.getLiveData();
    }

    public LiveData<String> getName() {
        return me.name;
    }

    public LiveData<String> getUsername() {
        return me.username;
    }

    public LiveData<Integer> getFriendRequestsCount() {
        return friendRequestsCount.getLiveData();
    }
}
