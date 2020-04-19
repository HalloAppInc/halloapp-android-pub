package com.halloapp.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.halloapp.content.ContentDb;
import com.halloapp.content.Message;
import com.halloapp.content.SeenReceipt;
import com.halloapp.util.ComputableLiveData;

import java.util.Collection;

public class MainViewModel extends AndroidViewModel {

    final ComputableLiveData<Integer> unseenChatsCount;

    private final ContentDb contentDb;

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        public void onMessageAdded(@NonNull Message message) {
            if (message.isIncoming()) {
                unseenChatsCount.invalidate();
            }
        }

        public void onChatSeen(@NonNull String chatId, @NonNull Collection<SeenReceipt> seenReceipts) {
            unseenChatsCount.invalidate();
        }

        public void onChatDeleted(@NonNull String chatId) {
            unseenChatsCount.invalidate();
        }
    };

    public MainViewModel(@NonNull Application application) {
        super(application);

        contentDb = ContentDb.getInstance(application);
        contentDb.addObserver(contentObserver);

        unseenChatsCount = new ComputableLiveData<Integer>() {
            @Override
            protected Integer compute() {
                return contentDb.getUnseenChatsCount();
            }
        };
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
    }
}
