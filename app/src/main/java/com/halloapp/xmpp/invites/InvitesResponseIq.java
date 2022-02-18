package com.halloapp.xmpp.invites;

import androidx.annotation.Nullable;

import com.halloapp.proto.server.Invite;
import com.halloapp.proto.server.InvitesResponse;
import com.halloapp.proto.server.Iq;
import com.halloapp.ui.invites.InviteCountAndRefreshTime;
import com.halloapp.xmpp.HalloIq;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InvitesResponseIq extends HalloIq {

    final InviteCountAndRefreshTime inviteCountRefreshData;

    final Map<String, Integer> failedInvites = new HashMap<>();
    final Set<String> successfulInvites = new HashSet<>();

    public @interface Result {
        int UNKNOWN = -1;
        int SUCCESS = 0;
        int INVALID_NUMBER = 1;
        int NO_INVITES_LEFT = 2;
        int EXISTING_USER = 3;
        int NO_ACCOUNT = 4;
    }

    private @interface ReasonResponse {
        String INVALID_NUMBER = "invalid_number";
        String NO_INVITES_LEFT = "no_invites_left";
        String EXISTING_USER = "existing_user";
        String NO_ACCOUNT = "no_account";
    }

    private InvitesResponseIq(int invitesLeft, long refreshTimeLeft, Set<String> successfulInvites, Map<String, Integer> failedInvites) {
        this.inviteCountRefreshData = new InviteCountAndRefreshTime(invitesLeft, refreshTimeLeft);
        this.successfulInvites.addAll(successfulInvites);
        for (String key : failedInvites.keySet()) {
            this.failedInvites.put(key, failedInvites.get(key));
        }
    }

    private static @Result int parseResult(@Nullable String resultStr, @Nullable String reasonStr) {
        if ("ok".equals(resultStr)) {
            return Result.SUCCESS;
        }
        if (resultStr == null || reasonStr == null) {
            return Result.UNKNOWN;
        }
        switch (reasonStr) {
            case ReasonResponse.INVALID_NUMBER:
                return Result.INVALID_NUMBER;
            case ReasonResponse.NO_INVITES_LEFT:
                return Result.NO_INVITES_LEFT;
            case ReasonResponse.EXISTING_USER:
                return Result.EXISTING_USER;
            case ReasonResponse.NO_ACCOUNT:
                return Result.NO_ACCOUNT;
            default:
                return Result.UNKNOWN;
        }
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static InvitesResponseIq fromProto(InvitesResponse invitesResponse) {
        int invitesLeft = invitesResponse.getInvitesLeft();
        long refreshTimeLeft = invitesResponse.getTimeUntilRefresh();
        Map<String, Integer> failedInvites = new HashMap<>();
        Set<String> successfulInvites = new HashSet<>();
        for (Invite invite : invitesResponse.getInvitesList()) {
            String phone = invite.getPhone();
            if (phone != null) {
                @Result int result = parseResult(invite.getResult(), invite.getReason());
                if (result == Result.SUCCESS) {
                    successfulInvites.add(phone);
                } else {
                    failedInvites.put(phone, result);
                }
            }
        }
        return new InvitesResponseIq(invitesLeft, refreshTimeLeft, successfulInvites, failedInvites);
    }
}
