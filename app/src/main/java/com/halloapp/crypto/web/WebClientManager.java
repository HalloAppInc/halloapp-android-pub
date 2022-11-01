package com.halloapp.crypto.web;

import static java.lang.Long.parseLong;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.google.protobuf.ByteString;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.id.UserId;
import com.halloapp.noise.NoiseException;
import com.halloapp.noise.WebClientNoiseSocket;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.WebClientInfo;
import com.halloapp.proto.web.ConnectionInfo;
import com.halloapp.proto.web.UserDisplayInfo;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

import javax.crypto.BadPaddingException;
import javax.crypto.ShortBufferException;

public class WebClientManager {

    private static WebClientManager instance;

    private final Me me;
    private final Connection connection;

    private boolean connectedToWebClient = false;
    private WebClientNoiseSocket noiseSocket = null;

    public static WebClientManager getInstance() {
        if (instance == null) {
            synchronized (WebClientManager.class) {
                if (instance == null) {
                    instance = new WebClientManager(Me.getInstance(), Connection.getInstance());
                }
            }
        }
        return instance;
    }

    public WebClientManager(@NonNull Me me, @NonNull Connection connection) {
        this.me = me;
        this.connection = connection;
    }

    @WorkerThread
    public void connect(byte[] webClientStaticKey) {
        me.setWebClientStaticKey(webClientStaticKey);
        setKeyAsAuthenticatedToServer(webClientStaticKey);
        connectedToWebClient = true;
    }

    @WorkerThread
    public void disconnect() {
        // TODO(justin): remove saved web client key, and set current my web client key == null
    }

    public boolean isConnectedToWebClient() {
        return connectedToWebClient;
    }

    @WorkerThread
    public void connectToWebClient() {
        byte[] noiseKey = me.getMyWebClientEd25519NoiseKey();

        if (noiseKey == null) {
            noiseKey = CryptoUtils.generateEd25519KeyPair();
            me.saveMyWebClientNoiseKey(noiseKey);
        }
        noiseSocket = new WebClientNoiseSocket(me, connection);

        try {
            noiseSocket.initialize(getInitializationBytes());
        } catch (NoiseException  e) {
            Log.e("WebClientManager.connectToWebClient error", e);
        }
    }

    public void finishHandshake(byte[] msgBContents) throws NoiseException, BadPaddingException, ShortBufferException {
        noiseSocket.finishHandshake(msgBContents);
    }

    private byte[] getInitializationBytes() {
        long userId = parseLong(me.getUser());
        String avatarId = ContactsDb.getInstance().getContactAvatarInfo(UserId.ME).avatarId;
        UserDisplayInfo user = UserDisplayInfo.newBuilder().setContactName(me.getName()).setUid(userId).setAvatarId(avatarId).build();
        ConnectionInfo connectionInfo = ConnectionInfo.newBuilder().setVersion(Constants.USER_AGENT).setUser(user).build();
        return connectionInfo.toByteArray();
    }


    private void setKeyAsAuthenticatedToServer(byte[] staticKey) {
        Iq.Builder builder = Iq.newBuilder();
        builder.setType(Iq.Type.SET)
                .setWebClientInfo(WebClientInfo.newBuilder()
                        .setAction(WebClientInfo.Action.AUTHENTICATE_KEY)
                        .setStaticKey(ByteString.copyFrom(staticKey))
                        .build());

        Connection.getInstance().sendIqRequest(builder)
                .onResponse((handler) -> connectToWebClient())
                .onError((handler) -> Log.d("Error sending web client authenticated key to server"));
    }

     // currently unused function, for removing key when disconnecting
    private void removeKey(byte[] staticKey) {
        Iq.Builder builder = Iq.newBuilder();

        builder.setType(Iq.Type.SET)
                .setWebClientInfo(WebClientInfo.newBuilder()
                        .setAction(WebClientInfo.Action.REMOVE_KEY)
                        .setStaticKey(ByteString.copyFrom(staticKey)).build());
        Connection.getInstance().sendIqRequest(builder)
                .onResponse((handler) -> Log.d("Successfully removed web client authenticated key from server"))
                .onError((handler) -> Log.d("Error removing web client authenticated key from server"));
    }
}
