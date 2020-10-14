package com.halloapp.ui.groups;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Chat;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.content.PostsDataSource;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.privacy.BlockListManager;
import com.halloapp.util.ComputableLiveData;

public class GroupFeedViewModel extends AndroidViewModel {

    private static final int ADAPTER_PAGE_SIZE = 50;

    final LiveData<PagedList<Post>> postList;
    private final ContentDb contentDb;
    private final ContactsDb contactsDb;
    private final BlockListManager blockListManager;
    private final PostsDataSource.Factory dataSourceFactory;

    private final GroupId groupId;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private Parcelable savedScrollState;

    public final ComputableLiveData<Chat> chat;

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onPostAdded(@NonNull Post post) {
            if (groupId.equals(post.getParentGroup())) {
                invalidatePosts();
            }
        }

        @Override
        public void onPostRetracted(@NonNull Post post) {
            if (groupId.equals(post.getParentGroup())) {
                invalidatePosts();
            }
        }

        @Override
        public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
            invalidatePosts();
        }

        @Override
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
            invalidatePosts();
        }

        @Override
        public void onCommentAdded(@NonNull Comment comment) {
            Post parentPost = comment.getParentPost();
            if (parentPost != null && groupId.equals(parentPost.getParentGroup())) {
                invalidatePosts();
            }
        }

        @Override
        public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
            invalidatePosts();
        }

        @Override
        public void onFeedCleanup() {
            invalidatePosts();
        }

        private void invalidatePosts() {
            mainHandler.post(dataSourceFactory::invalidateLatestDataSource);
        }
    };


    public GroupFeedViewModel(@NonNull Application application, @NonNull GroupId groupId) {
        super(application);
        this.groupId = groupId;

        blockListManager = BlockListManager.getInstance();
        contentDb = ContentDb.getInstance(application);
        contentDb.addObserver(contentObserver);
        contactsDb = ContactsDb.getInstance();

        dataSourceFactory = new PostsDataSource.Factory(contentDb, null, groupId);
        postList = new LivePagedListBuilder<>(dataSourceFactory, ADAPTER_PAGE_SIZE).build();

        chat = new ComputableLiveData<Chat>() {
            @Override
            protected Chat compute() {
                return contentDb.getChat(groupId);
            }
        };
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
    }

    void reloadPostsAt(long timestamp) {
        final PagedList pagedList = postList.getValue();
        if (pagedList != null) {
            ((PostsDataSource)pagedList.getDataSource()).reloadAt(timestamp);
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final GroupId groupId;

        Factory(@NonNull Application application, @NonNull GroupId groupId) {
            this.application = application;
            this.groupId = groupId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(GroupFeedViewModel.class)) {
                //noinspection unchecked
                return (T) new GroupFeedViewModel(application, groupId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
