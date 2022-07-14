package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.ExpiryInfo;
import com.halloapp.proto.server.GroupMember;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

import java.util.List;

public class CreateGroupIq extends HalloIq {

    private final String name;
    private final List<UserId> uids;
    private final ExpiryInfo expiryInfo;

    protected CreateGroupIq(@NonNull String name, @NonNull List<UserId> uids, @NonNull ExpiryInfo expiryInfo) {
        this.name = name;
        this.uids = uids;
        this.expiryInfo = expiryInfo;
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
        builder.setExpiryInfo(expiryInfo);
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setGroupStanza(builder);
    }
}
