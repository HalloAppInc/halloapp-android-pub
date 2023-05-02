package com.halloapp.xmpp.calls;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.halloapp.proto.server.AnswerCall;
import com.halloapp.proto.server.WebRtcSessionDescription;

public class AnswerCallElement {

    private final String callId;
    private final WebRtcSessionDescription webrtcAnswer;

    public AnswerCallElement(String callId, @NonNull WebRtcSessionDescription webrtcAnswer) {
        this.callId = callId;
        this.webrtcAnswer = webrtcAnswer;
    }

    public String getCallId() {
        return callId;
    }

    public WebRtcSessionDescription getWebrtcAnswer() {
        return webrtcAnswer;
    }

    public AnswerCall toProto() {
        return AnswerCall.newBuilder()
                .setCallId(callId)
                .setWebrtcAnswer(webrtcAnswer)
                .build();
    }
}
