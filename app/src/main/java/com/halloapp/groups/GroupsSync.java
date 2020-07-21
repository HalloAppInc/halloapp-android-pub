package com.halloapp.groups;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.Constants;
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.groups.GroupsApi;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GroupsSync {

    private static final String GROUPS_SYNC_WORK_ID = "groups-sync";

    private static GroupsSync instance;

    private final Context context;
    private final GroupsApi groupsApi;
    private final BgWorkers bgWorkers;
    private final Connection connection;
    private final ContentDb contentDb;

    private UUID lastSyncRequestId;

    public static GroupsSync getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized(GroupsSync.class) {
                if (instance == null) {
                    instance = new GroupsSync(context);
                }
            }
        }
        return instance;
    }

    private GroupsSync(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.groupsApi = new GroupsApi(Connection.getInstance());
        this.bgWorkers = BgWorkers.getInstance();
        this.connection = Connection.getInstance();
        this.contentDb = ContentDb.getInstance(context);
    }

    public LiveData<List<WorkInfo>> getWorkInfoLiveData() {
        return WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(GroupsSync.GROUPS_SYNC_WORK_ID);
    }

    public UUID getLastSyncRequestId() {
        return lastSyncRequestId;
    }

    public void cancelGroupsSync() {
        WorkManager.getInstance(context).cancelUniqueWork(GROUPS_SYNC_WORK_ID);
    }

    public void startGroupsSync() {
        Log.d("GroupsSync.startGroupsSync");
        if (!Constants.GROUPS_ENABLED) {
            return;
        }
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(GroupSyncWorker.class).build();
        lastSyncRequestId = workRequest.getId();
        WorkManager.getInstance(context).enqueueUniqueWork(GROUPS_SYNC_WORK_ID, ExistingWorkPolicy.REPLACE, workRequest);
    }

    @WorkerThread
    private ListenableWorker.Result performGroupSync() {
        Log.i("GroupsSync.performGroupSync");
        try {
            List<GroupInfo> groups = groupsApi.getGroupsList().await();
            List<Chat> chats = contentDb.getChats();

            Map<String, Chat> chatMap = new HashMap<>();
            for (Chat chat : chats) {
                chatMap.put(chat.chatId, chat);
            }

            List<GroupInfo> added = new ArrayList<>();
            List<GroupInfo> updated = new ArrayList<>();
            for (GroupInfo groupInfo : groups) {
                Chat chat = chatMap.remove(groupInfo.gid);
                if (chat == null) {
                    added.add(groupInfo);
                } else if (!haveSameMetadata(groupInfo, chat)) {
                    updated.add(groupInfo);
                }
            }
            List<Chat> deleted = new ArrayList<>(chatMap.values());

            Log.d("GroupsSync.perfromGroupSync adding " + added.size() + " groups");
            for (GroupInfo groupInfo : added) {
                contentDb.addGroupChat(groupInfo, null);
            }

            Log.d("GroupsSync.perfromGroupSync updating " + updated.size() + " groups");
            for (GroupInfo groupInfo : updated) {
                contentDb.updateGroupChat(groupInfo, null);
            }

            Log.d("GroupsSync.perfromGroupSync ignoring " + deleted.size() + " deleted groups");
            // TODO(jack): mark deleted chats so users cannot send messages to them
        } catch (ObservableErrorException e) {
            Log.e("GroupsSync.perfromGroupSync observable error", e);
        } catch (InterruptedException e) {
            Log.e("GroupsSync.perfromGroupSync interrupted", e);
        }
        return ListenableWorker.Result.failure();
    }

    private boolean haveSameMetadata(@NonNull GroupInfo groupInfo, @NonNull Chat chat) {
        Preconditions.checkArgument(groupInfo.gid.equals(chat.chatId));
        return TextUtils.equals(groupInfo.name, chat.name)
                && TextUtils.equals(groupInfo.description, chat.groupDescription)
                && TextUtils.equals(groupInfo.avatar, chat.groupAvatarId);
    }

    public static class GroupSyncWorker extends Worker {

        public GroupSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
            super(context, params);
        }

        @Override
        public @NonNull Result doWork() {
            final Result result = GroupsSync.getInstance(getApplicationContext()).performGroupSync();
            if  (!Result.success().equals(result)) {
                Log.sendErrorReport("GroupsSync failed");
            }
            return result;
        }
    }
}
