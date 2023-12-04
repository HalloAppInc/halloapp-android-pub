package com.halloapp.xmpp;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Build;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.android.gms.common.util.Hex;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.AppContext;
import com.halloapp.BuildConfig;
import com.halloapp.ConnectionObservers;
import com.halloapp.Constants;
import com.halloapp.ForegroundObserver;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.contacts.ContactSyncResult;
import com.halloapp.contacts.RelationshipInfo;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.KatchupPost;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.content.MomentPost;
import com.halloapp.content.Post;
import com.halloapp.content.Reaction;
import com.halloapp.content.ReactionComment;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.group.GroupFeedSessionManager;
import com.halloapp.crypto.group.GroupSetupInfo;
import com.halloapp.crypto.home.HomeFeedSessionManager;
import com.halloapp.crypto.home.HomePostSetupInfo;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.crypto.signal.SignalSessionSetupInfo;
import com.halloapp.crypto.web.WebClientManager;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.noise.HANoiseSocket;
import com.halloapp.noise.NoiseException;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.Background;
import com.halloapp.proto.clients.CommentContainer;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.clients.EncryptedPayload;
import com.halloapp.proto.clients.KMomentContainer;
import com.halloapp.proto.clients.PostContainer;
import com.halloapp.proto.clients.SenderKey;
import com.halloapp.proto.clients.SenderState;
import com.halloapp.proto.log_events.EventData;
import com.halloapp.proto.server.Ack;
import com.halloapp.proto.server.AiImage;
import com.halloapp.proto.server.Audience;
import com.halloapp.proto.server.AuthRequest;
import com.halloapp.proto.server.AuthResult;
import com.halloapp.proto.server.Avatar;
import com.halloapp.proto.server.ChatRetract;
import com.halloapp.proto.server.ChatStanza;
import com.halloapp.proto.server.ChatState;
import com.halloapp.proto.server.ClientMode;
import com.halloapp.proto.server.ClientVersion;
import com.halloapp.proto.server.Contact;
import com.halloapp.proto.server.ContactHash;
import com.halloapp.proto.server.ContactList;
import com.halloapp.proto.server.ContactSyncError;
import com.halloapp.proto.server.ContentMissing;
import com.halloapp.proto.server.DeliveryReceipt;
import com.halloapp.proto.server.DeviceInfo;
import com.halloapp.proto.server.ErrorStanza;
import com.halloapp.proto.server.ExportData;
import com.halloapp.proto.server.ExternalSharePost;
import com.halloapp.proto.server.FeedItems;
import com.halloapp.proto.server.FriendListRequest;
import com.halloapp.proto.server.FriendshipRequest;
import com.halloapp.proto.server.GeoTagRequest;
import com.halloapp.proto.server.GroupChatRetract;
import com.halloapp.proto.server.GroupChatStanza;
import com.halloapp.proto.server.GroupFeedHistory;
import com.halloapp.proto.server.GroupFeedItem;
import com.halloapp.proto.server.GroupFeedItems;
import com.halloapp.proto.server.GroupFeedRerequest;
import com.halloapp.proto.server.GroupMember;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.HaError;
import com.halloapp.proto.server.HalloappProfileUpdate;
import com.halloapp.proto.server.HalloappUserProfile;
import com.halloapp.proto.server.HistoryResend;
import com.halloapp.proto.server.HomeFeedRerequest;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.Link;
import com.halloapp.proto.server.MomentInfo;
import com.halloapp.proto.server.Msg;
import com.halloapp.proto.server.NoiseMessage;
import com.halloapp.proto.server.Packet;
import com.halloapp.proto.server.Ping;
import com.halloapp.proto.server.PlayedReceipt;
import com.halloapp.proto.server.PostSubscriptionRequest;
import com.halloapp.proto.server.Presence;
import com.halloapp.proto.server.ProfileUpdate;
import com.halloapp.proto.server.RelationshipRequest;
import com.halloapp.proto.server.ReportUserContent;
import com.halloapp.proto.server.Rerequest;
import com.halloapp.proto.server.ScreenshotReceipt;
import com.halloapp.proto.server.SeenReceipt;
import com.halloapp.proto.server.SenderStateBundle;
import com.halloapp.proto.server.SenderStateWithKeyInfo;
import com.halloapp.proto.server.UploadMedia;
import com.halloapp.proto.server.UsernameRequest;
import com.halloapp.proto.server.WebStanza;
import com.halloapp.proto.server.WhisperKeys;
import com.halloapp.ui.ExportDataActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.ThreadUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.util.logs.LogUploaderWorker;
import com.halloapp.util.stats.Counter;
import com.halloapp.util.stats.Stats;
import com.halloapp.xmpp.chat.ChatMessageProtocol;
import com.halloapp.xmpp.feed.FeedContentEncoder;
import com.halloapp.xmpp.feed.FeedContentParser;
import com.halloapp.xmpp.feed.FeedItem;
import com.halloapp.xmpp.feed.FeedUpdateIq;
import com.halloapp.xmpp.feed.GroupFeedUpdateIq;
import com.halloapp.xmpp.feed.SharePosts;
import com.halloapp.xmpp.groups.GroupResponseIq;
import com.halloapp.xmpp.groups.MemberElement;
import com.halloapp.xmpp.privacy.PrivacyList;
import com.halloapp.xmpp.props.ServerPropsRequestIq;
import com.halloapp.xmpp.props.ServerPropsResponseIq;
import com.halloapp.xmpp.util.BackgroundObservable;
import com.halloapp.xmpp.util.ExceptionHandler;
import com.halloapp.xmpp.util.MutableObservable;
import com.halloapp.xmpp.util.Observable;
import com.halloapp.xmpp.util.ResponseHandler;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.ShortBufferException;

public class ConnectionImpl extends Connection {

    private static final String HOST = "s.halloapp.net";
    private static final String DEBUG_HOST = "s-test.halloapp.net";
    private static final int NOISE_PORT = 5222;

    public static final String FEED_THREAD_ID = "feed";

    private static final long READ_TIMEOUT_MS = 150 * DateUtils.SECOND_IN_MILLIS;

    private final Me me;
    private final BgWorkers bgWorkers;
    private final Preferences preferences;
    private final ConnectionObservers connectionObservers;

    private final ConnectionExecutor executor = new ConnectionExecutor();
    private final Executor chatStanzaExecutor = Executors.newSingleThreadExecutor();
    private final Executor groupStanzaExecutor = Executors.newSingleThreadExecutor();
    private final Executor homeStanzaExecutor = Executors.newSingleThreadExecutor();

    public boolean clientExpired = false;
    private HANoiseSocket socket = null;

    private boolean isAuthenticated;

    private final Object startupShutdownLock = new Object();
    private final PacketWriter packetWriter = new PacketWriter();
    private final PacketReader packetReader = new PacketReader();
    private final IqRouter iqRouter = new IqRouter();
    private final MsgRouter msgRouter = new MsgRouter();

    private final SocketConnectorAsync socketConnectorAsync;

    private final FeedContentParser feedContentParser;

    private final Timer timer = new Timer();

    private int iqShortId;

    ConnectionImpl(
            @NonNull Me me,
            @NonNull BgWorkers bgWorkers,
            @NonNull Preferences preferences,
            @NonNull ConnectionObservers connectionObservers) {
        this.me = me;
        this.bgWorkers = bgWorkers;
        this.preferences = preferences;
        this.connectionObservers = connectionObservers;

        this.feedContentParser = new FeedContentParser(me);
        this.socketConnectorAsync = new SocketConnectorAsync(me, bgWorkers, ForegroundObserver.getInstance(), new SocketConnectorAsync.SocketListener() {
            @Override
            public void onConnected(@NonNull HANoiseSocket socket) {
                executor.submit(() -> {
                    ConnectionImpl.this.onSocketConnected(socket);
                });
            }

            @Override
            public boolean isConnected() {
                return ConnectionImpl.this.isConnected();
            }
        });

        randomizeShortId();
    }

    @Override
    public void resetConnectionBackoff() {
        socketConnectorAsync.resetConnectionBackoff();
    }

    @Override
    public Future<Boolean> connect() {
        return executor.submit(this::connectInBackground);
    }

    @WorkerThread
    private boolean connectInBackground() throws ConnectException {
        ThreadUtils.setSocketTag();
        if (me == null) {
            Log.i("connection: me is null");
            throw new ConnectException("connection: me is null");
        }
        if (!me.isRegistered()) {
            Log.i("connection: not registered");
            throw new ConnectException("connection: not registered");
        }
        if (isConnected() && isAuthenticated()) {
            Log.i("connection: already connected");
            return true;
        }
        if (clientExpired) {
            Log.i("connection: expired client");
            throw new ConnectException("Client expired");
        }
        ConnectivityManager connectivityManager
                = (ConnectivityManager) AppContext.getInstance().get().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() == null) {
            Log.i("connection: no network connections available");
            return false;
        }
        if (socketConnectorAsync.isConnecting()) {
            Log.i("connection: currently connecting");
            return true;
        }

        final String host = preferences.getUseDebugHost() ? DEBUG_HOST : HOST;
        Log.i("connection: connecting... " + host + ":" + NOISE_PORT);
        socketConnectorAsync.connect(host, NOISE_PORT);
        return true;
    }

    private void onSocketConnected(@NonNull HANoiseSocket noiseSocket) {
        this.socket = noiseSocket;
        try {
            noiseSocket.initialize(createAuthRequest().toByteArray());
            isAuthenticated = true;
            randomizeShortId();
            synchronized (startupShutdownLock) {
                Log.i("connection: onSocketConnected acquired startup lock");
                packetWriter.init();
                packetReader.init();
                iqRouter.onConnected();
                msgRouter.onConnected();
            }
            Log.i("connection: onSocketConnected finished initialization");
            connectionObservers.notifyConnected();
            isAuthenticated = true;
            socketConnectorAsync.onSocketHandled();
        } catch (Exception e) {
            Log.e("connection: cannot create connection", e);
            socketConnectorAsync.onSocketHandled();
            disconnect();
        }
    }

    @WorkerThread
    private synchronized void randomizeShortId() {
        final int max = 16777216; // 2^24 (3 bytes)
        byte[] bytes = new byte[3];
        new Random().nextBytes(bytes);
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.put(new byte[] {0});
        byteBuffer.put(bytes);
        byteBuffer.rewind();
        iqShortId = byteBuffer.getInt() % max;
    }

    private AuthRequest createAuthRequest() {
        ClientVersion clientVersion = ClientVersion.newBuilder()
                .setVersion(Constants.USER_AGENT)
                .build();
        ClientMode clientMode = ClientMode.newBuilder()
                .setMode(ClientMode.Mode.ACTIVE)
                .build();
        DeviceInfo deviceInfo = DeviceInfo.newBuilder()
                .setDevice(Build.BRAND + ":" + Build.MODEL)
                .setOsVersion(Integer.toString(Build.VERSION.SDK_INT))
                .build();

        AuthRequest.Builder authRequestBuilder = AuthRequest.newBuilder()
                .setUid(Long.parseLong(me.getUser()))
                .setClientVersion(clientVersion)
                .setClientMode(clientMode)
                .setDeviceInfo(deviceInfo)
                .setResource("android");
        return authRequestBuilder.build();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @WorkerThread
    private boolean reconnectIfNeeded() {
        if (socket != null && isConnected() && isAuthenticated()) {
            return true;
        }
        if (me == null) {
            Log.e("connection: cannot reconnect, me is null");
            return false;
        }
        try {
            return connectInBackground();
        } catch (ConnectException e) {
            Log.e("connection: reconnect failed", e);
            return false;
        }
    }

    private void sendPacket(Packet packet) {
        sendPacket(packet, null);
    }

    private void sendPacket(Packet packet, @Nullable PacketCallback packetCallback) {
        if (!reconnectIfNeeded()) {
            Log.e("connection: can't connect; dropping: " + ProtoPrinter.toString(packet));
            if (packetCallback != null) {
                packetCallback.onPacketDropped();
            }
            return;
        }
        packetWriter.sendPacket(packet, packetCallback);
    }

    private void sendMsgInternal(Msg msg, @Nullable MsgCallback callback, boolean resendable) {
        msgRouter.sendMsg(msg, callback, resendable);
    }

    private void sendMsgInternal(Msg msg, @Nullable Runnable ackHandler, boolean resendable) {
        MsgCallback callback;
        if (ackHandler == null) {
            callback = null;
        } else {
            callback = new MsgCallback() {
                @Override
                public void onAck() {
                    ackHandler.run();
                }

                @Override
                public void onTimeout() {

                }
            };
        }
        sendMsgInternal(msg, callback, resendable);
    }

    /**
     * Version of sendMsgInternal that allows for duplicated content ids. This is
     * because currently we use the content id as the id for the msg.
     *
     * Eventually we will migrate these away and this method can be removed
     *
     * TODO (clarkc)
     * @param msg
     * @param ackHandler
     */
    @Deprecated
    private void sendMsgInternalIgnoreDuplicateId(Msg msg, @Nullable Runnable ackHandler) {
        MsgCallback callback;
        if (ackHandler == null) {
            callback = null;
        } else {
            callback = new MsgCallback() {
                @Override
                public void onAck() {
                    ackHandler.run();
                }

                @Override
                public void onTimeout() {

                }
            };
        }
        msgRouter.sendMsg(msg, MsgRouter.DEFAULT_MSG_TIMEOUT_MS, callback, false, true);
    }

    private void sendMsgInternal(Msg msg, @Nullable Runnable ackHandler) {
        sendMsgInternal(msg, ackHandler, false);
    }

    @Override
    public void sendMsg(@NonNull Msg msg, @Nullable MsgCallback callback, long timeout, boolean resendable) {
        executor.execute(() -> {
            msgRouter.sendMsg(msg, timeout, callback, resendable);
        });
    }

    private boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    @Override
    public void clientExpired() {
        clientExpired = true;
        disconnect();
        connectionObservers.notifyClientVersionExpiringSoon(0);
    }

    public void disconnect() {
        shutdownComponents();
        int canceled = executor.reset();
        Log.i("connection: disconnect canceled " + canceled + " tasks in executor queue");
        executor.execute(this::disconnectInBackground);
    }


    @WorkerThread
    private void disconnectInBackground() {
        if (socket == null) {
            Log.e("connection: cannot disconnect, no connection");
            return;
        }
        if (isConnected()) {
            Log.i("connection: disconnecting");
            try {
                socket.close();
            } catch (IOException e) {
                Log.w("Failed to close socket", e);
            }
        }
        socket = null;

        connectionObservers.notifyDisconnected();
    }

    @WorkerThread
    private void shutdownComponents() {
        Log.i("Shutting down packet handlers");
        synchronized (startupShutdownLock) {
            packetWriter.shutdown();
            packetReader.shutdown();
            iqRouter.onDisconnected();
            msgRouter.onDisconnected();
        }
    }

    @Override
    public void requestServerProps() {
        ServerPropsRequestIq requestIq = new ServerPropsRequestIq();
        sendIqRequestAsync(requestIq)
                .onResponse(response -> {
                    ServerPropsResponseIq responseIq = ServerPropsResponseIq.fromProto(response.getProps());
                    connectionObservers.notifyServerPropsReceived(responseIq.getProps(), responseIq.getHash());
                }).onError(e -> Log.e("connection: failed to get server props", e));
    }

    @Override
    public Observable<Integer> requestSecondsToExpiration() {
        final SecondsToExpirationIq secondsToExpirationIq = new SecondsToExpirationIq();
        return sendIqRequestAsync(secondsToExpirationIq).map(r -> SecondsToExpirationIq.fromProto(r.getClientVersion()).secondsLeft);
    }

    @Override
    public Observable<MediaUploadIq.Urls> requestMediaUpload(long fileSize, @Nullable String downloadUrl, @Nullable UploadMedia.Type type) {
        final MediaUploadIq mediaUploadIq = new MediaUploadIq(fileSize, downloadUrl, type);
        return sendIqRequestAsync(mediaUploadIq).map(response -> MediaUploadIq.fromProto(response.getUploadMedia()).urls);
    }

    @Override
    public Observable<ContactSyncResult> syncContacts(@Nullable Collection<String> addPhones, @Nullable Collection<String> deletePhones, boolean fullSync, @Nullable String syncId, int index, boolean lastBatch) {
        final MutableObservable<ContactSyncResult> result = new MutableObservable<>();
        final ContactsSyncRequestIq contactsSyncIq = new ContactsSyncRequestIq(
                addPhones, deletePhones, fullSync, syncId, index, lastBatch);

        sendIqRequestAsync(contactsSyncIq)
                .onResponse(response -> {
                    List<Contact> contacts = response.getContactList().getContactsList();
                    result.setResponse(ContactSyncResult.success(contacts));
                })
                .onError(exception -> {
                    if (!(exception instanceof IqErrorException)) {
                        result.setException(exception);
                        return;
                    }
                    IqErrorException iqErrorException = (IqErrorException) exception;
                    if (iqErrorException.getErrorIq() == null) {
                        result.setException(exception);
                        return;
                    }
                    Iq errorIq = iqErrorException.getErrorIq();
                    if (!errorIq.hasContactSyncError()) {
                        result.setException(exception);
                        return;
                    }
                    ContactSyncError syncError = errorIq.getContactSyncError();
                    result.setResponse(ContactSyncResult.failure(syncError.getRetryAfterSecs()));
                });

        return result;
    }

    @Override
    public void sendPushToken(@NonNull String pushToken, @NonNull String languageCode, long timeZoneOffset) {
        final PushRegisterRequestIq pushIq = new PushRegisterRequestIq(pushToken, languageCode, timeZoneOffset, false);
        sendIqRequestAsync(pushIq)
                .onResponse(response -> {
                    Log.d("connection: response after setting the push token " + ProtoPrinter.toString(response));
                    preferences.setLastPushToken(pushToken);
                    preferences.setLastDeviceLocale(languageCode);
                    preferences.setLastTimeZoneOffset(timeZoneOffset);
                    preferences.setLastPushTokenSyncTime(System.currentTimeMillis());
                })
                .onError(e -> Log.e("connection: cannot send push token", e));
    }

    @Override
    public void sendHuaweiPushToken(@NonNull String pushToken, @NonNull String languageCode, long timeZoneOffset) {
        final PushRegisterRequestIq pushIq = new PushRegisterRequestIq(pushToken, languageCode, timeZoneOffset, true);
        sendIqRequestAsync(pushIq)
                .onResponse(response -> {
                    Log.d("connection: response after setting the huawei push token " + ProtoPrinter.toString(response));
                    preferences.setLastHuaweiPushToken(pushToken);
                    preferences.setLastDeviceLocale(languageCode);
                    preferences.setLastTimeZoneOffset(timeZoneOffset);
                    preferences.setLastHuaweiPushTokenSyncTime(System.currentTimeMillis());
                })
                .onError(e -> Log.e("connection: cannot send huawei push token", e));
    }

    @Override
    public Observable<Void> sendName(@NonNull String name) {
        final UserNameIq nameIq = new UserNameIq(name);
        return sendIqRequestAsync(nameIq).map(r -> {
            Log.d("connection: response after setting name " + ProtoPrinter.toString(r));
            return null;
        });
    }

    @Override
    public void subscribePresence(UserId userId) {
        executor.execute(() -> {
            Presence.Builder builder = Presence.newBuilder();
            builder.setId(getAndIncrementShortId());
            builder.setType(Presence.Type.SUBSCRIBE);
            if (userId != null) {
                builder.setToUid(Long.parseLong(userId.rawId()));
            }
            sendPacket(Packet.newBuilder().setPresence(builder).build());
        });
    }

    @Override
    public void updatePresence(boolean available) {
        executor.execute(() -> {
            Presence.Builder builder = Presence.newBuilder();
            builder.setId(getAndIncrementShortId());
            builder.setType(available ? Presence.Type.AVAILABLE : Presence.Type.AWAY);

            sendPacket(Packet.newBuilder().setPresence(builder).build());
        });
    }

    @Override
    public void updateChatState(@NonNull ChatId chat, int state) {
        executor.execute(() -> {
            ChatState.ThreadType threadType;
            if (chat instanceof UserId) {
                threadType = ChatState.ThreadType.CHAT;
            } else if (chat instanceof GroupId) {
                threadType = ChatState.ThreadType.GROUP_CHAT;
            } else {
                threadType = ChatState.ThreadType.CHAT;
                Log.e("ChatStateStanza invalid type of chat id for chat state");
            }
            ChatState chatState = ChatState.newBuilder()
                    .setType(state == com.halloapp.xmpp.ChatState.Type.TYPING ? ChatState.Type.TYPING : ChatState.Type.AVAILABLE)
                    .setThreadId(chat.rawId())
                    .setThreadType(threadType)
                    .build();
            sendPacket(Packet.newBuilder().setChatState(chatState).build());
        });
    }

    @Override
    public void uploadMoreOneTimePreKeys(@NonNull List<byte[]> oneTimePreKeys) {
        final WhisperKeysUploadIq uploadIq = new WhisperKeysUploadIq(oneTimePreKeys);
        sendIqRequestAsync(uploadIq).map(response -> {
            Log.d("connection: response after uploading keys " + ProtoPrinter.toString(response));
            return null;
        });
    }

    @Override
    public Observable<WhisperKeysResponseIq> downloadKeys(@NonNull UserId userId) {
        final WhisperKeysDownloadIq downloadIq = new WhisperKeysDownloadIq(userId.rawId(), userId);
        return sendIqRequestAsync(downloadIq).map(response -> {
            Log.d("connection: response after downloading keys " + ProtoPrinter.toString(response));
            return WhisperKeysResponseIq.fromProto(response.getWhisperKeys());
        });
    }

    @Override
    public Observable<Void> sendEvents(Collection<EventData> events) {
        final EventsIq eventsIq = new EventsIq(events);
        return sendIqRequestAsync(eventsIq, true).map(r -> null);
    }

    @Override
    public void sendStats(List<Counter> counters) {
        final StatsIq statsIq = new StatsIq(counters);
        sendIqRequestAsync(statsIq)
                .onResponse(response -> {
                    Log.d("connection: response for send stats  " + ProtoPrinter.toString(response));
                })
                .onError(e -> Log.e("connection: cannot send stats", e));
    }

    @Override
    public Observable<String> setAvatar(byte[] bytes, byte[] largeBytes) {
        final AvatarIq avatarIq = new AvatarIq(bytes, largeBytes);
        return sendIqRequestAsync(avatarIq).map(res -> AvatarIq.fromProto(res.getAvatar()).avatarId);
    }

    @Override
    public Observable<String> removeAvatar() {
        return setAvatar(new byte[] {}, new byte[] {});
    }

    @Override
    public Observable<String> setGroupAvatar(GroupId groupId, byte[] bytes, byte[] largeBytes) {
        final GroupAvatarIq avatarIq = new GroupAvatarIq(groupId, bytes, largeBytes);
        return sendIqRequestAsync(avatarIq).map(res -> GroupResponseIq.fromProto(res.getGroupStanza()).avatar);
    }

    @Override
    public Observable<String> removeGroupAvatar(GroupId groupId) {
        return setGroupAvatar(groupId, new byte[] {}, new byte[] {});
    }

    @Override
    public Observable<String> getAvatarId(UserId userId) {
        final AvatarIq setAvatarIq = new AvatarIq(userId);
        return sendIqRequestAsync(setAvatarIq).map(res -> AvatarIq.fromProto(res.getAvatar()).avatarId);
    }

    @Override
    public Observable<String> getMyAvatarId() {
        final AvatarIq getAvatarIq = new AvatarIq(new UserId(me.getUser()));
        return sendIqRequestAsync(getAvatarIq).map(response -> response.getAvatar().getId());
    }

    @Override
    public Observable<Void> sharePosts(Map<UserId, Collection<Post>> shareMap) {
        List<SharePosts> sharePosts = new ArrayList<>();
        for (UserId user : shareMap.keySet()) {
            Collection<Post> postsToShare = shareMap.get(user);
            if (postsToShare == null) {
                continue;
            }
            ArrayList<FeedItem> itemList = new ArrayList<>(postsToShare.size());
            for (Post post : postsToShare) {
                MediaCounts mediaCounts = new MediaCounts(post.media);
                FeedItem sharedPost = new FeedItem(FeedItem.Type.POST, post.id, null, mediaCounts);
                itemList.add(sharedPost);
            }
            sharePosts.add(new SharePosts(user, itemList));
        }
        FeedUpdateIq updateIq = new FeedUpdateIq(sharePosts);
        return sendIqRequestAsync(updateIq, true).map(r -> null);
    }

    @Override
    public void sendPost(@NonNull Post post) {
        MediaCounts mediaCounts = new MediaCounts(post.media);

        Container.Builder containerBuilder = Container.newBuilder();
        FeedContentEncoder.encodePost(containerBuilder, post);
        if (BuildConfig.IS_KATCHUP) {
            if (!containerBuilder.hasKMomentContainer()) {
                Log.e("connection: sendPost no post content");
                return;
            }
        } else {
            if (!containerBuilder.hasPostContainer()) {
                Log.e("connection: sendPost no post content");
                return;
            }
        }

        bgWorkers.execute(() -> {
            HalloIq publishIq;
            byte[] payload = containerBuilder.build().toByteArray();
            final byte[] protoHash = CryptoUtils.sha256(payload);

            if (post.getParentGroup() == null) {
                byte[] encPayload = null;
                List<SenderStateBundle> senderStateBundles = new ArrayList<>();

                Stats stats = Stats.getInstance();
                try {
                    boolean favorites = PrivacyList.Type.ONLY.equals(post.getAudienceType());
                    HomePostSetupInfo homePostSetupInfo = HomeFeedSessionManager.getInstance().ensureSetUp(favorites);
                    senderStateBundles = homePostSetupInfo.senderStateBundles;
                    encPayload = HomeFeedSessionManager.getInstance().encryptPost(payload, favorites);
                    stats.reportHomeEncryptSuccess(false);
                } catch (CryptoException e) {
                    String errorMessage = e.getMessage();
                    Log.e("Failed to encrypt home post", e);
                    Log.sendErrorReport("Home post encrypt failed: " + errorMessage);
                    stats.reportHomeEncryptError(errorMessage, false);
                    return;
                }

                FeedItem feedItem = new FeedItem(FeedItem.Type.POST, post.id, payload, encPayload, senderStateBundles, null, mediaCounts);

                FeedUpdateIq updateIq = new FeedUpdateIq(FeedUpdateIq.Action.PUBLISH, feedItem);
                if (post instanceof KatchupPost) {
                    updateIq.setKatchupPost((KatchupPost) post);
                }
                updateIq.setPostAudience(post.getAudienceType(), post.getAudienceList());
                if (post.type == Post.TYPE_KATCHUP) {
                    updateIq.setTag(com.halloapp.proto.server.Post.Tag.PUBLIC_MOMENT);
                } else if (post.type == Post.TYPE_MOMENT) {
                    updateIq.setTag(com.halloapp.proto.server.Post.Tag.MOMENT);
                    if (post instanceof MomentPost) {
                        updateIq.setUnlockMomentUserId(((MomentPost) post).unlockedUserId);
                    }
                } else if (post.type == Post.TYPE_MOMENT_PSA) {
                    updateIq.setTag(com.halloapp.proto.server.Post.Tag.MOMENT);
                    updateIq.setPsaTag(post.psaTag);
                }
                publishIq = updateIq;
            } else {
                GroupId groupId = post.getParentGroup();
                byte[] encPayload = null;
                List<SenderStateBundle> senderStateBundles = new ArrayList<>();
                byte[] audienceHash = null;

                Stats stats = Stats.getInstance();
                try {
                    GroupSetupInfo groupSetupInfo = GroupFeedSessionManager.getInstance().ensureGroupSetUp(groupId);
                    senderStateBundles = groupSetupInfo.senderStateBundles;
                    audienceHash = groupSetupInfo.audienceHash;
                    encPayload = GroupFeedSessionManager.getInstance().encryptMessage(payload, groupId);
                    stats.reportGroupPostEncryptSuccess();
                } catch (CryptoException e) {
                    String errorMessage = e.getMessage();
                    Log.e("Failed to encrypt group post", e);
                    Log.sendErrorReport("Group post encrypt failed: " + errorMessage);
                    stats.reportGroupPostEncryptError(errorMessage);
                    return;
                } catch (NoSuchAlgorithmException e) {
                    String errorMessage = "no_such_algo";
                    Log.e("Failed to calculate audience hash", e);
                    Log.sendErrorReport("Group post encrypt failed: " + errorMessage);
                    stats.reportGroupPostEncryptError(errorMessage);
                    return;
                }

                FeedItem feedItem = new FeedItem(FeedItem.Type.POST, post.id, payload, encPayload, senderStateBundles, audienceHash, mediaCounts);
                GroupFeedUpdateIq groupFeedUpdateIq = new GroupFeedUpdateIq(post.getParentGroup(), GroupFeedUpdateIq.Action.PUBLISH, feedItem);
                groupFeedUpdateIq.expiryTimestamp = post.expirationTime / 1000;
                publishIq = groupFeedUpdateIq;
            }
            sendIqRequestAsync(publishIq, true)
                    .onResponse(response -> {
                        connectionObservers.notifyOutgoingPostSent(post.id, protoHash);
                        WebClientManager.getInstance().sendFeedUpdate(post, false);
                    })
                    .onError(e -> {
                        Log.e("connection: cannot send post", e);
                        if (e instanceof IqErrorException) {
                            String reason = ((IqErrorException) e).getReason();
                            if ("audience_hash_mismatch".equals(reason)) {
                                connectionObservers.notifyAudienceHashMismatch(post);
                            }
                        }
                    });
        });
    }

    @Override
    public void sendRerequestedGroupPost(@NonNull Post post, @NonNull UserId userId) {
        Container.Builder containerBuilder = Container.newBuilder();
        FeedContentEncoder.encodePost(containerBuilder, post);
        if (!containerBuilder.hasPostContainer()) {
            Log.e("connection: sendRerequestedGroupPost no post content");
            return;
        }

        GroupId groupId = post.getParentGroup();
        SignalSessionSetupInfo signalSessionSetupInfo = null;
        try {
            signalSessionSetupInfo = SignalSessionManager.getInstance().getSessionSetupInfo(userId);
        } catch (Exception e) {
            Log.e("connection: sendRerequestedGroupPost failed to get session setup info for group post rerequest", e);
            return;
        }

        try {
            GroupSetupInfo groupSetupInfo = GroupFeedSessionManager.getInstance().ensureGroupSetUp(groupId);
        } catch (Exception e) {
            Log.e("connection: sendRerequestedGroupPost failed to get group session setup info for group post rerequest", e);
            return;
        }

        SenderStateWithKeyInfo.Builder senderStateWithKeyInfoBuilder = SenderStateWithKeyInfo.newBuilder();
        try {
            SenderState senderState = GroupFeedSessionManager.getInstance().getSenderState(groupId);
            byte[] encSenderState = SignalSessionManager.getInstance().encryptMessage(senderState.toByteArray(), userId);
            senderStateWithKeyInfoBuilder.setEncSenderState(ByteString.copyFrom(encSenderState));
            if (signalSessionSetupInfo != null) {
                senderStateWithKeyInfoBuilder.setPublicKey(ByteString.copyFrom(signalSessionSetupInfo.identityKey.getKeyMaterial()));
                if (signalSessionSetupInfo.oneTimePreKeyId != null) {
                    senderStateWithKeyInfoBuilder.setOneTimePreKeyId(signalSessionSetupInfo.oneTimePreKeyId);
                }
            }
        } catch (CryptoException e) {
            Log.e("connection: sendRerequestedGroupPost failed to encrypt sender state for group post rerequest", e);
        }

        executor.execute(() -> {
            try {
                GroupFeedItem.Builder builder = GroupFeedItem.newBuilder();
                builder.setAction(GroupFeedItem.Action.PUBLISH);
                builder.setGid(groupId.rawId());
                builder.setSenderClientVersion(Constants.USER_AGENT);
                builder.setSenderState(senderStateWithKeyInfoBuilder.build());

                byte[] payload = containerBuilder.build().toByteArray();
                byte[] encPayload = SignalSessionManager.getInstance().encryptMessage(payload, userId);
                com.halloapp.proto.server.Post.Builder pb = com.halloapp.proto.server.Post.newBuilder();
                if (ServerProps.getInstance().getSendPlaintextGroupFeed()) {
                    pb.setPayload(ByteString.copyFrom(payload));
                }
                EncryptedPayload encryptedPayload = EncryptedPayload.newBuilder()
                        .setOneToOneEncryptedPayload(ByteString.copyFrom(encPayload))
                        .build();
                pb.setEncPayload(ByteString.copyFrom(encryptedPayload.toByteArray()));
                pb.setId(post.id);
                pb.setTimestamp(post.timestamp / 1000);
                pb.setPublisherUid(Long.parseLong(me.getUser()));
                builder.setPost(pb);

                Msg msg = Msg.newBuilder()
                        .setId(post.id)
                        .setType(Msg.Type.GROUPCHAT)
                        .setToUid(Long.parseLong(userId.rawId()))
                        .setGroupFeedItem(builder.build())
                        .setRerequestCount(ContentDb.getInstance().getOutboundPostRerequestCount(userId, post.id))
                        .build();
                sendMsgInternalIgnoreDuplicateId(msg, () -> Log.i("group post rerequest acked " + post.id));
            } catch (CryptoException e) {
                Log.e("Failed to send rerequested group post", e);
            }
        });
    }

    @Override
    public void sendRerequestedHistoryResend(@NonNull HistoryResend.Builder historyResend, @NonNull UserId userId) {
        GroupId groupId = new GroupId(historyResend.getGid());
        SignalSessionSetupInfo signalSessionSetupInfo = null;
        try {
            signalSessionSetupInfo = SignalSessionManager.getInstance().getSessionSetupInfo(userId);
        } catch (Exception e) {
            Log.e("connection: sendRerequestedHistoryResend failed to get session setup info for group history resend rerequest", e);
            return;
        }

        try {
            GroupSetupInfo groupSetupInfo = GroupFeedSessionManager.getInstance().ensureGroupSetUp(groupId);
        } catch (Exception e) {
            Log.e("connection: sendRerequestedHistoryResend failed to get group session setup info for group history resend rerequest", e);
            return;
        }

        SenderStateWithKeyInfo.Builder senderStateWithKeyInfoBuilder = SenderStateWithKeyInfo.newBuilder();
        try {
            SenderState senderState = GroupFeedSessionManager.getInstance().getSenderState(groupId);
            byte[] encSenderState = SignalSessionManager.getInstance().encryptMessage(senderState.toByteArray(), userId);
            senderStateWithKeyInfoBuilder.setEncSenderState(ByteString.copyFrom(encSenderState));
            if (signalSessionSetupInfo != null) {
                senderStateWithKeyInfoBuilder.setPublicKey(ByteString.copyFrom(signalSessionSetupInfo.identityKey.getKeyMaterial()));
                if (signalSessionSetupInfo.oneTimePreKeyId != null) {
                    senderStateWithKeyInfoBuilder.setOneTimePreKeyId(signalSessionSetupInfo.oneTimePreKeyId);
                }
            }
        } catch (CryptoException e) {
            Log.e("connection: sendRerequestedHistoryResend failed to encrypt sender state for group history resend rerequest", e);
        }
        historyResend.setSenderState(senderStateWithKeyInfoBuilder);

        executor.execute(() -> {
            Msg msg = Msg.newBuilder().setToUid(Long.parseLong(userId.rawId())).setId(historyResend.getId()).setHistoryResend(historyResend).build();
            sendMsgInternalIgnoreDuplicateId(msg, () -> Log.i("group history resend rerequest acked " + historyResend.getId()));
        });
    }

    @Override
    public void sendRerequestedHomePost(@NonNull Post post, @NonNull UserId userId) {
        Container.Builder containerBuilder = Container.newBuilder();
        FeedContentEncoder.encodePost(containerBuilder, post);
        if (!containerBuilder.hasPostContainer()) {
            Log.e("connection: sendRerequestedHomePost no post content");
            return;
        }

        SignalSessionSetupInfo signalSessionSetupInfo = null;
        try {
            signalSessionSetupInfo = SignalSessionManager.getInstance().getSessionSetupInfo(userId);
        } catch (Exception e) {
            Log.e("connection: sendRerequestedHomePost failed to get session setup info for home post rerequest", e);
            return;
        }

        boolean favorites = PrivacyList.Type.ONLY.equals(post.getAudienceType());
        SenderStateWithKeyInfo.Builder senderStateWithKeyInfoBuilder = SenderStateWithKeyInfo.newBuilder();
        try {
            SenderState senderState = HomeFeedSessionManager.getInstance().getSenderState(favorites);
            byte[] encSenderState = SignalSessionManager.getInstance().encryptMessage(senderState.toByteArray(), userId);
            senderStateWithKeyInfoBuilder.setEncSenderState(ByteString.copyFrom(encSenderState));
            if (signalSessionSetupInfo != null) {
                senderStateWithKeyInfoBuilder.setPublicKey(ByteString.copyFrom(signalSessionSetupInfo.identityKey.getKeyMaterial()));
                if (signalSessionSetupInfo.oneTimePreKeyId != null) {
                    senderStateWithKeyInfoBuilder.setOneTimePreKeyId(signalSessionSetupInfo.oneTimePreKeyId);
                }
            }
        } catch (CryptoException e) {
            Log.e("connection: sendRerequestedHomePost failed to encrypt sender state for home post rerequest", e);
        }

        executor.execute(() -> {
            try {
                com.halloapp.proto.server.FeedItem.Builder builder = com.halloapp.proto.server.FeedItem.newBuilder();
                builder.setSenderClientVersion(Constants.USER_AGENT);
                builder.setAction(com.halloapp.proto.server.FeedItem.Action.PUBLISH);
                builder.setSenderState(senderStateWithKeyInfoBuilder.build());

                byte[] payload = containerBuilder.build().toByteArray();
                byte[] encPayload = SignalSessionManager.getInstance().encryptMessage(payload, userId);
                com.halloapp.proto.server.Post.Builder pb = com.halloapp.proto.server.Post.newBuilder();
                if (ServerProps.getInstance().getSendPlaintextHomeFeed()) {
                    pb.setPayload(ByteString.copyFrom(payload));
                }
                EncryptedPayload encryptedPayload = EncryptedPayload.newBuilder()
                        .setOneToOneEncryptedPayload(ByteString.copyFrom(encPayload))
                        .build();
                pb.setEncPayload(ByteString.copyFrom(encryptedPayload.toByteArray()));
                pb.setId(post.id);
                pb.setTimestamp(post.timestamp / 1000);
                pb.setPublisherUid(Long.parseLong(me.getUser()));
                pb.setAudience(Audience.newBuilder().setType(post.getAudienceType().equals(PrivacyList.Type.ONLY) ? Audience.Type.ONLY : Audience.Type.ALL).build());
                builder.setPost(pb);

                Msg msg = Msg.newBuilder()
                        .setId(post.id)
                        .setType(Msg.Type.CHAT)
                        .setToUid(Long.parseLong(userId.rawId()))
                        .setFeedItem(builder.build())
                        .setRerequestCount(ContentDb.getInstance().getOutboundPostRerequestCount(userId, post.id))
                        .build();
                sendMsgInternalIgnoreDuplicateId(msg, () -> Log.i("home post rerequest acked " + post.id));
            } catch (CryptoException e) {
                Log.e("Failed to send rerequested home post", e);
            }
        });
    }

    @Override
    public void sendRerequestedHomeComment(@NonNull Comment comment, @NonNull UserId userId) {
        Container.Builder containerBuilder = Container.newBuilder();
        FeedContentEncoder.encodeComment(containerBuilder, comment);
        if (!containerBuilder.hasCommentContainer()) {
            Log.e("connection: sendRerequestedHomeComment no comment content");
            return;
        }

        executor.execute(() -> {
            try {
                com.halloapp.proto.server.FeedItem.Builder builder = com.halloapp.proto.server.FeedItem.newBuilder();
                builder.setSenderClientVersion(Constants.USER_AGENT);
                builder.setAction(com.halloapp.proto.server.FeedItem.Action.PUBLISH);

                byte[] payload = containerBuilder.build().toByteArray();
                byte[] encPayload = HomeFeedSessionManager.getInstance().encryptComment(payload, comment.postId);
                com.halloapp.proto.server.Comment.Builder cb = com.halloapp.proto.server.Comment.newBuilder();
                if (ServerProps.getInstance().getSendPlaintextHomeFeed()) {
                    cb.setPayload(ByteString.copyFrom(payload));
                }
                EncryptedPayload encryptedPayload = EncryptedPayload.newBuilder()
                        .setCommentKeyEncryptedPayload(ByteString.copyFrom(encPayload))
                        .build();
                cb.setEncPayload(ByteString.copyFrom(encryptedPayload.toByteArray()));
                if (comment.parentCommentId != null) {
                    cb.setParentCommentId(comment.parentCommentId);
                }
                cb.setPostId(comment.postId);
                cb.setId(comment.id);
                cb.setTimestamp(comment.timestamp / 1000);
                cb.setPublisherUid(Long.parseLong(comment.senderUserId.isMe() ? me.getUser() : comment.senderUserId.rawId()));
                builder.setComment(cb);

                Msg msg = Msg.newBuilder()
                        .setId(comment.id)
                        .setType(Msg.Type.CHAT)
                        .setToUid(Long.parseLong(userId.rawId()))
                        .setFeedItem(builder.build())
                        .setRerequestCount(ContentDb.getInstance().getOutboundCommentRerequestCount(userId, comment.id))
                        .build();
                sendMsgInternalIgnoreDuplicateId(msg, () -> Log.i("home comment rerequest acked " + comment.id));
            } catch (CryptoException e) {
                Log.e("Failed to send rerequested home comment", e);
            }
        });
    }

    @Override
    public void retractPost(@NonNull String postId) {
        FeedUpdateIq requestIq = new FeedUpdateIq(FeedUpdateIq.Action.RETRACT, new FeedItem(FeedItem.Type.POST, postId, null, null));
        sendIqRequestAsync(requestIq, true)
                .onResponse(response -> connectionObservers.notifyOutgoingPostSent(postId, null))
                .onError(e -> Log.e("connection: cannot retract post", e));
    }

    @Override
    public void retractRerequestedPost(@NonNull String postId, @NonNull UserId peerUserId) {
        Msg msg = Msg.newBuilder()
                .setId(RandomId.create())
                .setToUid(peerUserId.rawIdLong())
                .setFeedItem(com.halloapp.proto.server.FeedItem.newBuilder()
                        .setAction(com.halloapp.proto.server.FeedItem.Action.RETRACT)
                        .setPost(com.halloapp.proto.server.Post.newBuilder().setId(postId).setPublisherUid(Long.parseLong(Me.getInstance().getUser())).setPublisherName(Me.getInstance().getName()).build()))
                .build();
        sendMsgInternal(msg, null);
    }

    @Override
    public void retractGroupPost(@NonNull GroupId groupId, @NonNull String postId) {
        GroupFeedUpdateIq requestIq = new GroupFeedUpdateIq(groupId, GroupFeedUpdateIq.Action.RETRACT, new FeedItem(FeedItem.Type.POST, postId, null, null));
        sendIqRequestAsync(requestIq, true)
                .onResponse(response -> connectionObservers.notifyOutgoingPostSent(postId, null))
                .onError(e -> Log.e("connection: cannot retract post", e));
    }

    @Override
    public void retractRerequestedGroupPost(@NonNull GroupId groupId, @NonNull String postId, @NonNull UserId peerUserId) {
        Msg msg = Msg.newBuilder()
                .setId(RandomId.create())
                .setToUid(peerUserId.rawIdLong())
                .setGroupFeedItem(com.halloapp.proto.server.GroupFeedItem.newBuilder()
                        .setAction(com.halloapp.proto.server.GroupFeedItem.Action.RETRACT)
                        .setGid(groupId.rawId())
                        .setPost(com.halloapp.proto.server.Post.newBuilder().setId(postId).setPublisherUid(Long.parseLong(Me.getInstance().getUser())).setPublisherName(Me.getInstance().getName()).build()))
                .build();
        sendMsgInternal(msg, null);
    }

    @Override
    public void sendComment(@NonNull Comment comment) {
        byte[] payload = FeedContentEncoder.encodeComment(comment);
        final byte[] protoHash = CryptoUtils.sha256(payload);

        MediaCounts mediaCounts = new MediaCounts(comment.media);

        Stats stats = Stats.getInstance();
        HalloIq requestIq;
        @FeedItem.Type int type = comment instanceof ReactionComment || comment.type == Comment.TYPE_VIDEO_REACTION ? !TextUtils.isEmpty(comment.parentCommentId) ? FeedItem.Type.COMMENT_REACTION : FeedItem.Type.POST_REACTION : FeedItem.Type.COMMENT;
        if (comment.getParentPost() == null || comment.getParentPost().getParentGroup() == null) {
            byte[] encPayload = null;

            try {
                encPayload = HomeFeedSessionManager.getInstance().encryptComment(payload, comment.postId);
                stats.reportHomeEncryptSuccess(true);
            } catch (CryptoException e) {
                String errorMessage = e.getMessage();
                Log.e("Failed to encrypt home comment", e);
                Log.sendErrorReport("Home comment encrypt failed: " + errorMessage);
                stats.reportHomeEncryptError(errorMessage, true);
            }

            FeedItem commentItem = new FeedItem(type, comment.id, comment.postId, payload, encPayload, mediaCounts);
            commentItem.parentCommentId = comment.parentCommentId;
            requestIq = new FeedUpdateIq(FeedUpdateIq.Action.PUBLISH, commentItem);
        } else {
            Post parentPost = comment.getParentPost();
            GroupId groupId = parentPost.getParentGroup();

            byte[] encPayload = null;
            List<SenderStateBundle> senderStateBundles = new ArrayList<>();
            byte[] audienceHash = null;

            try {
                GroupSetupInfo groupSetupInfo = GroupFeedSessionManager.getInstance().ensureGroupSetUp(groupId);
                senderStateBundles = groupSetupInfo.senderStateBundles;
                audienceHash = groupSetupInfo.audienceHash;
                encPayload = GroupFeedSessionManager.getInstance().encryptMessage(payload, groupId);
                stats.reportGroupCommentEncryptSuccess();
            } catch (CryptoException e) {
                String errorMessage = e.getMessage();
                Log.e("Failed to encrypt group comment", e);
                Log.sendErrorReport("Group comment encrypt failed: " + errorMessage);
                stats.reportGroupCommentEncryptError(errorMessage);
                return;
            } catch (NoSuchAlgorithmException e) {
                String errorMessage = "no_such_algo";
                Log.e("Failed to calculate audience hash", e);
                Log.sendErrorReport("Group comment encrypt failed: " + errorMessage);
                stats.reportGroupCommentEncryptError(errorMessage);
                return;
            }

            FeedItem feedItem = new FeedItem(type, comment.id, parentPost.id, payload, encPayload, senderStateBundles, audienceHash, mediaCounts);
            feedItem.parentCommentId = comment.parentCommentId;
            requestIq = new GroupFeedUpdateIq(groupId, GroupFeedUpdateIq.Action.PUBLISH, feedItem);
        }
        sendIqRequestAsync(requestIq, true)
                .onResponse(response -> {
                    if (comment instanceof ReactionComment) {
                        ContentDb.getInstance().markReactionSent(((ReactionComment) comment).reaction);
                    } else {
                        connectionObservers.notifyOutgoingCommentSent(comment.postId, comment.id, protoHash);
                        WebClientManager.getInstance().sendFeedUpdate(comment, false);
                    }
                })
                .onError(e -> {
                    Log.e("connection: cannot send comment", e);
                    if (e instanceof IqErrorException) {
                        String reason = ((IqErrorException) e).getReason();
                        if ("audience_hash_mismatch".equals(reason)) {
                            connectionObservers.notifyAudienceHashMismatch(comment);
                        }
                    }
                });
    }

    @Override
    public void sendRerequestedGroupMessage(@NonNull Message message, @NonNull UserId userId) {
        if (message.isLocalMessage()) {
            Log.i("connection: System message shouldn't be sent");
            return;
        }
        Msg msg = Msg.newBuilder()
                .setId(message.id)
                .setType(Msg.Type.GROUPCHAT)
                .setToUid(Long.parseLong(userId.rawId()))
                .setGroupChatStanza(ChatMessageProtocol.getInstance().serializeGroupMessageRerequest(message, userId))
                .setRerequestCount(ContentDb.getInstance().getOutboundMessageRerequestCount(userId, message.id))
                .build();
        executor.execute(() -> {
            if (message.isLocalMessage()) {
                Log.i("connection: System message shouldn't be sent");
                return;
            }
            sendMsgInternalIgnoreDuplicateId(msg, () -> connectionObservers.notifyOutgoingMessageSent(message.chatId, message.id));
        });
    }

    @Override
    public void sendRerequestedGroupComment(@NonNull Comment comment, @NonNull UserId userId) {
        Container.Builder containerBuilder = Container.newBuilder();
        FeedContentEncoder.encodeComment(containerBuilder, comment);
        if (!containerBuilder.hasCommentContainer()) {
            Log.e("connection: sendRerequestedGroupComment no comment content");
            return;
        }

        GroupId groupId = comment.getParentPost().getParentGroup();
        SignalSessionSetupInfo signalSessionSetupInfo;
        try {
            signalSessionSetupInfo = SignalSessionManager.getInstance().getSessionSetupInfo(userId);
        } catch (Exception e) {
            Log.e("connection: sendRerequestedGroupComment failed to get setup info", e);
            return;
        }

        try {
            GroupSetupInfo groupSetupInfo = GroupFeedSessionManager.getInstance().ensureGroupSetUp(groupId);
        } catch (Exception e) {
            Log.e("connection: sendRerequestedGroupComment failed to get group session setup info", e);
            return;
        }

        SenderStateWithKeyInfo.Builder senderStateWithKeyInfoBuilder = SenderStateWithKeyInfo.newBuilder();
        try {
            SenderState senderState = GroupFeedSessionManager.getInstance().getSenderState(groupId);
            byte[] encSenderState = SignalSessionManager.getInstance().encryptMessage(senderState.toByteArray(), userId);
            senderStateWithKeyInfoBuilder.setEncSenderState(ByteString.copyFrom(encSenderState));
            if (signalSessionSetupInfo != null) {
                senderStateWithKeyInfoBuilder.setPublicKey(ByteString.copyFrom(signalSessionSetupInfo.identityKey.getKeyMaterial()));
                if (signalSessionSetupInfo.oneTimePreKeyId != null) {
                    senderStateWithKeyInfoBuilder.setOneTimePreKeyId(signalSessionSetupInfo.oneTimePreKeyId);
                }
            }
        } catch (CryptoException e) {
            Log.e("connection: sendRerequestedGroupComment failed to encrypt sender state", e);
        }

        executor.execute(() -> {
            try {
                GroupFeedItem.Builder builder = GroupFeedItem.newBuilder();
                builder.setAction(GroupFeedItem.Action.PUBLISH);
                builder.setGid(groupId.rawId());
                builder.setSenderClientVersion(Constants.USER_AGENT);
                builder.setSenderState(senderStateWithKeyInfoBuilder.build());

                byte[] payload = containerBuilder.build().toByteArray();
                byte[] encPayload = SignalSessionManager.getInstance().encryptMessage(payload, userId);

                com.halloapp.proto.server.Comment.Builder cb = com.halloapp.proto.server.Comment.newBuilder();
                if (ServerProps.getInstance().getSendPlaintextGroupFeed()) {
                    cb.setPayload(ByteString.copyFrom(payload));
                }
                EncryptedPayload encryptedPayload = EncryptedPayload.newBuilder()
                        .setOneToOneEncryptedPayload(ByteString.copyFrom(encPayload))
                        .build();
                cb.setEncPayload(ByteString.copyFrom(encryptedPayload.toByteArray()));
                if (comment.parentCommentId != null) {
                    cb.setParentCommentId(comment.parentCommentId);
                }
                cb.setPostId(comment.postId);
                cb.setId(comment.id);
                cb.setTimestamp(comment.timestamp / 1000);
                cb.setPublisherUid(Long.parseLong(me.getUser()));
                builder.setComment(cb);

                Msg msg = Msg.newBuilder()
                        .setId(comment.id)
                        .setType(Msg.Type.GROUPCHAT)
                        .setToUid(Long.parseLong(userId.rawId()))
                        .setGroupFeedItem(builder.build())
                        .setRerequestCount(ContentDb.getInstance().getOutboundCommentRerequestCount(userId, comment.id))
                        .build();
                sendMsgInternalIgnoreDuplicateId(msg, () -> Log.i("rerequested group comment acked " + comment.id));
            } catch (CryptoException e) {
                Log.e("Failed to send rerequested group comment", e);
            }
        });
    }

    @Override
    public void sendGroupHistory(@NonNull GroupFeedHistory groupFeedHistory, @NonNull UserId userId) {
        Msg msg = Msg.newBuilder()
                .setGroupFeedHistory(groupFeedHistory).setId(RandomId.create()).setType(Msg.Type.NORMAL).setToUid(Long.parseLong(userId.rawId()))
                .build();
        sendMsgInternal(msg, () -> Log.i("History resend made it to server for " + userId));
    }

    @Override
    public void sendMissingContentNotice(@NonNull ContentMissing.ContentType contentType, @NonNull String contentId, @NonNull UserId userId) {
        if (userId.isMe()) {
            Log.e("Attempting to send missing content notice to self");
            Log.sendErrorReport("Self Missing Content Notice");
            return;
        }
        Msg msg = Msg.newBuilder()
                .setId(RandomId.create())
                .setToUid(Long.parseLong(userId.rawId()))
                .setContentMissing(ContentMissing.newBuilder()
                        .setContentType(contentType)
                        .setContentId(contentId)
                        .setSenderClientVersion(Constants.USER_AGENT))
                .build();
        sendMsgInternal(msg, () -> Log.i("Sent content missing notice for " + contentId));
    }

    @Override
    public void retractComment(@NonNull String postId, @NonNull String commentId) {
        FeedItem commentItem = new FeedItem(FeedItem.Type.COMMENT, commentId, postId, null, null);
        FeedUpdateIq requestIq = new FeedUpdateIq(FeedUpdateIq.Action.RETRACT, commentItem);
        sendIqRequestAsync(requestIq, true)
                .onResponse(response -> connectionObservers.notifyOutgoingCommentSent(postId, commentId, null))
                .onError(e -> Log.e("connection: cannot retract comment", e));
    }

    @Override
    public void retractRerequestedComment(@NonNull String postId, @NonNull String commentId, @NonNull UserId peerUserId) {
        Msg msg = Msg.newBuilder()
                .setId(RandomId.create())
                .setToUid(peerUserId.rawIdLong())
                .setFeedItem(com.halloapp.proto.server.FeedItem.newBuilder()
                        .setAction(com.halloapp.proto.server.FeedItem.Action.RETRACT)
                        .setComment(com.halloapp.proto.server.Comment.newBuilder().setId(commentId).setPostId(postId).setPublisherUid(Long.parseLong(Me.getInstance().getUser())).setPublisherName(Me.getInstance().getName()).build()))
                .build();
        sendMsgInternal(msg, null);
    }

    @Override
    public void retractMessage(@NonNull UserId chatUserId, @NonNull String messageId) {
        executor.execute(() -> {
            String id = RandomId.create();

            Msg msg = Msg.newBuilder()
                    .setId(id)
                    .setToUid(Long.parseLong(chatUserId.rawId()))
                    .setType(Msg.Type.CHAT)
                    .setChatRetract(ChatRetract.newBuilder().setId(messageId).build())
                    .build();
            sendMsgInternal(msg, () -> connectionObservers.notifyOutgoingMessageSent(chatUserId, messageId));
        });
    }

    @Override
    public void retractGroupMessage(@NonNull GroupId groupId, @NonNull String messageId) {
        executor.execute(() -> {
            String id = RandomId.create();

            Msg msg = Msg.newBuilder()
                    .setId(id)
                    .setType(Msg.Type.GROUPCHAT)
                    .setGroupchatRetract(GroupChatRetract.newBuilder().setId(messageId).setGid(groupId.rawId()).build())
                    .build();
            sendMsgInternal(msg, () -> connectionObservers.notifyOutgoingMessageSent(groupId, messageId));
        });
    }

    @Override
    public void retractGroupComment(@NonNull GroupId groupId, @NonNull String postId, @NonNull String commentId) {
        FeedItem commentItem = new FeedItem(FeedItem.Type.COMMENT, commentId, postId, null, null);
        GroupFeedUpdateIq requestIq = new GroupFeedUpdateIq(groupId, GroupFeedUpdateIq.Action.RETRACT, commentItem);
        sendIqRequestAsync(requestIq, true)
                .onResponse(r -> connectionObservers.notifyOutgoingCommentSent(postId, commentId, null))
                .onError(e -> Log.e("connection: cannot retract comment", e));
    }

    @Override
    public void retractRerequestedGroupComment(@NonNull GroupId groupId, @NonNull String postId, @NonNull String commentId, @NonNull UserId peerUserId) {
        Msg msg = Msg.newBuilder()
                .setId(RandomId.create())
                .setToUid(peerUserId.rawIdLong())
                .setGroupFeedItem(com.halloapp.proto.server.GroupFeedItem.newBuilder()
                        .setAction(com.halloapp.proto.server.GroupFeedItem.Action.RETRACT)
                        .setGid(groupId.rawId())
                        .setComment(com.halloapp.proto.server.Comment.newBuilder().setId(commentId).setPostId(postId).setPublisherUid(Long.parseLong(Me.getInstance().getUser())).setPublisherName(Me.getInstance().getName()).build()))
                .build();
        sendMsgInternal(msg, null);
    }

    @Override
    public void sendGroupMessage(@NonNull Message message) {
        if (message.isLocalMessage()) {
            Log.i("connection: System message shouldn't be sent");
            return;
        }
        Msg msg = Msg.newBuilder()
                .setId(message.id)
                .setType(Msg.Type.GROUPCHAT)
                .setGroupChatStanza(ChatMessageProtocol.getInstance().serializeGroupMessage(message))
                .build();
        executor.execute(() -> {
            if (message.isLocalMessage()) {
                Log.i("connection: System message shouldn't be sent");
                return;
            }
            sendMsgInternalIgnoreDuplicateId(msg, () -> connectionObservers.notifyOutgoingMessageSent(message.chatId, message.id));
        });
    }

    @Override
    public void sendMessage(@NonNull Message message, @Nullable SignalSessionSetupInfo signalSessionSetupInfo) {
        executor.execute(() -> {
            if (message.isLocalMessage()) {
                Log.i("connection: System message shouldn't be sent");
                return;
            }
            final UserId recipientUserId = (UserId)message.chatId;

            Msg msg = Msg.newBuilder()
                    .setId(message.id)
                    .setType(Msg.Type.CHAT)
                    .setToUid(Long.parseLong(message.chatId.rawId()))
                    .setChatStanza(ChatMessageProtocol.getInstance().serializeMessage(message, recipientUserId, signalSessionSetupInfo))
                    .build();
            sendMsgInternalIgnoreDuplicateId(msg, () -> connectionObservers.notifyOutgoingMessageSent(message.chatId, message.id));
        });
    }

    @Override
    public void sendNoiseMessageToWebClient(@NonNull byte[] connectionInfo, @NonNull NoiseMessage.MessageType type, @NonNull PublicEdECKey webClientStaticKey, @NonNull int msgLength) {
        executor.execute(() -> {
            NoiseMessage noiseMessage = NoiseMessage.newBuilder()
                    .setMessageType(type)
                    .setContent(ByteString.copyFrom(connectionInfo, 0, msgLength))
                    .build();

            WebStanza webStanza = WebStanza.newBuilder()
                    .setStaticKey(ByteString.copyFrom(webClientStaticKey.getKeyMaterial()))
                    .setNoiseMessage(noiseMessage)
                    .build();

            Msg msg = Msg.newBuilder()
                    .setId(RandomId.create())
                    .setType(Msg.Type.NORMAL)
                    .setWebStanza(webStanza)
                    .build();

            sendMsgInternal(msg, () -> Log.d("Web client noise message successfully made it server"));
        });
    }

    @Override
    public void sendMessageToWebClient(@NonNull byte[] content, @NonNull PublicEdECKey webClientStaticKey, String msgId) {
        executor.execute(() -> {
            WebStanza webStanza = WebStanza.newBuilder()
                    .setStaticKey(ByteString.copyFrom(webClientStaticKey.getKeyMaterial()))
                    .setContent(ByteString.copyFrom(content, 0, content.length))
                    .build();

            Msg msg = Msg.newBuilder()
                    .setId(msgId)
                    .setWebStanza(webStanza)
                    .build();

            sendMsgInternal(msg, () -> Log.d("Web client message successfully made it server"));
        });
    }

    @Override
    public void sendChatReaction(@NonNull Reaction reaction, @NonNull Message message, @Nullable SignalSessionSetupInfo signalSessionSetupInfo) {
        executor.execute(() -> {
            if (!reaction.senderUserId.isMe()) {
                Log.i("connection: Cannot send others' reactions");
                return;
            }

            Msg msg = Msg.newBuilder()
                    .setId(reaction.reactionId)
                    .setType(Msg.Type.CHAT)
                    .setToUid(Long.parseLong(message.chatId.rawId()))
                    .setChatStanza(ChatMessageProtocol.getInstance().serializeReaction(reaction, (UserId)message.chatId, signalSessionSetupInfo))
                    .build();
            sendMsgInternal(msg, () -> ContentDb.getInstance().markReactionSent(reaction));
        });
    }

    @Override
    public void sendGroupChatReaction(@NonNull Reaction reaction, @NonNull Message message) {
        if (!(message.chatId instanceof GroupId)) {
            Log.e("connection: cant send non group reaction as a group reaction");
            return;
        }
        Msg msg = Msg.newBuilder()
                .setId(reaction.reactionId)
                .setType(Msg.Type.GROUPCHAT)
                .setGroupChatStanza(ChatMessageProtocol.getInstance().serializeGroupReaction((GroupId) message.chatId, reaction))
                .build();
        executor.execute(() -> {
            if (!reaction.senderUserId.isMe()) {
                Log.i("connection: Cannot send others' reactions");
                return;
            }
            sendMsgInternal(msg, () -> ContentDb.getInstance().markReactionSent(reaction));
        });
    }

    @Override
    public Observable<ExternalShareRetrieveResponseIq> getSharedPost(@NonNull String shareId) {
        HalloIq getSharedPostIq = new HalloIq() {
            @Override
            public Iq.Builder toProtoIq() {
                Iq.Builder builder = Iq.newBuilder();
                builder.setExternalSharePost(ExternalSharePost.newBuilder()
                        .setAction(ExternalSharePost.Action.GET)
                        .setBlobId(shareId)
                        .build()
                );
                return builder;
            };
        };
        return sendRequestIq(getSharedPostIq, true);
    }

    @Override
    public Observable<HalloIq> revokeSharedPost(@NonNull String shareId) {
        HalloIq revokeSharedPostIq = new HalloIq() {
            @Override
            public Iq.Builder toProtoIq() {
                ExternalSharePost externalSharePost = ExternalSharePost.newBuilder()
                        .setAction(ExternalSharePost.Action.DELETE)
                        .setBlobId(shareId)
                        .build();
                return Iq.newBuilder()
                        .setId(RandomId.create())
                        .setType(Iq.Type.SET)
                        .setExternalSharePost(externalSharePost);
            }
        };
         return sendRequestIq(revokeSharedPostIq, true);
    }

    // NOTE: Should NOT be called from executor.
    @Override
    public Observable<Iq> sendIqRequest(@NonNull HalloIq iq) {
        MutableObservable<Iq> iqResponse = new MutableObservable<>();
        sendIqRequestAsync(iq).onResponse(resultIq -> {
            try {
                iqResponse.setResponse(resultIq);
            } catch (ClassCastException e) {
                iqResponse.setException(e);
            }
        }).onError(iqResponse::setException);
        return iqResponse;
    }

    // NOTE: Should NOT be called from executor.
    @Override
    public Observable<Iq> sendIqRequest(@NonNull Iq.Builder iq) {
        MutableObservable<Iq> iqResponse = new MutableObservable<>();
        sendIqRequestAsync(iq, false).onResponse(resultIq -> {
            try {
                iqResponse.setResponse(resultIq);
            } catch (ClassCastException e) {
                iqResponse.setException(e);
            }
        }).onError(iqResponse::setException);
        return iqResponse;
    }

    // NOTE: Should NOT be called from executor.
    @Override
    public <T extends HalloIq> Observable<T> sendRequestIq(@NonNull HalloIq iq) {
        MutableObservable<T> iqResponse = new MutableObservable<>();
        sendIqRequestAsync(iq).onResponse(resultIq -> {
            try {
                iqResponse.setResponse((T) HalloIq.fromProtoIq(resultIq));
            } catch (ClassCastException e) {
                iqResponse.setException(e);
            }
        }).onError(iqResponse::setException);
        return iqResponse;
    }

    // NOTE: Should NOT be called from executor.
    @Override
    public <T extends HalloIq> Observable<T> sendRequestIq(@NonNull HalloIq iq, boolean resendable) {
        MutableObservable<T> iqResponse = new MutableObservable<>();
        sendIqRequestAsync(iq, resendable).onResponse(resultIq -> {
            try {
                iqResponse.setResponse((T) HalloIq.fromProtoIq(resultIq));
            } catch (ClassCastException e) {
                iqResponse.setException(e);
            }
        }).onError(iqResponse::setException);
        return iqResponse;
    }

    private Observable<Iq> sendIqRequestAsync(@NonNull HalloIq iq) {
        return sendIqRequestAsync(iq, false);
    }

    private Observable<Iq> sendIqRequestAsync(@NonNull HalloIq iq, boolean resendable) {
        BackgroundObservable<Iq> iqResponse = new BackgroundObservable<>(bgWorkers);
        executor.executeWithDropHandler(() -> {
            Iq.Builder protoIq = iq.toProtoIq();
            iqRouter.sendAsync(protoIq, resendable)
                    .onResponse(iqResponse::setResponse)
                    .onError(iqResponse::setException);
        }, () -> iqResponse.setException(new ExecutorResetException()));
        return iqResponse;
    }

    private Observable<Iq> sendIqRequestAsync(@NonNull Iq.Builder protoIq, boolean resendable) {
        BackgroundObservable<Iq> iqResponse = new BackgroundObservable<>(bgWorkers);
        executor.executeWithDropHandler(() -> {
            iqRouter.sendAsync(protoIq, resendable)
                    .onResponse(iqResponse::setResponse)
                    .onError(iqResponse::setException);
        }, () -> iqResponse.setException(new ExecutorResetException()));
        return iqResponse;
    }

    @Override
    public void sendRerequest(final @NonNull UserId senderUserId, @NonNull String messageId, boolean isReaction, int rerequestCount, @Nullable byte[] teardownKey) {
        executor.execute(() -> {
            RerequestElement rerequestElement = new RerequestElement(messageId, senderUserId, rerequestCount, teardownKey, isReaction ? Rerequest.ContentType.CHAT_REACTION : Rerequest.ContentType.CHAT);
            Log.i("connection: sending rerequest for " + messageId + " to " + senderUserId);
            sendPacket(Packet.newBuilder().setMsg(rerequestElement.toProto()).build());
        });
    }

    @Override
    public void sendGroupPostRerequest(@NonNull UserId senderUserId, @NonNull GroupId groupId, @NonNull String postId, int rerequestCount, boolean senderStateIssue) {
        executor.execute(() -> {
            GroupRerequestElement groupRerequestElement = new GroupRerequestElement(senderUserId, groupId, postId, senderStateIssue, GroupFeedRerequest.ContentType.POST, rerequestCount);
            Log.i("connection: sending group post rerequest for " + postId + " in " + groupId + " to " + senderUserId);
            sendPacket(Packet.newBuilder().setMsg(groupRerequestElement.toProto()).build());
        });
    }

    @Override
    public void sendGroupMessageRerequest(@NonNull UserId senderUserId, @NonNull GroupId groupId, @NonNull String msgId, int rerequestCount, boolean senderStateIssue) {
        executor.execute(() -> {
            GroupRerequestElement groupRerequestElement = new GroupRerequestElement(senderUserId, groupId, msgId, senderStateIssue, GroupFeedRerequest.ContentType.MESSAGE, rerequestCount);
            Log.i("connection: sending group message rerequest for " + msgId + " in " + groupId + " to " + senderUserId);
            sendPacket(Packet.newBuilder().setMsg(groupRerequestElement.toProto()).build());
        });
    }

    @Override
    public void sendGroupCommentRerequest(@NonNull UserId senderUserId, @NonNull GroupId groupId, @NonNull String commentId, int rerequestCount, boolean senderStateIssue, @NonNull com.halloapp.proto.server.Comment.CommentType commentType) {
        executor.execute(() -> {
            GroupFeedRerequest.ContentType contentType;
            switch (commentType) {
                case COMMENT:
                    contentType = GroupFeedRerequest.ContentType.COMMENT;
                    break;
                case COMMENT_REACTION:
                    contentType = GroupFeedRerequest.ContentType.COMMENT_REACTION;
                    break;
                case POST_REACTION:
                    contentType = GroupFeedRerequest.ContentType.POST_REACTION;
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled CommentType " + commentType);
            }
            GroupRerequestElement groupRerequestElement = new GroupRerequestElement(senderUserId, groupId, commentId, senderStateIssue, contentType, rerequestCount);
            Log.i("connection: sending group comment rerequest for " + commentId + " in " + groupId + " to " + senderUserId);
            sendPacket(Packet.newBuilder().setMsg(groupRerequestElement.toProto()).build());
        });
    }

    @Override
    public void sendGroupFeedHistoryRerequest(@NonNull UserId senderUserId, @NonNull GroupId groupId, @NonNull String historyId, boolean senderStateIssue) {
        executor.execute(() -> {
            int rerequestCount = ContentDb.getInstance().getHistoryResendRerequestCount(senderUserId, historyId);
            GroupRerequestElement groupRerequestElement = new GroupRerequestElement(senderUserId, groupId, historyId, senderStateIssue, GroupFeedRerequest.ContentType.HISTORY_RESEND, rerequestCount);
            Log.i("connection: sending group history rerequest for " + historyId + " in " + groupId + " to " + senderUserId);
            sendPacket(Packet.newBuilder().setMsg(groupRerequestElement.toProto()).build());
        });
    }

    @Override
    public void sendGroupHistoryPayloadRerequest(final @NonNull UserId senderUserId, @NonNull String messageId, @Nullable byte[] teardownKey) {
        executor.execute(() -> {
            RerequestElement rerequestElement = new RerequestElement(messageId, senderUserId, 0, teardownKey, Rerequest.ContentType.GROUP_HISTORY);
            Log.i("connection: sending rerequest for group history " + messageId + " to " + senderUserId);
            sendPacket(Packet.newBuilder().setMsg(rerequestElement.toProto()).build());
        });
    }

    @Override
    public void sendHomePostRerequest(@NonNull UserId senderUserId, boolean favorites, @NonNull String contentId, int rerequestCount, boolean senderStateIssue) {
        executor.execute(() -> {
            HomeRerequestElement homeRerequestElement = new HomeRerequestElement(senderUserId, contentId, senderStateIssue, HomeFeedRerequest.ContentType.POST, rerequestCount);
            Log.i("connection: sending home post rerequest for " + contentId + " to " + senderUserId);
            sendPacket(Packet.newBuilder().setMsg(homeRerequestElement.toProto()).build());
        });
    }

    @Override
    public void sendHomeCommentRerequest(@NonNull UserId postSenderUserId, @NonNull UserId commentSenderUserId, int rerequestCount, @NonNull String contentId, @NonNull com.halloapp.proto.server.Comment.CommentType commentType) {
        executor.execute(() -> {
            HomeFeedRerequest.ContentType contentType;
            switch (commentType) {
                case COMMENT:
                    contentType = HomeFeedRerequest.ContentType.COMMENT;
                    break;
                case COMMENT_REACTION:
                    contentType = HomeFeedRerequest.ContentType.COMMENT_REACTION;
                    break;
                case POST_REACTION:
                    contentType = HomeFeedRerequest.ContentType.POST_REACTION;
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled CommentType " + commentType);
            }
            HomeRerequestElement homeRerequestElement = new HomeRerequestElement(postSenderUserId, contentId, false, contentType, rerequestCount);
            Log.i("connection: sending home comment rerequest for " + contentId + " to " + postSenderUserId);
            sendPacket(Packet.newBuilder().setMsg(homeRerequestElement.toProto()).build());
        });
    }

    @Override
    public void sendAck(@NonNull String id) {
        if (id == null) {
            Log.e("connection: null ack id!");
            return;
        }
        executor.execute(() -> {
            Log.i("connection: sending ack for " + id);
            sendPacket(Packet.newBuilder().setAck(Ack.newBuilder()
                    .setId(id)).build());
        });
    }

    @Override
    public void sendPostSeenReceipt(@NonNull UserId senderUserId, @NonNull String postId) {
        executor.execute(() -> {
            String id = RandomId.create();

            SeenReceiptElement seenReceiptElement = new SeenReceiptElement(FEED_THREAD_ID, postId);
            Msg msg = Msg.newBuilder()
                    .setId(id)
                    .setSeenReceipt(seenReceiptElement.toProto())
                    .setToUid(Long.parseLong(senderUserId.rawId()))
                    .build();

            Log.i("connection: sending post seen receipt " + postId + " to " + senderUserId);
            sendMsgInternal(msg, () -> connectionObservers.notifyIncomingPostSeenReceiptSent(senderUserId, postId), true);
        });
    }

    @Override
    public void sendMomentScreenshotReceipt(@NonNull UserId senderUserId, @NonNull String postId) {
        executor.execute(() -> {
            String id = RandomId.create();

            ScreenshotReceipt.Builder builder = ScreenshotReceipt.newBuilder();
            builder.setId(postId);
            builder.setThreadId(FEED_THREAD_ID);
            Msg msg = Msg.newBuilder()
                    .setId(id)
                    .setScreenshotReceipt(builder)
                    .setToUid(Long.parseLong(senderUserId.rawId()))
                    .build();

            Log.i("connection: sending post seen receipt " + postId + " to " + senderUserId);
            sendMsgInternal(msg, () -> connectionObservers.notifyIncomingMomentScreenshotReceiptSent(senderUserId, postId), true);
        });
    }

    @Override
    public void sendMessageSeenReceipt(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        executor.execute(() -> {
            String id = RandomId.create();

            SeenReceiptElement seenReceiptElement = new SeenReceiptElement(senderUserId.equals(chatId) ? null : chatId.rawId(), messageId);
            Msg msg = Msg.newBuilder()
                    .setId(id)
                    .setSeenReceipt(seenReceiptElement.toProto())
                    .setToUid(Long.parseLong(senderUserId.rawId()))
                    .build();
            sendMsgInternal(msg, () -> connectionObservers.notifyIncomingMessageSeenReceiptSent(chatId, senderUserId, messageId), true);
            Log.i("connection: sending message seen receipt " + messageId + " to " + senderUserId);
        });
    }

    @Override
    public void sendMessagePlayedReceipt(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        executor.execute(() -> {
            String id = RandomId.create();

            PlayedReceipt playedReceipt = PlayedReceipt.newBuilder()
                    .setId(messageId)
                    .setThreadId(senderUserId.equals(chatId) ? "" : chatId.rawId())
                    .build();
            Msg msg = Msg.newBuilder()
                    .setId(id)
                    .setPlayedReceipt(playedReceipt)
                    .setToUid(Long.parseLong(senderUserId.rawId()))
                    .build();
            sendMsgInternal(msg, () -> connectionObservers.notifyIncomingMessagePlayedReceiptSent(chatId, senderUserId, messageId), true);
            Log.i("connection: sending message seen receipt " + messageId + " to " + senderUserId);
        });
    }

    @Override
    public Observable<Iq> deleteAccount(@Nullable String phone, @Nullable String username, @Nullable String reason) {
        return sendIqRequestAsync(new DeleteAccountRequestIq(phone, username, reason)).map(response -> {
            Log.d("connection: response after deleting account " + ProtoPrinter.toString(response));
            return response;
        });
    }

    @Override
    public Observable<Iq> reportUserContent(@NonNull UserId userId, @Nullable String contentId, @Nullable ReportUserContent.Reason reason) {
        return sendIqRequestAsync(new ReportContentIq(userId, contentId, reason)).map(response -> {
            Log.d("connection: response after reporting content " + ProtoPrinter.toString(response));
            return response;
        });
    }

    @Override
    public Observable<ExportDataResponseIq> requestAccountData() {
        return sendIqRequestAsync(ExportDataRequestIq.requestExport()).map(response -> {
            Log.d("connection: response after export request " + ProtoPrinter.toString(response));
            ExportDataResponseIq iq = ExportDataResponseIq.fromProto(response.getExportData());
            preferences.setExportDataState(iq.status == ExportData.Status.PENDING ? ExportDataActivity.EXPORT_STATE_PENDING : ExportDataActivity.EXPORT_STATE_READY);
            return iq;
        });
    }

    @Override
    public Observable<ExportDataResponseIq> getAccountDataRequestState() {
        return sendIqRequestAsync(ExportDataRequestIq.getRequestState()).map(response -> {
            Log.d("connection: response after export state request " + ProtoPrinter.toString(response));
            ExportDataResponseIq iq = ExportDataResponseIq.fromProto(response.getExportData());
            preferences.setExportDataState(iq.status == ExportData.Status.PENDING ? ExportDataActivity.EXPORT_STATE_PENDING : ExportDataActivity.EXPORT_STATE_READY);
            return iq;
        });
    }

    @Override
    public Observable<RelationshipListResponseIq> requestRelationshipList(@RelationshipInfo.Type int relationshipType) {
        return sendIqRequestAsync(new RelationshipListRequestIq(relationshipType)).map(response -> {
            Log.d("connection: response after relationship list request " + ProtoPrinter.toString(response));
            return RelationshipListResponseIq.fromProto(response.getRelationshipList());
        });
    }

    @Override
    public Observable<RelationshipResponseIq> requestFollowUser(@NonNull UserId userId) {
        return sendIqRequestAsync(new RelationshipRequestIq(userId, RelationshipRequest.Action.FOLLOW)).map(response -> {
            Log.d("connection: response after relationship request " + ProtoPrinter.toString(response));
            return RelationshipResponseIq.fromProto(response.getRelationshipResponse());
        });
    }

    @Override
    public Observable<RelationshipResponseIq> requestUnfollowUser(@NonNull UserId userId) {
        return sendIqRequestAsync(new RelationshipRequestIq(userId, RelationshipRequest.Action.UNFOLLOW)).map(response -> {
            Log.d("connection: response after relationship request " + ProtoPrinter.toString(response));
            return RelationshipResponseIq.fromProto(response.getRelationshipResponse());
        });
    }

    @Override
    public Observable<RelationshipResponseIq> requestRemoveFollower(@NonNull UserId userId) {
        return sendIqRequestAsync(new RelationshipRequestIq(userId, RelationshipRequest.Action.REMOVE_FOLLOWER)).map(response -> {
            Log.d("connection: response after relationship request " + ProtoPrinter.toString(response));
            return RelationshipResponseIq.fromProto(response.getRelationshipResponse());
        });
    }

    @Override
    public Observable<RelationshipResponseIq> requestBlockUser(@NonNull UserId userId) {
        return sendIqRequestAsync(new RelationshipRequestIq(userId, RelationshipRequest.Action.BLOCK)).map(response -> {
            Log.d("connection: response after relationship request " + ProtoPrinter.toString(response));
            return RelationshipResponseIq.fromProto(response.getRelationshipResponse());
        });
    }

    @Override
    public Observable<RelationshipResponseIq> requestUnblockUser(@NonNull UserId userId) {
        return sendIqRequestAsync(new RelationshipRequestIq(userId, RelationshipRequest.Action.UNBLOCK)).map(response -> {
            Log.d("connection: response after relationship request " + ProtoPrinter.toString(response));
            return RelationshipResponseIq.fromProto(response.getRelationshipResponse());
        });
    }

    @Override
    public Observable<PostMetricsResultIq> requestPostMetrics(@NonNull String postId) {
        return sendIqRequestAsync(new PostMetricsRequestIq(postId)).map(response -> {
            Log.d("connection: response after post metrics request " + ProtoPrinter.toString(response));
            return PostMetricsResultIq.fromProto(response.getPostMetricsResult());
        });
    }

    @Override
    public Observable<UsernameResponseIq> sendUsername(@NonNull String username) {
        return sendIqRequestAsync(new UsernameRequestIq(username, UsernameRequest.Action.SET)).map(response -> {
            Log.d("connection: response after username request " + ProtoPrinter.toString(response));
            return UsernameResponseIq.fromProto(response.getUsernameResponse());
        });
    }

    @Override
    public Observable<UsernameResponseIq> checkUsernameIsAvailable(@NonNull String username) {
        return sendIqRequestAsync(new UsernameRequestIq(username, UsernameRequest.Action.IS_AVAILABLE)).map(response -> {
            Log.d("connection: response after username request " + ProtoPrinter.toString(response));
            return UsernameResponseIq.fromProto(response.getUsernameResponse());
        });
    }

    @Override
    public Observable<FollowSuggestionsResponseIq> requestFollowSuggestions() {
        return sendIqRequestAsync(new FollowSuggestionsRequestIq()).map(response -> {
            Log.d("connection: response after relationship request " + ProtoPrinter.toString(response));
            return FollowSuggestionsResponseIq.fromProto(response.getFollowSuggestionsResponse());
        });
    }

    @Override
    public Observable<FriendListResponseIq> requestFriendList(@Nullable String cursor, @NonNull FriendListRequest.Action action) {
        return sendIqRequestAsync(new FriendListRequestIq(cursor, action)).map(response -> {
            Log.d("connection: response after friend request " + ProtoPrinter.toString(response));
            return FriendListResponseIq.fromProto(response.getFriendListResponse());
        });
    }

    @Override
    public Observable<FriendshipResponseIq> sendFriendRequest(@NonNull UserId userId) {
        return sendIqRequestAsync(new FriendshipRequestIq(userId, FriendshipRequest.Action.ADD_FRIEND)).map(response -> {
            Log.d("connection: response after sending friend request " + ProtoPrinter.toString(response));
            return FriendshipResponseIq.fromProto(response.getFriendshipResponse());
        });
    }

    @Override
    public Observable<FriendshipResponseIq> withdrawFriendRequest(@NonNull UserId userId) {
        return sendIqRequestAsync(new FriendshipRequestIq(userId, FriendshipRequest.Action.WITHDRAW_FRIEND_REQUEST)).map(response -> {
            Log.d("connection: response after withdrawing friend request " + ProtoPrinter.toString(response));
            return FriendshipResponseIq.fromProto(response.getFriendshipResponse());
        });
    }

    @Override
    public Observable<FriendshipResponseIq> removeFriend(@NonNull UserId userId) {
        return sendIqRequestAsync(new FriendshipRequestIq(userId, FriendshipRequest.Action.REMOVE_FRIEND)).map(response -> {
            Log.d("connection: response after removing friend " + ProtoPrinter.toString(response));
            return FriendshipResponseIq.fromProto(response.getFriendshipResponse());
        });
    }

    @Override
    public Observable<FriendshipResponseIq> acceptFriendRequest(@NonNull UserId userId) {
        return sendIqRequestAsync(new FriendshipRequestIq(userId, FriendshipRequest.Action.ACCEPT_FRIEND)).map(response -> {
            Log.d("connection: response after accepting friend request " + ProtoPrinter.toString(response));
            return FriendshipResponseIq.fromProto(response.getFriendshipResponse());
        });
    }

    @Override
    public Observable<FriendshipResponseIq> rejectFriendRequest(@NonNull UserId userId) {
        return sendIqRequestAsync(new FriendshipRequestIq(userId, FriendshipRequest.Action.REJECT_FRIEND)).map(response -> {
            Log.d("connection: response after rejecting friend request " + ProtoPrinter.toString(response));
            return FriendshipResponseIq.fromProto(response.getFriendshipResponse());
        });
    }

    @Override
    public Observable<FriendshipResponseIq> blockFriend(@NonNull UserId userId) {
        return sendIqRequestAsync(new FriendshipRequestIq(userId, FriendshipRequest.Action.BLOCK)).map(response -> {
            Log.d("connection: response after blocking friend " + ProtoPrinter.toString(response));
            return FriendshipResponseIq.fromProto(response.getFriendshipResponse());
        });
    }

    @Override
    public Observable<FriendshipResponseIq> unblockFriend(@NonNull UserId userId) {
        return sendIqRequestAsync(new FriendshipRequestIq(userId, FriendshipRequest.Action.UNBLOCK)).map(response -> {
            Log.d("connection: response after unblocking friend " + ProtoPrinter.toString(response));
            return FriendshipResponseIq.fromProto(response.getFriendshipResponse());
        });
    }

    @Override
    public Observable<FriendshipResponseIq> rejectFriendSuggestion(@NonNull UserId userId) {
        return sendIqRequestAsync(new FriendshipRequestIq(userId, FriendshipRequest.Action.REJECT_SUGGESTION)).map(response -> {
            Log.d("connection: response after rejecting friend suggestion " + ProtoPrinter.toString(response));
            return FriendshipResponseIq.fromProto(response.getFriendshipResponse());
        });
    }

    @Override
    public Observable<HalloappProfileResponseIq> getHalloappProfileInfo(@NonNull UserId userId, @Nullable String username) {
        return sendIqRequestAsync(new HalloappProfileRequestIq(userId, username), true).map(response -> {
            Log.d("connection: response after getting profile info: " + ProtoPrinter.toString(response));
            return HalloappProfileResponseIq.fromProto(response.getHallaoppProfileResult());
        });
    }

    @Override
    public Observable<ReverseGeocodeResponseIq> getGeocodeLocation(double latitude, double longitude) {
        return sendIqRequestAsync(new ReverseGeocodeRequestIq(latitude, longitude), true).map(response -> {
            Log.d("connection: response after getting geocode location: " + ProtoPrinter.toString(response));
            return ReverseGeocodeResponseIq.fromProto(response.getReverseGeocodeResult());
        });
    }

    @Override
    public Observable<UserSearchResponseIq> searchForUser(@NonNull String text) {
        return sendIqRequestAsync(new UserSearchRequestIq(text)).map(response -> {
            Log.d("connection: response after user search request " + ProtoPrinter.toString(response));
            return UserSearchResponseIq.fromProto(response.getSearchResponse());
        });
    }

    @Override
    public Observable<Iq> rejectFollowSuggestion(@NonNull UserId userId) {
        return sendIqRequestAsync(new FollowSuggestionsRequestIq(userId)).map(response -> {
            Log.d("connection: response after reject follow suggestion " + ProtoPrinter.toString(response));
            return response;
        });
    }

    @Override
    public Observable<PublicFeedResponseIq> requestPublicFeed(@Nullable String cursor, @Nullable Double latitude, @Nullable Double longitude, boolean showDevContent) {
        return sendIqRequestAsync(new PublicFeedRequestIq(cursor, latitude, longitude, showDevContent)).map(response -> {
            Log.d("connection: response after public feed request " + ProtoPrinter.toString(response));
            return PublicFeedResponseIq.fromProto(response.getPublicFeedResponse());
        });
    }

    @Override
    public Observable<SetBioResponseIq> sendBio(@NonNull String bio) {
        return sendIqRequestAsync(new SetBioRequestIq(bio)).map(response -> {
            Log.d("connection: response after set bio request " + ProtoPrinter.toString(response));
            return SetBioResponseIq.fromProto(response.getSetBioResult());
        });
    }

    @Override
    public Observable<SetLinkResponseIq> sendUserDefinedLink(@NonNull String text) {
        return sendIqRequestAsync(new SetLinkRequestIq(text, Link.Type.USER_DEFINED)).map(response -> {
            Log.d("connection: response after user defined set link request " + ProtoPrinter.toString(response));
            return SetLinkResponseIq.fromProto(response.getSetLinkResult());
        });
    }

    @Override
    public Observable<SetLinkResponseIq> sendTikTokLink(@NonNull String text) {
        return sendIqRequestAsync(new SetLinkRequestIq(text, Link.Type.TIKTOK)).map(response -> {
            Log.d("connection: response after tiktok set link request " + ProtoPrinter.toString(response));
            return SetLinkResponseIq.fromProto(response.getSetLinkResult());
        });
    }

    @Override
    public Observable<SetLinkResponseIq> sendInstagramLink(@NonNull String text) {
        return sendIqRequestAsync(new SetLinkRequestIq(text, Link.Type.INSTAGRAM)).map(response -> {
            Log.d("connection: response after instagram set link request " + ProtoPrinter.toString(response));
            return SetLinkResponseIq.fromProto(response.getSetLinkResult());
        });
    }

    @Override
    public Observable<SetLinkResponseIq> sendSnapchatLink(@NonNull String text) {
        return sendIqRequestAsync(new SetLinkRequestIq(text, Link.Type.SNAPCHAT)).map(response -> {
            Log.d("connection: response after snapchat set link request " + ProtoPrinter.toString(response));
            return SetLinkResponseIq.fromProto(response.getSetLinkResult());
        });
    }

    @Override
    public Observable<AiImageResponseIq> sendAiImageRequest(@NonNull String text, int count, boolean custom) {
        return sendIqRequestAsync(new AiImageRequestIq(text, count, custom)).map(response -> {
            Log.d("connection: response after ai image request " + ProtoPrinter.toString(response));
            return AiImageResponseIq.fromProto(response.getAiImageResult());
        });
    }

    @Override
    public Observable<PostSubscriptionResponseIq> sendPostSubscriptionRequest(@NonNull String postId) {
        return sendIqRequestAsync(new PostSubscriptionRequestIq(postId, PostSubscriptionRequest.Action.SUBSCRIBE)).map(response -> {
            Log.d("connection: response after post subscription request " + ProtoPrinter.toString(response));
            return PostSubscriptionResponseIq.fromProto(response.getPostSubscriptionResponse());
        });
    }

    @Override
    public Observable<GeotagResponseIq> forceAddGeotag(@NonNull Location location) {
        return sendIqRequestAsync(new GeotagRequestIq(null, location, GeoTagRequest.Action.FORCE_ADD)).map(response -> {
            Log.d("connection: response after force add geotag request " + ProtoPrinter.toString(response));
            return GeotagResponseIq.fromProto(response.getGeoTagResponse());
        });
    }

    @Override
    public Observable<GeotagResponseIq> removeGeotag(@NonNull String geotag) {
        return sendIqRequestAsync(new GeotagRequestIq(geotag, null, GeoTagRequest.Action.BLOCK)).map(response -> {
            Log.d("connection: response after remove geotag request " + ProtoPrinter.toString(response));
            return GeotagResponseIq.fromProto(response.getGeoTagResponse());
        });
    }

    @Override
    public Observable<ArchiveResultIq> requestArchive(@NonNull UserId userId) {
        return sendIqRequestAsync(new ArchiveRequestIq(userId)).map(response -> {
            Log.d("connection: response after request archive " + ProtoPrinter.toString(response));
            return ArchiveResultIq.fromProto(response.getArchiveResult());
        });
    }

    @Override
    public Observable<HalloappUserSearchResponseIq> searchForHalloappUser(@NonNull String text) {
        return sendIqRequestAsync(new HalloappUserSearchRequestIq(text)).map(response -> {
            Log.d("connection: response after halloapp user search request " + ProtoPrinter.toString(response));
            return HalloappUserSearchResponseIq.fromProto(response.getHalloappSearchResponse());
        });
    }

    @Override
    public UserId getUserId(@NonNull String user) {
        return isMe(user) ? UserId.ME : new UserId(user);
    }

    @Override
    public Observable<Iq> getKatchupUserProfileInfo(@Nullable UserId userId, @Nullable String username){
        return sendIqRequestAsync(new UserProfileRequestIq(userId, username), true).map(response -> {
            Log.d("connection: response after getting katchup profile info: " + ProtoPrinter.toString(response));
            return response;
        });
    }

    @Override
    public boolean getClientExpired() {
        return clientExpired;
    }

    @Override
    public synchronized String getAndIncrementShortId() {
        final int max = 16777216; // 2^24 (3 bytes)
        iqShortId = (iqShortId + 1) % max;
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(iqShortId);
        return Base64.encodeToString(Arrays.copyOfRange(byteBuffer.array(), 1, 4), Base64.URL_SAFE | Base64.NO_WRAP);
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
        private Thread readerThread;
        private ReaderRunnable readerRunnable;

        synchronized void init() {
            shutdown();
            readerRunnable = new ReaderRunnable();
            readerThread = ThreadUtils.go(readerRunnable, "Packet Reader"); // TODO(jack): Connection counter
        }

        synchronized void shutdown() {
            if (readerRunnable != null) {
                readerRunnable.shutdown();
                readerRunnable = null;
            }
            if (readerThread != null) {
                readerThread.interrupt();
                readerThread = null;
            }
        }

        private class ReaderRunnable implements Runnable {

            private TimerTask readTimeoutTimer;
            private volatile boolean done = false;

            @Override
            public void run() {
                ThreadUtils.setSocketTag();
                while (!done) {
                    try {
                        if (socket == null) {
                            throw new IOException("Socket is null");
                        }
                        Log.d("connection: waiting for next packet");
                        scheduleReadTimeout();
                        byte[] packet = socket.readPacket();
                        if (packet == null) {
                            throw new IOException("No more packets");
                        }
                        cancelReadTimeout();
                        parsePacket(packet);
                    } catch (Exception e) {
                        Log.e("Packet Reader error; maybe disconnecting", e);
                        if (e instanceof AEADBadTagException) {
                            Log.sendErrorReport("Noise bad tag");
                        }
                        if (!done) {
                            disconnect();
                        }
                    }
                }
                if (readTimeoutTimer != null) {
                    Log.i("connection: done reading, stopping read timeout timer");
                    readTimeoutTimer.cancel();
                    readTimeoutTimer = null;
                }
                Log.i("connection: reader finished");
            }

            private void cancelReadTimeout() {
                if (readTimeoutTimer != null) {
                    readTimeoutTimer.cancel();
                    readTimeoutTimer = null;
                }
            }

            private void scheduleReadTimeout() {
                cancelReadTimeout();
                readTimeoutTimer = new TimerTask() {
                    @Override
                    public void run() {
                        Log.e("connection: havent received packet from server, disconnecting");
                        if (!done) {
                            disconnect();
                        }
                    }
                };
                timer.schedule(readTimeoutTimer, READ_TIMEOUT_MS);
            }

            public void shutdown() {
                done = true;
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
            } catch (InvalidProtocolBufferException e) {
                try {
                    AuthResult authResult = AuthResult.parseFrom(bytes);
                    handleAuth(authResult);
                } catch (InvalidProtocolBufferException f) {
                    Log.e("Failed to parse incoming protobuf; was not auth", f);
                    Log.sendErrorReport("Failed to parse incoming protobuf");
                    disconnect();
                }
            }
        }

        private void handleAuth(AuthResult authResult) {
            String connectionPropHash = Hex.bytesToStringLowercase(authResult.getPropsHash().toByteArray());
            if (AuthResult.Reason.SPUB_MISMATCH.equals(authResult.getReason())) {
                Log.e("connection: failed to login");
                disconnectInBackground();
                connectionObservers.notifyLoginFailed(false);
            } else if (AuthResult.Reason.INVALID_CLIENT_VERSION.equals(authResult.getReason())) {
                Log.e("connection: invalid client version");
                clientExpired();
            } else if (AuthResult.Reason.ACCOUNT_DELETED.equals(authResult.getReason())) {
                Log.e("connection: account deleted");
                disconnectInBackground();
                connectionObservers.notifyLoginFailed(true);
            } else {
                ServerProps.getInstance().onReceiveServerPropsHash(connectionPropHash);
                long secondsLeft = authResult.getVersionTtl();
                int daysLeft = (int) (secondsLeft / Constants.SECONDS_PER_DAY) + 1;
                Log.d("connection: build daysLeft=" + daysLeft);
                if (daysLeft <= Constants.BUILD_EXPIRES_SOON_THRESHOLD_DAYS) {
                    connectionObservers.notifyClientVersionExpiringSoon(daysLeft);
                }
            }
        }

        private void handleMsg(Msg msg) {
            boolean handled = false;
            if (msg.getType() == Msg.Type.ERROR) {
                // TODO(jack): Remove this portion if Josh and Chris agree this should be reported inside AiImage, not at the top level
                if (msg.hasAiImage()) {
                    AiImage aiImage = msg.getAiImage();
                    connectionObservers.notifyAiImageReceived(aiImage.getId(), null, msg.getId());
                    handled = true;
                } else {
                    Log.w("connection: got error message " + ProtoPrinter.toString(msg));
                }
            } else {
                if (msg.hasEndOfQueue()) {
                    Log.i("connection: end of offline queue");
                    preferences.setPendingOfflineQueue(false);
                    connectionObservers.notifyOfflineQueueComplete(msg.getId());
                    handled = true;
                } else if (msg.hasFeedItem()) {
                    Log.i("connection: got feed item " + ProtoPrinter.toString(msg));
                    com.halloapp.proto.server.FeedItem feedItem = msg.getFeedItem();
                    homeStanzaExecutor.execute(() -> {
                        processFeedPubSubItems(Collections.singletonList(feedItem), msg.getId());
                    });
                    handled = true;
                } else if (msg.hasFeedItems()) {
                    Log.i("connection: got feed items " + ProtoPrinter.toString(msg));
                    FeedItems feedItems = msg.getFeedItems();
                    homeStanzaExecutor.execute(() -> {
                        processFeedPubSubItems(feedItems.getItemsList(), msg.getId());
                    });
                    handled = true;
                } else if (msg.hasGroupFeedItem()) {
                    Log.i("connection: got group feed item " + ProtoPrinter.toString(msg));
                    GroupFeedItem groupFeedItem = msg.getGroupFeedItem();
                    groupStanzaExecutor.execute(() -> {
                        processGroupFeedItems(Collections.singletonList(groupFeedItem), msg.getId(), false);
                    });
                    handled = true;
                } else if (msg.hasGroupFeedItems()) {
                    Log.i("connection: got group feed items " + ProtoPrinter.toString(msg));
                    GroupFeedItems groupFeedItems = msg.getGroupFeedItems();
                    List<GroupFeedItem> inList = groupFeedItems.getItemsList();
                    List<GroupFeedItem> outList = new ArrayList<>();
                    for (GroupFeedItem item : inList) {
                        GroupFeedItem newItem = GroupFeedItem.newBuilder(item)
                                .setGid(groupFeedItems.getGid())
                                .setName(groupFeedItems.getName())
                                .setAvatarId(groupFeedItems.getAvatarId())
                                .build();
                        outList.add(newItem);
                    }
                    groupStanzaExecutor.execute(() -> {
                        processGroupFeedItems(outList, msg.getId(), false);
                    });
                    handled = true;
                } else if (msg.hasChatStanza()) {
                    Log.i("connection: got chat stanza " + ProtoPrinter.toString(msg));
                    ChatStanza chatStanza = msg.getChatStanza();
                    String senderName = chatStanza.getSenderName();
                    String senderPhone = chatStanza.getSenderPhone();
                    UserId fromUserId = new UserId(Long.toString(msg.getFromUid()));

                    Log.i("message " + msg.getId() + " from version " + chatStanza.getSenderClientVersion() + ": " + chatStanza.getSenderLogInfo());

                    if (!TextUtils.isEmpty(senderName)) {
                        connectionObservers.notifyUserNamesReceived(Collections.singletonMap(fromUserId, senderName));
                    }

                    if (!TextUtils.isEmpty(senderPhone)) {
                        connectionObservers.notifyUserPhonesReceived(Collections.singletonMap(fromUserId, senderPhone));
                    }

                    chatStanzaExecutor.execute(() -> {
                        Message message = ChatMessageProtocol.getInstance().parseMessage(chatStanza, msg.getId(), fromUserId);
                        if (message == null) {
                            Log.e("connection: got empty message");
                            sendAck(msg.getId());
                            return;
                        }
                        processMentions(message.mentions);
                        connectionObservers.notifyIncomingMessageReceived(message);
                    });

                    handled = true;
                } else if (msg.hasChatRetract()) {
                    Log.i("connection: got chat retract " + ProtoPrinter.toString(msg));
                    ChatRetract retractStanza = msg.getChatRetract();
                    UserId fromUserId = new UserId(Long.toString(msg.getFromUid()));
                    String msgId = retractStanza.getId();
                    connectionObservers.notifyMessageRetracted(fromUserId, fromUserId, msgId, msg.getId());
                    handled = true;
                } else if (msg.hasGroupchatRetract()) {
                    Log.i("connection: got group chat retract " + ProtoPrinter.toString(msg));
                    GroupChatRetract retractStanza = msg.getGroupchatRetract();
                    UserId fromUserId = new UserId(Long.toString(msg.getFromUid()));
                    String msgId = retractStanza.getId();
                    GroupId groupId = new GroupId(retractStanza.getGid());
                    connectionObservers.notifyMessageRetracted(groupId, fromUserId, msgId, msg.getId());
                    handled = true;
                } else if (msg.hasGroupChatStanza()) {
                    Log.i("connection: got group chat " + ProtoPrinter.toString(msg));
                    GroupChatStanza groupChatStanza = msg.getGroupChatStanza();
                    String senderName = groupChatStanza.getSenderName();
                    String senderPhone = groupChatStanza.getSenderPhone();
                    UserId fromUserId = new UserId(Long.toString(msg.getFromUid()));
                    Log.i("message " + msg.getId() + " from version " + groupChatStanza.getSenderClientVersion() + ": " + groupChatStanza.getSenderLogInfo());

                    if (!TextUtils.isEmpty(senderName)) {
                        connectionObservers.notifyUserNamesReceived(Collections.singletonMap(fromUserId, senderName));
                    }

                    if (!TextUtils.isEmpty(senderPhone)) {
                        connectionObservers.notifyUserPhonesReceived(Collections.singletonMap(fromUserId, senderPhone));
                    }

                    groupStanzaExecutor.execute(() -> {
                        Message message = ChatMessageProtocol.getInstance().parseGroupMessage(groupChatStanza, msg.getId(), fromUserId);
                        if (message == null) {
                            Log.e("connection: got empty message");
                            sendAck(msg.getId());
                            return;
                        }
                        processMentions(message.mentions);
                        connectionObservers.notifyIncomingMessageReceived(message);
                    });
                    handled = true;
                } else if (msg.hasDeliveryReceipt()) {
                    Log.i("connection: got delivery receipt " + ProtoPrinter.toString(msg));
                    DeliveryReceipt deliveryReceipt = msg.getDeliveryReceipt();
                    final String threadId = deliveryReceipt.getThreadId();
                    final UserId userId = getUserId(Long.toString(msg.getFromUid()));
                    connectionObservers.notifyOutgoingMessageDelivered(TextUtils.isEmpty(threadId) ? userId : ChatId.fromNullable(threadId), userId, deliveryReceipt.getId(), deliveryReceipt.getTimestamp() * 1000L, msg.getId());
                    handled = true;
                } else if (msg.hasSeenReceipt()) {
                    Log.i("connection: got seen receipt " + ProtoPrinter.toString(msg));
                    SeenReceipt seenReceipt = msg.getSeenReceipt();
                    final String threadId = seenReceipt.getThreadId();
                    final UserId userId = getUserId(Long.toString(msg.getFromUid()));
                    final long timestamp = seenReceipt.getTimestamp() * 1000L;
                    if (FEED_THREAD_ID.equals(threadId)) {
                        connectionObservers.notifyOutgoingPostSeen(userId, seenReceipt.getId(), timestamp, msg.getId());
                    } else {
                        connectionObservers.notifyOutgoingMessageSeen(TextUtils.isEmpty(threadId) ? userId : ChatId.fromNullable(threadId), userId, seenReceipt.getId(), timestamp, msg.getId());
                    }
                    handled = true;
                } else if (msg.hasScreenshotReceipt()) {
                    Log.i("connection: got screenshot receipt " + ProtoPrinter.toString(msg));
                    ScreenshotReceipt screenshotReceipt = msg.getScreenshotReceipt();
                    final String threadId = screenshotReceipt.getThreadId();
                    final UserId userId = getUserId(Long.toString(msg.getFromUid()));
                    final long timestamp = screenshotReceipt.getTimestamp() * 1000L;
                    if (FEED_THREAD_ID.equals(threadId)) {
                        connectionObservers.notifyOutgoingMomentScreenshotted(userId, screenshotReceipt.getId(), timestamp, msg.getId());
                    }
                } else if (msg.hasPlayedReceipt()) {
                    PlayedReceipt playedReceipt = msg.getPlayedReceipt();
                    final String threadId = playedReceipt.getThreadId();
                    final UserId userId = getUserId(Long.toString(msg.getFromUid()));
                    final long timestamp = playedReceipt.getTimestamp() * 1000L;
                    if (FEED_THREAD_ID.equals(threadId)) {
                        Log.w("connection: received played receipt for a feed post");
                    } else {
                        connectionObservers.notifyOutgoingMessagePlayed(TextUtils.isEmpty(threadId) ? userId : ChatId.fromNullable(threadId), userId, playedReceipt.getId(), timestamp, msg.getId());
                    }
                    handled = true;
                } else if (msg.hasContactHash()) {
                    Log.i("connection: got contact hash " + ProtoPrinter.toString(msg));
                    ContactHash contactHash = msg.getContactHash();
                    connectionObservers.notifyContactsChanged(new ArrayList<>(), Collections.singletonList(Base64.encodeToString(contactHash.getHash().toByteArray(), Base64.NO_WRAP)), msg.getId());
                    handled = true;
                } else if (msg.hasContactList()) {
                    Log.i("connection: got contact list " + ProtoPrinter.toString(msg));
                    ContactList contactList = msg.getContactList();
                    List<Contact> contacts = contactList.getContactsList();
                    List<ContactInfo> infos = new ArrayList<>();
                    for (Contact contact : contacts) {
                        infos.add(new ContactInfo(contact));
                    }
                    if (msg.getType() == Msg.Type.HEADLINE) {
                        connectionObservers.notifyInvitesAccepted(infos, msg.getId());
                    } else {
                        connectionObservers.notifyContactsChanged(infos, new ArrayList<>(), msg.getId());
                    }
                    handled = true;
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

                    Map<UserId, String> nameMap = new HashMap<>();
                    long rawUserId = groupStanza.getSenderUid();
                    UserId senderUserId = rawUserId != 0 ? getUserId(Long.toString(rawUserId)) : null;
                    String senderName = groupStanza.getSenderName();
                    if (!TextUtils.isEmpty(senderName) && rawUserId != 0) {
                        nameMap.put(senderUserId, senderName);
                    }

                    String ackId = msg.getId();
                    GroupId groupId = new GroupId(groupStanza.getGid());
                    List<GroupMember> members = groupStanza.getMembersList();
                    List<MemberElement> elements = new ArrayList<>();
                    if (members != null) {
                        for (GroupMember member : members) {
                            elements.add(new MemberElement(member));
                            long rawMemberId = member.getUid();
                            UserId memberUserId = rawMemberId != 0 ? getUserId(Long.toString(rawMemberId)) : null;
                            String memberName = member.getName();
                            if (!TextUtils.isEmpty(memberName) && rawMemberId != 0) {
                                nameMap.put(memberUserId, member.getName());
                            }
                        }
                    }

                    if (!nameMap.isEmpty()) {
                        connectionObservers.notifyUserNamesReceived(nameMap);
                    }

                    handled = true;
                    if (groupStanza.getAction().equals(GroupStanza.Action.CREATE)) {
                        if (groupStanza.getGroupType().equals(GroupStanza.GroupType.FEED)) {
                            connectionObservers.notifyGroupFeedCreated(groupId, groupStanza.getName(), groupStanza.getAvatarId(), elements, Preconditions.checkNotNull(senderUserId), senderName, groupStanza.hasExpiryInfo() ? groupStanza.getExpiryInfo() : null, ackId);
                        } else {
                            connectionObservers.notifyGroupChatCreated(groupId, groupStanza.getName(), groupStanza.getAvatarId(), elements, Preconditions.checkNotNull(senderUserId), senderName, groupStanza.hasExpiryInfo() ? groupStanza.getExpiryInfo() : null, ackId);
                        }
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.MODIFY_MEMBERS)) {
                        HistoryResend historyResend = groupStanza.hasHistoryResend() ? groupStanza.getHistoryResend() : null;
                        connectionObservers.notifyGroupMemberChangeReceived(groupId, groupStanza.getName(), groupStanza.getAvatarId(), elements, Preconditions.checkNotNull(senderUserId), senderName, historyResend, groupStanza.getGroupType(), ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.LEAVE)) {
                        connectionObservers.notifyGroupMemberLeftReceived(groupId, elements, groupStanza.getGroupType(), ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.MODIFY_ADMINS)) {
                        connectionObservers.notifyGroupAdminChangeReceived(groupId, elements, Preconditions.checkNotNull(senderUserId), senderName, groupStanza.getGroupType(), ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.CHANGE_NAME)) {
                        connectionObservers.notifyGroupNameChangeReceived(groupId, groupStanza.getName(), Preconditions.checkNotNull(senderUserId), senderName, groupStanza.getGroupType(), ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.CHANGE_AVATAR)) {
                        connectionObservers.notifyGroupAvatarChangeReceived(groupId, groupStanza.getAvatarId(), Preconditions.checkNotNull(senderUserId), senderName, groupStanza.getGroupType(), ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.AUTO_PROMOTE_ADMINS)) {
                        connectionObservers.notifyGroupAdminAutoPromoteReceived(groupId, elements, groupStanza.getGroupType(), ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.DELETE)) {
                        connectionObservers.notifyGroupDeleteReceived(groupId, Preconditions.checkNotNull(senderUserId), senderName, groupStanza.getGroupType(), ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.JOIN)) {
                        connectionObservers.notifyGroupMemberJoinReceived(groupId, groupStanza.getName(), groupStanza.getAvatarId(), elements, Preconditions.checkNotNull(senderUserId), senderName, groupStanza.getGroupType(), ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.SET_BACKGROUND)) {
                        Background background = null;
                        try {
                            background = Background.parseFrom(groupStanza.getBackgroundBytes());
                        } catch (InvalidProtocolBufferException e) {
                            Log.e("connection: invalid background received", e);
                        }
                        connectionObservers.notifyGroupBackgroundChangeReceived(groupId, background == null ? 0 : background.getTheme(), senderUserId, senderName, ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.CHANGE_DESCRIPTION)) {
                        connectionObservers.notifyGroupDescriptionChanged(groupId, groupStanza.getDescription(), Preconditions.checkNotNull(senderUserId), senderName, groupStanza.getGroupType(), ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.CHANGE_EXPIRY)
                            && groupStanza.hasExpiryInfo()) {
                        connectionObservers.notifyGroupExpiryChanged(groupId, groupStanza.getExpiryInfo(), senderUserId, senderName, ackId);
                    } else {
                        handled = false;
                        Log.w("Unrecognized group stanza action " + groupStanza.getAction());
                    }
                } else if (msg.hasRerequest()) {
                    Log.i("connection: got rerequest message " + ProtoPrinter.toString(msg));
                    UserId userId = getUserId(Long.toString(msg.getFromUid()));
                    Rerequest rerequest = msg.getRerequest();

                    PublicEdECKey peerIdentityKey = new PublicEdECKey(rerequest.getIdentityKey().toByteArray());
                    long otpkIdL = rerequest.getOneTimePreKeyId();
                    Integer otpkId = otpkIdL == 0 ? null : (int) otpkIdL;
                    byte[] sessionSetupKey = rerequest.getSessionSetupEphemeralKey().toByteArray();
                    byte[] messageEphemeralKey = rerequest.getMessageEphemeralKey().toByteArray();

                    connectionObservers.notifyMessageRerequest(rerequest.getContentType(), userId, rerequest.getId(), peerIdentityKey, otpkId, sessionSetupKey, messageEphemeralKey, msg.getId());
                    handled = true;
                } else if (msg.hasGroupFeedRerequest()) {
                    Log.i("connection: got group rerequest message " + ProtoPrinter.toString(msg));
                    UserId userId = getUserId(Long.toString(msg.getFromUid()));
                    GroupFeedRerequest groupFeedRerequest = msg.getGroupFeedRerequest();

                    String contentId = groupFeedRerequest.getId();
                    String rawGroupId = groupFeedRerequest.getGid();
                    GroupId groupId = new GroupId(rawGroupId);
                    boolean senderStateIssue = GroupFeedRerequest.RerequestType.SENDER_STATE == groupFeedRerequest.getRerequestType();
                    boolean historicalContent = GroupFeedRerequest.ContentType.HISTORY_RESEND == groupFeedRerequest.getContentType();

                    if (historicalContent) {
                        connectionObservers.notifyGroupFeedHistoryRerequest(userId, groupId, contentId, senderStateIssue, msg.getId());
                    } else {
                        connectionObservers.notifyGroupFeedRerequest(groupFeedRerequest.getContentType(), userId, groupId, contentId, senderStateIssue, msg.getId());
                    }
                    handled = true;
                } else if (msg.hasHomeFeedRerequest()) {
                    Log.i("connection: got home rerequest message " + ProtoPrinter.toString(msg));
                    UserId userId = getUserId(Long.toString(msg.getFromUid()));
                    HomeFeedRerequest homeFeedRerequest = msg.getHomeFeedRerequest();
                    boolean senderStateIssue = HomeFeedRerequest.RerequestType.SENDER_STATE == homeFeedRerequest.getRerequestType();

                    String contentId = homeFeedRerequest.getId();
                    connectionObservers.notifyHomeFeedRerequest(homeFeedRerequest.getContentType(), userId, contentId, senderStateIssue, msg.getId());
                    handled = true;
                } else if (msg.hasRequestLogs()) {
                    Log.i("connection: got log request message " + ProtoPrinter.toString(msg));
                    LogUploaderWorker.uploadLogs(AppContext.getInstance().get(), msg.getId());
                    handled = true;
                } else if (msg.hasIncomingCall()) {
                    // TODO(nikola): Discuss this with the android team. I would rather do this
                    // } else if (msg.getType() == Msg.Type.CALL) {
                    //      connectionObservers.notifyCallMsg(msg);
                    Log.i("connection: got incoming call message " + ProtoPrinter.toString(msg));
                    UserId peerUid = getUserId(Long.toString(msg.getFromUid()));
                    connectionObservers.notifyIncomingCall(peerUid, msg.getIncomingCall(), msg.getId());
                    handled = true;
                } else if (msg.hasCallRinging()) {
                    Log.i("connection: got call ringing message " + ProtoPrinter.toString(msg));
                    UserId peerUid = getUserId(Long.toString(msg.getFromUid()));
                    connectionObservers.notifyCallRinging(peerUid, msg.getCallRinging(), msg.getId());
                    handled = true;
                } else if (msg.hasPublicFeedUpdate()) {
                    Log.i("connection: got public feed update message " + ProtoPrinter.toString(msg));
                    connectionObservers.notifyPublicFeedUpdate(msg.getPublicFeedUpdate(), msg.getId());
                    handled = true;
                } else if (msg.hasAnswerCall()) {
                    Log.i("connection: got answer call message " + ProtoPrinter.toString(msg));
                    UserId peerUid = getUserId(Long.toString(msg.getFromUid()));
                    connectionObservers.notifyAnswerCall(peerUid, msg.getAnswerCall(), msg.getId());
                    handled = true;
                } else if (msg.hasEndCall()) {
                    Log.i("connection: got end call message " + ProtoPrinter.toString(msg));
                    UserId peerUid = getUserId(Long.toString(msg.getFromUid()));
                    connectionObservers.notifyEndCall(peerUid, msg.getEndCall(), msg.getId());
                    handled = true;
                } else if (msg.hasIceCandidate()) {
                    Log.i("connection: got ice candidate message " + ProtoPrinter.toString(msg));
                    UserId peerUid = getUserId(Long.toString(msg.getFromUid()));
                    connectionObservers.notifyIceCandidate(peerUid, msg.getIceCandidate(), msg.getId());
                    handled = true;
                } else if (msg.hasIceRestartOffer()) {
                    Log.i("connection: got ice restart offer message " + ProtoPrinter.toString(msg));
                    UserId peerUid = getUserId(Long.toString(msg.getFromUid()));
                    connectionObservers.notifyIceRestartOffer(peerUid, msg.getIceRestartOffer(), msg.getId());
                    handled = true;
                } else if (msg.hasIceRestartAnswer()) {
                    Log.i("connection: got ice restart answer message " + ProtoPrinter.toString(msg));
                    UserId peerUid = getUserId(Long.toString(msg.getFromUid()));
                    connectionObservers.notifyIceRestartAnswer(peerUid, msg.getIceRestartAnswer(), msg.getId());
                    handled = true;
                } else if (msg.hasHoldCall()) {
                    Log.i("connection: got hold call message " + ProtoPrinter.toString(msg));
                    UserId peerUid = getUserId(Long.toString(msg.getFromUid()));
                    connectionObservers.notifyHoldCall(peerUid, msg.getHoldCall(), msg.getId());
                    handled = true;
                } else if (msg.hasMuteCall()) {
                    Log.i("connection: got mute call message " + ProtoPrinter.toString(msg));
                    UserId peerUid = getUserId(Long.toString(msg.getFromUid()));
                    connectionObservers.notifyMuteCall(peerUid, msg.getMuteCall(), msg.getId());
                    handled = true;
                } else if (msg.hasGroupFeedHistory()) {
                    bgWorkers.execute(() -> {
                        GroupFeedHistory groupFeedHistory = msg.getGroupFeedHistory();
                        String historyId = groupFeedHistory.getId();
                        ByteString encrypted = groupFeedHistory.getEncPayload(); // TODO(jack): Verify plaintext matches if present
                        if (encrypted != null && encrypted.size() > 0) {
                            GroupId groupId = new GroupId(groupFeedHistory.getGid());
                            UserId peerUserId = new UserId(Long.toString(msg.getFromUid()));

                            byte[] identityKeyBytes = groupFeedHistory.getPublicKey().toByteArray();
                            PublicEdECKey identityKey = identityKeyBytes == null || identityKeyBytes.length == 0 ? null : new PublicEdECKey(identityKeyBytes);
                            SignalSessionSetupInfo signalSessionSetupInfo = new SignalSessionSetupInfo(identityKey, groupFeedHistory.getOneTimePreKeyId());

                            String errorMessage;
                            try {
                                byte[] decrypted = SignalSessionManager.getInstance().decryptMessage(encrypted.toByteArray(), peerUserId, signalSessionSetupInfo);
                                GroupFeedItems groupFeedItems = GroupFeedItems.parseFrom(decrypted);

                                List<GroupFeedItem> inList = groupFeedItems.getItemsList();
                                List<GroupFeedItem> outList = new ArrayList<>();
                                for (GroupFeedItem item : inList) {
                                    if (item.hasComment() && item.getComment().getPublisherUid() != msg.getFromUid()) {
                                        Log.w("Dropping group history comment " + item.getComment().getId() + " due to publisher and from fields not matching");
                                        continue;
                                    } else if (item.hasPost() && item.getPost().getPublisherUid() != msg.getFromUid()) {
                                        Log.w("Dropping group history post " + item.getPost().getId() + " due to publisher and from fields not matching");
                                        continue;
                                    }
                                    GroupFeedItem newItem = GroupFeedItem.newBuilder(item)
                                            .setGid(groupFeedHistory.getGid())
                                            .build();
                                    outList.add(newItem);
                                }
                                processGroupFeedItems(outList, msg.getId(), true);
                            } catch (CryptoException e) {
                                Log.e("Failed to decrypt group feed history", e);
                                SignalSessionManager.getInstance().tearDownSession(peerUserId);
                                errorMessage = e.getMessage();
                                Log.sendErrorReport("Group history decryption failed: " + errorMessage);
                                // TODO(jack): Stats
//                                    stats.reportGroupDecryptError(errorMessage, true, senderPlatform, senderVersion);

                                Log.i("Rerequesting group history " + historyId);
                                ContentDb contentDb = ContentDb.getInstance();
                                int count;
                                count = contentDb.getHistoryResendRerequestCount(peerUserId, historyId);
                                count += 1;
                                contentDb.setHistoryResendRerequestCount(peerUserId, historyId, count);
                                GroupFeedSessionManager.getInstance().sendGroupHistoryPayloadRerequest(peerUserId, historyId, e.teardownKey);
                                sendAck(msg.getId());
                            } catch (InvalidProtocolBufferException e) {
                                Log.e("Failed to parse group feed items for group feed history", e);
                                sendAck(msg.getId());
                            }
                        }
                    });
                    handled = true;
                } else if (msg.hasHistoryResend()) {
                    UserId peerUid = getUserId(Long.toString(msg.getFromUid()));
                    connectionObservers.notifyHistoryResend(msg.getHistoryResend(), peerUid, msg.getId());
                    handled = true;
                } else if (msg.hasContentMissing()) {
                    UserId peerUid = getUserId(Long.toString(msg.getFromUid()));
                    ContentMissing contentMissing = msg.getContentMissing();
                    connectionObservers.notifyContentMissing(contentMissing.getContentType(), peerUid, contentMissing.getContentId(), msg.getId());
                    handled = true;
                }  else if (msg.hasMomentNotification()) {
                    Log.i("connection: got moment notification " + ProtoPrinter.toString(msg));
                    connectionObservers.notifyMomentNotificationReceived(msg.getMomentNotification(), msg.getId());

                    handled = true;
                } else if (msg.hasWebStanza())  {
                    WebStanza webstanza = msg.getWebStanza();
                    if (webstanza.hasNoiseMessage()) {
                        NoiseMessage noiseMessage = webstanza.getNoiseMessage();
                        try {
                            WebClientManager webClientManager = WebClientManager.getInstance();
                            if (noiseMessage.getMessageType() == NoiseMessage.MessageType.IK_B) {
                                webClientManager.finishHandshake(noiseMessage.getContent().toByteArray());
                            } else if (noiseMessage.getMessageType() == NoiseMessage.MessageType.KK_A) {
                                webClientManager.receiveKKHandshake(noiseMessage.getContent().toByteArray());
                            } else if (noiseMessage.getMessageType() == NoiseMessage.MessageType.KK_B) {
                                webClientManager.finishHandshake(noiseMessage.getContent().toByteArray());
                            }
                            webClientManager.setIsConnectedToWebClient(true);
                            sendAck(msg.getId());
                            handled = true;
                        } catch (NoiseException | BadPaddingException | ShortBufferException | NoSuchAlgorithmException | CryptoException e) {
                            Log.e("connection: error finishing handshake with web client", e);
                            throw new RuntimeException(e);
                        }
                    } else if (webstanza.getContent() != null) {
                        try {
                            WebClientManager webClientManager = WebClientManager.getInstance();
                            webClientManager.handleIncomingWebContainer(webstanza.getContent().toByteArray());
                            handled = true;
                            sendAck(msg.getId());
                        } catch (ShortBufferException | BadPaddingException | InvalidProtocolBufferException | NoiseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else if (msg.hasProfileUpdate()) {
                    ProfileUpdate profileUpdate = msg.getProfileUpdate();
                    connectionObservers.notifyProfileUpdateReceived(profileUpdate, msg.getId());
                    handled = true;
                } else if (msg.hasAiImage()) {
                    AiImage aiImage = msg.getAiImage();
                    connectionObservers.notifyAiImageReceived(aiImage.getId(), aiImage.getImage().toByteArray(), msg.getId());
                    handled = true;
                } else if (msg.hasHalloappProfileUpdate()) {
                    Log.i("connection: got halloapp profile update " + ProtoPrinter.toString(msg));
                    HalloappProfileUpdate profileUpdate = msg.getHalloappProfileUpdate();
                    connectionObservers.notifyHalloappProfileUpdateReceived(profileUpdate, msg.getId());
                    handled = true;
                } else if (msg.hasFriendListRequest()) {
                    Log.i("connection: got friend list request " + ProtoPrinter.toString(msg));
                    FriendListRequest friendList = msg.getFriendListRequest();
                    connectionObservers.notifyFriendListRequestReceived(friendList, msg.getId());
                    handled = true;
                }
            }
            if (!handled) {
                Log.i("connection: did not handle message " + ProtoPrinter.toString(msg));
                sendAck(msg.getId());
            }
        }

        private void processFeedPubSubItems(@NonNull List<com.halloapp.proto.server.FeedItem> items, @NonNull String ackId) {
            final List<Post> posts = new ArrayList<>();
            final List<Comment> comments = new ArrayList<>();
            final List<Comment> publicComments = new ArrayList<>();
            final Map<UserId, String> names = new HashMap<>();
            boolean isPublic;
            boolean senderStateIssue = false;
            for (com.halloapp.proto.server.FeedItem item : items) {
                String senderAgent = item.getSenderClientVersion();
                String senderPlatform = senderAgent == null ? "" : senderAgent.contains("Android") ? "android" : senderAgent.contains("iOS") ? "ios" : "";
                String senderVersion = senderPlatform.equals("android") ? senderAgent.split("Android")[1] : senderPlatform.equals("ios") ? senderAgent.split("iOS")[1] : "";
                isPublic = item.getAction().equals(com.halloapp.proto.server.FeedItem.Action.PUBLIC_UPDATE_PUBLISH) ||
                            item.getAction().equals(com.halloapp.proto.server.FeedItem.Action.PUBLIC_UPDATE_RETRACT);
                if (item.hasSenderState()) {
                    SenderStateWithKeyInfo senderStateWithKeyInfo = item.getSenderState();

                    long publisherUid;
                    if (item.hasComment()) {
                        publisherUid = item.getComment().getPublisherUid();
                    } else if (item.hasPost()) {
                        publisherUid = item.getPost().getPublisherUid();
                    } else {
                        Log.e("HomeFeedItem " + ProtoPrinter.toString(item) + " has neither post nor comment");
                        continue;
                    }
                    UserId publisherUserId = new UserId(Long.toString(publisherUid));

                    byte[] encSenderState = senderStateWithKeyInfo.getEncSenderState().toByteArray();
                    try {
                        byte[] peerPublicIdentityKey = senderStateWithKeyInfo.getPublicKey().toByteArray();
                        long oneTimePreKeyId = senderStateWithKeyInfo.getOneTimePreKeyId();
                        SignalSessionSetupInfo signalSessionSetupInfo = peerPublicIdentityKey == null || peerPublicIdentityKey.length == 0 ? null : new SignalSessionSetupInfo(new PublicEdECKey(peerPublicIdentityKey), (int) oneTimePreKeyId);
                        byte[] senderStateDec = SignalSessionManager.getInstance().decryptMessage(encSenderState, publisherUserId, signalSessionSetupInfo);
                        SenderState senderState = SenderState.parseFrom(senderStateDec);
                        SenderKey senderKey = senderState.getSenderKey();
                        int currentChainIndex = senderState.getCurrentChainIndex();
                        byte[] chainKey = senderKey.getChainKey().toByteArray();
                        byte[] publicSignatureKeyBytes = senderKey.getPublicSignatureKey().toByteArray();
                        PublicEdECKey publicSignatureKey = new PublicEdECKey(publicSignatureKeyBytes);
                        Log.i("Received sender state with current chain index of " + currentChainIndex + " from " + publisherUid);

                        boolean favorites = item.getPost().getAudience().getType().equals(Audience.Type.ONLY);
                        EncryptedKeyStore.getInstance().edit()
                                .setPeerHomeCurrentChainIndex(favorites, publisherUserId, currentChainIndex)
                                .setPeerHomeChainKey(favorites, publisherUserId, chainKey)
                                .setPeerHomeSigningKey(favorites, publisherUserId, publicSignatureKey)
                                .apply();
                    } catch (CryptoException e) {
                        Log.e("Failed to decrypt sender state for " + ProtoPrinter.toString(item), e);
                        senderStateIssue = true;
                    } catch (InvalidProtocolBufferException e) {
                        Log.e("Failed to parse sender state for " + ProtoPrinter.toString(item), e);
                    }
                }

                if (item.getAction().equals(com.halloapp.proto.server.FeedItem.Action.RETRACT) ||
                    item.getAction().equals(com.halloapp.proto.server.FeedItem.Action.PUBLIC_UPDATE_RETRACT)) {
                    if (item.hasComment()) {
                        com.halloapp.proto.server.Comment comment = item.getComment();
                        UserId publisherUserId = new UserId(Long.toString(comment.getPublisherUid()));
                        if (comment.getPublisherUid() != 0 && comment.getPublisherName() != null) {
                            names.put(publisherUserId, comment.getPublisherName());
                        }
                        if (isPublic) {
                            connectionObservers.notifyPublicCommentRetracted(comment.getId(), comment.getPostId());
                        } else {
                            connectionObservers.notifyCommentRetracted(comment.getId(), publisherUserId, comment.getPostId(), comment.getTimestamp() * 1000L);
                        }
                    } else if (item.hasPost()) {
                        com.halloapp.proto.server.Post post = item.getPost();
                        UserId publisherUserId = new UserId(Long.toString(post.getPublisherUid()));
                        if (post.getPublisherUid() != 0 && post.getPublisherName() != null) {
                            names.put(publisherUserId, post.getPublisherName());
                        }
                        if (isPublic) {
                            connectionObservers.notifyPublicPostRetracted(post.getId());
                        } else {
                            connectionObservers.notifyPostRetracted(publisherUserId, post.getId(), post.getTimestamp() * 1000L);
                        }
                    }
                } else if (item.getAction().equals(com.halloapp.proto.server.FeedItem.Action.SHARE) ||
                            item.getAction().equals(com.halloapp.proto.server.FeedItem.Action.PUBLISH) ||
                            item.getAction().equals(com.halloapp.proto.server.FeedItem.Action.PUBLIC_UPDATE_PUBLISH)) {
                    if (item.hasPost()) {
                        com.halloapp.proto.server.Post protoPost = item.getPost();
                        Post post = processPost(protoPost, names, null, senderStateIssue, senderPlatform, senderVersion);
                        if (post != null) {
                            post.seen = item.getAction().equals(com.halloapp.proto.server.FeedItem.Action.PUBLISH) ? Post.SEEN_NO : Post.SEEN_NO_HIDDEN;
                            if (protoPost.hasAudience() && Audience.Type.ONLY.equals(protoPost.getAudience().getType())) {
                                post.setAudience(PrivacyList.Type.ONLY, new ArrayList<>());
                            }
                            posts.add(post);
                        } else {
                            Log.e("connection: invalid post");
                        }

                    } else if (item.hasComment()) {
                        com.halloapp.proto.server.Comment protoComment = item.getComment();
                        Comment comment = processComment(protoComment, names, null, senderStateIssue, senderPlatform, senderVersion, isPublic);
                        if (comment != null) {
                            comment.seen = item.getAction().equals(com.halloapp.proto.server.FeedItem.Action.SHARE);
                            if (isPublic) {
                                publicComments.add(comment);
                            } else {
                                comments.add(comment);
                            }
                        } else {
                            Log.e("connection: invalid comment");
                        }
                    }
                } else if (item.getAction().equals(com.halloapp.proto.server.FeedItem.Action.EXPIRE)) {
                    if (item.hasPost() && !isPublic) {
                        com.halloapp.proto.server.Post protoPost = item.getPost();
                        connectionObservers.notifyPostExpired(protoPost.getId());
                    } else {
                        Log.e("connection: cannot expire non-post");
                    }
                }
            }

            if (!names.isEmpty()) {
                connectionObservers.notifyUserNamesReceived(names);
            }

            if (publicComments.size() > 0) {
                connectionObservers.notifyIncomingPublicFeedItemsReceived(publicComments);
            }
            connectionObservers.notifyIncomingFeedItemsReceived(posts, comments, ackId);
        }

        private Post processPost(com.halloapp.proto.server.Post protoPost, Map<UserId, String> names, @Nullable GroupId groupId, boolean senderStateIssue, @Nullable String senderPlatform, @Nullable String senderVersion) {
            if (protoPost.getPublisherUid() != 0 && protoPost.getPublisherName() != null) {
                names.put(new UserId(Long.toString(protoPost.getPublisherUid())), protoPost.getPublisherName());
            }

            UserId publisherUserId = getUserId(Long.toString(protoPost.getPublisherUid()));
            byte[] payload = protoPost.getPayload().toByteArray();

            String errorMessage = null;
            ContentDb contentDb = ContentDb.getInstance();
            int rerequestCount = contentDb.getPostRerequestCount(groupId, publisherUserId, protoPost.getId());
            if (groupId != null) {
                byte[] encPayload = protoPost.getEncPayload().toByteArray();
                if (encPayload != null && encPayload.length > 0) {
                    Stats stats = Stats.getInstance();
                    try {
                        byte[] decPayload;
                        try {
                            EncryptedPayload encryptedPayload = EncryptedPayload.parseFrom(encPayload);
                            switch (encryptedPayload.getPayloadCase()) {
                                case SENDER_STATE_ENCRYPTED_PAYLOAD: {
                                    byte[] toDecrypt = encryptedPayload.getSenderStateEncryptedPayload().toByteArray();
                                    decPayload = GroupFeedSessionManager.getInstance().decryptMessage(toDecrypt, groupId, publisherUserId);
                                    break;
                                }
                                case ONE_TO_ONE_ENCRYPTED_PAYLOAD: {
                                    byte[] toDecrypt = encryptedPayload.getOneToOneEncryptedPayload().toByteArray();
                                    decPayload = SignalSessionManager.getInstance().decryptMessage(toDecrypt, publisherUserId, null);
                                    break;
                                }
                                default: {
                                    throw new CryptoException("no_accepted_enc_payload");
                                }
                            }
                        } catch (InvalidProtocolBufferException e) {
                            throw new CryptoException("grp_invalid_proto", e);
                        }
                        if (payload.length > 0 && !Arrays.equals(payload, decPayload)) {
                            Log.e("Group Feed Encryption plaintext and decrypted differ");
                            throw new CryptoException("grp_post_payload_differs");
                        }
                        stats.reportGroupPostDecryptSuccess(senderPlatform, senderVersion);
                        payload = decPayload;
                    } catch (CryptoException e) {
                        Log.e("Failed to decrypt group post", e);
                        errorMessage = (senderStateIssue ? "sender_state_" : "") + e.getMessage();
                        Log.sendErrorReport("Group post decryption failed: " + errorMessage);
                        stats.reportGroupPostDecryptError(errorMessage, senderPlatform, senderVersion);

                        Log.i("Rerequesting post " + protoPost.getId());
                        rerequestCount += 1;
                        if (senderStateIssue) {
                            Log.i("Tearing down session because of sender state issue");
                            SignalSessionManager.getInstance().tearDownSession(publisherUserId);
                        }
                        GroupFeedSessionManager.getInstance().sendPostRerequest(publisherUserId, groupId, protoPost.getId(), rerequestCount, senderStateIssue);

                        if (!ServerProps.getInstance().getUsePlaintextGroupFeed()) {
                            Post post = new Post(
                                    0,
                                    publisherUserId,
                                    protoPost.getId(),
                                    1000L * protoPost.getTimestamp(),
                                    Post.TRANSFERRED_DECRYPT_FAILED,
                                    Post.SEEN_NO,
                                    ""
                            );
                            post.clientVersion = Constants.FULL_VERSION;
                            post.senderPlatform = senderPlatform;
                            post.senderVersion = senderVersion;
                            post.failureReason = errorMessage;
                            post.rerequestCount = rerequestCount;
                            return post;
                        }
                    }
                }
            } else { // is a home feed post
                boolean favorites = protoPost.getAudience().getType().equals(Audience.Type.ONLY);
                byte[] encPayload = protoPost.getEncPayload().toByteArray();
                if (encPayload != null && encPayload.length > 0) {
                    Stats stats = Stats.getInstance();
                    try {
                        byte[] decPayload;
                        try {
                            EncryptedPayload encryptedPayload = EncryptedPayload.parseFrom(encPayload);
                            switch (encryptedPayload.getPayloadCase()) {
                                case SENDER_STATE_ENCRYPTED_PAYLOAD: {
                                    byte[] toDecrypt = encryptedPayload.getSenderStateEncryptedPayload().toByteArray();
                                    decPayload = HomeFeedSessionManager.getInstance().decryptPost(toDecrypt, favorites, publisherUserId);
                                    break;
                                }
                                case ONE_TO_ONE_ENCRYPTED_PAYLOAD: {
                                    byte[] toDecrypt = encryptedPayload.getOneToOneEncryptedPayload().toByteArray();
                                    decPayload = SignalSessionManager.getInstance().decryptMessage(toDecrypt, publisherUserId, null);
                                    break;
                                }
                                default: {
                                    throw new CryptoException("no_accepted_enc_payload");
                                }
                            }
                        } catch (InvalidProtocolBufferException e) {
                            throw new CryptoException("home_invalid_proto", e);
                        }
                        if (payload.length > 0 && !Arrays.equals(payload, decPayload)) {
                            Log.e("Home Feed Encryption plaintext and decrypted differ");
                            throw new CryptoException("home_post_payload_differs");
                        }
                        stats.reportHomeDecryptSuccess(false, senderPlatform, senderVersion);
                        payload = decPayload;
                    } catch (CryptoException e) {
                        Log.e("Failed to decrypt home post", e);
                        errorMessage = e.getMessage();
                        Log.sendErrorReport("Home post decryption failed: " + errorMessage);
                        stats.reportHomeDecryptError(errorMessage, false, senderPlatform, senderVersion);

                        Log.i("Rerequesting post " + protoPost.getId());
                        rerequestCount += 1;
                        if (senderStateIssue) {
                            Log.i("Tearing down session because of sender state issue");
                            SignalSessionManager.getInstance().tearDownSession(publisherUserId);
                        }
                        HomeFeedSessionManager.getInstance().sendPostRerequest(publisherUserId, favorites, protoPost.getId(), rerequestCount, senderStateIssue);

                        if (!ServerProps.getInstance().getUsePlaintextHomeFeed()) {
                            Post post = new Post(
                                    0,
                                    publisherUserId,
                                    protoPost.getId(),
                                    1000L * protoPost.getTimestamp(),
                                    Post.TRANSFERRED_DECRYPT_FAILED,
                                    Post.SEEN_NO,
                                    ""
                            );
                            post.clientVersion = Constants.FULL_VERSION;
                            post.senderPlatform = senderPlatform;
                            post.senderVersion = senderVersion;
                            post.failureReason = errorMessage;
                            post.rerequestCount = rerequestCount;
                            return post;
                        }
                    }
                }
            }

            final byte[] protoHash = CryptoUtils.sha256(payload);

            Container container;
            try {
                container = Container.parseFrom(payload);
            } catch (InvalidProtocolBufferException e) {
                Log.e("connection: invalid post payload", e);
                return null;
            }
            UserId posterUserId = getUserId(Long.toString(protoPost.getPublisherUid()));
            long timeStamp = 1000L * protoPost.getTimestamp();
            Post post;
            if (container.hasKMomentContainer()) {
                KMomentContainer katchupContainer = container.getKMomentContainer();
                KatchupPost katchupPost = feedContentParser.parseKatchupPost(protoPost.getId(), posterUserId, timeStamp, katchupContainer, errorMessage != null);
                MomentInfo momentInfo = protoPost.getMomentInfo();
                katchupPost.timeTaken = momentInfo.getTimeTaken();
                katchupPost.numSelfieTakes = (int) momentInfo.getNumSelfieTakes();
                katchupPost.numTakes = (int) momentInfo.getNumTakes();
                katchupPost.notificationId = momentInfo.getNotificationId();
                katchupPost.notificationTimestamp = momentInfo.getNotificationTimestamp() * 1000L;
                katchupPost.contentType = momentInfo.getContentType();
                post = katchupPost;
            } else {
                PostContainer postContainer = container.getPostContainer();

                post = feedContentParser.parsePost(protoPost.getId(), posterUserId, timeStamp, postContainer, errorMessage != null);
                if (!postContainer.getCommentKey().isEmpty()) {
                    post.commentKey = postContainer.getCommentKey().toByteArray();
                }
                if (post instanceof MomentPost) {
                    ((MomentPost) post).unlockedUserId = isMe(Long.toString(protoPost.getMomentUnlockUid())) ? UserId.ME : null;
                }
                if (protoPost.hasAudience()) {
                    Audience audience = protoPost.getAudience();
                    if (Audience.Type.ONLY.equals(audience.getType())) {
                        post.setAudience(PrivacyList.Type.ONLY, new ArrayList<>());
                    }
                }
            }
            post.protoHash = protoHash;
            post.clientVersion = Constants.FULL_VERSION;
            post.senderPlatform = senderPlatform;
            post.senderVersion = senderVersion;
            post.failureReason = errorMessage;
            post.rerequestCount = rerequestCount;
            post.psaTag = protoPost.getPsaTag();
            return post;
        }

        private Comment processComment(com.halloapp.proto.server.Comment protoComment, Map<UserId, String> names, @Nullable GroupId groupId, boolean senderStateIssue, @Nullable String senderPlatform, @Nullable String senderVersion, boolean isPublic) {
            if (protoComment.getPublisherUid() != 0 && protoComment.getPublisherName() != null) {
                names.put(new UserId(Long.toString(protoComment.getPublisherUid())), protoComment.getPublisherName());
            }

            UserId publisherUserId = getUserId(Long.toString(protoComment.getPublisherUid()));
            byte[] payload = protoComment.getPayload().toByteArray();

            String errorMessage = null;
            ContentDb contentDb = ContentDb.getInstance();
            int rerequestCount = contentDb.getCommentRerequestCount(groupId, publisherUserId, protoComment.getId());
            if (groupId != null) {
                byte[] encPayload = protoComment.getEncPayload().toByteArray();
                if (encPayload != null && encPayload.length > 0) {
                    Stats stats = Stats.getInstance();
                    try {
                        byte[] decPayload;
                        try {
                            EncryptedPayload encryptedPayload = EncryptedPayload.parseFrom(encPayload);
                            switch (encryptedPayload.getPayloadCase()) {
                                case SENDER_STATE_ENCRYPTED_PAYLOAD: {
                                    byte[] toDecrypt = encryptedPayload.getSenderStateEncryptedPayload().toByteArray();
                                    decPayload = GroupFeedSessionManager.getInstance().decryptMessage(toDecrypt, groupId, publisherUserId);
                                    break;
                                }
                                case ONE_TO_ONE_ENCRYPTED_PAYLOAD: {
                                    byte[] toDecrypt = encryptedPayload.getOneToOneEncryptedPayload().toByteArray();
                                    decPayload = SignalSessionManager.getInstance().decryptMessage(toDecrypt, publisherUserId, null);
                                    break;
                                }
                                default: {
                                    throw new CryptoException("no_accepted_enc_payload");
                                }
                            }
                        } catch (InvalidProtocolBufferException e) {
                            throw new CryptoException("grp_invalid_proto", e);
                        }
                        if (payload.length > 0 && !Arrays.equals(payload, decPayload)) {
                            Log.e("Group Feed Encryption plaintext and decrypted differ");
                            throw new CryptoException("grp_cmnt_payload_differs");
                        }
                        stats.reportGroupCommentDecryptSuccess(senderPlatform, senderVersion);
                        payload = decPayload;
                    } catch (CryptoException e) {
                        Log.e("Failed to decrypt group comment", e);
                        errorMessage = (senderStateIssue ? "sender_state_" : "") + e.getMessage();
                        Log.sendErrorReport("Group comment decryption failed: " + errorMessage);
                        stats.reportGroupCommentDecryptError(errorMessage, senderPlatform, senderVersion);

                        Log.i("Rerequesting comment " + protoComment.getId());
                        rerequestCount += 1;
                        if (senderStateIssue) {
                            Log.i("Tearing down session because of sender state issue");
                            SignalSessionManager.getInstance().tearDownSession(publisherUserId);
                        }
                        GroupFeedSessionManager.getInstance().sendCommentRerequest(publisherUserId, groupId, protoComment.getId(), rerequestCount, senderStateIssue, protoComment.getCommentType());

                        if (!ServerProps.getInstance().getUsePlaintextGroupFeed()) {
                            Comment comment = new Comment(0,
                                    protoComment.getPostId(),
                                    publisherUserId,
                                    protoComment.getId(),
                                    protoComment.getParentCommentId(),
                                    1000L * protoComment.getTimestamp(),
                                    Comment.TRANSFERRED_DECRYPT_FAILED,
                                    false,
                                    "");
                            comment.clientVersion = Constants.FULL_VERSION;
                            comment.senderPlatform = senderPlatform;
                            comment.senderVersion = senderVersion;
                            comment.failureReason = errorMessage;
                            comment.rerequestCount = rerequestCount;
                            return comment;
                        }
                    }
                }
            } else if (isPublic) { // is a public feed comment
                Container container;
                try {
                     container = Container.parseFrom(protoComment.getPayload());
                } catch (InvalidProtocolBufferException e) {
                    Log.e("connection: invalid comment payload");
                    return null;
                }

                String userIdStr = Long.toString(protoComment.getPublisherUid());
                Comment comment = feedContentParser.parseComment(
                        protoComment.getId(),
                        protoComment.getParentCommentId(),
                        Me.getInstance().equals(userIdStr) ? UserId.ME : new UserId(userIdStr),
                        protoComment.getTimestamp() * 1000L,
                        container.getCommentContainer(),
                        false
                );
                return comment;
            } else { // is a home feed comment
                byte[] encPayload = protoComment.getEncPayload().toByteArray();
                if (encPayload != null && encPayload.length > 0) {
                    Stats stats = Stats.getInstance();
                    try {
                        byte[] decPayload;
                        try {
                            EncryptedPayload encryptedPayload = EncryptedPayload.parseFrom(encPayload);
                            switch (encryptedPayload.getPayloadCase()) {
                                case COMMENT_KEY_ENCRYPTED_PAYLOAD: {
                                    byte[] toDecrypt = encryptedPayload.getCommentKeyEncryptedPayload().toByteArray();
                                    decPayload = HomeFeedSessionManager.getInstance().decryptComment(toDecrypt, protoComment.getPostId());
                                    break;
                                }
                                case ONE_TO_ONE_ENCRYPTED_PAYLOAD: {
                                    byte[] toDecrypt = encryptedPayload.getOneToOneEncryptedPayload().toByteArray();
                                    decPayload = SignalSessionManager.getInstance().decryptMessage(toDecrypt, publisherUserId, null);
                                    break;
                                }
                                default: {
                                    throw new CryptoException("no_accepted_enc_payload");
                                }
                            }
                        } catch (InvalidProtocolBufferException e) {
                            throw new CryptoException("home_invalid_proto", e);
                        }
                        if (payload.length > 0 && !Arrays.equals(payload, decPayload)) {
                            Log.e("Home Feed Encryption plaintext and decrypted differ");
                            throw new CryptoException("home_cmnt_payload_differs");
                        }
                        stats.reportHomeDecryptSuccess(true, senderPlatform, senderVersion);
                        payload = decPayload;
                    } catch (CryptoException e) {
                        Log.e("Failed to decrypt home comment", e);
                        errorMessage = e.getMessage();
                        Log.sendErrorReport("Home comment decryption failed: " + errorMessage);
                        stats.reportHomeDecryptError(errorMessage, true, senderPlatform, senderVersion);

                        Log.i("Rerequesting comment " + protoComment.getId());
                        rerequestCount += 1;
                        if (senderStateIssue) {
                            Log.i("Tearing down session because of sender state issue");
                            SignalSessionManager.getInstance().tearDownSession(publisherUserId);
                        }
                        Post post = contentDb.getPost(protoComment.getPostId());
                        if (post == null) {
                            Log.e("Cannot rerequest home comment for non-existent post");
                        } else {
                            boolean favorites = PrivacyList.Type.ONLY.equals(post.getAudienceType());
                            HomeFeedSessionManager.getInstance().sendPostRerequest(post.senderUserId, favorites, protoComment.getPostId(), post.rerequestCount, senderStateIssue);
                            HomeFeedSessionManager.getInstance().sendCommentRerequest(post.senderUserId, publisherUserId, rerequestCount, protoComment.getId(), protoComment.getCommentType());
                        }

                        if (!ServerProps.getInstance().getUsePlaintextHomeFeed()) {
                            Comment comment = new Comment(0,
                                    protoComment.getPostId(),
                                    publisherUserId,
                                    protoComment.getId(),
                                    protoComment.getParentCommentId(),
                                    1000L * protoComment.getTimestamp(),
                                    Comment.TRANSFERRED_DECRYPT_FAILED,
                                    false,
                                    "");
                            comment.clientVersion = Constants.FULL_VERSION;
                            comment.senderPlatform = senderPlatform;
                            comment.senderVersion = senderVersion;
                            comment.failureReason = errorMessage;
                            comment.rerequestCount = rerequestCount;
                            return comment;
                        }
                    }
                }
            }

            final byte[] protoHash = CryptoUtils.sha256(payload);

            Container container;
            try {
                container = Container.parseFrom(payload);
            } catch (InvalidProtocolBufferException e) {
                Log.e("connection: invalid comment payload");
                return null;
            }

            CommentContainer commentContainer = container.getCommentContainer();
            long timestamp = protoComment.getTimestamp() * 1000L;
            UserId publisherId = getUserId(Long.toString(protoComment.getPublisherUid()));

            Comment comment = feedContentParser.parseComment(protoComment.getId(), protoComment.getParentCommentId(), publisherId, timestamp, commentContainer, errorMessage != null);

            comment.protoHash = protoHash;
            comment.clientVersion = Constants.FULL_VERSION;
            comment.senderPlatform = senderPlatform;
            comment.senderVersion = senderVersion;
            comment.failureReason = errorMessage;
            comment.rerequestCount = rerequestCount;

            return comment;
        }


        private boolean processGroupFeedItems(@NonNull List<GroupFeedItem> items, @NonNull String ackId, boolean fromHistory) {
            final List<Post> posts = new ArrayList<>();
            final List<Comment> comments = new ArrayList<>();
            final Map<UserId, String> names = new HashMap<>();

            boolean senderStateIssue = false;
            for (GroupFeedItem item : items) {
                // TODO(jack): Skip items from server based on server prop for rollout progress
//                if (item.getAction().equals(GroupFeedItem.Action.SHARE)) {
//                    Log.d("Skipping item from server: " + ProtoPrinter.toString(item));
//                    continue;
//                }
                String senderAgent = item.getSenderClientVersion();
                String senderPlatform = senderAgent == null ? "" : senderAgent.contains("Android") ? "android" : senderAgent.contains("iOS") ? "ios" : "";
                String senderVersion = senderPlatform.equals("android") ? senderAgent.split("Android")[1] : senderPlatform.equals("ios") ? senderAgent.split("iOS")[1] : "";

                GroupId groupId = new GroupId(item.getGid());
                if (item.hasSenderState()) {
                    SenderStateWithKeyInfo senderStateWithKeyInfo = item.getSenderState();

                    long publisherUid;
                    if (item.hasComment()) {
                        publisherUid = item.getComment().getPublisherUid();
                    } else if (item.hasPost()) {
                        publisherUid = item.getPost().getPublisherUid();
                    } else {
                        Log.e("GroupFeedItem " + ProtoPrinter.toString(item) + " has neither post nor comment");
                        continue;
                    }
                    UserId publisherUserId = new UserId(Long.toString(publisherUid));

                    byte[] encSenderState = senderStateWithKeyInfo.getEncSenderState().toByteArray();
                    try {
                        byte[] peerPublicIdentityKey = senderStateWithKeyInfo.getPublicKey().toByteArray();
                        long oneTimePreKeyId = senderStateWithKeyInfo.getOneTimePreKeyId();
                        SignalSessionSetupInfo signalSessionSetupInfo = peerPublicIdentityKey == null || peerPublicIdentityKey.length == 0 ? null : new SignalSessionSetupInfo(new PublicEdECKey(peerPublicIdentityKey), (int) oneTimePreKeyId);
                        byte[] senderStateDec = SignalSessionManager.getInstance().decryptMessage(encSenderState, publisherUserId, signalSessionSetupInfo);
                        SenderState senderState = SenderState.parseFrom(senderStateDec);
                        SenderKey senderKey = senderState.getSenderKey();
                        int currentChainIndex = senderState.getCurrentChainIndex();
                        byte[] chainKey = senderKey.getChainKey().toByteArray();
                        byte[] publicSignatureKeyBytes = senderKey.getPublicSignatureKey().toByteArray();
                        PublicEdECKey publicSignatureKey = new PublicEdECKey(publicSignatureKeyBytes);
                        Log.i("Received sender state with current chain index of " + currentChainIndex + " from " + publisherUid);

                        EncryptedKeyStore.getInstance().edit()
                                .setPeerGroupCurrentChainIndex(groupId, publisherUserId, currentChainIndex)
                                .setPeerGroupChainKey(groupId, publisherUserId, chainKey)
                                .setPeerGroupSigningKey(groupId, publisherUserId, publicSignatureKey)
                                .apply();
                    } catch (CryptoException e) {
                        Log.e("Failed to decrypt sender state for " + ProtoPrinter.toString(item), e);
                        senderStateIssue = true;
                    } catch (InvalidProtocolBufferException e) {
                        Log.e("Failed to parse sender state for " + ProtoPrinter.toString(item), e);
                    }
                }

                if (item.getAction() == GroupFeedItem.Action.PUBLISH || item.getAction() == GroupFeedItem.Action.SHARE) {
                    if (item.hasComment()) {
                        com.halloapp.proto.server.Comment protoComment = item.getComment();
                        Comment comment = processComment(protoComment, names, groupId, senderStateIssue, senderPlatform, senderVersion, false);
                        if (comment != null) {
                            comment.fromHistory = fromHistory;
                            comments.add(comment);
                        } else {
                            Log.e("connection: invalid comment");
                        }
                    } else if (item.hasPost()) {
                        com.halloapp.proto.server.Post protoPost = item.getPost();
                        Post post = processPost(protoPost, names, groupId, senderStateIssue, senderPlatform, senderVersion);
                        if (post != null) {
                            post.seen = item.getAction().equals(GroupFeedItem.Action.PUBLISH) ? Post.SEEN_NO : Post.SEEN_NO_HIDDEN;
                            post.setParentGroup(new GroupId(item.getGid()));
                            long expirationTimeStamp = item.getExpiryTimestamp();
                            if (expirationTimeStamp == 0) {
                                expirationTimeStamp = post.timestamp + Constants.POSTS_EXPIRATION;
                            } else if (expirationTimeStamp == -1) {
                                expirationTimeStamp = Post.POST_EXPIRATION_NEVER;
                            } else {
                                expirationTimeStamp *= 1000L;
                            }
                            post.expirationTime = expirationTimeStamp;
                            post.fromHistory = fromHistory;
                            posts.add(post);
                        }
                    }
                } else if (item.getAction() == GroupFeedItem.Action.RETRACT) {
                    if (item.hasComment()) {
                        com.halloapp.proto.server.Comment comment = item.getComment();
                        connectionObservers.notifyCommentRetracted(comment.getId(), getUserId(Long.toString(comment.getPublisherUid())), comment.getPostId(), comment.getTimestamp() * 1000L);
                    } else if (item.hasPost()) {
                        com.halloapp.proto.server.Post post = item.getPost();
                        connectionObservers.notifyPostRetracted(getUserId(Long.toString(post.getPublisherUid())), new GroupId(item.getGid()), post.getId(), post.getTimestamp() * 1000L);
                    }
                }
            }

            if (!names.isEmpty()) {
                connectionObservers.notifyUserNamesReceived(names);
            }

            connectionObservers.notifyIncomingFeedItemsReceived(posts, comments, ackId);

            return !posts.isEmpty() || !comments.isEmpty();
        }

        private void handleIq(Iq iq) {
            if (iq.getType().equals(Iq.Type.RESULT)) {
                iqRouter.onResponse(iq.getId(), iq);
            } else if (iq.getType().equals(Iq.Type.ERROR)) {
                iqRouter.onError(iq.getId(), iq);
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
            msgRouter.handleAck(ack);
        }

        private void handlePresence(Presence presence) {
            long lastSeen = presence.getLastSeen();
            connectionObservers.notifyPresenceReceived(getUserId(Long.toString(presence.getFromUid())), lastSeen > 0 ? lastSeen : null);
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

    interface PacketCallback {
        void onPacketDropped();
    }

    private class PacketWriter {
        private final LinkedBlockingQueue<Packet> queue = new LinkedBlockingQueue<>();

        private final Object callbackLock = new Object();

        private final HashMap<Packet, PacketCallback> packetCallbacks = new HashMap<>();

        private Thread writerThread;
        private WriterRunnable writerRunnable;


        void init() {
            cleanQueue();
            startWriter();
        }

        void cleanQueue() {
            queue.clear();
        }

        void shutdown() {
            stopWriter();
            cleanQueue();
        }

        void sendPacket(Packet packet, @Nullable PacketCallback callback) {
            try {
                synchronized (callbackLock) {
                    if (callback != null) {
                        packetCallbacks.put(packet, callback);
                    }
                }
                enqueue(packet);
            } catch (InterruptedException e) {
                Log.w("Interrupted while enqueueing packet for send. dropping", e);
                if (callback != null) {
                    callback.onPacketDropped();
                }
            }
        }

        private void enqueue(Packet packet) throws InterruptedException {
            queue.put(packet);
        }

        private synchronized void startWriter() {
            stopWriter();

            writerRunnable = new WriterRunnable();
            writerThread = ThreadUtils.go(writerRunnable, "Packet Writer");
        }

        private synchronized void stopWriter() {
            if (writerRunnable != null) {
                writerRunnable.shutdown();
                writerRunnable = null;
            }
            if (writerThread != null) {
                writerThread.interrupt();
                writerThread = null;
            }

            List<PacketCallback> callbacks;
            synchronized (callbackLock) {
                Log.i("PacketWriter/stopWriter callback lock acquired");
                callbacks = new ArrayList<>(packetCallbacks.values());
                packetCallbacks.clear();
            }
            Log.i("PacketWriter/stopWriter notifying packet dropped");
            for (PacketCallback callback : callbacks) {
                if (callback != null) {
                    callback.onPacketDropped();
                }
            }
        }

        private class WriterRunnable implements Runnable {

            private volatile boolean done = false;

            @Override
            public void run() {
                ThreadUtils.setSocketTag();
                try {
                    while (!done) {
                        try {
                            if (socket == null) {
                                throw new IOException("Socket is null");
                            }
                            Packet packet = queue.take();
                            Log.i("connection: send: " + ProtoPrinter.toString(packet));
                            synchronized (callbackLock) {
                                packetCallbacks.remove(packet);
                            }
                            socket.writePacket(packet);
                        } catch (InterruptedException e) {
                            Log.w("Packet writing interrupted", e);
                        }
                    }
                } catch (Exception e) {
                    if (!done) {
                        Log.e("Packet Writer error", e);
                        disconnect();
                    }
                }
            }

            public void shutdown() {
                done = true;
            }
        }
    }

    private class MsgRouter {
        private static final long DEFAULT_MSG_TIMEOUT_MS = 30_000;
        private final LinkedHashMap<String, PendingMsg> pendingMessages = new LinkedHashMap<>();

        private class PendingMsg {
            public final String id;
            public final Packet packet;
            public final MsgCallback callback;
            public final TimerTask timeoutTask;
            public boolean resendable = false;

            public PendingMsg(@NonNull String id, @NonNull Packet packet, @NonNull TimerTask task, @Nullable MsgCallback callback) {
                this.id = id;
                this.packet = packet;
                this.callback = callback;
                this.timeoutTask = task;
            }

            public void setResendable(boolean resendable) {
                this.resendable = resendable;
            }
        }

        public void sendMsg(Msg msg, @Nullable MsgCallback msgCallback, boolean resendable) {
            sendMsg(msg, DEFAULT_MSG_TIMEOUT_MS, msgCallback, resendable);
        }

        public void sendMsg(Msg msg, long timeout, @Nullable MsgCallback msgCallback, boolean resendable) {
            sendMsg(msg, timeout, msgCallback, resendable, false);
        }

        public void sendMsg(Msg msg, long timeout, @Nullable MsgCallback msgCallback, boolean resendable, boolean permitDuplicate) {
            Packet packet = buildPacket(msg);
            final String id = msg.getId();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    PendingMsg pendingMsg;
                    synchronized (MsgRouter.this) {
                        pendingMsg = pendingMessages.remove(id);
                    }
                    if (pendingMsg != null) {
                        Log.i("connection: msg id " + id + " never acked, timed out");
                        if (pendingMsg.callback != null) {
                            pendingMsg.callback.onTimeout();
                        }
                    }
                }
            };
            PendingMsg pendingMsg = new PendingMsg(id, packet, task, msgCallback);
            pendingMsg.setResendable(resendable);
            synchronized (MsgRouter.this) {
                if (pendingMessages.containsKey(id)) {
                    Log.e("connection: duplicate outgoing msg id " + id);
                    if (permitDuplicate) {
                        // TODO (clarkc): disallow duplicates when we no longer leverage content id as msg id in some cases
                        PendingMsg existing = pendingMessages.remove(id);
                        if (existing != null) {
                            existing.timeoutTask.cancel();
                        }
                        Log.e("connection: allowing duplicate id");
                    } else {
                        throw new RuntimeException("an outgoing msg with the same id=" + id + " already exists!");
                    }
                }
                pendingMessages.put(id, pendingMsg);
                timer.schedule(task, timeout);
            }
            sendPacket(packet);
        }

        public void handleAck(Ack ack) {
            final String ackId = ack.getId();
            final PendingMsg ackedMessage;
            synchronized (MsgRouter.this) {
                ackedMessage = pendingMessages.remove(ackId);
                if (ackedMessage != null) {
                    ackedMessage.timeoutTask.cancel();
                }
            }
            if (ackedMessage == null) {
                Log.w("connection: ack doesn't match any pending message " + ProtoPrinter.toString(ack));
            } else if (ackedMessage.callback != null) {
                ackedMessage.callback.onAck();
            }
        }

        public void onConnected() {
            Log.i("connection: re-connected msg router requeing messages");
            synchronized (MsgRouter.this) {
                Iterator<PendingMsg> pendingMessageIterator = pendingMessages.values().iterator();
                while (pendingMessageIterator.hasNext()) {
                    PendingMsg msg = pendingMessageIterator.next();
                    if (!msg.resendable) {
                        Log.i("connection: dropping msg id " + msg.id + " as not safe to resend");
                        pendingMessageIterator.remove();
                        msg.timeoutTask.cancel();
                    } else {
                        sendPacket(msg.packet);
                    }
                }
            }
        }

        public void onDisconnected() {
            Log.i("connection: disconnected msg router clearing out non-resendable messages");
            ArrayList<PendingMsg> droppedMessages = new ArrayList<>();
            synchronized (MsgRouter.this) {
                Iterator<PendingMsg> pendingMessageIterator = pendingMessages.values().iterator();
                while (pendingMessageIterator.hasNext()) {
                    PendingMsg msg = pendingMessageIterator.next();
                    if (!msg.resendable) {
                        pendingMessageIterator.remove();
                        msg.timeoutTask.cancel();
                        droppedMessages.add(msg);
                    }
                }
            }
            for (PendingMsg msg : droppedMessages) {
                Log.i("connection: dropped non-resendable msg id " + msg.id);
                if (msg.callback != null) {
                    msg.callback.onTimeout();
                }
            }
        }

        private Packet buildPacket(Msg msg) {
            return Packet.newBuilder().setMsg(msg).build();
        }
    }

    private class IqRouter {
        private static final long IQ_TIMEOUT_MS = 20_000;

        private final LinkedHashMap<String, PendingIq> pendingIqs = new LinkedHashMap<>();

        private class PendingIq {
            public String id;
            public final Iq.Builder iq;
            public final ResponseHandler<Iq> successCallback;
            public final ExceptionHandler failureCallback;
            public TimerTask timeoutTask;
            public boolean resendable = false;

            public PendingIq(@NonNull String id, @NonNull Iq.Builder iq, @Nullable ResponseHandler<Iq> successCallback, @Nullable ExceptionHandler failureCallback) {
                this.id = id;
                this.iq = iq;

                this.successCallback = successCallback;
                this.failureCallback = failureCallback;
            }

            public void setId(@NonNull String id) {
                this.id = id;
                this.iq.setId(id);
            }

            public void setTimeoutTask(@NonNull TimerTask task) {
                this.timeoutTask = task;
            }

            public void setResendable(boolean resendable) {
                this.resendable = resendable;
            }
        }

        private ResponseHandler<Iq> fetchSuccessCallback(String id) {
            PendingIq pendingIq;
            synchronized (pendingIqs) {
                pendingIq = pendingIqs.remove(id);
            }
            if (pendingIq != null) {
                pendingIq.timeoutTask.cancel();
                return pendingIq.successCallback;
            }
            return null;
        }

        private ExceptionHandler fetchFailureCallback(String id) {
            PendingIq pendingIq;
            synchronized (pendingIqs) {
                pendingIq = pendingIqs.remove(id);
            }
            if (pendingIq != null) {
                pendingIq.timeoutTask.cancel();
                return pendingIq.failureCallback;
            }
            return null;
        }

        public void onResponse(String id, Iq iq) {
            ResponseHandler<Iq> callback = fetchSuccessCallback(id);
            if (callback != null) {
                callback.handleResponse(iq);
            } else {
                Log.w("IqRouter: no response callback for " + id);
            }
        }

        public void onError(String id, Iq errorIq) {
            Exception e;
            if (errorIq.hasErrorStanza()) {
                ErrorStanza errorStanza = errorIq.getErrorStanza();
                String reason = errorStanza.getReason();
                Log.d("IqRouter: got error for id " + id + " with reason " + reason);
                e = new IqErrorException(id, reason);
            } else {
                Log.d("IqRouter: got error for id " + id);
                e = new IqErrorException(id, errorIq);
            }
            ExceptionHandler callback = fetchFailureCallback(id);
            if (callback != null) {
                callback.handleException(e);
            } else {
                Log.w("IqRouter: no error callback for " + id);
            }
        }

        public Observable<Iq> sendAsync(Iq.Builder iq) {
            return sendAsync(iq, false);
        }

        public Observable<Iq> sendAsync(Iq.Builder iq, boolean resendable) {
            final String id = getAndIncrementShortId();
            iq.setId(id);

            BackgroundObservable<Iq> observable = new BackgroundObservable<>(bgWorkers);
            PendingIq pendingIq = new PendingIq(id, iq, observable::setResponse, observable::setException);
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    executor.execute(() -> {
                        performIqTimeout(pendingIq.id);
                    });
                }
            };
            pendingIq.setResendable(resendable);
            pendingIq.setTimeoutTask(timerTask);
            timer.schedule(timerTask, IQ_TIMEOUT_MS);
            synchronized (pendingIqs) {
                pendingIqs.put(id, pendingIq);
            }
            PacketCallback packetCallback;
            if (resendable) {
                packetCallback = null;
            } else {
                packetCallback = () -> {
                    PendingIq pending;
                    synchronized (pendingIqs) {
                        pending = pendingIqs.remove(pendingIq.id);
                    }
                    if (pending != null) {
                        pending.timeoutTask.cancel();
                        observable.setException(new NotConnectedException());
                    }
                };
            }
            sendPacket(buildIqPacket(iq), packetCallback);
            return observable;
        }

        private Packet buildIqPacket(Iq.Builder iqBuilder) {
            return Packet.newBuilder().setIq(iqBuilder).build();
        }

        void onConnected() {
            Log.i("connection: re-connected iq router requeing iqs");
            synchronized (pendingIqs) {
                ArrayList<PendingIq> iqsToRequeue = new ArrayList<>();
                for (PendingIq iq : pendingIqs.values()) {
                    if (!iq.resendable) {
                        Log.i("connection: dropping msg id " + iq.id + " as not safe to resend");
                        iq.timeoutTask.cancel();
                        if (iq.failureCallback != null) {
                            iq.failureCallback.handleException(new IqRouterResetException(iq.id));
                        }
                    } else {
                        String id = getAndIncrementShortId();
                        Log.i("connection: requeued iq: " + iq.id + " new id: " + id);
                        iq.setId(id);
                        iqsToRequeue.add(iq);
                    }
                }
                pendingIqs.clear();
                for (PendingIq iq : iqsToRequeue) {
                    pendingIqs.put(iq.id, iq);
                    sendPacket(buildIqPacket(iq.iq));
                }
            }
        }

        void onDisconnected() {
            Log.i("connection: disconnected iq router clearing out non-resendable iqs");
            ArrayList<PendingIq> droppedIqs = new ArrayList<>();
            synchronized (pendingIqs) {
                Iterator<PendingIq> pendingIqIterator = pendingIqs.values().iterator();
                while (pendingIqIterator.hasNext()) {
                    PendingIq msg = pendingIqIterator.next();
                    if (!msg.resendable) {
                        pendingIqIterator.remove();
                        msg.timeoutTask.cancel();
                        droppedIqs.add(msg);
                    }
                }
            }
            for (PendingIq msg : droppedIqs) {
                Log.i("IqRouter marking " + msg.id + " as failure during reset");
                if (msg.failureCallback != null) {
                    msg.failureCallback.handleException(new IqRouterResetException(msg.id));
                }
            }
        }

        private void performIqTimeout(String id) {
            PendingIq pendingIq;
            synchronized (pendingIqs) {
                pendingIq = pendingIqs.remove(id);
            }
            if (pendingIq == null) {
                Log.i("connection: iq timed out, but already cleared; id=" + id);
                return;
            }
            if (pendingIq.failureCallback != null) {
                pendingIq.failureCallback.handleException(new IqTimeoutException(id));
            }
        }
    }

    private static class ConnectionExecutor extends ThreadPoolExecutor {
        private static final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        private static final WeakHashMap<Runnable, Runnable> dropHandlers = new WeakHashMap<>();

        public ConnectionExecutor() {
            super(1, 1, 0L, TimeUnit.MILLISECONDS, queue);
        }

        public int reset() {
            List<Runnable> drain = new ArrayList<>();
            queue.drainTo(drain);
            for (Runnable drained : drain) {
                Runnable dropHandler = dropHandlers.get(drained);
                if (dropHandler != null) {
                    BgWorkers.getInstance().execute(dropHandler);
                }
            }
            return drain.size();
        }

        public void executeWithDropHandler(Runnable toExecute, Runnable dropHandler) {
            dropHandlers.put(toExecute, dropHandler);
            execute(toExecute);
        }
    }

    private static class ExecutorResetException extends Exception {
        public ExecutorResetException() {
            super("Executor reset while queueing");
        }
    }

    private static class IqTimeoutException extends Exception {
        public IqTimeoutException(String id) {
            super("Iq timeout for Iq with id " + id);
        }
    }

    private static class IqRouterResetException extends Exception {
        public IqRouterResetException(String id) {
            super("IqRouter reset while waiting for " + id);
        }
    }

    private static class NotConnectedException extends Exception {
        public NotConnectedException() {
            super("Could not connect to server");
        }
    }
}
