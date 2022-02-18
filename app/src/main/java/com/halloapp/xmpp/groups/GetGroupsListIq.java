package com.halloapp.xmpp.groups;

import com.halloapp.proto.server.GroupsStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

public class GetGroupsListIq extends HalloIq {

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setId(getStanzaId())
                .setGroupsStanza(GroupsStanza.newBuilder().setAction(GroupsStanza.Action.GET));
    }
}
