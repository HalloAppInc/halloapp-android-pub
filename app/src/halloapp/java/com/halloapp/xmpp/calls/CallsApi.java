package com.halloapp.xmpp.calls;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.halloapp.ConnectionObservers;
import com.halloapp.calling.calling.CallManager;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.crypto.signal.SignalSessionSetupInfo;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.AnswerCall;
import com.halloapp.proto.server.CallConfig;
import com.halloapp.proto.server.CallRinging;
import com.halloapp.proto.server.CallType;
import com.halloapp.proto.server.EndCall;
import com.halloapp.proto.server.HoldCall;
import com.halloapp.proto.server.IceRestartAnswer;
import com.halloapp.proto.server.IceRestartOffer;
import com.halloapp.proto.server.IncomingCall;
import com.halloapp.proto.server.Msg;
import com.halloapp.proto.server.MuteCall;
import com.halloapp.proto.server.Rerequest;
import com.halloapp.proto.server.StunServer;
import com.halloapp.proto.server.TurnServer;
import com.halloapp.proto.server.WebRtcSessionDescription;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ProtoPrinter;
import com.halloapp.xmpp.RerequestElement;
import com.halloapp.xmpp.util.Observable;

import org.webrtc.IceCandidate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * Implements API to send Calls related messages and IQs as well as handles parsing incoming
 * call messages and calls the CallManager to handle them.
 */
public class CallsApi extends Connection.Observer {

    private static final long CALL_MSG_TIMEOUT_MS = 60_000;

    private final Executor callsApiExecutor = Executors.newSingleThreadExecutor();
    
    private final Connection connection;
    private final CallManager callManager;

    public CallsApi(@NonNull CallManager callManager, @NonNull Connection connection) {
        this.callManager = callManager;
        this.connection = connection;
    }

    public void init() {
        ConnectionObservers.getInstance().addObserver(this);
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

    @Override
    public void onIncomingCall(@NonNull UserId peerUid, @NonNull IncomingCall incomingCall, @NonNull String ackId) {
        callsApiExecutor.execute(() -> {
            String callId = incomingCall.getCallId();

            String webrtcOffer = null;
            CryptoException cryptoException = null;
            try {
                webrtcOffer = CallsApi.decryptCallPayload(incomingCall.getWebrtcOffer(), peerUid);
                Log.d("CallsApi: Decrypted offer");
            } catch (CryptoException e) {
                Log.e("CallsApi: Decryption error onIncomingCall", e);
                cryptoException = e;
            }
            // String webrtcOffer = incomingCall.getWebrtcOffer().getEncPayload().toStringUtf8();
            List<StunServer> stunServers = incomingCall.getStunServersList();
            List<TurnServer> turnServers = incomingCall.getTurnServersList();
            CallConfig callConfig = incomingCall.getCallConfig();
            CallType callType = incomingCall.getCallType();
            long timestamp = incomingCall.getTimestampMs();
            long serverSentTimestamp = incomingCall.getServerSentTsMs();
            callManager.handleIncomingCall(callId, peerUid, callType, webrtcOffer, stunServers, turnServers, callConfig, timestamp, serverSentTimestamp, cryptoException);
            connection.sendAck(ackId);
        });
    }

    @Override
    public void onCallRinging(@NonNull UserId peerUid, @NonNull CallRinging callRinging, @NonNull String ackId) {
        callsApiExecutor.execute(() -> {
            String callId = callRinging.getCallId();
            long timestamp = callRinging.getTimestampMs();
            callManager.handleCallRinging(callId, peerUid, timestamp);
            connection.sendAck(ackId);
        });
    }

    @Override
    public void onAnswerCall(@NonNull UserId peerUid, @NonNull AnswerCall answerCall, @NonNull String ackId) {
        callsApiExecutor.execute(() -> {
            String callId = answerCall.getCallId();
            String answer = null;
            CryptoException cryptoException = null;
            try {
                answer = CallsApi.decryptCallPayload(answerCall.getWebrtcAnswer(), peerUid);
                Log.i("CallsApi: Decrypted answer");
            } catch (CryptoException e) {
                Log.e("CallsApi: Decryption error onAnswerCall", e);
                cryptoException = e;
            }
            // String answer = answerCall.getWebrtcAnswer().getEncPayload().toStringUtf8();
            long timestamp = answerCall.getTimestampMs();
            callManager.handleAnswerCall(callId, peerUid, answer, timestamp, cryptoException);
            connection.sendAck(ackId);
        });
    }

    @Override
    public void onEndCall(@NonNull UserId peerUid, @NonNull EndCall endCall, @NonNull String ackId) {
        callsApiExecutor.execute(() -> {
            String callId = endCall.getCallId();
            EndCall.Reason reason = endCall.getReason();
            long timestamp = endCall.getTimestampMs();
            callManager.handleEndCall(callId, peerUid, reason, timestamp);
            connection.sendAck(ackId);
        });
    }

    @Override
    public void onIceCandidate(@NonNull UserId peerUid, @NonNull com.halloapp.proto.server.IceCandidate iceCandidate, @NonNull String ackId) {
        callsApiExecutor.execute(() -> {
            String callId = iceCandidate.getCallId();
            String sdpMediaId = iceCandidate.getSdpMediaId();
            int sdpMediaLineIndex = iceCandidate.getSdpMediaLineIndex();
            String sdp = iceCandidate.getSdp();
            callManager.handleIceCandidate(callId, peerUid, sdpMediaId, sdpMediaLineIndex, sdp);
            connection.sendAck(ackId);
        });
    }

    @Override
    public void onIceRestartOffer(@NonNull UserId peerUid, @NonNull IceRestartOffer iceRestartOffer, @NonNull String ackId) {
        callsApiExecutor.execute(() -> {
            String callId = iceRestartOffer.getCallId();
            int restartIndex = iceRestartOffer.getIdx();

            String webrtcOffer = null;
            CryptoException cryptoException = null;
            try {
                webrtcOffer = CallsApi.decryptCallPayload(iceRestartOffer.getWebrtcOffer(), peerUid);
                Log.i("CallsApi: Decrypted iceRestartOffer: " + webrtcOffer);
            } catch (CryptoException e) {
                Log.e("CallsApi: Decryption error onIceRestartOffer", e);
                cryptoException = e;
            }

            callManager.handleIceRestartOffer(callId, restartIndex, webrtcOffer, cryptoException);
            connection.sendAck(ackId);
        });
    }

    @Override
    public void onIceRestartAnswer(@NonNull UserId peerUid, @NonNull IceRestartAnswer iceRestartAnswer, @NonNull String ackId) {
        callsApiExecutor.execute(() -> {
            String callId = iceRestartAnswer.getCallId();
            int restartIndex = iceRestartAnswer.getIdx();

            String webrtcAnswer = null;
            CryptoException cryptoException = null;
            try {
                webrtcAnswer = CallsApi.decryptCallPayload(iceRestartAnswer.getWebrtcAnswer(), peerUid);
                Log.i("CallsApi: Decrypted iceRestartAnswer: " + webrtcAnswer);
            } catch (CryptoException e) {
                Log.e("CallsApi: Decryption error onIceRestartAnswer", e);
                cryptoException = e;
            }

            callManager.handleIceRestartAnswer(callId, restartIndex, webrtcAnswer, cryptoException);
            connection.sendAck(ackId);
        });
    }

    @Override
    public void onHoldCall(@NonNull UserId peerUid, @NonNull HoldCall holdCall, @NonNull String ackId) {
        callsApiExecutor.execute(() -> {
            String callId = holdCall.getCallId();
            boolean hold = holdCall.getHold();
            callManager.handleHoldCall(callId, peerUid, hold);
            connection.sendAck(ackId);
        });
    }

    @Override
    public void onMuteCall(@NonNull UserId peerUid, @NonNull MuteCall muteCall, @NonNull String ackId) {
        callsApiExecutor.execute(() -> {
            callManager.handleMuteCall(peerUid, muteCall);
            connection.sendAck(ackId);
        });
    }

    public Observable<StartCallResponseIq> startCall(@NonNull String callId, @NonNull UserId peerUid, @NonNull CallType callType, @NonNull String webrtcOfferString) throws CryptoException {
        WebRtcSessionDescription webrtcOffer = encryptCallPayload(webrtcOfferString, peerUid);
        Log.i("CallsApi: encrypted offer: " + webrtcOffer.getEncPayload().size());
        final StartCallIq requestIq = new StartCallIq(callId, peerUid, callType, webrtcOffer);
        return connection.sendRequestIq(requestIq, true);
    }

    public Observable<GetCallServersResponseIq> getCallServers(@NonNull String callId, @NonNull UserId peerUid, @NonNull CallType callType) {
        final GetCallServersIq requestIq = new GetCallServersIq(callId, peerUid, callType);
        return connection.sendRequestIq(requestIq, true);
    }


    public void sendAnswerCall(@NonNull String callId, @NonNull UserId peerUid, @NonNull WebRtcSessionDescription webrtcAnswer) {
        AnswerCallElement answerCallElement = new AnswerCallElement(callId, webrtcAnswer);
        String id = RandomId.create();
        Msg msg = Msg.newBuilder()
                .setId(id)
                .setType(Msg.Type.CALL)
                .setAnswerCall(answerCallElement.toProto())
                .setToUid(peerUid.rawIdLong())
                .build();
        Log.d("CallsApi: send call_answer msg " + ProtoPrinter.toString(msg));
        sendCallMsg(msg);
    }

    public void sendIceCandidate(@NonNull String callId, @NonNull UserId peerUid, @NonNull IceCandidate iceCandidate) {
        IceCandidateElement iceCandidateElement = new IceCandidateElement(
                callId, iceCandidate.sdpMid, iceCandidate.sdpMLineIndex, iceCandidate.sdp);
        String id = RandomId.create();
        Msg msg = Msg.newBuilder()
                .setId(id)
                .setType(Msg.Type.CALL)
                .setIceCandidate(iceCandidateElement.toProto())
                .setToUid(peerUid.rawIdLong())
                .build();

        sendCallMsg(msg);
    }

    public void sendEndCall(@NonNull String callId, @NonNull UserId peerUid, @NonNull EndCall.Reason reason) {
        EndCallElement endCallElement = new EndCallElement(callId, reason);
        String id = RandomId.create();
        Msg msg = Msg.newBuilder()
                .setId(id)
                .setType(Msg.Type.CALL)
                .setEndCall(endCallElement.toProto())
                .setToUid(peerUid.rawIdLong())
                .build();
        Log.d("CallsApi: sending end_call msg " + ProtoPrinter.toString(msg));
        sendCallMsg(msg);
    }

    public void sendRinging(@NonNull String callId, @NonNull UserId peerUid) {
        CallRingingElement callRingingElement = new CallRingingElement(callId);
        String id = RandomId.create();
        Msg msg = Msg.newBuilder()
                .setId(id)
                .setType(Msg.Type.CALL)
                .setCallRinging(callRingingElement.toProto())
                .setToUid(peerUid.rawIdLong())
                .build();
        Log.d("CallsApi: sending call_ringing msg " + ProtoPrinter.toString(msg));
        sendCallMsg(msg);
    }

    public void sendIceRestartOffer(@NonNull String callId, @NonNull UserId peerUid, int restartIndex, @NonNull String webrtcOfferString) throws CryptoException {
        WebRtcSessionDescription webrtcOffer = encryptCallPayload(webrtcOfferString, peerUid);
        Log.i("CallsApi: encrypted restart offer: " + webrtcOffer.getEncPayload().size());

        IceRestartOfferElement iceRestartOfferElement = new IceRestartOfferElement(callId, restartIndex, webrtcOffer);

        String id = RandomId.create();
        Msg msg = Msg.newBuilder()
                .setId(id)
                .setType(Msg.Type.CALL)
                .setIceRestartOffer(iceRestartOfferElement.toProto())
                .setToUid(peerUid.rawIdLong())
                .build();

        sendCallMsg(msg);
    }

    public void sendIceRestartAnswer(@NonNull String callId, @NonNull UserId peerUid, int restartIndex, @NonNull String webrtcAnswerString) throws CryptoException {
        WebRtcSessionDescription webrtcAnswer = encryptCallPayload(webrtcAnswerString, peerUid);
        Log.i("CallsApi: encrypted restart answer: " + webrtcAnswer.getEncPayload().size());

        IceRestartAnswerElement iceRestartAnswerElement = new IceRestartAnswerElement(callId, restartIndex, webrtcAnswer);

        String id = RandomId.create();
        Msg msg = Msg.newBuilder()
                .setId(id)
                .setType(Msg.Type.CALL)
                .setIceRestartAnswer(iceRestartAnswerElement.toProto())
                .setToUid(peerUid.rawIdLong())
                .build();

        sendCallMsg(msg);
    }

    public void sendCallRerequest(@NonNull String callId, @NonNull UserId peerUid, int rerequestCount, @Nullable byte[] teardownKey) {
        RerequestElement rerequestElement = new RerequestElement(callId,  peerUid, rerequestCount, teardownKey, Rerequest.ContentType.CALL);
        sendCallMsg(rerequestElement.toProto());
    }

    public void sendMuteCall(@NonNull String callId, @NonNull UserId peerUid, @NonNull MuteCall.MediaType mediaType, boolean mute) {
        MuteCall.Builder builder = MuteCall.newBuilder()
                .setCallId(callId)
                .setMediaType(mediaType)
                .setMuted(mute);

        String id = RandomId.create();
        Msg msg = Msg.newBuilder()
                .setId(id)
                .setType(Msg.Type.CALL)
                .setMuteCall(builder)
                .setToUid(peerUid.rawIdLong())
                .build();
        sendCallMsg(msg);
    }

    public void sendHoldCall(@NonNull String callId, @NonNull UserId peerUid, boolean hold) {
        HoldCall.Builder builder = HoldCall.newBuilder()
                .setCallId(callId)
                .setHold(hold);

        String id = RandomId.create();
        Msg msg = Msg.newBuilder()
                .setId(id)
                .setType(Msg.Type.CALL)
                .setHoldCall(builder)
                .setToUid(peerUid.rawIdLong())
                .build();
        sendCallMsg(msg);
    }

    private void sendCallMsg(@NonNull Msg msg) {
        Log.i("CallsApi: sending " + ProtoPrinter.toString(msg));
        connection.sendMsg(msg, null, CALL_MSG_TIMEOUT_MS, true);
    }
}
