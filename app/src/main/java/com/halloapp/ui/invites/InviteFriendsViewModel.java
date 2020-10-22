package com.halloapp.ui.invites;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.Preferences;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.invites.InvitesApi;
import com.halloapp.xmpp.invites.InvitesResponseIq;

import java.util.List;

public class InviteFriendsViewModel extends AndroidViewModel {

    private Connection connection;
    private ContactsDb contactsDb;
    private Preferences preferences;

    private final InvitesApi invitesApi;

    MutableLiveData<Integer> inviteCountData;
    ComputableLiveData<List<Contact>> contactList;

    public static final int RESPONSE_RETRYABLE = -1;

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {

        @Override
        public void onContactsChanged() {
            contactList.invalidate();
        }
    };

    public InviteFriendsViewModel(@NonNull Application application) {
        super(application);
        connection = Connection.getInstance();
        contactsDb = ContactsDb.getInstance();
        preferences = Preferences.getInstance();

        invitesApi = new InvitesApi(connection);

        inviteCountData = new MutableLiveData<>();

        contactList = new ComputableLiveData<List<Contact>>() {

            @Override
            protected List<Contact> compute() {
                return Contact.sort(ContactsDb.getInstance().getAllUsers());
            }
        };

        contactsDb.addObserver(contactsObserver);
        fetchInvites();
    }

    private void fetchInvites() {
        invitesApi.getAvailableInviteCount().onResponse(response -> {
            if (response == null) {
                inviteCountData.postValue(RESPONSE_RETRYABLE);
            } else {
                preferences.setInvitesRemaining(response);
                inviteCountData.postValue(response);
            }
        }).onError(e -> {
            inviteCountData.postValue(RESPONSE_RETRYABLE);
        });
    }

    public void refreshInvites() {
        fetchInvites();
    }

    public void refreshContacts() {
        contactList.invalidate();
    }

    public LiveData<Integer> getInviteCount() {
        return inviteCountData;
    }

    public LiveData<List<Contact>> getContactList() {
        return contactList.getLiveData();
    }

    public LiveData<Integer> sendInvite(@NonNull String phoneNumber) {
        MutableLiveData<Integer> inviteResult = new MutableLiveData<>();
        invitesApi.sendInvite(phoneNumber).onResponse(result -> {
            inviteResult.postValue(result);
            fetchInvites();
        }).onError(e -> {
            inviteResult.postValue(InvitesResponseIq.Result.UNKNOWN);
            Log.e("inviteFriendsViewModel/sendInvite failed to send invite", e);
        });
        return inviteResult;
    }

    @Override
    protected void onCleared() {
        contactsDb.removeObserver(contactsObserver);
    }
}
