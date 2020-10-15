package com.halloapp.content;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.PositionalDataSource;

import com.halloapp.id.UserId;

import java.util.List;

public class CommentsDataSource extends PositionalDataSource<Comment> {

    private final ContentDb contentDb;
    private final String postId;

    public static class Factory extends DataSource.Factory<Integer, Comment> {

        private final ContentDb contentDb;
        private final String postId;

        private CommentsDataSource latestSource;

        public Factory(@NonNull ContentDb contentDb, @NonNull String postId) {
            this.contentDb = contentDb;
            this.postId = postId;
            latestSource = new CommentsDataSource(contentDb, postId);
        }

        @Override
        public @NonNull DataSource<Integer, Comment> create() {
            if (latestSource.isInvalid()) {
                latestSource = new CommentsDataSource(contentDb, postId);
            }
            return latestSource;
        }

        public void invalidateLatestDataSource() {
            latestSource.invalidate();
        }
    }

    private CommentsDataSource(@NonNull ContentDb contentDb, @NonNull String postId) {
        this.contentDb = contentDb;
        this.postId = postId;

    }

    @Override
    public void loadInitial(@NonNull PositionalDataSource.LoadInitialParams params, @NonNull LoadInitialCallback<Comment> callback) {
        final List<Comment> comments = contentDb.getComments(postId, params.requestedStartPosition, params.requestedLoadSize);
        callback.onResult(comments, params.requestedStartPosition);
    }

    @Override
    public void loadRange(@NonNull PositionalDataSource.LoadRangeParams params, @NonNull LoadRangeCallback<Comment> callback) {
        callback.onResult(contentDb.getComments(postId, params.startPosition, params.loadSize));
    }
}
