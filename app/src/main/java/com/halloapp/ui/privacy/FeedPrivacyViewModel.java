package com.halloapp.ui.privacy;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.Preferences;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.privacy.PrivacyList;
import com.halloapp.xmpp.privacy.PrivacyListApi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FeedPrivacyViewModel extends AndroidViewModel {

    private Connection connection;
    private ContactsDb contactsDb;
    private Preferences preferences;
    private PrivacyListApi privacyListApi;

    private MutableLiveData<FeedPrivacy> feedPrivacyLiveData;

    public FeedPrivacyViewModel(@NonNull Application application) {
        super(application);

        connection = Connection.getInstance();
        contactsDb = ContactsDb.getInstance(application);
        preferences = Preferences.getInstance(application);

        privacyListApi = new PrivacyListApi(connection);
    }

    @NonNull
    public LiveData<FeedPrivacy> getFeedPrivacy() {
        if (feedPrivacyLiveData == null) {
            feedPrivacyLiveData = new MutableLiveData<>();
            privacyListApi.getFeedPrivacy().onResponse(result -> {
                feedPrivacyLiveData.postValue(result);
                preferences.setFeedPrivacyActiveList(result.activeList);
                contactsDb.setFeedExclusionList(result.exceptList);
                contactsDb.setFeedShareList(result.onlyList);
            }).onError(exception -> {
                feedPrivacyLiveData.postValue(null);
            });
        }
        return feedPrivacyLiveData;
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

    public boolean hasChanges(@PrivacyList.Type String newSetting, @NonNull List<UserId> userIds) {
        FeedPrivacy privacy = feedPrivacyLiveData.getValue();
        if (privacy == null) {
            return false;
        }
        List<UserId> addedUsers = new ArrayList<>();
        List<UserId> deletedUsers = new ArrayList<>();
        switch (newSetting) {
            case PrivacyList.Type.ALL:
                break;
            case PrivacyList.Type.EXCEPT:
                diffList(privacy.exceptList, userIds, addedUsers, deletedUsers);
                break;
            case PrivacyList.Type.ONLY:
                diffList(privacy.onlyList, userIds, addedUsers, deletedUsers);
                break;
        }
        if (newSetting.equals(privacy.activeList)) {
            return !addedUsers.isEmpty() || !deletedUsers.isEmpty();
        }
        return true;
    }

    @NonNull
    public LiveData<Boolean> savePrivacy(@PrivacyList.Type String newSetting, @NonNull List<UserId> userIds) {
        MutableLiveData<Boolean> savingLiveData = new MutableLiveData<>();
        FeedPrivacy feedPrivacy = feedPrivacyLiveData.getValue();
        if (feedPrivacy == null) {
            savingLiveData.setValue(Boolean.TRUE);
            return savingLiveData;
        }
        List<UserId> addedUsers = new ArrayList<>();
        List<UserId> deletedUsers = new ArrayList<>();
        switch (newSetting) {
            case PrivacyList.Type.ALL:
                break;
            case PrivacyList.Type.EXCEPT:
                diffList(feedPrivacy.exceptList, userIds, addedUsers, deletedUsers);
                break;
            case PrivacyList.Type.ONLY:
                diffList(feedPrivacy.onlyList, userIds, addedUsers, deletedUsers);
                break;
        }
        if (newSetting.equals(feedPrivacy.activeList)) {
            if (addedUsers.isEmpty() && deletedUsers.isEmpty()) {
                savingLiveData.setValue(Boolean.TRUE);
                return savingLiveData;
            }
        }
        privacyListApi.setFeedPrivacy(newSetting, addedUsers, deletedUsers)
                .onError(e -> {
                    savingLiveData.postValue(Boolean.FALSE);
                })
                .onResponse(success -> {
                    savingLiveData.postValue(success);
                    preferences.setFeedPrivacyActiveList(feedPrivacy.activeList);
                    contactsDb.setFeedShareList(feedPrivacy.onlyList);
                    switch (newSetting) {
                        case PrivacyList.Type.EXCEPT:
                            contactsDb.setFeedExclusionList(userIds);
                            break;
                        case PrivacyList.Type.ONLY:
                            contactsDb.setFeedShareList(userIds);
                            break;
                    }
                });
        return savingLiveData;
    }

}
