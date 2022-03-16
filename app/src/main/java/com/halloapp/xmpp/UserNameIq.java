package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.Name;

public class UserNameIq extends HalloIq {

    private final String name;

    UserNameIq(@NonNull String name) {
        this.name = name;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setName(Name.newBuilder().setName(name));
    }
}
