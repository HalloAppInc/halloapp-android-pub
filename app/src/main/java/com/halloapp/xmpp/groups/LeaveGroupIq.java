package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;

import com.halloapp.id.GroupId;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

import org.jivesoftware.smack.packet.IQ;

public class LeaveGroupIq extends HalloIq {

    public static final String ELEMENT = "group";
    public static final String NAMESPACE = "halloapp:groups";

    private static final String ATTRIBUTE_ACTION = "action";
    private static final String ATTRIBUTE_GID = "gid";

    private final GroupId groupId;

    protected LeaveGroupIq(@NonNull GroupId groupId) {
        super(ELEMENT, NAMESPACE);
        setType(Type.set);
        this.groupId = groupId;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.xmlnsAttribute(NAMESPACE);
        xml.attribute(ATTRIBUTE_ACTION, "leave");
        xml.attribute(ATTRIBUTE_GID, groupId.rawId());
        xml.rightAngleBracket();
        return xml;
    }

    @Override
    public Iq toProtoIq() {
        GroupStanza groupStanza = GroupStanza.newBuilder()
                .setGid(groupId.rawId())
                .setAction(GroupStanza.Action.LEAVE)
                .build();
        return Iq.newBuilder().setType(Iq.Type.SET).setId(getStanzaId()).setGroupStanza(groupStanza).build();
    }
}
