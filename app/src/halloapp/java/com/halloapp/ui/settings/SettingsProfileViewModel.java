package com.halloapp.ui.settings;

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
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.AppContext;
import com.halloapp.BuildConfig;
import com.halloapp.FileStore;
import com.halloapp.Me;
import com.halloapp.Notifications;
import com.halloapp.Preferences;
import com.halloapp.contacts.SocialLink;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.Link;
import com.halloapp.proto.server.SetLinkRequest;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.FileUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.UsernameResponseIq;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsProfileViewModel extends AndroidViewModel {

    private final Me me;
    private final BgWorkers bgWorkers;

    private final WorkManager workManager;

    private final MutableLiveData<Bitmap> tempAvatarLiveData;
    private final MutableLiveData<Boolean> nameChangedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> usernameChangedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> linksChangedLiveData = new MutableLiveData<>(false);
    private final MediatorLiveData<Boolean> canSave;
    private final MutableLiveData<Boolean> hasAvatarSet = new MutableLiveData<>();

    private String tempName;
    private String tempUsername;
    private final MutableLiveData<List<SocialLink>> tempLinksLiveData = new MutableLiveData<>();
    private ArrayList<SocialLink> serverLinks = new ArrayList<>();

    private String avatarFile;
    private String largeAvatarFile;
    private Integer avatarWidth;
    private Integer avatarHeight;
    private boolean avatarDeleted = false;

    public SettingsProfileViewModel(@NonNull Application application) {
        super(application);

        me = Me.getInstance();
        bgWorkers = BgWorkers.getInstance();

        workManager = WorkManager.getInstance(application);

        tempAvatarLiveData = new MutableLiveData<>();
        bgWorkers.execute(me::getName);
        bgWorkers.execute(me::getUsername);
        canSave = new MediatorLiveData<>();
        canSave.addSource(nameChangedLiveData, nameChanged -> setCanSave());
        canSave.addSource(usernameChangedLiveData, usernameChanged -> setCanSave());
        canSave.addSource(tempAvatarLiveData, bitmap -> setCanSave());
        canSave.addSource(linksChangedLiveData, linksChanged -> setCanSave());
        bgWorkers.execute(() -> {
            hasAvatarSet.postValue(AvatarLoader.getInstance().hasAvatar());
            computeLinks();
        });

    }

    public void computeLinks() {
        Connection.getInstance().getHalloappProfileInfo(new UserId(me.getUser()), me.getUsername()).onResponse(response -> {
            if (response != null && response.success) {
                ArrayList<SocialLink> socialLinks = new ArrayList<>();
                serverLinks = new ArrayList<>();
                for (Link link: response.profile.getLinksList()) {
                    socialLinks.add(new SocialLink(link.getText(), SocialLink.fromProtoType(link.getType())));
                    serverLinks.add(new SocialLink(link.getText(), SocialLink.fromProtoType(link.getType())));
                }
                tempLinksLiveData.postValue(socialLinks);
            }
        }).onError(err -> {
            Log.e("Failed to get social media links", err);
        });
    }

    public void addLink(@NonNull SocialLink link) {
        List<SocialLink> links = tempLinksLiveData.getValue() == null ? new ArrayList<>() : tempLinksLiveData.getValue();
        links.add(link);
        tempLinksLiveData.postValue(links);
    }

    public void removeLink(@NonNull SocialLink link) {
        List<SocialLink> links = tempLinksLiveData.getValue() == null ? new ArrayList<>() : tempLinksLiveData.getValue();
        links.remove(link);
        tempLinksLiveData.postValue(links);
    }

    public void setTempLinks(@NonNull SocialLink item, @NonNull String link) {
        item.text = link;
        int size = tempLinksLiveData.getValue() != null ? tempLinksLiveData.getValue().size() : 0;
        linksChangedLiveData.setValue(serverLinks.size() != size || !serverLinks.contains(item));
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

    public LiveData<List<SocialLink>> getLinks() {
        return tempLinksLiveData;
    }

    public void saveProfile() {
        sendName();
    }

    private void sendName() {
        Data.Builder builder = new Data.Builder();
        if (nameChangedLiveData.getValue() != null && nameChangedLiveData.getValue()) {
            builder.putString(UpdateProfileWorker.WORKER_PARAM_NAME, tempName);
        }
        if (usernameChangedLiveData.getValue() != null && usernameChangedLiveData.getValue()) {
            builder.putString(UpdateProfileWorker.WORKER_PARAM_USERNAME, tempUsername);
        }
        if (tempAvatarLiveData.getValue() != null && avatarHeight != null && avatarWidth != null) {
            builder.putInt(UpdateProfileWorker.WORKER_PARAM_AVATAR_HEIGHT, avatarHeight);
            builder.putInt(UpdateProfileWorker.WORKER_PARAM_AVATAR_WIDTH, avatarWidth);
            builder.putString(UpdateProfileWorker.WORKER_PARAM_AVATAR_FILE, avatarFile);
            builder.putString(UpdateProfileWorker.WORKER_PARAM_LARGE_AVATAR_FILE, largeAvatarFile);
        }
        if (avatarDeleted) {
            builder.putBoolean(UpdateProfileWorker.WORKER_PARAM_AVATAR_REMOVAL, true);
        }
        if (linksChangedLiveData.getValue() != null && linksChangedLiveData.getValue()) {
            Set<String> toRemove = new HashSet<>();
            Set<String> toAdd = new HashSet<>();
            List<SocialLink> updatedLinks = tempLinksLiveData.getValue() != null ? tempLinksLiveData.getValue() : new ArrayList<>();

            for (SocialLink link : serverLinks) {
                if (!updatedLinks.contains(link)) {
                    toRemove.add(link.text);
                }
            }

            for (SocialLink link : updatedLinks) {
                if (!serverLinks.contains(link) && !link.getPrefix().equals(link.text)) {
                    toAdd.add(link.text);
                }
            }
            builder.putStringArray(UpdateProfileWorker.WORKER_PARAM_ADD_LINKS, toAdd.toArray(new String[0]));
            builder.putStringArray(UpdateProfileWorker.WORKER_PARAM_REMOVE_LINKS, toRemove.toArray(new String[0]));
        }

        final Data data = builder.build();
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(UpdateProfileWorker.class).setInputData(data).build();
        workManager.enqueueUniqueWork(UpdateProfileWorker.WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest);
    }

    private void setCanSave() {
        boolean nameChanged = nameChangedLiveData.getValue() != null ? nameChangedLiveData.getValue() : false;
        boolean usernameChanged = usernameChangedLiveData.getValue() != null ? usernameChangedLiveData.getValue() : false;
        boolean avatarChanged = tempAvatarLiveData.getValue() != null;
        boolean linksChanged = linksChangedLiveData.getValue() != null ? linksChangedLiveData.getValue() : false;
        canSave.setValue(nameChanged || usernameChanged || avatarChanged || avatarDeleted || linksChanged);
    }

    public LiveData<String> getName() {
        return me.name;
    }

    public LiveData<String> getUsername() {
        return me.username;
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

    public boolean hasChanges() {
        Boolean hasChanges = canSave.getValue();
        return hasChanges != null && hasChanges;
    }

    public void setTempName(String tempName) {
        this.tempName = tempName;
        nameChangedLiveData.setValue(!tempName.equals(me.name.getValue()));
    }

    public void setTempUsername(String tempUsername) {
        this.tempUsername = tempUsername;
        usernameChangedLiveData.postValue(!tempUsername.equals(me.username.getValue()));
    }


    public static class UpdateProfileWorker extends Worker {

        private static final String WORK_NAME = "set-name";

        private static final String WORKER_PARAM_AVATAR_FILE = "avatar_file";
        private static final String WORKER_PARAM_LARGE_AVATAR_FILE = "large_avatar_file";
        private static final String WORKER_PARAM_AVATAR_WIDTH = "avatar_width";
        private static final String WORKER_PARAM_AVATAR_HEIGHT = "avatar_height";
        private static final String WORKER_PARAM_NAME = "name";
        private static final String WORKER_PARAM_USERNAME = "username";
        private static final String WORKER_PARAM_AVATAR_REMOVAL = "avatar_removal";
        private static final String WORKER_PARAM_ADD_LINKS = "add_links";
        private static final String WORKER_PARAM_REMOVE_LINKS = "remove_links";

        private final AvatarLoader avatarLoader;

        public UpdateProfileWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
            avatarLoader = AvatarLoader.getInstance();
        }

        @Override
        public @NonNull Result doWork() {
            final String name = getInputData().getString(WORKER_PARAM_NAME);
            final String username = getInputData().getString(WORKER_PARAM_USERNAME);
            final String avatarFilePath = getInputData().getString(WORKER_PARAM_AVATAR_FILE);
            final String largeAvatarFilePath = getInputData().getString(WORKER_PARAM_LARGE_AVATAR_FILE);
            final boolean avatarDeleted = getInputData().getBoolean(WORKER_PARAM_AVATAR_REMOVAL,false);
            final String[] toAddLinks = getInputData().getStringArray(WORKER_PARAM_ADD_LINKS);
            final String[] toRemoveLinks = getInputData().getStringArray(WORKER_PARAM_REMOVE_LINKS);
            int avatarWidth = getInputData().getInt(WORKER_PARAM_AVATAR_WIDTH, -1);
            int avatarHeight = getInputData().getInt(WORKER_PARAM_AVATAR_HEIGHT, -1);
            try {
                final Me me = Me.getInstance();
                final boolean nameSet, usernameSet;
                if (!TextUtils.isEmpty(name)) {
                    Connection.getInstance().sendName(Preconditions.checkNotNull(name)).await();
                    me.saveName(name);
                    nameSet = true;
                } else {
                    nameSet = !TextUtils.isEmpty(me.getName());
                }
                if (!TextUtils.isEmpty(username)) {
                    UsernameResponseIq responseIq = Connection.getInstance().sendUsername(Preconditions.checkNotNull(username)).await();
                    if (responseIq.success) {
                        me.saveUsername(username);
                        Notifications.getInstance(AppContext.getInstance().get()).clearFriendModelNotification();
                        Preferences.getInstance().setPrefFriendModelDelayInDaysTimeOne(1);
                        Preferences.getInstance().setPrefFriendModelDelayInDaysTimeTwo(1);
                        Preferences.getInstance().setPrefPrevFriendModelNotifyTimeInMillis(0);
                    }
                    usernameSet = responseIq.success;
                } else {
                    usernameSet = !TextUtils.isEmpty(me.getUsername());
                }
                if (nameSet && (!BuildConfig.IS_KATCHUP || usernameSet)) {
                    Preferences.getInstance().setProfileSetup(true);
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

                        String avatarId = Connection.getInstance().setAvatar(fileBytes, largeFileBytes).await();
                        if (avatarId == null) {
                            return Result.failure();
                        }
                        final File outFile = FileStore.getInstance().getAvatarFile(UserId.ME.rawId());
                        final File largeOutFile = FileStore.getInstance().getAvatarFile(UserId.ME.rawId(), true);
                        FileUtils.copyFile(avatarFile, outFile);
                        FileUtils.copyFile(largeAvatarFile, largeOutFile);
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
                    avatarLoader.removeMyAvatar();
                    Connection.getInstance().removeAvatar();
                }
                if (toAddLinks != null) {
                    for (String link : toAddLinks) {
                        Connection.getInstance().sendLink(link, getLinkType(link), SetLinkRequest.Action.SET).await();
                    }
                }
                if (toRemoveLinks != null) {
                    for (String link : toRemoveLinks) {
                        Connection.getInstance().sendLink(link, getLinkType(link), SetLinkRequest.Action.REMOVE).await();
                    }
                }
                return Result.success();
            } catch (InterruptedException | ObservableErrorException e) {
                Log.e("UpdateProfileWorker", e);
                return Result.failure();
            }
        }

        private Link.Type getLinkType(@NonNull String text) {
            if (text.startsWith("instagram.com/")) {
                return Link.Type.INSTAGRAM;
            } else if (text.startsWith("tiktok.com/")) {
                return Link.Type.TIKTOK;
            } else if (text.startsWith("x.com/")) {
                return Link.Type.X;
            } else if (text.startsWith("youtube.com/")) {
                return Link.Type.YOUTUBE;
            } else {
                return Link.Type.USER_DEFINED;
            }
        }
    }
}
