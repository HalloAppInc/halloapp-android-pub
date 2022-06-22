package com.halloapp.xmpp.calls;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.IceRestartOffer;
import com.halloapp.proto.server.WebRtcSessionDescription;

public class IceRestartOfferElement {

    private final String callId;
    private final int restartIndex;
    private final WebRtcSessionDescription sdp;

    public IceRestartOfferElement(@NonNull String callId, int restartIndex, @NonNull WebRtcSessionDescription sdp) {
        this.callId = callId;
        this.restartIndex = restartIndex;
        this.sdp = sdp;
    }

    public IceRestartOffer toProto() {
        return IceRestartOffer.newBuilder()
                .setCallId(callId)
                .setIdx(restartIndex)
                .setWebrtcOffer(sdp)
                .build();
    }
}
