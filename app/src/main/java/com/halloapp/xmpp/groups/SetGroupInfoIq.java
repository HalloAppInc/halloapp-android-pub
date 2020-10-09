package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.GroupId;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

import org.jivesoftware.smack.packet.IQ;

public class SetGroupInfoIq extends HalloIq {

    public static final String ELEMENT = "group";
    public static final String NAMESPACE = "halloapp:groups";

    private static final String ATTRIBUTE_ACTION = "action";
    private static final String ATTRIBUTE_GID = "gid";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_AVATAR = "avatar";

    private final GroupId groupId;
    private final String name;
    private final String avatar;

    protected SetGroupInfoIq(@NonNull GroupId groupId, @Nullable String name, @Nullable String avatar) {
        super(ELEMENT, NAMESPACE);
        setType(Type.set);
        this.groupId = groupId;
        this.name = name;
        this.avatar = avatar;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.xmlnsAttribute(NAMESPACE);
        xml.attribute(ATTRIBUTE_ACTION, "set_name");
        xml.attribute(ATTRIBUTE_GID, groupId.rawId());
        if (name != null) {
            xml.attribute(ATTRIBUTE_NAME, name);
        }
        if (avatar != null) {
            xml.attribute(ATTRIBUTE_AVATAR, avatar);
        }
        xml.rightAngleBracket();
        return xml;
    }

    @Override
    public Iq toProtoIq() {
        GroupStanza.Builder builder = GroupStanza.newBuilder();
        builder.setGid(groupId.rawId());
        builder.setAction(GroupStanza.Action.SET_NAME);
        if (name != null) {
            builder.setName(name);
        }
        if (avatar != null) {
            builder.setAvatarId(avatar);
        }
        return Iq.newBuilder().setType(Iq.Type.SET).setId(getStanzaId()).setGroupStanza(builder.build()).build();
    }
}
