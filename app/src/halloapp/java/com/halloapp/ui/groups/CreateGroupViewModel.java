package com.halloapp.ui.groups;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.MainThread;
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

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.groups.GroupInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.ExpiryInfo;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.BgWorkers;
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
import java.util.ArrayList;
import java.util.List;

public class CreateGroupViewModel extends AndroidViewModel {

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final WorkManager workManager;

    private final MutableLiveData<Bitmap> avatarLiveData = new MutableLiveData<>();

    private String avatarFile;
    private String largeAvatarFile;

    private final MutableLiveData<Integer> groupExpiryLiveData = new MutableLiveData<>(SelectGroupExpiryDialogFragment.OPTION_30_DAYS);

    public CreateGroupViewModel(@NonNull Application application) {
        super(application);

        workManager = WorkManager.getInstance(application);
    }

    public LiveData<Bitmap> getAvatar() {
        return avatarLiveData;
    }

    public void setAvatar(@NonNull String filepath, @NonNull String largeFilepath) {
        this.avatarFile = filepath;
        this.largeAvatarFile = largeFilepath;
        bgWorkers.execute(() -> avatarLiveData.postValue(BitmapFactory.decodeFile(filepath)));
    }

    public LiveData<List<WorkInfo>> getCreateGroupWorkInfo() {
        return workManager.getWorkInfosForUniqueWorkLiveData(CreateGroupWorker.WORK_NAME);
    }

    public String getAvatarFile() {
        return avatarFile;
    }

    public String getLargeAvatarFile() {
        return largeAvatarFile;
    }

    public void setContentExpiry(int expiry) {
        groupExpiryLiveData.setValue(expiry);
    }

    public LiveData<Integer> getContentExpiry() {
        return groupExpiryLiveData;
    }

    @MainThread
    public void createFeedGroup(@NonNull String name, @NonNull List<UserId> userIds) {
        String[] userIdStrings = new String[userIds.size()];
        for (int i = 0; i < userIds.size(); i++) {
            userIdStrings[i] = userIds.get(i).rawId();
        }

        Data.Builder builder = new Data.Builder();
        builder.putString(CreateGroupWorker.WORKER_PARAM_GROUP_NAME, name);
        builder.putStringArray(CreateGroupWorker.WORKER_PARAM_USER_IDS, userIdStrings);
        builder.putString(CreateGroupWorker.WORKER_PARAM_AVATAR_FILE, avatarFile);
        builder.putString(CreateGroupWorker.WORKER_PARAM_LARGE_AVATAR_FILE, largeAvatarFile);
        Integer groupExpiry = groupExpiryLiveData.getValue();
        builder.putInt(CreateGroupWorker.WORKER_PARAM_GROUP_EXPIRY, groupExpiry == null ? SelectGroupExpiryDialogFragment.OPTION_30_DAYS : groupExpiry);
        final Data data = builder.build();
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(CreateGroupWorker.class).setInputData(data).build();
        workManager.enqueueUniqueWork(CreateGroupWorker.WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest);
    }

    @MainThread
    public void createGroupChat(@NonNull String name, @NonNull List<UserId> userIds) {
        String[] userIdStrings = new String[userIds.size()];
        for (int i = 0; i < userIds.size(); i++) {
            userIdStrings[i] = userIds.get(i).rawId();
        }

        Data.Builder builder = new Data.Builder();
        builder.putString(CreateGroupWorker.WORKER_PARAM_GROUP_NAME, name);
        builder.putStringArray(CreateGroupWorker.WORKER_PARAM_USER_IDS, userIdStrings);
        builder.putString(CreateGroupWorker.WORKER_PARAM_AVATAR_FILE, avatarFile);
        builder.putString(CreateGroupWorker.WORKER_PARAM_LARGE_AVATAR_FILE, largeAvatarFile);
        builder.putBoolean(CreateGroupWorker.WORKER_PARAM_IS_GROUP_CHAT, true);
        final Data data = builder.build();
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(CreateGroupWorker.class).setInputData(data).build();
        workManager.enqueueUniqueWork(CreateGroupWorker.WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest);
    }

    public static class CreateGroupWorker extends Worker {

        private static final String WORK_NAME = "create-group";

        private static final String WORKER_PARAM_GROUP_NAME = "group_name";
        private static final String WORKER_PARAM_USER_IDS = "user_ids";
        private static final String WORKER_PARAM_AVATAR_FILE = "avatar_file";
        private static final String WORKER_PARAM_LARGE_AVATAR_FILE = "large_avatar_file";
        private static final String WORKER_PARAM_GROUP_EXPIRY = "group_expiry";
        private static final String WORKER_PARAM_IS_GROUP_CHAT = "is_group_chat";

        public static final String WORKER_OUTPUT_GROUP_ID = "group_id";
        public static final String WORKER_OUTPUT_MEMBER_COUNT = "member_count";

        private final GroupsApi groupsApi = GroupsApi.getInstance();

        public CreateGroupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @Override
        public @NonNull Result doWork() {
            final String groupName = Preconditions.checkNotNull(getInputData().getString(WORKER_PARAM_GROUP_NAME));
            final String[] rawUserIds = Preconditions.checkNotNull(getInputData().getStringArray(WORKER_PARAM_USER_IDS));
            final String avatarFilePath = getInputData().getString(WORKER_PARAM_AVATAR_FILE);
            final String largeAvatarFilePath = getInputData().getString(WORKER_PARAM_LARGE_AVATAR_FILE);
            final int groupExpiry = getInputData().getInt(WORKER_PARAM_GROUP_EXPIRY, SelectGroupExpiryDialogFragment.OPTION_30_DAYS);
            final boolean isGroupChat = getInputData().getBoolean(WORKER_PARAM_IS_GROUP_CHAT, false);
            ExpiryInfo expiryInfo;
            if (groupExpiry == SelectGroupExpiryDialogFragment.OPTION_NEVER) {
                expiryInfo = ExpiryInfo.newBuilder()
                        .setExpiryType(ExpiryInfo.ExpiryType.NEVER)
                        .build();
            } else if (groupExpiry == SelectGroupExpiryDialogFragment.OPTION_24_HOURS) {
                expiryInfo = ExpiryInfo.newBuilder()
                        .setExpiryType(ExpiryInfo.ExpiryType.EXPIRES_IN_SECONDS)
                        .setExpiresInSeconds(Constants.SECONDS_PER_DAY)
                        .build();
            } else {
                expiryInfo = ExpiryInfo.newBuilder()
                        .setExpiryType(ExpiryInfo.ExpiryType.EXPIRES_IN_SECONDS)
                        .setExpiresInSeconds(Constants.SECONDS_PER_DAY * 30)
                        .build();
            }
            List<UserId> userIds = new ArrayList<>();
            for (String rawId : rawUserIds) {
                userIds.add(new UserId(rawId));
            }

            try {
                GroupInfo groupInfo;
                if (!isGroupChat) {
                    groupInfo = groupsApi.createFeedGroup(groupName, userIds, expiryInfo).await();
                } else {
                    groupInfo = groupsApi.createGroupChat(groupName, userIds).await();
                }
                GroupId groupId = groupInfo.groupId;

                if (avatarFilePath != null && largeAvatarFilePath != null) {
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

                        AvatarLoader avatarLoader = AvatarLoader.getInstance();
                        avatarLoader.reportAvatarUpdate(groupInfo.groupId, avatarId);
                    } catch (IOException e) {
                        Log.e("Failed to get base64", e);
                        return Result.failure();
                    } catch (InterruptedException | ObservableErrorException e) {
                        Log.e("Avatar upload interrupted", e);
                        return Result.failure();
                    }
                }

                // Get group invite link as we will be displaying it on the next screen, don't
                // fail if we don't succeed though.
                groupsApi.getGroupInviteLink(groupId).await();

                Data.Builder builder = new Data.Builder();
                builder.putString(WORKER_OUTPUT_GROUP_ID, groupId.rawId());
                builder.putInt(WORKER_OUTPUT_MEMBER_COUNT, userIds.size());
                Data output = builder.build();

                return Result.success(output);
            } catch (ObservableErrorException e) {
                Log.e("Create group observable error", e);
                return Result.failure();
            } catch (InterruptedException e) {
                Log.e("Interrupted while creating group", e);
                return Result.failure();
            }
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;

        Factory(@NonNull Application application) {
            this.application = application;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CreateGroupViewModel.class)) {
                //noinspection unchecked
                return (T) new CreateGroupViewModel(application);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
