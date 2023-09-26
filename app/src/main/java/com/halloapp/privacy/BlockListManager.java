package com.halloapp.privacy;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.Preferences;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.IqErrorException;
import com.halloapp.xmpp.privacy.PrivacyListApi;
import com.halloapp.xmpp.util.MutableObservable;
import com.halloapp.xmpp.util.Observable;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockListManager {

    private static BlockListManager instance;

    public static BlockListManager getInstance() {
        if (instance == null) {
            synchronized (BlockListManager.class) {
                if (instance == null) {
                    instance = new BlockListManager(
                            Connection.getInstance(),
                            ContactsDb.getInstance(),
                            Preferences.getInstance());
                }
            }
        }
        return instance;
    }

    private final Set<Observer> observers = new HashSet<>();

    public interface Observer {
        void onBlockListChanged();
    }

    private final ContactsDb contactsDb;
    private final Preferences preferences;
    private final PrivacyListApi privacyListApi;

    private BlockListManager(
            @NonNull Connection connection,
            @NonNull ContactsDb contactsDb,
            @NonNull Preferences preferences) {
        this.contactsDb = contactsDb;
        this.preferences = preferences;

        this.privacyListApi = new PrivacyListApi(connection);
    }

    public void addObserver(Observer observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }

    public void removeObserver(@NonNull Observer observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }

    private void notifyBlockListChanged() {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onBlockListChanged();
            }
        }
    }

    @WorkerThread
    public void onLoginFailed() {
        preferences.setLastBlockListSyncTime(0);
    }

    @WorkerThread
    @Nullable
    public List<UserId> getBlockList() {
        if (preferences.getLastBlockListSyncTime() == 0) {
            fetchBlockList();
            return null;
        }
        return contactsDb.getBlockList();
    }

    @WorkerThread
    public void fetchInitialBlockList() {
        if (preferences.getLastBlockListSyncTime() == 0) {
            Log.i("BlockListManager/fetching initial block list");
            fetchBlockList();
        }
    }

    @WorkerThread
    public void fetchBlockList() {
        List<UserId> blockList = privacyListApi.getBlockList();
        if (blockList != null) {
           saveBlockList(blockList);
        }
    }

    private void saveBlockList(@NonNull List<UserId> blockList) {
        contactsDb.setBlockList(blockList);
        preferences.setLastBlockListSyncTime(System.currentTimeMillis());
        notifyBlockListChanged();
    }

    public boolean refetchBlockListSync() {
        List<UserId> blockList = privacyListApi.getBlockList();
        if (blockList == null) {
            return false;
        }
        saveBlockList(blockList);
        return true;
    }

    @AnyThread
    private Observable<Boolean> updateBlockList(@NonNull List<UserId> currentList, @NonNull List<UserId> newBlockList) {
        List<UserId> addedUsers = new ArrayList<>();
        List<UserId> deletedUsers = new ArrayList<>();

        diffList(currentList, newBlockList, addedUsers, deletedUsers);

        return privacyListApi.updateBlockList(addedUsers, deletedUsers);
    }

    private void diffList(@NonNull List<UserId> oldList, @NonNull List<UserId> newList, List<UserId> addedUsers, List<UserId> deletedUsers) {
        HashSet<UserId> oldSet = new HashSet<>(oldList);
        HashSet<UserId> newSet = new HashSet<>(newList);
        for (UserId userId : oldSet) {
            if (!newSet.contains(userId)) {
                deletedUsers.add(userId);
            }
        }
        for (UserId userId : newSet) {
            if (!oldSet.contains(userId)) {
                addedUsers.add(userId);
            }
        }
    }

    @AnyThread
    public Observable<Boolean> blockContact(@NonNull UserId userId) {
        MutableObservable<Boolean> resultObservable = new MutableObservable<>();
        Connection.getInstance().blockFriend(userId).onResponse(result -> {
            if (result == null || !result.success) {
                resultObservable.setResponse(false);
                return;
            }
            contactsDb.addUserToBlockList(userId);
            contactsDb.addFriendship(result.info);
            resultObservable.setResponse(true);
            notifyBlockListChanged();
        }).onError(e -> {
          if (e instanceof IqErrorException) {
              IqErrorException iqError = (IqErrorException) e;
              if ("hash_mismatch".equals(iqError.getReason())) {
                  Log.i("BlockListManager/blockContact hash_mismatch retrying!");
                  List<UserId> blockList = contactsDb.getBlockList();
                  blockList.add(userId);
                  if (refetchBlockListSync()) {
                      updateBlockList(contactsDb.getBlockList(), blockList)
                              .onResponse(result -> {
                                  if (result == null || !result) {
                                      resultObservable.setResponse(false);
                                      return;
                                  }
                                  saveBlockList(blockList);
                                  resultObservable.setResponse(true);
                              })
                              .onError(resultObservable::setException);
                      return;
                  }
              }
          }
          resultObservable.setException(e);
        });

        return resultObservable;
    }

    @AnyThread
    public Observable<Boolean> unblockContact(@NonNull UserId userId) {
        MutableObservable<Boolean> resultObservable = new MutableObservable<>();
        Connection.getInstance().unblockFriend(userId).onResponse(result -> {
            if (result == null || !result.success) {
                resultObservable.setResponse(false);
                return;
            }
            contactsDb.removeUserFromBlockList(userId);
            contactsDb.addFriendship(result.info);
            resultObservable.setResponse(true);
            notifyBlockListChanged();
        }).onError(e -> {
            if (e instanceof IqErrorException) {
                IqErrorException iqError = (IqErrorException) e;
                if ("hash_mismatch".equals(iqError.getReason())) {
                    Log.i("BlockListManager/unblockContact hash_mismatch retrying!");
                    List<UserId> blockList = contactsDb.getBlockList();
                    blockList.remove(userId);
                    if (refetchBlockListSync()) {
                        updateBlockList(contactsDb.getBlockList(), blockList)
                                .onResponse(result -> {
                                    if (result == null || !result) {
                                        resultObservable.setResponse(false);
                                        return;
                                    }
                                    saveBlockList(blockList);
                                    resultObservable.setResponse(true);
                                })
                                .onError(resultObservable::setException);
                        return;
                    }
                }
            }
            resultObservable.setException(e);
        });

        return resultObservable;
    }
}
