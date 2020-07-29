package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;

import org.jivesoftware.smack.packet.IQ;

import java.util.List;

public class AddRemoveMembersIq extends IQ {

    public static final String ELEMENT = "group";
    public static final String NAMESPACE = "halloapp:groups";

    private static final String ATTRIBUTE_ACTION = "action";
    private static final String ATTRIBUTE_GID = "gid";

    private final GroupId groupId;
    private final List<UserId> addUids;
    private final List<UserId> removeUids;

    protected AddRemoveMembersIq(@NonNull GroupId groupId, @Nullable List<UserId> addUids, @Nullable List<UserId> removeUids) {
        super(ELEMENT, NAMESPACE);
        setType(Type.set);
        this.groupId = groupId;
        this.addUids = addUids;
        this.removeUids = removeUids;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.xmlnsAttribute(NAMESPACE);
        xml.attribute(ATTRIBUTE_ACTION, "modify_members");
        xml.attribute(ATTRIBUTE_GID, groupId.rawId());
        xml.rightAngleBracket();
        if (addUids != null) {
            for (UserId uid : addUids) {
                xml.append(new MemberElement(uid, "add").toXML(NAMESPACE));
            }
        }
        if (removeUids != null) {
            for (UserId uid : removeUids) {
                xml.append(new MemberElement(uid, "remove").toXML(NAMESPACE));
            }
        }
        return xml;
    }
}
