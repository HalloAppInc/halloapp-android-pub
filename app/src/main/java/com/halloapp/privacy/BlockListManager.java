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
import com.halloapp.xmpp.privacy.PrivacyListApi;
import com.halloapp.xmpp.util.MutableObservable;
import com.halloapp.xmpp.util.Observable;

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

    private ContactsDb contactsDb;
    private Preferences preferences;

    private PrivacyListApi privacyListApi;

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
    public void fetchBlockList() {
        privacyListApi.getBlockList().onResponse(blockListResponse -> {
           if (blockListResponse != null) {
               contactsDb.setBlockList(blockListResponse);
               preferences.setLastBlockListSyncTime(System.currentTimeMillis());
               notifyBlockListChanged();
           }
        }).onError(e -> {
            Log.e("BlockListManager/fetchBlockList failed to fetch blocklist", e);
        });
    }

    @AnyThread
    public Observable<Boolean> blockContact(@NonNull UserId userId) {
        MutableObservable<Boolean> resultObservable = new MutableObservable<>();
        privacyListApi.blockUsers(Collections.singleton(userId)).onResponse(result -> {
            if (result == null || !result) {
                resultObservable.setResponse(false);
                return;
            }
            contactsDb.addUserToBlockList(userId);
            resultObservable.setResponse(true);
            notifyBlockListChanged();
        }).onError(resultObservable::setException);
        return resultObservable;
    }

    @AnyThread
    public Observable<Boolean> unblockContact(@NonNull UserId userId) {
        MutableObservable<Boolean> resultObservable = new MutableObservable<>();
        privacyListApi.unblockUsers(Collections.singleton(userId)).onResponse(result -> {
            if (result == null || !result) {
                resultObservable.setResponse(false);
                return;
            }
            contactsDb.removeUserFromBlockList(userId);
            resultObservable.setResponse(true);
            notifyBlockListChanged();
        }).onError(resultObservable::setException);
        return resultObservable;
    }
}
