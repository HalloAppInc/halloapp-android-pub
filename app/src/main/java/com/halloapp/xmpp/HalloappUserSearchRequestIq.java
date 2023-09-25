package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.HalloappSearchRequest;
import com.halloapp.proto.server.Iq;

public class HalloappUserSearchRequestIq extends HalloIq {

    private final String text;

    public HalloappUserSearchRequestIq(@NonNull String text) {
        this.text = text;
    }

    @Override
    public Iq.Builder toProtoIq() {
        HalloappSearchRequest.Builder builder = HalloappSearchRequest.newBuilder();
        builder.setUsernameString(text);
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setHalloappSearchRequest(builder);
    }
}
