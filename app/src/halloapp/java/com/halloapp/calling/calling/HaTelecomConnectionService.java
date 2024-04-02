package com.halloapp.calling.calling;

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
import com.halloapp.props.ServerProps;
import com.halloapp.proto.server.CallType;
import com.halloapp.util.logs.Log;

@RequiresApi(api = 23)
public class HaTelecomConnectionService extends ConnectionService {

    public static final String EXTRA_CALL_ID = "call_id";
    public static final String EXTRA_PEER_UID = "peer_uid";
    public static final String EXTRA_PEER_UID_PHONE = "peer_uid_phone";
    public static final String EXTRA_PEER_UID_NAME = "peer_uid_name";
    public static final String EXTRA_CALL_TYPE = "call_type";

    @Override
    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.i("HaTelecomConnectionService onCreateIncomingConnection request:" + request);
        String callId = request.getExtras().getString(EXTRA_CALL_ID, null);
        String peerUid = request.getExtras().getString(EXTRA_PEER_UID, null);
        String callerPhone = request.getExtras().getString(EXTRA_PEER_UID_PHONE, "unknown number");
        String callerDisplayName = request.getExtras().getString(EXTRA_PEER_UID_NAME, "unknown caller");
        CallType callType = CallType.forNumber(request.getExtras().getInt(EXTRA_CALL_TYPE, CallType.AUDIO_VALUE));

        HaTelecomConnection telecomConnection = new HaTelecomConnection(callId, peerUid, callerDisplayName, callerPhone, false, callType);
        if (Build.VERSION.SDK_INT >= 30) {
            telecomConnection.setConnectionProperties(Connection.PROPERTY_SELF_MANAGED | Connection.PROPERTY_HIGH_DEF_AUDIO);
        } else if (Build.VERSION.SDK_INT >= 26) {
            telecomConnection.setConnectionProperties(Connection.PROPERTY_SELF_MANAGED);
        }
        telecomConnection.setConnectionCapabilities(connectionCapabilities());

        telecomConnection.setCallerDisplayName(callerDisplayName, TelecomManager.PRESENTATION_ALLOWED);
        Uri address = Uri.fromParts("tel", callerPhone, null);

        telecomConnection.setAddress(address, TelecomManager.PRESENTATION_ALLOWED);
        if (callType == CallType.AUDIO) {
            telecomConnection.setVideoState(VideoProfile.STATE_AUDIO_ONLY);
        } else {
            telecomConnection.setVideoState(VideoProfile.STATE_BIDIRECTIONAL);
        }
        CallManager.getInstance().setTelecomConnection(telecomConnection);
        return telecomConnection;
    }

    @Override
    public void onCreateIncomingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.i("HaTelecomConnectionService onCreateIncomingConnectionFailed request:" + request);
        Log.sendErrorReport("Telecom Incoming Call failed");
        // TODO: Move string to strings
        String text = "HalloApp call failed";
        Toast.makeText(AppContext.getInstance().get(), text, Toast.LENGTH_SHORT).show();
        // TODO: We should tell the other side the call has failed.
    }

    @Override
    public void onCreateOutgoingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.e("HaTelecomConnectionService.onCreateOutgoingConnectionFailed request:" + request);
        Log.sendErrorReport("Telecom Outgoing Call failed");
        // TODO: Move string to strings
        String text = "HalloApp call failed";
        Toast.makeText(AppContext.getInstance().get(), text, Toast.LENGTH_SHORT).show();
        // TODO: We should tell the other side the call has failed.
    }

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.i("HaTelecomConnectionService.onCreateOutgoingConnection(request: " + request + ")");
        Bundle extras = request.getExtras();
        String callId = request.getExtras().getString(EXTRA_CALL_ID, null);
        String peerUid = request.getExtras().getString(EXTRA_PEER_UID, null);
        String callerPhone = request.getExtras().getString(EXTRA_PEER_UID_PHONE, "unknown number");
        String callerDisplayName = request.getExtras().getString(EXTRA_PEER_UID_NAME, "unknown caller");
        CallType callType = CallType.forNumber(request.getExtras().getInt(EXTRA_CALL_TYPE, CallType.AUDIO_VALUE));

        HaTelecomConnection telecomConnection = new HaTelecomConnection(callId, peerUid, callerDisplayName, callerPhone, true, callType);
        if (Build.VERSION.SDK_INT >= 30) {
            telecomConnection.setConnectionProperties(Connection.PROPERTY_SELF_MANAGED | Connection.PROPERTY_HIGH_DEF_AUDIO);
        } else if (Build.VERSION.SDK_INT >= 26) {
            telecomConnection.setConnectionProperties(Connection.PROPERTY_SELF_MANAGED);
        }
        telecomConnection.setConnectionCapabilities(connectionCapabilities());

        telecomConnection.setCallerDisplayName(callerDisplayName, TelecomManager.PRESENTATION_ALLOWED);
        Uri address = Uri.fromParts("tel", callerPhone, null);

        telecomConnection.setAddress(address, TelecomManager.PRESENTATION_ALLOWED);
        if (callType == CallType.AUDIO) {
            telecomConnection.setVideoState(VideoProfile.STATE_AUDIO_ONLY);
        } else {
            telecomConnection.setVideoState(VideoProfile.STATE_BIDIRECTIONAL);
        }
        CallManager.getInstance().setTelecomConnection(telecomConnection);
        CallManager.getInstance().finishStartCall();
        return telecomConnection;
    }

    private static int connectionCapabilities() {
        int cap = Connection.CAPABILITY_MUTE;
        if (ServerProps.getInstance().getCallHoldEnabled()) {
            Log.i("HaTelecomConnectionService: call will have hold support");
            cap |= Connection.CAPABILITY_HOLD | Connection.CAPABILITY_SUPPORT_HOLD;
        }
        return cap;
    }

}
