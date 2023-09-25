package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.FriendshipRequest;
import com.halloapp.proto.server.Iq;

public class FriendshipRequestIq extends HalloIq {

    private final UserId userId;
    private final FriendshipRequest.Action action;

    public FriendshipRequestIq(@NonNull UserId userId, @NonNull FriendshipRequest.Action action) {
        this.userId = userId;
        this.action = action;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setFriendshipRequest(FriendshipRequest.newBuilder()
                        .setUid(Long.parseLong(userId.rawId()))
                        .setAction(action));
    }
}
