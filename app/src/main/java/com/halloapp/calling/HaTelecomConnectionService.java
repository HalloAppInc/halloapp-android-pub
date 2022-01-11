package com.halloapp.calling;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.halloapp.AppContext;
import com.halloapp.util.logs.Log;

@RequiresApi(api = 23)
public class HaTelecomConnectionService extends ConnectionService {

    public static final String EXTRA_CALL_ID = "call_id";
    public static final String EXTRA_PEER_UID = "peer_uid";
    public static final String EXTRA_PEER_UID_PHONE = "peer_uid_phone";
    public static final String EXTRA_PEER_UID_NAME = "peer_uid_name";


    @Override
    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.i("HaTelecomConnectionService onCreateIncomingConnection request:" + request);
        String callId = request.getExtras().getString(EXTRA_CALL_ID, null);
        String peerUid = request.getExtras().getString(EXTRA_PEER_UID, null);
        String callerPhone = request.getExtras().getString(EXTRA_PEER_UID_PHONE, "unknown number");
        String callerDisplayName = request.getExtras().getString(EXTRA_PEER_UID_NAME, "unknown caller");

        HaTelecomConnection telecomConnection = new HaTelecomConnection(callId, peerUid, callerDisplayName, callerPhone, false);
        if (Build.VERSION.SDK_INT >= 25) {
            telecomConnection.setConnectionProperties(Connection.PROPERTY_SELF_MANAGED | Connection.PROPERTY_HIGH_DEF_AUDIO);
        }
        telecomConnection.setConnectionCapabilities(Connection.CAPABILITY_MUTE);

        telecomConnection.setCallerDisplayName(callerDisplayName, TelecomManager.PRESENTATION_ALLOWED);
        Uri address = Uri.fromParts("tel", callerPhone, null);

        telecomConnection.setAddress(address, TelecomManager.PRESENTATION_ALLOWED);
        telecomConnection.setVideoState(VideoProfile.STATE_AUDIO_ONLY);
        CallManager.getInstance().setTelecomConnection(telecomConnection);
        return telecomConnection;
    }

    @Override
    public void onCreateIncomingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.i("HaTelecomConnectionService onCreateIncomingConnectionFailed request:" + request);
        Log.sendErrorReport("Telecom Incoming Call failed");
        // TODO(nikola): Move string to strings
        String text = "HalloApp call failed";
        Toast.makeText(AppContext.getInstance().get(), text, Toast.LENGTH_SHORT).show();
        // TODO(nikola): We should tell the other side the call has failed.
    }

    @Override
    public void onCreateOutgoingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.e("HaTelecomConnectionService.onCreateOutgoingConnectionFailed request:" + request);
        Log.sendErrorReport("Telecom Outgoing Call failed");
        // TODO(nikola): Move string to strings
        String text = "HalloApp call failed";
        Toast.makeText(AppContext.getInstance().get(), text, Toast.LENGTH_SHORT).show();
        // TODO(nikola): We should tell the other side the call has failed.
    }

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.i("HaTelecomConnectionService.onCreateOutgoingConnection(request: " + request + ")");
        Bundle extras = request.getExtras();
        String callId = request.getExtras().getString(EXTRA_CALL_ID, null);
        String peerUid = request.getExtras().getString(EXTRA_PEER_UID, null);
        String callerPhone = request.getExtras().getString(EXTRA_PEER_UID_PHONE, "unknown number");
        String callerDisplayName = request.getExtras().getString(EXTRA_PEER_UID_NAME, "unknown caller");

        HaTelecomConnection telecomConnection = new HaTelecomConnection(callId, peerUid, callerDisplayName, callerPhone, true);
        if (Build.VERSION.SDK_INT >= 25) {
            telecomConnection.setConnectionProperties(Connection.PROPERTY_SELF_MANAGED | Connection.PROPERTY_HIGH_DEF_AUDIO);
        }
        telecomConnection.setConnectionCapabilities(Connection.CAPABILITY_MUTE);

        telecomConnection.setCallerDisplayName(callerDisplayName, TelecomManager.PRESENTATION_ALLOWED);
        Uri address = Uri.fromParts("tel", callerPhone, null);

        telecomConnection.setAddress(address, TelecomManager.PRESENTATION_ALLOWED);
        telecomConnection.setVideoState(VideoProfile.STATE_AUDIO_ONLY);
        CallManager.getInstance().setTelecomConnection(telecomConnection);
        CallManager.getInstance().finishStartCall();
        return telecomConnection;
    }

}