package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;

import com.halloapp.id.GroupId;
import com.halloapp.proto.clients.Background;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

public class SetGroupBackgroundIq extends HalloIq {

    private final GroupId groupId;
    private final int background;

    protected SetGroupBackgroundIq(int background, @NonNull GroupId groupId) {
        this.groupId = groupId;
        this.background = background;
    }

    @Override
    public Iq toProtoIq() {
        Background bgStanza = Background.newBuilder()
                .setTheme(background).build();
        GroupStanza setBgStanza = GroupStanza.newBuilder()
                .setGid(groupId.rawId())
                .setAction(GroupStanza.Action.SET_BACKGROUND)
                .setBackgroundBytes(bgStanza.toByteString()).build();
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setId(getStanzaId())
                .setGroupStanza(setBgStanza)
                .build();
    }
}
