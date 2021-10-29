package com.halloapp.ui.calling;

import android.content.Context;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.halloapp.calling.CallObserver;
import com.halloapp.calling.CallManager;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.EndCall;
import com.halloapp.util.logs.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CallViewModel extends ViewModel implements CallObserver {

    @IntDef({State.STATE_INIT, State.STATE_CALLING, State.STATE_IN_CALL, State.STATE_RINGING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
        int STATE_INIT = 0;
        int STATE_CALLING = 1;
        int STATE_IN_CALL = 2;
        int STATE_RINGING = 3;
        int STATE_END = 4;
    }

    private final MutableLiveData<Boolean> isMuted = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSpeakerOn = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isPeerRinging = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> state = new MutableLiveData<>();

    private final CallManager callManager;

    private UserId peerUid;

    public CallViewModel() {
        state.postValue(State.STATE_INIT);
        callManager = CallManager.getInstance();
        callManager.addObserver(this);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        callManager.removeObserver(this);
    }

    public void setPeerUid(UserId peerUid) {
        this.peerUid = peerUid;
    }


    @NonNull
    public LiveData<Integer> getState() {
        return state;
    }

    @NonNull
    public LiveData<Boolean> isPeerRinging() {
        return isPeerRinging;
    }

    public boolean inCall() {
        return state.getValue() != null && state.getValue() == State.STATE_IN_CALL;
    }

    public boolean isRinging() {
        return state.getValue() != null && state.getValue() == State.STATE_RINGING;
    }

    public boolean isCalling() {
        return state.getValue() != null && state.getValue() == State.STATE_CALLING;
    }

    @SuppressWarnings("ConstantConditions")
    public boolean isMuted() {
        return isMuted.getValue();
    }

    @SuppressWarnings("ConstantConditions")
    public boolean isSpeakerOn() {
        return isSpeakerOn.getValue();
    }

    public void onStart(Context context) {
        state.postValue(State.STATE_CALLING);
        callManager.startCall(peerUid);
    }

    public void onCancelCall() {
        // TODO(nikola): add new reason Cancel
        callManager.onEndCall(EndCall.Reason.REJECT);
        endCall();
    }


    @Override
    public void onIncomingCall(String callId, UserId peerUid) {
        this.onIncomingCall();
    }

    @Override
    public void onPeerIsRinging(String callId, UserId peerUid) {
        Log.i("onPeerIsRinging");
        isPeerRinging.postValue(true);
    }

    @Override
    public void onAnsweredCall(String callId, UserId peerUid) {
        Log.i("onAnswerCall");
        state.postValue(State.STATE_IN_CALL);
    }

    @Override
    public void onEndCall(String callId, UserId peerUid) {
        Log.i("onEndCall");
        endCall();
    }

    public void onIncomingCall() {
        Log.i("onIncomingCall");
        state.postValue(State.STATE_RINGING);
    }

    public void onDeclineCall() {
        Log.i("onDeclineCall");
        callManager.onEndCall(EndCall.Reason.REJECT);
        endCall();
    }

    public void onAcceptCall() {
        Log.i("onAcceptCall");
        state.postValue(State.STATE_IN_CALL);
        // TODO(nikola): we should include the call id here.
        callManager.onAcceptCall();
    }

    public void onHangUp() {
        Log.i("onHangUp");
        callManager.onEndCall(EndCall.Reason.CALL_END);
        endCall();
    }

    @UiThread
    public void onEndCallCleanup() {
        if (callManager != null) {
            callManager.stop();
        }
    }

    private void endCall() {
        state.postValue(State.STATE_END);
    }

    public void onMute() {
        Boolean curState = isMuted.getValue();
        Log.i("onMute " + curState + " " + !isMuted.getValue());
        // TODO(nikola): postValue or setValue?
        isMuted.setValue(!isMuted.getValue());
        callManager.setMicrophoneMute(isMuted.getValue());
    }

    public void onSpeakerPhone() {
        Log.i("onSpeakerPhone");
        Boolean curState = isSpeakerOn.getValue();
        Log.i("onSpeakerPhone " + curState + " " + !isSpeakerOn.getValue());
        isSpeakerOn.setValue(!isSpeakerOn.getValue());
        // TODO(nikola): move the isSpeakerOn and isMuted states into the callManager
        callManager.setSpeakerPhoneOn(isSpeakerOn.getValue());
    }

}
