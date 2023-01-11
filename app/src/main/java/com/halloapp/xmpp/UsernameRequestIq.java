package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.UsernameRequest;

public class UsernameRequestIq extends HalloIq {
    private final String username;
    private final UsernameRequest.Action action;

    public UsernameRequestIq(@NonNull String username, @NonNull UsernameRequest.Action action) {
        this.action = action;
        this.username = username;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(action == UsernameRequest.Action.SET ? Iq.Type.SET : Iq.Type.GET)
                .setUsernameRequest(UsernameRequest.newBuilder()
                        .setAction(action)
                        .setUsername(username));
    }
}
