package com.halloapp.xmpp;

import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Preconditions;

import com.halloapp.BuildConfig;
import com.halloapp.Me;
import com.halloapp.contacts.UserId;
import com.halloapp.posts.Comment;
import com.halloapp.posts.Media;
import com.halloapp.posts.Post;
import com.halloapp.util.Log;

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
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.StanzaError;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.debugger.android.AndroidDebugger;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.Affiliation;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.EventElement;
import org.jivesoftware.smackx.pubsub.EventElementType;
import org.jivesoftware.smackx.pubsub.ItemReply;
import org.jivesoftware.smackx.pubsub.ItemsExtension;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.NotificationType;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubException;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.jivesoftware.smackx.pubsub.packet.PubSubNamespace;
import org.jivesoftware.smackx.pubsub.provider.AffiliationsProvider;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.xdata.FormField;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SuppressWarnings("WeakerAccess")
public class Connection {

    private static Connection instance;

    public static final String XMPP_DOMAIN = "s.halloapp.net";
    private static final String HOST = "s.halloapp.net";
    private static final int PORT = 5222;
    private static final int CONNECTION_TIMEOUT = 20_000;
    private static final int REPLY_TIMEOUT = 20_000;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private @Nullable XMPPTCPConnection connection;
    private Me me;
    private Observer observer;
    private Map<String, Runnable> ackHandlers = new ConcurrentHashMap<>();

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
        void onIncomingPostReceived(@NonNull Post post);
        void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId, long timestamp);
        void onOutgoingCommentSent(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull String commentId);
        void onIncomingCommentReceived(@NonNull Comment comment);
        void onSeenReceiptSent(@NonNull UserId senderUserId, @NonNull String postId);
        void onFeedHistoryReceived(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments);
        void onSubscribersChanged();
    }

    private Connection() {
        final HandlerThread handlerThread = new HandlerThread("ConnectionThread");
        handlerThread.start();
        SmackConfiguration.DEBUG = BuildConfig.DEBUG;
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
        if (!ackHandlers.isEmpty()) {
            Log.i("connection: " + ackHandlers.size() + " ack handlers cleared");
        }
        ackHandlers.clear();

        Log.i("connection: connecting...");

        ProviderManager.addExtensionProvider("affiliations", "http://jabber.org/protocol/pubsub#owner", new AffiliationsProvider()); // looks like a bug in smack -- this provider is not registered by default, so getAffiliationsAsOwner crashes with ClassCastException
        ProviderManager.addExtensionProvider("affiliation", "http://jabber.org/protocol/pubsub", new HalloAffiliationProvider()); // smack doesn't handle affiliation='publish-only' type
        ProviderManager.addExtensionProvider("item", "http://jabber.org/protocol/pubsub", new PubsubItemProvider()); // smack doesn't handle 'publisher' and 'timestamp' attributes
        ProviderManager.addExtensionProvider("item", "http://jabber.org/protocol/pubsub#event", new PubsubItemProvider()); // smack doesn't handle 'publisher' and 'timestamp' attributes
        ProviderManager.addExtensionProvider(SeenReceipt.ELEMENT, SeenReceipt.NAMESPACE, new SeenReceipt.Provider());
        ProviderManager.addIQProvider(ContactsSyncResponseIq.ELEMENT, ContactsSyncResponseIq.NAMESPACE, new ContactsSyncResponseIq.Provider());
        ProviderManager.addIQProvider(MediaUploadIq.ELEMENT, MediaUploadIq.NAMESPACE, new MediaUploadIq.Provider());

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

        Log.i("connection: configuring my nodes...");
        try {
            configureNode(getMyContactsNodeId(), (ItemEventListener<PayloadItem<SimplePayload>>) items -> {
                Log.i("connection: got " + items.getItems().size() + " items on my contacts node, " + items.getPublishedDate());
                observer.onSubscribersChanged();
            });
            configureNode(getMyFeedNodeId(), null);
        } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException | InterruptedException | PubSubException.NotAPubSubNodeException | ConfigureNodeException e) {
            Log.e("connection: cannot subscribe to pubsub", e);
            disconnectInBackground();
            return;
        }

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

    public Future<Boolean> syncPubSub(@NonNull final Collection<UserId> userIds) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: sync pubsub: no connection");
                return false;
            }
            try {
                final Collection<Jid> jids = new ArrayList<>(userIds.size());
                for (UserId userId : userIds) {
                    if (!isMe(userId.rawId())) {
                        jids.add(userIdToJid(userId));
                    }
                }
                syncAffiliations(jids, getMyContactsNodeId());
                syncAffiliations(jids, getMyFeedNodeId());
                syncSubscriptions(jids);
            } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException | InterruptedException | PubSubException.NotAPubSubNodeException e) {
                Log.e("connection: sync pubsub", e);
                return false;
            }
            return true;
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

    public Future<List<ContactsSyncResponseIq.Contact>> syncContacts(@NonNull final Collection<String> phones) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: sync contacts: no connection");
                return null;
            }
            final ContactsSyncRequestIq contactsSyncIq = new ContactsSyncRequestIq(connection.getXMPPServiceDomain(), phones);
            try {
                final ContactsSyncResponseIq response = connection.createStanzaCollectorAndSend(contactsSyncIq).nextResultOrThrow();
                return response.contactList;
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

    public void sendPost(final @NonNull Post post) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot send post, no connection");
                return;
            }
            final PubSubManager pubSubManager = PubSubManager.getInstance(connection);
            try {
                final LeafNode myFeedNode = pubSubManager.getNode(getMyFeedNodeId());
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
                myFeedNode.publish(item);
                // the LeafNode.publish waits for IQ reply, so we can report the post was acked here
                observer.onOutgoingPostSent(post.postId);
            } catch (SmackException.NotConnectedException | InterruptedException | PubSubException.NotAPubSubNodeException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.e("connection: cannot send post", e);
            }
        });
    }

    public void sendComment(final @NonNull Comment comment) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot send comment, no connection");
                return;
            }
            final PubSubManager pubSubManager = PubSubManager.getInstance(connection);
            try {
                final LeafNode feedNode = pubSubManager.getNode(
                        comment.postSenderUserId.isMe() ? getMyFeedNodeId() : getFeedNodeId(userIdToJid(comment.postSenderUserId)));
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
                feedNode.publish(item);
                // the LeafNode.publish waits for IQ reply, so we can report the post was acked here
                observer.onOutgoingCommentSent(comment.postSenderUserId, comment.postId, comment.commentId);
            } catch (SmackException.NotConnectedException | InterruptedException | PubSubException.NotAPubSubNodeException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.e("connection: cannot send comment", e);
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
                message.setStanzaId(postId);
                message.addExtension(new SeenReceipt(postId));
                ackHandlers.put(postId, () -> observer.onSeenReceiptSent(senderUserId, postId));
                Log.i("connection: sending post seen receipt " + postId + " to " + recipientJid);
                connection.sendStanza(message);
            } catch (SmackException.NotConnectedException | InterruptedException e) {
                Log.e("connection: cannot send post seen receipt", e);
            }
        });
    }

    private class ConfigureNodeException extends PubSubException {

        protected ConfigureNodeException(String nodeId) {
            super(nodeId);
        }
    }

    @WorkerThread
    private void configureNode(@NonNull String nodeId, @Nullable ItemEventListener listener) throws XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException, PubSubException.NotAPubSubNodeException, ConfigureNodeException {
        Preconditions.checkNotNull(connection);
        final PubSubManager pubSubManager = PubSubManager.getInstance(connection);
        Node node = null;
        try {
            node = pubSubManager.getNode(nodeId);
        } catch (XMPPException.XMPPErrorException e) {
            if (e.getStanzaError().getCondition() == StanzaError.Condition.item_not_found) {
                final ConfigureForm configureForm = new ConfigureForm(DataForm.Type.submit);
                configureForm.setAccessModel(AccessModel.whitelist);
                configureForm.setPublishModel(PublishModel.open);
                configureForm.setMaxItems(10);
                configureForm.setNotifyDelete(false);
                configureForm.setNotifyRetract(false);
                configureForm.setNotificationType(NotificationType.normal);
                configureForm.setItemReply(ItemReply.publisher);
                final FormField field = new FormField("pubsub#send_last_published_item");
                field.setType(FormField.Type.hidden);
                configureForm.addField(field);
                configureForm.setAnswer("pubsub#send_last_published_item", "never");
                node = pubSubManager.createNode(nodeId, configureForm);
            }
        }
        if (node == null) {
            Log.e("connection: cannot create node");
            throw new ConfigureNodeException(nodeId);
        }
        if (listener != null) {
            node.addItemEventListener(listener);
        }

        node.subscribe(connection.getUser().asBareJid().toString());
    }

    @WorkerThread
    private void syncAffiliations(@NonNull Collection<Jid> jids, @NonNull String nodeId) throws XMPPException.XMPPErrorException, PubSubException.NotAPubSubNodeException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
        Preconditions.checkNotNull(connection);
        final Jid selfJid = connection.getUser().asEntityBareJid();
        final PubSubManager pubSubManager = PubSubManager.getInstance(connection);

        final Node node = pubSubManager.getNode(nodeId);
        final List<Affiliation> currentAffiliations = node.getAffiliationsAsOwner();
        final List<Jid> currentJids  = new ArrayList<>();
        for (Affiliation affiliation : currentAffiliations) {
            if (affiliation.getJid() == null) {
                continue;
            }
            Jid jid = affiliation.getJid();
            if (jid.equals(selfJid)) {
                continue;
            }
            if (affiliation.getAffiliation() == Affiliation.Type.member) {
                currentJids.add(jid);
            }
        }

        final List<Jid> removeJids = new ArrayList<>(currentJids);
        removeJids.removeAll(jids);

        final List<Jid> addJids = new ArrayList<>(jids);
        addJids.removeAll(currentJids);

        final List<Affiliation> modifyAffiliations = new ArrayList<>();
        for (Jid jid : removeJids) {
            if (!jid.equals(selfJid)) {
                modifyAffiliations.add(new Affiliation(jid.asBareJid(), Affiliation.Type.none));
            }
        }
        for (Jid jid : addJids) {
            if (!jid.equals(selfJid)) {
                modifyAffiliations.add(new Affiliation(jid.asBareJid(), Affiliation.Type.member));
            }
        }
        if (!modifyAffiliations.isEmpty()) {
            node.modifyAffiliationAsOwner(modifyAffiliations);
        }
    }

    @WorkerThread
    private void syncSubscriptions(@NonNull Collection<Jid> jids) throws XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
        Preconditions.checkNotNull(connection);
        final Jid selfJid = connection.getUser().asEntityBareJid();
        final PubSubManager pubSubManager = PubSubManager.getInstance(connection);

        final List<Affiliation> allAffiliations = pubSubManager.getAffiliations();
        final Set<String> affiliatedFeedNodeIds = new HashSet<>();
        for (Affiliation affiliation : allAffiliations) {
            final String nodeId = affiliation.getNode();
            if (isFeedNodeId(nodeId)) {
                affiliatedFeedNodeIds.add(nodeId);
            }
        }

        final List<Subscription> subscriptions = pubSubManager.getSubscriptions();
        final List<String> addFeedNodeIds = new ArrayList<>(jids.size());
        final List<String> subscribedFeedNodeIds = new ArrayList<>(jids.size());
        for (Jid jid : jids) {
            final String feedNodeId = getFeedNodeId(jid);
            if (affiliatedFeedNodeIds.contains(feedNodeId)) {
                addFeedNodeIds.add(feedNodeId);
            }
        }
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
            if (!addFeedNodeIds.remove(feedNodeId)) {
                try {
                    final Node node = pubSubManager.getNode(feedNodeId);
                    node.unsubscribe(subscription.getJid().toString());
                } catch (PubSubException.NotAPubSubNodeException | XMPPException.XMPPErrorException e) {
                    Log.e("connection: sync pubsub: cannot unsubscribe, no such node", e);
                }
            } else {
                subscribedFeedNodeIds.add(feedNodeId);
            }
        }
        for (String addFeedNodeId : addFeedNodeIds) {
            try {
                final Node node = pubSubManager.getNode(addFeedNodeId);
                node.subscribe(selfJid.asBareJid().toString());
                if (!addFeedNodeId.equals(getMyFeedNodeId())){
                    subscribedFeedNodeIds.add(addFeedNodeId);
                }
            } catch (PubSubException.NotAPubSubNodeException | XMPPException.XMPPErrorException e) {
                Log.e("connection: sync pubsub: cannot subscribe, no such node", e);
                subscribedFeedNodeIds.remove(addFeedNodeId);
            }
        }

        // TODO (ds): make server send offline posts, should be no need to pull posts here
        final ArrayList<Post> historyPosts = new ArrayList<>();
        final Collection<Comment> historyComments = new ArrayList<>();
        for (String subscribedFeed : subscribedFeedNodeIds) {
            try {
                final LeafNode node = pubSubManager.getNode(subscribedFeed);
                parsePublishedHistoryItems(getFeedUserId(subscribedFeed), node.getItems(), historyPosts, historyComments);
            } catch (PubSubException.NotAPubSubNodeException e) {
                Log.e("connection: sync pubsub: no such node", e);
            }
        }
        try {
            final LeafNode node = pubSubManager.getNode(getMyFeedNodeId());
            parsePublishedHistoryItems(UserId.ME, node.getItems(), historyPosts, historyComments);
        } catch (PubSubException.NotAPubSubNodeException e) {
            Log.e("connection: sync pubsub: no such node", e);
        }
        observer.onFeedHistoryReceived(historyPosts, historyComments);
    }

    private void parsePublishedHistoryItems(UserId feedUserId, List<PubsubItem> items, Collection<Post> posts, Collection<Comment> comments) {
        final List<PublishedEntry> entries = PublishedEntry.getPublishedItems(items);
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

    private void processPublishedItems(UserId feedUserId, List<PubsubItem> items) {
        Preconditions.checkNotNull(connection);
        final List<PublishedEntry> entries = PublishedEntry.getPublishedItems(items);
        for (PublishedEntry entry : entries) {
            if (isMe(entry.user)) {
                sendAck(entry.id);
            } else {
                if (entry.type == PublishedEntry.ENTRY_FEED) {
                    final Post post = new Post(0,
                            getUserId(entry.user),
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
                    observer.onIncomingPostReceived(post);
                } else if (entry.type == PublishedEntry.ENTRY_COMMENT) {
                    final Comment comment = new Comment(0,
                            feedUserId,
                            entry.feedItemId,
                            getUserId(entry.user),
                            entry.id,
                            entry.parentCommentId,
                            entry.timestamp,
                            true,
                            false,
                            entry.text
                    );
                    observer.onIncomingCommentReceived(comment);
                }
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

    private String getMyFeedNodeId() {
        return getFeedNodeId(Preconditions.checkNotNull(connection).getUser());
    }

    private String getMyContactsNodeId() {
        return getContactsNodeId(Preconditions.checkNotNull(connection).getUser());
    }

    private static String getFeedNodeId(@NonNull Jid jid) {
        return getNodeId("feed", jid);
    }

    private static String getContactsNodeId(@NonNull Jid jid) {
        return getNodeId("contacts", jid);
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
                        //noinspection unchecked
                        processPublishedItems(getFeedUserId(itemsElem.getNode()), (List<PubsubItem>) itemsElem.getItems());
                        handled = true;
                    }
                }
                if (!handled) {
                    final SeenReceipt seenReceipt = packet.getExtension(SeenReceipt.ELEMENT, SeenReceipt.NAMESPACE);
                    if (seenReceipt != null) {
                        Log.i("connection: got seen receipt " + msg);
                        observer.onOutgoingPostSeen(getUserId(packet.getFrom()), seenReceipt.getId(), seenReceipt.getTimestamp());
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
