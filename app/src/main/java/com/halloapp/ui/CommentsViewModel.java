package com.halloapp.ui;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.content.Comment;
import com.halloapp.content.CommentsDataSource;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Mention;
import com.halloapp.content.Post;
import com.halloapp.util.ComputableLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

class CommentsViewModel extends ViewModel {

    final LiveData<PagedList<Comment>> commentList;
    final ComputableLiveData<Long> lastSeenCommentRowId;
    final ComputableLiveData<List<Contact>> mentionableContacts;
    final MutableLiveData<Post> post = new MutableLiveData<>();
    final MutableLiveData<Contact> replyContact = new MutableLiveData<>();
    final MutableLiveData<Boolean> postDeleted = new MutableLiveData<>();

    private final ContentDb contentDb = ContentDb.getInstance();
    private final ContactsDb contactsDb = ContactsDb.getInstance();

    private final String postId;
    private final CommentsDataSource.Factory dataSourceFactory;

    private LoadUserTask loadUserTask;

    private Observer<Post> postObserver;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onPostRetracted(@NonNull Post post) {
            if (CommentsViewModel.this.postId.equals(post.id)) {
                postDeleted.postValue(true);
            }
        }

        @Override
        public void onCommentAdded(@NonNull Comment comment) {
            if (CommentsViewModel.this.postId.equals(comment.postId)) {
                Post parentPost = comment.getParentPost();
                if (parentPost != null) {
                    contentDb.setCommentsSeen(comment.postId);
                }
                invalidateDataSource();
            }
        }

        @Override
        public void onCommentUpdated(@NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
            if (CommentsViewModel.this.postId.equals(postId)) {
                invalidateDataSource();
            }
        }

        @Override
        public void onCommentRetracted(@NonNull Comment comment) {
            if (CommentsViewModel.this.postId.equals(comment.postId)) {
                invalidateDataSource();
            }
        }

        @Override
        public void onFeedCleanup() {
            invalidateDataSource();
        }

        private void invalidateDataSource() {
            mainHandler.post(dataSourceFactory::invalidateLatestDataSource);
        }
    };

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsChanged() {
            mentionableContacts.invalidate();
        }
    };

    private CommentsViewModel(@NonNull String postId) {
        this.postId = postId;

        contentDb.addObserver(contentObserver);

        lastSeenCommentRowId = new ComputableLiveData<Long>() {
            @Override
            protected Long compute() {
                long rowId = contentDb.getLastSeenCommentRowId(postId);
                contentDb.setCommentsSeen(postId);
                return rowId;
            }
        };

        dataSourceFactory = new CommentsDataSource.Factory(contentDb, postId);
        commentList = new LivePagedListBuilder<>(dataSourceFactory, new PagedList.Config.Builder().setPageSize(50).setEnablePlaceholders(false).build()).build();
        mentionableContacts = new ComputableLiveData<List<Contact>>() {
            @Override
            protected List<Contact> compute() {
                HashSet<UserId> contactSet = new HashSet<>();
                HashMap<UserId, String> mentionSet = new HashMap<>();
                Post parentPost = post.getValue();
                if (parentPost != null) {
                    // Allow mentioning every mention from the post
                    for (Mention mention : parentPost.mentions) {
                        if (!mention.userId.isMe()) {
                            mentionSet.put(mention.userId, mention.fallbackName);
                            contactSet.add(mention.userId);
                        }
                    }
                    if (!parentPost.senderUserId.isMe()) {
                        // Allow mentioning poster
                        contactSet.add(parentPost.senderUserId);
                    } else {
                        // Otherwise we can mention everyone in our friends since they should be able to see our post
                        List<Contact> friends = contactsDb.getFriends();
                        for(Contact contact : friends) {
                            contactSet.add(contact.userId);
                        }
                    }
                }
                // Allow mentioning everyone who has commented on the post
                PagedList<Comment> comments = commentList.getValue();
                if (comments != null) {
                    for (Comment comment : comments) {
                        if (!comment.commentSenderUserId.isMe()) {
                            contactSet.add(comment.commentSenderUserId);
                        }
                    }
                }
                List<Contact> contactList = new ArrayList<>();
                for (UserId userId : contactSet) {
                    Contact contact = contactsDb.getContact(userId);
                    String mentionName = mentionSet.get(userId);
                    if (!TextUtils.isEmpty(mentionName)) {
                        contact.fallbackName = mentionName;
                    }
                    contactList.add(contact);
                }
                Contact.sort(contactList);
                return contactList;
            }
        };
        postObserver = post -> mentionableContacts.invalidate();
        post.observeForever(postObserver);
        contactsDb.addObserver(contactsObserver);
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
        contactsDb.removeObserver(contactsObserver);
        post.removeObserver(postObserver);
    }

    void loadReplyUser(@NonNull UserId userId) {
        if (loadUserTask != null) {
            loadUserTask.cancel(true);
        }

        loadUserTask = new LoadUserTask(userId, replyContact);
        loadUserTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void resetReplyUser() {
        if (loadUserTask != null) {
            loadUserTask.cancel(true);
        }
        replyContact.setValue(null);
    }

    void loadPost(@NonNull String postId) {
        new LoadPostTask(postId, post).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    static class LoadUserTask extends AsyncTask<Void, Void, Contact> {

        private final UserId userId;
        private final MutableLiveData<Contact> contact;

        LoadUserTask(@NonNull UserId userId, @NonNull MutableLiveData<Contact> contact) {
            this.userId = userId;
            this.contact = contact;
        }

        @Override
        protected Contact doInBackground(Void... voids) {
            return ContactsDb.getInstance().getContact(userId);
        }

        @Override
        protected void onPostExecute(final Contact contact) {
            this.contact.postValue(contact);
        }
    }

    static class LoadPostTask extends AsyncTask<Void, Void, Post> {

        private final String postId;
        private final MutableLiveData<Post> post;

        LoadPostTask(@NonNull String postId, @NonNull MutableLiveData<Post> post) {
            this.postId = postId;
            this.post = post;
        }

        @Override
        protected Post doInBackground(Void... voids) {
            return ContentDb.getInstance().getPost(postId);
        }

        @Override
        protected void onPostExecute(final Post post) {
            this.post.postValue(post);
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final String postId;

        Factory(@NonNull String postId) {
            this.postId = postId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CommentsViewModel.class)) {
                //noinspection unchecked
                return (T) new CommentsViewModel(postId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}

