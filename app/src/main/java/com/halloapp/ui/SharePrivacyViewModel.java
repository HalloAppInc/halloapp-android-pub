package com.halloapp.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.privacy.FeedPrivacyManager;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SharePrivacyViewModel extends ViewModel {

    private final ComputableLiveData<List<Group>> groupsList;
    private ComputableLiveData<FeedPrivacy> feedPrivacyLiveData;

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final ContentDb contentDb = ContentDb.getInstance();
    private final FeedPrivacyManager feedPrivacyManager = FeedPrivacyManager.getInstance();

    private final FeedPrivacyManager.Observer feedPrivacyObserver = () -> {
        feedPrivacyLiveData.invalidate();
    };

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        public void onGroupFeedAdded(@NonNull GroupId groupId) {
            invalidateGroups();
        }

        @Override
        public void onGroupMetadataChanged(@NonNull GroupId groupId) {
            invalidateGroups();
        }

        public void onChatDeleted(@NonNull ChatId chatId) {
            invalidateGroups();
        }

        private void invalidateGroups() {
            groupsList.invalidate();
        }
    };

    public SharePrivacyViewModel() {
        contentDb.addObserver(contentObserver);
        groupsList = new ComputableLiveData<List<Group>>() {
            @Override
            protected List<Group> compute() {
                final List<Group> groups = contentDb.getGroups();
                final Collator collator = Collator.getInstance(Locale.getDefault());
                Collections.sort(groups, (obj1, obj2) -> {
                    if (obj2.timestamp == obj1.timestamp) {
                        if (Objects.equals(obj1.name, obj2.name)) {
                            return 0;
                        }
                        if (obj1.name == null) {
                            return 1;
                        }
                        if (obj2.name == null) {
                            return -1;
                        }
                        return collator.compare(obj1.name, obj2.name);
                    }
                    return obj1.timestamp < obj2.timestamp ? 1 : -1;
                });
                return groups;
            }
        };
        feedPrivacyLiveData = new ComputableLiveData<FeedPrivacy>() {
            @Override
            protected FeedPrivacy compute() {
                return feedPrivacyManager.getFeedPrivacy();
            }
        };
        feedPrivacyManager.addObserver(feedPrivacyObserver);
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
        feedPrivacyManager.removeObserver(feedPrivacyObserver);
    }

    public LiveData<FeedPrivacy> getFeedPrivacy() {
        return feedPrivacyLiveData.getLiveData();
    }

    @NonNull
    public LiveData<List<Group>> getGroupList() {
        return groupsList.getLiveData();
    }

    @NonNull
    public LiveData<Boolean> savePrivacy(@PrivacyList.Type String newSetting, @NonNull List<UserId> userIds) {
        MutableLiveData<Boolean> savingLiveData = new MutableLiveData<>();
        bgWorkers.execute(() -> savingLiveData.postValue(feedPrivacyManager.updateFeedPrivacy(newSetting, new ArrayList<>(userIds))));
        return savingLiveData;
    }
}
