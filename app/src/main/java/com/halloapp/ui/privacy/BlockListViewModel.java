package com.halloapp.ui.privacy;

import android.app.Application;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.Constants;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.PresenceLoader;
import com.halloapp.xmpp.privacy.PrivacyListApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class BlockListViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Contact>> blockList;
    private final MutableLiveData<Boolean> inProgress;

    private final BgWorkers bgWorkers;
    private final Connection connection;
    private final ContactsDb contactsDb;
    private final PrivacyListApi privacyListApi;
    private final PresenceLoader presenceLoader;

    public BlockListViewModel(@NonNull Application application) {
        super(application);
        bgWorkers = BgWorkers.getInstance();
        connection = Connection.getInstance();
        contactsDb = ContactsDb.getInstance(application);
        presenceLoader = PresenceLoader.getInstance();
        privacyListApi = new PrivacyListApi(connection);
        blockList = new MutableLiveData<>();
        inProgress = new MutableLiveData<>();

        loadBlockList();
    }

    public LiveData<Boolean> getProgressLiveData() {
        return inProgress;
    }

    private boolean fetchInProgress;

    @MainThread
    private void loadBlockList() {
        if (fetchInProgress) {
            return;
        }
        inProgress.setValue(true);
        bgWorkers.execute(() -> {
            List<UserId> deviceBlockList = contactsDb.getBlockList();
            inProgress.postValue(false);
            updateBlockedIds(deviceBlockList);
            fetchBlockList();
        });
    }

    @MainThread
    private void fetchBlockList() {
        if (fetchInProgress) {
            return;
        }
        fetchInProgress = true;
        privacyListApi.getBlockList().onResponse(ids -> {
            if (ids == null) {
                return;
            }
            contactsDb.setBlockList(ids);
            updateBlockedIds(ids);
            fetchInProgress = false;
        }).onError(e -> {
            fetchInProgress = false;
            Log.e("BlockListViewModel failed to fetch block list", e);
        });
    }

    @WorkerThread
    private void updateBlockedIds(@NonNull List<UserId> blockedIds) {
        Set<UserId> idSet = new HashSet<>(blockedIds);
        List<Contact> blockedContacts = new ArrayList<>();
        for (UserId id : idSet) {
            blockedContacts.add(contactsDb.getContact(id));
        }
        Contact.sort(blockedContacts);
        blockList.postValue(blockedContacts);
    }

    @NonNull
    public LiveData<List<Contact>> getBlockList() {
        return blockList;
    }

    @MainThread
    public LiveData<Boolean> unblockContact(@NonNull UserId userId) {
        MutableLiveData<Boolean> unblockResult = new MutableLiveData<>();
        long startTime = System.currentTimeMillis();
        privacyListApi.unblockUsers(Collections.singleton(userId)).onResponse(result -> {
            long dT = System.currentTimeMillis() - startTime;
            if (dT < Constants.MINIMUM_PROGRESS_DIALOG_TIME_MILLIS) {
                try {
                    Thread.sleep(Constants.MINIMUM_PROGRESS_DIALOG_TIME_MILLIS - dT);
                } catch (InterruptedException ignored) { }
            }
            if (result == null || !result) {
                unblockResult.postValue(false);
                return;
            }
            List<Contact> currentList = blockList.getValue();
            if (currentList != null) {
                currentList = new ArrayList<>(currentList);
                ListIterator<Contact> listIterator = currentList.listIterator();
                while (listIterator.hasNext()) {
                    Contact contact = listIterator.next();
                    if (userId.equals(contact.userId)) {
                        listIterator.remove();
                        break;
                    }
                }
                blockList.postValue(currentList);
            }
            fetchBlockList();
            unblockResult.postValue(result);
        }).onError(exception -> {
            fetchBlockList();
            unblockResult.postValue(false);
        });
        return unblockResult;
    }

    @MainThread
    public LiveData<Boolean> blockContact(@NonNull UserId userId) {
        MutableLiveData<Boolean> blockResult = new MutableLiveData<>();
        long startTime = System.currentTimeMillis();
        privacyListApi.blockUsers(Collections.singleton(userId)).onResponse(result -> {
            long dT = System.currentTimeMillis() - startTime;
            if (dT < Constants.MINIMUM_PROGRESS_DIALOG_TIME_MILLIS) {
                try {
                    Thread.sleep(Constants.MINIMUM_PROGRESS_DIALOG_TIME_MILLIS - dT);
                } catch (InterruptedException ignored) { }
            }
            if (result == null || !result) {
                blockResult.postValue(false);
                return;
            }
            List<Contact> currentList = blockList.getValue();
            if (currentList != null) {
                currentList = new ArrayList<>(currentList);
                currentList.add(contactsDb.getContact(userId));
                Contact.sort(currentList);
                blockList.postValue(currentList);
            }
            fetchBlockList();
            presenceLoader.reportBlocked(userId);
            blockResult.postValue(true);
        }).onError(exception -> {
            fetchBlockList();
            blockResult.postValue(false);
        });
        return blockResult;
    }

}
