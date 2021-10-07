package com.halloapp.ui.calling;

import android.content.Context;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.halloapp.calling.CallAudioManager;
import com.halloapp.calling.CallManager;
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
    }

    private final MutableLiveData<Boolean> isMuted = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSpeakerOn = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> state = new MutableLiveData<>();

    @Nullable
    private CallAudioManager audioManager;
    //TODO(nikola): Move the audio manager to the callManager
    @Nullable
    private CallManager callManager;

    public CallViewModel() {
        state.postValue(State.STATE_INIT);
    }

    public void initAudioManager(Context context) {
        if (audioManager != null) {
            return;
        }
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
        callManager = new CallManager(this, context);
    }

    public void onCancelCall() {
        state.postValue(State.STATE_INIT);
    }

    public void onDeclineCall() {
        Log.i("onDeclineCall");
        state.postValue(State.STATE_INIT);
    }

    public void onAcceptCall() {
        Log.i("onAcceptCall");
        state.postValue(State.STATE_IN_CALL);
    }

    public void onHangUp() {
        Log.i("onHangUp");
        state.postValue(State.STATE_INIT);
        if (callManager != null) {
            callManager.stop();
            callManager = null;
        }
        stopAudioManager();
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
