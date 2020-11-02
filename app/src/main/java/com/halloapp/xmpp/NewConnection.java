package com.halloapp.xmpp;

import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.android.gms.common.util.Hex;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.BuildConfig;
import com.halloapp.ConnectionObservers;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.content.Comment;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.crypto.SessionSetupInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.Ack;
import com.halloapp.proto.server.AuthRequest;
import com.halloapp.proto.server.AuthResult;
import com.halloapp.proto.server.Avatar;
import com.halloapp.proto.server.ChatStanza;
import com.halloapp.proto.server.ChatState;
import com.halloapp.proto.server.ClientMode;
import com.halloapp.proto.server.ClientVersion;
import com.halloapp.proto.server.Contact;
import com.halloapp.proto.server.ContactHash;
import com.halloapp.proto.server.ContactList;
import com.halloapp.proto.server.DeliveryReceipt;
import com.halloapp.proto.server.ErrorStanza;
import com.halloapp.proto.server.FeedItems;
import com.halloapp.proto.server.GroupChat;
import com.halloapp.proto.server.GroupFeedItem;
import com.halloapp.proto.server.GroupMember;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.HaError;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.Msg;
import com.halloapp.proto.server.Packet;
import com.halloapp.proto.server.Ping;
import com.halloapp.proto.server.Presence;
import com.halloapp.proto.server.SeenReceipt;
import com.halloapp.proto.server.SilentChatStanza;
import com.halloapp.proto.server.WhisperKeys;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.ThreadUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Stats;
import com.halloapp.xmpp.feed.FeedItem;
import com.halloapp.xmpp.feed.FeedUpdateIq;
import com.halloapp.xmpp.feed.GroupFeedUpdateIq;
import com.halloapp.xmpp.feed.SharePosts;
import com.halloapp.xmpp.groups.GroupChatMessage;
import com.halloapp.xmpp.groups.GroupResponseIq;
import com.halloapp.xmpp.groups.MemberElement;
import com.halloapp.xmpp.props.ServerPropsRequestIq;
import com.halloapp.xmpp.props.ServerPropsResponseIq;
import com.halloapp.xmpp.util.BackgroundObservable;
import com.halloapp.xmpp.util.ExceptionHandler;
import com.halloapp.xmpp.util.Observable;
import com.halloapp.xmpp.util.ObservableErrorException;
import com.halloapp.xmpp.util.ResponseHandler;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.util.Async;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import static com.halloapp.xmpp.OldConnection.FEED_THREAD_ID;

public class NewConnection extends Connection {

    private static final String HOST = "s.halloapp.net";
    private static final String DEBUG_HOST = "s-test.halloapp.net";
    private static final int PORT = 5210;
    private static final int CONNECTION_TIMEOUT = 20_000;
    private static final int REPLY_TIMEOUT = 20_000;

    public static final String XMPP_DOMAIN = "s.halloapp.net";
    private static final Jid SERVER_JID = JidCreate.bareFromOrThrowUnchecked(HOST); // TODO(jack): remove after switch

    private Me me;
    private BgWorkers bgWorkers;
    private Preferences preferences;
    private ConnectionObservers connectionObservers;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<String, Runnable> ackHandlers = new ConcurrentHashMap<>();
    public boolean clientExpired = false;
    private String connectionPropHash;
    private SSLSocket sslSocket = null;
    private OutputStream outputStream;
    public InputStream inputStream;
    private boolean isAuthenticated;
    private AuthResult authResult;

    private final Object startupShutdownLock = new Object();
    private PacketWriter packetWriter = new PacketWriter();
    private PacketReader packetReader = new PacketReader();
    private IqRouter iqRouter = new IqRouter();

    NewConnection(
            @NonNull Me me,
            @NonNull BgWorkers bgWorkers,
            @NonNull Preferences preferences,
            @NonNull ConnectionObservers connectionObservers) {
        this.me = me;
        this.bgWorkers = bgWorkers;
        this.preferences = preferences;
        this.connectionObservers = connectionObservers;
    }

    @Override
    public void connect() {
        executor.execute(this::connectInBackground);
    }

    @WorkerThread
    private void connectInBackground() {
        ThreadUtils.setSocketTag();
        if (me == null) {
            Log.i("connection: me is null");
            return;
        }
        if (!me.isRegistered()) {
            Log.i("connection: not registered");
            return;
        }
        if (isConnected() && isAuthenticated()) {
            Log.i("connection: already connected");
            return;
        }
        if (clientExpired) {
            Log.i("connection: expired client");
            return;
        }
        if (!ackHandlers.isEmpty()) {
            Log.i("connection: " + ackHandlers.size() + " ack handlers cleared");
        }
        ackHandlers.clear();

        Log.i("connection: connecting...");

        final String host = DEBUG_HOST; //preferences.getUseDebugHost() ? DEBUG_HOST : HOST; // TODO(jack)

        try {
            final InetAddress address = InetAddress.getByName(host);
            HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(address, PORT);
            sslSocket.setEnabledProtocols(new String[]{"TLSv1.1", "TLSv1.2"});

            SSLSession session = sslSocket.getSession();
            if (!hostnameVerifier.verify(host, session)) {
                // TODO(jack)
//                throw new SSLPeerUnverifiedException("Could not verify hostname " + host);
            }

            Log.i("connection: Established " + session.getProtocol() + " connection with " + session.getPeerHost() + " using " + session.getCipherSuite());

            outputStream = sslSocket.getOutputStream();
            inputStream = sslSocket.getInputStream();

            synchronized (startupShutdownLock) {
                packetWriter.init();
                packetReader.init();
                iqRouter.reset();
            }

            ClientVersion clientVersion = ClientVersion.newBuilder()
                    .setVersion(Constants.USER_AGENT)
                    .build();
            ClientMode clientMode = ClientMode.newBuilder()
                    .setMode(ClientMode.Mode.ACTIVE)
                    .build();

            AuthRequest authRequest = AuthRequest.newBuilder()
                    .setUid(Long.parseLong(me.getUser()))
                    .setPwd(me.getPassword())
                    .setClientVersion(clientVersion)
                    .setClientMode(clientMode)
                    .setResource("android")
                    .build();

            // TODO(jack): check client expiration

            final AuthResult authResult = sendAndRecvAuth(authRequest);

            Log.i("connection: auth result: " + ProtoPrinter.toString(authResult));
            final String result = authResult.getResult();
            if ("invalid client version".equals(result)) {
                clientExpired();
                connectionObservers.notifyClientVersionExpired();
                return;
            } else if (!"success".equals(result)) {
                Log.e("connection: failed to login");
                disconnectInBackground();
                connectionObservers.notifyLoginFailed();
                return;
            }
            connectionObservers.notifyConnected();
            isAuthenticated = true;
        } catch (IOException e) {
            Log.e("connection: cannot create connection", e);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @WorkerThread
    private boolean reconnectIfNeeded() {
        if (sslSocket != null && isConnected() && isAuthenticated()) {
            return true;
        }
        if (me == null) {
            Log.e("connection: cannot reconnect, me is null");
            return false;
        }
        connectInBackground();
        return sslSocket != null && isConnected() && isAuthenticated();
    }

    public AuthResult sendAndRecvAuth(AuthRequest authRequest) throws IOException {
        byte[] size = ByteBuffer.allocate(4).putInt(authRequest.getSerializedSize()).array();
        byte[] packet = authRequest.toByteArray();

        byte[] rawBytes = new byte[size.length + packet.length];
        System.arraycopy(size, 0, rawBytes, 0, size.length);
        System.arraycopy(packet, 0, rawBytes, size.length, packet.length);
        packetWriter.sendRawBytes(rawBytes);
        long startTime = System.currentTimeMillis();
        while (authResult == null) { // TODO(jack): await/notify
            if (System.currentTimeMillis() - startTime > 5000) {
                throw new IOException();
            }
        }
        AuthResult tmp = authResult;
        authResult = null;
        return tmp;
    }

    public void sendPacket(Packet packet) {
        packetWriter.sendPacket(packet);
    }

    public boolean isConnected() {
        return sslSocket != null && sslSocket.isConnected() && !sslSocket.isClosed();
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }






    @Nullable
    @Override
    public String getConnectionPropHash() {
        return connectionPropHash;
    }

    @Override
    public void clientExpired() {
        clientExpired = true;
        disconnect();
    }

    public void disconnect() {
        shutdownComponents();
        executor.execute(this::disconnectInBackground);
    }


    @WorkerThread
    private void disconnectInBackground() {
        if (sslSocket == null) {
            Log.e("connection: cannot disconnect, no connection");
            return;
        }
        if (isConnected()) {
            Log.i("connection: disconnecting");
//            connection.setReplyTimeout(1_000);
            try {
                sslSocket.close();
            } catch (IOException e) {
                Log.w("Failed to close socket", e);
            }
        }
        sslSocket = null;

        connectionObservers.notifyDisconnected();
    }

    @WorkerThread
    private void shutdownComponents() {
        Log.i("Shutting down packet handlers");
        synchronized (startupShutdownLock) {
            packetWriter.shutdown();
            packetReader.shutdown();
            iqRouter.reset();
        }
    }

    @Override
    public void requestServerProps() {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: request server props: no connection");
                return;
            }
            ServerPropsRequestIq requestIq = new ServerPropsRequestIq();
            requestIq.setTo(SERVER_JID);
            try {
                Observable<ServerPropsResponseIq> observable = sendIqRequestAsync(requestIq).map(response -> ServerPropsResponseIq.fromProto(response.getProps()));
                ServerPropsResponseIq responseIq = observable.await();
                connectionObservers.notifyServerPropsReceived(responseIq.getProps(), responseIq.getHash());
            } catch (ObservableErrorException | InterruptedException e) {
                Log.e("connection: failed to get server props", e);
            }
        });
    }

    @Override
    public Future<Integer> requestSecondsToExpiration() {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: request seconds to expiration: no connection");
                return null;
            }
            final SecondsToExpirationIq secondsToExpirationIq = new SecondsToExpirationIq(SERVER_JID);
            try {
                final SecondsToExpirationIq iqResponse = sendIqRequestAsync(secondsToExpirationIq).map(response -> SecondsToExpirationIq.fromProto(response.getClientVersion())).await();
                return iqResponse.secondsLeft;
            } catch (ObservableErrorException | InterruptedException e) {
                Log.e("connection: request seconds to expiration", e);
            }
            return null;
        });
    }

    @Override
    public Future<MediaUploadIq.Urls> requestMediaUpload(long fileSize) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: request media upload: no connection");
                return null;
            }
            final MediaUploadIq mediaUploadIq = new MediaUploadIq(SERVER_JID, fileSize);
            try {
                final MediaUploadIq responseIq = sendIqRequestAsync(mediaUploadIq).map(response -> MediaUploadIq.fromProto(response.getUploadMedia())).await();
                return responseIq.urls;
            } catch (ObservableErrorException | InterruptedException e) {
                Log.e("connection: request media upload", e);
            }
            return null;
        });
    }

    @Override
    public Future<List<ContactInfo>> syncContacts(@Nullable Collection<String> addPhones, @Nullable Collection<String> deletePhones, boolean fullSync, @Nullable String syncId, int index, boolean lastBatch) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: sync contacts: no connection");
                return null;
            }
            final ContactsSyncRequestIq contactsSyncIq = new ContactsSyncRequestIq(SERVER_JID,
                    addPhones, deletePhones, fullSync, syncId, index, lastBatch);
            try {
                final Iq response = iqRouter.sendSync(contactsSyncIq.toProtoIq());
                List<Contact> contacts = response.getContactList().getContactsList();
                List<ContactInfo> ret = new ArrayList<>();
                for (Contact contact : contacts) {
                    ret.add(new ContactInfo(contact));
                }
                return ret;
            } catch (ExecutionException e) {
                Log.e("connection: cannot sync contacts", e);
            }
            return null;
        });
    }

    @Override
    public void sendPushToken(@NonNull String pushToken) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: send push token: no connection");
                return;
            }
            final PushRegisterRequestIq pushIq = new PushRegisterRequestIq(SERVER_JID, pushToken);
            try {
                final Iq response = sendIqRequestAsync(pushIq).await();
                Log.d("connection: response after setting the push token " + ProtoPrinter.toString(response));
            } catch (ObservableErrorException | InterruptedException e) {
                Log.e("connection: cannot send push token", e);
            }
        });
    }

    @Override
    public Future<Boolean> sendName(@NonNull String name) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: send name: no connection");
                return Boolean.FALSE;
            }
            final UserNameIq nameIq = new UserNameIq(SERVER_JID, name);
            try {
                final Iq response = sendIqRequestAsync(nameIq).await();
                Log.d("connection: response after setting name " + ProtoPrinter.toString(response));
                return Boolean.TRUE;
            } catch (ObservableErrorException | InterruptedException e) {
                Log.e("connection: cannot send name", e);
                return Boolean.FALSE;
            }
        });
    }

    @Override
    public void subscribePresence(UserId userId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: request presence subscription: no connection");
                return;
            }
            PresenceStanza stanza = new PresenceStanza(userIdToJid(userId), "subscribe");
            sendPacket(Packet.newBuilder().setPresence(stanza.toProto()).build());
        });
    }

    @Override
    public void updatePresence(boolean available) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: update presence: no connection");
                return;
            }
            // TODO(jack): Uniquely-generated IDs without using Smack
            PresenceStanza stanza = new PresenceStanza(SERVER_JID, available ? "available" : "away");
            Packet packet = Packet.newBuilder().setPresence(stanza.toProto()).build();
            sendPacket(packet);
        });
    }

    @Override
    public void updateChatState(@NonNull ChatId chat, int state) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: update chat state: no connection");
                return;
            }
//            try {
                ChatStateStanza stanza = new ChatStateStanza(SERVER_JID, state == com.halloapp.xmpp.ChatState.Type.TYPING ? "typing" : "available", chat);
                sendPacket(Packet.newBuilder().setChatState(stanza.toProto()).build());
//            } catch (InterruptedException | SmackException.NotConnectedException e) {
//                Log.e("Failed to update chat state", e);
//            }
        });
    }

    @Override
    public Future<Boolean> uploadKeys(@Nullable byte[] identityKey, @Nullable byte[] signedPreKey, @NonNull List<byte[]> oneTimePreKeys) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: upload keys: no connection");
                return Boolean.FALSE;
            }
            final WhisperKeysUploadIq uploadIq = new WhisperKeysUploadIq(SERVER_JID, identityKey, signedPreKey, oneTimePreKeys);
            try {
                final Iq response = sendIqRequestAsync(uploadIq).await();
                Log.d("connection: response after uploading keys " + ProtoPrinter.toString(response));
                return Boolean.TRUE;
            } catch (InterruptedException e) {
                Log.e("connection: cannot upload keys", e);
                return Boolean.FALSE;
            }
        });
    }

    @Override
    public void uploadMoreOneTimePreKeys(@NonNull List<byte[]> oneTimePreKeys) {
        uploadKeys(null, null, oneTimePreKeys);
    }

    @Override
    public Future<WhisperKeysResponseIq> downloadKeys(@NonNull UserId userId) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: download keys: no connection");
                return null;
            }
            final WhisperKeysDownloadIq downloadIq = new WhisperKeysDownloadIq(SERVER_JID, userId.rawId(), userId);
            try {
                Observable<WhisperKeysResponseIq> observable = sendIqRequestAsync(downloadIq).map(response -> WhisperKeysResponseIq.fromProto(response.getWhisperKeys()));
                final WhisperKeysResponseIq response = observable.await();
                Log.d("connection: response after downloading keys " + response.toString());
                return response;
            } catch (InterruptedException e) {
                Log.e("connection: cannot download keys", e);
            }
            return null;
        });
    }

    @Override
    public Future<Integer> getOneTimeKeyCount() {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: get one time key count: no connection");
                return null;
            }
            final WhisperKeysCountIq countIq = new WhisperKeysCountIq(SERVER_JID);
            try {
                final WhisperKeysResponseIq response = sendIqRequestAsync(countIq).map(res -> WhisperKeysResponseIq.fromProto(res.getWhisperKeys())).await();
                Log.d("connection: response for get key count  " + response.toString());
                return response.count;
            } catch (ObservableErrorException | InterruptedException e) {
                Log.e("connection: cannot get one time key count", e);
            }
            return null;
        });
    }

    @Override
    public Future<Void> sendStats(List<Stats.Counter> counters) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: send stats: no connection");
                return null;
            }
            final StatsIq statsIq = new StatsIq(SERVER_JID, counters);
            try {
                final Iq response = sendIqRequestAsync(statsIq).await();
                Log.d("connection: response for send stats  " + ProtoPrinter.toString(response));
                return null;
            } catch (ObservableErrorException | InterruptedException e) {
                Log.e("connection: cannot send stats", e);
            }
            return null;
        });
    }

    @Override
    public Future<String> setAvatar(String base64, long numBytes, int width, int height) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: cannot update avatar, no connection");
                return null;
            }
            try {
                final AvatarIq avatarIq = new AvatarIq(SERVER_JID, base64, numBytes, height, width);
                final AvatarIq response = sendIqRequestAsync(avatarIq).map(res -> AvatarIq.fromProto(res.getAvatar())).await();
                return response.avatarId;
            } catch (ObservableErrorException | InterruptedException e) {
                Log.w("connection: cannot update avatar", e);
            }
            return null;
        });
    }

    @Override
    public Future<String> setGroupAvatar(GroupId groupId, String base64) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: cannot set group avatar, no connection");
                return null;
            }
            try {
                final GroupAvatarIq avatarIq = new GroupAvatarIq(SERVER_JID, groupId, base64);
                final GroupResponseIq response = sendIqRequestAsync(avatarIq).map(res -> GroupResponseIq.fromProto(res.getGroupStanza())).await();
                return response.avatar;
            } catch (ObservableErrorException | InterruptedException e) {
                Log.w("connection: cannot update avatar", e);
            }
            return null;
        });
    }

    @Override
    public Future<String> getAvatarId(UserId userId) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: cannot update avatar, no connection");
                return null;
            }
            try {
                final AvatarIq setAvatarIq = new AvatarIq(SERVER_JID, userId);
                final AvatarIq response = sendIqRequestAsync(setAvatarIq).map(res -> AvatarIq.fromProto(res.getAvatar())).await();
                return response.avatarId;
            } catch (ObservableErrorException | InterruptedException e) {
                Log.w("connection: cannot update avatar", e);
            }
            return null;
        });
    }

    @Override
    public Future<String> getMyAvatarId() {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: cannot get my avatar, no connection");
                return null;
            }
            try {
                final AvatarIq getAvatarIq = new AvatarIq(SERVER_JID, new UserId(me.getUser()));
                final Iq response = iqRouter.sendSync(getAvatarIq.toProtoIq());
                return response.getAvatar().getId();
            } catch (ExecutionException e) {
                Log.w("connection: cannot get my avatar", e);
            }
            return null;
        });
    }

    @Override
    public Future<Boolean> sharePosts(Map<UserId, Collection<Post>> shareMap) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: cannot share posts, no connection");
                return false;
            }
            try {
                List<SharePosts> sharePosts = new ArrayList<>();
                for (UserId user : shareMap.keySet()) {
                    Collection<Post> postsToShare = shareMap.get(user);
                    if (postsToShare == null) {
                        continue;
                    }
                    ArrayList<FeedItem> itemList = new ArrayList<>(postsToShare.size());
                    for (Post post : postsToShare) {
                        FeedItem sharedPost = new FeedItem(FeedItem.Type.POST, post.id, null);
                        itemList.add(sharedPost);
                    }
                    sharePosts.add(new SharePosts(user, itemList));
                }
                FeedUpdateIq updateIq = new FeedUpdateIq(sharePosts);
                updateIq.setTo(SERVER_JID);

                sendIqRequestAsync(updateIq).await();
            } catch (ObservableErrorException | InterruptedException e) {
                Log.e("connection: cannot send post", e);
                return false;
            }
            return true;
        });
    }

    @Override
    public void sendPost(@NonNull Post post) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: cannot send post, no connection");
                return;
            }
            try {
                final PublishedEntry entry = new PublishedEntry(
                        PublishedEntry.ENTRY_FEED,
                        null,
                        post.timestamp,
                        me.getUser(),
                        post.text,
                        null,
                        null);
                for (Media media : post.media) {
                    entry.media.add(new PublishedEntry.Media(PublishedEntry.getMediaType(media.type), media.url, media.encKey, media.sha256hash, media.width, media.height));
                }
                for (Mention mention : post.mentions) {
                    entry.mentions.add(Mention.toProto(mention));
                }
                if (post.getAudienceType() == null && post.getParentGroup() == null) {
                    Log.e("connection: sendPost null audience type but not a group post");
                    return;
                }
                FeedItem feedItem = new FeedItem(FeedItem.Type.POST, post.id, entry.getEncodedEntryString());
                if (post.getParentGroup() == null) {
                    FeedUpdateIq publishIq = new FeedUpdateIq(FeedUpdateIq.Action.PUBLISH, feedItem);
                    publishIq.setPostAudience(post.getAudienceType(), post.getAudienceList());
                    publishIq.setTo(SERVER_JID);

                    Observable<Iq> observable = sendIqRequestAsync(publishIq);
                    observable.await();
                } else {
                    GroupFeedUpdateIq publishIq = new GroupFeedUpdateIq(post.getParentGroup(), GroupFeedUpdateIq.Action.PUBLISH, feedItem);
                    publishIq.setTo(SERVER_JID);

                    Observable<Iq> observable = sendIqRequestAsync(publishIq);
                    observable.await();
                }
                connectionObservers.notifyOutgoingPostSent(post.id);
            } catch (ObservableErrorException | InterruptedException e) {
                Log.e("connection: cannot send post", e);
            }
        });
    }

    @Override
    public void retractPost(@NonNull String postId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: cannot retract post, no connection");
                return;
            }
            try {
                FeedUpdateIq requestIq = new FeedUpdateIq(FeedUpdateIq.Action.RETRACT, new FeedItem(FeedItem.Type.POST, postId, null));
                requestIq.setTo(SERVER_JID);
                sendIqRequestAsync(requestIq).await();
                // the {@link PubSubHelper#retractItem(String, Item)} waits for IQ reply, so we can report the post was acked here
                connectionObservers.notifyOutgoingPostSent(postId);
            } catch (ObservableErrorException | InterruptedException e) {
                Log.e("connection: cannot retract post", e);
            }
        });
    }

    @Override
    public void retractGroupPost(@NonNull GroupId groupId, @NonNull String postId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: cannot retract post, no connection");
                return;
            }
            try {
                GroupFeedUpdateIq requestIq = new GroupFeedUpdateIq(groupId, GroupFeedUpdateIq.Action.RETRACT, new FeedItem(FeedItem.Type.POST, postId, null));
                requestIq.setTo(SERVER_JID);
                sendIqRequestAsync(requestIq).await();
                // the {@link PubSubHelper#retractItem(String, Item)} waits for IQ reply, so we can report the post was acked here
                connectionObservers.notifyOutgoingPostSent(postId);
            } catch (ObservableErrorException | InterruptedException e) {
                Log.e("connection: cannot retract post", e);
            }
        });
    }

    @Override
    public void sendComment(@NonNull Comment comment) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: cannot send comment, no connection");
                return;
            }
            try {
                final PublishedEntry entry = new PublishedEntry(
                        PublishedEntry.ENTRY_COMMENT,
                        null,
                        comment.timestamp,
                        me.getUser(),
                        comment.text,
                        comment.postId,
                        comment.parentCommentId);
                for (Mention mention : comment.mentions) {
                    entry.mentions.add(Mention.toProto(mention));
                }
                // Since we're sending a comment, we should have parent post set
                UserId postSender = Preconditions.checkNotNull(comment.getPostSenderUserId());

                FeedItem commentItem = new FeedItem(FeedItem.Type.COMMENT, comment.commentId, comment.postId, entry.getEncodedEntryString());
                commentItem.parentCommentId = comment.parentCommentId;
                if (comment.getParentPost() == null || comment.getParentPost().getParentGroup() == null) {
                    FeedUpdateIq requestIq = new FeedUpdateIq(FeedUpdateIq.Action.PUBLISH, commentItem);
                    requestIq.setTo(SERVER_JID);

                    Observable<Iq> observable = sendIqRequestAsync(requestIq);
                    observable.await();
                } else {
                    GroupFeedUpdateIq requestIq = new GroupFeedUpdateIq(comment.getParentPost().getParentGroup(), FeedUpdateIq.Action.PUBLISH, commentItem);
                    requestIq.setTo(SERVER_JID);

                    Observable<Iq> observable = sendIqRequestAsync(requestIq);
                    observable.await();
                }
                connectionObservers.notifyOutgoingCommentSent(comment.postId, comment.commentId);
            } catch (ObservableErrorException | InterruptedException e) {
                Log.e("connection: cannot send comment", e);
            }
        });
    }

    // TODO: (clarkc) remove post sender user id when server tells us
    @Override
    public void retractComment(@Nullable UserId postSenderUserId, @NonNull String postId, @NonNull String commentId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: cannot retract comment, no connection");
                return;
            }
            try {
                FeedItem commentItem = new FeedItem(FeedItem.Type.COMMENT, commentId, postId, null);
                FeedUpdateIq requestIq = new FeedUpdateIq(FeedUpdateIq.Action.RETRACT, commentItem);
                requestIq.setTo(SERVER_JID);
                sendIqRequestAsync(requestIq).await();
                // the {@link PubSubHelper#retractItem(String, Item)} waits for IQ reply, so we can report the comment was acked here
                connectionObservers.notifyOutgoingCommentSent(postId, commentId);
            } catch (ObservableErrorException | InterruptedException e) {
                Log.e("connection: cannot retract comment", e);
            }
        });
    }

    @Override
    public void retractGroupComment(@NonNull GroupId groupId, @NonNull UserId postSenderUserId, @NonNull String postId, @NonNull String commentId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: cannot retract group comment, no connection");
                return;
            }
            try {
                FeedItem commentItem = new FeedItem(FeedItem.Type.COMMENT, commentId, postId, null);
                GroupFeedUpdateIq requestIq = new GroupFeedUpdateIq(groupId, GroupFeedUpdateIq.Action.RETRACT, commentItem);
                requestIq.setTo(SERVER_JID);
                sendIqRequestAsync(requestIq).await();

                connectionObservers.notifyOutgoingCommentSent(postId, commentId);
            } catch (ObservableErrorException | InterruptedException e) {
                Log.e("connection: cannot retract comment", e);
            }
        });
    }

    @Override
    public void sendMessage(@NonNull Message message, @Nullable SessionSetupInfo sessionSetupInfo) {
        executor.execute(() -> {
            if (message.isLocalMessage()) {
                Log.i("connection: System message shouldn't be sent");
                return;
            }
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: cannot send message, no connection");
                return;
            }
            final UserId recipientUserId = (UserId)message.chatId;

            ChatMessageElement chatMessageElement = new ChatMessageElement(
                    message,
                    recipientUserId,
                    sessionSetupInfo);

            Msg msg = Msg.newBuilder()
                    .setId(message.id)
                    .setType(Msg.Type.CHAT)
                    .setToUid(Long.parseLong(message.chatId.rawId()))
                    .setChatStanza(chatMessageElement.toProto())
                    .build();
            ackHandlers.put(message.id, () -> connectionObservers.notifyOutgoingMessageSent(message.chatId, message.id));
            sendPacket(Packet.newBuilder().setMsg(msg).build());
        });
    }

    public void sendSilentMessage(@NonNull Message message, @Nullable SessionSetupInfo sessionSetupInfo) {
        executor.execute(() -> {
            if (message.isLocalMessage()) {
                Log.i("connection: System message shouldn't be sent");
                return;
            }
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: cannot send message, no connection");
                return;
            }
            final UserId recipientUserId = (UserId)message.chatId;

            ChatMessageElement chatMessageElement = new ChatMessageElement(
                    message,
                    recipientUserId,
                    sessionSetupInfo);

            SilentChatStanza silentChatStanza = SilentChatStanza.newBuilder().setChatStanza(chatMessageElement.toProto()).build();

            Msg msg = Msg.newBuilder()
                    .setId(message.id)
                    .setType(Msg.Type.CHAT)
                    .setToUid(Long.parseLong(message.chatId.rawId()))
                    .setSilentChatStanza(silentChatStanza)
                    .build();
            ackHandlers.put(message.id, () -> connectionObservers.notifyOutgoingMessageSent(message.chatId, message.id));
            sendPacket(Packet.newBuilder().setMsg(msg).build());
        });
    }

    @Override
    public void sendGroupMessage(@NonNull Message message, @Nullable SessionSetupInfo sessionSetupInfo) {
        executor.execute(() -> {
            if (message.isLocalMessage()) {
                Log.i("connection: System message shouldn't be sent");
                return;
            }
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: cannot send message, no connection");
                return;
            }

            GroupChatMessage groupChatMessage = new GroupChatMessage((GroupId)message.chatId, message);

            Msg msg = Msg.newBuilder()
                    .setId(message.id)
                    .setType(Msg.Type.GROUPCHAT)
                    .setGroupChat(groupChatMessage.toProto())
                    .build();

            ackHandlers.put(message.id, () -> connectionObservers.notifyOutgoingMessageSent(message.chatId, message.id));
            Log.i("connection: sending group message " + message.id + " to " + message.chatId);
            sendPacket(Packet.newBuilder().setMsg(msg).build());
        });
    }

    // NOTE: Should NOT be called from executor.
    @Override
    public <T extends IQ> Observable<T> sendRequestIq(@NonNull HalloIq iq) {
        BackgroundObservable<T> iqResponse = new BackgroundObservable<>(bgWorkers);
        executor.execute(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: cannot send iq " + iq + ", no connection");
                iqResponse.setException(new SmackException.NotConnectedException());
                return;
            }
            Observable<Iq> observable = sendIqRequestAsync(iq);
            observable.onResponse(resultIq -> {
                try {
                    iqResponse.setResponse((T) HalloIq.fromProtoIq(resultIq));
                } catch (ClassCastException e) {
                    iqResponse.setException(e);
                }
            });
            observable.onError(iqResponse::setException);
        });
        return iqResponse;
    }

    private Observable<Iq> sendIqRequestAsync(@NonNull HalloIq iq) {
        BackgroundObservable<Iq> iqResponse = new BackgroundObservable<>(bgWorkers);
        Iq protoIq = iq.toProtoIq();
        iqRouter.sendAsync(protoIq).onResponse(iqResponse::setResponse).onError(iqResponse::setException);
        return iqResponse;
    }

    @Override
    public void sendRerequest(String encodedIdentityKey, @NonNull Jid originalSender, final @NonNull UserId senderUserId, @NonNull String messageId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: cannot send rerequest, no connection");
                return;
            }
            RerequestElement rerequestElement = new RerequestElement(encodedIdentityKey, senderUserId);
            Log.i("connection: sending rerequest for " + messageId + " to " + originalSender);
            sendPacket(Packet.newBuilder().setMsg(rerequestElement.toProto()).build());
        });
    }

    @Override
    public void sendAck(@NonNull String id) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: cannot send ack, no connection");
                return;
            }
            final AckStanza ack = new AckStanza(SERVER_JID, id);
            Log.i("connection: sending ack for " + id);
            sendPacket(Packet.newBuilder().setAck(ack.toProto()).build());
        });
    }

    @Override
    public void sendPostSeenReceipt(@NonNull UserId senderUserId, @NonNull String postId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: cannot send seen receipt, no connection");
                return;
            }
            String id = RandomId.create();

            SeenReceiptElement seenReceiptElement = new SeenReceiptElement(FEED_THREAD_ID, postId);
            Msg msg = Msg.newBuilder()
                    .setId(id)
                    .setSeenReceipt(seenReceiptElement.toProto())
                    .setToUid(Long.parseLong(senderUserId.rawId()))
                    .build();

            ackHandlers.put(id, () -> connectionObservers.notifyIncomingPostSeenReceiptSent(senderUserId, postId));
            sendPacket(Packet.newBuilder().setMsg(msg).build());
            Log.i("connection: sending post seen receipt " + postId + " to " + senderUserId);
        });
    }

    @Override
    public void sendMessageSeenReceipt(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || sslSocket == null) {
                Log.e("connection: cannot send message seen receipt, no connection");
                return;
            }
            String id = RandomId.create();

            SeenReceiptElement seenReceiptElement = new SeenReceiptElement(senderUserId.equals(chatId) ? null : chatId.rawId(), messageId);
            Msg msg = Msg.newBuilder()
                    .setId(id)
                    .setSeenReceipt(seenReceiptElement.toProto())
                    .setToUid(Long.parseLong(senderUserId.rawId()))
                    .build();
            ackHandlers.put(id, () -> connectionObservers.notifyIncomingMessageSeenReceiptSent(chatId, senderUserId, messageId));
            sendPacket(Packet.newBuilder().setMsg(msg).build());
            Log.i("connection: sending message seen receipt " + messageId + " to " + senderUserId);
        });
    }
    
    @Override
    public UserId getUserId(@NonNull String user) {
        return isMe(user) ? UserId.ME : new UserId(user);
    }

    @Override
    public boolean getClientExpired() {
        return clientExpired;
    }

    private void processMentions(@NonNull Collection<Mention> mentions) {
        for (Mention mention : mentions) {
            processMention(mention);
        }
    }

    private void processMention(@NonNull Mention mention) {
        if (mention.userId != null) {
            if (isMe(mention.userId.rawId())) {
                mention.userId = UserId.ME;
            }
        }
    }

    private boolean isMe(@NonNull String user) {
        return user.equals(me.getUser());
    }

    private class PacketReader {
        private static final int BUF_SIZE = 4096;

        private volatile boolean done;

        void init() {
            done = false;
            Async.go(this::parsePackets, "Packet Reader"); // TODO(jack): Connection counter
        }

        void shutdown() {
            done = true;
        }

        private void parsePackets() {
            ThreadUtils.setSocketTag();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (!done) {
                try {
                    InputStream is = inputStream;
                    if (is == null) {
                        disconnect();
                        throw new IOException("Input stream is null");
                    }

                    boolean needMoreBytes = true;
                    byte[] buf = new byte[BUF_SIZE];
                    int packetSize = 0;
                    while (needMoreBytes) {
                        int c = inputStream.read(buf);
                        if (c < 0) {
                            disconnect();
                            throw new IOException("No bytes read from input stream");
                        }

                        baos.write(buf, 0, c);

                        if (baos.size() >= 4) {
                            byte[] header = Arrays.copyOfRange(baos.toByteArray(), 0, 4);
                            ByteBuffer wrapped = ByteBuffer.wrap(header); // big-endian by default
                            packetSize = wrapped.getInt();

                            if (baos.size() >= packetSize + 4) {
                                needMoreBytes = false;
                            }
                        }
                    }

                    byte[] bytes = baos.toByteArray();
                    byte[] nextPacket = Arrays.copyOfRange(bytes, 4, packetSize + 4);
                    byte[] leftovers = Arrays.copyOfRange(bytes, packetSize + 4, bytes.length);

                    baos.reset();
                    baos.write(leftovers);

                    parsePacket(nextPacket);
                    // TODO(jack): Interrupted exception anywhere? Hmmm
                } catch (Exception e) {
                    if (!done) {
                        Log.e("Packet Reader error", e);
                    }
                }
            }
        }

        private void parsePacket(@NonNull byte[] bytes) {
            try {
                Packet packet = Packet.parseFrom(bytes);
                Log.i("connection: recv: " + ProtoPrinter.toString(packet));

                if (packet.hasMsg()) {
                    handleMsg(packet.getMsg());
                } else if (packet.hasIq()) {
                    handleIq(packet.getIq());
                } else if (packet.hasAck()) {
                    handleAck(packet.getAck());
                } else if (packet.hasPresence()) {
                    handlePresence(packet.getPresence());
                } else if (packet.hasHaError()) {
                    handleHaError(packet.getHaError());
                } else if (packet.hasChatState()) {
                    handleChatState(packet.getChatState());
                } else {
                    Log.w("Unrecognized top-level subpacket");
                }
            } catch (InvalidProtocolBufferException e) { // TODO(jack): be explicit about when auth expected
                try {
                    AuthResult authResult = AuthResult.parseFrom(bytes);
                    handleAuth(authResult);
                } catch (InvalidProtocolBufferException f) {
                    Log.e("Failed to parse incoming protobuf; was not auth", f);
                    disconnect();
                }
            }
        }

        private void handleAuth(AuthResult authResult) {
            connectionPropHash = Hex.bytesToStringLowercase(authResult.getPropsHash().toByteArray());
            NewConnection.this.authResult = authResult; // TODO(jack): use wait() and notify() instead
        }

        private void handleMsg(Msg msg) {
            boolean handled = false;
            if (msg.getType() == Msg.Type.ERROR) {
                Log.w("connection: got error message " + ProtoPrinter.toString(msg));
            } else {
                if (msg.hasFeedItem()) {
                    Log.i("connection: got feed item " + ProtoPrinter.toString(msg));
                    com.halloapp.proto.server.FeedItem feedItem = msg.getFeedItem();
                    handled = processFeedPubSubItems(Collections.singletonList(feedItem), msg.getId());
                } else if (msg.hasFeedItems()) {
                    Log.i("connection: got feed items " + ProtoPrinter.toString(msg));
                    FeedItems feedItems = msg.getFeedItems();
                    handled = processFeedPubSubItems(feedItems.getItemsList(), msg.getId());
                } else if (msg.hasGroupFeedItem()) {
                    Log.i("connection: got group feed item " + ProtoPrinter.toString(msg));
                    GroupFeedItem groupFeedItem = msg.getGroupFeedItem();
                    handled = processGroupFeedItems(Collections.singletonList(groupFeedItem), msg.getId());
                } else if (msg.hasChatStanza()) {
                    Log.i("connection: got chat stanza " + ProtoPrinter.toString(msg));
                    ChatStanza chatStanza = msg.getChatStanza();
                    String senderName = chatStanza.getSenderName();
                    UserId fromUserId = new UserId(Long.toString(msg.getFromUid()));

                    if (!TextUtils.isEmpty(senderName)) {
                        connectionObservers.notifyUserNamesReceived(Collections.singletonMap(fromUserId, senderName));
                    }

                    ChatMessageElement chatMessageElement = ChatMessageElement.fromProto(chatStanza);
                    Jid fromJid = JidCreate.bareFrom(Localpart.fromOrThrowUnchecked(fromUserId.rawId()), SERVER_JID.getDomain());
                    Message message = chatMessageElement.getMessage(fromJid, fromUserId, msg.getId());
                    processMentions(message.mentions);
                    connectionObservers.notifyIncomingMessageReceived(message);
                    handled = true;
                } else if (msg.hasSilentChatStanza()) { // TODO(jack): remove silent chat stanzas when no longer needed
                    Log.i("connection: got silent chat stanza " + ProtoPrinter.toString(msg));
                    ChatStanza chatStanza = msg.getSilentChatStanza().getChatStanza();
                    UserId fromUserId = new UserId(Long.toString(msg.getFromUid()));

                    // NOTE: push names are not collected because eventually these messages will be removed

                    ChatMessageElement chatMessageElement = ChatMessageElement.fromProto(chatStanza);
                    Jid fromJid = JidCreate.bareFrom(Localpart.fromOrThrowUnchecked(fromUserId.rawId()), SERVER_JID.getDomain());
                    Message message = chatMessageElement.getMessage(fromJid, fromUserId, msg.getId());
                    processMentions(message.mentions);
                    // Do not call connectionObservers; this message is not user-visible
                    handled = true;
                } else if (msg.hasGroupChat()) {
                    Log.i("connection: got group chat " + ProtoPrinter.toString(msg));
                    GroupChat groupChat = msg.getGroupChat();
                    String senderName = groupChat.getSenderName();
                    UserId fromUserId = new UserId(Long.toString(msg.getFromUid()));

                    if (!TextUtils.isEmpty(senderName)) {
                        connectionObservers.notifyUserNamesReceived(Collections.singletonMap(fromUserId, senderName));
                    }

                    GroupChatMessage groupChatMessage = GroupChatMessage.fromProto(groupChat);
                    Jid fromJid = JidCreate.bareFrom(Localpart.fromOrThrowUnchecked(fromUserId.rawId()), SERVER_JID.getDomain());
                    Message message = groupChatMessage.getMessage(fromJid, msg.getId());
                    processMentions(message.mentions);
                    connectionObservers.notifyIncomingMessageReceived(message);
                    handled = true;
                } else if (msg.hasDeliveryReceipt()) {
                    Log.i("connection: got delivery receipt " + ProtoPrinter.toString(msg));
                    DeliveryReceipt deliveryReceipt = msg.getDeliveryReceipt();
                    final String threadId = deliveryReceipt.getThreadId();
                    final UserId userId = getUserId(Long.toString(msg.getFromUid()));
                    connectionObservers.notifyOutgoingMessageDelivered(TextUtils.isEmpty(threadId) ? userId : ChatId.fromNullable(threadId), userId, deliveryReceipt.getId(), deliveryReceipt.getTimestamp(), msg.getId());
                    handled = true;
                } else if (msg.hasSeenReceipt()) {
                    Log.i("connection: got seen receipt " + ProtoPrinter.toString(msg));
                    SeenReceipt seenReceipt = msg.getSeenReceipt();
                    final String threadId = seenReceipt.getThreadId();
                    final UserId userId = getUserId(Long.toString(msg.getFromUid()));
                    if (FEED_THREAD_ID.equals(threadId)) {
                        connectionObservers.notifyOutgoingPostSeen(userId, seenReceipt.getId(), seenReceipt.getTimestamp(), msg.getId());
                    } else {
                        connectionObservers.notifyOutgoingMessageSeen(TextUtils.isEmpty(threadId) ? userId : ChatId.fromNullable(threadId), userId, seenReceipt.getId(), seenReceipt.getTimestamp(), msg.getId());
                    }
                    handled = true;
                } else if (msg.hasContactHash()) {
                    Log.i("connection: got contact hash " + ProtoPrinter.toString(msg));
                    ContactHash contactHash = msg.getContactHash();
                    connectionObservers.notifyContactsChanged(new ArrayList<>(), Collections.singletonList(Hex.bytesToStringLowercase(contactHash.getHash().toByteArray())), msg.getId());
                    handled = true;
                } else if (msg.hasContactList()) {
                    Log.i("connection: got contact list " + ProtoPrinter.toString(msg));
                    ContactList contactList = msg.getContactList();
                    List<Contact> contacts = contactList.getContactsList();
                    List<ContactInfo> infos = new ArrayList<>();
                    for (Contact contact : contacts) {
                        infos.add(new ContactInfo(contact));
                    }
                    connectionObservers.notifyContactsChanged(infos, new ArrayList<>(), msg.getId());
                } else if (msg.hasWhisperKeys()) {
                    Log.i("connection: got whisper keys " + ProtoPrinter.toString(msg));
                    WhisperKeys whisperKeys = msg.getWhisperKeys();

                    WhisperKeysMessage whisperKeysMessage = null;
                    if (whisperKeys.getAction().equals(WhisperKeys.Action.UPDATE)) {
                        whisperKeysMessage = new WhisperKeysMessage(new UserId(Long.toString(whisperKeys.getUid())));
                    } else if (whisperKeys.getAction().equals(WhisperKeys.Action.NORMAL)) {
                        whisperKeysMessage = new WhisperKeysMessage(whisperKeys.getOtpKeyCount());
                    }
                    connectionObservers.notifyWhisperKeysMessage(whisperKeysMessage, msg.getId());
                    handled = true;
                } else if (msg.hasAvatar()) {
                    Log.i("connection: got avatar " + ProtoPrinter.toString(msg));
                    Avatar avatar = msg.getAvatar();
                    connectionObservers.notifyAvatarChangeMessageReceived(getUserId(Long.toString(avatar.getUid())), avatar.getId(), msg.getId());
                    handled = true;
                } else if (msg.hasGroupStanza()) {
                    Log.i("connection: got group change message " + ProtoPrinter.toString(msg));
                    GroupStanza groupStanza = msg.getGroupStanza();

                    long rawUserId = groupStanza.getSenderUid();
                    UserId senderUserId = rawUserId != 0 ? getUserId(Long.toString(rawUserId)) : null;
                    String senderName = groupStanza.getSenderName();
                    if (!TextUtils.isEmpty(senderName) && rawUserId != 0) {
                        connectionObservers.notifyUserNamesReceived(Collections.singletonMap(senderUserId, senderName));
                    }

                    String ackId = msg.getId();
                    GroupId groupId = new GroupId(groupStanza.getGid());
                    List<GroupMember> members = groupStanza.getMembersList();
                    List<MemberElement> elements = new ArrayList<>();
                    if (members != null) {
                        for (GroupMember member : members) {
                            elements.add(new MemberElement(member));
                        }
                    }

                    handled = true;
                    if (groupStanza.getAction().equals(GroupStanza.Action.CREATE)) {
                        connectionObservers.notifyGroupCreated(groupId, groupStanza.getName(), groupStanza.getAvatarId(), elements, senderUserId, senderName, ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.MODIFY_MEMBERS)) {
                        connectionObservers.notifyGroupMemberChangeReceived(groupId, groupStanza.getName(), groupStanza.getAvatarId(), elements, senderUserId, senderName, ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.LEAVE)) {
                        connectionObservers.notifyGroupMemberLeftReceived(groupId, elements, ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.MODIFY_ADMINS)) {
                        connectionObservers.notifyGroupAdminChangeReceived(groupId, elements, senderUserId, senderName, ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.CHANGE_NAME)) {
                        connectionObservers.notifyGroupNameChangeReceived(groupId, groupStanza.getName(), senderUserId, senderName, ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.CHANGE_AVATAR)) {
                        connectionObservers.notifyGroupAvatarChangeReceived(groupId, groupStanza.getAvatarId(), senderUserId, senderName, ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.AUTO_PROMOTE_ADMINS)) {
                        connectionObservers.notifyGroupAdminAutoPromoteReceived(groupId, elements, ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.DELETE)) {
                        connectionObservers.notifyGroupDeleteReceived(groupId, senderUserId, senderName, ackId);
                    } else {
                        handled = false;
                        Log.w("Unrecognized group stanza action " + groupStanza.getAction());
                    }
                } else if (msg.hasRerequest()) {
                    Log.i("connection: got rerequest message " + ProtoPrinter.toString(msg));
                    connectionObservers.notifyMessageRerequest(getUserId(Long.toString(msg.getFromUid())), msg.getRerequest().getId(), msg.getId());
                    handled = true;
                }
            }
            if (!handled) {
                Log.i("connection: got unknown message " + ProtoPrinter.toString(msg));
                sendAck(msg.getId());
            }
        }

        private boolean processFeedPubSubItems(@NonNull List<com.halloapp.proto.server.FeedItem> items, @NonNull String ackId) {
            final List<Post> posts = new ArrayList<>();
            final List<Comment> comments = new ArrayList<>();
            final Map<UserId, String> names = new HashMap<>();

            for (com.halloapp.proto.server.FeedItem item : items) {
                if (item.getAction().equals(com.halloapp.proto.server.FeedItem.Action.RETRACT)) {
                    if (item.hasComment()) {
                        com.halloapp.proto.server.Comment comment = item.getComment();
                        UserId publisherUserId = new UserId(Long.toString(comment.getPublisherUid()));
                        if (comment.getPublisherUid() != 0 && comment.getPublisherName() != null) {
                            names.put(publisherUserId, comment.getPublisherName());
                        }
                        connectionObservers.notifyCommentRetracted(comment.getId(), publisherUserId, comment.getPostId(), comment.getTimestamp());
                    } else if (item.hasPost()) {
                        com.halloapp.proto.server.Post post = item.getPost();
                        UserId publisherUserId = new UserId(Long.toString(post.getPublisherUid()));
                        if (post.getPublisherUid() != 0 && post.getPublisherName() != null) {
                            names.put(publisherUserId, post.getPublisherName());
                        }
                        connectionObservers.notifyPostRetracted(publisherUserId, post.getId());
                    }
                } else if (item.getAction().equals(com.halloapp.proto.server.FeedItem.Action.SHARE) || item.getAction() == com.halloapp.proto.server.FeedItem.Action.PUBLISH) {
                    if (item.hasPost()) {
                        com.halloapp.proto.server.Post protoPost = item.getPost();
                        if (protoPost.getPublisherUid() != 0 && protoPost.getPublisherName() != null) {
                            names.put(new UserId(Long.toString(protoPost.getPublisherUid())), protoPost.getPublisherName());
                        }

                        byte[] payload = protoPost.getPayload().toByteArray();
                        PublishedEntry publishedEntry = PublishedEntry.getFeedEntry(Base64.encodeToString(payload, Base64.NO_WRAP), protoPost.getId(), protoPost.getTimestamp(), Long.toString(protoPost.getPublisherUid()));

                        // NOTE: publishedEntry.timestamp == 1000L * protoPost.getTimestamp()
                        Post np = new Post(-1, getUserId(Long.toString(protoPost.getPublisherUid())), protoPost.getId(), publishedEntry.timestamp, publishedEntry.media.isEmpty() ? Post.TRANSFERRED_YES : Post.TRANSFERRED_NO, Post.SEEN_NO, publishedEntry.text);
                        for (PublishedEntry.Media entryMedia : publishedEntry.media) {
                            np.media.add(Media.createFromUrl(PublishedEntry.getMediaType(entryMedia.type), entryMedia.url,
                                    entryMedia.encKey, entryMedia.sha256hash,
                                    entryMedia.width, entryMedia.height));
                        }
                        for (com.halloapp.proto.clients.Mention mentionProto : publishedEntry.mentions) {
                            Mention mention = Mention.parseFromProto(mentionProto);
                            processMention(mention);
                            np.mentions.add(mention);
                        }
                        posts.add(np);
                    } else if (item.hasComment()) {
                        com.halloapp.proto.server.Comment protoComment = item.getComment();
                        if (protoComment.getPublisherUid() != 0 && protoComment.getPublisherName() != null) {
                            names.put(new UserId(Long.toString(protoComment.getPublisherUid())), protoComment.getPublisherName());
                        }

                        byte[] payload = protoComment.getPayload().toByteArray();
                        PublishedEntry publishedEntry = PublishedEntry.getFeedEntry(Base64.encodeToString(payload, Base64.NO_WRAP), protoComment.getId(), protoComment.getTimestamp(), Long.toString(protoComment.getPublisherUid()));

                        final Comment comment = new Comment(0,
                                publishedEntry.feedItemId,
                                getUserId(Long.toString(protoComment.getPublisherUid())),
                                publishedEntry.id,
                                publishedEntry.parentCommentId,
                                publishedEntry.timestamp,
                                true,
                                false,
                                publishedEntry.text
                        );
                        for (com.halloapp.proto.clients.Mention mentionProto : publishedEntry.mentions) {
                            Mention mention = Mention.parseFromProto(mentionProto);
                            processMention(mention);
                            comment.mentions.add(mention);
                        }
                        comments.add(comment);
                    }
                }
            }

            if (!names.isEmpty()) {
                connectionObservers.notifyUserNamesReceived(names);
            }

            if (!posts.isEmpty() || !comments.isEmpty()) {
                connectionObservers.notifyIncomingFeedItemsReceived(posts, comments, ackId);
            }

            return !posts.isEmpty() || !comments.isEmpty();
        }

        private boolean processGroupFeedItems(@NonNull List<GroupFeedItem> items, @NonNull String ackId) {
            final List<Post> posts = new ArrayList<>();
            final List<Comment> comments = new ArrayList<>();
            final Map<UserId, String> names = new HashMap<>();

            for (GroupFeedItem item : items) {
                if (item.getAction() == GroupFeedItem.Action.PUBLISH) {
                    if (item.hasComment()) {
                        com.halloapp.proto.server.Comment protoComment = item.getComment();
                        byte[] payload = protoComment.getPayload().toByteArray();
                        PublishedEntry publishedEntry = PublishedEntry.getFeedEntry(Base64.encodeToString(payload, Base64.NO_WRAP), protoComment.getId(), protoComment.getTimestamp(), Long.toString(protoComment.getPublisherUid()));
                        final Comment comment = new Comment(0,
                                publishedEntry.feedItemId,
                                getUserId(Long.toString(protoComment.getPublisherUid())),
                                publishedEntry.id,
                                publishedEntry.parentCommentId,
                                publishedEntry.timestamp,
                                true,
                                false,
                                publishedEntry.text);
                        for (com.halloapp.proto.clients.Mention mentionProto : publishedEntry.mentions) {
                            Mention mention = Mention.parseFromProto(mentionProto);
                            processMention(mention);
                            comment.mentions.add(mention);
                        }
                        comments.add(comment);
                    } else if (item.hasPost()) {
                        com.halloapp.proto.server.Post protoPost = item.getPost();
                        if (protoPost.getPublisherUid() != 0 && protoPost.getPublisherName() != null) {
                            names.put(new UserId(Long.toString(protoPost.getPublisherUid())), protoPost.getPublisherName());
                        }

                        byte[] payload = protoPost.getPayload().toByteArray();
                        PublishedEntry publishedEntry = PublishedEntry.getFeedEntry(Base64.encodeToString(payload, Base64.NO_WRAP), protoPost.getId(), protoPost.getTimestamp(), Long.toString(protoPost.getPublisherUid()));

                        // NOTE: publishedEntry.timestamp == 1000L * protoPost.getTimestamp()
                        Post post = new Post(-1, getUserId(Long.toString(protoPost.getPublisherUid())), protoPost.getId(), publishedEntry.timestamp, publishedEntry.media.isEmpty() ? Post.TRANSFERRED_YES : Post.TRANSFERRED_NO, Post.SEEN_NO, publishedEntry.text);
                        for (PublishedEntry.Media entryMedia : publishedEntry.media) {
                            post.media.add(Media.createFromUrl(PublishedEntry.getMediaType(entryMedia.type), entryMedia.url,
                                    entryMedia.encKey, entryMedia.sha256hash,
                                    entryMedia.width, entryMedia.height));
                        }
                        for (com.halloapp.proto.clients.Mention mentionProto : publishedEntry.mentions) {
                            Mention mention = Mention.parseFromProto(mentionProto);
                            processMention(mention);
                            post.mentions.add(mention);
                        }
                        post.setParentGroup(new GroupId(item.getGid()));
                        posts.add(post);
                    }

                    if (!names.isEmpty()) {
                        connectionObservers.notifyUserNamesReceived(names);
                    }

                    if (!posts.isEmpty() || !comments.isEmpty()) {
                        connectionObservers.notifyIncomingFeedItemsReceived(posts, comments, ackId);
                    }
                    return !posts.isEmpty() || !comments.isEmpty();
                } else if (item.getAction() == GroupFeedItem.Action.RETRACT) {
                    if (item.hasComment()) {
                        com.halloapp.proto.server.Comment comment = item.getComment();
                        connectionObservers.notifyCommentRetracted(comment.getId(), getUserId(Long.toString(comment.getPublisherUid())), comment.getPostId(), comment.getTimestamp());
                    } else if (item.hasPost()) {
                        com.halloapp.proto.server.Post post = item.getPost();
                        connectionObservers.notifyPostRetracted(getUserId(Long.toString(post.getPublisherUid())), new GroupId(item.getGid()), post.getId());
                    }
                }
            }
            return false;
        }

        private void handleIq(Iq iq) {
            if (iq.getType().equals(Iq.Type.RESULT)) {
                iqRouter.onResponse(iq.getId(), iq);
            } else if (iq.getType().equals(Iq.Type.ERROR)) {
                ErrorStanza errorStanza = iq.getErrorStanza();
                iqRouter.onError(iq.getId(), errorStanza.getReason());
            } else if (iq.getType().equals(Iq.Type.GET)) {
                if (iq.hasPing()) {
                    Iq ping = Iq.newBuilder().setId(iq.getId()).setType(Iq.Type.RESULT).setPing(Ping.newBuilder().build()).build();
                    sendPacket(Packet.newBuilder().setIq(ping).build());
                } else {
                    Log.w("connection: unexpected GET iq " + ProtoPrinter.toString(iq));
                }
            } else {
                Log.w("connection: unexpected iq type " + iq.getType());
            }
        }

        private void handleAck(Ack ack) {
            final Runnable handler = ackHandlers.remove(ack.getId());
            if (handler != null) {
                handler.run();
            } else {
                Log.w("connection: ack doesn't match any pending message " + ProtoPrinter.toString(ack));
            }
        }

        private void handlePresence(Presence presence) {
            long lastSeen = presence.getLastSeen();
            connectionObservers.notifyPresenceReceived(getUserId(Long.toString(presence.getUid())), lastSeen > 0 ? lastSeen : null);
        }

        private void handleHaError(HaError haError) {
            Log.e("connection: server error: " + haError.getReason());
        }

        private void handleChatState(ChatState chatState) {
            com.halloapp.xmpp.ChatState state = null;
            if (chatState.getThreadType().equals(ChatState.ThreadType.CHAT)) {
                state = new com.halloapp.xmpp.ChatState(processChatStateType(chatState.getType()), getUserId(chatState.getThreadId()));
            } else if (chatState.getThreadType().equals(ChatState.ThreadType.GROUP_CHAT)) {
                state = new com.halloapp.xmpp.ChatState(processChatStateType(chatState.getType()), new GroupId(chatState.getThreadId()));
            }
            UserId from = getUserId(Long.toString(chatState.getFromUid()));
            if (from != null && !from.isMe()) {
                connectionObservers.notifyChatStateReceived(from, state);
            }
        }

        private @com.halloapp.xmpp.ChatState.Type int processChatStateType(ChatState.Type type) {
            return com.halloapp.proto.server.ChatState.Type.TYPING.equals(type) ? com.halloapp.xmpp.ChatState.Type.TYPING : com.halloapp.xmpp.ChatState.Type.AVAILABLE;
        }
    }

    private class PacketWriter {
        private static final int QUEUE_CAPACITY = 100;

        private ArrayBlockingQueue<byte[]> queue = new ArrayBlockingQueue<byte[]>(QUEUE_CAPACITY, true);

        private volatile boolean done;

        void init() {
            done = false;
            Async.go(this::writePackets, "Packet Writer"); // TODO(jack): Connection counter
        }

        void shutdown() {
            done = true;
        }

        void sendPacket(Packet packet) {
            Log.i("connection: send: " + ProtoPrinter.toString(packet));
            byte[] size = ByteBuffer.allocate(4).putInt(packet.getSerializedSize()).array();
            byte[] finalPacket = packet.toByteArray();

            byte[] bytes = new byte[size.length + finalPacket.length];
            System.arraycopy(size, 0, bytes, 0, size.length);
            System.arraycopy(finalPacket, 0, bytes, size.length, finalPacket.length);

            sendRawBytes(bytes);
        }

        void sendRawBytes(byte[] bytes) {
            try {
                enqueue(bytes);
            } catch (InterruptedException e) {
                Log.w("Interrupted enqueueing packet for writing", e);
            }
        }

        private void enqueue(byte[] bytes) throws InterruptedException {
            queue.put(bytes);
        }

        private void writePackets() {
            ThreadUtils.setSocketTag();
            try {
                while (!done) {
                    try { // TODO(jack): Await login success
                        byte[] bytes = queue.take();
                        if (bytes == null) {
                            Log.w("Got null bytes in write queue");
                            continue;
                        }

                        OutputStream os = outputStream;
                        if (os == null) {
                            disconnect();
                            throw new IOException("Output stream is null");
                        }

                        outputStream.write(bytes);
                    } catch (InterruptedException e) {
                        Log.w("Packet writing interrupted", e);
                    }
                }
            } catch (Exception e) {
                if (!done) {
                    Log.e("Packet Writer error", e);
                }
            }
        }
    }

    private class IqRouter {
        private static final long IQ_TIMEOUT_MS = 20_000;

        private Map<String, Iq> responses = new ConcurrentHashMap<>();
        private Map<String, ResponseHandler<Iq>> successCallbacks = new ConcurrentHashMap<>();
        private Map<String, ExceptionHandler> failureCallbacks = new ConcurrentHashMap<>();

        public void onResponse(String id, Iq iq) {
            responses.put(id, iq);

            ResponseHandler<Iq> callback = successCallbacks.remove(id);
            if (callback != null) {
                callback.handleResponse(iq);
            } else {
                Log.w("no callback for " + id);
            }
        }

        public void onError(String id, String reason) {
            Log.d("IqRouter: got error for id " + id + " with reason " + reason);
            ExceptionHandler callback = failureCallbacks.remove(id);
            if (callback != null) {
                callback.handleException(new RuntimeException("IQ Error: " + reason)); // TODO(jack): custom exception
            } else {
                Log.w("IqRouter: no callback for " + id);
            }
        }

        public Observable<Iq> sendAsync(Iq iq) {
            BackgroundObservable<Iq> observable = new BackgroundObservable<>(bgWorkers);
            Packet packet = Packet.newBuilder().setIq(iq).build();
            setCallbacks(iq.getId(), observable::setResponse, observable::setException);
            sendPacket(packet);
            return observable;
        }

        public Iq sendSync(Iq iq) throws ExecutionException {
            try {
                return sendAsync(iq).await();
            } catch (InterruptedException | ObservableErrorException e) {
                throw new ExecutionException(e);
            }
        }

        void reset() {
            responses.clear();
            successCallbacks.clear();

            Map<String, ExceptionHandler> failureCallbacksCopy = new HashMap<>(failureCallbacks);
            failureCallbacks.clear();

            for (String key : failureCallbacksCopy.keySet()) {
                Log.i("IqRouter marking " + key + " as failure during reset");
                ExceptionHandler failure = failureCallbacksCopy.get(key);
                if (failure != null) {
                    failure.handleException(new TimeoutException("IqRouter reset while waiting for " + key));
                }
            }
        }

        private void setCallbacks(String id, ResponseHandler<Iq> success, ExceptionHandler failure) {
            if (TextUtils.isEmpty(id)) {
                Log.w("connection: trying to set callbacks for empty id!");
            }
            successCallbacks.put(id, success);
            failureCallbacks.put(id, failure);
            scheduleRemoval(id);
        }

        private void clear(String id) {
            responses.remove(id);
            successCallbacks.remove(id);
            failureCallbacks.remove(id);
        }

        private void scheduleRemoval(String id) {
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    executor.execute(() -> {
                        Iq response = responses.get(id);
                        if (response == null) {
                            ExceptionHandler failure = failureCallbacks.get(id);
                            if (failure != null) {
                                failure.handleException(new TimeoutException("Timeout for " + id));
                            }
                        } else {
                            ResponseHandler<Iq> success = successCallbacks.get(id);
                            if (success != null) {
                                success.handleResponse(response);
                            }
                        }
                        clear(id);
                    });
                }
            };
            timer.schedule(timerTask, IQ_TIMEOUT_MS);
        }
    }

    // TODO(jack): Remove once XMPP is gone
    private static Jid userIdToJid(@NonNull UserId userId) {
        return JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked(userId.rawId()), Domainpart.fromOrNull(XMPP_DOMAIN));
    }
}
