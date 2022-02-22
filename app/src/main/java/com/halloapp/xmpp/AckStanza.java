package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.Ack;

public class AckStanza extends HalloStanza {

    AckStanza(@NonNull String id) {
        setStanzaId(id);
    }

    @Override
    public @NonNull String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Ack Stanza [");
        logCommonAttributes(sb);
        sb.append(']');
        return sb.toString();
    }

    public Ack toProto() {
        return Ack.newBuilder()
                .setId(getStanzaId())
                .build();
    }
}
