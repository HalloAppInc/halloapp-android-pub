package com.halloapp.xmpp;

import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;
import androidx.core.util.Preconditions;

import com.halloapp.BuildConfig;
import com.halloapp.Me;
import com.halloapp.contacts.UserId;
import com.halloapp.posts.Comment;
import com.halloapp.posts.Media;
import com.halloapp.posts.Post;
import com.halloapp.util.Log;
import com.halloapp.util.RandomId;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.NamedElement;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.debugger.android.AndroidDebugger;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.pubsub.EventElement;
import org.jivesoftware.smackx.pubsub.EventElementType;
import org.jivesoftware.smackx.pubsub.ItemsExtension;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.packet.PubSubNamespace;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SuppressWarnings("WeakerAccess")
public class Connection {

    private static Connection instance;

    public static final String XMPP_DOMAIN = "s.halloapp.net";
    public static final String HOST = "s.halloapp.net";
    private static final int PORT = 5222;
    private static final int CONNECTION_TIMEOUT = 20_000;
    private static final int REPLY_TIMEOUT = 20_000;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private @Nullable XMPPTCPConnection connection;
    private PubSubHelper pubSubHelper;
    private Me me;
    private Observer observer;
    private final Map<String, Runnable> ackHandlers = new ConcurrentHashMap<>();
    public boolean clientExpired = false;

    public static Connection getInstance() {
        if (instance == null) {
            synchronized(Connection.class) {
                if (instance == null) {
                    instance = new Connection();
                }
            }
        }
        return instance;
    }

    public interface Observer {
        void onConnected();
        void onDisconnected();
        void onLoginFailed();
        void onOutgoingPostSent(@NonNull String postId);
        void onIncomingFeedItemsReceived(@NonNull List<Post> posts, @NonNull List<Comment> comment, @NonNull String ackId);
        void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId, long timestamp, @NonNull String ackId);
        void onOutgoingCommentSent(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull String commentId);
        void onSeenReceiptSent(@NonNull UserId senderUserId, @NonNull String postId);
        void onContactsChanged(@NonNull List<ContactInfo> protocolContacts, @NonNull String ackId);
    }

    private Connection() {
        final HandlerThread handlerThread = new HandlerThread("ConnectionThread");
        handlerThread.start();
        SmackConfiguration.DEBUG = BuildConfig.DEBUG;
        Roster.setRosterLoadedAtLoginDefault(false);
    }

    public void setObserver(@NonNull Observer observer) {
        this.observer = observer;
    }

    public void connect(final @NonNull Me me) {
        executor.execute(() -> {
            this.me = me;
            connectInBackground();
        });
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

        ProviderManager.addExtensionProvider("item", "http://jabber.org/protocol/pubsub", new PubsubItemProvider()); // smack doesn't handle 'publisher' and 'timestamp' attributes
        ProviderManager.addExtensionProvider("item", "http://jabber.org/protocol/pubsub#event", new PubsubItemProvider()); // smack doesn't handle 'publisher' and 'timestamp' attributes
        ProviderManager.addExtensionProvider("retract", "http://jabber.org/protocol/pubsub", new PubsubItemProvider()); // smack doesn't handle 'publisher' and 'timestamp' attributes
        ProviderManager.addExtensionProvider("retract", "http://jabber.org/protocol/pubsub#event", new PubsubItemProvider()); // smack doesn't handle 'publisher' and 'timestamp' attributes
        ProviderManager.addExtensionProvider(SeenReceipt.ELEMENT, SeenReceipt.NAMESPACE, new SeenReceipt.Provider());
        ProviderManager.addExtensionProvider(ContactList.ELEMENT, ContactList.NAMESPACE, new ContactList.Provider());
        ProviderManager.addIQProvider(ContactsSyncResponseIq.ELEMENT, ContactsSyncResponseIq.NAMESPACE, new ContactsSyncResponseIq.Provider());
        ProviderManager.addIQProvider(MediaUploadIq.ELEMENT, MediaUploadIq.NAMESPACE, new MediaUploadIq.Provider());
        ProviderManager.addIQProvider(DaysToExpirationIq.ELEMENT, DaysToExpirationIq.NAMESPACE, new DaysToExpirationIq.Provider());

        try {
            final XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(me.getUser(), me.getPassword())
                    .setResource("android")
                    .setXmppDomain(XMPP_DOMAIN)
                    .setHost(HOST)
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
            connection = new XMPPTCPConnection(config);
            connection.setReplyTimeout(REPLY_TIMEOUT);
            connection.setUseStreamManagement(false);
            connection.addConnectionListener(new HalloConnectionListener());
        } catch (XmppStringprepException e) {
            Log.e("connection: cannot create connection", e);
            connection = null;
            return;
        }

        connection.addSyncStanzaListener(new MessageStanzaListener(), new StanzaTypeFilter(Message.class));
        connection.addSyncStanzaListener(new AckStanzaListener(), new StanzaTypeFilter(AckStanza.class));

        final ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(connection);
        if (!sdm.includesFeature(DeliveryReceipt.NAMESPACE)) {
            sdm.addFeature(DeliveryReceipt.NAMESPACE);
        }

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
                observer.onLoginFailed();
            }
            return;
        } catch (XMPPException | SmackException | IOException | InterruptedException e) {
            Log.e("connection: cannot login", e);
            disconnectInBackground();
            return;
        }

        pubSubHelper = new PubSubHelper(connection);

        try {
            connection.sendStanza(new Presence(Presence.Type.available));
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            Log.e("connection: cannot send presence", e);
            disconnectInBackground();
            return;
        }

        observer.onConnected();

        Log.i("connection: connected");
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
        observer.onDisconnected();
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

    public Future<Integer> requestDaysToExpiration() {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: request days to expiration: no connection");
                return null;
            }
            final DaysToExpirationIq daysToExpirationIq = new DaysToExpirationIq(connection.getXMPPServiceDomain());
            // TODO(jack): Remove this short-circuiting once server released to prod
            if (true) {
                return 20;
            }
            try {
                final DaysToExpirationIq response = connection.createStanzaCollectorAndSend(daysToExpirationIq).nextResultOrThrow();
                return response.daysLeft;
            } catch (SmackException.NotConnectedException | InterruptedException | XMPPException.XMPPErrorException | SmackException.NoResponseException e) {
                Log.e("connection: request days to expiration", e);
            }
            return null;
        });
    }

    public Future<MediaUploadIq.Urls> requestMediaUpload() {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: request media upload: no connection");
                return null;
            }
            final MediaUploadIq mediaUploadIq = new MediaUploadIq(connection.getXMPPServiceDomain());
            try {
                final MediaUploadIq response = connection.createStanzaCollectorAndSend(mediaUploadIq).nextResultOrThrow();
                return response.urls;
            } catch (SmackException.NotConnectedException | InterruptedException | XMPPException.XMPPErrorException | SmackException.NoResponseException e) {
                Log.e("connection: request media upload", e);
            }
            return null;
        });
    }

    public Future<List<ContactInfo>> syncContacts(@NonNull Collection<String> phones, @ContactSyncRequest.Type String type) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: sync contacts: no connection");
                return null;
            }
            final ContactsSyncRequestIq contactsSyncIq = new ContactsSyncRequestIq(connection.getXMPPServiceDomain(), phones, type);
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

    public void publishAvatarData(String id, String base64Data) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot update avatar data, no connection");
                return;
            }
            try {
                final PublishedAvatarData data = new PublishedAvatarData(base64Data);
                final SimplePayload payload = new SimplePayload(data.toXml());
                final PayloadItem<SimplePayload> item = new PayloadItem<>(id, payload);
                pubSubHelper.publishItem(getMyAvatarDataNodeId(), item);
                // the {@link PubSubHelper#publishItem(String, Item)} waits for IQ reply, so we can report the post was acked here

                // TODO(jack): Observer avatar update? (For all)
            } catch (SmackException.NotConnectedException | InterruptedException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.w("connection: cannot publish avatar data", e);
            }
        });
    }

    public Future<PubsubItem> getMyMostRecentAvatarData() {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot get avatar metadata, no connection");
                return null;
            }
            try {
                final List<PubsubItem> items = pubSubHelper.getItems(getMyAvatarDataNodeId(), 1);
                if (items.size() > 0) {
                    return items.get(0);
                }
            } catch (SmackException.NotConnectedException | InterruptedException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.w("connection: cannot get my avatar data", e);
            }
            return null;
        });
    }

    public Future<PubsubItem> getAvatarData(UserId userId, String itemId) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot get avatar metadata, no connection");
                return null;
            }
            try {
                // TODO(jack): Possible to make this me handling cleaner?
                List<String> itemIds = new ArrayList<>();
                itemIds.add(itemId);
                List<PubsubItem> items = pubSubHelper.getItems(userId.isMe() ? getMyAvatarDataNodeId() : getAvatarDataNodeId(userIdToJid(userId)), itemIds);
                if (items.size() > 0) {
                    return items.get(0);
                }
            } catch (SmackException.NotConnectedException | InterruptedException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.w("connection: cannot get avatar metadata", e);
            }
            return null;
        });
    }

    public void publishAvatarMetadata(String id) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot update avatar metadata, no connection");
                return;
            }
            try {
                final PublishedAvatarMetadata metadata = new PublishedAvatarMetadata(
                        id,
                        3000,
                        64, 64);
                final SimplePayload payload = new SimplePayload(metadata.toXml());
                final PayloadItem<SimplePayload> item = new PayloadItem<>(id, payload);
                pubSubHelper.publishItem(getMyAvatarMetadataNodeId(), item);
                // the {@link PubSubHelper#publishItem(String, Item)} waits for IQ reply, so we can report the post was acked here

                //observer.onOutgoingPostSent(post.postId);
            } catch (SmackException.NotConnectedException | InterruptedException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.w("connection: cannot update avatar metadata", e);
            }
        });
    }

    public Future<PubsubItem> getMyMostRecentAvatarMetadata() {
        return getMostRecentAvatarMetadata(UserId.ME);
    }

    public Future<PubsubItem> getMostRecentAvatarMetadata(UserId userId) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot get avatar metadata, no connection");
                return null;
            }
            try {
                List<PubsubItem> items = pubSubHelper.getItems(userId.isMe() ? getMyAvatarMetadataNodeId() : getAvatarMetadataNodeId(userIdToJid(userId)), 1);
                if (items.size() > 0) {
                    return items.get(0);
                }
            } catch (SmackException.NotConnectedException | InterruptedException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.w("connection: cannot get avatar metadata", e);
            }
            return null;
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
                    entry.media.add(new PublishedEntry.Media(getMediaType(media.type), media.url, media.encKey, media.sha256hash, media.width, media.height));
                }
                final SimplePayload payload = new SimplePayload(entry.toXml());
                final PayloadItem<SimplePayload> item = new PayloadItem<>(post.postId, payload);
                pubSubHelper.publishItem(getMyFeedNodeId(), item);
                // the {@link PubSubHelper#publishItem(String, Item)} waits for IQ reply, so we can report the post was acked here
                observer.onOutgoingPostSent(post.postId);
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
                final PublishedEntry entry = new PublishedEntry(
                        PublishedEntry.ENTRY_FEED,
                        null,
                        0,
                        connection.getUser().getLocalpart().toString(),
                        null,
                        null,
                        null);
                final SimplePayload payload = new SimplePayload(entry.toXml());
                final PayloadItem<SimplePayload> item = new PayloadItem<>(postId, payload);
                // TODO (ds): uncomment when server implements 'retract'
                pubSubHelper.retractItem(getMyFeedNodeId(), item);
                // the {@link PubSubHelper#retractItem(String, Item)} waits for IQ reply, so we can report the post was acked here
                observer.onOutgoingPostSent(postId);
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
                final SimplePayload payload = new SimplePayload(entry.toXml());
                final PayloadItem<SimplePayload> item = new PayloadItem<>(comment.commentId, payload);
                pubSubHelper.publishItem(comment.postSenderUserId.isMe() ? getMyFeedNodeId() : getFeedNodeId(userIdToJid(comment.postSenderUserId)), item);
                // the {@link PubSubHelper#publishItem(String, Item)} waits for IQ reply, so we can report the comment was acked here
                observer.onOutgoingCommentSent(comment.postSenderUserId, comment.postId, comment.commentId);
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
                final PublishedEntry entry = new PublishedEntry(
                        PublishedEntry.ENTRY_COMMENT,
                        null,
                        0,
                        connection.getUser().getLocalpart().toString(),
                        null,
                        postId,
                        null);
                final SimplePayload payload = new SimplePayload(entry.toXml());
                final PayloadItem<SimplePayload> item = new PayloadItem<>(commentId, payload);
                // TODO (ds): uncomment when server implements 'retract'
                pubSubHelper.retractItem(postSenderUserId.isMe() ? getMyFeedNodeId() : getFeedNodeId(userIdToJid(postSenderUserId)), item);
                // the {@link PubSubHelper#retractItem(String, Item)} waits for IQ reply, so we can report the comment was acked here
                observer.onOutgoingCommentSent(postSenderUserId, postId, commentId);
            } catch (SmackException.NotConnectedException | InterruptedException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.e("connection: cannot retract comment", e);
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

    public void sendSeenReceipt(@NonNull UserId senderUserId, @NonNull String postId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot send seen receipt, no connection");
                return;
            }
            try {
                final Jid recipientJid = userIdToJid(senderUserId);
                final Message message = new Message(recipientJid);
                message.setStanzaId(RandomId.create());
                message.addExtension(new SeenReceipt(postId));
                ackHandlers.put(message.getStanzaId(), () -> observer.onSeenReceiptSent(senderUserId, postId));
                Log.i("connection: sending post seen receipt " + postId + " to " + recipientJid);
                connection.sendStanza(message);
            } catch (SmackException.NotConnectedException | InterruptedException e) {
                Log.e("connection: cannot send post seen receipt", e);
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

    private boolean processFeedPubSubItems(@NonNull UserId feedUserId, @NonNull List<? extends NamedElement> items, @NonNull String ackId) {
        Preconditions.checkNotNull(connection);
        final List<PublishedEntry> entries = PublishedEntry.getFeedEntries(items);
        final List<Post> posts = new ArrayList<>();
        final List<Comment> comments = new ArrayList<>();
        for (PublishedEntry entry : entries) {
            final UserId senderUserId = entry.user == null ? feedUserId : getUserId(entry.user);
            if (!senderUserId.isMe()) {
                if (entry.type == PublishedEntry.ENTRY_FEED) {
                    final Post post = new Post(0,
                            feedUserId,
                            entry.id,
                            entry.timestamp,
                            entry.media.isEmpty(),
                            Post.POST_SEEN_NO,
                            entry.text
                    );
                    for (PublishedEntry.Media entryMedia : entry.media) {
                        post.media.add(Media.createFromUrl(getMediaType(entryMedia.type), entryMedia.url,
                                entryMedia.encKey, entryMedia.sha256hash,
                                entryMedia.width, entryMedia.height));
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
                    comments.add(comment);
                }
            }
        }
        if (!posts.isEmpty() || !comments.isEmpty()) {
            observer.onIncomingFeedItemsReceived(posts, comments, ackId);
        }
        return !posts.isEmpty() || !comments.isEmpty();
    }

    private void parseFeedHistoryItems(UserId feedUserId, List<PubsubItem> items, Collection<Post> posts, Collection<Comment> comments) {
        final List<PublishedEntry> entries = PublishedEntry.getFeedEntries(items);
        for (PublishedEntry entry : entries) {
            if (entry.type == PublishedEntry.ENTRY_FEED) {
                final Post post = new Post(0,
                        getUserId(entry.user),
                        entry.id,
                        entry.timestamp,
                        isMe(entry.user) || entry.media.isEmpty(),
                        Post.POST_SEEN_YES,
                        entry.text
                );
                for (PublishedEntry.Media entryMedia : entry.media) {
                    post.media.add(Media.createFromUrl(getMediaType(entryMedia.type),
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

    private UserId getUserId(@NonNull String user) {
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

    private String getMyAvatarDataNodeId() {
        return getAvatarDataNodeId(Preconditions.checkNotNull(connection).getUser());
    }

    private String getMyFeedNodeId() {
        return getFeedNodeId(Preconditions.checkNotNull(connection).getUser());
    }

    private static String getAvatarMetadataNodeId(@NonNull Jid jid) {
        return getNodeId("avatar-metadata", jid);
    }

    private static String getAvatarDataNodeId(@NonNull Jid jid) {
        return getNodeId("avatar-data", jid);
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

    private static @Media.MediaType int getMediaType(@PublishedEntry.Media.MediaType String protocolMediaType) {
        switch (protocolMediaType) {
            case PublishedEntry.Media.MEDIA_TYPE_IMAGE: {
                return Media.MEDIA_TYPE_IMAGE;
            }
            case PublishedEntry.Media.MEDIA_TYPE_VIDEO: {
                return Media.MEDIA_TYPE_VIDEO;
            }
            default: {
                return Media.MEDIA_TYPE_UNKNOWN;
            }
        }
    }

    private static @PublishedEntry.Media.MediaType String getMediaType(@Media.MediaType int mediaType) {
        switch (mediaType) {
            case Media.MEDIA_TYPE_IMAGE: {
                return PublishedEntry.Media.MEDIA_TYPE_IMAGE;
            }
            case Media.MEDIA_TYPE_VIDEO: {
                return PublishedEntry.Media.MEDIA_TYPE_VIDEO;
            }
            case Media.MEDIA_TYPE_UNKNOWN:
            default: {
                throw new IllegalArgumentException();
            }
        }
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

    class MessageStanzaListener implements StanzaListener {

        @Override
        public void processStanza(final Stanza packet) {
            if (!(packet instanceof Message)) {
                Log.w("connection: got packet instead of message " + packet);
                return;
            }
            boolean handled = false;
            final Message msg = (Message) packet;
            if (msg.getType() == Message.Type.error) {
                Log.w("connection: got error message " + msg);
            } else {
                final EventElement event = packet.getExtension("event", PubSubNamespace.event.getXmlns());
                if (event != null && EventElementType.items.equals(event.getEventType())) {
                    final ItemsExtension itemsElem = (ItemsExtension) event.getEvent();
                    if (itemsElem != null && isFeedNodeId(itemsElem.getNode())) {
                        Log.i("connection: got pubsub " + msg);
                        handled = processFeedPubSubItems(getFeedUserId(itemsElem.getNode()), itemsElem.getItems(), msg.getStanzaId());
                    }
                }
                if (!handled) {
                    final SeenReceipt seenReceipt = packet.getExtension(SeenReceipt.ELEMENT, SeenReceipt.NAMESPACE);
                    if (seenReceipt != null) {
                        Log.i("connection: got seen receipt " + msg);
                        observer.onOutgoingPostSeen(getUserId(packet.getFrom()), seenReceipt.getId(), seenReceipt.getTimestamp(), packet.getStanzaId());
                        handled = true;
                    }
                }
                if (!handled) {
                    final ContactList contactList = packet.getExtension(ContactList.ELEMENT, ContactList.NAMESPACE);
                    if (contactList != null) {
                        Log.i("connection: got contact list " + msg + " size:" + contactList.contacts.size());
                        observer.onContactsChanged(contactList.contacts, packet.getStanzaId());
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
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            Log.w("connection: onConnectedOnError", e);
        }
    }
}
