package com.halloapp.crypto.web;

import static java.lang.Long.parseLong;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentDb;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.id.UserId;
import com.halloapp.noise.NoiseException;
import com.halloapp.noise.WebClientNoiseSocket;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.WebClientInfo;
import com.halloapp.proto.web.ConnectionInfo;
import com.halloapp.proto.web.UserDisplayInfo;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.ShortBufferException;

public class WebClientManager {

    private static WebClientManager instance;

    private final Me me;
    private final Connection connection;
    private final Preferences preferences;
    private final BgWorkers bgWorkers;
    private final ContentDb contentDb;
    private final ContactsDb contactsDb;

    private final Set<WebClientObserver> observers = new HashSet<>();
    private WebClientNoiseSocket noiseSocket = null;

    public static WebClientManager getInstance() {
        if (instance == null) {
            synchronized (WebClientManager.class) {
                if (instance == null) {
                    instance = new WebClientManager(Me.getInstance(), Connection.getInstance(), Preferences.getInstance(), BgWorkers.getInstance(), ContentDb.getInstance(), ContactsDb.getInstance());
                }
            }
        }
        return instance;
    }

    public WebClientManager(@NonNull Me me, @NonNull Connection connection, @NonNull Preferences preferences, @NonNull BgWorkers bgWorkers, @NonNull ContentDb contentDb, @NonNull ContactsDb contactsDb) {
        this.me = me;
        this.connection = connection;
        this.preferences = preferences;
        this.bgWorkers = bgWorkers;
        this.contentDb = contentDb;
        this.contactsDb = contactsDb;
    }

    @WorkerThread
    public void reconnect() {
        if (preferences.getIsConnectedToWebClient()) {
            try {
                if (noiseSocket == null) {
                    noiseSocket = new WebClientNoiseSocket(me, connection, contentDb, contactsDb);
                }
                noiseSocket.initialize(getConnectionInfo(), true);
            } catch (NoiseException e) {
                Log.e("WebClientManager.connectToWebClient error", e);
            }
        }
    }

    @WorkerThread
    public void connect(byte[] webClientStaticKey) {
        me.setWebClientStaticKey(webClientStaticKey);
        setKeyAsAuthenticatedToServer(webClientStaticKey);
    }

    @WorkerThread
    public void disconnect() {
        if (preferences.getIsConnectedToWebClient()) {
            removeKeyFromServer(me.getWebClientStaticKey().getKeyMaterial());
        }
        setIsConnectedToWebClient(false);
    }

    public boolean isConnectedToWebClient() {
        return preferences.getIsConnectedToWebClient();
    }

    public void setIsConnectedToWebClient(boolean connected) {
        bgWorkers.execute(() -> {
            preferences.setIsConnectedToWebClient(connected);
            notifyConnectedToWebClientChanged();
        });
    }

    @WorkerThread
    public void connectToWebClient() {
        byte[] noiseKey = me.getMyWebClientEd25519NoiseKey();

        if (noiseKey == null) {
            noiseKey = CryptoUtils.generateEd25519KeyPair();
            me.saveMyWebClientNoiseKey(noiseKey);
        }
        noiseSocket = new WebClientNoiseSocket(me, connection, contentDb, contactsDb);

        try {
            noiseSocket.initialize(getConnectionInfo(), false);
        } catch (NoiseException e) {
            setIsConnectedToWebClient(false);
            Log.e("WebClientManager.connectToWebClient error", e);
        }
    }

    public void finishHandshake(byte[] msgBContents) throws NoiseException, BadPaddingException, ShortBufferException {
        noiseSocket.finishHandshake(msgBContents);
    }

    public void receiveKKHandshake(byte[] msgAContents) throws NoiseException, BadPaddingException, ShortBufferException, NoSuchAlgorithmException, CryptoException {
        noiseSocket.receiveKKHandshake(msgAContents, getConnectionInfo());
    }

    public void handleIncomingWebContainer(@NonNull byte[] encryptedWebContainer) throws ShortBufferException , BadPaddingException , InvalidProtocolBufferException, NoiseException {
        noiseSocket.handleIncomingContainer(encryptedWebContainer);
    }

    private byte[] getConnectionInfo() {
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
                .onError((handler) -> Log.i("Error sending web client authenticated key to server"));
    }

    private void removeKeyFromServer(byte[] staticKey) {
        Iq.Builder builder = Iq.newBuilder();

        builder.setType(Iq.Type.SET)
                .setWebClientInfo(WebClientInfo.newBuilder()
                        .setAction(WebClientInfo.Action.REMOVE_KEY)
                        .setStaticKey(ByteString.copyFrom(staticKey))
                        .build());

        Connection.getInstance().sendIqRequest(builder)
                .onResponse((handler) -> Log.i("Successfully removed web client authenticated key from server"))
                .onError((handler) -> Log.e("Error removing web client authenticated key from server"));
    }

    public void addObserver(@NonNull WebClientObserver observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }

    public void removeObserver(@NonNull WebClientObserver observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }

    private void notifyConnectedToWebClientChanged() {
        synchronized (observers) {
            for (WebClientObserver observer : observers) {
                observer.onConnectedToWebClientChanged(preferences.getIsConnectedToWebClient());
            }
        }
    }

    public interface WebClientObserver {
        void onConnectedToWebClientChanged(boolean isConnected);
    }
}
