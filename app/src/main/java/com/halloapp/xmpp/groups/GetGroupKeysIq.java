package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;

import com.halloapp.id.GroupId;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

public class GetGroupKeysIq extends HalloIq {

    private final GroupId groupId;

    protected GetGroupKeysIq(@NonNull GroupId groupId) {
        this.groupId = groupId;
    }

    @Override
    public Iq.Builder toProtoIq() {
        GroupStanza groupStanza = GroupStanza.newBuilder()
                .setAction(GroupStanza.Action.GET_MEMBER_IDENTITY_KEYS)
                .setGid(groupId.rawId())
                .build();
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setGroupStanza(groupStanza);
    }
}
