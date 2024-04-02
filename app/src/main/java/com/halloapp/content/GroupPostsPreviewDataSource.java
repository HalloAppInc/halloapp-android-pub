package com.halloapp.content;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.util.logs.Log;

import java.util.List;

public class GroupPostsPreviewDataSource extends ItemKeyedDataSource<Long, Post> {

    private final ContentDb contentDb;
    private final @Nullable UserId senderUserId;
    private final @Nullable GroupId groupId;
    private Long keyTimestamp;

    public static class Factory extends DataSource.Factory<Long, Post> {

        private final ContentDb contentDb;
        private final UserId senderUserId;
        private final GroupId groupId;
        private GroupPostsPreviewDataSource latestSource;

        public Factory(@NonNull ContentDb contentDb, @Nullable UserId senderUserId) {
            this(contentDb, senderUserId, null);
        }

        public Factory(@NonNull ContentDb contentDb, @Nullable UserId senderUserId, @Nullable GroupId groupId) {
            this.contentDb = contentDb;
            this.senderUserId = senderUserId;
            this.groupId = groupId;
            latestSource = new GroupPostsPreviewDataSource(contentDb, senderUserId, groupId);
        }

        @Override
        public @NonNull DataSource<Long, Post> create() {
            Log.i("GroupPostsPreviewDataSource.Factory.create");
            if (latestSource.isInvalid()) {
                Log.i("GroupPostsPreviewDataSource.Factory.create old source was invalidated; creating a new one");
                latestSource = new GroupPostsPreviewDataSource(contentDb, senderUserId, groupId);
            }
            return latestSource;
        }

        public void invalidateLatestDataSource() {
            Log.i("GroupPostsPreviewDataSource.Factory.invalidateLatestDataSource");
            latestSource.invalidate();
        }
    }

    private GroupPostsPreviewDataSource(@NonNull ContentDb contentDb, @Nullable UserId senderUserId, @Nullable GroupId groupId) {
        this.contentDb = contentDb;
        this.senderUserId = senderUserId;
        this.groupId = groupId;
    }

    @Override
    public @NonNull Long getKey(@NonNull Post item) {
        Log.d("GroupPostsPreviewDataSource.getKey item=" + item);
        if (keyTimestamp != null) {
            Log.i("GroupPostsPreviewDataSource.getKey reload was requested at " + keyTimestamp + "; clearing reload request");
            Long tmp = keyTimestamp;
            keyTimestamp = null;
            return tmp;
        }
        return item.updateTime;
    }

    public void reloadAt(long timestamp) { // TODO: Find a better way to scroll to an area not paged in; keyTimestamp feels hacky
        // next call to getKey on this data source will be used by framework to find load point of next data source after current one is invalidated;
        // this ensures that next call to getKey returns timestamp regardless of what actual post item is
        Log.d("GroupPostsPreviewDataSource.reloadAt timestamp=" + timestamp);
        keyTimestamp = timestamp;
        invalidate();
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<Post> callback) {
        final List<Post> posts;
        if (params.requestedInitialKey == null || params.requestedInitialKey == Long.MAX_VALUE) {
            posts = contentDb.getPostsOrderLastUpdate(null, params.requestedLoadSize, true, senderUserId, groupId);
        } else {
            // load around params.requestedInitialKey, otherwise the view that represents this data may jump
            posts = contentDb.getPostsOrderLastUpdate(params.requestedInitialKey, params.requestedLoadSize / 2, false, senderUserId, groupId);
            posts.addAll(contentDb.getPostsOrderLastUpdate(params.requestedInitialKey + 1, params.requestedLoadSize / 2, true, senderUserId, groupId));

        }
        Log.d("GroupPostsPreviewDataSource.loadInitial: requestedInitialKey=" + params.requestedInitialKey + " requestedLoadSize:" + params.requestedLoadSize + " got " + posts.size() +
                (posts.isEmpty() ? "" : " posts from " + posts.get(0).timestamp + " to " + posts.get(posts.size()-1).timestamp));
        callback.onResult(posts);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Post> callback) {
        Log.d("GroupPostsPreviewDataSource.loadAfter: key=" + params.key + " requestedLoadSize:" + params.requestedLoadSize);
        callback.onResult(contentDb.getPostsOrderLastUpdate(params.key, params.requestedLoadSize, true, senderUserId, groupId));
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Post> callback) {
        Log.d("GroupPostsPreviewDataSource.loadBefore: key=" + params.key + " requestedLoadSize:" + params.requestedLoadSize);
        callback.onResult(contentDb.getPostsOrderLastUpdate(params.key, params.requestedLoadSize, false, senderUserId, groupId));
    }
}
