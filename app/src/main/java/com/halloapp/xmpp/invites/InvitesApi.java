package com.halloapp.xmpp.invites;

import androidx.annotation.NonNull;

import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.util.Observable;


import java.util.Collections;

public class InvitesApi {

    private final Connection connection;

    public InvitesApi(@NonNull Connection connection) {
        this.connection = connection;
    }

    public Observable<Integer> getAvailableInviteCount() {
        final InvitesRequestIq requestIq = InvitesRequestIq.createGetInviteIq();
        Observable<InvitesResponseIq> inviteRequest = connection.sendRequestIq(requestIq);
        return inviteRequest.map(inviteIq -> inviteIq.invitesLeft);
    }

    public Observable<Integer> sendInvite(@NonNull String phoneNumber) {
        final InvitesRequestIq requestIq = InvitesRequestIq.createSendInviteIq(Collections.singleton(phoneNumber));
        Observable<InvitesResponseIq> sendResponse = connection.sendRequestIq(requestIq);
        return sendResponse.map(responseIq -> {
            if (!responseIq.successfulInvites.isEmpty()) {
                return InvitesResponseIq.Result.SUCCESS;
            } else {
                for (String phone : responseIq.failedInvites.keySet()) {
                    return responseIq.failedInvites.get(phone);
                }
            }
            return InvitesResponseIq.Result.UNKNOWN;
        });
    }

}
