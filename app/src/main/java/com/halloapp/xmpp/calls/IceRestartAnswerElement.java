package com.halloapp.xmpp.calls;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.IceCandidate;
import com.halloapp.proto.server.IceRestartAnswer;
import com.halloapp.proto.server.WebRtcSessionDescription;

public class IceRestartAnswerElement {

    private final String callId;
    private final int restartIndex;
    private final WebRtcSessionDescription sdp;

    public IceRestartAnswerElement(@NonNull String callId, int restartIndex, @NonNull WebRtcSessionDescription sdp) {
        this.callId = callId;
        this.restartIndex = restartIndex;
        this.sdp = sdp;
    }

    public IceRestartAnswer toProto() {
        return IceRestartAnswer.newBuilder()
                .setCallId(callId)
                .setIdx(restartIndex)
                .setWebrtcAnswer(sdp)
                .build();
    }
}
