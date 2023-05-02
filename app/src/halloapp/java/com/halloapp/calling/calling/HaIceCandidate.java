package com.halloapp.calling.calling;

import androidx.annotation.NonNull;

import org.webrtc.IceCandidate;

public class HaIceCandidate {
    private final String callId;
    private final IceCandidate iceCandidate;
    private final long timestamp;

    public HaIceCandidate(@NonNull String callId, @NonNull IceCandidate iceCandidate) {
        this.callId = callId;
        this.iceCandidate = iceCandidate;
        this.timestamp = System.currentTimeMillis();
    }

    public String getCallId() {
        return callId;
    }

    public IceCandidate getIceCandidate() {
        return iceCandidate;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
