package com.halloapp.ui.settings;

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
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.FileStore;
import com.halloapp.Me;
import com.halloapp.id.UserId;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.FileUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class SettingsProfileViewModel extends AndroidViewModel {

    private Me me;
    private BgWorkers bgWorkers;

    private WorkManager workManager;

    private MutableLiveData<Bitmap> tempAvatarLiveData;
    private MutableLiveData<Boolean> nameChangedLiveData = new MutableLiveData<>(false);
    private MediatorLiveData<Boolean> canSave;
    private MutableLiveData<Boolean> hasAvatarSet = new MutableLiveData<>();

    private String tempName;

    private String avatarFile;
    private Integer avatarWidth;
    private Integer avatarHeight;
    private boolean avatarDeleted = false;

    public SettingsProfileViewModel(@NonNull Application application) {
        super(application);

        me = Me.getInstance();
        bgWorkers = BgWorkers.getInstance();

        workManager = WorkManager.getInstance(application);

        tempAvatarLiveData = new MutableLiveData<>();
        bgWorkers.execute(() -> me.getName());
        canSave = new MediatorLiveData<>();
        canSave.addSource(nameChangedLiveData, nameChanged -> {
            setCanSave();
        });
        canSave.addSource(tempAvatarLiveData, bitmap -> {
            setCanSave();
        });
        bgWorkers.execute(() -> {
            hasAvatarSet.postValue(AvatarLoader.getInstance().hasAvatar());
        });

    }
    public void removeAvatar() {
        avatarDeleted = true;
        tempAvatarLiveData.setValue(null);
        hasAvatarSet.setValue(false);
    }

    public LiveData<List<WorkInfo>> getSaveProfileWorkInfo() {
        return workManager.getWorkInfosForUniqueWorkLiveData(UpdateProfileWorker.WORK_NAME);
    }

    public LiveData<Boolean> getHasAvatarSet() {
        return hasAvatarSet;
    }

    public void saveProfile() {
        sendName();
    }

    private void sendName() {
        Data.Builder builder = new Data.Builder();
        if (nameChangedLiveData.getValue() != null && nameChangedLiveData.getValue()) {
            builder.putString(UpdateProfileWorker.WORKER_PARAM_NAME, tempName);
        }
        if (tempAvatarLiveData.getValue() != null && avatarHeight != null && avatarWidth != null) {
            builder.putInt(UpdateProfileWorker.WORKER_PARAM_AVATAR_HEIGHT, avatarHeight);
            builder.putInt(UpdateProfileWorker.WORKER_PARAM_AVATAR_WIDTH, avatarWidth);
            builder.putString(UpdateProfileWorker.WORKER_PARAM_AVATAR_FILE, avatarFile);
        }
        if (avatarDeleted) {
            builder.putBoolean(UpdateProfileWorker.WORKER_PARAM_AVATAR_REMOVAL, true);
        }
        final Data data = builder.build();
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(UpdateProfileWorker.class).setInputData(data).build();
        workManager.enqueueUniqueWork(UpdateProfileWorker.WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest);
    }

    private void setCanSave() {
        boolean nameChanged = nameChangedLiveData.getValue() != null ? nameChangedLiveData.getValue() : false;
        boolean avatarChanged = tempAvatarLiveData.getValue() != null;
        canSave.setValue(nameChanged || avatarChanged || avatarDeleted);
    }

    public LiveData<String> getName() {
        return me.name;
    }

    public LiveData<Bitmap> getTempAvatar() {
        return tempAvatarLiveData;
    }

    public void setTempAvatar(@NonNull String filepath, int width, int height) {
        this.avatarWidth = width;
        this.avatarHeight = height;
        this.avatarFile = filepath;
        bgWorkers.execute(() -> tempAvatarLiveData.postValue(BitmapFactory.decodeFile(filepath)));
        hasAvatarSet.setValue(true);
        avatarDeleted = false;
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
        nameChangedLiveData.setValue(!tempName.equals(me.name.getValue()));
    }


    public static class UpdateProfileWorker extends Worker {

        private static final String WORK_NAME = "set-name";

        private static final String WORKER_PARAM_AVATAR_FILE = "avatar_file";
        private static final String WORKER_PARAM_AVATAR_WIDTH = "avatar_width";
        private static final String WORKER_PARAM_AVATAR_HEIGHT = "avatar_height";
        private static final String WORKER_PARAM_NAME = "name";
        private static final String WORKER_PARAM_AVATAR_REMOVAL = "avatar_removal";

        public UpdateProfileWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @Override
        public @NonNull Result doWork() {
            final String name = getInputData().getString(WORKER_PARAM_NAME);
            final String avatarFilePath = getInputData().getString(WORKER_PARAM_AVATAR_FILE);
            final boolean avatarDeleted = getInputData().getBoolean(WORKER_PARAM_AVATAR_REMOVAL,false);
            int avatarWidth = getInputData().getInt(WORKER_PARAM_AVATAR_WIDTH, -1);
            int avatarHeight = getInputData().getInt(WORKER_PARAM_AVATAR_HEIGHT, -1);
            try {
                if (!TextUtils.isEmpty(name)) {
                    Connection.getInstance().sendName(Preconditions.checkNotNull(name)).await();
                    Me.getInstance().saveName(name);
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
                        String avatarId = Connection.getInstance().setAvatar(base64, fileBytes.length, avatarWidth, avatarHeight).await();
                        if (avatarId == null) {
                            return Result.failure();
                        }
                        final File outFile = FileStore.getInstance().getAvatarFile(UserId.ME.rawId());
                        FileUtils.copyFile(avatarFile, outFile);
                        AvatarLoader avatarLoader = AvatarLoader.getInstance();
                        avatarLoader.reportMyAvatarChanged(avatarId);
                    } catch (IOException e) {
                        Log.e("Failed to get base64", e);
                        return Result.failure();
                    } catch (InterruptedException | ObservableErrorException e) {
                        Log.e("Avatar upload interrupted", e);
                        return Result.failure();
                    }
                }
                if (avatarDeleted) {
                    AvatarLoader avatarLoader = AvatarLoader.getInstance();
                    avatarLoader.removeMyAvatar();
                    Connection.getInstance().removeAvatar();
                }
                return Result.success();
            } catch (InterruptedException | ObservableErrorException e) {
                Log.e("UpdateProfileWorker", e);
                return Result.failure();
            }
        }
    }
}
