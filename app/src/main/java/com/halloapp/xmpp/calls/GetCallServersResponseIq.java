package com.halloapp.xmpp.calls;

import com.halloapp.proto.server.CallConfig;
import com.halloapp.proto.server.GetCallServersResult;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.StunServer;
import com.halloapp.proto.server.TurnServer;
import com.halloapp.xmpp.HalloIq;

import java.util.List;

public class GetCallServersResponseIq extends HalloIq {

    public final GetCallServersResult.Result result;
    public final List<StunServer> stunServers;
    public final List<TurnServer> turnServers;
    public final CallConfig callConfig;

    private GetCallServersResponseIq(GetCallServersResult getCallServersResultResult) {
        result = getCallServersResultResult.getResult();
        stunServers = getCallServersResultResult.getStunServersList();
        turnServers = getCallServersResultResult.getTurnServersList();
        callConfig = getCallServersResultResult.getCallConfig();
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static GetCallServersResponseIq fromProto(GetCallServersResult getCallServersResult) {
        return new GetCallServersResponseIq(getCallServersResult);
    }
}
