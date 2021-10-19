package com.halloapp.xmpp.calls;

import androidx.annotation.NonNull;

import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.CallType;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.StartCall;
import com.halloapp.proto.server.WebRtcSessionDescription;
import com.halloapp.xmpp.HalloIq;

import java.nio.charset.StandardCharsets;

public class StartCallIq extends HalloIq {

    private final String callId;
    private final UserId peerUid;
    private final CallType callType;
    private final String webrtcOffer;

    protected StartCallIq(@NonNull String callId, @NonNull UserId peerUid, @NonNull CallType callType, @NonNull String webrtcOffer) {
        this.callId = callId;
        this.peerUid = peerUid;
        this.callType = callType;
        this.webrtcOffer = webrtcOffer;
    }


    @Override
    public Iq toProtoIq() {
        StartCall.Builder builder = StartCall.newBuilder();
        builder.setCallType(callType);
        builder.setPeerUid(peerUid.rawIdLong());
        builder.setCallId(callId);
        WebRtcSessionDescription.Builder offerBuilder = WebRtcSessionDescription.newBuilder();
        // TODO: do the encryption here
//        byte[] bytes = webrtcOffer.getBytes(StandardCharsets.UTF_8);
//        try {
//            SignalSessionManager.getInstance().encryptMessage(bytes, peerUid);
//        } catch (CryptoException e) {
//            // TODO:
//            e.printStackTrace();
//        }
        offerBuilder.setEncPayload(com.google.protobuf.ByteString.copyFromUtf8(webrtcOffer));
        builder.setWebrtcOffer(offerBuilder.build());
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setId(getStanzaId())
                .setStartCall(builder)
                .build();
    }
}
