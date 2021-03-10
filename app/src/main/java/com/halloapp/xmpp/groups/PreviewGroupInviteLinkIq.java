package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.GroupInviteLink;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

public class PreviewGroupInviteLinkIq extends HalloIq {

    private String inviteCode;

    protected PreviewGroupInviteLinkIq(@NonNull String inviteCode) {
        this.inviteCode = inviteCode;
    }

    @Override
    public Iq toProtoIq() {
        GroupInviteLink groupInviteLink =
                GroupInviteLink.newBuilder()
                        .setAction(GroupInviteLink.Action.PREVIEW)
                        .setLink(inviteCode).build();
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setId(getStanzaId())
                .setGroupInviteLink(groupInviteLink)
                .build();
    }
}
