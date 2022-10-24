package com.halloapp.ui.groups;

import android.app.Application;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.content.GroupPostsPreviewDataSource;
import com.halloapp.content.Post;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.DelayedProgressLiveData;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.groups.GroupsApi;

import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class GroupListViewModel extends AndroidViewModel {

    final ComputableLiveData<List<Group>> groupsList;
    final MutableLiveData<Boolean> groupPostUpdated;

    private final ContentDb contentDb;
    private final GroupsApi groupsApi;
    private final ContactsDb contactsDb;
    final GroupPostLoader groupPostLoader;

    private Parcelable savedScrollState;

    private final HashMap<GroupId, GroupPostsPreviewDataSource.Factory> groupFactories = new HashMap<>();
    private final HashMap<GroupId, LiveData<PagedList<Post>>> groupPostLists = new HashMap<>();

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsChanged() {
            groupsList.invalidate();
        }
    };

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onPostAdded(@NonNull Post post) {
            if (post.getParentGroup() == null) {
                return;
            }
            GroupId parentGroup = post.getParentGroup();
            if (groupFactories.containsKey(parentGroup)) {
                groupFactories.get(parentGroup).invalidateLatestDataSource();
            }
            groupsList.invalidate();
        }

        @Override
        public void onPostRetracted(@NonNull Post post) {
            if (post.getParentGroup() == null) {
                return;
            }
            GroupId parentGroup = post.getParentGroup();
            if (groupFactories.containsKey(parentGroup)) {
                groupFactories.get(parentGroup).invalidateLatestDataSource();
            }
            groupPostLoader.removeFromCache(parentGroup);
        }

        @Override
        public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId, @Nullable GroupId groupId) {
            if (groupId != null && groupFactories.containsKey(groupId)) {
                groupFactories.get(groupId).invalidateLatestDataSource();
            }
        }

        @Override
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
        }

        @Override
        public void onCommentAdded(@NonNull Comment comment) {
            Post parentPost = comment.getParentPost();
            if (parentPost != null && parentPost.getParentGroup() != null) {
                GroupId parentGroup = parentPost.getParentGroup();
                if (groupFactories.containsKey(parentGroup)) {
                    groupFactories.get(parentGroup).invalidateLatestDataSource();
                }
            }
        }

        @Override
        public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId, @Nullable GroupId parentGroup) {
            if (parentGroup != null) {
                if (groupFactories.containsKey(parentGroup)) {
                    groupFactories.get(parentGroup).invalidateLatestDataSource();
                }
            }
        }

        @Override
        public void onGroupFeedAdded(@NonNull GroupId groupId) {
            invalidateGroups();
        }

        @Override
        public void onGroupMetadataChanged(@NonNull GroupId groupId) {
            invalidateGroups();
        }

        @Override
        public void onGroupSeen(@NonNull GroupId groupId) {
            invalidateGroups();
        }

        @Override
        public void onGroupDeleted(@NonNull GroupId groupId) {
            invalidateGroups();
        }

        private void invalidateGroups() {
            groupsList.invalidate();
        }
    };

    public GroupListViewModel(@NonNull Application application) {
        super(application);

        contactsDb = ContactsDb.getInstance();
        contactsDb.addObserver(contactsObserver);
        groupsApi = GroupsApi.getInstance();

        contentDb = ContentDb.getInstance();
        contentDb.addObserver(contentObserver);

        groupPostLoader = new GroupPostLoader();
        groupsList = new ComputableLiveData<List<Group>>() {
            @Override
            protected List<Group> compute() {
                final List<Group> groups = contentDb.getGroups();
                final Collator collator = Collator.getInstance(Locale.getDefault());
                Collections.sort(groups, (obj1, obj2) -> {
                    if (obj2.timestamp == obj1.timestamp) {
                        if (Objects.equals(obj1.name, obj2.name)) {
                            return 0;
                        }
                        if (obj1.name == null) {
                            return 1;
                        }
                        if (obj2.name == null) {
                            return -1;
                        }
                        return collator.compare(obj1.name, obj2.name);
                    }
                    return obj1.timestamp < obj2.timestamp ? 1 : -1;
                });
                return groups;
            }
        };

        groupPostUpdated = new MutableLiveData<>(false);
    }

    public void refreshAll() {
        groupsList.invalidate();
        for (GroupPostsPreviewDataSource.Factory factory : groupFactories.values()) {
            factory.invalidateLatestDataSource();
        }
    }

    private GroupPostsPreviewDataSource.Factory getOrCreateFactory(@NonNull GroupId groupId) {
        GroupPostsPreviewDataSource.Factory factory;
        if (!groupFactories.containsKey(groupId)) {
            factory = new GroupPostsPreviewDataSource.Factory(contentDb, null, groupId);
            groupFactories.put(groupId, factory);
        } else {
            factory = groupFactories.get(groupId);
        }
        return factory;
    }

    public LiveData<PagedList<Post>> getGroupPagedList(@NonNull GroupId groupId) {
        LiveData<PagedList<Post>> list;
        if (!groupPostLists.containsKey(groupId)) {
            list = new LivePagedListBuilder<>(getOrCreateFactory(groupId), 5).build();
            groupPostLists.put(groupId, list);
        } else {
            list = groupPostLists.get(groupId);
        }

        return list;
    }

    public void saveScrollState(@Nullable Parcelable savedScrollState) {
        this.savedScrollState = savedScrollState;
    }

    public @Nullable Parcelable getSavedScrollState() {
        return savedScrollState;
    }

    public LiveData<Boolean> leaveGroup(Collection<GroupId> groupIds) {
        MediatorLiveData<Boolean> combine = new MediatorLiveData<>();
        HashSet<GroupId> waitingIds = new HashSet<>(groupIds);
        for (GroupId groupId : groupIds) {
            MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
            combine.addSource(result, requestResult -> {
                if (combine.getValue() != null && !combine.getValue()) {
                    return;
                }
                waitingIds.remove(groupId);
                if (requestResult == null) {
                    return;
                }
                if (!requestResult) {
                    combine.setValue(false);
                } else if (waitingIds.isEmpty()) {
                    combine.setValue(true);
                }
            });
            groupsApi.leaveGroup(groupId)
                    .onResponse(result::postValue)
                    .onError(error -> {
                        Log.e("Leave group failed", error);
                        result.postValue(false);
                    });
        }
        return combine;
    }

    @Override
    protected void onCleared() {
        contactsDb.removeObserver(contactsObserver);
        contentDb.removeObserver(contentObserver);
        groupPostLoader.destroy();
    }
}
