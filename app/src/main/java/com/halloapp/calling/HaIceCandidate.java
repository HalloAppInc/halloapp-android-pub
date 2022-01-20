package com.halloapp.calling;

import androidx.annotation.NonNull;

import org.webrtc.IceCandidate;

public class HaIceCandidate {
    private String callId;
    private IceCandidate iceCandidate;
    private long ts;

    public HaIceCandidate(@NonNull String callId, @NonNull IceCandidate iceCandidate) {
        this.callId = callId;
        this.iceCandidate = iceCandidate;
        this.ts = System.currentTimeMillis();
    }

    public String getCallId() {
        return callId;
    }

    public IceCandidate getIceCandidate() {
        return iceCandidate;
    }

    public long getTs() {
        return ts;
    }
}
