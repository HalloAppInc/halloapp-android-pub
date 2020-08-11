package com.halloapp.ui.privacy;

import android.app.Application;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.privacy.BlockListManager;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.DelayedProgressLiveData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockListViewModel extends AndroidViewModel {

    private ComputableLiveData<List<Contact>> blockListLiveData;

    private final ContactsDb contactsDb;
    private final BlockListManager blockListManager;

    private final BlockListManager.Observer blockListObserver = () -> {
        blockListLiveData.invalidate();
    };

    public BlockListViewModel(@NonNull Application application) {
        super(application);
        contactsDb = ContactsDb.getInstance();
        blockListManager = BlockListManager.getInstance();

        blockListLiveData = new ComputableLiveData<List<Contact>>() {
            @Override
            protected List<Contact> compute() {
                List<UserId> blockedIds = blockListManager.getBlockList();
                return convertBlockedIdsToContacts(blockedIds);
            }
        };

        blockListManager.addObserver(blockListObserver);
    }

    @WorkerThread
    @Nullable
    private List<Contact> convertBlockedIdsToContacts(@Nullable List<UserId> blockedIds) {
        if (blockedIds == null) {
            return null;
        }
        Set<UserId> idSet = new HashSet<>(blockedIds);
        List<Contact> blockedContacts = new ArrayList<>();
        for (UserId id : idSet) {
            blockedContacts.add(contactsDb.getContact(id));
        }
        Contact.sort(blockedContacts);
        return blockedContacts;
    }

    @NonNull
    public LiveData<List<Contact>> getBlockList() {
        return blockListLiveData.getLiveData()  ;
    }

    @MainThread
    public LiveData<Boolean> unblockContact(@NonNull UserId userId) {
        MutableLiveData<Boolean> unblockResult = new DelayedProgressLiveData<>();
        blockListManager.unblockContact(userId).onResponse(result -> {
            if (result == null || !result) {
                unblockResult.postValue(false);
            } else {
                unblockResult.postValue(true);
            }
        }).onError(e -> {
            unblockResult.postValue(false);
        });
        return unblockResult;
    }

    @MainThread
    public LiveData<Boolean> blockContact(@NonNull UserId userId) {
        MutableLiveData<Boolean> blockResult = new DelayedProgressLiveData<>();
        blockListManager.blockContact(userId).onResponse(result -> {
            if (result == null || !result) {
                blockResult.postValue(false);
            } else {
                blockResult.postValue(true);
            }
        }).onError(e -> {
            blockResult.postValue(false);
        });
        return blockResult;
    }

    @Override
    protected void onCleared() {
        blockListManager.removeObserver(blockListObserver);
    }
}
