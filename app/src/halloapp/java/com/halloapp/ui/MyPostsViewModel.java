package com.halloapp.ui;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.AppContext;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.content.PostsDataSource;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.VoiceNotePlayer;

public class MyPostsViewModel extends ViewModel {

    final LiveData<PagedList<Post>> postList;

    private final ContentDb contentDb = ContentDb.getInstance();

    private final PostsDataSource.Factory dataSourceFactory;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private Parcelable savedScrollState;

    private final VoiceNotePlayer voiceNotePlayer;

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onPostAdded(@NonNull Post post) {
            if (post.senderUserId.isMe()) {
                invalidatePosts();
            }
        }

        @Override
        public void onPostRetracted(@NonNull Post post) {
            if (post.senderUserId.isMe()) {
                invalidatePosts();
            }
        }

        @Override
        public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
            if (senderUserId.isMe()) {
                invalidatePosts();
            }
        }

        @Override
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
            invalidatePosts();
        }

        @Override
        public void onCommentAdded(@NonNull Comment comment) {
            Post parentPost = comment.getParentPost();
            if (parentPost != null && parentPost.senderUserId.isMe()) {
                invalidatePosts();
            }
        }

        @Override
        public void onCommentRetracted(@NonNull Comment comment) {
            if (UserId.ME.equals(comment.getPostSenderUserId())) {
                invalidatePosts();
            }
        }

        @Override
        public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId, @Nullable GroupId parentGroup) {
            if (postSenderUserId.equals(UserId.ME)) {
                invalidatePosts();
            }
        }

        @Override
        public void onFeedCleanup() {
            invalidatePosts();
        }

        private void invalidatePosts() {
            mainHandler.post(dataSourceFactory::invalidateLatestDataSource);
        }
    };

    public MyPostsViewModel() {
        contentDb.addObserver(contentObserver);

        dataSourceFactory = new PostsDataSource.Factory(contentDb, UserId.ME);

        voiceNotePlayer = new VoiceNotePlayer((Application)AppContext.getInstance().get());

        postList = new LivePagedListBuilder<>(dataSourceFactory, 50).build();
    }

    public void saveScrollState(@Nullable Parcelable savedScrollState) {
        this.savedScrollState = savedScrollState;
    }

    public @Nullable Parcelable getSavedScrollState() {
        return savedScrollState;
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
        voiceNotePlayer.onCleared();
    }

    @NonNull
    public VoiceNotePlayer getVoiceNotePlayer() {
        return voiceNotePlayer;
    }
}
