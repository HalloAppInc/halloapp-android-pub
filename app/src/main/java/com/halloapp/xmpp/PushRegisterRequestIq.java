package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.PushRegister;
import com.halloapp.proto.server.PushToken;

public class PushRegisterRequestIq extends HalloIq {

    private final String token;
    private final String languageCode;

    PushRegisterRequestIq(@NonNull String token, @NonNull String languageCode) {
        this.token = token;
        this.languageCode = languageCode;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setPushRegister(
                        PushRegister.newBuilder()
                                .setPushToken(
                                        PushToken.newBuilder()
                                                .setTokenType(PushToken.TokenType.ANDROID)
                                                .setToken(token)
                                )
                        .setLangId(languageCode)
                );
    }
}

