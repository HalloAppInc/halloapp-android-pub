package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.groups.GroupInfo;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.GroupInviteLink;
import com.halloapp.proto.server.GroupMember;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.HalloIq;
import com.halloapp.xmpp.util.Observable;

import java.util.ArrayList;
import java.util.List;

public class GroupsApi {

    private static GroupsApi instance;

    private final Connection connection;

    public static GroupsApi getInstance() {
        if (instance == null) {
            synchronized (GroupsApi.class) {
                if (instance == null) {
                    instance = new GroupsApi(Connection.getInstance());
                }
            }
        }
        return instance;
    }

    private GroupsApi(Connection connection) {
        this.connection = connection;
    }

    public Observable<GroupInfo> createGroup(@NonNull String name, @NonNull List<UserId> uids) {
        final CreateGroupIq requestIq = new CreateGroupIq(name, uids);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            List<MemberInfo> memberInfos = new ArrayList<>();
            for (MemberElement memberElement : response.memberElements) {
                memberInfos.add(new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name));
            }
            return new GroupInfo(response.groupId, response.name, response.description, response.avatar, memberInfos);
        });
    }

    public Observable<Boolean> deleteGroup(@NonNull GroupId groupId) {
        final DeleteGroupIq requestIq = new DeleteGroupIq(groupId);
        final Observable<HalloIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            return true;
        });
    }

    public Observable<Boolean> addRemoveMembers(@NonNull GroupId groupId, @Nullable List<UserId> addUids, @Nullable List<UserId> removeUids) {
        final AddRemoveMembersIq requestIq = new AddRemoveMembersIq(groupId, addUids, removeUids);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            for (MemberElement memberElement : response.memberElements) {
                if (!MemberElement.Result.OK.equals(memberElement.result)) {
                    return false;
                }
            }

            return true;
        });
    }

    public Observable<Boolean> promoteDemoteAdmins(@NonNull GroupId groupId, @Nullable List<UserId> promoteUids, @Nullable List<UserId> demoteUids) {
        final PromoteDemoteAdminsIq requestIq = new PromoteDemoteAdminsIq(groupId, promoteUids, demoteUids);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            for (MemberElement memberElement : response.memberElements) {
                if (!MemberElement.Result.OK.equals(memberElement.result)) {
                    return false;
                }
            }

            return true;
        });
    }

    public Observable<GroupInfo> getGroupInfo(@NonNull GroupId groupId) {
        final GetGroupInfoIq requestIq = new GetGroupInfoIq(groupId);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            List<MemberInfo> memberInfos = new ArrayList<>();
            for (MemberElement memberElement : response.memberElements) {
                memberInfos.add(new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name));
            }
            return new GroupInfo(response.groupId, response.name, response.description, response.avatar, memberInfos);
        });
    }

    public Observable<String> getGroupInviteLink(@NonNull GroupId groupId) {
        final GetGroupInviteLinkIq requestIq = new GetGroupInviteLinkIq(groupId);

        return connection.sendIqRequest(requestIq).map(response -> {
            GroupInviteLink groupInviteLink = response.getGroupInviteLink();
            if (groupInviteLink == null) {
                return null;
            }
            return groupInviteLink.getLink();
        });
    }

    public Observable<Boolean> resetGroupInviteLink(@NonNull GroupId groupId) {
        final ResetGroupInviteLinkIq requestIq = new ResetGroupInviteLinkIq(groupId);

        return connection.sendIqRequest(requestIq).map(response -> {
            GroupInviteLink groupInviteLink = response.getGroupInviteLink();
            if (groupInviteLink == null) {
                return null;
            }
            return GroupInviteLink.Action.RESET.equals(groupInviteLink.getAction());
        });
    }

    public Observable<GroupInfo> previewGroupInviteLink(@NonNull String code) {
        final PreviewGroupInviteLinkIq requestIq = new PreviewGroupInviteLinkIq(code);

        return connection.sendIqRequest(requestIq).map(response -> {
            GroupInviteLink groupInviteLink = response.getGroupInviteLink();
            if (groupInviteLink == null) {
                return null;
            }
            GroupStanza groupStanza = groupInviteLink.getGroup();
            List<GroupMember> groupMembers = groupStanza.getMembersList();
            List<MemberInfo> membersList = new ArrayList<>();
            for (GroupMember g : groupMembers) {
                membersList.add(MemberInfo.fromGroupMember(g));
            }
            return new GroupInfo(GroupId.fromNullable(groupInviteLink.getGid()), groupStanza.getName(), null, groupStanza.getAvatarId(), membersList);
        });
    }

    public Observable<Boolean> setGroupBackground(@NonNull GroupId groupId, int theme) {
        final SetGroupBackgroundIq requestIq = new SetGroupBackgroundIq(theme, groupId);

        return connection.sendIqRequest(requestIq).map(response -> response.getGroupStanza() != null);
    }

    public Observable<Boolean> joinGroupViaInviteLink(@NonNull String code) {
        final JoinGroupInviteLinkIq requestIq = new JoinGroupInviteLinkIq(code);

        return connection.sendIqRequest(requestIq).map(response -> {
            GroupInviteLink groupInviteLink = response.getGroupInviteLink();
            if (groupInviteLink == null) {
                return false;
            }
            return true;
        });
    }

    public Observable<List<GroupInfo>> getGroupsList() {
        final GetGroupsListIq requestIq = new GetGroupsListIq();
        final Observable<GroupsListResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            return response.groupInfos;
        });
    }

    public Observable<String> setGroupName(@NonNull GroupId groupId, @NonNull String name) {
        final SetGroupInfoIq requestIq = new SetGroupInfoIq(groupId, name, null);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            return response.name;
        });
    }

    public Observable<Boolean> leaveGroup(@NonNull GroupId groupId) {
        final LeaveGroupIq requestIq = new LeaveGroupIq(groupId);
        final Observable<HalloIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            // TODO(jack): mark group as left somehow
            return true;
        });
    }
}
