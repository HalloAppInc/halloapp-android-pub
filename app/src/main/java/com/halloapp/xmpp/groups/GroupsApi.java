package com.halloapp.xmpp.groups;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.content.ContentDb;
import com.halloapp.groups.GroupInfo;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.util.Observable;

import org.jivesoftware.smack.packet.IQ;

import java.util.ArrayList;
import java.util.List;

public class GroupsApi {

    private static GroupsApi instance;

    private ContentDb contentDb;
    private Connection connection;

    public static GroupsApi getInstance(Context context) {
        if (instance == null) {
            synchronized (GroupsApi.class) {
                if (instance == null) {
                    instance = new GroupsApi(ContentDb.getInstance(context), Connection.getInstance());
                }
            }
        }
        return instance;
    }

    private GroupsApi(ContentDb contentDb, Connection connection) {
        this.contentDb = contentDb;
        this.connection = connection;
    }

    // TODO(jack): trigger: upon delete of group, delete members

    // TODO(jack): add group to db here
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
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            return "ok".equals(response.result);
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

    // TODO(jack): Return type
    public Observable<String> promoteDemoteAdmins(@NonNull GroupId groupId, @Nullable List<UserId> promoteUids, @Nullable List<UserId> demoteUids) {
        final PromoteDemoteAdminsIq requestIq = new PromoteDemoteAdminsIq(groupId, promoteUids, demoteUids);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            return "TODO";
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

    // TODO(jack): What about remove avatar?
    public Observable<String> setGroupAvatar(@NonNull GroupId groupId, String avatar) {
        final SetGroupInfoIq requestIq = new SetGroupInfoIq(groupId, null, avatar);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            return response.avatar;
        });
    }

    public Observable<Boolean> leaveGroup(@NonNull GroupId groupId) {
        final LeaveGroupIq requestIq = new LeaveGroupIq(groupId);
        final Observable<IQ> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            // TODO(jack): mark group as left somehow
            return true;
        });
    }
}
