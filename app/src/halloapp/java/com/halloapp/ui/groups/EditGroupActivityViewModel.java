package com.halloapp.ui.groups;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

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
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.id.GroupId;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.groups.GroupsApi;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class EditGroupActivityViewModel extends AndroidViewModel {

    private final BgWorkers bgWorkers;

    private final WorkManager workManager;

    private final MutableLiveData<Bitmap> tempAvatarLiveData;
    private final MutableLiveData<Boolean> nameChangedLiveData = new MutableLiveData<>(false);
    private final MediatorLiveData<Boolean> canSave;
    private final MutableLiveData<Boolean> hasAvatarSet = new MutableLiveData<>();
    private final ComputableLiveData<String> groupNameLiveData;

    private final GroupId groupId;

    private String tempName;

    private String avatarFile;
    private String largeAvatarFile;
    private Integer avatarWidth;
    private Integer avatarHeight;
    private boolean avatarDeleted = false;

    public EditGroupActivityViewModel(@NonNull Application application, @NonNull GroupId groupId) {
        super(application);

        this.groupId = groupId;

        groupNameLiveData = new ComputableLiveData<String>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected String compute() {
                Group group = Preconditions.checkNotNull(ContentDb.getInstance().getGroupFeedOrChat(groupId));
                return group.name;
            }
        };

        bgWorkers = BgWorkers.getInstance();

        workManager = WorkManager.getInstance(application);

        tempAvatarLiveData = new MutableLiveData<>();
        canSave = new MediatorLiveData<>();
        canSave.addSource(nameChangedLiveData, nameChanged -> setCanSave());
        canSave.addSource(tempAvatarLiveData, bitmap -> setCanSave());
        bgWorkers.execute(() -> {
            hasAvatarSet.postValue(AvatarLoader.getInstance().hasAvatar(groupId));
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
            builder.putString(UpdateGroupWorker.WORKER_PARAM_LARGE_AVATAR_FILE, largeAvatarFile);
        }
        if (avatarDeleted) {
            builder.putBoolean(UpdateGroupWorker.WORKER_PARAM_AVATAR_REMOVAL, true);
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

    public void setTempAvatar(@NonNull String filepath, @NonNull String largeFilepath, int width, int height) {
        this.avatarWidth = width;
        this.avatarHeight = height;
        this.avatarFile = filepath;
        this.largeAvatarFile = largeFilepath;
        bgWorkers.execute(() -> tempAvatarLiveData.postValue(BitmapFactory.decodeFile(filepath)));
        hasAvatarSet.setValue(true);
        avatarDeleted = false;
    }

    public LiveData<Boolean> canSave() {
        return canSave;
    }

    public LiveData<Boolean> getHasAvatarSet() {
        return hasAvatarSet;
    }

    private void setCanSave() {
        boolean nameChanged = nameChangedLiveData.getValue() != null ? nameChangedLiveData.getValue() : false;
        boolean avatarChanged = tempAvatarLiveData.getValue() != null;
        canSave.setValue(nameChanged || avatarChanged || avatarDeleted);
    }

    public boolean hasChanges() {
        Boolean hasChanges = canSave.getValue();
        return hasChanges != null && hasChanges;
    }

    public void setTempName(String tempName) {
        this.tempName = tempName;
        nameChangedLiveData.setValue(!tempName.equals(groupNameLiveData.getLiveData().getValue()));
    }

    public void removeAvatar() {
        avatarDeleted = true;
        tempAvatarLiveData.setValue(null);
        hasAvatarSet.setValue(false);
    }


    public static class UpdateGroupWorker extends Worker {

        private static final String WORK_NAME = "update-group";

        private static final String WORKER_PARAM_AVATAR_FILE = "avatar_file";
        private static final String WORKER_PARAM_LARGE_AVATAR_FILE = "large_avatar_file";
        private static final String WORKER_PARAM_AVATAR_WIDTH = "avatar_width";
        private static final String WORKER_PARAM_AVATAR_HEIGHT = "avatar_height";
        private static final String WORKER_PARAM_NAME = "name";
        private static final String WORKER_PARAM_AVATAR_REMOVAL = "avatar_removal";
        private static final String WORKER_PARAM_GROUP_ID = "group_id";

        private final AvatarLoader avatarLoader;

        public UpdateGroupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
            avatarLoader = AvatarLoader.getInstance();
        }

        @Override
        public @NonNull Result doWork() {
            final String name = getInputData().getString(WORKER_PARAM_NAME);
            final GroupId groupId = new GroupId(Preconditions.checkNotNull(getInputData().getString(WORKER_PARAM_GROUP_ID)));
            final String avatarFilePath = getInputData().getString(WORKER_PARAM_AVATAR_FILE);
            final String largeAvatarFilePath = getInputData().getString(WORKER_PARAM_LARGE_AVATAR_FILE);
            final boolean avatarDeleted = getInputData().getBoolean(WORKER_PARAM_AVATAR_REMOVAL,false);
            int avatarWidth = getInputData().getInt(WORKER_PARAM_AVATAR_WIDTH, -1);
            int avatarHeight = getInputData().getInt(WORKER_PARAM_AVATAR_HEIGHT, -1);
            try {
                if (!TextUtils.isEmpty(name)) {
                    try {
                        final String result = GroupsApi.getInstance().setGroupName(groupId, Preconditions.checkNotNull(name)).await();
                        if (result == null) {
                            return Result.failure();
                        }
                    } catch (ObservableErrorException e) {
                        Log.w("Observable error updating group name", e);
                        return Result.failure();
                    }
                }
                if (avatarFilePath != null && largeAvatarFilePath != null && avatarWidth > 0 && avatarHeight > 0) {
                    File avatarFile = new File(avatarFilePath);
                    File largeAvatarFile = new File(largeAvatarFilePath);
                    try (FileInputStream fileInputStream = new FileInputStream(avatarFile);
                         FileInputStream largeFileInputStream = new FileInputStream(largeAvatarFile)) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int c;
                        while ((c = fileInputStream.read(buf)) != -1) {
                            baos.write(buf, 0, c);
                        }
                        byte[] fileBytes = baos.toByteArray();

                        baos.reset();
                        while ((c = largeFileInputStream.read(buf)) != -1) {
                            baos.write(buf, 0, c);
                        }
                        byte[] largeFileBytes = baos.toByteArray();

                        String avatarId = Connection.getInstance().setGroupAvatar(groupId, fileBytes, largeFileBytes).await();
                        if (avatarId == null) {
                            return Result.failure();
                        }
                        final File outFile = FileStore.getInstance().getAvatarFile(groupId.rawId());
                        final File largeOutFile = FileStore.getInstance().getAvatarFile(groupId.rawId(), true);
                        FileUtils.copyFile(avatarFile, outFile);
                        FileUtils.copyFile(largeAvatarFile, largeOutFile);
                        avatarLoader.reportAvatarUpdate(groupId, avatarId);
                    } catch (IOException e) {
                        Log.e("Failed to get base64", e);
                        return Result.failure();
                    } catch (InterruptedException | ObservableErrorException e) {
                        Log.e("Avatar upload interrupted", e);
                        return Result.failure();
                    }
                }
                if (avatarDeleted) {
                    avatarLoader.removeAvatar(groupId);
                    Connection.getInstance().removeGroupAvatar(groupId);
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
