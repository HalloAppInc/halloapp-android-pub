package com.halloapp.posts;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

public class PostsDataSourceFactory extends DataSource.Factory<Long, Post> {

    private final PostsDb postsDb;
    private MutableLiveData<PostsDataSource> sourceLiveData = new MutableLiveData<>();

    private PostsDataSource latestSource;

    public PostsDataSourceFactory(final @NonNull PostsDb postsDb) {
        this.postsDb = postsDb;
    }

    @Override
    public @NonNull DataSource<Long, Post> create() {
        latestSource = new PostsDataSource(postsDb);
        sourceLiveData.postValue(latestSource);
        return latestSource;
    }
}
