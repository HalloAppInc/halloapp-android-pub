package com.halloapp.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.content.SeenReceipt;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.registration.CheckRegistration;
import com.halloapp.util.ComputableLiveData;

import java.util.Collection;

public class MainViewModel extends AndroidViewModel {

    final ComputableLiveData<Integer> unseenChatsCount;
    final ComputableLiveData<Integer> unseenGroupsCount;
    final ComputableLiveData<CheckRegistration.CheckResult> registrationStatus;

    private final Me me;
    private final ContentDb contentDb;
    private final Preferences preferences;

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onPostAdded(@NonNull Post post) {
            unseenGroupsCount.invalidate();
        }

        @Override
        public void onPostRetracted(@NonNull Post post) {
            unseenGroupsCount.invalidate();
        }

        @Override
        public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId) {
            unseenGroupsCount.invalidate();
        }

        public void onMessageAdded(@NonNull Message message) {
            if (message.isIncoming()) {
                unseenChatsCount.invalidate();
            }
        }

        public void onChatSeen(@NonNull ChatId chatId, @NonNull Collection<SeenReceipt> seenReceipts) {
            unseenChatsCount.invalidate();
        }

        public void onChatDeleted(@NonNull ChatId chatId) {
            if (chatId instanceof GroupId) {
                unseenGroupsCount.invalidate();
            } else {
                unseenChatsCount.invalidate();
            }
        }
    };

    public MainViewModel(@NonNull Application application) {
        super(application);

        me = Me.getInstance();
        contentDb = ContentDb.getInstance();
        preferences = Preferences.getInstance();
        contentDb.addObserver(contentObserver);

        unseenChatsCount = new ComputableLiveData<Integer>() {
            @Override
            protected Integer compute() {
                return contentDb.getUnseenChatsCount();
            }
        };
        unseenGroupsCount = new ComputableLiveData<Integer>() {
            @Override
            protected Integer compute() {
                return contentDb.getUnseenGroups();
            }
        };
        registrationStatus = new ComputableLiveData<CheckRegistration.CheckResult>() {
            @Override
            protected CheckRegistration.CheckResult compute() {
                return CheckRegistration.checkRegistration(me, preferences);
            }
        };
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
    }
}
