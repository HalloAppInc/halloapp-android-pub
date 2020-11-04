package com.halloapp.xmpp.groups;

import com.halloapp.id.GroupId;
import com.halloapp.proto.server.GroupMember;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

import java.util.ArrayList;
import java.util.List;

public class GroupResponseIq extends HalloIq {

    public final GroupId groupId;
    public final String name;
    public final String description;
    public final String avatar;
    public final String action;
    public final String result;

    public final List<MemberElement> memberElements;

    private GroupResponseIq(GroupStanza groupStanza) {
        groupId = new GroupId(groupStanza.getGid());
        name = groupStanza.getName();
        description = null; // TODO(jack): fetch this once supported
        avatar = groupStanza.getAvatarId();
        action = groupStanza.getAction().name();
        result = null;
        memberElements = new ArrayList<>();
        for (GroupMember groupMember : groupStanza.getMembersList()) {
            memberElements.add(new MemberElement(groupMember));
        }
    }

    @Override
    public Iq toProtoIq() {
        return null;
    }

    public static GroupResponseIq fromProto(GroupStanza groupStanza) {
        return new GroupResponseIq(groupStanza);
    }
}
