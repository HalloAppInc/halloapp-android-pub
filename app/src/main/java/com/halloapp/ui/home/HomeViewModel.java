package com.halloapp.ui.home;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.util.Preconditions;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.contacts.UserId;
import com.halloapp.posts.Comment;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDataSource;
import com.halloapp.posts.PostsDb;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeViewModel extends AndroidViewModel {

    final LiveData<PagedList<Post>> postList;
    private final PostsDb postsDb;
    private final AtomicBoolean pendingOutgoing = new AtomicBoolean(false);
    private final AtomicBoolean pendingIncoming = new AtomicBoolean(false);

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final PostsDb.Observer postsObserver = new PostsDb.Observer() {
        @Override
        public void onPostAdded(@NonNull Post post) {
            if (post.isOutgoing()) {
                pendingOutgoing.set(true);
            } else {
                pendingIncoming.set(true);
            }
            invalidateDataSource();
        }

        @Override
        public void onPostDuplicate(@NonNull Post post) {
            // do not update model on duplicate post
        }

        @Override
        public void onPostDeleted(@NonNull Post post) {
            invalidateDataSource();
        }

        @Override
        public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
            invalidateDataSource();
        }

        @Override
        public void onCommentAdded(@NonNull Comment comment) {
            if (comment.isIncoming()) {
                invalidateDataSource();
            }
        }

        @Override
        public void onCommentDuplicate(@NonNull Comment comment) {
        }

        @Override
        public void onCommentUpdated(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
        }

        @Override
        public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
            invalidateDataSource();
        }

        @Override
        public void onHistoryAdded(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments) {
            invalidateDataSource();
        }

        private void invalidateDataSource() {
            mainHandler.post(() -> Preconditions.checkNotNull(postList.getValue()).getDataSource().invalidate());
        }
    };

    public HomeViewModel(@NonNull Application application) {
        super(application);

        postsDb = PostsDb.getInstance(application);
        postsDb.addObserver(postsObserver);

        final PostsDataSource.Factory dataSourceFactory = new PostsDataSource.Factory(postsDb, false);
        postList = new LivePagedListBuilder<>(dataSourceFactory, 50).build();

    }

    @Override
    protected void onCleared() {
        postsDb.removeObserver(postsObserver);
    }

    boolean checkPendingOutgoing() {
        return pendingOutgoing.compareAndSet(true, false);
    }

    boolean checkPendingIncoming() {
        return pendingIncoming.compareAndSet(true, false);
    }
}