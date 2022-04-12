package com.halloapp.ui.groups;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.id.GroupId;
import com.halloapp.util.BgWorkers;
import com.halloapp.xmpp.groups.GroupsApi;

public class GroupBackgroundViewModel extends ViewModel {

    private final GroupId groupId;

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final ContentDb contentDb = ContentDb.getInstance();
    private final GroupsApi groupsApi = GroupsApi.getInstance();

    private final MutableLiveData<Integer> selectedBackground = new MutableLiveData<>();

    public GroupBackgroundViewModel(@NonNull GroupId groupId) {
        this.groupId = groupId;

        bgWorkers.execute(() -> {
            Group group = contentDb.getGroup(groupId);
            if (group == null) {
                return;
            }
            selectedBackground.postValue(group.theme);
        });
    }

    public LiveData<Integer> getBackground() {
        return selectedBackground;
    }

    public void setBackground(int theme) {
        selectedBackground.postValue(theme);
    }

    public LiveData<Boolean> saveBackground() {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        Integer theme = selectedBackground.getValue();
        if (theme == null) {
            theme = 0;
        }
        groupsApi.setGroupBackground(groupId, theme)
                .onResponse(result::postValue)
                .onError(e -> result.postValue(false));
        return result;
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final GroupId groupId;

        Factory(@NonNull GroupId groupId) {
            this.groupId = groupId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(GroupBackgroundViewModel.class)) {
                //noinspection unchecked
                return (T) new GroupBackgroundViewModel(groupId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
