package com.halloapp.xmpp.groups;

import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.GroupsStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

import org.jivesoftware.smack.packet.IQ;

public class GetGroupsListIq extends HalloIq {

    public static final String ELEMENT = "groups";
    public static final String NAMESPACE = "halloapp:groups";

    private static final String ATTRIBUTE_ACTION = "action";

    protected GetGroupsListIq() {
        super(ELEMENT, NAMESPACE);
        setType(Type.get);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.xmlnsAttribute(NAMESPACE);
        xml.attribute(ATTRIBUTE_ACTION, "get");
        xml.rightAngleBracket();
        return xml;
    }

    @Override
    public Iq toProtoIq() {
        return Iq.newBuilder().setType(Iq.Type.GET).setId(getStanzaId()).setGroupsStanza(GroupsStanza.newBuilder().setAction(GroupsStanza.Action.GET).build()).build();
    }
}
