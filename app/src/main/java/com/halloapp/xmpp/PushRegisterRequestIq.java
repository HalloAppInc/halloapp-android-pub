package com.halloapp.xmpp;


import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.PushRegister;
import com.halloapp.proto.server.PushToken;

import java.util.TimeZone;

public class PushRegisterRequestIq extends HalloIq {

    private final String token;
    private final String languageCode;
    private final boolean forHuawei;

    PushRegisterRequestIq(@NonNull String token, @NonNull String languageCode, boolean forHuawei) {
        this.token = token;
        this.languageCode = languageCode;
        this.forHuawei = forHuawei;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setPushRegister(
                        PushRegister.newBuilder()
                                .setPushToken(
                                        PushToken.newBuilder()
                                                .setTokenType(forHuawei ? PushToken.TokenType.ANDROID_HUAWEI : PushToken.TokenType.ANDROID)
                                                .setToken(token)
                                )
                        .setLangId(languageCode)
                        .setZoneOffset(TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000)
                );
    }
}

