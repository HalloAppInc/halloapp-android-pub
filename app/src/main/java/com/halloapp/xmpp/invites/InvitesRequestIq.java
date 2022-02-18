package com.halloapp.xmpp.invites;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.Invite;
import com.halloapp.proto.server.InvitesRequest;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InvitesRequestIq extends HalloIq {

    private List<String> invitedNumbers;

    public static InvitesRequestIq createSendInviteIq(@NonNull Collection<String> invitedNumbers) {
        InvitesRequestIq requestIq = new InvitesRequestIq();
        requestIq.invitedNumbers = new ArrayList<>(invitedNumbers);
        return requestIq;
    }

    public static InvitesRequestIq createGetInviteIq() {
        return new InvitesRequestIq();
    }

    private InvitesRequestIq() {}

    @Override
    public Iq.Builder toProtoIq() {
        InvitesRequest.Builder builder = InvitesRequest.newBuilder();
        if (invitedNumbers != null) {
            for (String phone : invitedNumbers) {
                builder.addInvites(Invite.newBuilder().setPhone(phone).build());
            }
        }
        return Iq.newBuilder()
                .setType(invitedNumbers != null && !invitedNumbers.isEmpty() ? Iq.Type.SET : Iq.Type.GET)
                .setInvitesRequest(builder.build());
    }
}
