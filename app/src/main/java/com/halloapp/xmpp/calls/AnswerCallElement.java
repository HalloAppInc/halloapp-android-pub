package com.halloapp.xmpp.calls;

import com.google.protobuf.ByteString;
import com.halloapp.proto.server.AnswerCall;
import com.halloapp.proto.server.WebRtcSessionDescription;

public class AnswerCallElement {

    private final String callId;
    // TODO: change to the encrypted version
    private final String webrtcAnswer;

    public AnswerCallElement(String callId, String webrtcAnswer) {
        this.callId = callId;
        this.webrtcAnswer = webrtcAnswer;
    }

    public String getCallId() {
        return callId;
    }

    public String getWebrtcAnswer() {
        return webrtcAnswer;
    }

    public AnswerCall toProto() {
        return AnswerCall.newBuilder()
                .setCallId(callId)
                .setWebrtcAnswer(WebRtcSessionDescription.newBuilder()
                        .setEncPayload(ByteString.copyFrom(webrtcAnswer.getBytes()))
                        .build())
                .build();
    }
}
