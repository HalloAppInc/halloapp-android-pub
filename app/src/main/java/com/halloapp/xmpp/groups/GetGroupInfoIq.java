package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;

import com.halloapp.id.GroupId;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

import org.jivesoftware.smack.packet.IQ;

public class GetGroupInfoIq extends HalloIq {

    public static final String ELEMENT = "group";
    public static final String NAMESPACE = "halloapp:groups";

    private static final String ATTRIBUTE_ACTION = "action";
    private static final String ATTRIBUTE_GID = "gid";

    private final GroupId groupId;

    protected GetGroupInfoIq(@NonNull GroupId groupId) {
        super(ELEMENT, NAMESPACE);
        setType(Type.get);
        this.groupId = groupId;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.xmlnsAttribute(NAMESPACE);
        xml.attribute(ATTRIBUTE_ACTION, "get");
        xml.attribute(ATTRIBUTE_GID, groupId.rawId());
        xml.rightAngleBracket();
        return xml;
    }

    @Override
    public Iq toProtoIq() {
        GroupStanza groupStanza = GroupStanza.newBuilder()
                .setAction(GroupStanza.Action.GET)
                .setGid(groupId.rawId())
                .build();
        return Iq.newBuilder().setType(Iq.Type.GET).setId(getStanzaId()).setGroupStanza(groupStanza).build();
    }
}
