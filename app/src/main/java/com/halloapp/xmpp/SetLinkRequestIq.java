package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.Link;
import com.halloapp.proto.server.SetLinkRequest;

public class SetLinkRequestIq extends HalloIq {

    private final String text;
    private final Link.Type type;
    private final SetLinkRequest.Action action;

    public SetLinkRequestIq(@NonNull String text, @NonNull Link.Type type) {
        this.text = text;
        this.type = type;
        this.action = SetLinkRequest.Action.SET;
    }
    public SetLinkRequestIq(@NonNull String text, @NonNull Link.Type type, @NonNull SetLinkRequest.Action action) {
        this.text = text;
        this.type = type;
        this.action = action;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setSetLinkRequest(SetLinkRequest.newBuilder().setAction(action).setLink(
                   Link.newBuilder().setText(text).setType(type)
                ));
    }
}

