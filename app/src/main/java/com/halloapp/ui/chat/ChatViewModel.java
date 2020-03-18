package com.halloapp.ui.chat;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ComputableLiveData;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.posts.ChatMessage;
import com.halloapp.posts.MessagesDataSource;
import com.halloapp.posts.PostsDb;
import com.halloapp.util.Preconditions;

public class ChatViewModel extends AndroidViewModel {

    final LiveData<PagedList<ChatMessage>> messageList;
    final ComputableLiveData<Contact> contact;

    private final PostsDb postsDb;
    private final String chatId;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final PostsDb.Observer postsObserver = new PostsDb.DefaultObserver() {

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

        postsDb = PostsDb.getInstance(application);
        postsDb.addObserver(postsObserver);

        final MessagesDataSource.Factory dataSourceFactory = new MessagesDataSource.Factory(postsDb, chatId);
        messageList = new LivePagedListBuilder<>(dataSourceFactory, 50).build();

        contact = new ComputableLiveData<Contact>() {
            @Override
            protected Contact compute() {
                final Contact contact = ContactsDb.getInstance(application).getContact(new UserId(chatId));
                return contact == null ? new Contact(new UserId(chatId)) : contact;
            }
        };
    }

    @Override
    protected void onCleared() {
        postsDb.removeObserver(postsObserver);
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