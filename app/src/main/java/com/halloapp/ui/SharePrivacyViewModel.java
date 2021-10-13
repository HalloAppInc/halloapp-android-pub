package com.halloapp.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.privacy.FeedPrivacyManager;
import com.halloapp.util.ComputableLiveData;

import java.text.Collator;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SharePrivacyViewModel extends ViewModel {

    private final ComputableLiveData<List<Chat>> groupsList;
    private ComputableLiveData<FeedPrivacy> feedPrivacyLiveData;

    private final ContentDb contentDb = ContentDb.getInstance();
    private final FeedPrivacyManager feedPrivacyManager = FeedPrivacyManager.getInstance();

    private final FeedPrivacyManager.Observer feedPrivacyObserver = () -> {
        feedPrivacyLiveData.invalidate();
    };

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        public void onGroupChatAdded(@NonNull GroupId groupId) {
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
        groupsList = new ComputableLiveData<List<Chat>>() {
            @Override
            protected List<Chat> compute() {
                final List<Chat> chats = contentDb.getGroups();
                final Collator collator = Collator.getInstance(Locale.getDefault());
                Collections.sort(chats, (obj1, obj2) -> {
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
                return chats;
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
    public LiveData<List<Chat>> getGroupList() {
        return groupsList.getLiveData();
    }
}
