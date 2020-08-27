package com.halloapp.ui.privacy;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.contacts.Contact;
import com.halloapp.id.UserId;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.privacy.FeedPrivacyManager;
import com.halloapp.ui.profile.ProfileViewModel;
import com.halloapp.util.BgWorkers;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.util.Collections;

public class HideFuturePostsViewModel extends ViewModel {

    private BgWorkers bgWorkers;
    private FeedPrivacyManager feedPrivacyManager;

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
                hideResult.postValue(feedPrivacyManager.updateFeedPrivacy(currentPrivacy.activeList, null, Collections.singletonList(contact.userId)));
                hiding.postValue(null);
            } else if (PrivacyList.Type.EXCEPT.equals(currentPrivacy.activeList)) {
                hideResult.postValue(feedPrivacyManager.updateFeedPrivacy(currentPrivacy.activeList, Collections.singletonList(contact.userId), null));
                hiding.postValue(null);
            } else {
                hideResult.postValue(feedPrivacyManager.updateFeedPrivacy(PrivacyList.Type.EXCEPT, Collections.singletonList(contact.userId), null));
                hiding.postValue(null);
            }
        });
        return hideResult;
    }
}
