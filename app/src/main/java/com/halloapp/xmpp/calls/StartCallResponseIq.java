package com.halloapp.xmpp.calls;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.StartCallResult;
import com.halloapp.proto.server.StunServer;
import com.halloapp.proto.server.TurnServer;
import com.halloapp.xmpp.HalloIq;

import java.util.List;

public class StartCallResponseIq extends HalloIq {

    public final StartCallResult.Result result;
    public final List<StunServer> stunServers;
    public final List<TurnServer> turnServers;
    public final Long timestamp;

    private StartCallResponseIq(StartCallResult startCallResult) {
        result = startCallResult.getResult();
        stunServers = startCallResult.getStunServersList();
        turnServers = startCallResult.getTurnServersList();
        timestamp = startCallResult.getTimestampMs();
    }

    @Override
    public Iq toProtoIq() {
        return null;
    }

    public static StartCallResponseIq fromProto(StartCallResult startCallResult) {
        return new StartCallResponseIq(startCallResult);
    }
}
