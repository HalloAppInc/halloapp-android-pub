package com.halloapp.ui.privacy;

import android.app.Application;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.privacy.PrivacyListApi;
import com.halloapp.xmpp.util.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockListViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Contact>> blockList;
    private final MutableLiveData<Boolean> inProgress;

    private final Connection connection;
    private final ContactsDb contactsDb;
    private final PrivacyListApi privacyListApi;

    public BlockListViewModel(@NonNull Application application) {
        super(application);
        connection = Connection.getInstance();
        contactsDb = ContactsDb.getInstance(application);
        privacyListApi = new PrivacyListApi(connection);
        blockList = new MutableLiveData<>();
        inProgress = new MutableLiveData<>();
    }

    public LiveData<Boolean> getProgressLiveData() {
        return inProgress;
    }

    @MainThread
    public void fetchBlockList() {
        privacyListApi.getBlockList().onResponse(ids -> {
            List<Contact> blockedContacts = new ArrayList<>();
            if (ids != null) {
                for (UserId userId : ids) {
                    blockedContacts.add(contactsDb.getContact(userId));
                }
            }
            blockList.postValue(blockedContacts);
            inProgress.postValue(false);
            contactsDb.setBlockList(ids);
        }).onError(exception -> {
            inProgress.postValue(false);
            blockList.postValue(null);
        });
    }

    @NonNull
    public LiveData<List<Contact>> getBlockList() {
        return blockList;
    }

    @MainThread
    public void unblockContact(@NonNull UserId userId) {
        inProgress.setValue(true);
        privacyListApi.unblockUsers(Collections.singleton(userId)).onResponse(result -> {
            fetchBlockList();
        }).onError(exception -> {
            fetchBlockList();
        });
    }

    @MainThread
    public void blockContact(@NonNull UserId userId) {
        inProgress.setValue(true);
        privacyListApi.blockUsers(Collections.singleton(userId)).onResponse(result -> {
            fetchBlockList();
        }).onError(exception -> {
            fetchBlockList();
        });
    }

}
