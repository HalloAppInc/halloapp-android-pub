package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.PushRegister;
import com.halloapp.proto.server.PushToken;

public class PushRegisterRequestIq extends HalloIq {

    private final String token;

    PushRegisterRequestIq(@NonNull String token) {
        this.token = token;
    }

    @Override
    public Iq toProtoIq() {
        return Iq.newBuilder()
                .setId(getStanzaId())
                .setType(Iq.Type.SET)
                .setPushRegister(
                        PushRegister.newBuilder()
                                .setPushToken(
                                        PushToken.newBuilder()
                                                .setOs(PushToken.Os.ANDROID)
                                                .setToken(token)
                                )
                )
                .build();
    }
}

