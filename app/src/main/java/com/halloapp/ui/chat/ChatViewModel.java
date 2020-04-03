package com.halloapp.ui.chat;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Message;
import com.halloapp.content.MessagesDataSource;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Preconditions;

import java.util.concurrent.atomic.AtomicBoolean;

public class ChatViewModel extends AndroidViewModel {

    private final String chatId;

    final LiveData<PagedList<Message>> messageList;
    final ComputableLiveData<Contact> contact;
    final ComputableLiveData<Chat> chat;

    private final ContentDb contentDb;
    private final AtomicBoolean pendingOutgoing = new AtomicBoolean(false);
    private final AtomicBoolean pendingIncoming = new AtomicBoolean(false);

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onMessageAdded(@NonNull Message message) {
            if (ChatViewModel.this.chatId.equals(message.chatId)) {
                if (message.isOutgoing()) {
                    pendingOutgoing.set(true);
                    mainHandler.post(() -> reloadPostsAt(Long.MAX_VALUE));
                } else {
                    pendingIncoming.set(true);
                    invalidateMessages();
                }
            }
        }

        public void onMessageRetracted(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
            if (ChatViewModel.this.chatId.equals(chatId)) {
                invalidateMessages();
            }
        }

        public void onMessageUpdated(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
            if (ChatViewModel.this.chatId.equals(chatId)) {
                invalidateMessages();
            }
        }

        public void onOutgoingMessageDelivered(@NonNull String chatId, @NonNull UserId recipientUserId, @NonNull String messageId) {
            if (ChatViewModel.this.chatId.equals(chatId)) {
                invalidateMessages();
            }
        }

        public void onOutgoingMessageSeen(@NonNull String chatId, @NonNull UserId seenByUserId, @NonNull String messageId) {
            if (ChatViewModel.this.chatId.equals(chatId)) {
                invalidateMessages();
            }
        }

        public void onChatDeleted(@NonNull String chatId) {
            if (ChatViewModel.this.chatId.equals(chatId)) {
                invalidateMessages();
            }
        }

        @Override
        public void onDbCreated() {
        }

        private void invalidateMessages() {
            mainHandler.post(() -> Preconditions.checkNotNull(messageList.getValue()).getDataSource().invalidate());
        }
    };

    public ChatViewModel(@NonNull Application application, @NonNull String chatId) {
        super(application);

        this.chatId = chatId;

        contentDb = ContentDb.getInstance(application);
        contentDb.addObserver(contentObserver);

        final MessagesDataSource.Factory dataSourceFactory = new MessagesDataSource.Factory(contentDb, chatId);
        messageList = new LivePagedListBuilder<>(dataSourceFactory, 50).build();

        contact = new ComputableLiveData<Contact>() {
            @Override
            protected Contact compute() {
                final Contact contact = ContactsDb.getInstance(application).getContact(new UserId(chatId));
                return contact == null ? new Contact(new UserId(chatId)) : contact;
            }
        };

        chat = new ComputableLiveData<Chat>() {
            @Override
            protected Chat compute() {
                return contentDb.getChat(chatId);
            }
        };
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
    }

    boolean checkPendingOutgoing() {
        return pendingOutgoing.compareAndSet(true, false);
    }

    boolean checkPendingIncoming() {
        return pendingIncoming.compareAndSet(true, false);
    }

    void reloadPostsAt(long timestamp) {
        final PagedList pagedList = messageList.getValue();
        if (pagedList != null) {
            ((MessagesDataSource)pagedList.getDataSource()).reloadAt(timestamp);
        }
    }


    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final String chatId;

        Factory(@NonNull Application application, @NonNull String chatId) {
            this.application = application;
            this.chatId = chatId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ChatViewModel.class)) {
                //noinspection unchecked
                return (T) new ChatViewModel(application, chatId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}