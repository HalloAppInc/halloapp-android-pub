package com.halloapp.ui.privacy;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.halloapp.contacts.Contact;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.privacy.FeedPrivacyManager;
import com.halloapp.util.BgWorkers;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.util.Collections;

public class HideFuturePostsViewModel extends ViewModel {

    private final BgWorkers bgWorkers;
    private final FeedPrivacyManager feedPrivacyManager;

    private final MutableLiveData<Boolean> hiding;

    public HideFuturePostsViewModel() {
        bgWorkers = BgWorkers.getInstance();
        feedPrivacyManager = FeedPrivacyManager.getInstance();

        hiding = new MutableLiveData<>(null);
    }

    public LiveData<Boolean> inProgress() {
        return hiding;
    }

    public LiveData<Boolean> hideContact(Contact contact) {
        hiding.setValue(true);
        MutableLiveData<Boolean> hideResult = new MutableLiveData<>(null);
        bgWorkers.execute(() -> {
            FeedPrivacy currentPrivacy = feedPrivacyManager.getFeedPrivacy();
            if (currentPrivacy == null) {
                hideResult.postValue(false);
                hiding.postValue(null);
                return;
            }
            if (PrivacyList.Type.ONLY.equals(currentPrivacy.activeList)) {
                currentPrivacy.onlyList.remove(contact.userId);
                hideResult.postValue(feedPrivacyManager.updateFeedPrivacy(currentPrivacy.activeList, currentPrivacy.onlyList));
                hiding.postValue(null);
            } else if (PrivacyList.Type.EXCEPT.equals(currentPrivacy.activeList)) {
                if (!currentPrivacy.exceptList.contains(contact.userId)) {
                    currentPrivacy.exceptList.add(contact.userId);
                }
                hideResult.postValue(feedPrivacyManager.updateFeedPrivacy(currentPrivacy.activeList, currentPrivacy.exceptList));
                hiding.postValue(null);
            } else {
                hideResult.postValue(feedPrivacyManager.updateFeedPrivacy(PrivacyList.Type.EXCEPT, Collections.singletonList(contact.userId)));
                hiding.postValue(null);
            }
        });
        return hideResult;
    }
}
