package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.GroupMember;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

import org.jivesoftware.smack.packet.IQ;

import java.util.List;

public class PromoteDemoteAdminsIq extends HalloIq {

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

    @Override
    public Iq toProtoIq() {
        GroupStanza.Builder builder = GroupStanza.newBuilder();
        builder.setAction(GroupStanza.Action.MODIFY_ADMINS);
        builder.setGid(groupId.rawId());
        if (promoteUids != null) {
            for (UserId uid : promoteUids) {
                GroupMember groupMember = GroupMember.newBuilder()
                        .setUid(Long.parseLong(uid.rawId()))
                        .setAction(GroupMember.Action.PROMOTE)
                        .build();
                builder.addMembers(groupMember);
            }
        }
        if (demoteUids != null) {
            for (UserId uid : demoteUids) {
                GroupMember groupMember = GroupMember.newBuilder()
                        .setUid(Long.parseLong(uid.rawId()))
                        .setAction(GroupMember.Action.DEMOTE)
                        .build();
                builder.addMembers(groupMember);
            }
        }
        return Iq.newBuilder().setType(Iq.Type.SET).setId(getStanzaId()).setGroupStanza(builder.build()).build();
    }
}
