package com.halloapp.posts;

import androidx.annotation.NonNull;
import androidx.paging.ItemKeyedDataSource;

public class PostsDataSource extends ItemKeyedDataSource<Long, Post> {

    private final PostsDb postsDb;

    PostsDataSource(@NonNull PostsDb postsDb) {
        this.postsDb = postsDb;
    }

    @Override
    public @NonNull Long getKey(@NonNull Post item) {
        return item.rowId;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<Post> callback) {
        callback.onResult(postsDb.getPosts(/*params.requestedInitialKey*/null, params.requestedLoadSize, true));
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Post> callback) {
        callback.onResult(postsDb.getPosts(params.key, params.requestedLoadSize, true));
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Post> callback) {
        callback.onResult(postsDb.getPosts(params.key, params.requestedLoadSize, false));
    }
}
