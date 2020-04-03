package com.halloapp.ui;

import android.app.Application;
import android.os.AsyncTask;
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

public class PostDetailsViewModel extends AndroidViewModel {

    final MutableLiveData<Post> post = new MutableLiveData<>();
    final ComputableLiveData<List<Contact>> contactsList;

    private final ContentDb contentDb;
    private final ContactsDb contactsDb;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

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

    private PostDetailsViewModel(@NonNull Application application, @NonNull String postId) {
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
                    Contact contact = contactsDb.getContact(userId);
                    if (contact == null) {
                        contact = new Contact(userId);
                    }
                    contacts.add(contact);
                }
                return contacts;
            }
        };
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
        contactsDb.removeObserver(contactsObserver);
    }

    void loadPost(@NonNull String postId) {
        new LoadPostTask(getApplication(), postId, post).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    static class LoadPostTask extends AsyncTask<Void, Void, Void> {

        private final String postId;
        private final MutableLiveData<Post> post;
        private final Application application;

        LoadPostTask(@NonNull Application application, @NonNull String postId, @NonNull MutableLiveData<Post> post) {
            this.application = application;
            this.postId = postId;
            this.post = post;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            post.postValue(ContentDb.getInstance(application).getPost(UserId.ME, postId));
            return null;
        }
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
            if (modelClass.isAssignableFrom(PostDetailsViewModel.class)) {
                //noinspection unchecked
                return (T) new PostDetailsViewModel(application, postId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
