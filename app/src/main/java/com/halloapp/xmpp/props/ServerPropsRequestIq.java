package com.halloapp.xmpp.props;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.Props;
import com.halloapp.xmpp.HalloIq;

public class ServerPropsRequestIq extends HalloIq {

    @Override
    public Iq toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setId(getStanzaId())
                .setProps(Props.newBuilder().build())
                .build();
    }
}
