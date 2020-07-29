package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;

import org.jivesoftware.smack.packet.IQ;

import java.util.List;

public class PromoteDemoteAdminsIq extends IQ {

    public static final String ELEMENT = "group";
    public static final String NAMESPACE = "halloapp:groups";

    private static final String ATTRIBUTE_ACTION = "action";
    private static final String ATTRIBUTE_GID = "gid";

    private final GroupId groupId;
    private final List<UserId> promoteUids;
    private final List<UserId> demoteUids;

    protected PromoteDemoteAdminsIq(@NonNull GroupId groupId, @Nullable List<UserId> promoteUids, @Nullable List<UserId> demoteUids) {
        super(ELEMENT, NAMESPACE);
        setType(Type.set);
        this.groupId = groupId;
        this.promoteUids = promoteUids;
        this.demoteUids = demoteUids;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.xmlnsAttribute(NAMESPACE);
        xml.attribute(ATTRIBUTE_ACTION, "modify_admins");
        xml.attribute(ATTRIBUTE_GID, groupId.rawId());
        xml.rightAngleBracket();
        if (promoteUids != null) {
            for (UserId uid : promoteUids) {
                xml.append(new MemberElement(uid, "promote").toXML(NAMESPACE));
            }
        }
        if (demoteUids != null) {
            for (UserId uid : demoteUids) {
                xml.append(new MemberElement(uid, "demote").toXML(NAMESPACE));
            }
        }
        return xml;
    }
}
