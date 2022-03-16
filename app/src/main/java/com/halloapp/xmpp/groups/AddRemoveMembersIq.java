package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.GroupMember;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.HistoryResend;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

import java.util.List;

public class AddRemoveMembersIq extends HalloIq {

    private final GroupId groupId;
    private final List<UserId> addUids;
    private final List<UserId> removeUids;
    private final HistoryResend historyResend;

    public AddRemoveMembersIq(@NonNull GroupId groupId, @Nullable List<UserId> addUids, @Nullable List<UserId> removeUids, @Nullable HistoryResend historyResend) {
        this.groupId = groupId;
        this.addUids = addUids;
        this.removeUids = removeUids;
        this.historyResend = historyResend;
    }

    @Override
    public Iq.Builder toProtoIq() {
        GroupStanza.Builder builder = GroupStanza.newBuilder();
        builder.setAction(GroupStanza.Action.MODIFY_MEMBERS);
        builder.setGid(groupId.rawId());
        if (historyResend != null) {
            builder.setHistoryResend(historyResend);
        }
        if (addUids != null) {
            for (UserId uid : addUids) {
                GroupMember groupMember = GroupMember.newBuilder()
                        .setUid(Long.parseLong(uid.rawId()))
                        .setAction(GroupMember.Action.ADD)
                        .build();
                builder.addMembers(groupMember);
            }
        }
        if (removeUids != null) {
            for (UserId uid : removeUids) {
                GroupMember groupMember = GroupMember.newBuilder()
                        .setUid(Long.parseLong(uid.rawId()))
                        .setAction(GroupMember.Action.REMOVE)
                        .build();
                builder.addMembers(groupMember);
            }
        }
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setGroupStanza(builder);
    }
}
