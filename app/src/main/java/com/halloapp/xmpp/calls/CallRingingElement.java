package com.halloapp.xmpp.calls;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.CallRinging;

public class CallRingingElement {

    private final String callId;

    public CallRingingElement(@NonNull String callId) {
        this.callId = callId;
    }

    public CallRinging toProto() {
        return CallRinging.newBuilder()
                .setCallId(callId)
                .build();
    }
}
