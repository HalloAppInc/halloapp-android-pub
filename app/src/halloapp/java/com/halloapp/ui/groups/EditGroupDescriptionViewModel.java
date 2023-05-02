package com.halloapp.ui.groups;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.id.GroupId;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.groups.GroupsApi;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.util.List;

public class EditGroupDescriptionViewModel extends AndroidViewModel {

    private final WorkManager workManager;

    private final MutableLiveData<Boolean> descriptionChangedLiveData = new MutableLiveData<>(false);
    private final ComputableLiveData<String> groupNameDescriptionLiveData;

    private final GroupId groupId;

    private String tempDescription;
    public EditGroupDescriptionViewModel(@NonNull Application application, @NonNull GroupId groupId) {
        super(application);

        this.groupId = groupId;

        groupNameDescriptionLiveData = new ComputableLiveData<String>() {
            @Override
            protected String compute() {
                Group group = Preconditions.checkNotNull(ContentDb.getInstance().getGroupFeedOrChat(groupId));
                return group.groupDescription;
            }
        };

        workManager = WorkManager.getInstance(application);
    }

    public LiveData<List<WorkInfo>> getSaveProfileWorkInfo() {
        return workManager.getWorkInfosForUniqueWorkLiveData(UpdateGroupDescriptionWorker.WORK_NAME);
    }

    public void saveGroup() {
        startWork();
    }

    private void startWork() {
        Data.Builder builder = new Data.Builder();
        builder.putString(UpdateGroupDescriptionWorker.WORKER_PARAM_GROUP_ID, groupId.rawId());
        if (descriptionChangedLiveData.getValue() != null && descriptionChangedLiveData.getValue()) {
            builder.putString(UpdateGroupDescriptionWorker.WORKER_PARAM_DESCRIPTION, StringUtils.prepareGroupDescriptionText(tempDescription));
        }
        final Data data = builder.build();
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(UpdateGroupDescriptionWorker.class).setInputData(data).build();
        workManager.enqueueUniqueWork(UpdateGroupDescriptionWorker.WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest);
    }

    public LiveData<String> getName() {
        return groupNameDescriptionLiveData.getLiveData();
    }

    public LiveData<Boolean> canSave() {
        return descriptionChangedLiveData;
    }

    public void setTempDescription(String tempName) {
        this.tempDescription = tempName;
        descriptionChangedLiveData.setValue(!tempName.equals(groupNameDescriptionLiveData.getLiveData().getValue()));
    }


    public static class UpdateGroupDescriptionWorker extends Worker {

        private static final String WORK_NAME = "update-group-description";

        private static final String WORKER_PARAM_DESCRIPTION = "name";
        private static final String WORKER_PARAM_GROUP_ID = "group_id";

        public UpdateGroupDescriptionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @Override
        public @NonNull Result doWork() {
            final String description = getInputData().getString(WORKER_PARAM_DESCRIPTION);
            final GroupId groupId = new GroupId(Preconditions.checkNotNull(getInputData().getString(WORKER_PARAM_GROUP_ID)));
            try {
                try {
                    final Boolean result = GroupsApi.getInstance().setGroupDescription(groupId, Preconditions.checkNotNull(description)).await();
                    if (result == null || !result) {
                        return Result.failure();
                    }
                } catch (ObservableErrorException e) {
                    Log.e("Observable error updating group description", e);
                    return Result.failure();
                }
                return Result.success();
            } catch (InterruptedException e) {
                Log.e("UpdateGroupDescriptionWorker", e);
                return Result.failure();
            }
        }
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
            if (modelClass.isAssignableFrom(EditGroupDescriptionViewModel.class)) {
                //noinspection unchecked
                return (T) new EditGroupDescriptionViewModel(application, groupId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
