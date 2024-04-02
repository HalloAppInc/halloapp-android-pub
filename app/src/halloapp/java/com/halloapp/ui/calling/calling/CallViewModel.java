package com.halloapp.ui.calling.calling;

import android.app.Application;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.calling.calling.CallObserver;
import com.halloapp.calling.calling.CallManager;
import com.halloapp.calling.calling.HAVideoCapturer;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.CallType;
import com.halloapp.proto.server.EndCall;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

public class CallViewModel extends ViewModel implements CallObserver {

    public static class Factory implements ViewModelProvider.Factory {
        private final UserId peerUid;
        private final boolean isInitiator;
        private final CallType callType;


        Factory(@NonNull Application application, @NonNull UserId peerUid, boolean isInitiator, @NonNull CallType callType) {
            this.peerUid = peerUid;
            this.isInitiator = isInitiator;
            this.callType = callType;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CallViewModel.class)) {
                //noinspection unchecked
                return (T) new CallViewModel(peerUid, isInitiator, callType);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }


    private final MutableLiveData<Integer> state = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isMicrophoneMuted = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSpeakerPhoneOn = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isOnHold = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isRemoteVideoMute = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isRemoteAudioMute = new MutableLiveData<>(false);

    private final CallManager callManager;

    private final UserId peerUid;
    private final boolean isInitiator;
    private final CallType callType;

    public CallViewModel(@NonNull UserId peerUid, boolean isInitiator, @NonNull CallType callType) {
        callManager = CallManager.getInstance();

        if (callManager.getIsInCall().getValue()) {
            this.peerUid = callManager.getPeerUid();
            this.isInitiator = callManager.getIsInitiator();
            this.callType = callManager.getCallType();
        } else {
            this.peerUid = peerUid;
            this.isInitiator = isInitiator;
            this.callType = Preconditions.checkNotNull(callType);
        }

        callManager.addObserver(this);
        Log.i("CallViewModel/ state: " + state.getValue() + " -> " + callManager.getState());
        state.setValue(callManager.getState());
        isMicrophoneMuted.setValue(callManager.isMicrophoneMuted());
        isSpeakerPhoneOn.setValue(callManager.isSpeakerPhoneOn());
        isRemoteVideoMute.setValue(callManager.isRemoteVideoMute());
        isRemoteAudioMute.setValue(callManager.isRemoteAudioMute());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        callManager.removeObserver(this);
    }

    public UserId getPeerUid() {
        return peerUid;
    }

    @Nullable
    public CallType getCallType() {
        return callType;
    }

    public boolean getIsInitiator() {
        return isInitiator;
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

    @NonNull
    public LiveData<Boolean> getIsRemoteAudioMuted() {
        return isRemoteAudioMute;
    }

    @NonNull
    public LiveData<Boolean> getIsRemoteVideoMuted() {
        return isRemoteVideoMute;
    }

    @NonNull
    public LiveData<Boolean> getIsOnHold() {
        return isOnHold;
    }

    public boolean inCall() {
        return state.getValue() != null && state.getValue() == CallManager.State.IN_CALL;
    }

    public boolean isCallConnecting() {
        return state.getValue() != null && state.getValue() == CallManager.State.IN_CALL_CONNECTING;
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

    // TODO: It is not great that that we have to pass all this video specific arguments for voice calls..
    public void onStartCall(@NonNull CallType callType, HAVideoCapturer videoCapturer) {
        if (callManager.startCall(peerUid, callType, videoCapturer)) {
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
        state.postValue(CallManager.State.IN_CALL_CONNECTING);
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
    public void onRemoteMicrophoneMute(boolean mute) {
        isRemoteAudioMute.postValue(mute);
    }

    @Override
    public void onRemoteVideoMute(boolean mute) {
        isRemoteVideoMute.postValue(mute);
    }

    @Override
    public void onSpeakerPhoneOn(boolean on) {
        isSpeakerPhoneOn.postValue(on);
    }

    @Override
    public void onCallConnected(String callId) {
        Log.i("onCallConnected");
        state.postValue(CallManager.State.IN_CALL);
    }

    @Override
    public void onHold(boolean hold) {
        isOnHold.postValue(hold);
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

    // TODO: It is not great that that we have to pass all this video specific arguments for voice calls..
    public void onAcceptCall(HAVideoCapturer videoCapturer) {
        Log.i("CallViewModel.onAcceptCall");
        // TODO: we should include the call id here.
        if (callManager.acceptCall(videoCapturer)) {
            state.postValue(CallManager.State.IN_CALL_CONNECTING);
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

    @MainThread
    public void toggleSpeakerPhone() {
        callManager.toggleSpeakerPhone();
    }

    public long getCallConnectTime() {
        return callManager.getCallConnectTimestamp();
    }

}
