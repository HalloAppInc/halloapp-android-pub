package com.halloapp.xmpp.calls;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.EndCall;

public class EndCallElement {

    private final String callId;
    private final EndCall.Reason reason;
    private final long timestamp = 0;

    public EndCallElement(@NonNull String callId, @NonNull EndCall.Reason reason) {
        this.callId = callId;
        this.reason = reason;
    }

    public String getCallId() {
        return callId;
    }

    public EndCall.Reason getReason() {
        return reason;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public EndCall toProto() {
        return EndCall.newBuilder()
                .setCallId(callId)
                .setReason(reason)
                .build();
    }
}
