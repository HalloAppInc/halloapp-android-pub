package com.halloapp.calling;

import com.halloapp.id.UserId;

public interface CallObserver {

    void onIncomingCall(String callId, UserId peerUid);

    void onPeerIsRinging(String callId, UserId peerUid);

    void onAnsweredCall(String callId, UserId peerUid);

    void onEndCall(String callId, UserId peerUid);

    void onMicrophoneMute(boolean mute);

    void onSpeakerPhoneOn(boolean on);
}
