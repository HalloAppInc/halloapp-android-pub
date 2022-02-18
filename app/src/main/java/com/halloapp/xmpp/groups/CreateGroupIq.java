package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.GroupMember;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

import java.util.List;

public class CreateGroupIq extends HalloIq {

    private final String name;
    private final List<UserId> uids;

    protected CreateGroupIq(@NonNull String name, @NonNull List<UserId> uids) {
        this.name = name;
        this.uids = uids;
    }

    @Override
    public Iq.Builder toProtoIq() {
        GroupStanza.Builder builder = GroupStanza.newBuilder();
        builder.setAction(GroupStanza.Action.CREATE);
        builder.setName(name);
        for (UserId userId : uids) {
            GroupMember groupMember = GroupMember.newBuilder()
                    .setUid(Long.parseLong(userId.rawId()))
                    .build();
            builder.addMembers(groupMember);
        }
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setId(getStanzaId())
                .setGroupStanza(builder);
    }
}
