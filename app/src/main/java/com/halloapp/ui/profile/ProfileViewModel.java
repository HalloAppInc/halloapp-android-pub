package com.halloapp.ui.profile;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Message;
import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.content.PostsDataSource;
import com.halloapp.privacy.BlockListManager;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.DelayedProgressLiveData;
import com.halloapp.util.RandomId;

import java.util.Collection;
import java.util.List;

public class ProfileViewModel extends AndroidViewModel {

    final LiveData<PagedList<Post>> postList;
    private final ContentDb contentDb;
    private final ContactsDb contactsDb;
    private final BlockListManager blockListManager;
    private final PostsDataSource.Factory dataSourceFactory;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ComputableLiveData<Contact> contactLiveData;
    private final MutableLiveData<Boolean> isBlocked;

    private final UserId userId;

    private Parcelable savedScrollState;

    private final BlockListManager.Observer blockListObserver = new BlockListManager.Observer() {
        @Override
        public void onBlockListChanged() {
            updateIsBlocked();
        }
    };

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onPostAdded(@NonNull Post post) {
            if (post.senderUserId.equals(userId)) {
                invalidatePosts();
            }
        }

        @Override
        public void onPostRetracted(@NonNull Post post) {
            if (post.senderUserId.equals(userId)) {
                invalidatePosts();
            }
        }

        @Override
        public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
            if (senderUserId.equals(userId)) {
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
            if (parentPost != null && parentPost.senderUserId.equals(userId)) {
                invalidatePosts();
            }
        }

        @Override
        public void onCommentRetracted(@NonNull Comment comment) {
            if (userId.equals(comment.getPostSenderUserId())) {
                invalidatePosts();
            }
        }

        @Override
        public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
            if (postSenderUserId.equals(userId)) {
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

        this.userId = userId;

        blockListManager = BlockListManager.getInstance();
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
        isBlocked = new MutableLiveData<>();
        updateIsBlocked();
    }

    private void updateIsBlocked() {
        BgWorkers.getInstance().execute(() -> {
            List<UserId> blockList = blockListManager.getBlockList();
            isBlocked.postValue(blockList != null ? blockList.contains(userId) : null);
        });
    }

    public LiveData<Contact> getContact() {
        return contactLiveData.getLiveData();
    }

    public LiveData<Boolean> getIsBlocked() {
        return isBlocked;
    }

    @MainThread
    public LiveData<Boolean> unblockContact(@NonNull UserId userId) {
        MutableLiveData<Boolean> unblockResult = new DelayedProgressLiveData<>();
        blockListManager.unblockContact(userId).onResponse(result -> {
            if (result == null || !result) {
                unblockResult.postValue(false);
            } else {
                unblockResult.postValue(true);
            }
        }).onError(e -> {
            unblockResult.postValue(false);
        });
        return unblockResult;
    }

    @MainThread
    public LiveData<Boolean> blockContact(@NonNull UserId userId) {
        MutableLiveData<Boolean> blockResult = new DelayedProgressLiveData<>();
        blockListManager.blockContact(userId).onResponse(result -> {
            if (result == null || !result) {
                blockResult.postValue(false);
            } else {
                blockResult.postValue(true);
            }
        }).onError(e -> {
            blockResult.postValue(false);
        });
        return blockResult;
    }

    public void sendSystemMessage(@Message.Usage int usage, ChatId chatId) {
        final Message message = new Message(0,
                chatId,
                UserId.ME,
                RandomId.create(),
                System.currentTimeMillis(),
                Message.TYPE_SYSTEM,
                usage,
                Message.STATE_OUTGOING_DELIVERED,
                null,
                null,
                -1,
                null,
                -1,
                null,
                0);
        message.addToStorage(contentDb);
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
