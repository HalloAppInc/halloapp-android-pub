package com.halloapp.ui.privacy;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.id.UserId;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.privacy.FeedPrivacyManager;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FeedPrivacyViewModel extends AndroidViewModel {

    private final BgWorkers bgWorkers;
    private final FeedPrivacyManager feedPrivacyManager;

    private final ComputableLiveData<FeedPrivacy> feedPrivacyLiveData;

    private final FeedPrivacyManager.Observer feedPrivacyObserver = new FeedPrivacyManager.Observer() {
        @Override
        public void onFeedPrivacyChanged() {
            feedPrivacyLiveData.invalidate();
        }
    };

    public FeedPrivacyViewModel(@NonNull Application application) {
        super(application);

        bgWorkers = BgWorkers.getInstance();
        feedPrivacyManager = FeedPrivacyManager.getInstance();

        feedPrivacyManager.addObserver(feedPrivacyObserver);

        feedPrivacyLiveData = new ComputableLiveData<FeedPrivacy>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected FeedPrivacy compute() {
                return feedPrivacyManager.getFeedPrivacy();
            }
        };
    }

    @NonNull
    public LiveData<FeedPrivacy> getFeedPrivacy() {
        return feedPrivacyLiveData.getLiveData();
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
        FeedPrivacy privacy = feedPrivacyLiveData.getLiveData().getValue();
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
        bgWorkers.execute(() -> savingLiveData.postValue(feedPrivacyManager.updateFeedPrivacy(newSetting, new ArrayList<>(userIds))));
        return savingLiveData;
    }

    @Override
    protected void onCleared() {
        feedPrivacyManager.removeObserver(feedPrivacyObserver);
    }
}
