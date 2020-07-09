package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jivesoftware.smack.packet.IQ;

public class SetGroupInfoIq extends IQ {

    public static final String ELEMENT = "group";
    public static final String NAMESPACE = "halloapp:groups";

    private static final String ATTRIBUTE_ACTION = "action";
    private static final String ATTRIBUTE_GID = "gid";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_AVATAR = "avatar";

    private final String gid;
    private final String name;
    private final String avatar;

    protected SetGroupInfoIq(@NonNull String gid, @Nullable String name, @Nullable String avatar) {
        super(ELEMENT, NAMESPACE);
        setType(Type.set);
        this.gid = gid;
        this.name = name;
        this.avatar = avatar;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.xmlnsAttribute(NAMESPACE);
        xml.attribute(ATTRIBUTE_ACTION, "set");
        xml.attribute(ATTRIBUTE_GID, gid);
        if (name != null) {
            xml.attribute(ATTRIBUTE_NAME, name);
        }
        if (avatar != null) {
            xml.attribute(ATTRIBUTE_AVATAR, avatar);
        }
        xml.rightAngleBracket();
        return xml;
    }
}
