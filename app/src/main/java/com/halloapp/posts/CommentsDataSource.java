package com.halloapp.posts;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PositionalDataSource;

import com.halloapp.contacts.UserId;
import com.halloapp.util.Log;

import java.util.List;

public class CommentsDataSource extends PositionalDataSource<Comment> {

    private final PostsDb postsDb;
    private final UserId postSenderUserId;
    private final String postId;

    public static class Factory extends DataSource.Factory<Integer, Comment> {

        private final PostsDb postsDb;
        private final UserId postSenderUserId;
        private final String postId;

        private final MutableLiveData<CommentsDataSource> sourceLiveData = new MutableLiveData<>();

        public Factory(@NonNull PostsDb postsDb, @NonNull UserId postSenderUserId, @NonNull String postId) {
            this.postsDb = postsDb;
            this.postSenderUserId = postSenderUserId;
            this.postId = postId;
        }

        @Override
        public @NonNull DataSource<Integer, Comment> create() {
            CommentsDataSource latestSource = new CommentsDataSource(postsDb, postSenderUserId, postId);
            sourceLiveData.postValue(latestSource);
            return latestSource;
        }
    }

    private CommentsDataSource(@NonNull PostsDb postsDb, @NonNull UserId postSenderUserId, @NonNull String postId) {
        this.postsDb = postsDb;
        this.postSenderUserId = postSenderUserId;
        this.postId = postId;

    }

    @Override
    public void loadInitial(@NonNull PositionalDataSource.LoadInitialParams params, @NonNull LoadInitialCallback<Comment> callback) {
        final List<Comment> comments = postsDb.getComments(postSenderUserId, postId, params.requestedStartPosition, params.requestedLoadSize);
        //Log.d("CommentsDataSource.loadInitial: requestedInitialKey=" + params.requestedInitialKey + " requestedLoadSize:" + params.requestedLoadSize + " got " + comments.size() +
        //        (comments.isEmpty() ? "" : " posts from " + comments.get(0).timestamp + " to " + comments.get(comments.size()-1).timestamp));
        callback.onResult(comments, params.requestedStartPosition);
    }

    @Override
    public void loadRange(@NonNull PositionalDataSource.LoadRangeParams params, @NonNull LoadRangeCallback<Comment> callback) {
        //Log.d("CommentsDataSource.loadAfter: key=" + params.key + " requestedLoadSize:" + params.requestedLoadSize);
        callback.onResult(postsDb.getComments(postSenderUserId, postId, params.startPosition, params.loadSize));
    }
}
