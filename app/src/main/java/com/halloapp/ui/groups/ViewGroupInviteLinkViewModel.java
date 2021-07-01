package com.halloapp.ui.groups;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.groups.GroupInfo;
import com.halloapp.registration.CheckRegistration;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.xmpp.groups.GroupsApi;


public class ViewGroupInviteLinkViewModel extends AndroidViewModel{

    private final Me me = Me.getInstance();
    private final GroupsApi groupsApi = GroupsApi.getInstance();
    private final Preferences preferences = Preferences.getInstance();

    private final String linkCode;

    final ComputableLiveData<CheckRegistration.CheckResult> registrationStatus;
    private final MutableLiveData<InviteLinkResult> inviteLinkPreview;

    public static class InviteLinkResult {
        public final GroupInfo groupInfo;

        private InviteLinkResult(@Nullable GroupInfo groupInfo) {
            this.groupInfo = groupInfo;
        }
    }

    public ViewGroupInviteLinkViewModel(@NonNull Application application, @NonNull String linkCode) {
        super(application);

        this.linkCode = linkCode;

        inviteLinkPreview = new MutableLiveData<>();

        registrationStatus = new ComputableLiveData<CheckRegistration.CheckResult>() {
            @Override
            protected CheckRegistration.CheckResult compute() {
                return CheckRegistration.checkRegistration(me, preferences);
            }
        };

        fetchInvitePreview();
    }

    public LiveData<InviteLinkResult> getInvitePreview() {
        return inviteLinkPreview;
    }

    public LiveData<Boolean> joinGroup() {
        MutableLiveData<Boolean> requestLiveData = new MutableLiveData<>();
        groupsApi.joinGroupViaInviteLink(linkCode).onResponse(requestLiveData::postValue).onError(e -> {
            requestLiveData.postValue(false);
        });
        return requestLiveData;
    }

    private void fetchInvitePreview() {
        groupsApi.previewGroupInviteLink(linkCode).onResponse(info -> {
            inviteLinkPreview.postValue(new InviteLinkResult(info));
        }).onError(e -> {
            inviteLinkPreview.postValue(new InviteLinkResult(null));
        });
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final String linkCode;

        public Factory(@NonNull Application application, @NonNull String linkCode) {
            this.application = application;
            this.linkCode = linkCode;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ViewGroupInviteLinkViewModel.class)) {
                //noinspection unchecked
                return (T) new ViewGroupInviteLinkViewModel(application, linkCode);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
