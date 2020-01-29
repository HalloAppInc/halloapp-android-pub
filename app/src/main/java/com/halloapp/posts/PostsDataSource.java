package com.halloapp.posts;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.halloapp.util.Log;

import java.util.List;

public class PostsDataSource extends ItemKeyedDataSource<Long, Post> {

    private final PostsDb postsDb;
    private final boolean outgoingOnly;

    public static class Factory extends DataSource.Factory<Long, Post> {

        private final PostsDb postsDb;
        private final boolean outgoingOnly;

        private final MutableLiveData<PostsDataSource> sourceLiveData = new MutableLiveData<>();

        public Factory(@NonNull PostsDb postsDb, boolean outgoingOnly) {
            this.postsDb = postsDb;
            this.outgoingOnly = outgoingOnly;
        }

        @Override
        public @NonNull DataSource<Long, Post> create() {
            final PostsDataSource latestSource = new PostsDataSource(postsDb, outgoingOnly);
            sourceLiveData.postValue(latestSource);
            return latestSource;
        }
    }

    private PostsDataSource(@NonNull PostsDb postsDb, boolean outgoingOnly) {
        this.postsDb = postsDb;
        this.outgoingOnly = outgoingOnly;
    }

    @Override
    public @NonNull Long getKey(@NonNull Post item) {
        return item.timestamp;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<Post> callback) {
        final List<Post> posts;
        if (params.requestedInitialKey == null) {
            posts = postsDb.getPosts(null, params.requestedLoadSize, true, outgoingOnly);
        } else {
            // load around params.requestedInitialKey, otherwise the view that represents this data may jump
            posts = postsDb.getPosts(params.requestedInitialKey, params.requestedLoadSize / 2, false, outgoingOnly);
            posts.addAll(postsDb.getPosts(params.requestedInitialKey + 1, params.requestedLoadSize / 2, true, outgoingOnly));

        }
        Log.d("PostsDataSource.loadInitial: requestedInitialKey=" + params.requestedInitialKey + " requestedLoadSize:" + params.requestedLoadSize + " got " + posts.size() +
                (posts.isEmpty() ? "" : " posts from " + posts.get(0).timestamp + " to " + posts.get(posts.size()-1).timestamp));
        callback.onResult(posts);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Post> callback) {
        Log.d("PostsDataSource.loadAfter: key=" + params.key + " requestedLoadSize:" + params.requestedLoadSize);
        callback.onResult(postsDb.getPosts(params.key, params.requestedLoadSize, true, outgoingOnly));
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Post> callback) {
        Log.d("PostsDataSource.loadBefore: key=" + params.key + " requestedLoadSize:" + params.requestedLoadSize);
        callback.onResult(postsDb.getPosts(params.key, params.requestedLoadSize, false, outgoingOnly));
    }
}
