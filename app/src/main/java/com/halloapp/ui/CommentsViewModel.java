package com.halloapp.ui;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.content.Comment;
import com.halloapp.content.CommentsDataSource;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.util.ComputableLiveData;

class CommentsViewModel extends AndroidViewModel {

    final LiveData<PagedList<Comment>> commentList;
    final ComputableLiveData<Long> lastSeenCommentRowId;
    final MutableLiveData<Post> post = new MutableLiveData<>();
    final MutableLiveData<Contact> replyContact = new MutableLiveData<>();
    final MutableLiveData<Boolean> postDeleted = new MutableLiveData<>();

    private final ContentDb contentDb;
    private final UserId postSenderUserId;
    private final String postId;
    private final CommentsDataSource.Factory dataSourceFactory;

    private LoadUserTask loadUserTask;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onPostRetracted(@NonNull UserId senderUserId, @NonNull String postId) {
            if (CommentsViewModel.this.postSenderUserId.equals(senderUserId) && CommentsViewModel.this.postId.equals(postId)) {
                postDeleted.postValue(true);
            }
        }

        @Override
        public void onCommentAdded(@NonNull Comment comment) {
            if (CommentsViewModel.this.postSenderUserId.equals(comment.postSenderUserId) && CommentsViewModel.this.postId.equals(comment.postId)) {
                contentDb.setCommentsSeen(comment.postSenderUserId, comment.postId);
                invalidateDataSource();
            }
        }

        @Override
        public void onCommentUpdated(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
            if (CommentsViewModel.this.postSenderUserId.equals(postSenderUserId) && CommentsViewModel.this.postId.equals(postId)) {
                invalidateDataSource();
            }
        }

        @Override
        public void onCommentRetracted(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
            if (CommentsViewModel.this.postSenderUserId.equals(postSenderUserId) && CommentsViewModel.this.postId.equals(postId)) {
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

    private CommentsViewModel(@NonNull Application application, @NonNull UserId postSenderUserId, @NonNull String postId) {
        super(application);

        this.postSenderUserId = postSenderUserId;
        this.postId = postId;

        contentDb = ContentDb.getInstance(application);
        contentDb.addObserver(contentObserver);

        lastSeenCommentRowId = new ComputableLiveData<Long>() {
            @Override
            protected Long compute() {
                long rowId = contentDb.getLastSeenCommentRowId(postSenderUserId, postId);
                contentDb.setCommentsSeen(postSenderUserId, postId);
                return rowId;
            }
        };

        dataSourceFactory = new CommentsDataSource.Factory(contentDb, postSenderUserId, postId);
        commentList = new LivePagedListBuilder<>(dataSourceFactory, new PagedList.Config.Builder().setPageSize(50).setEnablePlaceholders(false).build()).build();
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
    }

    void loadReplyUser(@NonNull UserId userId) {
        if (loadUserTask != null) {
            loadUserTask.cancel(true);
        }

        loadUserTask = new LoadUserTask(getApplication(), userId, replyContact);
        loadUserTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void resetReplyUser() {
        if (loadUserTask != null) {
            loadUserTask.cancel(true);
        }
        replyContact.setValue(null);
    }

    void loadPost(@NonNull UserId userId, @NonNull String postId) {
        new LoadPostTask(getApplication(), userId, postId, post).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    static class LoadUserTask extends AsyncTask<Void, Void, Contact> {

        private final UserId userId;
        private final MutableLiveData<Contact> contact;
        private final Application application;

        LoadUserTask(@NonNull Application application, @NonNull UserId userId, @NonNull MutableLiveData<Contact> contact) {
            this.application = application;
            this.userId = userId;
            this.contact = contact;
        }

        @Override
        protected Contact doInBackground(Void... voids) {
            return ContactsDb.getInstance(application).getContact(userId);
        }

        @Override
        protected void onPostExecute(final Contact contact) {
            this.contact.postValue(contact);
        }
    }

    static class LoadPostTask extends AsyncTask<Void, Void, Post> {

        private final UserId userId;
        private final String postId;
        private final MutableLiveData<Post> post;
        private final Application application;

        LoadPostTask(@NonNull Application application, @NonNull UserId userId, @NonNull String postId, @NonNull MutableLiveData<Post> post) {
            this.application = application;
            this.userId = userId;
            this.postId = postId;
            this.post = post;
        }

        @Override
        protected Post doInBackground(Void... voids) {
            return ContentDb.getInstance(application).getPost(userId, postId);
        }

        @Override
        protected void onPostExecute(final Post post) {
            this.post.postValue(post);
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final UserId postSenderUserId;
        private final String postId;

        Factory(@NonNull Application application, @NonNull UserId postSenderUserId, @NonNull String postId) {
            this.application = application;
            this.postSenderUserId = postSenderUserId;
            this.postId = postId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CommentsViewModel.class)) {
                //noinspection unchecked
                return (T) new CommentsViewModel(application, postSenderUserId, postId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}

