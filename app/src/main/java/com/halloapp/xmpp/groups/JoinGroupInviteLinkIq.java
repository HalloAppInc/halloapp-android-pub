package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.GroupInviteLink;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

public class JoinGroupInviteLinkIq extends HalloIq {

    private final String inviteCode;

    protected JoinGroupInviteLinkIq(@NonNull String inviteCode) {
        this.inviteCode = inviteCode;
    }

    @Override
    public Iq toProtoIq() {
        GroupInviteLink groupInviteLink =
                GroupInviteLink.newBuilder()
                        .setAction(GroupInviteLink.Action.JOIN)
                        .setLink(inviteCode).build();
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setId(getStanzaId())
                .setGroupInviteLink(groupInviteLink)
                .build();
    }
}
