package com.halloapp.ui.profile;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.Me;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.content.PostsDataSource;
import com.halloapp.util.ComputableLiveData;

import java.util.Collection;

public class ProfileViewModel extends AndroidViewModel {

    final LiveData<PagedList<Post>> postList;
    private final ContentDb contentDb;
    private final ContactsDb contactsDb;
    private final PostsDataSource.Factory dataSourceFactory;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ComputableLiveData<Contact> contactLiveData;

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onPostAdded(@NonNull Post post) {
            if (post.isOutgoing()) {
                invalidatePosts();
            }
        }

        @Override
        public void onPostRetracted(@NonNull UserId senderUserId, @NonNull String postId) {
            invalidatePosts();
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
            invalidatePosts();
        }

        @Override
        public void onCommentRetracted(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
            invalidatePosts();
        }

        @Override
        public void onCommentUpdated(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
        }

        @Override
        public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
            if (postSenderUserId.isMe()) {
                invalidatePosts();
            }
        }

        @Override
        public void onFeedHistoryAdded(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments) {
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

    public ProfileViewModel(@NonNull Application application, @NonNull UserId userId) {
        super(application);

        contentDb = ContentDb.getInstance(application);
        contentDb.addObserver(contentObserver);
        contactsDb = ContactsDb.getInstance();

        dataSourceFactory = new PostsDataSource.Factory(contentDb, userId);
        postList = new LivePagedListBuilder<>(dataSourceFactory, 50).build();
        contactLiveData = new ComputableLiveData<Contact>() {
            @Override
            protected Contact compute() {
                if (userId.isMe()) {
                    return null;
                } else {
                    return contactsDb.getContact(userId);
                }
            }
        };
        contactLiveData.invalidate();
    }

    public LiveData<Contact> getContact() {
        return contactLiveData.getLiveData();
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final UserId profileUserId;

        Factory(@NonNull Application application, @NonNull UserId profileUserId) {
            this.application = application;
            this.profileUserId = profileUserId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
                //noinspection unchecked
                return (T) new ProfileViewModel(application, profileUserId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
