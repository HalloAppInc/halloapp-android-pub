package com.halloapp.xmpp;

import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;

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
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.stats.Stats;
import com.halloapp.xmpp.feed.FeedItem;
import com.halloapp.xmpp.feed.FeedMessageElement;
import com.halloapp.xmpp.feed.FeedUpdateIq;
import com.halloapp.xmpp.feed.SharePosts;
import com.halloapp.xmpp.groups.GroupChangeMessage;
import com.halloapp.xmpp.groups.GroupChatMessage;
import com.halloapp.xmpp.groups.GroupResponseIq;
import com.halloapp.xmpp.groups.GroupsListResponseIq;
import com.halloapp.xmpp.groups.MemberElement;
import com.halloapp.xmpp.invites.InvitesResponseIq;
import com.halloapp.xmpp.privacy.PrivacyListsResponseIq;
import com.halloapp.xmpp.props.ServerPropsRequestIq;
import com.halloapp.xmpp.props.ServerPropsResponseIq;
import com.halloapp.xmpp.util.BackgroundObservable;
import com.halloapp.xmpp.util.Observable;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackFuture;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.NamedElement;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.debugger.android.AndroidDebugger;
import org.jivesoftware.smackx.pubsub.EventElement;
import org.jivesoftware.smackx.pubsub.EventElementType;
import org.jivesoftware.smackx.pubsub.ItemsExtension;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.packet.PubSubNamespace;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SuppressWarnings("WeakerAccess")
public class OldConnection extends Connection {

    public static final String XMPP_DOMAIN = "s.halloapp.net";
    private static final String HOST = "s.halloapp.net";
    private static final String DEBUG_HOST = "s-test.halloapp.net";
    private static final int PORT = 5222;
    private static final int CONNECTION_TIMEOUT = 20_000;
    private static final int REPLY_TIMEOUT = 20_000;

    public static final String FEED_THREAD_ID = "feed";

    private BgWorkers bgWorkers;
    private ConnectionObservers connectionObservers;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private @Nullable XMPPTCPConnection connection;
    private PubSubHelper pubSubHelper;
    private Me me;
    private Preferences preferences;
    private final Map<String, Runnable> ackHandlers = new ConcurrentHashMap<>();
    public boolean clientExpired = false;
    private String connectionPropHash;

    OldConnection(
            @NonNull Me me,
            @NonNull BgWorkers bgWorkers,
            @NonNull Preferences preferences,
            @NonNull ConnectionObservers connectionObservers) {
        this.me = me;
        this.bgWorkers = bgWorkers;
        this.preferences = preferences;
        this.connectionObservers = connectionObservers;
        final HandlerThread handlerThread = new HandlerThread("ConnectionThread");
        handlerThread.start();
        SmackConfiguration.DEBUG = BuildConfig.DEBUG;
        Roster.setRosterLoadedAtLoginDefault(false);
    }

    public void connect() {
        executor.execute(this::connectInBackground);
    }

    @WorkerThread
    private void connectInBackground() {
        if (me == null) {
            Log.i("connection: me is null");
            return;
        }
        if (!me.isRegistered()) {
            Log.i("connection: not registered");
            return;
        }
        if (connection != null && connection.isConnected() && connection.isAuthenticated()) {
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

        ProviderManager.addExtensionProvider("item", "http://jabber.org/protocol/pubsub", new PubSubItem.Provider()); // smack doesn't handle 'publisher' and 'timestamp' attributes
        ProviderManager.addExtensionProvider("item", "http://jabber.org/protocol/pubsub#event", new PubSubItem.Provider()); // smack doesn't handle 'publisher' and 'timestamp' attributes
        ProviderManager.addExtensionProvider("retract", "http://jabber.org/protocol/pubsub", new PubSubItem.Provider()); // smack doesn't handle 'publisher' and 'timestamp' attributes
        ProviderManager.addExtensionProvider("retract", "http://jabber.org/protocol/pubsub#event", new PubSubItem.Provider()); // smack doesn't handle 'publisher' and 'timestamp' attributes
        ProviderManager.addExtensionProvider(ChatMessageElement.ELEMENT, ChatMessageElement.NAMESPACE, new ChatMessageElement.Provider());
        ProviderManager.addExtensionProvider(DeliveryReceiptElement.ELEMENT, DeliveryReceiptElement.NAMESPACE, new DeliveryReceiptElement.Provider());
        ProviderManager.addExtensionProvider(SeenReceiptElement.ELEMENT, SeenReceiptElement.NAMESPACE, new SeenReceiptElement.Provider());
        ProviderManager.addExtensionProvider(ContactList.ELEMENT, ContactList.NAMESPACE, new ContactList.Provider());
        ProviderManager.addExtensionProvider(WhisperKeysMessage.ELEMENT, WhisperKeysMessage.NAMESPACE, new WhisperKeysMessage.Provider());
        ProviderManager.addExtensionProvider(AvatarChangeMessage.ELEMENT, AvatarChangeMessage.NAMESPACE, new AvatarChangeMessage.Provider());
        ProviderManager.addExtensionProvider(GroupChangeMessage.ELEMENT, GroupChangeMessage.NAMESPACE, new GroupChangeMessage.Provider());
        ProviderManager.addExtensionProvider(GroupChatMessage.ELEMENT, GroupChatMessage.NAMESPACE, new GroupChatMessage.Provider());
        ProviderManager.addExtensionProvider(MemberElement.ELEMENT, MemberElement.NAMESPACE, new MemberElement.Provider());
        ProviderManager.addExtensionProvider(RerequestElement.ELEMENT, RerequestElement.NAMESPACE, new RerequestElement.Provider());
        ProviderManager.addExtensionProvider(FeedMessageElement.ELEMENT, FeedMessageElement.NAMESPACE, new FeedMessageElement.Provider());
        ProviderManager.addIQProvider(ContactsSyncResponseIq.ELEMENT, ContactsSyncResponseIq.NAMESPACE, new ContactsSyncResponseIq.Provider());
        ProviderManager.addIQProvider(PrivacyListsResponseIq.ELEMENT, PrivacyListsResponseIq.NAMESPACE, new PrivacyListsResponseIq.Provider());
        ProviderManager.addIQProvider(InvitesResponseIq.ELEMENT, InvitesResponseIq.NAMESPACE, new InvitesResponseIq.Provider());
        ProviderManager.addIQProvider(ServerPropsResponseIq.ELEMENT, ServerPropsResponseIq.NAMESPACE, new ServerPropsResponseIq.Provider());
        ProviderManager.addIQProvider(MediaUploadIq.ELEMENT, MediaUploadIq.NAMESPACE, new MediaUploadIq.Provider());
        ProviderManager.addIQProvider(SecondsToExpirationIq.ELEMENT, SecondsToExpirationIq.NAMESPACE, new SecondsToExpirationIq.Provider());
        ProviderManager.addIQProvider(WhisperKeysResponseIq.ELEMENT, WhisperKeysResponseIq.NAMESPACE, new WhisperKeysResponseIq.Provider());
        ProviderManager.addIQProvider(AvatarIq.ELEMENT, AvatarIq.NAMESPACE, new AvatarIq.Provider());
        ProviderManager.addIQProvider(GroupResponseIq.ELEMENT, GroupResponseIq.NAMESPACE, new GroupResponseIq.Provider());
        ProviderManager.addIQProvider(GroupsListResponseIq.ELEMENT, GroupsListResponseIq.NAMESPACE, new GroupsListResponseIq.Provider());

        final String host = preferences.getUseDebugHost() ? DEBUG_HOST : HOST;
        try {
            final XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(me.getUser(), me.getPassword())
                    .setResource("android")
                    .setXmppDomain(XMPP_DOMAIN)
                    .setHost(host)
                    .setConnectTimeout(CONNECTION_TIMEOUT)
                    .setSendPresence(false)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
                    .setPort(PORT)
                    .setDebuggerFactory(connection -> new AndroidDebugger(connection) {
                        @Override
                        protected void log(String logMessage) {
                            Log.d("connection: " + logMessage);
                        }

                        @Override
                        protected void log(String logMessage, Throwable throwable) {
                            Log.w("connection: " + logMessage, throwable);
                        }
                    })
                    .build();
            connection = new XMPPTCPConnection(config, () -> {
                clientExpired();
                connectionObservers.notifyClientVersionExpired();
            });
            connection.setReplyTimeout(REPLY_TIMEOUT);
            connection.setUseStreamManagement(false);
            connection.addConnectionListener(new HalloConnectionListener());
        } catch (XmppStringprepException e) {
            Log.e("connection: cannot create connection", e);
            connection = null;
            return;
        }

        connection.addSyncStanzaListener(new MessageStanzaListener(), new StanzaTypeFilter(org.jivesoftware.smack.packet.Message.class));
        connection.addSyncStanzaListener(new AckStanzaListener(), new StanzaTypeFilter(AckStanza.class));
        connection.addSyncStanzaListener(new PresenceStanzaListener(), new StanzaTypeFilter(PresenceStanza.class));
        connection.addSyncStanzaListener(new ChatStateStanzaListener(), new StanzaTypeFilter(ChatStateStanza.class));

        Log.i("connection: connecting...");
        try {
            connection.connect();
        } catch (XMPPException | SmackException | IOException | InterruptedException e) {
            Log.e("connection: cannot connect", e);
            disconnectInBackground();
            return;
        }

        Log.i("connection: logging in...");
        try {
            connection.login();
        } catch (SASLErrorException e) {
            Log.e("connection: cannot login", e);
            disconnectInBackground();
            if ("not-authorized".equals(e.getSASLFailure().getSASLErrorString())) {
                connectionObservers.notifyLoginFailed();
            }
            return;
        } catch (XMPPException | SmackException | IOException | InterruptedException e) {
            Log.e("connection: cannot login", e);
            disconnectInBackground();
            return;
        }

        pubSubHelper = new PubSubHelper(connection);

        connectionPropHash = connection.getServerPropsHash();

        connectionObservers.notifyConnected();

        Log.i("connection: connected");
    }

    @Nullable
    public String getConnectionPropHash() {
        return connectionPropHash;
    }

    public void clientExpired() {
        clientExpired = true;
        disconnect();
    }

    public void disconnect() {
        executor.execute(this::disconnectInBackground);
    }

    @WorkerThread
    private void disconnectInBackground() {
        if (connection == null) {
            Log.e("connection: cannot disconnect, no connection");
            return;
        }
        if (connection.isConnected()) {
            Log.i("connection: disconnecting");
            connection.setReplyTimeout(1_000);
            connection.disconnect();
        }
        connection = null;
        connectionObservers.notifyDisconnected();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @WorkerThread
    private boolean reconnectIfNeeded() {
        if (connection != null && connection.isConnected() && connection.isAuthenticated()) {
            return true;
        }
        if (me == null) {
            Log.e("connection: cannot reconnect, me is null");
            return false;
        }
        connectInBackground();
        return connection != null && connection.isConnected() && connection.isAuthenticated();
    }

    public void requestServerProps() {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: request server props: no connection");
                return;
            }
            ServerPropsRequestIq requestIq = new ServerPropsRequestIq();
            requestIq.setTo(connection.getXMPPServiceDomain());
            try {
                ServerPropsResponseIq responseIq = connection.createStanzaCollectorAndSend(requestIq).nextResultOrThrow();
                connectionObservers.notifyServerPropsReceived(responseIq.getProps(), responseIq.getHash());
            } catch (InterruptedException | SmackException.NoResponseException | SmackException.NotConnectedException | XMPPException.XMPPErrorException e) {
                Log.e("connection: failed to get server props", e);
            }
        });
    }

    public Future<Integer> requestSecondsToExpiration() {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: request seconds to expiration: no connection");
                return null;
            }
            final SecondsToExpirationIq secondsToExpirationIq = new SecondsToExpirationIq(connection.getXMPPServiceDomain());
            try {
                final SecondsToExpirationIq response = connection.createStanzaCollectorAndSend(secondsToExpirationIq).nextResultOrThrow();
                return response.secondsLeft;
            } catch (SmackException.NotConnectedException | InterruptedException | XMPPException.XMPPErrorException | SmackException.NoResponseException e) {
                Log.e("connection: request seconds to expiration", e);
            }
            return null;
        });
    }

    public Future<MediaUploadIq.Urls> requestMediaUpload(long fileSize) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: request media upload: no connection");
                return null;
            }
            final MediaUploadIq mediaUploadIq = new MediaUploadIq(connection.getXMPPServiceDomain(), fileSize);
            try {
                final MediaUploadIq response = connection.createStanzaCollectorAndSend(mediaUploadIq).nextResultOrThrow();
                return response.urls;
            } catch (SmackException.NotConnectedException | InterruptedException | XMPPException.XMPPErrorException | SmackException.NoResponseException e) {
                Log.e("connection: request media upload", e);
            }
            return null;
        });
    }

    public Future<List<ContactInfo>> syncContacts(@Nullable Collection<String> addPhones, @Nullable Collection<String> deletePhones, boolean fullSync, @Nullable String syncId, int index, boolean lastBatch) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: sync contacts: no connection");
                return null;
            }
            final ContactsSyncRequestIq contactsSyncIq = new ContactsSyncRequestIq(connection.getXMPPServiceDomain(),
                    addPhones, deletePhones, fullSync, syncId, index, lastBatch);
            try {
                final ContactsSyncResponseIq response = connection.createStanzaCollectorAndSend(contactsSyncIq).nextResultOrThrow();
                return response.contactList.contacts;
            } catch (SmackException.NotConnectedException | InterruptedException | XMPPException.XMPPErrorException | SmackException.NoResponseException e) {
                Log.e("connection: cannot sync contacts", e);
            }
            return null;
        });
    }

    public void sendPushToken(@NonNull final String pushToken) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: send push token: no connection");
                return;
            }
            final PushRegisterRequestIq pushIq = new PushRegisterRequestIq(connection.getXMPPServiceDomain(), pushToken);
            try {
                final IQ response = connection.createStanzaCollectorAndSend(pushIq).nextResultOrThrow();
                Log.d("connection: response after setting the push token " + response.toString());
            } catch (SmackException.NotConnectedException | InterruptedException | XMPPException.XMPPErrorException | SmackException.NoResponseException e) {
                Log.e("connection: cannot send push token", e);
            }
        });
    }

    public Future<Boolean> sendName(@NonNull final String name) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: send name: no connection");
                return Boolean.FALSE;
            }
            final UserNameIq nameIq = new UserNameIq(connection.getXMPPServiceDomain(), name);
            try {
                final IQ response = connection.createStanzaCollectorAndSend(nameIq).nextResultOrThrow();
                Log.d("connection: response after setting name " + response.toString());
                return Boolean.TRUE;
            } catch (SmackException.NotConnectedException | InterruptedException | XMPPException.XMPPErrorException | SmackException.NoResponseException e) {
                Log.e("connection: cannot send name", e);
                return Boolean.FALSE;
            }
        });
    }

    public void subscribePresence(UserId userId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: request presence subscription: no connection");
                return;
            }
            try {
                PresenceStanza stanza = new PresenceStanza(userIdToJid(userId), "subscribe");
                connection.sendStanza(stanza);
            } catch (InterruptedException | SmackException.NotConnectedException e) {
                Log.e("Failed to subscribe", e);
            }
        });
    }

    public void updatePresence(boolean available) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: update presence: no connection");
                return;
            }
            try {
                PresenceStanza stanza = new PresenceStanza(connection.getXMPPServiceDomain(), available ? "available" : "away");
                connection.sendStanza(stanza);
            } catch (InterruptedException | SmackException.NotConnectedException e) {
                Log.e("Failed to update presence", e);
            }
        });
    }

    public void updateChatState(@NonNull ChatId chat, @ChatState.Type int state) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: update chat state: no connection");
                return;
            }
            try {
                ChatStateStanza stanza = new ChatStateStanza(connection.getXMPPServiceDomain(), state == ChatState.Type.TYPING ? "typing" : "available", chat);
                connection.sendStanza(stanza);
            } catch (InterruptedException | SmackException.NotConnectedException e) {
                Log.e("Failed to update chat state", e);
            }
        });
    }

    public Future<Boolean> uploadKeys(@Nullable byte[] identityKey, @Nullable byte[] signedPreKey, @NonNull List<byte[]> oneTimePreKeys) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: upload keys: no connection");
                return Boolean.FALSE;
            }
            final WhisperKeysUploadIq uploadIq = new WhisperKeysUploadIq(connection.getXMPPServiceDomain(), identityKey, signedPreKey, oneTimePreKeys);
            try {
                final IQ response = connection.createStanzaCollectorAndSend(uploadIq).nextResultOrThrow();
                Log.d("connection: response after uploading keys " + response.toString());
                return Boolean.TRUE;
            } catch (SmackException.NotConnectedException | InterruptedException | XMPPException.XMPPErrorException | SmackException.NoResponseException e) {
                Log.e("connection: cannot upload keys", e);
                return Boolean.FALSE;
            }
        });
    }

    public void uploadMoreOneTimePreKeys(@NonNull List<byte[]> oneTimePreKeys) {
        uploadKeys(null, null, oneTimePreKeys);
    }

    public Future<WhisperKeysResponseIq> downloadKeys(@NonNull UserId userId) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: download keys: no connection");
                return null;
            }
            final WhisperKeysDownloadIq downloadIq = new WhisperKeysDownloadIq(connection.getXMPPServiceDomain(), userIdToJid(userId).toString(), userId);
            try {
                final WhisperKeysResponseIq response = connection.createStanzaCollectorAndSend(downloadIq).nextResultOrThrow();
                Log.d("connection: response after downloading keys " + response.toString());
                return response;
            } catch (SmackException.NotConnectedException | InterruptedException | XMPPException.XMPPErrorException | SmackException.NoResponseException e) {
                Log.e("connection: cannot download keys", e);
            }
            return null;
        });
    }

    public Future<Integer> getOneTimeKeyCount() {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: get one time key count: no connection");
                return null;
            }
            final WhisperKeysCountIq countIq = new WhisperKeysCountIq(connection.getXMPPServiceDomain());
            try {
                final WhisperKeysResponseIq response = connection.createStanzaCollectorAndSend(countIq).nextResultOrThrow();
                Log.d("connection: response for get key count  " + response.toString());
                return response.count;
            } catch (SmackException.NotConnectedException | InterruptedException | XMPPException.XMPPErrorException | SmackException.NoResponseException e) {
                Log.e("connection: cannot get one time key count", e);
            }
            return null;
        });
    }

    public Future<Void> sendStats(List<Stats.Counter> counters) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: send stats: no connection");
                return null;
            }
            final StatsIq statsIq = new StatsIq(connection.getXMPPServiceDomain(), counters);
            try {
                final IQ response = connection.createStanzaCollectorAndSend(statsIq).nextResultOrThrow();
                Log.d("connection: response for send stats  " + response.toString());
                return null;
            } catch (SmackException.NotConnectedException | InterruptedException | XMPPException.XMPPErrorException | SmackException.NoResponseException e) {
                Log.e("connection: cannot send stats", e);
            }
            return null;
        });
    }

    public Future<String> setAvatar(String base64, long numBytes, int width, int height) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot update avatar, no connection");
                return null;
            }
            try {
                final AvatarIq avatarIq = new AvatarIq(connection.getXMPPServiceDomain(), base64, numBytes, height, width);
                final AvatarIq response = connection.createStanzaCollectorAndSend(avatarIq).nextResultOrThrow();
                return response.avatarId;
            } catch (SmackException.NotConnectedException | InterruptedException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.w("connection: cannot update avatar", e);
            }
            return null;
        });
    }

    public Future<String> setGroupAvatar(GroupId groupId, String base64) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot set group avatar, no connection");
                return null;
            }
            try {
                final GroupAvatarIq avatarIq = new GroupAvatarIq(connection.getXMPPServiceDomain(), groupId, base64);
                final GroupResponseIq response = connection.createStanzaCollectorAndSend(avatarIq).nextResultOrThrow();
                return response.avatar;
            } catch (SmackException.NotConnectedException | InterruptedException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.w("connection: cannot update avatar", e);
            }
            return null;
        });
    }

    public Future<String> getAvatarId(UserId userId) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot update avatar, no connection");
                return null;
            }
            try {
                final AvatarIq setAvatarIq = new AvatarIq(connection.getXMPPServiceDomain(), userId);
                final AvatarIq response = connection.createStanzaCollectorAndSend(setAvatarIq).nextResultOrThrow();
                return response.avatarId;
            } catch (SmackException.NotConnectedException | InterruptedException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.w("connection: cannot update avatar", e);
            }
            return null;
        });
    }

    public Future<String> getMyAvatarId() {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot get my avatar, no connection");
                return null;
            }
            try {
                final AvatarIq getAvatarIq = new AvatarIq(connection.getXMPPServiceDomain(), new UserId(connection.getUser().getLocalpart().toString()));
                final AvatarIq response = connection.createStanzaCollectorAndSend(getAvatarIq).nextResultOrThrow();
                return response.avatarId;
            } catch (SmackException.NotConnectedException | InterruptedException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.w("connection: cannot get my avatar", e);
            }
            return null;
        });
    }

    public Future<Boolean> sharePosts(final Map<UserId, Collection<Post>> shareMap) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
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
                updateIq.setTo(connection.getXMPPServiceDomain());

                connection.createStanzaCollectorAndSend(updateIq).nextResultOrThrow();
            } catch (SmackException.NotConnectedException | InterruptedException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.e("connection: cannot send post", e);
                return false;
            }
            return true;
        });
    }

    public void sendPost(final @NonNull Post post) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot send post, no connection");
                return;
            }
            try {
                final PublishedEntry entry = new PublishedEntry(
                        PublishedEntry.ENTRY_FEED,
                        null,
                        post.timestamp,
                        connection.getUser().getLocalpart().toString(),
                        post.text,
                        null,
                        null);
                for (Media media : post.media) {
                    entry.media.add(new PublishedEntry.Media(PublishedEntry.getMediaType(media.type), media.url, media.encKey, media.sha256hash, media.width, media.height));
                }
                for (Mention mention : post.mentions) {
                    entry.mentions.add(Mention.toProto(mention));
                }
                if (Constants.NEW_FEED_API && post.getAudienceType() != null) {
                    FeedItem feedItem = new FeedItem(FeedItem.Type.POST, post.id, entry.getEncodedEntryString());
                    FeedUpdateIq publishIq = new FeedUpdateIq(FeedUpdateIq.Action.PUBLISH, feedItem);
                    publishIq.setPostAudience(post.getAudienceType(), post.getAudienceList());
                    publishIq.setTo(connection.getXMPPServiceDomain());

                    connection.createStanzaCollectorAndSend(publishIq).nextResultOrThrow();
                } else {
                    final SimplePayload payload = new SimplePayload(entry.toXml());
                    final PubSubItem item = new PubSubItem(PubSubItem.PUB_SUB_ITEM_TYPE_FEED_POST, post.id, payload);
                    pubSubHelper.publishItem(getMyFeedNodeId(), item);
                    // the {@link PubSubHelper#publishItem(String, Item)} waits for IQ reply, so we can report the post was acked here
                }
                connectionObservers.notifyOutgoingPostSent(post.id);
            } catch (SmackException.NotConnectedException | InterruptedException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.e("connection: cannot send post", e);
            }
        });
    }

    public void retractPost(final @NonNull String postId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot retract post, no connection");
                return;
            }
            try {
                if (Constants.NEW_FEED_API) {
                    FeedUpdateIq requestIq = new FeedUpdateIq(FeedUpdateIq.Action.RETRACT, new FeedItem(FeedItem.Type.POST, postId, null));
                    requestIq.setTo((connection.getXMPPServiceDomain()));
                    connection.createStanzaCollectorAndSend(requestIq).nextResultOrThrow();
                } else {
                    final PublishedEntry entry = new PublishedEntry(
                            PublishedEntry.ENTRY_FEED,
                            null,
                            0,
                            connection.getUser().getLocalpart().toString(),
                            null,
                            null,
                            null);
                    final SimplePayload payload = new SimplePayload(entry.toXml());
                    final PubSubItem item = new PubSubItem(PubSubItem.PUB_SUB_ITEM_TYPE_FEED_POST, postId, payload);
                    pubSubHelper.retractItem(getMyFeedNodeId(), item);
                }
                // the {@link PubSubHelper#retractItem(String, Item)} waits for IQ reply, so we can report the post was acked here
                connectionObservers.notifyOutgoingPostSent(postId);
            } catch (SmackException.NotConnectedException | InterruptedException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.e("connection: cannot retract post", e);
            }
        });
    }

    public void sendComment(final @NonNull Comment comment) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot send comment, no connection");
                return;
            }
            try {
                final PublishedEntry entry = new PublishedEntry(
                        PublishedEntry.ENTRY_COMMENT,
                        null,
                        comment.timestamp,
                        connection.getUser().getLocalpart().toString(),
                        comment.text,
                        comment.postId,
                        comment.parentCommentId);
                for (Mention mention : comment.mentions) {
                    entry.mentions.add(Mention.toProto(mention));
                }
                if (Constants.NEW_FEED_API) {
                    UserId postSender = comment.postSenderUserId;
                    if (postSender.isMe()) {
                        postSender = new UserId(Preconditions.checkNotNull(connection).getUser().getLocalpart().toString());
                    }
                    FeedItem commentItem = new FeedItem(FeedItem.Type.COMMENT, comment.commentId, comment.postId, postSender, entry.getEncodedEntryString());
                    FeedUpdateIq requestIq = new FeedUpdateIq(FeedUpdateIq.Action.PUBLISH, commentItem);
                    requestIq.setTo(connection.getXMPPServiceDomain());
                    connection.createStanzaCollectorAndSend(requestIq).nextResultOrThrow();
                } else {
                    final SimplePayload payload = new SimplePayload(entry.toXml());
                    final PubSubItem item = new PubSubItem(PubSubItem.PUB_SUB_ITEM_TYPE_COMMENT, comment.commentId, payload);
                    pubSubHelper.publishItem(comment.postSenderUserId.isMe() ? getMyFeedNodeId() : getFeedNodeId(userIdToJid(comment.postSenderUserId)), item);
                }
                // the {@link PubSubHelper#publishItem(String, Item)} waits for IQ reply, so we can report the comment was acked here
                connectionObservers.notifyOutgoingCommentSent(comment.postSenderUserId, comment.postId, comment.commentId);
            } catch (SmackException.NotConnectedException | InterruptedException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.e("connection: cannot send comment", e);
            }
        });
    }

    public void retractComment(final @NonNull UserId postSenderUserId, final @NonNull String postId, final @NonNull String commentId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot retract comment, no connection");
                return;
            }
            try {
                if (Constants.NEW_FEED_API) {
                    UserId postSender = postSenderUserId;
                    if (postSender.isMe()) {
                        postSender = new UserId(Preconditions.checkNotNull(connection).getUser().getLocalpart().toString());
                    }
                    FeedItem commentItem = new FeedItem(FeedItem.Type.COMMENT, commentId, postId, postSender, null);
                    FeedUpdateIq requestIq = new FeedUpdateIq(FeedUpdateIq.Action.RETRACT, commentItem);
                    requestIq.setTo(connection.getXMPPServiceDomain());
                    connection.createStanzaCollectorAndSend(requestIq).nextResultOrThrow();
                } else {
                    final PublishedEntry entry = new PublishedEntry(
                            PublishedEntry.ENTRY_COMMENT,
                            null,
                            0,
                            connection.getUser().getLocalpart().toString(),
                            null,
                            postId,
                            null);
                    final SimplePayload payload = new SimplePayload(entry.toXml());
                    final PubSubItem item = new PubSubItem(PubSubItem.PUB_SUB_ITEM_TYPE_COMMENT, commentId, payload);
                    pubSubHelper.retractItem(postSenderUserId.isMe() ? getMyFeedNodeId() : getFeedNodeId(userIdToJid(postSenderUserId)), item);
                }
                // the {@link PubSubHelper#retractItem(String, Item)} waits for IQ reply, so we can report the comment was acked here
                connectionObservers.notifyOutgoingCommentSent(postSenderUserId, postId, commentId);
            } catch (SmackException.NotConnectedException | InterruptedException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.e("connection: cannot retract comment", e);
            }
        });
    }

    public void sendMessage(final @NonNull Message message, final @Nullable SessionSetupInfo sessionSetupInfo) {
        executor.execute(() -> {
            if (message.isLocalMessage()) {
                Log.i("connection: System message shouldn't be sent");
                return;
            }
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot send message, no connection");
                return;
            }
            final Jid recipientJid = JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked(message.chatId.rawId()), Domainpart.fromOrNull(XMPP_DOMAIN));
            final UserId recipientUserId = (UserId)message.chatId;
            try {
                final org.jivesoftware.smack.packet.Message xmppMessage = new org.jivesoftware.smack.packet.Message(recipientJid);
                xmppMessage.setStanzaId(message.id);
                xmppMessage.addExtension(new ChatMessageElement(
                        message,
                        recipientUserId,
                        sessionSetupInfo));
                ackHandlers.put(xmppMessage.getStanzaId(), () -> connectionObservers.notifyOutgoingMessageSent(message.chatId, message.id));
                Log.i("connection: sending message " + message.id + " to " + recipientJid);
                connection.sendStanza(xmppMessage);
            } catch (SmackException.NotConnectedException | InterruptedException e) {
                Log.e("connection: cannot send message", e);
            }
        });
    }

    public void sendGroupMessage(final @NonNull Message message, final @Nullable SessionSetupInfo sessionSetupInfo) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot send message, no connection");
                return;
            }
            try {
                final org.jivesoftware.smack.packet.Message xmppMessage = new org.jivesoftware.smack.packet.Message(JidCreate.bareFromOrNull(XMPP_DOMAIN));
                xmppMessage.setType(org.jivesoftware.smack.packet.Message.Type.groupchat);
                xmppMessage.setStanzaId(message.id);
                xmppMessage.addExtension(new GroupChatMessage((GroupId)message.chatId, message));
                ackHandlers.put(xmppMessage.getStanzaId(), () -> connectionObservers.notifyOutgoingMessageSent(message.chatId, message.id));
                Log.i("connection: sending group message " + message.id + " to " + message.chatId);
                connection.sendStanza(xmppMessage);
            } catch (SmackException.NotConnectedException | InterruptedException e) {
                Log.e("connection: cannot send group message", e);
            }
        });
    }

    public <T extends IQ> Observable<T> sendRequestIq(@NonNull IQ iq) {
        BackgroundObservable<T> iqResponse = new BackgroundObservable<>(bgWorkers);
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot send iq " + iq + ", no connection");
                iqResponse.setException(new SmackException.NotConnectedException());
                return;
            }
            iq.setTo(connection.getXMPPServiceDomain());
            SmackFuture<IQ, Exception> futureResponse = connection.sendIqRequestAsync(iq);
            futureResponse.onSuccess(resultIq -> {
                try {
                    iqResponse.setResponse((T) resultIq);
                } catch (ClassCastException e) {
                    iqResponse.setException(e);
                }
            });
            futureResponse.onError(iqResponse::setException);
        });
        return iqResponse;
    }

    public void sendRerequest(final String encodedIdentityKey, final @NonNull Jid originalSender, final @NonNull String messageId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot send message, no connection");
                return;
            }
            try {
                final org.jivesoftware.smack.packet.Message xmppMessage = new org.jivesoftware.smack.packet.Message(originalSender);
                xmppMessage.addExtension(new RerequestElement(messageId, encodedIdentityKey));
                Log.i("connection: sending rerequest for " + messageId + " to " + originalSender);
                connection.sendStanza(xmppMessage);
            } catch (SmackException.NotConnectedException | InterruptedException e) {
                Log.e("connection: cannot send message", e);
            }
        });
    }

    public void sendAck(final @NonNull String id) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot send ack, no connection");
                return;
            }
            try {
                final AckStanza ack = new AckStanza(connection.getXMPPServiceDomain(), id);
                Log.i("connection: sending ack for " + id);
                connection.sendStanza(ack);
            } catch (SmackException.NotConnectedException | InterruptedException e) {
                Log.e("connection: cannot send ack", e);
            }

        });
    }

    public void sendPostSeenReceipt(@NonNull UserId senderUserId, @NonNull String postId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot send seen receipt, no connection");
                return;
            }
            try {
                final Jid recipientJid = userIdToJid(senderUserId);
                final org.jivesoftware.smack.packet.Message message = new org.jivesoftware.smack.packet.Message(recipientJid);
                message.setStanzaId(RandomId.create());
                message.addExtension(new SeenReceiptElement(FEED_THREAD_ID, postId));
                ackHandlers.put(message.getStanzaId(), () -> connectionObservers.notifyIncomingPostSeenReceiptSent(senderUserId, postId));
                Log.i("connection: sending post seen receipt " + postId + " to " + recipientJid);
                connection.sendStanza(message);
            } catch (SmackException.NotConnectedException | InterruptedException e) {
                Log.e("connection: cannot send post seen receipt", e);
            }
        });
    }

    public void sendMessageSeenReceipt(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot send message seen receipt, no connection");
                return;
            }
            try {
                final Jid recipientJid = userIdToJid(senderUserId);
                final org.jivesoftware.smack.packet.Message message = new org.jivesoftware.smack.packet.Message(recipientJid);
                message.setStanzaId(RandomId.create());
                message.addExtension(new SeenReceiptElement(senderUserId.equals(chatId) ? null : chatId.rawId(), messageId));
                ackHandlers.put(message.getStanzaId(), () -> connectionObservers.notifyIncomingMessageSeenReceiptSent(chatId, senderUserId, messageId));
                Log.i("connection: sending message seen receipt " + messageId + " to " + recipientJid);
                connection.sendStanza(message);
            } catch (SmackException.NotConnectedException | InterruptedException e) {
                Log.e("connection: cannot send message seen receipt", e);
            }
        });
    }

    // TODO (ds): remove
    public Future<Pair<Collection<Post>, Collection<Comment>>> getFeedHistory() {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot retrieve feed history, no connection");
                return null;
            }
            try {
                final List<Subscription> subscriptions = pubSubHelper.getSubscriptions();

                final ArrayList<Post> historyPosts = new ArrayList<>();
                final Collection<Comment> historyComments = new ArrayList<>();
                for (Subscription subscription : subscriptions) {
                    if (subscription.getJid() == null) {
                        continue;
                    }
                    final String feedNodeId = subscription.getNode();
                    if (!isFeedNodeId(feedNodeId)) {
                        continue;
                    }
                    if (feedNodeId.equals(getMyFeedNodeId())) {
                        continue;
                    }
                    try {
                        parseFeedHistoryItems(getFeedUserId(feedNodeId), pubSubHelper.getItems(feedNodeId), historyPosts, historyComments);
                    } catch (XMPPException.XMPPErrorException e) {
                        Log.e("connection: retrieve feed history: no such node", e);
                    }
                }
                try {
                    parseFeedHistoryItems(UserId.ME, pubSubHelper.getItems(getMyFeedNodeId()), historyPosts, historyComments);
                } catch (XMPPException.XMPPErrorException e) {
                    Log.e("connection: retrieve feed history: no such node", e);
                }
                return Pair.create(historyPosts, historyComments);
            } catch (SmackException.NotConnectedException | SmackException.NoResponseException | InterruptedException | XMPPException.XMPPErrorException e) {
                Log.e("connection: cannot retrieve feed history", e);
                return null;
            }
        });
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

    private boolean processFeedPubSubItems(@NonNull FeedMessageElement element, @NonNull String ackId) {
        if (element.action == FeedMessageElement.Action.SHARE || element.action == FeedMessageElement.Action.PUBLISH) {
            final List<Post> posts = new ArrayList<>();
            final List<Comment> comments = new ArrayList<>();
            for (FeedItem item : element.feedItemList) {
                PublishedEntry entry = PublishedEntry.getFeedEntry(item);
                if (entry == null) {
                    continue;
                }
                final UserId senderUserId = getUserId(item.publisherId);
                if (senderUserId != null && !senderUserId.isMe()) {
                    if (entry.type == PublishedEntry.ENTRY_FEED) {
                        final Post post = new Post(0,
                                senderUserId,
                                entry.id,
                                entry.timestamp,
                                entry.media.isEmpty() ? Post.TRANSFERRED_YES : Post.TRANSFERRED_NO,
                                Post.SEEN_NO,
                                entry.text
                        );
                        for (PublishedEntry.Media entryMedia : entry.media) {
                            post.media.add(Media.createFromUrl(PublishedEntry.getMediaType(entryMedia.type), entryMedia.url,
                                    entryMedia.encKey, entryMedia.sha256hash,
                                    entryMedia.width, entryMedia.height));
                        }
                        for (com.halloapp.proto.Mention mentionProto : entry.mentions) {
                            Mention mention = Mention.parseFromProto(mentionProto);
                            processMention(mention);
                            post.mentions.add(mention);
                        }
                        posts.add(post);
                    } else if (entry.type == PublishedEntry.ENTRY_COMMENT) {
                        final Comment comment = new Comment(0,
                                getUserId(item.parentPostSenderId),
                                entry.feedItemId,
                                senderUserId,
                                entry.id,
                                entry.parentCommentId,
                                entry.timestamp,
                                true,
                                false,
                                entry.text
                        );
                        for (com.halloapp.proto.Mention mentionProto : entry.mentions) {
                            Mention mention = Mention.parseFromProto(mentionProto);
                            processMention(mention);
                            comment.mentions.add(mention);
                        }
                        comments.add(comment);
                    }
                }
            }

            final Map<UserId, String> names = new HashMap<>();
            for (FeedItem item : element.feedItemList) {
                if (item.type == FeedItem.Type.COMMENT) {
                    if (item.publisherId != null && item.publisherName != null) {
                        names.put(new UserId(item.publisherId), item.publisherName);
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
        } else if (element.action == FeedMessageElement.Action.RETRACT) {
            for (FeedItem item : element.feedItemList) {
                if (item.type == FeedItem.Type.COMMENT) {
                    connectionObservers.notifyCommentRetracted(item.id, getUserId(item.publisherId), item.parentPostId, getUserId(item.parentPostSenderId), item.timestamp);
                } else if (item.type == FeedItem.Type.POST) {
                    connectionObservers.notifyPostRetracted(getUserId(item.publisherId), item.id);
                }
            }
        }
        return false;
    }

    private boolean processFeedPubSubItems(@NonNull UserId feedUserId, @NonNull List<? extends NamedElement> items, @NonNull String ackId) {
        Preconditions.checkNotNull(connection);
        final List<PublishedEntry> entries = PublishedEntry.getEntries(items);
        final List<Post> posts = new ArrayList<>();
        final List<Comment> comments = new ArrayList<>();
        for (PublishedEntry entry : entries) {
            final UserId senderUserId = entry.user == null ? feedUserId : getUserId(entry.user);
            if (senderUserId != null && !senderUserId.isMe()) {
                if (entry.type == PublishedEntry.ENTRY_FEED) {
                    final Post post = new Post(0,
                            feedUserId,
                            entry.id,
                            entry.timestamp,
                            entry.media.isEmpty() ? Post.TRANSFERRED_YES : Post.TRANSFERRED_NO,
                            Post.SEEN_NO,
                            entry.text
                    );
                    for (PublishedEntry.Media entryMedia : entry.media) {
                        post.media.add(Media.createFromUrl(PublishedEntry.getMediaType(entryMedia.type), entryMedia.url,
                                entryMedia.encKey, entryMedia.sha256hash,
                                entryMedia.width, entryMedia.height));
                    }
                    for (com.halloapp.proto.Mention mentionProto : entry.mentions) {
                        Mention mention = Mention.parseFromProto(mentionProto);
                        processMention(mention);
                        post.mentions.add(mention);
                    }
                    posts.add(post);
                } else if (entry.type == PublishedEntry.ENTRY_COMMENT) {
                    final Comment comment = new Comment(0,
                            feedUserId,
                            entry.feedItemId,
                            senderUserId,
                            entry.id,
                            entry.parentCommentId,
                            entry.timestamp,
                            true,
                            false,
                            entry.text
                    );
                    for (com.halloapp.proto.Mention mentionProto : entry.mentions) {
                        Mention mention = Mention.parseFromProto(mentionProto);
                        processMention(mention);
                        comment.mentions.add(mention);
                    }
                    comments.add(comment);
                }
            }
        }

        final Map<UserId, String> names = new HashMap<>();
        for (NamedElement item : items) {
            if (item instanceof PubSubItem) {
                final PubSubItem pubSubItem = (PubSubItem) item;
                if (pubSubItem.getPublisher() != null && pubSubItem.getPublisherName() != null) {
                    names.put(new UserId(pubSubItem.getPublisher().getLocalpartOrNull().toString()), pubSubItem.getPublisherName());
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

    private void parseFeedHistoryItems(UserId feedUserId, List<PubSubItem> items, Collection<Post> posts, Collection<Comment> comments) {
        final List<PublishedEntry> entries = PublishedEntry.getEntries(items);
        for (PublishedEntry entry : entries) {
            if (entry.type == PublishedEntry.ENTRY_FEED) {
                final Post post = new Post(0,
                        getUserId(entry.user),
                        entry.id,
                        entry.timestamp,
                        (isMe(entry.user) || entry.media.isEmpty()) ? Post.TRANSFERRED_YES : Post.TRANSFERRED_NO,
                        Post.SEEN_YES,
                        entry.text
                );
                for (PublishedEntry.Media entryMedia : entry.media) {
                    post.media.add(Media.createFromUrl(PublishedEntry.getMediaType(entryMedia.type),
                            entryMedia.url, entryMedia.encKey, entryMedia.sha256hash,
                            entryMedia.width, entryMedia.height));
                }
                posts.add(post);
            } else if (entry.type == PublishedEntry.ENTRY_COMMENT) {
                final Comment comment = new Comment(0,
                        feedUserId,
                        entry.feedItemId,
                        getUserId(entry.user),
                        entry.id,
                        entry.parentCommentId,
                        entry.timestamp,
                        true,
                        true,
                        entry.text
                );
                comment.seen = true;
                comments.add(comment);
            }
        }
    }

    public UserId getUserId(@NonNull String user) {
        return isMe(user) ? UserId.ME : new UserId(user);
    }

    private UserId getUserId(@NonNull Jid jid) {
        return getUserId(jid.getLocalpartOrNull().toString());
    }

    private boolean isMe(@NonNull String user) {
        return user.equals(Preconditions.checkNotNull(connection).getUser().getLocalpart().toString());
    }

    private String getMyAvatarMetadataNodeId() {
        return getAvatarMetadataNodeId(Preconditions.checkNotNull(connection).getUser());
    }

    private String getMyFeedNodeId() {
        return getFeedNodeId(Preconditions.checkNotNull(connection).getUser());
    }

    private static String getAvatarMetadataNodeId(@NonNull Jid jid) {
        return getNodeId("metadata", jid);
    }

    private static String getFeedNodeId(@NonNull Jid jid) {
        return getNodeId("feed", jid);
    }

    private static boolean isFeedNodeId(@NonNull String nodeId) {
        return nodeId.startsWith("feed-");
    }

    private UserId getFeedUserId(@NonNull String nodeId) {
        Preconditions.checkArgument(isFeedNodeId(nodeId));
        return getUserId(nodeId.substring("feed-".length()));
    }

    private static String getNodeId(@NonNull String prefix, @NonNull Jid jid) {
        return prefix + "-" + jid.asEntityBareJidOrThrow().getLocalpart().toString();
    }

    private static Jid userIdToJid(@NonNull UserId userId) {
        return JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked(userId.rawId()), Domainpart.fromOrNull(XMPP_DOMAIN));
    }

    @Override
    public boolean getClientExpired() {
        return clientExpired;
    }

    class AckStanzaListener implements StanzaListener {

        @Override
        public void processStanza(final Stanza packet) {
            if (!(packet instanceof AckStanza)) {
                Log.w("connection: got packet instead of ack " + packet);
                return;
            }
            final AckStanza ack = (AckStanza) packet;
            Log.i("connection: got ack " + ack);
            final Runnable handler = ackHandlers.remove(ack.getStanzaId());
            if (handler != null) {
                handler.run();
            } else {
                Log.w("connection: ack doesn't match any pedning message " + ack);
            }
        }
    }

    class PresenceStanzaListener implements StanzaListener {

        @Override
        public void processStanza(final Stanza packet) {
            if (!(packet instanceof PresenceStanza)) {
                Log.w("connection: got packet instead of ack " + packet);
                return;
            }
            final PresenceStanza presence = (PresenceStanza) packet;
            Log.i("connection: got presence " + presence);
            connectionObservers.notifyPresenceReceived(getUserId(packet.getFrom()), ((PresenceStanza) packet).lastSeen);
        }
    }

    class ChatStateStanzaListener implements StanzaListener {

        @Override
        public void processStanza(final Stanza packet) {
            if (!(packet instanceof ChatStateStanza)) {
                Log.w("connection: got packet instead of ack " + packet);
                return;
            }
            final ChatStateStanza presence = (ChatStateStanza) packet;
            Log.i("connection: got presence " + presence);
            ChatState chatState = null;
            if ("chat".equals(presence.threadType)) {
                chatState = new ChatState(processChatStateType(presence.type), getUserId(presence.threadId));
            } else if ("group_chat".equals(presence.threadType)) {
                chatState = new ChatState(processChatStateType(presence.type), new GroupId(presence.threadId));
            }
            if (chatState != null) {
                UserId from = getUserId(packet.getFrom());
                if (from != null && !from.isMe()) {
                    connectionObservers.notifyChatStateReceived(getUserId(packet.getFrom()), chatState);
                }
            }
        }

        private @ChatState.Type int processChatStateType(String type) {
            return "typing".equals(type) ? ChatState.Type.TYPING : ChatState.Type.AVAILABLE;
        }
    }

    class MessageStanzaListener implements StanzaListener {

        @Override
        public void processStanza(final Stanza packet) {
            if (!(packet instanceof org.jivesoftware.smack.packet.Message)) {
                Log.w("connection: got packet instead of message " + packet);
                return;
            }
            boolean handled = false;
            final org.jivesoftware.smack.packet.Message msg = (org.jivesoftware.smack.packet.Message) packet;
            if (msg.getType() == org.jivesoftware.smack.packet.Message.Type.error) {
                Log.w("connection: got error message " + msg);
            } else {
                if (!Constants.NEW_FEED_API) {
                    final EventElement event = packet.getExtension(PubSubNamespace.event.name(), PubSubNamespace.event.getXmlns());
                    if (event != null && EventElementType.items.equals(event.getEventType())) {
                        final ItemsExtension itemsElem = (ItemsExtension) event.getEvent();
                        if (itemsElem != null) {
                            String node = itemsElem.getNode();
                            if (isFeedNodeId(node)) {
                                Log.i("connection: got feed pubsub " + msg);
                                handled = processFeedPubSubItems(getFeedUserId(node), itemsElem.getItems(), msg.getStanzaId());
                            }
                        }
                    }
                }
                if (!handled) {
                    final FeedMessageElement feedMessage = packet.getExtension(FeedMessageElement.ELEMENT, FeedMessageElement.NAMESPACE);
                    if (feedMessage != null) {
                        Log.i("connection: got feed item  " + msg);
                        handled = processFeedPubSubItems(feedMessage, msg.getStanzaId());
                    }
                }
                if (!handled) {
                    final ChatMessageElement chatMessage = packet.getExtension(ChatMessageElement.ELEMENT, ChatMessageElement.NAMESPACE);
                    if (chatMessage != null) {
                        Log.i("connection: got chat message " + msg);
                        Message parsedMessage = chatMessage.getMessage(packet.getFrom(), packet.getStanzaId());
                        processMentions(parsedMessage.mentions);
                        connectionObservers.notifyIncomingMessageReceived(parsedMessage);
                        handled = true;
                    }
                }
                if (!handled) {
                    final DeliveryReceiptElement deliveryReceipt = packet.getExtension(DeliveryReceiptElement.ELEMENT, DeliveryReceiptElement.NAMESPACE);
                    if (deliveryReceipt != null) {
                        Log.i("connection: got delivery receipt " + msg);
                        final String threadId = deliveryReceipt.getThreadId();
                        final UserId userId = getUserId(packet.getFrom());
                        connectionObservers.notifyOutgoingMessageDelivered(threadId == null ? userId : ChatId.fromString(threadId), userId, deliveryReceipt.getId(), deliveryReceipt.getTimestamp(), packet.getStanzaId());
                        handled = true;
                    }
                }
                if (!handled) {
                    final SeenReceiptElement seenReceipt = packet.getExtension(SeenReceiptElement.ELEMENT, SeenReceiptElement.NAMESPACE);
                    if (seenReceipt != null) {
                        Log.i("connection: got seen receipt " + msg);
                        final String threadId = seenReceipt.getThreadId();
                        final UserId userId = getUserId(packet.getFrom());
                        if (FEED_THREAD_ID.equals(threadId)) {
                            connectionObservers.notifyOutgoingPostSeen(userId, seenReceipt.getId(), seenReceipt.getTimestamp(), packet.getStanzaId());
                        } else {
                            connectionObservers.notifyOutgoingMessageSeen(threadId == null ? userId : ChatId.fromString(threadId), userId, seenReceipt.getId(), seenReceipt.getTimestamp(), packet.getStanzaId());
                        }
                        handled = true;
                    }
                }
                if (!handled) {
                    final ContactList contactList = packet.getExtension(ContactList.ELEMENT, ContactList.NAMESPACE);
                    if (contactList != null) {
                        Log.i("connection: got contact list " + msg + " size:" + contactList.contacts.size());
                        connectionObservers.notifyContactsChanged(contactList.contacts, contactList.contactHashes, packet.getStanzaId());
                        handled = true;
                    }
                }
                if (!handled) {
                    final WhisperKeysMessage whisperKeysMessage = packet.getExtension(WhisperKeysMessage.ELEMENT, WhisperKeysMessage.NAMESPACE);
                    if (whisperKeysMessage != null) {
                        Log.i("connection: got whisper keys message " + msg);
                        connectionObservers.notifyWhisperKeysMessage(whisperKeysMessage, packet.getStanzaId());
                        handled = true;
                    }
                }
                if (!handled) {
                    final AvatarChangeMessage avatarChangeMessage = packet.getExtension(AvatarChangeMessage.ELEMENT, AvatarChangeMessage.NAMESPACE);
                    if (avatarChangeMessage != null) {
                        Log.i("connection: got avatar change message " + msg);
                        connectionObservers.notifyAvatarChangeMessageReceived(avatarChangeMessage.userId, avatarChangeMessage.avatarId, packet.getStanzaId());
                        handled = true;
                    }
                }
                if (!handled) {
                    final GroupChangeMessage groupChangeMessage = packet.getExtension(GroupChangeMessage.ELEMENT, GroupChangeMessage.NAMESPACE);
                    if (groupChangeMessage != null) {
                        Log.i("connection: got group change message " + msg);
                        if (groupChangeMessage.sender != null && groupChangeMessage.senderName != null) {
                            connectionObservers.notifyUserNamesReceived(Collections.singletonMap(groupChangeMessage.sender, groupChangeMessage.senderName));
                        }
                        String ackId = packet.getStanzaId();
                        switch (groupChangeMessage.action) {
                            case GroupChangeMessage.Action.CREATE: {
                                connectionObservers.notifyGroupCreated(groupChangeMessage.groupId, groupChangeMessage.name, groupChangeMessage.avatarId, groupChangeMessage.members, groupChangeMessage.sender, groupChangeMessage.senderName, ackId);
                                break;
                            }
                            case GroupChangeMessage.Action.MODIFY_MEMBERS: {
                                connectionObservers.notifyGroupMemberChangeReceived(groupChangeMessage.groupId, groupChangeMessage.members, groupChangeMessage.sender, groupChangeMessage.senderName, ackId);
                                break;
                            }
                            case GroupChangeMessage.Action.LEAVE: {
                                connectionObservers.notifyGroupMemberLeftReceived(groupChangeMessage.groupId, groupChangeMessage.members, ackId);
                                break;
                            }
                            case GroupChangeMessage.Action.MODIFY_ADMINS: {
                                connectionObservers.notifyGroupAdminChangeReceived(groupChangeMessage.groupId, groupChangeMessage.members, groupChangeMessage.sender, groupChangeMessage.senderName, ackId);
                                break;
                            }
                            case GroupChangeMessage.Action.CHANGE_NAME: {
                                connectionObservers.notifyGroupNameChangeReceived(groupChangeMessage.groupId, groupChangeMessage.name, groupChangeMessage.sender, groupChangeMessage.senderName, ackId);
                                break;
                            }
                            case GroupChangeMessage.Action.CHANGE_AVATAR: {
                                connectionObservers.notifyGroupAvatarChangeReceived(groupChangeMessage.groupId, groupChangeMessage.avatarId, groupChangeMessage.sender, groupChangeMessage.senderName, ackId);
                                break;
                            }
                            case GroupChangeMessage.Action.AUTO_PROMOTE: {
                                connectionObservers.notifyGroupAdminAutoPromoteReceived(groupChangeMessage.groupId, groupChangeMessage.members, ackId);
                                break;
                            }
                            case GroupChangeMessage.Action.DELETE: {
                                connectionObservers.notifyGroupDeleteReceived(groupChangeMessage.groupId, groupChangeMessage.sender, groupChangeMessage.senderName, ackId);
                                break;
                            }
                            default: {
                                Log.w("connection: unrecognized group change action " + groupChangeMessage.action);
                            }
                        }
                        handled = true;
                    }
                }
                if (!handled) {
                    final GroupChatMessage groupChatMessage = packet.getExtension(GroupChatMessage.ELEMENT, GroupChatMessage.NAMESPACE);
                    if (groupChatMessage != null) {
                        Log.i("connection: got group chat message " + msg);
                        connectionObservers.notifyUserNamesReceived(Collections.singletonMap(groupChatMessage.sender, groupChatMessage.senderName));
                        Message parsedMessage = groupChatMessage.getMessage(packet.getFrom(), packet.getStanzaId());
                        processMentions(parsedMessage.mentions);
                        connectionObservers.notifyIncomingMessageReceived(parsedMessage);
                        handled = true;
                    }
                }
                if (!handled) {
                    final RerequestElement rerequest = packet.getExtension(RerequestElement.ELEMENT, RerequestElement.NAMESPACE);
                    if (rerequest != null) {
                        Log.i("connection: got rerequest message " + msg);
                        connectionObservers.notifyMessageRerequest(getUserId(packet.getFrom()), rerequest.id, packet.getStanzaId());
                        handled = true;
                    }
                }
            }
            if (!handled) {
                Log.i("connection: got unknown message " + msg);
                sendAck(msg.getStanzaId());
            }
        }
    }

    class HalloConnectionListener implements ConnectionListener {

        @Override
        public void connected(XMPPConnection connection) {
            Log.i("connection: onConnected");
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {
            Log.i("connection: onAuthenticated");
        }

        @Override
        public void connectionClosed() {
            Log.i("connection: onClosed");
            connectionObservers.notifyDisconnected();
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            Log.w("connection: onConnectedOnError", e);
            connectionObservers.notifyDisconnected();
        }
    }
}
