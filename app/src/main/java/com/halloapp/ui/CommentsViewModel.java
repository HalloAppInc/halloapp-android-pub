package com.halloapp.ui;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.util.Preconditions;
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
import com.halloapp.posts.Comment;
import com.halloapp.posts.CommentsDataSource;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;

import java.util.Collection;

class CommentsViewModel extends AndroidViewModel {

    final LiveData<PagedList<Comment>> commentList;
    final MutableLiveData<Post> post = new MutableLiveData<>();
    final MutableLiveData<Contact> replyContact = new MutableLiveData<>();

    private final PostsDb postsDb;
    private final UserId postSenderUserId;
    private final String postId;

    private LoadUserTask loadUserTask;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final PostsDb.Observer postsObserver = new PostsDb.Observer() {

        @Override
        public void onPostAdded(@NonNull Post post) {
        }

        @Override
        public void onPostDuplicate(@NonNull Post post) {
        }

        @Override
        public void onPostDeleted(@NonNull UserId senderUserId, @NonNull String postId) {
        }

        @Override
        public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
        }

        @Override
        public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId) {
        }

        @Override
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
        }

        @Override
        public void onCommentAdded(@NonNull Comment comment) {
            if (CommentsViewModel.this.postSenderUserId.equals(comment.postSenderUserId) && CommentsViewModel.this.postId.equals(comment.postId)) {
                postsDb.setCommentsSeen(comment.postSenderUserId, comment.postId);
                invalidateDataSource();
            }
        }

        @Override
        public void onCommentDuplicate(@NonNull Comment comment) {
        }

        @Override
        public void onCommentUpdated(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
            if (CommentsViewModel.this.postSenderUserId.equals(postSenderUserId) && CommentsViewModel.this.postId.equals(postId)) {
                invalidateDataSource();
            }
        }

        @Override
        public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
        }

        @Override
        public void onHistoryAdded(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments) {
        }

        @Override
        public void onPostsCleanup() {
            invalidateDataSource();
        }

        private void invalidateDataSource() {
            mainHandler.post(() -> Preconditions.checkNotNull(commentList.getValue()).getDataSource().invalidate());
        }
    };

    private CommentsViewModel(@NonNull Application application, @NonNull UserId postSenderUserId, @NonNull String postId) {
        super(application);

        this.postSenderUserId = postSenderUserId;
        this.postId = postId;

        postsDb = PostsDb.getInstance(application);
        postsDb.addObserver(postsObserver);

        final CommentsDataSource.Factory dataSourceFactory = new CommentsDataSource.Factory(postsDb, postSenderUserId, postId);
        commentList = new LivePagedListBuilder<>(dataSourceFactory, new PagedList.Config.Builder().setPageSize(50).setEnablePlaceholders(false).build()).build();
    }

    @Override
    protected void onCleared() {
        postsDb.removeObserver(postsObserver);
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
            this.contact.postValue(contact == null ? new Contact(userId) : contact);
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
            return PostsDb.getInstance(application).getPost(userId, postId);
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

