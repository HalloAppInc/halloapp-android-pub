package com.halloapp.xmpp.calls;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.halloapp.ConnectionObservers;
import com.halloapp.calling.CallManager;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.AnswerCall;
import com.halloapp.proto.server.CallRinging;
import com.halloapp.proto.server.CallType;
import com.halloapp.proto.server.EndCall;
import com.halloapp.proto.server.IncomingCall;
import com.halloapp.proto.server.Msg;
import com.halloapp.proto.server.StunServer;
import com.halloapp.proto.server.TurnServer;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ConnectionImpl;
import com.halloapp.xmpp.ProtoPrinter;
import com.halloapp.xmpp.util.Observable;

import org.webrtc.IceCandidate;

import java.util.List;


/**
 * Implements API to send Calls related messages and IQs as well as handles parsing incoming
 * call messages and calls the CallManager to handle them.
 */
public class CallsApi extends Connection.Observer {

    private static CallsApi instance;

    private final Connection connection;

    public static CallsApi getInstance() {
        if (instance == null) {
            synchronized (CallsApi.class) {
                if (instance == null) {
                    instance = new CallsApi(Connection.getInstance());
                }
            }
        }
        return instance;
    }

    private CallsApi(@NonNull Connection connection) {
        this.connection = connection;
        ConnectionObservers.getInstance().addObserver(this);
    }

    @Override
    public void onIncomingCall(@NonNull UserId peerUid, @NonNull IncomingCall incomingCall, @NonNull String ackId) {
        String callId = incomingCall.getCallId();
        // TODO(nikola): do decryption here
        String webrtcOffer = incomingCall.getWebrtcOffer().getEncPayload().toStringUtf8();
        List<StunServer> stunServers = incomingCall.getStunServersList();
        List<TurnServer> turnServers = incomingCall.getTurnServersList();
        long timestamp = incomingCall.getTimestampMs();
        CallManager.getInstance().handleIncomingCall(callId, peerUid, webrtcOffer, stunServers, turnServers, timestamp);
        connection.sendAck(ackId);
    }

    @Override
    public void onCallRinging(@NonNull UserId peerUid, @NonNull CallRinging callRinging, @NonNull String ackId) {
        String callId = callRinging.getCallId();
        long timestamp = callRinging.getTimestampMs();
        CallManager.getInstance().handleCallRinging(callId, peerUid, timestamp);
        connection.sendAck(ackId);
    }

    @Override
    public void onAnswerCall(@NonNull UserId peerUid, @NonNull AnswerCall answerCall, @NonNull String ackId) {
        String callId = answerCall.getCallId();
        // TODO(nikola): do decryption here
        String answer = answerCall.getWebrtcAnswer().getEncPayload().toStringUtf8();
        long timestamp = answerCall.getTimestampMs();
        CallManager.getInstance().handleAnswerCall(callId, peerUid, answer, timestamp);
        connection.sendAck(ackId);
    }

    @Override
    public void onEndCall(@NonNull UserId peerUid, @NonNull EndCall endCall, @NonNull String ackId) {
        String callId = endCall.getCallId();
        EndCall.Reason reason = endCall.getReason();
        long timestamp = endCall.getTimestampMs();
        CallManager.getInstance().handleEndCall(callId, peerUid, reason, timestamp);
        connection.sendAck(ackId);
    }

    @Override
    public void onIceCandidate(@NonNull UserId peerUid, @NonNull com.halloapp.proto.server.IceCandidate iceCandidate, @NonNull String ackId) {
        String callId = iceCandidate.getCallId();
        String sdpMediaId = iceCandidate.getSdpMediaId();
        int sdpMediaLineIndex = iceCandidate.getSdpMediaLineIndex();
        String sdp = iceCandidate.getSdp();
        CallManager.getInstance().handleIceCandidate(callId, peerUid, sdpMediaId, sdpMediaLineIndex, sdp);
        connection.sendAck(ackId);
    }

    public Observable<StartCallResponseIq> startCall(@NonNull String callId, @NonNull UserId peerUid, @NonNull CallType callType, @NonNull String webrtcOffer) {
        final StartCallIq requestIq = new StartCallIq(callId, peerUid, callType, webrtcOffer);
        return connection.sendRequestIq(requestIq);
    }


    public void sendAnswerCall(@NonNull String callId, @NonNull UserId peerUid, @NonNull String webrtcAnswer) {
        AnswerCallElement answerCallElement = new AnswerCallElement(callId, webrtcAnswer);
        String id = RandomId.create();
        Msg msg = Msg.newBuilder()
                .setId(id)
                .setAnswerCall(answerCallElement.toProto())
                .setToUid(peerUid.rawIdLong())
                .build();
        Log.i("send call_answer msg " + ProtoPrinter.toString(msg));
        connection.sendCallMsg(msg);
    }

    public void sendIceCandidate(@NonNull String callId, @NonNull UserId peerUid, @NonNull IceCandidate iceCandidate) {
        IceCandidateElement iceCandidateElement = new IceCandidateElement(
                callId, iceCandidate.sdpMid, iceCandidate.sdpMLineIndex, iceCandidate.sdp);
        String id = RandomId.create();
        Msg msg = Msg.newBuilder()
                .setId(id)
                .setIceCandidate(iceCandidateElement.toProto())
                .setToUid(peerUid.rawIdLong())
                .build();

        // TODO(nikola): Ask Android team about sending the messages like this. Is this ok?
        connection.sendCallMsg(msg);
    }

    public void sendEndCall(@NonNull String callId, @NonNull UserId peerUid, @NonNull EndCall.Reason reason) {
        EndCallElement endCallElement = new EndCallElement(callId, reason);
        String id = RandomId.create();
        Msg msg = Msg.newBuilder()
                .setId(id)
                .setEndCall(endCallElement.toProto())
                .setToUid(peerUid.rawIdLong())
                .build();
        Log.i("sending end_call msg " + ProtoPrinter.toString(msg));
        connection.sendCallMsg(msg);
    }

    public void sendRinging(@NonNull String callId, @NonNull UserId peerUid) {
        CallRingingElement callRingingElement = new CallRingingElement(callId);
        String id = RandomId.create();
        Msg msg = Msg.newBuilder()
                .setId(id)
                .setCallRinging(callRingingElement.toProto())
                .setToUid(peerUid.rawIdLong())
                .build();
        Log.i("sending call_ringing msg " + msg);
        connection.sendCallMsg(msg);
    }
}
