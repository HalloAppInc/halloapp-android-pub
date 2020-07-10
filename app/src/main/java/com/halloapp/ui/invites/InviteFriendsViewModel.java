package com.halloapp.ui.invites;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.InvitesResponseIq;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class InviteFriendsViewModel extends AndroidViewModel {

    private BgWorkers bgWorkers;
    private Connection connection;

    ComputableLiveData<Integer> inviteCountData;
    ComputableLiveData<List<Contact>> contactList;

    public static final int RESPONSE_RETRYABLE = -1;

    public InviteFriendsViewModel(@NonNull Application application) {
        super(application);
        bgWorkers = BgWorkers.getInstance();
        connection = Connection.getInstance();
        
        inviteCountData = new ComputableLiveData<Integer>() {
            @Override
            protected Integer compute() {
                try {
                    Integer response = connection.getAvailableInviteCount().get();
                    if (response == null) {
                        return RESPONSE_RETRYABLE;
                    }
                    return response;
                } catch (ExecutionException | InterruptedException e) {
                    Log.e("InviteFriendsViewModel/inviteCountData failed to get count");
                    return RESPONSE_RETRYABLE;
                }
            }
        };

        contactList = new ComputableLiveData<List<Contact>>() {

            @Override
            protected List<Contact> compute() {
                return Contact.sort(ContactsDb.getInstance(application).getAllUsers());
            }
        };
    }

    public void refreshInvites() {
        inviteCountData.invalidate();
    }

    public void refreshContacts() {
        contactList.invalidate();
    }

    public LiveData<Integer> getInviteCount() {
        return inviteCountData.getLiveData();
    }

    public LiveData<List<Contact>> getContactList() {
        return contactList.getLiveData();
    }

    public LiveData<Integer> sendInvite(@NonNull String phoneNumber) {
        MutableLiveData<Integer> inviteResult = new MutableLiveData<>();
        bgWorkers.execute(() -> {
            Future<Integer> resultFuture = connection.sendInvite(phoneNumber);
            try {
                inviteResult.postValue(resultFuture.get());
                inviteCountData.invalidate();
                return;
            } catch (ExecutionException | InterruptedException e) {
                Log.e("inviteFriendsViewModel/sendInvite failed to send invite", e);
            }
            inviteResult.postValue(InvitesResponseIq.Result.UNKNOWN);
        });
        return inviteResult;
    }
}
