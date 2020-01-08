package com.halloapp.posts;

import androidx.annotation.NonNull;
import androidx.paging.ItemKeyedDataSource;

import com.halloapp.util.Log;

import java.util.List;

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
        final List<Post> posts;
        if (params.requestedInitialKey == null) {
            posts = postsDb.getPosts(null, params.requestedLoadSize, true);
        } else {
            // load around params.requestedInitialKey, otherwise the view that represents this data may jump
            posts = postsDb.getPosts(params.requestedInitialKey, params.requestedLoadSize / 2, false);
            posts.addAll(postsDb.getPosts(params.requestedInitialKey + 1, params.requestedLoadSize / 2, true));

        }
        Log.d("PostsDataSource.loadInitial: requestedInitialKey=" + params.requestedInitialKey + " requestedLoadSize:" + params.requestedLoadSize + " got " + posts.size() +
                (posts.isEmpty() ? "" : " posts from " + posts.get(0).rowId + " to " + posts.get(posts.size()-1).rowId));
        callback.onResult(posts);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Post> callback) {
        Log.d("PostsDataSource.loadAfter: key=" + params.key + " requestedLoadSize:" + params.requestedLoadSize);
        callback.onResult(postsDb.getPosts(params.key, params.requestedLoadSize, true));
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Post> callback) {
        Log.d("PostsDataSource.loadBefore: key=" + params.key + " requestedLoadSize:" + params.requestedLoadSize);
        callback.onResult(postsDb.getPosts(params.key, params.requestedLoadSize, false));
    }
}
