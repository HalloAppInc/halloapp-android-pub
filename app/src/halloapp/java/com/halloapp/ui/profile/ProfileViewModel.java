package com.halloapp.ui.profile;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.AppContext;
import com.halloapp.Me;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.content.PostsDataSource;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.privacy.BlockListManager;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.DelayedProgressLiveData;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;

import java.util.Collection;
import java.util.List;

public class ProfileViewModel extends ViewModel {

    final LiveData<PagedList<Post>> postList;

    private final Me me = Me.getInstance();
    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final ContentDb contentDb = ContentDb.getInstance();
    private final ContactsDb contactsDb = ContactsDb.getInstance();
    private final BlockListManager blockListManager = BlockListManager.getInstance();

    private final PostsDataSource.Factory dataSourceFactory;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ComputableLiveData<String> subtitleLiveData;
    private final ComputableLiveData<Contact> contactLiveData;
    private final ComputableLiveData<Boolean> hasGroupsInCommonLiveData;
    private final MutableLiveData<Boolean> isBlocked;

    private final UserId userId;

    private Parcelable savedScrollState;

    private final VoiceNotePlayer voiceNotePlayer;

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
        public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId, @Nullable GroupId parentGroup) {
            if (postSenderUserId.equals(userId)) {
                invalidatePosts();
            }
        }

        @Override
        public void onGroupMembersChanged(@NonNull GroupId groupId) {
            hasGroupsInCommonLiveData.invalidate();
        }

        @Override
        public void onFeedCleanup() {
            invalidatePosts();
        }

        private void invalidatePosts() {
            mainHandler.post(dataSourceFactory::invalidateLatestDataSource);
        }
    };

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsChanged() {
            contactLiveData.invalidate();
        }

        @Override
        public void onNewContacts(@NonNull Collection<UserId> newContacts) {
            contactLiveData.invalidate();
        }
    };

    public ProfileViewModel(@NonNull UserId userId) {
        this.userId = userId;

        contactsDb.addObserver(contactsObserver);
        contentDb.addObserver(contentObserver);

        dataSourceFactory = new PostsDataSource.Factory(contentDb, userId);

        voiceNotePlayer = new VoiceNotePlayer((Application)AppContext.getInstance().get());

        hasGroupsInCommonLiveData = new ComputableLiveData<Boolean>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected Boolean compute() {
                return !ContentDb.getInstance().getGroupsInCommon(userId).isEmpty();
            }
        };
        subtitleLiveData = new ComputableLiveData<String>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected String compute() {
                String username = userId.isMe() ? me.getUsername() : contactsDb.getContact(userId).getUsername();
                if (TextUtils.isEmpty(username)) {
                    return "";
                } else {
                    return "@" + username;
                }
            }
        };
        contactLiveData = new ComputableLiveData<Contact>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected Contact compute() {
                if (userId.isMe()) {
                    return null;
                } else {
                    return contactsDb.getContact(userId);
                }
            }
        };
        postList = Transformations.switchMap(contactLiveData.getLiveData(), contact -> {
            if (contact == null || contact.addressBookName != null) {
                return new LivePagedListBuilder<>(dataSourceFactory, 50).build();
            }
            return new MutableLiveData<>();
        });
        contactLiveData.invalidate();
        isBlocked = new MutableLiveData<>();
        updateIsBlocked();

        blockListManager.addObserver(this::updateIsBlocked);
    }

    private void updateIsBlocked() {
        bgWorkers.execute(() -> {
            List<UserId> blockList = blockListManager.getBlockList();
            isBlocked.postValue(blockList != null ? blockList.contains(userId) : null);
        });
    }

    public LiveData<String> getSubtitle() {
        return subtitleLiveData.getLiveData();
    }

    public LiveData<Contact> getContact() {
        return contactLiveData.getLiveData();
    }

    public LiveData<Boolean> getIsBlocked() {
        return isBlocked;
    }

    public LiveData<Boolean> getHasGroupsInCommon() {
        return hasGroupsInCommonLiveData.getLiveData();
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

    public void sendSystemMessage(@Message.Usage int usage, UserId userId) {
        bgWorkers.execute(() -> {
            if (contentDb.getChat(userId) == null) {
                Log.i("Skipping adding system message because chat with " + userId + " does not already exist");
            } else {
                final Message message = new Message(0,
                        userId,
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
        });
    }

    public void saveScrollState(@Nullable Parcelable savedScrollState) {
        this.savedScrollState = savedScrollState;
    }

    public @Nullable Parcelable getSavedScrollState() {
        return savedScrollState;
    }

    @Override
    protected void onCleared() {
        contactsDb.removeObserver(contactsObserver);
        contentDb.removeObserver(contentObserver);
        blockListManager.removeObserver(this::updateIsBlocked);
        voiceNotePlayer.onCleared();
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final UserId profileUserId;

        Factory(@NonNull UserId profileUserId) {
            this.profileUserId = profileUserId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
                //noinspection unchecked
                return (T) new ProfileViewModel(profileUserId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    @NonNull
    public VoiceNotePlayer getVoiceNotePlayer() {
        return voiceNotePlayer;
    }
}
