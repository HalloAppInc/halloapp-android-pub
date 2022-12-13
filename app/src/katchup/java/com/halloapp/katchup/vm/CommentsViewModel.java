package com.halloapp.katchup.vm;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.CommentsDataSource;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.katchup.KatchupCommentDataSource;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.RandomId;

public class CommentsViewModel extends ViewModel {

    private final ContentDb contentDb = ContentDb.getInstance();
    private final ContactsDb contactsDb = ContactsDb.getInstance();

    private ComputableLiveData<Post> postLiveData;

    private final String postId;

    private KatchupCommentDataSource.Factory dataSourceFactory;

    private LiveData<PagedList<Comment>> commentList;

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {
        @Override
        public void onCommentAdded(@NonNull Comment comment) {
            if (!CommentsViewModel.this.postId.equals(comment.postId)) {
                return;
            }
            invalidateLatestDataSource();
        }

        @Override
        public void onCommentRetracted(@NonNull Comment comment) {
            if (!CommentsViewModel.this.postId.equals(comment)) {
                return;
            }
            invalidateLatestDataSource();
        }

        @Override
        public void onCommentUpdated(@NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
            if (!CommentsViewModel.this.postId.equals(postId)) {
                return;
            }
            invalidateLatestDataSource();
        }
    };

    public CommentsViewModel(String postId) {
        this.postId = postId;
        postLiveData = new ComputableLiveData<Post>() {
            @Override
            protected Post compute() {
                return contentDb.getPost(postId);
            }
        };
        contentDb.addObserver(contentObserver);
    }

    protected LiveData<PagedList<Comment>> createCommentsList() {
        dataSourceFactory = new KatchupCommentDataSource.Factory(contentDb, contactsDb, postId);

        return new LivePagedListBuilder<>(dataSourceFactory, new PagedList.Config.Builder().setPageSize(50).setEnablePlaceholders(false).build()).build();
    }

    protected void invalidateLatestDataSource() {
        dataSourceFactory.invalidateLatestDataSource();
    }

    public LiveData<PagedList<Comment>> getCommentList() {
        if (commentList == null) {
            commentList = createCommentsList();
        }
        return commentList;
    }

    public void sendComment(String text) {
        final Comment comment = new Comment(
                0,
                postId,
                UserId.ME,
                RandomId.create(),
                null,
                System.currentTimeMillis(),
                Comment.TRANSFERRED_NO,
                true,
                text);
        contentDb.addComment(comment);
    }

    public LiveData<Post> getPost() {
        return postLiveData.getLiveData();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        contentDb.removeObserver(contentObserver);
    }

    public static class CommentsViewModelFactory implements ViewModelProvider.Factory {

        private final String postId;

        public CommentsViewModelFactory(String postId) {
            this.postId = postId;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CommentsViewModel.class)) {
                //noinspection unchecked
                return (T) new CommentsViewModel(postId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
