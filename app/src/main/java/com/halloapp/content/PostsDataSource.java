package com.halloapp.content;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.halloapp.util.Log;

import java.util.List;

public class PostsDataSource extends ItemKeyedDataSource<Long, Post> {

    private final ContentDb contentDb;
    private final boolean outgoingOnly;
    private Long keyTimestamp;

    public static class Factory extends DataSource.Factory<Long, Post> {

        private final ContentDb contentDb;
        private final boolean outgoingOnly;

        private final MutableLiveData<PostsDataSource> sourceLiveData = new MutableLiveData<>();

        public Factory(@NonNull ContentDb contentDb, boolean outgoingOnly) {
            this.contentDb = contentDb;
            this.outgoingOnly = outgoingOnly;
        }

        @Override
        public @NonNull DataSource<Long, Post> create() {
            final PostsDataSource latestSource = new PostsDataSource(contentDb, outgoingOnly);
            sourceLiveData.postValue(latestSource);
            return latestSource;
        }
    }

    private PostsDataSource(@NonNull ContentDb contentDb, boolean outgoingOnly) {
        this.contentDb = contentDb;
        this.outgoingOnly = outgoingOnly;
    }

    @Override
    public @NonNull Long getKey(@NonNull Post item) {
        if (keyTimestamp  != null) {
            return keyTimestamp;
        }
        return item.timestamp;
    }

    public void reloadAt(long timestamp) {
        // next call to getKey on this data source will be used by framework to find load point of next data source after current one is invalidated;
        // this ensures that next call to getKey returns timestamp regardless of what actual post item is
        keyTimestamp = timestamp;
        invalidate();
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<Post> callback) {
        final List<Post> posts;
        if (params.requestedInitialKey == null || params.requestedInitialKey == Long.MAX_VALUE) {
            posts = contentDb.getPosts(null, params.requestedLoadSize, true, outgoingOnly);
        } else {
            // load around params.requestedInitialKey, otherwise the view that represents this data may jump
            posts = contentDb.getPosts(params.requestedInitialKey, params.requestedLoadSize / 2, false, outgoingOnly);
            posts.addAll(contentDb.getPosts(params.requestedInitialKey + 1, params.requestedLoadSize / 2, true, outgoingOnly));

        }
        Log.d("PostsDataSource.loadInitial: requestedInitialKey=" + params.requestedInitialKey + " requestedLoadSize:" + params.requestedLoadSize + " got " + posts.size() +
                (posts.isEmpty() ? "" : " posts from " + posts.get(0).timestamp + " to " + posts.get(posts.size()-1).timestamp));
        callback.onResult(posts);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Post> callback) {
        Log.d("PostsDataSource.loadAfter: key=" + params.key + " requestedLoadSize:" + params.requestedLoadSize);
        callback.onResult(contentDb.getPosts(params.key, params.requestedLoadSize, true, outgoingOnly));
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Post> callback) {
        Log.d("PostsDataSource.loadBefore: key=" + params.key + " requestedLoadSize:" + params.requestedLoadSize);
        callback.onResult(contentDb.getPosts(params.key, params.requestedLoadSize, false, outgoingOnly));
    }
}
