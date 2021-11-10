package com.halloapp.nux;

import android.content.Context;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.groups.GroupInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.groups.GroupsApi;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class ZeroZoneManager {

    private static final String WORKER_ID = "zero-zone-initializer-worker";

    public static void initialize(@NonNull Context context) {
        Constraints.Builder constraintBuilder = new Constraints.Builder();
        constraintBuilder.setRequiredNetworkType(NetworkType.CONNECTED);
        final OneTimeWorkRequest workRequest = (new OneTimeWorkRequest.Builder(ZeroZoneWorker.class))
                .setConstraints(constraintBuilder.build()).build();
        WorkManager.getInstance(context).enqueueUniqueWork(WORKER_ID, ExistingWorkPolicy.APPEND_OR_REPLACE, workRequest);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            ZeroZoneState.WAITING_FOR_SYNC,
            ZeroZoneState.NOT_IN_ZERO_ZONE,
            ZeroZoneState.NEEDS_INITIALIZATION,
            ZeroZoneState.INITIALIZED
    })
    public @interface ZeroZoneState {
        int WAITING_FOR_SYNC = 0;
        int NOT_IN_ZERO_ZONE = 1;
        int NEEDS_INITIALIZATION = 2;
        int INITIALIZED = 3;
    }

    public static boolean isInZeroZone(@Nullable List<Contact> contacts) {
        if (contacts == null) {
            return true;
        }

        return contacts.size() <= 5;
    }

    public static void addHomeZeroZonePost(@NonNull ContentDb contentDb) {
        if (!contentDb.hasHomeZeroZonePost()) {
            Post systemPost = new Post(0,
                    UserId.ME,
                    RandomId.create(),
                    System.currentTimeMillis(),
                    Post.TRANSFERRED_YES,
                    Post.SEEN_YES,
                    Post.TYPE_ZERO_ZONE,
                    null);
            systemPost.addToStorage(contentDb);
        }
    }

    public static class ZeroZoneWorker extends Worker {

        public ZeroZoneWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {
            Me me = Me.getInstance();
            ContentDb contentDb = ContentDb.getInstance();
            GroupsApi groupsApi = GroupsApi.getInstance();
            Preferences preferences = Preferences.getInstance();

            @ZeroZoneManager.ZeroZoneState int state = preferences.getZeroZoneState();
            if (state != ZeroZoneManager.ZeroZoneState.NEEDS_INITIALIZATION) {
                Log.i("ZeroZoneWorker/doWork not waiting for initialization, nothing to do");
                return Result.success();
            }
            addHomeZeroZonePost(contentDb);

            GroupId zeroZoneGid = preferences.getZeroZoneGroupId();
            if (zeroZoneGid == null) {
                String groupName = getApplicationContext().getString(R.string.zero_zone_group_name, me.getName());
                GroupInfo zeroZoneGroup;
                try {
                    zeroZoneGroup = groupsApi.createGroup(groupName, new ArrayList<>()).await();
                } catch (ObservableErrorException | InterruptedException e) {
                    Log.e("ZeroZoneWorker/doWork failed to create group", e);
                    return Result.retry();
                }
                zeroZoneGid = zeroZoneGroup.groupId;
                preferences.setZeroZoneGroupId(zeroZoneGid);
            }
            if (zeroZoneGid == null) {
                Log.e("ZeroZoneWorker/doWork no group created?");
                return Result.retry();
            }
            if (!contentDb.hasGroupZeroZonePost(zeroZoneGid)) {
                try {
                    groupsApi.getGroupInviteLink(zeroZoneGid).await();
                } catch (ObservableErrorException | InterruptedException e) {
                    Log.e("ZeroZoneWorker/doWork failed to get invite link", e);
                    return Result.retry();
                }
                Post systemPost = new Post(0,
                        UserId.ME,
                        RandomId.create(),
                        System.currentTimeMillis(),
                        Post.TRANSFERRED_YES,
                        Post.SEEN_NO,
                        Post.TYPE_ZERO_ZONE,
                        null);
                systemPost.setParentGroup(zeroZoneGid);
                systemPost.addToStorage(contentDb);
            }
            preferences.setZeroZoneState(ZeroZoneManager.ZeroZoneState.INITIALIZED);
            return Result.success();
        }
    }

}
