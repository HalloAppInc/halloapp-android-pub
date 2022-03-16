package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;

import com.halloapp.id.GroupId;
import com.halloapp.proto.server.GroupInviteLink;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

public class ResetGroupInviteLinkIq extends HalloIq {

    private final GroupId groupId;

    protected ResetGroupInviteLinkIq(@NonNull GroupId groupId) {
        this.groupId = groupId;
    }

    @Override
    public Iq.Builder toProtoIq() {
        GroupInviteLink groupInviteLink =
                GroupInviteLink.newBuilder()
                        .setAction(GroupInviteLink.Action.RESET)
                        .setGid(groupId.rawId()).build();
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setGroupInviteLink(groupInviteLink);
    }
}
