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
import com.halloapp.xmpp.privacy.PrivacyList;
import com.halloapp.xmpp.privacy.PrivacyListApi;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.util.ArrayList;
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
    public void fetchInitialFeedPrivacy() {
        String cachedActiveList = preferences.getFeedPrivacyActiveList();
        if (PrivacyList.Type.INVALID.equals(cachedActiveList)) {
            fetchFeedPrivacy();
        }
    }

    @AnyThread
    public void fetchFeedPrivacy() {
        privacyListApi.getFeedPrivacy().onResponse(this::saveFeedPrivacy).onError(e -> {
            Log.e("FeedPrivacyManager/fetchFeedPrivacy failed to fetch privacy settings", e);
        });
    }

    private void saveFeedPrivacy(@NonNull FeedPrivacy feedPrivacy) {
        contactsDb.setFeedExclusionList(feedPrivacy.exceptList);
        contactsDb.setFeedShareList(feedPrivacy.onlyList);
        preferences.setFeedPrivacyActiveList(feedPrivacy.activeList);
        notifyFeedPrivacyChanged();
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

    @WorkerThread
    public boolean updateFeedPrivacy(@PrivacyList.Type String selectedList, @NonNull List<UserId> userIds) {
        return updateFeedPrivacy(selectedList, userIds, true);
    }

    @WorkerThread
    public boolean updateFeedPrivacy(@PrivacyList.Type String selectedList, @NonNull List<UserId> userIds, boolean allowRetry) {
        FeedPrivacy feedPrivacy = getFeedPrivacy();
        if (feedPrivacy == null) {
            Log.e("FeedPrivacyManager/updateFeedPrivacy failed to update, null current feed privacy?");
            return false;
        }
        List<UserId> addedUsers = new ArrayList<>();
        List<UserId> deletedUsers = new ArrayList<>();
        switch (selectedList) {
            case PrivacyList.Type.ALL:
                break;
            case PrivacyList.Type.EXCEPT:
                diffList(feedPrivacy.exceptList, userIds, addedUsers, deletedUsers);
                break;
            case PrivacyList.Type.ONLY:
                diffList(feedPrivacy.onlyList, userIds, addedUsers, deletedUsers);
                break;
        }
        if (selectedList.equals(feedPrivacy.activeList)) {
            if (addedUsers.isEmpty() && deletedUsers.isEmpty()) {
                return true;
            }
        }

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
        } catch (ObservableErrorException e) {
            if (allowRetry) {
                Throwable cause = e.getCause();
                if (cause instanceof IqErrorException) {
                    IqErrorException iqError = (IqErrorException) cause;
                    if ("hash_mismatch".equals(iqError.getReason())) {
                        Log.i("FeedPrivacyManager/updateFeedPrivacy: hash mismatch, retrying");
                        if (!refetchFeedPrivacySync()) {
                            return false;
                        }
                        return updateFeedPrivacy(selectedList, userIds, false);
                    }
                }
            }
            Log.e("FeedPrivacyManager/updateFeedPrivacy: failed to update feed privacy", e);
        } catch (InterruptedException e) {
            Log.e("FeedPrivacyManager/updateFeedPrivacy: failed to update feed privacy interrupted", e);
        }
        return false;
    }

    @WorkerThread
    private boolean refetchFeedPrivacySync() {
        FeedPrivacy feedPrivacy;
        try {
            feedPrivacy = privacyListApi.getFeedPrivacy().await();
        } catch (ObservableErrorException | InterruptedException e) {
            Log.e("FeedPrivacyManager/retryUpdateFeedPrivacyHash failed to fetch feed privacy");
            return false;
        }
        saveFeedPrivacy(feedPrivacy);
        return true;
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
