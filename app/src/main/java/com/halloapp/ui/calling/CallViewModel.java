package com.halloapp.ui.calling;

import android.content.Context;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.halloapp.calling.CallAudioManager;
import com.halloapp.calling.CallManager;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.EndCall;
import com.halloapp.util.logs.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

public class CallViewModel extends ViewModel {

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

    @Nullable
    private CallAudioManager audioManager;
    //TODO(nikola): Move the audio manager to the callManager
    @Nullable
    private CallManager callManager;

    private UserId peerUid;

    public CallViewModel() {
        state.postValue(State.STATE_INIT);

    }

    public void setPeerUid(UserId peerUid) {
        this.peerUid = peerUid;
    }

    public void initAudioManager(Context context) {
        if (audioManager != null) {
            return;
        }
        callManager = CallManager.getInstance();
        // TODO(nikola): remove this call. switch to to observer pattern
        callManager.setCallViewModel(this);
        audioManager = CallAudioManager.create(context.getApplicationContext());
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.i("Starting the audio manager " + audioManager);
        audioManager.start(new CallAudioManager.AudioManagerEvents() {
            // This method will be called each time the number of available audio
            // devices has changed.
            @Override
            public void onAudioDeviceChanged(
                    CallAudioManager.AudioDevice audioDevice, Set<CallAudioManager.AudioDevice> availableAudioDevices) {
                Log.d("onAudioManagerDevicesChanged: " +
                        availableAudioDevices + ", " + "selected: " + audioDevice);
            }
        });
    }

    public void stopAudioManager() {
        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }
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
        // had to move this to the constructor rather then here.
        // initAudioManager(context);
        callManager.startCall(this, peerUid);
    }

    public void onCancelCall() {
        // TODO(nikola): add new reason Cancel
        callManager.onEndCall(EndCall.Reason.REJECT);
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

    public void onPeerIsRinging() {
        Log.i("onPeerIsRinging");
        isPeerRinging.postValue(true);
    }
    public void onAnswerCall() {
        Log.i("onAnswerCall");
        state.postValue(State.STATE_IN_CALL);
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

    // TODO(nikola): rename to onRemoteEndCall()
    public void onEndCall() {
        Log.i("onEndCall");
        endCall();
    }

    @UiThread
    public void onEndCallCleanup() {
        if (callManager != null) {
            callManager.stop();
        }
        stopAudioManager();
    }

    private void endCall() {
        state.postValue(State.STATE_END);
    }

    public void onMute() {
        Boolean curState = isMuted.getValue();
        Log.i("onMute " + curState + " " + !isMuted.getValue());
        // TODO(nikola): postValue or setValue?
        isMuted.setValue(!isMuted.getValue());
        callManager.onMute(isMuted.getValue());
    }

    public void onSpeakerPhone() {
        Log.i("onSpeakerPhone");
        Boolean curState = isSpeakerOn.getValue();
        Log.i("onSpeakerPhone " + curState + " " + !isSpeakerOn.getValue());
        isSpeakerOn.setValue(!isSpeakerOn.getValue());
        if (isSpeakerOn.getValue()) {
            audioManager.setDefaultAudioDevice(CallAudioManager.AudioDevice.SPEAKER_PHONE);
        } else {
            audioManager.setDefaultAudioDevice(CallAudioManager.AudioDevice.EARPIECE);
        }
    }

}
