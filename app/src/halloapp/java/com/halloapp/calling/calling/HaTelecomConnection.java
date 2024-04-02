package com.halloapp.calling.calling;

import android.os.Build;
import android.os.Bundle;
import android.telecom.CallAudioState;
import android.telecom.Connection;
import android.telecom.DisconnectCause;

import androidx.annotation.RequiresApi;

import com.halloapp.AppContext;
import com.halloapp.Notifications;
import com.halloapp.proto.server.CallType;
import com.halloapp.proto.server.EndCall;
import com.halloapp.util.logs.Log;

@RequiresApi(api = 23)
public class HaTelecomConnection extends Connection {

    private final String callId;
    private final String peerUid;
    private final String peerDisplayName;
    private final String peerPhone;
    private final boolean isInitiator;
    private final CallType callType;

    private final CallManager callManager;


    public HaTelecomConnection(String callId, String peerUid, String peerDisplayName, String peerPhone, boolean isInitiator, CallType callType) {
        this.callManager = CallManager.getInstance();
        this.callId = callId;
        this.peerUid = peerUid;
        this.peerDisplayName = peerDisplayName;
        this.peerPhone = peerPhone;
        this.isInitiator = isInitiator;
        this.callType = callType;
    }

    @Override
    public void onCallAudioStateChanged(CallAudioState state) {
        super.onCallAudioStateChanged(state);
        Log.i("HaTelecomConnection.onCallAudioStateChanged: " + state);
        if (callType == CallType.VIDEO && state.getRoute() == CallAudioState.ROUTE_EARPIECE && (state.getSupportedRouteMask() & CallAudioState.ROUTE_SPEAKER) != 0) {
            if (Build.VERSION.SDK_INT >= 26) {
                Log.i("HaTelecomConnection.onCallAudioStateChanged: requesting SPEAKER");
                setAudioRoute(CallAudioState.ROUTE_SPEAKER);
            }
        }
    }

    @Override
    public void onStateChanged(int state) {
        Log.i("HaTelecomConnection.onStateChanged: " + Connection.stateToString(getState()) + " -> " + Connection.stateToString(state));
        super.onStateChanged(state);
    }

    @Override
    public void onDisconnect() {
        Log.i("HaTelecomConnection.onDisconnect()");
        callManager.endCall(EndCall.Reason.CALL_END);
    }

    @Override
    public void onAbort() {
        Log.i("HaTelecomConnection.onAbort()");
        super.onAbort();
    }

    @Override
    public void onHold() {
        Log.i("HaTelecomConnection.onHold()");
        callManager.onHold(callId);
    }

    @Override
    public void onUnhold() {
        Log.i("HaTelecomConnection.onUnhold()");
        callManager.onUnhold(callId);
    }

    @Override
    public void onAnswer(int videoState) {
        Log.i("HaTelecomConnection.onAnswer(videoState: " + videoState + ")");
        super.onAnswer(videoState);
    }

    @Override
    public void onAnswer() {
        Log.i("HaTelecomConnection.onAnswer()");
        super.onAnswer();
        callManager.telecomOnAnswer();
    }

    @Override
    public void onReject() {
        Log.i("HaTelecomConnection.onReject()");
        super.onReject();
        //TODO: clearing the notification should be moved in endCallAndStop
        Notifications.getInstance(AppContext.getInstance().get()).clearIncomingCallNotification();
        callManager.endCall(EndCall.Reason.REJECT);
    }

    @Override
    public void onReject(int rejectReason) {
        Log.i("HaTelecomConnection.onReject(rejectReason: " + rejectReason + ")");
        super.onReject(rejectReason);
        // TODO: use the right reject reason
        callManager.endCall(EndCall.Reason.REJECT);
    }

    @Override
    public void onReject(String replyMessage) {
        Log.i("HaTelecomConnection.onReject(replyMessage: " + replyMessage + ")");
        super.onReject(replyMessage);
    }

    @Override
    public void onSilence() {
        Log.i("HaTelecomConnection.onSilence()");
        if (Build.VERSION.SDK_INT < 29) {
            Log.i("HaTelecomConnection.onSilence returning early to avoid crash");
            return;
        }
        super.onSilence();
    }

    @Override
    public void onCallEvent(String event, Bundle extras) {
        Log.i("HaTelecomConnection.onCallEvent(event: " + event + ")");
        super.onCallEvent(event, extras);
    }

    @Override
    public void onShowIncomingCallUi() {
        Log.i("HaTelecomConnection.onShowIncomingCallUi");
        super.onShowIncomingCallUi();
        callManager.showIncomingCallNotification();
    }

    public void stop(EndCall.Reason reason) {
        Log.i("HaTelecomConnection.stop(" + reason + ")");
        if (reason == EndCall.Reason.TIMEOUT) {
            setDisconnected(new DisconnectCause(DisconnectCause.MISSED));
        } else if (reason == EndCall.Reason.REJECT) {
            setDisconnected(new DisconnectCause(DisconnectCause.REJECTED));
        } else if (reason == EndCall.Reason.CANCEL) {
            setDisconnected(new DisconnectCause(DisconnectCause.CANCELED));
        } else if (reason == EndCall.Reason.CALL_END) {
            setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));
        } else if (reason == EndCall.Reason.BUSY) {
            setDisconnected(new DisconnectCause(DisconnectCause.BUSY));
        } else {
            setDisconnected(new DisconnectCause(DisconnectCause.UNKNOWN));
        }
        destroy();
    }
}
