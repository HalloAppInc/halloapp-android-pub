package com.halloapp.ui.groups;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Me;
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.DelayedProgressLiveData;
import com.halloapp.util.Log;
import com.halloapp.xmpp.groups.GroupsApi;
import com.halloapp.xmpp.groups.MemberElement;

import java.util.Collections;
import java.util.List;

public class GroupViewModel extends AndroidViewModel {

    private final ContentDb contentDb;
    private final GroupsApi groupsApi;

    private final GroupId groupId;

    private final ComputableLiveData<Chat> chatLiveData;
    private final ComputableLiveData<List<MemberInfo>> membersLiveData;

    private final MutableLiveData<Boolean> userIsAdmin = new MutableLiveData<>();
    private final MutableLiveData<Boolean> chatIsActive = new MutableLiveData<>();

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onGroupMetadataChanged(@NonNull GroupId groupId) {
            if (groupId.equals(GroupViewModel.this.groupId)) {
                invalidateChat();
            }
        }

        @Override
        public void onGroupMembersChanged(@NonNull GroupId groupId) {
            if (groupId.equals(GroupViewModel.this.groupId)) {
                invalidateMembers();
            }
        }

        @Override
        public void onGroupAdminsChanged(@NonNull GroupId groupId) {
            if (groupId.equals(GroupViewModel.this.groupId)) {
                invalidateMembers();
            }
        }

        @Override
        public void onChatDeleted(@NonNull ChatId chatId) {
            // TODO(jack): handle chat deletion
        }

        private void invalidateChat() {
            chatLiveData.invalidate();
        }

        private void invalidateMembers() {
            membersLiveData.invalidate();
        }
    };

    public GroupViewModel(@NonNull Application application, @NonNull GroupId groupId) {
        super(application);

        this.groupId = groupId;

        contentDb = ContentDb.getInstance(application);
        contentDb.addObserver(contentObserver);
        groupsApi = GroupsApi.getInstance();

        chatLiveData = new ComputableLiveData<Chat>() {
            @Override
            protected Chat compute() {
                Chat chat = contentDb.getChat(groupId);
                chatIsActive.postValue(chat != null && chat.isActive);
                return chat;
            }
        };
        chatLiveData.invalidate();

        membersLiveData = new ComputableLiveData<List<MemberInfo>>() {
            @Override
            protected List<MemberInfo> compute() {
                List<MemberInfo> members = contentDb.getGroupMembers(groupId);
                for (MemberInfo member : members) {
                    if (member.userId.rawId().equals(Me.getInstance().getUser())) {
                        userIsAdmin.postValue(MemberElement.Type.ADMIN.equals(member.type));
                        break;
                    }
                }
                return members;
            }
        };
        membersLiveData.invalidate();
    }

    public LiveData<Chat> getChat() {
        return chatLiveData.getLiveData();
    }

    public LiveData<List<MemberInfo>> getMembers() {
        return membersLiveData.getLiveData();
    }

    public LiveData<Boolean> getUserIsAdmin() {
        return userIsAdmin;
    }

    public LiveData<Boolean> getChatIsActive() {
        return chatIsActive;
    }

    public LiveData<Boolean> addMembers(List<UserId> userIds) {
        MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
        groupsApi.addRemoveMembers(groupId, userIds, null)
                .onResponse(result::postValue)
                .onError(error -> {
                    Log.e("Add members failed", error);
                    result.postValue(false);
                });
        return result;
    }

    public LiveData<Boolean> removeMember(UserId userId) {
        MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
        groupsApi.addRemoveMembers(groupId, null, Collections.singletonList(userId))
                .onResponse(result::postValue)
                .onError(error -> {
                    Log.e("Remove member failed", error);
                    result.postValue(false);
                });
        return result;
    }

    public LiveData<Boolean> promoteAdmin(UserId userId) {
        MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
        groupsApi.promoteDemoteAdmins(groupId, Collections.singletonList(userId), null)
                .onResponse(result::postValue)
                .onError(error -> {
                    Log.e("Promote admin failed", error);
                    result.postValue(false);
                });
        return result;
    }

    public LiveData<Boolean> demoteAdmin(UserId userId) {
        MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
        groupsApi.promoteDemoteAdmins(groupId, null, Collections.singletonList(userId))
                .onResponse(result::postValue)
                .onError(error -> {
                    Log.e("Demote admin failed", error);
                    result.postValue(false);
                });
        return result;
    }

    public LiveData<Boolean> leaveGroup() {
        MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
        groupsApi.leaveGroup(groupId)
                .onResponse(result::postValue)
                .onError(error -> {
                    Log.e("Leave group failed", error);
                    result.postValue(false);
                });
        return result;
    }

    public LiveData<Boolean> deleteGroup() {
        MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
        groupsApi.deleteGroup(groupId)
                .onResponse(result::postValue)
                .onError(error -> {
                    Log.e("Delete group failed", error);
                    result.postValue(false);
                });
        return result;
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
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
            if (modelClass.isAssignableFrom(GroupViewModel.class)) {
                //noinspection unchecked
                return (T) new GroupViewModel(application, groupId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
