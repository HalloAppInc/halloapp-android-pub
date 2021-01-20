package com.halloapp.groups;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.groups.GroupsApi;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupsSync {

    private static final String GROUPS_SYNC_WORK_ID = "groups-sync";

    private static GroupsSync instance;

    private final Context context;
    private final GroupsApi groupsApi;
    private final ContentDb contentDb;
    private final ContactsDb contactsDb;

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
        this.groupsApi = GroupsApi.getInstance();
        this.contentDb = ContentDb.getInstance();
        this.contactsDb = ContactsDb.getInstance();
    }

    public void startGroupsSync() {
        Log.d("GroupsSync.startGroupsSync");
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(GroupSyncWorker.class).build();
        WorkManager.getInstance(context).enqueueUniqueWork(GROUPS_SYNC_WORK_ID, ExistingWorkPolicy.REPLACE, workRequest);
    }

    @WorkerThread
    private ListenableWorker.Result performGroupSync() {
        Log.i("GroupsSync.performGroupSync");
        try {
            List<GroupInfo> groups = groupsApi.getGroupsList().await();
            List<Chat> chats = contentDb.getGroups();

            Map<ChatId, Chat> chatMap = new HashMap<>();
            for (Chat chat : chats) {
                chatMap.put(chat.chatId, chat);
            }

            List<GroupInfo> addedGroups = new ArrayList<>();
            List<GroupInfo> updatedGroups = new ArrayList<>();
            for (GroupInfo groupInfo : groups) {
                Chat chat = chatMap.remove(groupInfo.groupId);
                if (chat == null) {
                    addedGroups.add(groupInfo);
                } else if (!haveSameMetadata(groupInfo, chat)) {
                    updatedGroups.add(groupInfo);
                }
            }
            List<Chat> deletedGroups = new ArrayList<>(chatMap.values());

            Log.d("GroupsSync.perfromGroupSync adding " + addedGroups.size() + " groups");
            for (GroupInfo groupInfo : addedGroups) {
                contentDb.addGroupChat(groupInfo, null);
            }

            Log.d("GroupsSync.perfromGroupSync updating " + updatedGroups.size() + " groups");
            for (GroupInfo groupInfo : updatedGroups) {
                contentDb.updateGroupChat(groupInfo, null);
            }

            Log.d("GroupsSync.perfromGroupSync ignoring " + deletedGroups.size() + " deleted groups");
            // TODO(jack): mark deleted chats so users cannot send messages to them

            Map<UserId, String> nameMap = new HashMap<>();
            for (GroupInfo groupInfo : groups) {
                List<MemberInfo> serverMembers = groupsApi.getGroupInfo(groupInfo.groupId).await().members;
                List<MemberInfo> localMembers = contentDb.getGroupMembers(groupInfo.groupId);

                Map<UserId, MemberInfo> memberMap = new HashMap<>();
                for (MemberInfo member : localMembers) {
                    memberMap.put(member.userId, member);
                }
                for (MemberInfo member : serverMembers) {
                    nameMap.put(member.userId, member.name);
                }

                List<MemberInfo> addedMembers = new ArrayList<>();
                List<MemberInfo> updatedMembers = new ArrayList<>();
                for (MemberInfo member : serverMembers) {
                    MemberInfo local = memberMap.remove(member.userId);
                    if (local == null) {
                        addedMembers.add(member);
                    } else if (!haveSameMetadata(member, local)) {
                        updatedMembers.add(member);
                    }
                }

                List<MemberInfo> deletedMembers = new ArrayList<>(memberMap.values());

                // TODO(jack): handle admin change (member updates)

                Log.d("GroupsSync.performGroupSync adding " + addedMembers.size() + " and removing " + deletedMembers.size() + " for group " + groupInfo.groupId);
                contentDb.addRemoveGroupMembers(groupInfo.groupId, null, null, addedMembers, deletedMembers, null);
            }

            contactsDb.updateUserNames(nameMap);

            return ListenableWorker.Result.success();
        } catch (ObservableErrorException e) {
            Log.e("GroupsSync.perfromGroupSync observable error", e);
        } catch (InterruptedException e) {
            Log.e("GroupsSync.perfromGroupSync interrupted", e);
        }
        return ListenableWorker.Result.failure();
    }

    private boolean haveSameMetadata(@NonNull GroupInfo groupInfo, @NonNull Chat chat) {
        Preconditions.checkArgument(groupInfo.groupId.equals(chat.chatId));
        return TextUtils.equals(groupInfo.name, chat.name)
                && TextUtils.equals(groupInfo.description, chat.groupDescription)
                && TextUtils.equals(groupInfo.avatar, chat.groupAvatarId);
    }

    private boolean haveSameMetadata(@NonNull MemberInfo a, @NonNull MemberInfo b) {
        Preconditions.checkArgument(a.userId.rawId().equals(b.userId.rawId()));
        return TextUtils.equals(a.type, b.type);
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
