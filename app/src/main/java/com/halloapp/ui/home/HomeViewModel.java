package com.halloapp.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
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
    private final DataSource<Long, Post> mostRecentDataSource;
    private final PostsDb postsDb;

    private final PostsDb.Observer postsObserver = new PostsDb.Observer() {
        @Override
        public void onPostAdded(@NonNull Post post) {
            //mostRecentDataSource.invalidate();
            postList.getValue().getDataSource().invalidate();
        }

        @Override
        public void onPostDeleted(@NonNull Post post) {
            //mostRecentDataSource.invalidate();
            postList.getValue().getDataSource().invalidate();
        }
    };

    public HomeViewModel(@NonNull Application application) {
        super(application);

        postsDb = PostsDb.getInstance(application);
        postsDb.addObserver(postsObserver);

        final PostsDataSourceFactory dataSourceFactory = new PostsDataSourceFactory(postsDb);
        mostRecentDataSource = dataSourceFactory.create();

        postList = new LivePagedListBuilder<>(dataSourceFactory, 50).build();

    }

    @Override
    protected void onCleared() {
        postsDb.removeObserver(postsObserver);
    }
}