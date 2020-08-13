package com.halloapp.ui.groups;

import android.app.Application;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.groups.GroupInfo;
import com.halloapp.id.UserId;
import com.halloapp.util.DelayedProgressLiveData;
import com.halloapp.util.Log;
import com.halloapp.xmpp.groups.GroupsApi;

import java.util.List;

public class CreateGroupViewModel extends AndroidViewModel {

    private GroupsApi groupsApi = GroupsApi.getInstance();

    public CreateGroupViewModel(@NonNull Application application) {
        super(application);
    }

    @MainThread
    public LiveData<GroupInfo> createGroup(@NonNull String name, @NonNull List<UserId> userIds) {
        MutableLiveData<GroupInfo> result = new DelayedProgressLiveData<>();
        groupsApi.createGroup(name, userIds)
        .onResponse(result::postValue)
        .onError(error -> {
            Log.e("Create group failed", error);
            result.postValue(null);
        });
        return result;
    }
}
