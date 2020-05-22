package com.halloapp.ui;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.util.ComputableLiveData;

import java.util.ArrayList;
import java.util.List;

public class PostSeenByViewModel extends AndroidViewModel {

    final ComputableLiveData<Post> post;
    final ComputableLiveData<List<Contact>> contactsList;
    final MutableLiveData<Boolean> postDeleted = new MutableLiveData<>();

    private final ContentDb contentDb;
    private final ContactsDb contactsDb;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onPostRetracted(@NonNull UserId senderUserId, @NonNull String postId) {
            final Post post = PostSeenByViewModel.this.post.getLiveData().getValue();
            if (post != null && post.senderUserId.equals(senderUserId) && post.id.equals(postId)) {
                postDeleted.postValue(true);
            }
        }

        @Override
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
            invalidateContacts();
        }

        @Override
        public void onFeedCleanup() {
            invalidateContacts();
        }

        private void invalidateContacts() {
            mainHandler.post(contactsList::invalidate);
        }
    };

    private final ContactsDb.Observer contactsObserver = new ContactsDb.Observer() {

        @Override
        public void onContactsChanged() {
            mainHandler.post(contactsList::invalidate);
        }

        @Override
        public void onContactsReset() {
        }
    };

    private PostSeenByViewModel(@NonNull Application application, @NonNull String postId) {
        super(application);

        contentDb = ContentDb.getInstance(application);
        contentDb.addObserver(contentObserver);

        contactsDb = ContactsDb.getInstance(application);
        contactsDb.addObserver(contactsObserver);

        contactsList = new ComputableLiveData<List<Contact>>() {

            @Override
            protected List<Contact> compute() {
                final List<UserId> userIds = contentDb.getPostSeenBy(postId);
                final List<Contact> contacts = new ArrayList<>(userIds.size());
                for (UserId userId : userIds) {
                    contacts.add(contactsDb.getContact(userId));
                }
                return contacts;
            }
        };

        post = new ComputableLiveData<Post>() {
            @Override
            protected Post compute() {
                return ContentDb.getInstance(application).getPost(UserId.ME, postId);
            }
        };
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
        contactsDb.removeObserver(contactsObserver);
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final String postId;

        Factory(@NonNull Application application, @NonNull String postId) {
            this.application = application;
            this.postId = postId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(PostSeenByViewModel.class)) {
                //noinspection unchecked
                return (T) new PostSeenByViewModel(application, postId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
