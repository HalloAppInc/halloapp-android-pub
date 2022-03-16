package com.halloapp.xmpp.calls;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.crypto.signal.SignalSessionSetupInfo;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.CallType;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.StartCall;
import com.halloapp.proto.server.WebRtcSessionDescription;
import com.halloapp.xmpp.HalloIq;

import java.nio.charset.StandardCharsets;

public class StartCallIq extends HalloIq {

    private final String callId;
    private final UserId peerUid;
    private final CallType callType;
    private final WebRtcSessionDescription webrtcOffer;

    protected StartCallIq(@NonNull String callId, @NonNull UserId peerUid, @NonNull CallType callType, @NonNull WebRtcSessionDescription webrtcOffer) {
        this.callId = callId;
        this.peerUid = peerUid;
        this.callType = callType;
        this.webrtcOffer = webrtcOffer;
    }


    @Override
    public Iq.Builder toProtoIq() {
        StartCall.Builder builder = StartCall.newBuilder()
                .setCallType(callType)
                .setPeerUid(peerUid.rawIdLong())
                .setCallId(callId)
                .setWebrtcOffer(webrtcOffer);
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setStartCall(builder);
    }
}
