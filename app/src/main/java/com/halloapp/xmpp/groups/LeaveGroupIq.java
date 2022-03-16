package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;

import com.halloapp.id.GroupId;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

public class LeaveGroupIq extends HalloIq {

    private final GroupId groupId;

    protected LeaveGroupIq(@NonNull GroupId groupId) {
        this.groupId = groupId;
    }

    @Override
    public Iq.Builder toProtoIq() {
        GroupStanza groupStanza = GroupStanza.newBuilder()
                .setGid(groupId.rawId())
                .setAction(GroupStanza.Action.LEAVE)
                .build();
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setGroupStanza(groupStanza);
    }
}
