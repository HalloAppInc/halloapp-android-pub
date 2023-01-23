package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.SetBioRequest;

public class SetBioRequestIq extends HalloIq {

    private final String bio;

    public SetBioRequestIq(@NonNull String bio) {
        this.bio = bio;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setSetBioRequest(SetBioRequest.newBuilder().setText(bio));
    }
}

