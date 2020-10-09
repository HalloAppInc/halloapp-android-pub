package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.GroupMember;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.util.Log;
import com.halloapp.xmpp.HalloIq;

import org.jivesoftware.smack.packet.IQ;

import java.util.List;

public class CreateGroupIq extends HalloIq {

    public static final String ELEMENT = "group";
    public static final String NAMESPACE = "halloapp:groups";

    private static final String ATTRIBUTE_ACTION = "action";
    private static final String ATTRIBUTE_NAME = "name";

    private final String name;
    private final List<UserId> uids;

    protected CreateGroupIq(@NonNull String name, @NonNull List<UserId> uids) {
        super(ELEMENT, NAMESPACE);
        setType(Type.set);
        this.name = name;
        this.uids = uids;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.xmlnsAttribute(NAMESPACE);
        xml.attribute(ATTRIBUTE_ACTION, "create");
        xml.attribute(ATTRIBUTE_NAME, name);
        xml.rightAngleBracket();
        for (UserId uid : uids) {
            xml.append(new MemberElement(uid).toXML(NAMESPACE));
        }
        return xml;
    }

    @Override
    public Iq toProtoIq() {
        GroupStanza.Builder builder = GroupStanza.newBuilder();
        builder.setAction(GroupStanza.Action.CREATE);
        builder.setName(name);
        for (UserId userId : uids) {
            GroupMember groupMember = GroupMember.newBuilder()
                    .setUid(Long.parseLong(userId.rawId()))
                    .build();
            builder.addMembers(groupMember);
        }
        return Iq.newBuilder().setType(Iq.Type.SET).setId(getStanzaId()).setGroupStanza(builder.build()).build();
    }
}
