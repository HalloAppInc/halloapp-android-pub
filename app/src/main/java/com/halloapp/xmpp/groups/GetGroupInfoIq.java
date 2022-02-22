package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;

import com.halloapp.id.GroupId;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

public class GetGroupInfoIq extends HalloIq {

    private final GroupId groupId;

    protected GetGroupInfoIq(@NonNull GroupId groupId) {
        this.groupId = groupId;
    }

    @Override
    public Iq toProtoIq() {
        GroupStanza groupStanza = GroupStanza.newBuilder()
                .setAction(GroupStanza.Action.GET)
                .setGid(groupId.rawId())
                .build();
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setId(getStanzaId())
                .setGroupStanza(groupStanza)
                .build();
    }
}
