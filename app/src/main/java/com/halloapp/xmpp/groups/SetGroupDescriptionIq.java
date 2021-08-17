package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;

import com.halloapp.id.GroupId;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

public class SetGroupDescriptionIq extends HalloIq {

    private final GroupId groupId;
    private final String description;

    protected SetGroupDescriptionIq(@NonNull  String description, @NonNull GroupId groupId) {
        this.groupId = groupId;
        this.description = description;
    }

    @Override
    public Iq toProtoIq() {
        GroupStanza setBgStanza = GroupStanza.newBuilder()
                .setGid(groupId.rawId())
                .setAction(GroupStanza.Action.CHANGE_DESCRIPTION)
                .setDescription(description).build();
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setId(getStanzaId())
                .setGroupStanza(setBgStanza)
                .build();
    }
}
