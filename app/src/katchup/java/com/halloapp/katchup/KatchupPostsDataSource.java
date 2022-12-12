package com.halloapp.katchup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.util.logs.Log;

import java.util.List;

public class KatchupPostsDataSource extends ItemKeyedDataSource<Long, Post> {

    private final ContentDb contentDb;
    private Long keyTimestamp;

    public static class Factory extends DataSource.Factory<Long, Post> {

        private final ContentDb contentDb;
        private KatchupPostsDataSource latestSource;

        public Factory(@NonNull ContentDb contentDb) {
            this.contentDb = contentDb;
            latestSource = new KatchupPostsDataSource(contentDb);
        }

        @Override
        public @NonNull DataSource<Long, Post> create() {
            Log.i("KatchupPostsDataSource.Factory.create");
            if (latestSource.isInvalid()) {
                Log.i("KatchupPostsDataSource.Factory.create old source was invalidated; creating a new one");
                latestSource = new KatchupPostsDataSource(contentDb);
            }
            return latestSource;
        }

        public void invalidateLatestDataSource() {
            Log.i("KatchupPostsDataSource.Factory.invalidateLatestDataSource");
            latestSource.invalidate();
        }
    }

    private KatchupPostsDataSource(@NonNull ContentDb contentDb) {
        this.contentDb = contentDb;
    }

    @Override
    public @NonNull Long getKey(@NonNull Post item) {
        Log.d("KatchupPostsDataSource.getKey item=" + item);
        if (keyTimestamp != null) {
            Log.i("KatchupPostsDataSource.getKey reload was requested at " + keyTimestamp + "; clearing reload request");
            Long tmp = keyTimestamp;
            keyTimestamp = null;
            return tmp;
        }
        return item.timestamp;
    }

    public void reloadAt(long timestamp) { // TODO(jack): Find a better way to scroll to an area not paged in; keyTimestamp feels hacky
        // next call to getKey on this data source will be used by framework to find load point of next data source after current one is invalidated;
        // this ensures that next call to getKey returns timestamp regardless of what actual post item is
        Log.d("KatchupPostsDataSource.reloadAt timestamp=" + timestamp);
        keyTimestamp = timestamp;
        invalidate();
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<Post> callback) {
        final List<Post> posts;
        if (params.requestedInitialKey == null || params.requestedInitialKey == Long.MAX_VALUE) {
            posts = contentDb.getSeenPosts(null, params.requestedLoadSize, true);
        } else {
            // load around params.requestedInitialKey, otherwise the view that represents this data may jump
            posts = contentDb.getSeenPosts(params.requestedInitialKey, params.requestedLoadSize / 2, false);
            posts.addAll(contentDb.getSeenPosts(params.requestedInitialKey + 1, params.requestedLoadSize / 2, true));

        }
        Log.d("KatchupPostsDataSource.loadInitial: requestedInitialKey=" + params.requestedInitialKey + " requestedLoadSize:" + params.requestedLoadSize + " got " + posts.size() +
                (posts.isEmpty() ? "" : " posts from " + posts.get(0).timestamp + " to " + posts.get(posts.size()-1).timestamp));
        callback.onResult(posts);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Post> callback) {
        Log.d("KatchupPostsDataSource.loadAfter: key=" + params.key + " requestedLoadSize:" + params.requestedLoadSize);
        callback.onResult(contentDb.getSeenPosts(params.key, params.requestedLoadSize, true));
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Post> callback) {
        Log.d("KatchupPostsDataSource.loadBefore: key=" + params.key + " requestedLoadSize:" + params.requestedLoadSize);
        callback.onResult(contentDb.getSeenPosts(params.key, params.requestedLoadSize, false));
    }
}
