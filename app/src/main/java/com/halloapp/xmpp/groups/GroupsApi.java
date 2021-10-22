package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.content.ContentDb;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.group.GroupFeedSessionManager;
import com.halloapp.crypto.group.GroupSetupInfo;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.groups.GroupInfo;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.clients.Background;
import com.halloapp.proto.server.GroupInviteLink;
import com.halloapp.proto.server.GroupMember;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.IdentityKey;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.HalloIq;
import com.halloapp.xmpp.util.IqResult;
import com.halloapp.xmpp.util.Observable;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupsApi {

    private static GroupsApi instance;

    private final ContentDb contentDb;
    private final Connection connection;

    public static GroupsApi getInstance() {
        if (instance == null) {
            synchronized (GroupsApi.class) {
                if (instance == null) {
                    instance = new GroupsApi(ContentDb.getInstance(), Connection.getInstance());
                }
            }
        }
        return instance;
    }

    private GroupsApi(ContentDb contentDb, Connection connection) {
        this.contentDb = contentDb;
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
            return new GroupInfo(response.groupId, response.name, response.description, response.avatar, response.background, memberInfos);
        });
    }

    public Observable<Boolean> addRemoveMembers(@NonNull GroupId groupId, @Nullable List<UserId> addUids, @Nullable List<UserId> removeUids) {
        final AddRemoveMembersIq requestIq = new AddRemoveMembersIq(groupId, addUids, removeUids);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            boolean success = true;
            List<MemberInfo> addedUsers = new ArrayList<>();
            for (MemberElement memberElement : response.memberElements) {
                if (!MemberElement.Result.OK.equals(memberElement.result)) {
                    success = false;
                } else if (MemberElement.Action.ADD.equals(memberElement.action)) {
                    addedUsers.add(new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name));
                }
            }


            if (!addedUsers.isEmpty()) {
                contentDb.addRemoveGroupMembers(groupId, null, null, addedUsers, new ArrayList<>(), () -> {
                    StringBuilder sb = new StringBuilder();
                    for (MemberInfo memberInfo : addedUsers) {
                        sb.append(memberInfo.userId.rawId()).append(",");
                    }
                    byte[] payload = sb.toString().getBytes();
                    try {
                        GroupSetupInfo groupSetupInfo = GroupFeedSessionManager.getInstance().ensureGroupSetUp(groupId);
                        byte[] encPayload = GroupFeedSessionManager.getInstance().encryptMessage(payload, groupId);
                        GroupsHistoryResendIq historyResendIq = new GroupsHistoryResendIq(groupId, groupSetupInfo, encPayload);
                        final Observable<HalloIq> resendObservable = connection.sendRequestIq(historyResendIq);
                        resendObservable.onResponse(res -> {
                            Log.d("History resend request succeeded");
                        }).onError(e -> {
                            Log.w("History resend request failed", e);
                        });
                    } catch (CryptoException | NoSuchAlgorithmException e) {
                        Log.e("Failed to encrypt member list for history resend request", e);
                    }
                });
            }

            return success;
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
            return new GroupInfo(response.groupId, response.name, response.description, response.avatar, response.background, memberInfos);
        });
    }

    public Observable<String> getGroupInviteLink(@NonNull GroupId groupId) {
        final GetGroupInviteLinkIq requestIq = new GetGroupInviteLinkIq(groupId);

        return connection.sendIqRequest(requestIq).map(response -> {
            GroupInviteLink groupInviteLink = response.getGroupInviteLink();
            if (groupInviteLink == null) {
                contentDb.setGroupLink(groupId, null);
                return null;
            }
            contentDb.setGroupLink(groupId, groupInviteLink.getLink());
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
            boolean success = GroupInviteLink.Action.RESET.equals(groupInviteLink.getAction());
            if (success) {
                contentDb.setGroupLink(groupId, null);
            }
            return success;
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
            Background b = null;
            try {
                b = Background.parseFrom(groupStanza.getBackgroundBytes());
            } catch (InvalidProtocolBufferException e) {
                Log.w("Failed to parse background", e);
            }
            return new GroupInfo(GroupId.fromNullable(groupInviteLink.getGid()), groupStanza.getName(), null, groupStanza.getAvatarId(), b, membersList);
        });
    }

    public Observable<Boolean> setGroupBackground(@NonNull GroupId groupId, int theme) {
        final SetGroupBackgroundIq requestIq = new SetGroupBackgroundIq(theme, groupId);

        return connection.sendIqRequest(requestIq).map(response -> response.getGroupStanza() != null);
    }

    public Observable<Boolean> setGroupDescription(@NonNull GroupId groupId, @NonNull String description) {
        final SetGroupDescriptionIq requestIq = new SetGroupDescriptionIq(description, groupId);

        return connection.sendIqRequest(requestIq).map(response -> response.getGroupStanza() != null);
    }

    public Observable<IqResult<GroupInviteLink>> joinGroupViaInviteLink(@NonNull String code) {
        final JoinGroupInviteLinkIq requestIq = new JoinGroupInviteLinkIq(code);

        return connection.sendIqRequest(requestIq).map(response -> {
            GroupInviteLink groupInviteLink = response.getGroupInviteLink();
            if (groupInviteLink == null) {
                return new IqResult<>();
            }
            return new IqResult<>(groupInviteLink);
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
            return true;
        });
    }

    public Observable<Map<UserId, PublicEdECKey>> getGroupKeys(@NonNull GroupId groupId) {
        final GetGroupKeysIq requestIq = new GetGroupKeysIq(groupId);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            Map<UserId, PublicEdECKey> ret = new HashMap<>();
            for (MemberElement member : response.memberElements) {
                byte[] identityKeyBytes = null;
                try {
                    IdentityKey identityKeyProto = IdentityKey.parseFrom(member.identityKey);
                    identityKeyBytes = identityKeyProto.getPublicKey().toByteArray();
                } catch (InvalidProtocolBufferException e) {
                    Log.e("Got invalid identity key proto", e);
                }
                ret.put(member.uid, new PublicEdECKey(identityKeyBytes));
            }
            return ret;
        });
    }
}
