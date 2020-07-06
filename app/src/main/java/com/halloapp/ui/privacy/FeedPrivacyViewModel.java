package com.halloapp.ui.privacy;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.contacts.UserId;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.PrivacyList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FeedPrivacyViewModel extends AndroidViewModel {

    private BgWorkers bgWorkers;
    private Connection connection;

    final ComputableLiveData<FeedPrivacy> feedPrivacy;

    public FeedPrivacyViewModel(@NonNull Application application) {
        super(application);

        bgWorkers = BgWorkers.getInstance();
        connection = Connection.getInstance();

        feedPrivacy = new ComputableLiveData<FeedPrivacy>() {
            @Override
            protected FeedPrivacy compute() {
                try {
                    return connection.getFeedPrivacy().get();
                } catch (ExecutionException | InterruptedException e) {
                    Log.e("FeedPrivacyViewModel failed to get feed privacy", e);
                }
                return null;
            }
        };
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
        FeedPrivacy privacy = feedPrivacy.getLiveData().getValue();
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

    public LiveData<Boolean> savePrivacy(@PrivacyList.Type String newSetting, @NonNull List<UserId> userIds) {
        MutableLiveData<Boolean> savingLiveData = new MutableLiveData<>();
        FeedPrivacy privacy = feedPrivacy.getLiveData().getValue();
        if (privacy == null) {
            savingLiveData.setValue(Boolean.TRUE);
            return savingLiveData;
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
            if (addedUsers.isEmpty() && deletedUsers.isEmpty()) {
                savingLiveData.setValue(Boolean.TRUE);
                return savingLiveData;
            }
        }
        bgWorkers.execute(() -> {
            boolean result = false;
            try {
                result = connection.setFeedPrivacy(newSetting, addedUsers, deletedUsers).get();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("FeedPrivacyViewModel/savePrivacy failed", e);
                savingLiveData.setValue(Boolean.FALSE);
            }
            savingLiveData.postValue(result);
        });
        return savingLiveData;
    }

}
