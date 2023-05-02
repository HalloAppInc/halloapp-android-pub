package com.halloapp.xmpp.calls;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.CallType;
import com.halloapp.proto.server.GetCallServers;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

public class GetCallServersIq extends HalloIq {

    private final String callId;
    private final UserId peerUid;
    private final CallType callType;

    protected GetCallServersIq(@NonNull String callId, @NonNull UserId peerUid, @NonNull CallType callType) {
        this.callId = callId;
        this.peerUid = peerUid;
        this.callType = callType;
    }

    @Override
    public Iq.Builder toProtoIq() {
        GetCallServers.Builder builder = GetCallServers.newBuilder()
                .setCallType(callType)
                .setPeerUid(peerUid.rawIdLong())
                .setCallId(callId);
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setGetCallServers(builder);
    }
}
