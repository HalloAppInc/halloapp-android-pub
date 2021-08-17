package com.halloapp.ui.groups;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.id.GroupId;
import com.halloapp.xmpp.groups.GroupsApi;

public class GroupInviteLinkViewModel extends AndroidViewModel {

    private final GroupsApi groupsApi;

    private final MutableLiveData<String> inviteLink;

    private final GroupId groupId;

    public GroupInviteLinkViewModel(@NonNull Application application, @NonNull GroupId groupId) {
        super(application);

        this.groupId = groupId;

        groupsApi = GroupsApi.getInstance();

        inviteLink = new MutableLiveData<>();

        fetchInviteLink();
    }

    public LiveData<String> getInviteLink() {
        return inviteLink;
    }

    public LiveData<Boolean> resetInviteLink() {
        MutableLiveData<Boolean> inviteReset = new MutableLiveData<>();
        groupsApi.resetGroupInviteLink(groupId).onResponse(r -> {
            inviteReset.postValue(r);
            fetchInviteLink();
        }).onError(e -> inviteReset.postValue(false));
        return inviteReset;
    }

    private void fetchInviteLink() {
        groupsApi.getGroupInviteLink(groupId).onResponse(s -> inviteLink.postValue(Constants.GROUP_INVITE_BASE_URL + s));
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
            if (modelClass.isAssignableFrom(GroupInviteLinkViewModel.class)) {
                //noinspection unchecked
                return (T) new GroupInviteLinkViewModel(application, groupId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
