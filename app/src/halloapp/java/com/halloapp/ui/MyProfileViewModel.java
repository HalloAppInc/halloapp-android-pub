package com.halloapp.ui;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.halloapp.Me;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.FriendshipInfo;
import com.halloapp.contacts.SocialLink;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.Link;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

import java.util.ArrayList;

public class MyProfileViewModel extends ViewModel {

    private final Me me = Me.getInstance();

    private final ComputableLiveData<String> phoneNumberLiveData;
    private final ComputableLiveData<Integer> friendRequestsCount;
    private final MutableLiveData<ArrayList<SocialLink>> linksLiveData = new MutableLiveData<>();

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

        computeLinks();
    }

    public MutableLiveData<ArrayList<SocialLink>> getLinks() {
        return linksLiveData;
    }

    public void computeLinks() {
        Connection.getInstance().getHalloappProfileInfo(new UserId(me.getUser()), me.getUsername()).onResponse(response -> {
            if (response != null && response.success) {
                ArrayList<SocialLink> socialLinks = new ArrayList<>();
                for (Link link : response.profile.getLinksList()) {
                    socialLinks.add(new SocialLink(link.getText(), SocialLink.fromProtoType(link.getType())));
                }
                linksLiveData.postValue(socialLinks);
            }
        }).onError(err -> {
            Log.e("Failed to get social media links", err);
        });
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
