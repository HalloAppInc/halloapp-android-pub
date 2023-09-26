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
import com.halloapp.contacts.FriendshipInfo;
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
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

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

    private final ComputableLiveData<String> usernameLiveData;
    private final ComputableLiveData<Contact> contactLiveData;
    private final ComputableLiveData<Boolean> hasGroupsInCommonLiveData;
    public final MutableLiveData<FriendshipInfo> profile = new MutableLiveData<>();
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

        @Override
        public void onFriendshipsChanged(@NonNull FriendshipInfo friendshipInfo) {
            computeUserProfileInfo();
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
        usernameLiveData = new ComputableLiveData<String>() {
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
            if (contact == null || contact.friendshipStatus == FriendshipInfo.Type.FRIENDS) {
                return new LivePagedListBuilder<>(dataSourceFactory, 50).build();
            }
            return new MutableLiveData<>(null);
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

    public LiveData<String> getUsername() {
        return usernameLiveData.getLiveData();
    }

    public LiveData<Contact> getContact() {
        return contactLiveData.getLiveData();
    }

    public LiveData<FriendshipInfo> getProfileInfo() {
        return profile;
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

    public void computeUserProfileInfo() {
        String username = getUsername().getValue();
        Connection.getInstance().getHalloappProfileInfo(userId.isMe() ? new UserId(me.getUser()) : userId, username).onResponse(response -> {
            if (!response.success) {
                // If the server does not send over info (eg person is blocked), use any old information
                Contact oldContact = ContactsDb.getInstance().getContact(userId);
                FriendshipInfo friendshipProfileInfo = new FriendshipInfo(
                        userId.isMe() ? new UserId(me.getUser()) : userId,
                        oldContact.username,
                        oldContact.halloName,
                        oldContact.avatarId,
                        oldContact.friendshipStatus,
                        System.currentTimeMillis());
                profile.postValue(friendshipProfileInfo);
                return;
            }
            FriendshipInfo friendshipProfileInfo = new FriendshipInfo(
                    userId.isMe() ? new UserId(me.getUser()) : userId,
                    response.profile.getUsername(),
                    response.profile.getName(),
                    response.profile.getAvatarId(),
                    FriendshipInfo.fromProtoType(response.profile.getStatus(), response.profile.getBlocked()),
                    System.currentTimeMillis());
            profile.postValue(friendshipProfileInfo);
        }).onError(err -> {
            Log.e("Failed to get profile info", err);
            // Use any old information in case of error
            Contact oldContact = ContactsDb.getInstance().getContact(userId);
            FriendshipInfo friendshipProfileInfo = new FriendshipInfo(
                    userId.isMe() ? new UserId(me.getUser()) : userId,
                    oldContact.username,
                    oldContact.halloName,
                    oldContact.avatarId,
                    oldContact.friendshipStatus,
                    System.currentTimeMillis());
            profile.postValue(friendshipProfileInfo);
        });
    }

    public LiveData<Boolean> removeFriend(@NonNull UserId userId) {
        MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
        Connection.getInstance().removeFriend(userId).onResponse(response -> {
            if (!response.success) {
                Log.e("Unable to remove friend " + userId);
                result.postValue(false);
            }
            ContactsDb.getInstance().removeFriendship(response.info);
            result.postValue(true);
        }).onError(e -> {
            Log.e("Unable to remove friend", e);
            result.postValue(false);
        });
        return result;
    }

    public LiveData<Boolean> rejectFriendRequest(@NonNull UserId userId) {
        MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
        Connection.getInstance().rejectFriendRequest(userId).onResponse(response -> {
            if (!response.success) {
                Log.e("Unable to reject the friend request of " + userId);
                result.postValue(false);
            }
            ContactsDb.getInstance().removeFriendship(response.info);
            result.postValue(true);
        }).onError(e -> {
            Log.e("Unable to reject friend request", e);
            result.postValue(false);
        });
        return result;
    }

    public LiveData<Boolean> updateFriendship(int friendType, @NonNull UserId userId) {
        MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
        if (friendType == FriendshipInfo.Type.INCOMING_PENDING) {
            Connection.getInstance().acceptFriendRequest(userId).onResponse(response -> {
                if (!response.success) {
                    Log.e("Unable to confirm the friend request of " + userId);
                    result.postValue(false);
                } else {
                    ContactsDb.getInstance().addFriendship(response.info);
                    result.postValue(true);
                }
            }).onError(e -> {
                Log.e("Unable to confirm friend request", e);
                result.postValue(false);
            });
        } else if (friendType == FriendshipInfo.Type.OUTGOING_PENDING) {
            Connection.getInstance().withdrawFriendRequest(userId).onResponse(response -> {
                if (!response.success) {
                    Log.e("Unable to withdraw the friend request of " + userId);
                    result.postValue(false);
                } else {
                    ContactsDb.getInstance().removeFriendship(response.info);
                    result.postValue(true);
                }
            }).onError(e -> {
                Log.e("Unable to withdraw friend request", e);
                result.postValue(false);
            });
        } else if (friendType == FriendshipInfo.Type.NONE_STATUS) {
            Connection.getInstance().sendFriendRequest(userId).onResponse(response -> {
                if (!response.success) {
                    Log.e("Unable to send a friend request to " + userId);
                    result.postValue(false);
                } else {
                    ContactsDb.getInstance().addFriendship(response.info);
                    result.postValue(true);
                }
            }).onError(e -> {
                Log.e("Unable to send friend request", e);
                result.postValue(false);
            });
        }
        return result;
    }
}
