package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;

import com.halloapp.id.GroupId;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

public class DeleteGroupIq extends HalloIq {

    private final GroupId groupId;

    protected DeleteGroupIq(@NonNull GroupId groupId) {
        this.groupId = groupId;
    }

    @Override
    public Iq.Builder toProtoIq() {
        GroupStanza groupStanza =  GroupStanza.newBuilder()
                .setAction(GroupStanza.Action.DELETE)
                .setGid(groupId.rawId())
                .build();
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setGroupStanza(groupStanza);
    }
}
