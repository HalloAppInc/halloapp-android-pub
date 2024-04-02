package com.halloapp.katchup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.util.logs.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KatchupPostsDataSource extends ItemKeyedDataSource<Long, Post> {

    public final static int POST_TYPE_SEEN = 1;
    public final static int POST_TYPE_UNSEEN = 2;
    public final static int POST_TYPE_MY_ARCHIVE = 3;

    private final ContentDb contentDb;
    private final int postType;
    private final String postId;
    private final boolean reversed;
    private Long keyTimestamp;


    public static class Factory extends DataSource.Factory<Long, Post> {

        private final ContentDb contentDb;
        private final int postType;
        private final String postId;
        private final boolean reversed;
        private KatchupPostsDataSource latestSource;

        public Factory(@NonNull ContentDb contentDb, int postType) {
            this(contentDb, postType, null, false);
        }

        public Factory(@NonNull ContentDb contentDb, int postType, @Nullable String postId, boolean reversed) {
            this.contentDb = contentDb;
            this.postType = postType;
            this.postId = postId;
            this.reversed = reversed;
            latestSource = new KatchupPostsDataSource(contentDb, postType, postId, reversed);
        }

        @Override
        public @NonNull DataSource<Long, Post> create() {
            Log.i("KatchupPostsDataSource.Factory.create");
            if (latestSource.isInvalid()) {
                Log.i("KatchupPostsDataSource.Factory.create old source was invalidated; creating a new one");
                latestSource = new KatchupPostsDataSource(contentDb, postType, postId, reversed);
            }
            return latestSource;
        }

        public void invalidateLatestDataSource() {
            Log.i("KatchupPostsDataSource.Factory.invalidateLatestDataSource");
            latestSource.invalidate();
        }
    }

    private KatchupPostsDataSource(@NonNull ContentDb contentDb, int postType, String postId, boolean reversed) {
        this.contentDb = contentDb;
        this.postType = postType;
        this.postId = postId;
        this.reversed = reversed;
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

    public void reloadAt(long timestamp) { // TODO: Find a better way to scroll to an area not paged in; keyTimestamp feels hacky
        // next call to getKey on this data source will be used by framework to find load point of next data source after current one is invalidated;
        // this ensures that next call to getKey returns timestamp regardless of what actual post item is
        Log.d("KatchupPostsDataSource.reloadAt timestamp=" + timestamp);
        keyTimestamp = timestamp;
        invalidate();
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<Post> callback) {
        final List<Post> posts;
        Long initialKey = params.requestedInitialKey;

        if (postId != null) {
            Post post = contentDb.getPost(postId);
            if (post != null) {
                initialKey = post.timestamp;
            }
        }

        if (initialKey == null || initialKey == Long.MAX_VALUE) {
            posts = getPosts(null, params.requestedLoadSize, true);
        } else {
            // load around initialKey, otherwise the view that represents this data may jump
            posts = getPosts(initialKey, params.requestedLoadSize / 2, false);
            posts.addAll(getPosts(initialKey + 1, params.requestedLoadSize / 2, true));
        }

        Log.d("KatchupPostsDataSource.loadInitial: requestedInitialKey=" + params.requestedInitialKey + " requestedLoadSize:" + params.requestedLoadSize + " got " + posts.size() +
                (posts.isEmpty() ? "" : " posts from " + posts.get(0).timestamp + " to " + posts.get(posts.size()-1).timestamp));

        if (reversed) {
            Collections.reverse(posts);
        }

        callback.onResult(posts);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Post> callback) {
        Log.d("KatchupPostsDataSource.loadAfter: key=" + params.key + " requestedLoadSize:" + params.requestedLoadSize);
        if (reversed) {
            List<Post> posts = getPosts(params.key, params.requestedLoadSize, false);
            Collections.reverse(posts);
            callback.onResult(posts);
        } else {
            callback.onResult(getPosts(params.key, params.requestedLoadSize, true));
        }
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Post> callback) {
        Log.d("KatchupPostsDataSource.loadBefore: key=" + params.key + " requestedLoadSize:" + params.requestedLoadSize);
        if (reversed) {
            List<Post> posts = getPosts(params.key, params.requestedLoadSize, true);
            Collections.reverse(posts);
            callback.onResult(posts);
        } else {
            callback.onResult(getPosts(params.key, params.requestedLoadSize, false));
        }
    }

    @NonNull
    private List<Post> getPosts(@Nullable Long timestamp, int count, boolean after) {
        switch (postType) {
            case POST_TYPE_SEEN:
                return contentDb.getSeenPosts(timestamp, count, after);
            case POST_TYPE_UNSEEN:
                return contentDb.getUnseenPosts(timestamp, count, after);
            case POST_TYPE_MY_ARCHIVE:
                return contentDb.getMyArchivePosts(timestamp, count, after);
        }

        return new ArrayList<>();
    }
}
