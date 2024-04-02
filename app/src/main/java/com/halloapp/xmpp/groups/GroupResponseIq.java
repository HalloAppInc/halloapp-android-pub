package com.halloapp.xmpp.groups;

import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.id.GroupId;
import com.halloapp.proto.clients.Background;
import com.halloapp.proto.server.ExpiryInfo;
import com.halloapp.proto.server.GroupMember;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.HalloIq;

import java.util.ArrayList;
import java.util.List;

public class GroupResponseIq extends HalloIq {

    public final GroupStanza.GroupType groupType;
    public final GroupId groupId;
    public final String name;
    public final String description;
    public final String avatar;
    public final String action;
    public final String result;
    public final Background background;

    public final List<MemberElement> memberElements;

    public final ExpiryInfo expiryInfo;

    private GroupResponseIq(GroupStanza groupStanza) {
        groupType = groupStanza.getGroupType();
        groupId = new GroupId(groupStanza.getGid());
        name = groupStanza.getName();
        description = null; // TODO: fetch this once supported
        avatar = groupStanza.getAvatarId();
        action = groupStanza.getAction().name();
        result = null;
        memberElements = new ArrayList<>();
        Background b = null;
        try {
            b = Background.parseFrom(groupStanza.getBackgroundBytes());
        } catch (InvalidProtocolBufferException e) {
            Log.w("Failed to parse background", e);
        }
        background = b;
        for (GroupMember groupMember : groupStanza.getMembersList()) {
            memberElements.add(new MemberElement(groupMember));
        }
        expiryInfo = groupStanza.hasExpiryInfo() ? groupStanza.getExpiryInfo() : null;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static GroupResponseIq fromProto(GroupStanza groupStanza) {
        return new GroupResponseIq(groupStanza);
    }
}
