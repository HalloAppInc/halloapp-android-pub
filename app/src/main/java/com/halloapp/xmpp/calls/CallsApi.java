package com.halloapp.xmpp.calls;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.halloapp.ConnectionObservers;
import com.halloapp.calling.CallManager;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.crypto.signal.SignalSessionSetupInfo;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.AnswerCall;
import com.halloapp.proto.server.CallRinging;
import com.halloapp.proto.server.CallType;
import com.halloapp.proto.server.EndCall;
import com.halloapp.proto.server.IncomingCall;
import com.halloapp.proto.server.Msg;
import com.halloapp.proto.server.StunServer;
import com.halloapp.proto.server.TurnServer;
import com.halloapp.proto.server.WebRtcSessionDescription;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ProtoPrinter;
import com.halloapp.xmpp.util.Observable;

import org.webrtc.IceCandidate;

import java.nio.charset.StandardCharsets;
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

    public static @NonNull WebRtcSessionDescription encryptCallPayload(String sessionDescription, UserId peerUid) throws CryptoException {
        WebRtcSessionDescription.Builder builder = WebRtcSessionDescription.newBuilder();
        byte[] bytes = sessionDescription.getBytes(StandardCharsets.UTF_8);
        try {
            Log.i("CallsApi: peerUid " + peerUid);
            SignalSessionSetupInfo signalSessionSetupInfo = SignalSessionManager.getInstance().getSessionSetupInfo(peerUid);
            byte[] encBytes = SignalSessionManager.getInstance().encryptMessage(bytes, peerUid);
            builder.setEncPayload(ByteString.copyFrom(encBytes));
            if (signalSessionSetupInfo != null) {
                builder.setPublicKey(ByteString.copyFrom(signalSessionSetupInfo.identityKey.getKeyMaterial()));
                if (signalSessionSetupInfo.oneTimePreKeyId != null) {
                    builder.setOneTimePreKeyId(signalSessionSetupInfo.oneTimePreKeyId);
                }
            }
            return builder.build();
        } catch (CryptoException e) {
            Log.e("CallsApi: failed to encrypt call session description", e);
            throw e;
        } catch (Exception e) {
            Log.e("CallsApi: failed to encrypt call session description", e);
            throw new CryptoException(e.getMessage(), e.getCause());
        }
    }


    public static @NonNull String decryptCallPayload(@NonNull WebRtcSessionDescription sessionDescription, @NonNull UserId peerUid) throws CryptoException {
        byte[] identityKeyBytes = sessionDescription.getPublicKey().toByteArray();
        PublicEdECKey identityKey = identityKeyBytes == null || identityKeyBytes.length == 0 ? null : new PublicEdECKey(identityKeyBytes);

        SignalSessionSetupInfo signalSessionSetupInfo = new SignalSessionSetupInfo(identityKey, sessionDescription.getOneTimePreKeyId());
        byte[] encryptedBytes = sessionDescription.getEncPayload().toByteArray();
        final byte[] dec = SignalSessionManager.getInstance().decryptMessage(encryptedBytes, peerUid, signalSessionSetupInfo);
        return new String(dec, StandardCharsets.UTF_8);
    }


    private CallsApi(@NonNull Connection connection) {
        this.connection = connection;
        ConnectionObservers.getInstance().addObserver(this);
    }

    @Override
    public void onIncomingCall(@NonNull UserId peerUid, @NonNull IncomingCall incomingCall, @NonNull String ackId) {
        String callId = incomingCall.getCallId();

        @Nullable String webrtcOffer = null;
        try {
            webrtcOffer = CallsApi.decryptCallPayload(incomingCall.getWebrtcOffer(), peerUid);
            Log.i("CallsApi: Decrypted offer: " + webrtcOffer);
        } catch (CryptoException e) {
            Log.e("CallsApi: Decryption error onIncomingCall", e);
        }
        // String webrtcOffer = incomingCall.getWebrtcOffer().getEncPayload().toStringUtf8();
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
        String answer = null;
        try {
            answer = CallsApi.decryptCallPayload(answerCall.getWebrtcAnswer(), peerUid);
            Log.i("CallsApi: Decrypted answer:" + answer);
        } catch (CryptoException e) {
            Log.e("CallsApi: Decryption error onAnswerCall", e);
        }
        // String answer = answerCall.getWebrtcAnswer().getEncPayload().toStringUtf8();
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

    public @Nullable Observable<StartCallResponseIq> startCall(@NonNull String callId, @NonNull UserId peerUid, @NonNull CallType callType, @NonNull String webrtcOfferString) throws CryptoException {
        WebRtcSessionDescription webrtcOffer = encryptCallPayload(webrtcOfferString, peerUid);
        Log.i("CallsApi: encrypted offer: " + webrtcOffer.getEncPayload().size());
        final StartCallIq requestIq = new StartCallIq(callId, peerUid, callType, webrtcOffer);
        return connection.sendRequestIq(requestIq);
    }


    public void sendAnswerCall(@NonNull String callId, @NonNull UserId peerUid, @NonNull WebRtcSessionDescription webrtcAnswer) {
        AnswerCallElement answerCallElement = new AnswerCallElement(callId, webrtcAnswer);
        String id = RandomId.create();
        Msg msg = Msg.newBuilder()
                .setId(id)
                .setAnswerCall(answerCallElement.toProto())
                .setToUid(peerUid.rawIdLong())
                .build();
        Log.i("CallsApi: send call_answer msg " + ProtoPrinter.toString(msg));
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
        Log.i("CallsApi: sending end_call msg " + ProtoPrinter.toString(msg));
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
        Log.i("CallsApi: sending call_ringing msg " + msg);
        connection.sendCallMsg(msg);
    }
}
