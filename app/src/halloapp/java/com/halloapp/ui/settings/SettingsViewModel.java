package com.halloapp.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.halloapp.id.UserId;
import com.halloapp.privacy.BlockListManager;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.privacy.FeedPrivacyManager;
import com.halloapp.util.ComputableLiveData;

import java.util.List;

public class SettingsViewModel extends ViewModel {

    private final BlockListManager blockListManager = BlockListManager.getInstance();
    private final FeedPrivacyManager feedPrivacyManager = FeedPrivacyManager.getInstance();

    private ComputableLiveData<FeedPrivacy> feedPrivacyLiveData;
    private ComputableLiveData<List<UserId>> blockListLiveData;

    private final BlockListManager.Observer blockListObserver = () -> {
        blockListLiveData.invalidate();
    };

    private final FeedPrivacyManager.Observer feedPrivacyObserver = () -> {
        feedPrivacyLiveData.invalidate();
    };

    public SettingsViewModel() {
        blockListLiveData = new ComputableLiveData<List<UserId>>() {
            @Override
            protected List<UserId> compute() {
                return blockListManager.getBlockList();
            }
        };
        blockListManager.addObserver(blockListObserver);
        feedPrivacyLiveData = new ComputableLiveData<FeedPrivacy>() {
            @Override
            protected FeedPrivacy compute() {
                return feedPrivacyManager.getFeedPrivacy();
            }
        };
        feedPrivacyManager.addObserver(feedPrivacyObserver);
    }

    public LiveData<FeedPrivacy> getFeedPrivacy() {
        return feedPrivacyLiveData.getLiveData();
    }

    public LiveData<List<UserId>> getBlockList() {
        return blockListLiveData.getLiveData();
    }

    @Override
    protected void onCleared() {
        blockListManager.removeObserver(blockListObserver);
        feedPrivacyManager.removeObserver(feedPrivacyObserver);
    }
}
