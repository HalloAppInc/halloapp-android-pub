package com.halloapp.content;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.util.logs.Log;

import java.util.List;

public class PostsDataSource extends ItemKeyedDataSource<Long, Post> {

    private final ContentDb contentDb;
    private final @Nullable UserId senderUserId;
    private final @Nullable GroupId groupId;
    private Long keyTimestamp;

    public static class Factory extends DataSource.Factory<Long, Post> {

        private final ContentDb contentDb;
        private final UserId senderUserId;
        private final GroupId groupId;
        private PostsDataSource latestSource;

        public Factory(@NonNull ContentDb contentDb, @Nullable UserId senderUserId) {
            this(contentDb, senderUserId, null);
        }

        public Factory(@NonNull ContentDb contentDb, @Nullable UserId senderUserId, @Nullable GroupId groupId) {
            this.contentDb = contentDb;
            this.senderUserId = senderUserId;
            this.groupId = groupId;
            latestSource = new PostsDataSource(contentDb, senderUserId, groupId);
        }

        @Override
        public @NonNull DataSource<Long, Post> create() {
            if (latestSource.isInvalid()) {
                latestSource = new PostsDataSource(contentDb, senderUserId, groupId);
            }
            return latestSource;
        }

        public void invalidateLatestDataSource() {
            Log.i("PostsDataSource.Factory.invalidateLatestDataSource");
            latestSource.invalidate();
        }
    }

    private PostsDataSource(@NonNull ContentDb contentDb, @Nullable UserId senderUserId, @Nullable GroupId groupId) {
        this.contentDb = contentDb;
        this.senderUserId = senderUserId;
        this.groupId = groupId;
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
            posts = contentDb.getPosts(null, params.requestedLoadSize, true, senderUserId, groupId);
        } else {
            // load around params.requestedInitialKey, otherwise the view that represents this data may jump
            posts = contentDb.getPosts(params.requestedInitialKey, params.requestedLoadSize / 2, false, senderUserId, groupId);
            posts.addAll(contentDb.getPosts(params.requestedInitialKey + 1, params.requestedLoadSize / 2, true, senderUserId, groupId));

        }
        Log.d("PostsDataSource.loadInitial: requestedInitialKey=" + params.requestedInitialKey + " requestedLoadSize:" + params.requestedLoadSize + " got " + posts.size() +
                (posts.isEmpty() ? "" : " posts from " + posts.get(0).timestamp + " to " + posts.get(posts.size()-1).timestamp));
        callback.onResult(posts);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Post> callback) {
        Log.d("PostsDataSource.loadAfter: key=" + params.key + " requestedLoadSize:" + params.requestedLoadSize);
        callback.onResult(contentDb.getPosts(params.key, params.requestedLoadSize, true, senderUserId, groupId));
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Post> callback) {
        Log.d("PostsDataSource.loadBefore: key=" + params.key + " requestedLoadSize:" + params.requestedLoadSize);
        callback.onResult(contentDb.getPosts(params.key, params.requestedLoadSize, false, senderUserId, groupId));
    }
}
