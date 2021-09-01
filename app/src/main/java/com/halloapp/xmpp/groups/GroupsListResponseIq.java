package com.halloapp.xmpp.groups;

import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.groups.GroupInfo;
import com.halloapp.id.GroupId;
import com.halloapp.proto.clients.Background;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.GroupsStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.HalloIq;

import java.util.ArrayList;
import java.util.List;

public class GroupsListResponseIq extends HalloIq {

    public final List<GroupInfo> groupInfos = new ArrayList<>();

    private GroupsListResponseIq(List<GroupInfo> groupInfos) {
        this.groupInfos.addAll(groupInfos);
    }

    @Override
    public Iq toProtoIq() {
        return null;
    }

    public static GroupsListResponseIq fromProto(GroupsStanza groupsStanza) {
        List<GroupInfo> groupInfos = new ArrayList<>();
        for (GroupStanza groupStanza : groupsStanza.getGroupStanzasList()) {
            Background b = null;
            try {
                b = Background.parseFrom(groupStanza.getBackgroundBytes());
            } catch (InvalidProtocolBufferException e) {
                Log.w("Failed to parse background", e);
            }
            groupInfos.add(new GroupInfo(new GroupId(groupStanza.getGid()), groupStanza.getName(), null, groupStanza.getAvatarId(), b, new ArrayList<>()));
        }
        return new GroupsListResponseIq(groupInfos);
    }
}
