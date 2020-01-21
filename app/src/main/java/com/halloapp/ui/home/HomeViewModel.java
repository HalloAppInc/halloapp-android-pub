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
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDataSource;
import com.halloapp.posts.PostsDb;

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
            mainHandler.post(() -> Preconditions.checkNotNull(postList.getValue()).getDataSource().invalidate());
        }

        @Override
        public void onPostDuplicate(@NonNull Post post) {
            // do not update model on duplicate post
        }

        @Override
        public void onPostDeleted(@NonNull Post post) {
            mainHandler.post(() -> Preconditions.checkNotNull(postList.getValue()).getDataSource().invalidate());
        }

        @Override
        public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
            // TODO (ds): probably not need to invalidate the entire data
            mainHandler.post(() -> Preconditions.checkNotNull(postList.getValue()).getDataSource().invalidate());
        }
    };

    public HomeViewModel(@NonNull Application application) {
        super(application);

        postsDb = PostsDb.getInstance(application);
        postsDb.addObserver(postsObserver);

        final PostsDataSource.Factory dataSourceFactory = new PostsDataSource.Factory(postsDb);
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