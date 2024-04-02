package com.halloapp.groups;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.Preferences;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.nux.ZeroZoneManager;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.server.GroupStanza;
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
    private final Preferences preferences;
    private final ServerProps serverProps;

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
        this.preferences = Preferences.getInstance();
        this.serverProps = ServerProps.getInstance();
    }

    public ListenableWorker.Result forceGroupSync() {
        Log.d("GroupSync.forcingGroupSync");
        preferences.setLastGroupSyncTime(0);
        return performGroupSync();
    }

    public void startGroupsSync() {
        Log.d("GroupsSync.startGroupsSync");
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(GroupSyncWorker.class).build();
        WorkManager.getInstance(context).enqueueUniqueWork(GROUPS_SYNC_WORK_ID, ExistingWorkPolicy.REPLACE, workRequest);
    }

    public boolean performSingleGroupSync(@NonNull GroupId groupId) {
        Log.d("GroupsSync.performSingleGroupSync " + groupId);
        try {
            return syncGroupChat(null, groupId, null);
        } catch (ObservableErrorException e) {
            Log.e("GroupsSync.performSingleGroupSync Observable error syncing single group " + groupId, e);
        } catch (InterruptedException e) {
            Log.e("GroupsSync.performSingleGroupSync interrupted syncing single group " + groupId, e);
        }
        return false;
    }

    @WorkerThread
    private ListenableWorker.Result performGroupSync() {
        Log.i("GroupsSync.performGroupSync");

        long now = System.currentTimeMillis();
        long lastSyncTime = preferences.getLastGroupSyncTime();
        if (now - lastSyncTime < serverProps.getMinGroupSyncIntervalSeconds() * DateUtils.SECOND_IN_MILLIS) {
            Log.i("GroupsSync.performGroupSync last group sync too recent: " + lastSyncTime);
            return ListenableWorker.Result.success();
        }

        try {
            List<GroupInfo> groupInfos = groupsApi.getGroupsList().await();
            List<Group> groupFeeds = contentDb.getGroups();
            List<Chat> groupChats = contentDb.getGroupChats();

            Map<GroupId, Group> groupFeedMap = new HashMap<>();
            Map<GroupId, Chat> groupChatMap = new HashMap<>();
            for (Group group : groupFeeds) {
                groupFeedMap.put(group.groupId, group);
            }
            for (Chat group : groupChats) {
                if (group.chatId instanceof GroupId) {
                    groupChatMap.put((GroupId) group.chatId, group);
                }
            }

            List<GroupInfo> addedGroups = new ArrayList<>();
            List<GroupInfo> existingGroups = new ArrayList<>();
            List<GroupInfo> existingChatGroups = new ArrayList<>();
            for (GroupInfo groupInfo : groupInfos) {
                if (GroupStanza.GroupType.FEED.equals(groupInfo.groupType)) {
                    Group group = groupFeedMap.get(groupInfo.groupId);
                    if (group == null) {
                        addedGroups.add(groupInfo);
                    } else if (!haveSameFeedMetadata(groupInfo, group)) {
                        existingGroups.add(groupInfo);
                    }
                } else {
                    Chat chat = groupChatMap.get(groupInfo.groupId);
                    if (chat == null) {
                        addedGroups.add(groupInfo);
                    } else if (!haveSameChatMetadata(groupInfo, chat)) {
                        existingGroups.add(groupInfo);
                    }
                }
            }
            Log.d("GroupsSync.perfromGroupSync adding " + addedGroups.size() + " groups");
            for (GroupInfo groupInfo : addedGroups) {
                if (GroupStanza.GroupType.FEED.equals(groupInfo.groupType)) {
                    contentDb.addFeedGroup(groupInfo, null);
                } else {
                    contentDb.addGroupChat(groupInfo, null);
                }
            }
            Log.d("GroupsSync.perfromGroupSync ignoring " + (groupFeeds.size() - existingGroups.size()) + " deleted groups");
            // TODO: mark deleted chats so users cannot send messages to them

            Map<UserId, String> nameMap = new HashMap<>();
            for (GroupInfo groupInfo : groupInfos) {
                if (GroupStanza.GroupType.FEED.equals(groupInfo.groupType)) {
                    syncGroupFeed(groupFeedMap.get(groupInfo.groupId), groupInfo.groupId, nameMap);
                } else {
                    syncGroupChat(groupChatMap.get(groupInfo.groupId), groupInfo.groupId, nameMap);
                }
            }

            contactsDb.updateUserNames(nameMap);

            preferences.setLastGroupSyncTime(now);

            ZeroZoneManager.initialize(context);
            return ListenableWorker.Result.success();
        } catch (ObservableErrorException e) {
            Log.e("GroupsSync.perfromGroupSync observable error", e);
        } catch (InterruptedException e) {
            Log.e("GroupsSync.perfromGroupSync interrupted", e);
        }
        return ListenableWorker.Result.failure();
    }

    private boolean syncGroupFeed(@Nullable Group group, @NonNull GroupId groupId, @Nullable Map<UserId, String> nameMap) throws ObservableErrorException, InterruptedException {
        GroupInfo groupInfo = groupsApi.getGroupInfo(groupId).await();
        if (group == null || !haveSameFeedMetadata(groupInfo, group)) {
            contentDb.updateFeedGroup(groupInfo, null);
        }


        return syncParticipants(groupId, groupInfo, nameMap);
    }

    private boolean syncParticipants(@NonNull GroupId groupId, @NonNull GroupInfo groupInfo, @Nullable Map<UserId, String> nameMap) {
        List<MemberInfo> serverMembers = groupInfo.members;
        List<MemberInfo> localMembers = contentDb.getGroupMembers(groupId);

        Map<UserId, MemberInfo> memberMap = new HashMap<>();
        for (MemberInfo member : localMembers) {
            memberMap.put(member.userId, member);
        }
        if (nameMap != null) {
            for (MemberInfo member : serverMembers) {
                nameMap.put(member.userId, member.name);
            }
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

        // TODO: handle admin change (member updates)

        Log.d("GroupsSync.syncGroup adding " + addedMembers.size() + " and removing " + deletedMembers.size() + " for group " + groupId);
        contentDb.addRemoveGroupMembers(groupId, null, null, addedMembers, deletedMembers, null);

        return !addedMembers.isEmpty() || !deletedMembers.isEmpty();
    }

    private boolean syncGroupChat(@Nullable Chat group, @NonNull GroupId groupId, @Nullable Map<UserId, String> nameMap) throws ObservableErrorException, InterruptedException {
        GroupInfo groupInfo = groupsApi.getGroupInfo(groupId).await();
        if (group == null || !haveSameChatMetadata(groupInfo, group)) {
            contentDb.updateGroupChat(groupInfo, null);
        }

        return syncParticipants(groupId, groupInfo, nameMap);
    }

    private boolean haveSameFeedMetadata(@NonNull GroupInfo groupInfo, @NonNull Group feed) {
        Preconditions.checkArgument(groupInfo.groupId.equals(feed.groupId));
        if (groupInfo.background != null && groupInfo.background.getTheme() != feed.theme) {
            return false;
        }
        if (groupInfo.expiryInfo != null) {
            if (!groupInfo.expiryInfo.equals(feed.expiryInfo)) {
                return false;
            }
        }
        return TextUtils.equals(groupInfo.name, feed.name)
                && TextUtils.equals(groupInfo.description, feed.groupDescription)
                && TextUtils.equals(groupInfo.avatar, feed.groupAvatarId);
    }

    private boolean haveSameChatMetadata(@NonNull GroupInfo groupInfo, @NonNull Chat chat) {
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
