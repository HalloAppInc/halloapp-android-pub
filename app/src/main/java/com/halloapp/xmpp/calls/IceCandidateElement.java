package com.halloapp.xmpp.calls;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.IceCandidate;

public class IceCandidateElement {

    private final String callId;
    private final String sdpMediaId;
    private final int sdpMediaLineIndex;
    private final String sdp;

    public IceCandidateElement(@NonNull String callId, @NonNull String sdpMediaId, int sdpMediaLineIndex, @NonNull String sdp) {
        this.callId = callId;
        this.sdpMediaId = sdpMediaId;
        this.sdpMediaLineIndex = sdpMediaLineIndex;
        this.sdp = sdp;
    }

    public IceCandidate toProto() {
        return IceCandidate.newBuilder()
                .setCallId(callId)
                .setSdpMediaId(sdpMediaId)
                .setSdpMediaLineIndex(sdpMediaLineIndex)
                .setSdp(sdp)
                .build();
    }
}
