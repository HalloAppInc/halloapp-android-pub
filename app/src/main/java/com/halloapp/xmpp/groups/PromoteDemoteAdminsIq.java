package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.GroupMember;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

import java.util.List;

public class PromoteDemoteAdminsIq extends HalloIq {

    private final GroupId groupId;
    private final List<UserId> promoteUids;
    private final List<UserId> demoteUids;

    protected PromoteDemoteAdminsIq(@NonNull GroupId groupId, @Nullable List<UserId> promoteUids, @Nullable List<UserId> demoteUids) {
        this.groupId = groupId;
        this.promoteUids = promoteUids;
        this.demoteUids = demoteUids;
    }

    @Override
    public Iq.Builder toProtoIq() {
        GroupStanza.Builder builder = GroupStanza.newBuilder();
        builder.setAction(GroupStanza.Action.MODIFY_ADMINS);
        builder.setGid(groupId.rawId());
        if (promoteUids != null) {
            for (UserId uid : promoteUids) {
                GroupMember groupMember = GroupMember.newBuilder()
                        .setUid(Long.parseLong(uid.rawId()))
                        .setAction(GroupMember.Action.PROMOTE)
                        .build();
                builder.addMembers(groupMember);
            }
        }
        if (demoteUids != null) {
            for (UserId uid : demoteUids) {
                GroupMember groupMember = GroupMember.newBuilder()
                        .setUid(Long.parseLong(uid.rawId()))
                        .setAction(GroupMember.Action.DEMOTE)
                        .build();
                builder.addMembers(groupMember);
            }
        }
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setGroupStanza(builder);
    }
}
