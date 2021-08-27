package com.halloapp.xmpp;

import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.android.gms.common.util.Hex;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.ConnectionObservers;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.contacts.ContactSyncResult;
import com.halloapp.content.Comment;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.group.GroupFeedKeyManager;
import com.halloapp.crypto.group.GroupFeedSessionManager;
import com.halloapp.crypto.group.GroupSetupInfo;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.signal.SessionSetupInfo;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.noise.HANoiseSocket;
import com.halloapp.noise.NoiseException;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.Background;
import com.halloapp.proto.clients.CommentContainer;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.clients.PostContainer;
import com.halloapp.proto.clients.SenderKey;
import com.halloapp.proto.clients.SenderState;
import com.halloapp.proto.log_events.EventData;
import com.halloapp.proto.server.Ack;
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
import com.halloapp.proto.server.DeliveryReceipt;
import com.halloapp.proto.server.ErrorStanza;
import com.halloapp.proto.server.ExportData;
import com.halloapp.proto.server.FeedItems;
import com.halloapp.proto.server.GroupFeedItem;
import com.halloapp.proto.server.GroupFeedItems;
import com.halloapp.proto.server.GroupMember;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.HaError;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.Msg;
import com.halloapp.proto.server.Packet;
import com.halloapp.proto.server.Ping;
import com.halloapp.proto.server.PlayedReceipt;
import com.halloapp.proto.server.Presence;
import com.halloapp.proto.server.Rerequest;
import com.halloapp.proto.server.SeenReceipt;
import com.halloapp.proto.server.SenderStateBundle;
import com.halloapp.proto.server.SenderStateWithKeyInfo;
import com.halloapp.proto.server.WhisperKeys;
import com.halloapp.registration.Registration;
import com.halloapp.ui.ExportDataActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.ThreadUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Counter;
import com.halloapp.xmpp.chat.ChatMessageProtocol;
import com.halloapp.xmpp.feed.FeedContentEncoder;
import com.halloapp.xmpp.feed.FeedContentParser;
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
import com.halloapp.xmpp.util.MutableObservable;
import com.halloapp.xmpp.util.Observable;
import com.halloapp.xmpp.util.ResponseHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConnectionImpl extends Connection {

    private static final String HOST = "s.halloapp.net";
    private static final String DEBUG_HOST = "s-test.halloapp.net";
    private static final int NOISE_PORT = 5222;

    public static final String FEED_THREAD_ID = "feed";

    private final Me me;
    private final BgWorkers bgWorkers;
    private final Preferences preferences;
    private final ConnectionObservers connectionObservers;

    private final ConnectionExecutor executor = new ConnectionExecutor();
    private final Map<String, Runnable> ackHandlers = new ConcurrentHashMap<>();
    public boolean clientExpired = false;
    private HANoiseSocket socket = null;

    private boolean isAuthenticated;

    private final Object startupShutdownLock = new Object();
    private final PacketWriter packetWriter = new PacketWriter();
    private final PacketReader packetReader = new PacketReader();
    private final IqRouter iqRouter = new IqRouter();

    private final FeedContentParser feedContentParser;

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

        randomizeShortId();
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

        final String host = preferences.getUseDebugHost() ? DEBUG_HOST : HOST;
        try {
            final InetAddress address = InetAddress.getByName(host);
            // TODO (clarkc) remove when we no longer need migration code to noise
            if (me.getMyEd25519NoiseKey() == null) {
                Log.i("connection: migrating registration to noise");
                Registration.RegistrationVerificationResult migrationResult = Registration.getInstance().migrateRegistrationToNoise();
                if (migrationResult.result != Registration.RegistrationVerificationResult.RESULT_OK) {
                    disconnectInBackground();
                    throw new IOException("Failed to migrate registration");
                }
                Log.i("connection: noise migration successful");
            }
            HANoiseSocket noiseSocket = new HANoiseSocket(me, address, NOISE_PORT);
            noiseSocket.initialize(createAuthRequest().toByteArray());
            this.socket = noiseSocket;
            isAuthenticated = true;

            synchronized (startupShutdownLock) {
                packetWriter.init();
                packetReader.init();
                iqRouter.init();
            }
            randomizeShortId();
            connectionObservers.notifyConnected();
            isAuthenticated = true;
            Log.i("connection: established");
        } catch (IOException | NoiseException e) {
            Log.e("connection: cannot create connection", e);
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

        AuthRequest.Builder authRequestBuilder = AuthRequest.newBuilder()
                .setUid(Long.parseLong(me.getUser()))
                .setClientVersion(clientVersion)
                .setClientMode(clientMode)
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
        connectInBackground();
        return socket != null && isConnected() && isAuthenticated();
    }

    public void sendPacket(Packet packet) {
        packetWriter.sendPacket(packet);
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    @Override
    public void clientExpired() {
        clientExpired = true;
        disconnect();
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
            iqRouter.shutdown();
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
    public Observable<MediaUploadIq.Urls> requestMediaUpload(long fileSize, @Nullable String downloadUrl) {
        final MediaUploadIq mediaUploadIq = new MediaUploadIq(fileSize, downloadUrl);
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
                    List<ContactInfo> ret = new ArrayList<>();
                    for (Contact contact : contacts) {
                        ret.add(new ContactInfo(contact));
                    }
                    result.setResponse(ContactSyncResult.success(ret));
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
    public void sendPushToken(@NonNull String pushToken, @NonNull String languageCode) {
        final PushRegisterRequestIq pushIq = new PushRegisterRequestIq(pushToken, languageCode);
        sendIqRequestAsync(pushIq)
                .onResponse(response -> {
                    Log.d("connection: response after setting the push token " + ProtoPrinter.toString(response));
                    preferences.setLastPushToken(pushToken);
                    preferences.setLastDeviceLocale(languageCode);
                    preferences.setLastPushTokenSyncTime(System.currentTimeMillis());
                })
                .onError(e -> Log.e("connection: cannot send push token", e));
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
            if (!reconnectIfNeeded() || socket == null) {
                Log.e("connection: request presence subscription: no connection");
                return;
            }
            PresenceStanza stanza = new PresenceStanza(userId, "subscribe");
            sendPacket(Packet.newBuilder().setPresence(stanza.toProto()).build());
        });
    }

    @Override
    public void updatePresence(boolean available) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || socket == null) {
                Log.e("connection: update presence: no connection");
                return;
            }
            PresenceStanza stanza = new PresenceStanza(null, available ? "available" : "away");
            Packet packet = Packet.newBuilder().setPresence(stanza.toProto()).build();
            sendPacket(packet);
        });
    }

    @Override
    public void updateChatState(@NonNull ChatId chat, int state) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || socket == null) {
                Log.e("connection: update chat state: no connection");
                return;
            }
            ChatStateStanza stanza = new ChatStateStanza(state == com.halloapp.xmpp.ChatState.Type.TYPING ? "typing" : "available", chat);
            sendPacket(Packet.newBuilder().setChatState(stanza.toProto()).build());
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
        return sendIqRequestAsync(eventsIq).map(r -> null);
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
    public Observable<String> setAvatar(String base64, long numBytes, int width, int height) {
        final AvatarIq avatarIq = new AvatarIq(base64, numBytes, height, width);
        return sendIqRequestAsync(avatarIq).map(res -> AvatarIq.fromProto(res.getAvatar()).avatarId);
    }

    @Override
    public Observable<String> removeAvatar() {
        return setAvatar("", 0, 0, 0);
    }

    @Override
    public Observable<String> setGroupAvatar(GroupId groupId, String base64) {
        final GroupAvatarIq avatarIq = new GroupAvatarIq(groupId, base64);
        return sendIqRequestAsync(avatarIq).map(res -> GroupResponseIq.fromProto(res.getGroupStanza()).avatar);
    }

    @Override
    public Observable<String> removeGroupAvatar(GroupId groupId) {
        return setGroupAvatar(groupId, "");
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
                FeedItem sharedPost = new FeedItem(FeedItem.Type.POST, post.id, null);
                itemList.add(sharedPost);
            }
            sharePosts.add(new SharePosts(user, itemList));
        }
        FeedUpdateIq updateIq = new FeedUpdateIq(sharePosts);
        return sendIqRequestAsync(updateIq).map(r -> null);
    }

    @Override
    public void sendPost(@NonNull Post post) {
        final PublishedEntry entry = new PublishedEntry(
                PublishedEntry.ENTRY_FEED,
                null,
                post.timestamp,
                me.getUser(),
                post.text,
                null,
                null);
        for (Media media : post.media) {
            entry.media.add(new PublishedEntry.Media(PublishedEntry.getMediaType(media.type), media.url, media.encKey, media.encSha256hash, media.width, media.height, media.blobVersion, media.chunkSize, media.blobSize));
        }
        for (Mention mention : post.mentions) {
            entry.mentions.add(Mention.toProto(mention));
        }
        if (post.getAudienceType() == null && post.getParentGroup() == null) {
            Log.e("connection: sendPost null audience type but not a group post");
            return;
        }
        Container.Builder containerBuilder = entry.getEntryBuilder();
        if (ServerProps.getInstance().getNewClientContainerEnabled()) {
            FeedContentEncoder.encodePost(containerBuilder, post);
        }
        if (!containerBuilder.hasPost()) {
            Log.e("connection: sendPost no post content");
            return;
        }
        HalloIq publishIq;
        if (post.getParentGroup() == null) {
            byte[] payload = containerBuilder.build().toByteArray();
            FeedItem feedItem = new FeedItem(FeedItem.Type.POST, post.id, payload);
            FeedUpdateIq updateIq = new FeedUpdateIq(FeedUpdateIq.Action.PUBLISH, feedItem);
            updateIq.setPostAudience(post.getAudienceType(), post.getAudienceList());
            publishIq = updateIq;
        } else {
            GroupId groupId = post.getParentGroup();
            byte[] payload = containerBuilder.build().toByteArray();
            byte[] encPayload = null;
            List<SenderStateBundle> senderStateBundles = new ArrayList<>();
            byte[] audienceHash = null;

            if (Constants.GROUP_FEED_ENC_ENABLED) {
                try {
                    GroupSetupInfo groupSetupInfo = GroupFeedKeyManager.getInstance().ensureGroupSetUp(groupId);
                    senderStateBundles = groupSetupInfo.senderStateBundles;
                    audienceHash = groupSetupInfo.audienceHash;
                    encPayload = GroupFeedSessionManager.getInstance().encryptMessage(payload, groupId);
                } catch (CryptoException e) {
                    Log.e("Failed to encrypt group post", e);
                } catch (NoSuchAlgorithmException e) {
                    Log.e("Failed to calculate audience hash", e);
                }
            }
            FeedItem feedItem = new FeedItem(FeedItem.Type.POST, post.id, payload, encPayload, senderStateBundles, audienceHash);
            publishIq = new GroupFeedUpdateIq(post.getParentGroup(), GroupFeedUpdateIq.Action.PUBLISH, feedItem);
        }
        sendIqRequestAsync(publishIq)
                .onResponse(response -> connectionObservers.notifyOutgoingPostSent(post.id))
                .onError(e -> {
                    Log.e("connection: cannot send post", e);
                    if (e instanceof IqErrorException) {
                        String reason = ((IqErrorException) e).getReason();
                        if ("audience_hash_mismatch".equals(reason)) {
                            connectionObservers.notifyAudienceHashMismatch(post);
                        }
                    }
                });
    }

    @Override
    public void retractPost(@NonNull String postId) {
        FeedUpdateIq requestIq = new FeedUpdateIq(FeedUpdateIq.Action.RETRACT, new FeedItem(FeedItem.Type.POST, postId, null));
        sendIqRequestAsync(requestIq)
                .onResponse(response -> connectionObservers.notifyOutgoingPostSent(postId))
                .onError(e -> Log.e("connection: cannot retract post", e));
    }

    @Override
    public void retractGroupPost(@NonNull GroupId groupId, @NonNull String postId) {
        GroupFeedUpdateIq requestIq = new GroupFeedUpdateIq(groupId, GroupFeedUpdateIq.Action.RETRACT, new FeedItem(FeedItem.Type.POST, postId, null));
        sendIqRequestAsync(requestIq)
                .onResponse(response -> connectionObservers.notifyOutgoingPostSent(postId))
                .onError(e -> Log.e("connection: cannot retract post", e));
    }

    @Override
    public void sendComment(@NonNull Comment comment) {
        byte[] encodedEntry;
        if (comment.type == Comment.TYPE_VOICE_NOTE) { // TODO: (clarkc) remove when old container is removed
            encodedEntry = FeedContentEncoder.encodeComment(comment);
        } else {
            final PublishedEntry entry = new PublishedEntry(
                    PublishedEntry.ENTRY_COMMENT,
                    null,
                    comment.timestamp,
                    me.getUser(),
                    comment.text,
                    comment.postId,
                    comment.parentCommentId);
            for (Media media : comment.media) {
                entry.media.add(new PublishedEntry.Media(PublishedEntry.getMediaType(media.type), media.url, media.encKey, media.encSha256hash, media.width, media.height, media.blobVersion, media.chunkSize, media.blobSize));
            }
            for (Mention mention : comment.mentions) {
                entry.mentions.add(Mention.toProto(mention));
            }
            Container.Builder containerBuilder = entry.getEntryBuilder();
            if (ServerProps.getInstance().getNewClientContainerEnabled()) {
                FeedContentEncoder.encodeComment(containerBuilder, comment);
            }
            encodedEntry = containerBuilder.build().toByteArray();
        }
        FeedItem commentItem = new FeedItem(FeedItem.Type.COMMENT, comment.id, comment.postId, encodedEntry);
        commentItem.parentCommentId = comment.parentCommentId;
        HalloIq requestIq;
        if (comment.getParentPost() == null || comment.getParentPost().getParentGroup() == null) {
            requestIq = new FeedUpdateIq(FeedUpdateIq.Action.PUBLISH, commentItem);
        } else {
            // TODO(jack): encryption for comments
            requestIq = new GroupFeedUpdateIq(comment.getParentPost().getParentGroup(), FeedUpdateIq.Action.PUBLISH, commentItem);
        }
        sendIqRequestAsync(requestIq)
                .onResponse(response -> connectionObservers.notifyOutgoingCommentSent(comment.postId, comment.id))
                .onError(e -> Log.e("connection: cannot send comment", e));
    }

    // TODO: (clarkc) remove post sender user id when server tells us
    @Override
    public void retractComment(@Nullable UserId postSenderUserId, @NonNull String postId, @NonNull String commentId) {
        FeedItem commentItem = new FeedItem(FeedItem.Type.COMMENT, commentId, postId, null);
        FeedUpdateIq requestIq = new FeedUpdateIq(FeedUpdateIq.Action.RETRACT, commentItem);
        sendIqRequestAsync(requestIq)
                .onResponse(response -> connectionObservers.notifyOutgoingCommentSent(postId, commentId))
                .onError(e -> Log.e("connection: cannot retract comment", e));
    }

    public void retractMessage(@NonNull UserId chatUserId, @NonNull String messageId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || socket == null) {
                Log.e("connection: cannot send message, no connection");
                return;
            }

            String id = RandomId.create();

            Msg msg = Msg.newBuilder()
                    .setId(id)
                    .setToUid(Long.parseLong(chatUserId.rawId()))
                    .setType(Msg.Type.CHAT)
                    .setChatRetract(ChatRetract.newBuilder().setId(messageId).build())
                    .build();
            ackHandlers.put(messageId, () -> connectionObservers.notifyOutgoingMessageSent(chatUserId, messageId));
            sendPacket(Packet.newBuilder().setMsg(msg).build());
        });
    }

    @Override
    public void retractGroupComment(@NonNull GroupId groupId, @NonNull UserId postSenderUserId, @NonNull String postId, @NonNull String commentId) {
        FeedItem commentItem = new FeedItem(FeedItem.Type.COMMENT, commentId, postId, null);
        GroupFeedUpdateIq requestIq = new GroupFeedUpdateIq(groupId, GroupFeedUpdateIq.Action.RETRACT, commentItem);
        sendIqRequestAsync(requestIq)
                .onResponse(r -> connectionObservers.notifyOutgoingCommentSent(postId, commentId))
                .onError(e -> Log.e("connection: cannot retract comment", e));
    }

    @Override
    public void sendMessage(@NonNull Message message, @Nullable SessionSetupInfo sessionSetupInfo) {
        executor.execute(() -> {
            if (message.isLocalMessage()) {
                Log.i("connection: System message shouldn't be sent");
                return;
            }
            if (!reconnectIfNeeded() || socket == null) {
                Log.e("connection: cannot send message, no connection");
                return;
            }
            final UserId recipientUserId = (UserId)message.chatId;

            Msg msg = Msg.newBuilder()
                    .setId(message.id)
                    .setType(Msg.Type.CHAT)
                    .setToUid(Long.parseLong(message.chatId.rawId()))
                    .setChatStanza(ChatMessageProtocol.getInstance().serializeMessage(message, recipientUserId, sessionSetupInfo))
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
            if (!reconnectIfNeeded() || socket == null) {
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

    private Observable<Iq> sendIqRequestAsync(@NonNull HalloIq iq) {
        BackgroundObservable<Iq> iqResponse = new BackgroundObservable<>(bgWorkers);
        executor.executeWithDropHandler(() -> {
            if (!reconnectIfNeeded() || socket == null) {
                Log.e("connection: cannot send iq " + iq + ", no connection");
                iqResponse.setException(new NotConnectedException());
                return;
            }
            Iq protoIq = iq.toProtoIq();
            iqRouter.sendAsync(protoIq)
                    .onResponse(iqResponse::setResponse)
                    .onError(iqResponse::setException);
        }, () -> iqResponse.setException(new ExecutorResetException()));
        return iqResponse;
    }

    @Override
    public void sendRerequest(final @NonNull UserId senderUserId, @NonNull String messageId, int rerequestCount, @Nullable byte[] teardownKey) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || socket == null) {
                Log.e("connection: cannot send rerequest, no connection");
                return;
            }
            RerequestElement rerequestElement = new RerequestElement(messageId, senderUserId, rerequestCount, teardownKey);
            Log.i("connection: sending rerequest for " + messageId + " to " + senderUserId);
            sendPacket(Packet.newBuilder().setMsg(rerequestElement.toProto()).build());
        });
    }

    @Override
    public void sendAck(@NonNull String id) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || socket == null) {
                Log.e("connection: cannot send ack, no connection");
                return;
            }
            final AckStanza ack = new AckStanza(id);
            Log.i("connection: sending ack for " + id);
            sendPacket(Packet.newBuilder().setAck(ack.toProto()).build());
        });
    }

    @Override
    public void sendPostSeenReceipt(@NonNull UserId senderUserId, @NonNull String postId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || socket == null) {
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
            if (!reconnectIfNeeded() || socket == null) {
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
    public void sendMessagePlayedReceipt(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || socket == null) {
                Log.e("connection: cannot send message seen receipt, no connection");
                return;
            }
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
            ackHandlers.put(id, () -> connectionObservers.notifyIncomingMessagePlayedReceiptSent(chatId, senderUserId, messageId));
            sendPacket(Packet.newBuilder().setMsg(msg).build());
            Log.i("connection: sending message seen receipt " + messageId + " to " + senderUserId);
        });
    }

    @Override
    public Observable<Iq> deleteAccount(@NonNull String phone) {
        return sendIqRequestAsync(new DeleteAccountRequestIq(phone)).map(response -> {
            Log.d("connection: response after deleting account " + ProtoPrinter.toString(response));
            return response;
        });
    }

    @Override
    public Observable<ExportDataResponseIq> requestAccountData() {
        return sendIqRequestAsync(new ExportDataRequestIq()).map(response -> {
            Log.d("connection: response after export request " + ProtoPrinter.toString(response));
            ExportDataResponseIq iq = ExportDataResponseIq.fromProto(response.getExportData());
            preferences.setExportDataState(iq.status == ExportData.Status.PENDING ? ExportDataActivity.EXPORT_STATE_PENDING : ExportDataActivity.EXPORT_STATE_READY);
            return iq;
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
        private volatile boolean done;
        private Thread readerThread;

        void init() {
            done = false;
            readerThread = ThreadUtils.go(this::parsePackets, "Packet Reader"); // TODO(jack): Connection counter
        }

        void shutdown() {
            done = true;
            if (readerThread != null) {
                readerThread.interrupt();
                readerThread = null;
            }
        }

        private void parsePackets() {
            ThreadUtils.setSocketTag();
            while (!done) {
                try {
                    if (socket == null) {
                        throw new IOException("Socket is null");
                    }
                    Log.d("connection: waiting for next packet");
                    byte[] packet = socket.readPacket();
                    if (packet == null) {
                        throw new IOException("No more packets");
                    }
                    parsePacket(packet);
                } catch (Exception e) {
                    Log.e("Packet Reader error; maybe disconnecting", e);
                    if (!done) {
                        disconnect();
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
            } catch (InvalidProtocolBufferException e) {
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
                    handled = processGroupFeedItems(outList, msg.getId());
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

                    bgWorkers.execute(() -> {
                        Message message = ChatMessageProtocol.getInstance().parseMessage(chatStanza, msg.getId(), fromUserId);
                        if (message == null) {
                            Log.e("connection: got plaintext payload");
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
                } else if (msg.hasGroupChat()) {
                    Log.i("connection: got group chat " + ProtoPrinter.toString(msg));
                    Log.i("connection: silently dropping group chat message " + msg.getId());
                    sendAck(msg.getId());
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
                        connectionObservers.notifyGroupCreated(groupId, groupStanza.getName(), groupStanza.getAvatarId(), elements, Preconditions.checkNotNull(senderUserId), senderName, ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.MODIFY_MEMBERS)) {
                        connectionObservers.notifyGroupMemberChangeReceived(groupId, groupStanza.getName(), groupStanza.getAvatarId(), elements, Preconditions.checkNotNull(senderUserId), senderName, ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.LEAVE)) {
                        connectionObservers.notifyGroupMemberLeftReceived(groupId, elements, ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.MODIFY_ADMINS)) {
                        connectionObservers.notifyGroupAdminChangeReceived(groupId, elements, Preconditions.checkNotNull(senderUserId), senderName, ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.CHANGE_NAME)) {
                        connectionObservers.notifyGroupNameChangeReceived(groupId, groupStanza.getName(), Preconditions.checkNotNull(senderUserId), senderName, ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.CHANGE_AVATAR)) {
                        connectionObservers.notifyGroupAvatarChangeReceived(groupId, groupStanza.getAvatarId(), Preconditions.checkNotNull(senderUserId), senderName, ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.AUTO_PROMOTE_ADMINS)) {
                        connectionObservers.notifyGroupAdminAutoPromoteReceived(groupId, elements, ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.DELETE)) {
                        connectionObservers.notifyGroupDeleteReceived(groupId, Preconditions.checkNotNull(senderUserId), senderName, ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.JOIN)) {
                        connectionObservers.notifyGroupMemberJoinReceived(groupId, groupStanza.getName(), groupStanza.getAvatarId(), elements, Preconditions.checkNotNull(senderUserId), senderName, ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.SET_BACKGROUND)) {
                        Background background = null;
                        try {
                            background = Background.parseFrom(groupStanza.getBackgroundBytes());
                        } catch (InvalidProtocolBufferException e) {
                            Log.e("connection: invalid background received", e);
                        }
                        connectionObservers.notifyGroupBackgroundChangeReceived(groupId, background == null ? 0 : background.getTheme(), senderUserId, senderName, ackId);
                    } else if (groupStanza.getAction().equals(GroupStanza.Action.CHANGE_DESCRIPTION)) {
                        connectionObservers.notifyGroupDescriptionChanged(groupId, groupStanza.getDescription(), Preconditions.checkNotNull(senderUserId), senderName, ackId);
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

                    connectionObservers.notifyMessageRerequest(userId, rerequest.getId(), peerIdentityKey, otpkId, sessionSetupKey, messageEphemeralKey, msg.getId());
                    handled = true;
                }
            }
            if (!handled) {
                Log.i("connection: did not handle message " + ProtoPrinter.toString(msg));
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
                        connectionObservers.notifyCommentRetracted(comment.getId(), publisherUserId, comment.getPostId(), comment.getTimestamp() * 1000L);
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
                        Post post = processPost(protoPost, names, null);
                        if (post != null) {
                            post.seen = item.getAction().equals(com.halloapp.proto.server.FeedItem.Action.PUBLISH) ? Post.SEEN_NO : Post.SEEN_NO_HIDDEN;
                            posts.add(post);
                        } else {
                            Log.e("connection: invalid post");
                        }

                    } else if (item.hasComment()) {
                        com.halloapp.proto.server.Comment protoComment = item.getComment();
                        Comment comment = processComment(protoComment, names, null);
                        if (comment != null) {
                            comment.seen = item.getAction().equals(com.halloapp.proto.server.FeedItem.Action.SHARE);
                            comments.add(comment);
                        } else {
                            Log.e("connection: invalid comment");
                        }
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

        private Post processPost(com.halloapp.proto.server.Post protoPost, Map<UserId, String> names, @Nullable GroupId groupId) {
            if (protoPost.getPublisherUid() != 0 && protoPost.getPublisherName() != null) {
                names.put(new UserId(Long.toString(protoPost.getPublisherUid())), protoPost.getPublisherName());
            }

            UserId publisherUserId = getUserId(Long.toString(protoPost.getPublisherUid()));
            byte[] payload = protoPost.getPayload().toByteArray();

            if (groupId != null) {
                byte[] encPayload = protoPost.getEncPayload().toByteArray();
                if (encPayload != null && encPayload.length > 0) {
                    try {
                        // TODO: stats
                        byte[] decPayload = GroupFeedSessionManager.getInstance().decryptMessage(encPayload, groupId, publisherUserId);
                        if (!Arrays.equals(payload, decPayload)) {
                            Log.e("Group Feed Encryption plaintext and decrypted differ");
                        }
                    } catch (CryptoException e) {
                        Log.e("Failed to decrypt encrypted group payload", e);
                    }
                }
            }

            Container container;
            try {
                container = Container.parseFrom(payload);
            } catch (InvalidProtocolBufferException e) {
                Log.e("connection: invalid post payload", e);
                return null;
            }
            if (!container.hasPostContainer()) {
                PublishedEntry publishedEntry = PublishedEntry.getFeedEntry(Base64.encodeToString(payload, Base64.NO_WRAP), protoPost.getId(), protoPost.getTimestamp(), Long.toString(protoPost.getPublisherUid()));

                // NOTE: publishedEntry.timestamp == 1000L * protoPost.getTimestamp()
                UserId posterUserId = getUserId(Long.toString(protoPost.getPublisherUid()));
                @Post.TransferredState int transferState = publishedEntry.media.isEmpty() || posterUserId.isMe() ? Post.TRANSFERRED_YES : Post.TRANSFERRED_NO;
                Post np = new Post(-1, posterUserId, protoPost.getId(), publishedEntry.timestamp, transferState, Post.SEEN_NO, publishedEntry.text);
                for (PublishedEntry.Media entryMedia : publishedEntry.media) {
                    np.media.add(Media.createFromUrl(PublishedEntry.getMediaType(entryMedia.type), entryMedia.url,
                            entryMedia.encKey, entryMedia.encSha256hash,
                            entryMedia.width, entryMedia.height,
                            entryMedia.blobVersion, entryMedia.chunkSize, entryMedia.blobSize));
                }
                for (com.halloapp.proto.clients.Mention mentionProto : publishedEntry.mentions) {
                    Mention mention = Mention.parseFromProto(mentionProto);
                    processMention(mention);
                    np.mentions.add(mention);
                }
                return np;
            } else {
                PostContainer postContainer = container.getPostContainer();
                UserId posterUserId = getUserId(Long.toString(protoPost.getPublisherUid()));
                long timeStamp = 1000L * protoPost.getTimestamp();

                return feedContentParser.parsePost(protoPost.getId(), posterUserId, timeStamp, postContainer);
            }
        }

        private Comment processComment(com.halloapp.proto.server.Comment protoComment, Map<UserId, String> names, @Nullable GroupId groupId) {
            if (protoComment.getPublisherUid() != 0 && protoComment.getPublisherName() != null) {
                names.put(new UserId(Long.toString(protoComment.getPublisherUid())), protoComment.getPublisherName());
            }

            UserId publisherUserId = getUserId(Long.toString(protoComment.getPublisherUid()));
            byte[] payload = protoComment.getPayload().toByteArray();

            if (groupId != null) {
                byte[] encPayload = protoComment.getEncPayload().toByteArray();
                if (encPayload != null && encPayload.length > 0) {
                    try {
                        // TODO: stats
                        byte[] decPayload = GroupFeedSessionManager.getInstance().decryptMessage(encPayload, groupId, publisherUserId);
                        if (!Arrays.equals(payload, decPayload)) {
                            Log.e("Group Feed Encryption plaintext and decrypted differ");
                        }
                    } catch (CryptoException e) {
                        Log.e("Failed to decrypt encrypted group payload", e);
                    }
                }
            }

            Container container;
            try {
                container = Container.parseFrom(payload);
            } catch (InvalidProtocolBufferException e) {
                Log.e("connection: invalid comment payload");
                return null;
            }

            if (!container.hasCommentContainer()) {
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
                for (PublishedEntry.Media entryMedia : publishedEntry.media) {
                    comment.media.add(Media.createFromUrl(PublishedEntry.getMediaType(entryMedia.type), entryMedia.url,
                            entryMedia.encKey, entryMedia.encSha256hash,
                            entryMedia.width, entryMedia.height,
                            entryMedia.blobVersion, entryMedia.chunkSize, entryMedia.blobSize));
                }
                for (com.halloapp.proto.clients.Mention mentionProto : publishedEntry.mentions) {
                    Mention mention = Mention.parseFromProto(mentionProto);
                    processMention(mention);
                    comment.mentions.add(mention);
                }
                return comment;
            } else {
                CommentContainer commentContainer = container.getCommentContainer();
                long timestamp = protoComment.getTimestamp() * 1000L;
                UserId publisherId = getUserId(Long.toString(protoComment.getPublisherUid()));

                return feedContentParser.parseComment(protoComment.getId(), protoComment.getParentCommentId(), publisherId, timestamp, commentContainer);
            }
        }


        private boolean processGroupFeedItems(@NonNull List<GroupFeedItem> items, @NonNull String ackId) {
            final List<Post> posts = new ArrayList<>();
            final List<Comment> comments = new ArrayList<>();
            final Map<UserId, String> names = new HashMap<>();

            for (GroupFeedItem item : items) {
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
                    byte[] peerPublicIdentityKey = senderStateWithKeyInfo.getPublicKey().toByteArray();
                    long oneTimePreKeyId = senderStateWithKeyInfo.getOneTimePreKeyId();
                    SessionSetupInfo sessionSetupInfo = peerPublicIdentityKey == null || peerPublicIdentityKey.length == 0 ? null : new SessionSetupInfo(new PublicEdECKey(peerPublicIdentityKey), (int) oneTimePreKeyId);
                    try {
                        byte[] senderStateDec = SignalSessionManager.getInstance().decryptMessage(encSenderState, publisherUserId, sessionSetupInfo);
                        SenderState senderState = SenderState.parseFrom(senderStateDec);
                        SenderKey senderKey = senderState.getSenderKey();
                        int currentChainIndex = senderState.getCurrentChainIndex();
                        byte[] chainKey = senderKey.getChainKey().toByteArray();
                        byte[] publicSignatureKeyBytes = senderKey.getPublicSignatureKey().toByteArray();
                        PublicEdECKey publicSignatureKey = new PublicEdECKey(publicSignatureKeyBytes);

                        EncryptedKeyStore encryptedKeyStore = EncryptedKeyStore.getInstance();
                        encryptedKeyStore.setPeerGroupCurrentChainIndex(groupId, publisherUserId, currentChainIndex);
                        encryptedKeyStore.setPeerGroupChainKey(groupId, publisherUserId, chainKey);
                        encryptedKeyStore.setPeerGroupSigningKey(groupId, publisherUserId, publicSignatureKey);
                    } catch (CryptoException e) {
                        Log.e("Failed to decrypt sender state for " + ProtoPrinter.toString(item), e);
                    } catch (InvalidProtocolBufferException e) {
                        Log.e("Failed to parse sender state for " + ProtoPrinter.toString(item), e);
                    }
                }

                if (item.getAction() == GroupFeedItem.Action.PUBLISH || item.getAction() == GroupFeedItem.Action.SHARE) {
                    if (item.hasComment()) {
                        com.halloapp.proto.server.Comment protoComment = item.getComment();
                        Comment comment = processComment(protoComment, names, groupId);
                        if (comment != null) {
                            comments.add(comment);
                        } else {
                            Log.e("connection: invalid comment");
                        }
                    } else if (item.hasPost()) {
                        com.halloapp.proto.server.Post protoPost = item.getPost();
                        Post post = processPost(protoPost, names, groupId);
                        if (post != null) {
                            post.seen = item.getAction().equals(GroupFeedItem.Action.PUBLISH) ? Post.SEEN_NO : Post.SEEN_NO_HIDDEN;
                            post.setParentGroup(new GroupId(item.getGid()));
                            posts.add(post);
                        }
                    }
                } else if (item.getAction() == GroupFeedItem.Action.RETRACT) {
                    if (item.hasComment()) {
                        com.halloapp.proto.server.Comment comment = item.getComment();
                        connectionObservers.notifyCommentRetracted(comment.getId(), getUserId(Long.toString(comment.getPublisherUid())), comment.getPostId(), comment.getTimestamp() * 1000L);
                    } else if (item.hasPost()) {
                        com.halloapp.proto.server.Post post = item.getPost();
                        connectionObservers.notifyPostRetracted(getUserId(Long.toString(post.getPublisherUid())), new GroupId(item.getGid()), post.getId());
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
            final Runnable handler = ackHandlers.remove(ack.getId());
            if (handler != null) {
                handler.run();
            } else {
                Log.w("connection: ack doesn't match any pending message " + ProtoPrinter.toString(ack));
            }
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

    private class PacketWriter {
        private static final int QUEUE_CAPACITY = 100;

        private final ArrayBlockingQueue<Packet> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY, true);

        private volatile boolean done;

        private Thread writerThread;

        void init() {
            queue.clear();
            done = false;
            writerThread = ThreadUtils.go(this::writePackets, "Packet Writer"); // TODO(jack): Connection counter
        }

        void shutdown() {
            done = true;
            if (writerThread != null) {
                writerThread.interrupt();
                writerThread = null;
            }
            queue.clear();
        }

        void sendPacket(Packet packet) {
            boolean success = false;
            while (!success && !done) {
                try {
                    enqueue(packet);
                    success = true;
                } catch (InterruptedException e) {
                    Log.w("Interrupted while enqueueing packet for send", e);
                }
            }
        }

        private void enqueue(Packet packet) throws InterruptedException {
            queue.put(packet);
        }

        private void writePackets() {
            ThreadUtils.setSocketTag();
            try {
                while (!done) {
                    try {
                        if (socket == null) {
                            throw new IOException("Socket is null");
                        }
                        Packet packet = queue.take();
                        Log.i("connection: send: " + ProtoPrinter.toString(packet));

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
    }

    private class IqRouter {
        private static final long IQ_TIMEOUT_MS = 20_000;

        private final Map<String, Iq> responses = new ConcurrentHashMap<>();
        private final Map<String, ResponseHandler<Iq>> successCallbacks = new ConcurrentHashMap<>();
        private final Map<String, ExceptionHandler> failureCallbacks = new ConcurrentHashMap<>();

        private boolean done;
        private final Object callbackRemovalLock = new Object(); // make sure exactly one callback is ever removed per id

        private ResponseHandler<Iq> fetchSuccessCallback(String id) {
            synchronized (callbackRemovalLock) {
                ResponseHandler<Iq> callback = successCallbacks.remove(id);
                failureCallbacks.remove(id);
                return callback;
            }
        }

        private ExceptionHandler fetchFailureCallback(String id) {
            synchronized (callbackRemovalLock) {
                ExceptionHandler callback = failureCallbacks.remove(id);
                successCallbacks.remove(id);
                return callback;
            }
        }

        public void onResponse(String id, Iq iq) {
            responses.put(id, iq);

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

        public Observable<Iq> sendAsync(Iq iq) {
            if (done) {
                MutableObservable<Iq> observable = new MutableObservable<>();
                observable.setException(new NotConnectedException());
                return observable;
            }
            BackgroundObservable<Iq> observable = new BackgroundObservable<>(bgWorkers);
            Packet packet = Packet.newBuilder().setIq(iq).build();
            setCallbacks(iq.getId(), observable::setResponse, observable::setException);
            sendPacket(packet);
            return observable;
        }

        void init() {
            done = false;
            reset();
        }

        void shutdown() {
            done = true;
            reset();
        }

        private void reset() {
            Map<String, ExceptionHandler> failureCallbacksCopy;
            synchronized (callbackRemovalLock) {
                responses.clear();
                successCallbacks.clear();

                failureCallbacksCopy = new HashMap<>(failureCallbacks);
                failureCallbacks.clear();
            }

            for (String id : failureCallbacksCopy.keySet()) {
                Log.i("IqRouter marking " + id + " as failure during reset");
                ExceptionHandler failure = failureCallbacksCopy.get(id);
                if (failure != null) {
                    failure.handleException(new IqRouterResetException(id));
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
            synchronized (callbackRemovalLock) {
                responses.remove(id);
                successCallbacks.remove(id);
                failureCallbacks.remove(id);
            }
        }

        private void scheduleRemoval(String id) {
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    executor.execute(() -> {
                        performRemoval(id);
                    });
                }
            };
            timer.schedule(timerTask, IQ_TIMEOUT_MS);
        }

        private void performRemoval(String id) {
            ExceptionHandler failure = null;
            ResponseHandler<Iq> success = null;
            Iq response;

            synchronized (callbackRemovalLock) {
                response = responses.get(id);
                if (response == null) {
                    failure = failureCallbacks.get(id);
                } else {
                    success = successCallbacks.get(id);
                }
                clear(id);
            }

            if (failure != null) {
                failure.handleException(new IqTimeoutException(id));
            } else if (success != null) {
                success.handleResponse(response);
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
