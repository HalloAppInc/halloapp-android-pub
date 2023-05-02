package com.halloapp.calling.calling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.halloapp.Notifications;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.EndCall;
import com.halloapp.util.logs.Log;

public class CallNotificationBroadcastReceiver extends BroadcastReceiver {
    public static final String DECLINE = "decline";

    public static final String EXTRA_CALL_ID = "call_id";
    public static final String EXTRA_PEER_UID = "peer_uid";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Received " + intent);
        // TODO(nikola): move the logic into the CallManager.
        Notifications.getInstance(context).clearIncomingCallNotification();
        CallManager callManager = CallManager.getInstance();
        if (DECLINE.equals(intent.getAction())) {
            Log.i("Call declined via notification button");
            // TODO(nikola): extract the call, to make sure we are rejecting the right call?
            callManager.endCall(EndCall.Reason.REJECT);
        } else {
            Log.e("Unknown intent action " + intent.getAction());
        }
    }

    public static Intent declineCallIntent(Context context, String callId, UserId peerUid) {

        Intent declineIntent = new Intent(context, CallNotificationBroadcastReceiver.class);
        declineIntent.setAction(CallNotificationBroadcastReceiver.DECLINE);
        declineIntent.putExtra(EXTRA_CALL_ID, callId);
        declineIntent.putExtra(EXTRA_PEER_UID, peerUid.rawId());
        return declineIntent;
    }

}
