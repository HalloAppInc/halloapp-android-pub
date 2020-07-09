package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;

import org.jivesoftware.smack.packet.IQ;

public class LeaveGroupIq extends IQ {

    public static final String ELEMENT = "group";
    public static final String NAMESPACE = "halloapp:groups";

    private static final String ATTRIBUTE_ACTION = "action";
    private static final String ATTRIBUTE_GID = "gid";

    private final String gid;

    protected LeaveGroupIq(@NonNull String gid) {
        super(ELEMENT, NAMESPACE);
        setType(Type.set);
        this.gid = gid;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.xmlnsAttribute(NAMESPACE);
        xml.attribute(ATTRIBUTE_ACTION, "leave");
        xml.attribute(ATTRIBUTE_GID, gid);
        xml.rightAngleBracket();
        return xml;
    }
}
