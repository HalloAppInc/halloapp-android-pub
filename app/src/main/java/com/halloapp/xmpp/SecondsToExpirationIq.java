package com.halloapp.xmpp;

import com.halloapp.Constants;
import com.halloapp.proto.server.ClientVersion;
import com.halloapp.proto.server.Iq;

public class SecondsToExpirationIq extends HalloIq {

    Integer secondsLeft = null;

    SecondsToExpirationIq(int secondsLeft) {
        this.secondsLeft = secondsLeft;
    }

    SecondsToExpirationIq() {
    }

    @Override
    public Iq toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setId(getStanzaId())
                .setClientVersion(
                        ClientVersion.newBuilder()
                                .setVersion(Constants.USER_AGENT)
                                .build())
                .build();
    }

    public static SecondsToExpirationIq fromProto(ClientVersion clientVersion) {
        return new SecondsToExpirationIq((int)clientVersion.getExpiresInSeconds());
    }
}
