package com.halloapp.ui.groups;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

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

import com.halloapp.FileStore;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.groups.GroupInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.FileUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.groups.GroupsApi;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CreateGroupViewModel extends AndroidViewModel {

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final ContactsDb contactsDb = ContactsDb.getInstance();
    private final WorkManager workManager;

    private final MutableLiveData<Bitmap> avatarLiveData = new MutableLiveData<>();
    private final ComputableLiveData<List<Contact>> contactsLiveData;

    private String avatarFile;

    public CreateGroupViewModel(@NonNull Application application, @NonNull List<UserId> userIds) {
        super(application);

        workManager = WorkManager.getInstance(application);

        contactsLiveData = new ComputableLiveData<List<Contact>>() {
            @Override
            protected List<Contact> compute() {
                List<Contact> ret = new ArrayList<>();
                for (UserId userId : userIds) {
                    ret.add(contactsDb.getContact(userId));
                }
                Contact.sort(ret);
                return ret;
            }
        };
        contactsLiveData.invalidate();
    }

    public LiveData<Bitmap> getAvatar() {
        return avatarLiveData;
    }

    public void setAvatar(@NonNull String filepath) {
        this.avatarFile = filepath;
        bgWorkers.execute(() -> avatarLiveData.postValue(BitmapFactory.decodeFile(filepath)));
    }

    public LiveData<List<Contact>> getContacts() {
        return contactsLiveData.getLiveData();
    }

    public LiveData<List<WorkInfo>> getCreateGroupWorkInfo() {
        return workManager.getWorkInfosForUniqueWorkLiveData(CreateGroupWorker.WORK_NAME);
    }

    @MainThread
    public void createGroup(@NonNull String name, @NonNull List<UserId> userIds) {
        String[] userIdStrings = new String[userIds.size()];
        for (int i = 0; i < userIds.size(); i++) {
            userIdStrings[i] = userIds.get(i).rawId();
        }

        Data.Builder builder = new Data.Builder();
        builder.putString(CreateGroupWorker.WORKER_PARAM_GROUP_NAME, name);
        builder.putStringArray(CreateGroupWorker.WORKER_PARAM_USER_IDS, userIdStrings);
        builder.putString(CreateGroupWorker.WORKER_PARAM_AVATAR_FILE, avatarFile);
        final Data data = builder.build();
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(CreateGroupWorker.class).setInputData(data).build();
        workManager.enqueueUniqueWork(CreateGroupWorker.WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest);
    }

    public static class CreateGroupWorker extends Worker {

        private static final String WORK_NAME = "create-group";

        private static final String WORKER_PARAM_GROUP_NAME = "group_name";
        private static final String WORKER_PARAM_USER_IDS = "user_ids";
        private static final String WORKER_PARAM_AVATAR_FILE = "avatar_file";

        public static final String WORKER_OUTPUT_GROUP_ID = "group_id";

        private final GroupsApi groupsApi = GroupsApi.getInstance();

        public CreateGroupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @Override
        public @NonNull Result doWork() {
            final String groupName = Preconditions.checkNotNull(getInputData().getString(WORKER_PARAM_GROUP_NAME));
            final String[] rawUserIds = Preconditions.checkNotNull(getInputData().getStringArray(WORKER_PARAM_USER_IDS));
            final String avatarFilePath = getInputData().getString(WORKER_PARAM_AVATAR_FILE);

            List<UserId> userIds = new ArrayList<>();
            for (String rawId : rawUserIds) {
                userIds.add(new UserId(rawId));
            }

            try {
                GroupInfo groupInfo = groupsApi.createGroup(groupName, userIds).await();
                GroupId groupId = groupInfo.groupId;

                if (avatarFilePath != null) {
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

                        // TODO(jack): make all the avatar code support GroupIds
//                        AvatarLoader avatarLoader = AvatarLoader.getInstance(getApplicationContext());
//                        avatarLoader.reportAvatarUpdate(groupInfo.groupId, avatarId);
                    } catch (IOException e) {
                        Log.e("Failed to get base64", e);
                        return Result.failure();
                    } catch (InterruptedException | ExecutionException e) {
                        Log.e("Avatar upload interrupted", e);
                        return Result.failure();
                    }
                }

                Data.Builder builder = new Data.Builder();
                builder.putString(WORKER_OUTPUT_GROUP_ID, groupId.rawId());
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
        private final List<UserId> userIds;

        Factory(@NonNull Application application, @NonNull List<UserId> userIds) {
            this.application = application;
            this.userIds = userIds;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CreateGroupViewModel.class)) {
                //noinspection unchecked
                return (T) new CreateGroupViewModel(application, userIds);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
