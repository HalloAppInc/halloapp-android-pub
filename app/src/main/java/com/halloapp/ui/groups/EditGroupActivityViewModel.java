package com.halloapp.ui.groups;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
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

import com.halloapp.FileStore;
import com.halloapp.content.ContentDb;
import com.halloapp.id.GroupId;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.groups.GroupsApi;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EditGroupActivityViewModel extends AndroidViewModel {

    private BgWorkers bgWorkers;

    private WorkManager workManager;

    private MutableLiveData<Bitmap> tempAvatarLiveData;
    private MutableLiveData<Boolean> nameChangedLiveData = new MutableLiveData<>(false);
    private MediatorLiveData<Boolean> canSave;
    private ComputableLiveData<String> groupNameLiveData;

    private final GroupId groupId;

    private String tempName;

    private String avatarFile;
    private Integer avatarWidth;
    private Integer avatarHeight;

    public EditGroupActivityViewModel(@NonNull Application application, @NonNull GroupId groupId) {
        super(application);

        this.groupId = groupId;

        groupNameLiveData = new ComputableLiveData<String>() {
            @Override
            protected String compute() {
                return ContentDb.getInstance(application).getChat(groupId).name;
            }
        };

        bgWorkers = BgWorkers.getInstance();

        workManager = WorkManager.getInstance(application);

        tempAvatarLiveData = new MutableLiveData<>();
        canSave = new MediatorLiveData<>();
        canSave.addSource(nameChangedLiveData, nameChanged -> {
            boolean avatarChanged = tempAvatarLiveData.getValue() != null;
            canSave.setValue(avatarChanged || nameChanged);
        });
        canSave.addSource(tempAvatarLiveData, bitmap -> {
            Boolean nameChanged = nameChangedLiveData.getValue();
            boolean avatarChanged = bitmap != null;
            if (nameChanged != null) {
                canSave.setValue(nameChanged || avatarChanged);
            } else {
                canSave.setValue(avatarChanged);
            }
        });
    }

    public LiveData<List<WorkInfo>> getSaveProfileWorkInfo() {
        return workManager.getWorkInfosForUniqueWorkLiveData(UpdateGroupWorker.WORK_NAME);
    }

    public void saveGroup() {
        startWork();
    }

    private void startWork() {
        Data.Builder builder = new Data.Builder();
        builder.putString(UpdateGroupWorker.WORKER_PARAM_GROUP_ID, groupId.rawId());
        if (nameChangedLiveData.getValue() != null && nameChangedLiveData.getValue()) {
            builder.putString(UpdateGroupWorker.WORKER_PARAM_NAME, tempName);
        }
        if (tempAvatarLiveData.getValue() != null && avatarHeight != null && avatarWidth != null) {
            builder.putInt(UpdateGroupWorker.WORKER_PARAM_AVATAR_HEIGHT, avatarHeight);
            builder.putInt(UpdateGroupWorker.WORKER_PARAM_AVATAR_WIDTH, avatarWidth);
            builder.putString(UpdateGroupWorker.WORKER_PARAM_AVATAR_FILE, avatarFile);
        }
        final Data data = builder.build();
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(UpdateGroupWorker.class).setInputData(data).build();
        workManager.enqueueUniqueWork(UpdateGroupWorker.WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest);
    }

    public LiveData<String> getName() {
        return groupNameLiveData.getLiveData();
    }

    public LiveData<Bitmap> getTempAvatar() {
        return tempAvatarLiveData;
    }

    public void setTempAvatar(@NonNull String filepath, int width, int height) {
        this.avatarWidth = width;
        this.avatarHeight = height;
        this.avatarFile = filepath;
        bgWorkers.execute(() -> {
            tempAvatarLiveData.postValue(BitmapFactory.decodeFile(filepath));
        });
    }

    public LiveData<Boolean> canSave() {
        return canSave;
    }

    public boolean hasChanges() {
        Boolean hasChanges = canSave.getValue();
        return hasChanges != null && hasChanges;
    }

    public void setTempName(String tempName) {
        this.tempName = tempName;
        nameChangedLiveData.setValue(!tempName.equals(groupNameLiveData.getLiveData().getValue()));
    }


    public static class UpdateGroupWorker extends Worker {

        private static final String WORK_NAME = "update-group";

        private static final String WORKER_PARAM_AVATAR_FILE = "avatar_file";
        private static final String WORKER_PARAM_AVATAR_WIDTH = "avatar_width";
        private static final String WORKER_PARAM_AVATAR_HEIGHT = "avatar_height";
        private static final String WORKER_PARAM_NAME = "name";
        private static final String WORKER_PARAM_GROUP_ID = "group_id";

        public UpdateGroupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @Override
        public @NonNull Result doWork() {
            final String name = getInputData().getString(WORKER_PARAM_NAME);
            final GroupId groupId = new GroupId(Preconditions.checkNotNull(getInputData().getString(WORKER_PARAM_GROUP_ID)));
            final String avatarFilePath = getInputData().getString(WORKER_PARAM_AVATAR_FILE);
            int avatarWidth = getInputData().getInt(WORKER_PARAM_AVATAR_WIDTH, -1);
            int avatarHeight = getInputData().getInt(WORKER_PARAM_AVATAR_HEIGHT, -1);
            try {
                if (!TextUtils.isEmpty(name)) {
                    try {
                        final String result = GroupsApi.getInstance().setGroupName(groupId, name).await();
                        if (result == null) {
                            return Result.failure();
                        }
                    } catch (ObservableErrorException e) {
                        Log.w("Observable error updating group name", e);
                        return Result.failure();
                    }
                }
                if (avatarFilePath != null && avatarWidth > 0 && avatarHeight > 0) {
                    File avatarFile = new File(avatarFilePath);
                    try (FileInputStream fileInputStream = new FileInputStream(avatarFile)) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int c;
                        while ((c = fileInputStream.read(buf)) != -1) {
                            baos.write(buf, 0, c);
                        }
                        byte[] fileBytes = baos.toByteArray();
                        String base64 = Base64.encodeToString(fileBytes, Base64.NO_WRAP);
                        String avatarId = Connection.getInstance().setGroupAvatar(groupId, base64).get();
                        if (avatarId == null) {
                            return Result.failure();
                        }
                        final File outFile = FileStore.getInstance(getApplicationContext()).getAvatarFile(groupId.rawId());
                        FileUtils.copyFile(avatarFile, outFile);
                        AvatarLoader avatarLoader = AvatarLoader.getInstance();
                        avatarLoader.reportAvatarUpdate(groupId, avatarId);
                    } catch (IOException e) {
                        Log.e("Failed to get base64", e);
                        return Result.failure();
                    } catch (InterruptedException | ExecutionException e) {
                        Log.e("Avatar upload interrupted", e);
                        return Result.failure();
                    }
                }
                return Result.success();
            } catch (InterruptedException e) {
                Log.e("UpdateGroupWorker", e);
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
            if (modelClass.isAssignableFrom(EditGroupActivityViewModel.class)) {
                //noinspection unchecked
                return (T) new EditGroupActivityViewModel(application, groupId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
