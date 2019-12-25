package com.halloapp.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.core.util.Preconditions;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDataSourceFactory;
import com.halloapp.posts.PostsDb;

public class HomeViewModel extends AndroidViewModel {

    final LiveData<PagedList<Post>> postList;
    private final PostsDb postsDb;

    private final PostsDb.Observer postsObserver = new PostsDb.Observer() {
        @Override
        public void onPostAdded(@NonNull Post post) {
            Preconditions.checkNotNull(postList.getValue()).getDataSource().invalidate();
        }

        @Override
        public void onPostDuplicate(@NonNull Post post) {
            // do not update model on duplicate post
        }

        @Override
        public void onPostDeleted(@NonNull Post post) {
            Preconditions.checkNotNull(postList.getValue()).getDataSource().invalidate();
        }

        @Override
        public void onPostStateChanged(@NonNull String chatJid, @NonNull String senderJid, @NonNull String postId, int state) {
            // TODO (ds): probably not need to invalidate the entire data
            Preconditions.checkNotNull(postList.getValue()).getDataSource().invalidate();
        }

        @Override
        public void onPostMediaUpdated(@NonNull String chatJid, @NonNull String senderJid, @NonNull String postId) {
            // TODO (ds): probably not need to invalidate the entire data
            Preconditions.checkNotNull(postList.getValue()).getDataSource().invalidate();
        }
    };

    public HomeViewModel(@NonNull Application application) {
        super(application);

        postsDb = PostsDb.getInstance(application);
        postsDb.addObserver(postsObserver);

        final PostsDataSourceFactory dataSourceFactory = new PostsDataSourceFactory(postsDb);
        postList = new LivePagedListBuilder<>(dataSourceFactory, 50).build();

    }

    @Override
    protected void onCleared() {
        postsDb.removeObserver(postsObserver);
    }
}