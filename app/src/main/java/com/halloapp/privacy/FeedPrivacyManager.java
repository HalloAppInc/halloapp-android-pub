package com.halloapp.privacy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.Preferences;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.privacy.PrivacyList;
import com.halloapp.xmpp.privacy.PrivacyListApi;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeedPrivacyManager {

    private static FeedPrivacyManager instance;

    private final ContactsDb contactsDb;
    private final Preferences preferences;

    private final PrivacyListApi privacyListApi;

    public static FeedPrivacyManager getInstance() {
        if (instance == null) {
            synchronized (FeedPrivacyManager.class) {
                if (instance == null) {
                    instance = new FeedPrivacyManager();
                }
            }
        }
        return instance;
    }

    private final Set<Observer> observers = new HashSet<>();

    public interface Observer {
        void onFeedPrivacyChanged();
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

    private void notifyFeedPrivacyChanged() {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onFeedPrivacyChanged();
            }
        }
    }

    private FeedPrivacyManager() {
        this.contactsDb = ContactsDb.getInstance();
        this.preferences = Preferences.getInstance();

        this.privacyListApi = new PrivacyListApi(Connection.getInstance());
    }

    @WorkerThread
    public void fetchFeedPrivacy() {
        privacyListApi.getFeedPrivacy().onResponse(this::saveFeedPrivacy).onError(e -> {
            Log.e("FeedPrivacyManager/fetchFeedPrivacy failed to fetch privacy settings", e);
        });
    }

    private void saveFeedPrivacy(@NonNull FeedPrivacy feedPrivacy) {
        contactsDb.setFeedExclusionList(feedPrivacy.exceptList);
        contactsDb.setFeedShareList(feedPrivacy.exceptList);
        preferences.setFeedPrivacyActiveList(feedPrivacy.activeList);
        notifyFeedPrivacyChanged();
    }

    @WorkerThread
    public boolean updateFeedPrivacy(@PrivacyList.Type String selectedList, @Nullable List<UserId> addedUsers, @Nullable List<UserId> deletedUsers) {
        try {
            Boolean result = privacyListApi.setFeedPrivacy(selectedList, addedUsers, deletedUsers).await();
            if (result == null || !result) {
                Log.e("FeedPrivacyManager/updateFeedPrivacy: failed to update feed privacy");
                return false;
            }
            preferences.setFeedPrivacyActiveList(selectedList);
            if (PrivacyList.Type.EXCEPT.equals(selectedList)) {
                contactsDb.updateFeedExclusionList(addedUsers, deletedUsers);
            } else if (PrivacyList.Type.ONLY.equals(selectedList)) {
                contactsDb.updateFeedShareList(addedUsers, deletedUsers);
            }
            notifyFeedPrivacyChanged();
            return true;
        } catch (ObservableErrorException | InterruptedException e) {
            Log.e("FeedPrivacyManager/updateFeedPrivacy: failed to update feed privacy", e);
        }
        return false;
    }

    @WorkerThread
    @Nullable
    public FeedPrivacy getFeedPrivacy() {
        String cachedActiveList = preferences.getFeedPrivacyActiveList();
        if (PrivacyList.Type.INVALID.equals(cachedActiveList)) {
            fetchFeedPrivacy();
            return null;
        }
        List<UserId> exceptList = contactsDb.getFeedExclusionList();
        List<UserId> onlyList = contactsDb.getFeedShareList();
        return new FeedPrivacy(cachedActiveList, exceptList, onlyList);
    }
}
