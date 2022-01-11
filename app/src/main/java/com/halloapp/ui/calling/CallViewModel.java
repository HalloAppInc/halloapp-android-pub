package com.halloapp.ui.calling;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.halloapp.calling.CallObserver;
import com.halloapp.calling.CallManager;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.EndCall;
import com.halloapp.util.logs.Log;

public class CallViewModel extends ViewModel implements CallObserver {

    private final MutableLiveData<Integer> state = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isMicrophoneMuted = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSpeakerPhoneOn = new MutableLiveData<>(false);

    private final CallManager callManager;

    private UserId peerUid;

    public CallViewModel() {
        callManager = CallManager.getInstance();
        callManager.addObserver(this);
        Log.i("CallViewModel/ state: " + state.getValue() + " -> " + callManager.getState());
        state.setValue(callManager.getState());
        isMicrophoneMuted.setValue(callManager.isMicrophoneMuted());
        isSpeakerPhoneOn.setValue(callManager.isSpeakerPhoneOn());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        callManager.removeObserver(this);
    }

    public void setPeerUid(@NonNull UserId peerUid) {
        this.peerUid = peerUid;
    }

    @NonNull
    public LiveData<Integer> getState() {
        return state;
    }

    @NonNull
    public LiveData<Boolean> getIsMicrophoneMuted() {
        return isMicrophoneMuted;
    }

    @NonNull
    public LiveData<Boolean> getIsSpeakerPhoneOn() {
        return isSpeakerPhoneOn;
    }

    public boolean inCall() {
        return state.getValue() != null && state.getValue() == CallManager.State.IN_CALL;
    }

    public boolean isRinging() {
        return state.getValue() != null && state.getValue() == CallManager.State.INCOMING_RINGING;
    }

    public boolean isCalling() {
        return state.getValue() != null && (state.getValue() == CallManager.State.CALLING || state.getValue() == CallManager.State.CALLING_RINGING);
    }

    public boolean isIdle() {
        return state.getValue() != null && state.getValue() == CallManager.State.IDLE;
    }

    public void onStartCall() {
        if (callManager.startCall(peerUid)) {
            state.postValue(CallManager.State.CALLING);
        }
    }

    public void onCancelCall() {
        callManager.endCall(EndCall.Reason.CANCEL);
        endCall();
    }


    @Override
    public void onIncomingCall(String callId, UserId peerUid) {
        this.onIncomingCall();
    }

    @Override
    public void onPeerIsRinging(String callId, UserId peerUid) {
        Log.i("onPeerIsRinging");
        state.postValue(CallManager.State.CALLING_RINGING);
    }

    @Override
    public void onAnsweredCall(String callId, UserId peerUid) {
        Log.i("onAnswerCall");
        state.postValue(CallManager.State.IN_CALL);
    }

    @Override
    public void onEndCall(String callId, UserId peerUid) {
        Log.i("onEndCall");
        endCall();
    }

    @Override
    public void onMicrophoneMute(boolean mute) {
        isMicrophoneMuted.postValue(mute);
    }

    @Override
    public void onSpeakerPhoneOn(boolean on) {
        isSpeakerPhoneOn.postValue(on);
    }

    public void onIncomingCall() {
        Log.i("onIncomingCall");
        state.postValue(CallManager.State.INCOMING_RINGING);
    }

    public void onDeclineCall() {
        Log.i("onDeclineCall");
        callManager.endCall(EndCall.Reason.REJECT);
        endCall();
    }

    public void onAcceptCall() {
        Log.i("CallViewModel.onAcceptCall");
        // TODO(nikola): we should include the call id here.
        if (callManager.acceptCall()) {
            state.postValue(CallManager.State.IN_CALL);
        }
    }

    public void onHangUp() {
        Log.i("onHangUp");
        callManager.endCall(EndCall.Reason.CALL_END);
        endCall();
    }


    private void endCall() {
        state.postValue(CallManager.State.END);
    }

    public void toggleMicrophoneMute() {
        callManager.toggleMicrophoneMute();
    }

    public void toggleSpeakerPhone() {
        callManager.toggleSpeakerPhone();
    }

    public long getCallStartTime() {
        return callManager.getCallStartTimestamp();
    }

}
