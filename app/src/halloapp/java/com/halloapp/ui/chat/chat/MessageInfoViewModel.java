package com.halloapp.ui.chat.chat;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.content.ContentDb;
import com.halloapp.content.MessageDeliveryState;
import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;
import com.halloapp.util.ComputableLiveData;

import java.util.List;

public class MessageInfoViewModel extends AndroidViewModel {

    private final ContentDb contentDb;

    private ComputableLiveData<List<MessageDeliveryState>> messageDeliveryData;

    private String messageId;

    private final ContentDb.Observer observer = new ContentDb.DefaultObserver() {

        @Override
        public void onOutgoingMessageDelivered(@NonNull ChatId chatId, @NonNull UserId recipientUserId, @NonNull String messageId) {
            if (messageId.equals(MessageInfoViewModel.this.messageId)) {
                messageDeliveryData.invalidate();
            }
        }

        @Override
        public void onOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId seenByUserId, @NonNull String messageId) {
            if (messageId.equals(MessageInfoViewModel.this.messageId)) {
                messageDeliveryData.invalidate();
            }
        }
    };

    public MessageInfoViewModel(@NonNull Application application, @NonNull String messageId) {
        super(application);

        this.messageId = messageId;

        contentDb = ContentDb.getInstance();
        contentDb.addObserver(observer);

        messageDeliveryData = new ComputableLiveData<List<MessageDeliveryState>>() {
            @Override
            protected List<MessageDeliveryState> compute() {
                List<MessageDeliveryState> state = contentDb.getOutgoingMessageDeliveryStates(messageId);
                return state;
            }
        };
    }

    @NonNull
    public LiveData<List<MessageDeliveryState>> getMessageDeliveryState() {
        return messageDeliveryData.getLiveData();
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(observer);
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final String messageId;

        Factory(@NonNull Application application, @NonNull String messageId) {
            this.application = application;
            this.messageId = messageId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(MessageInfoViewModel.class)) {
                //noinspection unchecked
                return (T) new MessageInfoViewModel(application, messageId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
