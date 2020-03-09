package com.halloapp.ui.profile;

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

public class ProfileViewModel extends AndroidViewModel {

    final LiveData<PagedList<Post>> postList;
    private final PostsDb postsDb;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final PostsDb.Observer postsObserver = new PostsDb.Observer() {

        @Override
        public void onPostAdded(@NonNull Post post) {
            if (post.isOutgoing()) {
                invalidatePosts();
            }
        }

        @Override
        public void onPostRetracted(@NonNull UserId senderUserId, @NonNull String postId) {
            invalidatePosts();
        }

        @Override
        public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
            if (senderUserId.isMe()) {
                invalidatePosts();
            }
        }

        @Override
        public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId) {
        }

        @Override
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
            invalidatePosts();
        }

        @Override
        public void onCommentAdded(@NonNull Comment comment) {
            invalidatePosts();
        }

        @Override
        public void onCommentRetracted(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
            invalidatePosts();
        }

        @Override
        public void onCommentUpdated(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
        }

        @Override
        public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
            if (postSenderUserId.isMe()) {
                invalidatePosts();
            }
        }

        @Override
        public void onHistoryAdded(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments) {
            invalidatePosts();
        }

        @Override
        public void onPostsCleanup() {
            invalidatePosts();
        }

        @Override
        public void onDbCreated() {
        }

        private void invalidatePosts() {
            mainHandler.post(() -> Preconditions.checkNotNull(postList.getValue()).getDataSource().invalidate());
        }
    };

    public ProfileViewModel(@NonNull Application application) {
        super(application);

        postsDb = PostsDb.getInstance(application);
        postsDb.addObserver(postsObserver);

        final PostsDataSource.Factory dataSourceFactory = new PostsDataSource.Factory(postsDb, true);
        postList = new LivePagedListBuilder<>(dataSourceFactory, 50).build();
    }

    @Override
    protected void onCleared() {
        postsDb.removeObserver(postsObserver);
    }
}