package com.halloapp.xmpp.calls;


import androidx.annotation.NonNull;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.CallType;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.util.Observable;


public class CallsApi {

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

    private CallsApi(Connection connection) {
        this.connection = connection;
    }

    public Observable<StartCallResponseIq> startCall(@NonNull String callId, @NonNull UserId peerUid, @NonNull CallType callType, @NonNull String webrtcOffer) {
        final StartCallIq requestIq = new StartCallIq(callId, peerUid, callType, webrtcOffer);
        final Observable<StartCallResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable;
    }
}
