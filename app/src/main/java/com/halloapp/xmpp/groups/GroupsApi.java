package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.contacts.UserId;
import com.halloapp.groups.GroupInfo;
import com.halloapp.groups.MemberInfo;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.util.Observable;

import java.util.ArrayList;
import java.util.List;

public class GroupsApi {

    private Connection connection;

    public GroupsApi(Connection connection) {
        this.connection = connection;
    }

    public Observable<GroupInfo> createGroup(@NonNull String name, @NonNull List<UserId> uids) {
        final CreateGroupIq requestIq = new CreateGroupIq(name, uids);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            List<MemberInfo> memberInfos = new ArrayList<>();
            for (MemberElement memberElement : response.memberElements) {
                memberInfos.add(new MemberInfo(memberElement.uid, memberElement.type, memberElement.name));
            }
            return new GroupInfo(response.gid, response.name, response.description, response.avatar, memberInfos);
        });
    }

    public Observable<Boolean> deleteGroup(@NonNull String gid) {
        final DeleteGroupIq requestIq = new DeleteGroupIq(gid);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            return "ok".equals(response.result);
        });
    }

    // TODO(jack): Return type
    public Observable<String> addRemoveMembers(@NonNull String gid, @Nullable List<UserId> addUids, @Nullable List<UserId> removeUids) {
        final AddRemoveMembersIq requestIq = new AddRemoveMembersIq(gid, addUids, removeUids);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            return "TODO";
        });
    }

    // TODO(jack): Return type
    public Observable<String> promoteDemoteAdmins(@NonNull String gid, @Nullable List<UserId> promoteUids, @Nullable List<UserId> demoteUids) {
        final PromoteDemoteAdminsIq requestIq = new PromoteDemoteAdminsIq(gid, promoteUids, demoteUids);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            return "TODO";
        });
    }

    public Observable<GroupInfo> getGroupInfo(@NonNull String gid) {
        final GetGroupInfoIq requestIq = new GetGroupInfoIq(gid);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            List<MemberInfo> memberInfos = new ArrayList<>();
            for (MemberElement memberElement : response.memberElements) {
                memberInfos.add(new MemberInfo(memberElement.uid, memberElement.type, memberElement.name));
            }
            return new GroupInfo(response.gid, response.name, response.description, response.avatar, memberInfos);
        });
    }

    public Observable<List<GroupInfo>> getGroupsList() {
        final GetGroupsListIq requestIq = new GetGroupsListIq();
        final Observable<GroupsListResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            return response.groupInfos;
        });
    }

    public Observable<String> setGroupName(@NonNull String gid, @NonNull String name) {
        final SetGroupInfoIq requestIq = new SetGroupInfoIq(gid, name, null);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            return response.name;
        });
    }

    // TODO(jack): What about remove avatar?
    public Observable<String> setGroupAvatar(@NonNull String gid, String avatar) {
        final SetGroupInfoIq requestIq = new SetGroupInfoIq(gid, null, avatar);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            return response.avatar;
        });
    }

    public Observable<Boolean> leaveGroup(@NonNull String gid) {
        final LeaveGroupIq requestIq = new LeaveGroupIq(gid);
        final Observable<GroupsListResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            return true;
        });
    }
}
