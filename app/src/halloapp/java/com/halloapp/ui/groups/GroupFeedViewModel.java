package com.halloapp.ui.groups;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.AppContext;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.content.Post;
import com.halloapp.content.PostsDataSource;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.util.ComputableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GroupFeedViewModel extends ViewModel {

    private static final int ADAPTER_PAGE_SIZE = 50;

    final LiveData<PagedList<Post>> postList;
    private final ContentDb contentDb = ContentDb.getInstance();
    private final ContactsDb contactsDb = ContactsDb.getInstance();
    private final AtomicBoolean loadedOutgoingPost = new AtomicBoolean(false);

    private final PostsDataSource.Factory dataSourceFactory;

    private final GroupId groupId;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private Parcelable savedScrollState;

    public final ComputableLiveData<Group> group;

    public final ComputableLiveData<List<Contact>> members;

    private final VoiceNotePlayer voiceNotePlayer;

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onPostAdded(@NonNull Post post) {
            if (groupId.equals(post.getParentGroup())) {
                invalidatePosts();
                if (post.isOutgoing()) {
                    reloadPostsAt(Long.MAX_VALUE);
                    loadedOutgoingPost.set(true);
                }
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
        public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId, @Nullable GroupId parentGroup) {
            invalidatePosts();
        }

        @Override
        public void onGroupBackgroundChanged(@NonNull GroupId gid) {
            if (groupId.equals(gid)) {
                group.invalidate();
            }
        }

        @Override
        public void onGroupMetadataChanged(@NonNull GroupId gid) {
            if (groupId.equals(gid)) {
                group.invalidate();
            }
        }

        @Override
        public void onGroupMembersChanged(@NonNull GroupId gid) {
            if (groupId.equals(gid)) {
                members.invalidate();
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


    public GroupFeedViewModel(@NonNull GroupId groupId) {
        this.groupId = groupId;

        voiceNotePlayer = new VoiceNotePlayer((Application)AppContext.getInstance().get());

        dataSourceFactory = new PostsDataSource.Factory(contentDb, null, groupId);
        postList = new LivePagedListBuilder<>(dataSourceFactory, ADAPTER_PAGE_SIZE).build();

        group = new ComputableLiveData<Group>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected Group compute() {
                return contentDb.getGroup(groupId);
            }
        };

        members = new ComputableLiveData<List<Contact>>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected List<Contact> compute() {
                List<MemberInfo> memberInfos = contentDb.getGroupMembers(groupId);
                List<Contact> contacts = new ArrayList<>();
                for (MemberInfo memberInfo : memberInfos) {
                    contacts.add(contactsDb.getContact(memberInfo.userId));
                }
                return contacts;
            }
        };

        contentDb.addObserver(contentObserver);
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

    boolean checkLoadedOutgoingPost() {
        return loadedOutgoingPost.compareAndSet(true, false);
    }

    void reloadPostsAt(long timestamp) {
        final PagedList<?> pagedList = postList.getValue();
        if (pagedList != null) {
            ((PostsDataSource)pagedList.getDataSource()).reloadAt(timestamp);
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final GroupId groupId;

        Factory(@NonNull GroupId groupId) {
            this.groupId = groupId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(GroupFeedViewModel.class)) {
                //noinspection unchecked
                return (T) new GroupFeedViewModel(groupId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    @NonNull
    public VoiceNotePlayer getVoiceNotePlayer() {
        return voiceNotePlayer;
    }
}
