package com.halloapp.xmpp.groups;

import org.jivesoftware.smack.packet.IQ;

public class GetGroupsListIq extends IQ {

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
}
