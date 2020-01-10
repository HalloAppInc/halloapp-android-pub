package com.halloapp;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Preconditions;

import com.halloapp.posts.Post;
import com.halloapp.protocol.PublishedEntry;
import com.halloapp.util.Log;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.debugger.SmackDebugger;
import org.jivesoftware.smack.debugger.SmackDebuggerFactory;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.StanzaError;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.ParserUtils;
import org.jivesoftware.smackx.debugger.android.AndroidDebugger;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.Affiliation;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
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
import org.jivesoftware.smackx.pubsub.provider.AffiliationProvider;
import org.jivesoftware.smackx.pubsub.provider.AffiliationsProvider;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.jivesoftware.smackx.xdata.FormField;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public class Connection {

    private static Connection instance;

    public static final String XMPP_DOMAIN = "s.halloapp.net";
    private static final String HOST = "s.halloapp.net";
    private static final int PORT = 5222;
    private static final int CONNECTION_TIMEOUT = 20_000;
    private static final int REPLY_TIMEOUT = 220_000; //testing-only

    public static final Jid FEED_JID = JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked("feed"), Domainpart.fromOrNull(XMPP_DOMAIN));

    private final Handler handler;
    private @Nullable XMPPTCPConnection connection;
    private final Observer observer;

    public static Connection getInstance(@NonNull Observer observer) {
        if (instance == null) {
            synchronized(Connection.class) {
                if (instance == null) {
                    instance = new Connection(observer);
                }
            }
        }
        return instance;
    }

    public interface Observer {
        void onConnected();
        void onDisconnected();
        void onLoginFailed();
        void onOutgoingPostAcked(@NonNull String chatJid, @NonNull String postId);
        void onOutgoingPostDelivered(@NonNull String chatJid, @NonNull String postId);
        void onIncomingPostReceived(@NonNull Post post);
    }

    private Connection(@NonNull Observer observer) {
        this.observer = observer;
        final HandlerThread handlerThread = new HandlerThread("ConnectionThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        SmackConfiguration.DEBUG = BuildConfig.DEBUG;
    }

    public void connect(final @NonNull String user, final @NonNull String password) {
        handler.post(() -> {
            if (connection != null && connection.isConnected() && connection.isAuthenticated()) {
                Log.i("connection: already connected");
                return;
            }

            Log.i("connection: connecting...");

            ProviderManager.addExtensionProvider("affiliations", "http://jabber.org/protocol/pubsub#owner", new AffiliationsProvider()); // looks like a bug in smack -- this provider is not registered by default, so getAffiliationsAsOwner crashes with ClassCastException
            ProviderManager.addExtensionProvider("affiliation", "http://jabber.org/protocol/pubsub", new HalloAffiliationProvider()); // smack doesn't handle affiliation='publish-only' type

            try {
                final XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                        .setUsernameAndPassword(user, password)
                        .setResource("android")
                        .setXmppDomain(XMPP_DOMAIN)
                        .setHost(HOST)
                        .setConnectTimeout(CONNECTION_TIMEOUT)
                        .setSendPresence(false)
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                        .setPort(PORT)
                        .setDebuggerFactory(new SmackDebuggerFactory() {
                            @Override
                            public SmackDebugger create(XMPPConnection connection) throws IllegalArgumentException {
                                return new AndroidDebugger(connection) {
                                    @Override
                                    protected void log(String logMessage) {
                                        Log.d("connection: " + logMessage);
                                    }

                                    @Override
                                    protected void log(String logMessage, Throwable throwable) {
                                        Log.w("connection: " + logMessage, throwable);
                                    }
                                };
                            }
                        })
                        .build();
                connection = new XMPPTCPConnection(config);
                connection.setReplyTimeout(REPLY_TIMEOUT);
                connection.addConnectionListener(new HalloConnectionListener());
            } catch (XmppStringprepException e) {
                Log.e("connection: cannot create connection", e);
                connection = null;
                return;
            }

            final ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(connection);
            if (!sdm.includesFeature(DeliveryReceipt.NAMESPACE)) {
                sdm.addFeature(DeliveryReceipt.NAMESPACE);
            }

            try {
                connection.connect();
            } catch (XMPPException | SmackException | IOException | InterruptedException e) {
                Log.e("connection: cannot connect", e);
                disconnectInBackground();
                return;
            }

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

            final DeliveryReceiptManager deliveryReceiptManager = DeliveryReceiptManager.getInstanceFor(connection);
            deliveryReceiptManager.addReceiptReceivedListener(new MessageReceiptsListener());
            connection.addSyncStanzaListener(new MessagePacketListener(), new StanzaTypeFilter(Message.class));
            connection.addStanzaAcknowledgedListener(new MessageAckListener());

            try {
                configureNode(getMyContactsNodeId(), (ItemEventListener<PayloadItem<SimplePayload>>) items -> {
                    Log.i("connection: got " + items.getItems().size() + " items on my contacts node, " + items.getPublishedDate());
                    // TODO (ds): refresh
                });
                configureNode(getMyFeedNodeId(), (ItemEventListener<PayloadItem<SimplePayload>>) items -> {
                    Log.i("connection: got " + items.getItems().size() + " items on my feed node, " + items.getPublishedDate());
                    processPublishedItems(items.getItems());
                });
            } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException | InterruptedException | PubSubException.NotAPubSubNodeException | ConfigureNodeException e) {
                Log.e("connection: cannot subscribe to pubsub", e);
                disconnectInBackground();
                return;
            }

            observer.onConnected();

            Log.i("connection: connected");
        });
    }

    public void disconnect() {
        handler.post(this::disconnectInBackground);
    }

    @WorkerThread
    private void disconnectInBackground() {
        if (connection == null) {
            Log.e("connection: cannot disconnect, no connection");
            return;
        }
        connection.disconnect();
        connection = null;
        observer.onDisconnected();
    }

    public void syncPubSub(@NonNull final List<Jid> jids) {
        handler.post(() -> {
            if (connection == null) {
                Log.e("connection: sync pubsub: no connection");
                return;
            }
            try {
                syncAffiliations(jids, getMyContactsNodeId());
                syncAffiliations(jids, getMyFeedNodeId());
                syncSubscriptions(jids);

                try {
                    connection.sendStanza(new Presence(Presence.Type.available));
                } catch (SmackException.NotConnectedException | InterruptedException e) {
                    Log.e("connection: cannot send presence", e);
                }

            } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException | InterruptedException | PubSubException.NotAPubSubNodeException e) {
                Log.e("connection: sync pubsub", e);
            } catch (XmppStringprepException e) {
                Log.e("connection: sync pubsub: invalid jid", e);
            }
        });
    }

    public void sendPost(final @NonNull Post post) {
        handler.post(() -> {
            if (connection == null) {
                Log.e("connection: cannot send message, no connection");
                return;
            }
            if (post.chatJid.startsWith(FEED_JID.getLocalpartOrThrow().toString())) {
                final PubSubManager pubSubManager = PubSubManager.getInstance(connection);
                try {
                    final LeafNode myFeedNode = pubSubManager.getNode(getMyFeedNodeId());
                    final SimplePayload payload = new SimplePayload(new PublishedEntry(PublishedEntry.ENTRY_FEED, null, post.timestamp, connection.getUser().getLocalpart().toString(), post.text, post.url, null).toXml());
                    final PayloadItem<SimplePayload> item = new PayloadItem<>(post.postId, payload);
                    myFeedNode.publish(item);
                } catch (SmackException.NotConnectedException | InterruptedException | PubSubException.NotAPubSubNodeException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                    Log.e("connection: cannot send message", e);
                }
            } else {
                try {
                    final Message message = new Message(post.chatJid, post.text);
                    message.setStanzaId(post.postId);
                    message.addExtension(new DeliveryReceiptRequest());
                    Log.i("connection: sending message " + post.postId + " to " + post.chatJid);
                    connection.sendStanza(message);
                } catch (XmppStringprepException | SmackException.NotConnectedException | InterruptedException e) {
                    Log.e("connection: cannot send message", e);
                }
            }
        });
    }

    public void sendDeliveryReceipt(final @NonNull Post post) {
        handler.post(() -> {
            if (connection == null) {
                Log.e("connection: cannot send message, no connection");
                return;
            }
            try {
                final Message message = new Message(JidCreate.from(post.senderJid));
                message.setStanzaId(post.postId);
                message.addExtension(new DeliveryReceipt(post.postId));
                Log.i("connection: sending delivery receipt " + post.postId + " to " + post.chatJid);
                connection.sendStanza(message);
            } catch (XmppStringprepException | SmackException.NotConnectedException | InterruptedException e) {
                Log.e("connection: cannot send message", e);
            }

        });
    }

    private class ConfigureNodeException extends PubSubException {

        protected ConfigureNodeException(String nodeId) {
            super(nodeId);
        }
    }

    @WorkerThread
    private void configureNode(@NonNull String nodeId, @NonNull ItemEventListener listener) throws XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException, PubSubException.NotAPubSubNodeException, ConfigureNodeException {
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
        node.addItemEventListener(listener);

        node.subscribe(connection.getUser().asBareJid().toString());

    }

    @WorkerThread
    private void syncAffiliations(@NonNull List<Jid> jids, @NonNull String nodeId) throws XMPPException.XMPPErrorException, PubSubException.NotAPubSubNodeException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException, XmppStringprepException {
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
                modifyAffiliations.add(new Affiliation(JidCreate.bareFrom(jid), Affiliation.Type.none));
            }
        }
        for (Jid jid : addJids) {
            if (!jid.equals(selfJid)) {
                modifyAffiliations.add(new Affiliation(JidCreate.bareFrom(jid), Affiliation.Type.member));
            }
        }
        if (!modifyAffiliations.isEmpty()) {
            node.modifyAffiliationAsOwner(modifyAffiliations);
        }
    }

    @WorkerThread
    private void syncSubscriptions(@NonNull List<Jid> jids) throws XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
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
        for (String subscribedFeed : subscribedFeedNodeIds) {
            final LeafNode node;
            try {
                node = pubSubManager.getNode(subscribedFeed);
                node.addItemEventListener((ItemEventListener<PayloadItem<SimplePayload>>) items -> {
                    Log.i("connection: got " + items.getItems().size() + " items on " + subscribedFeed + " feed, " + items.getPublishedDate());
                    processPublishedItems(items.getItems());
                });
                processPublishedItems(node.getItems()); // TODO (ds): make server send offline posts, should be no need to pull posts here
            } catch (PubSubException.NotAPubSubNodeException e) {
                Log.e("connection: sync pubsub: cannot listen, no such node", e);
            }
        }
    }

    private void processPublishedItems(List<PayloadItem<SimplePayload>> items) {
        Preconditions.checkNotNull(connection);
        final List<PublishedEntry> entries = PublishedEntry.getPublishedItems(items);
        for (PublishedEntry entry : entries) {
            if (entry.user.equals(connection.getUser().getLocalpart().toString())) {
                observer.onOutgoingPostAcked(FEED_JID.toString(), entry.id);
            } else {
                if (entry.type == PublishedEntry.ENTRY_FEED) {
                    Post post = new Post(0,
                            FEED_JID.toString(),
                            JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked(entry.user), Domainpart.fromOrThrowUnchecked(XMPP_DOMAIN)).toString(),
                            entry.id,
                            null,
                            0,
                            entry.timestamp,
                            Post.POST_STATE_INCOMING_PREPARING,
                            TextUtils.isEmpty(entry.url) ? Post.POST_TYPE_TEXT : Post.POST_TYPE_IMAGE,
                            entry.text,
                            entry.url,
                            null,
                            0,
                            0
                    );
                    observer.onIncomingPostReceived(post);
                } else {
                    // TODO (ds): process comments
                    Log.i("connection: comment received");
                }
            }
        }

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

    private static String getNodeId(@NonNull String prefix, @NonNull Jid jid) {
        return prefix + "-" + jid.asEntityBareJidOrThrow().getLocalpart().toString();
    }

    class MessagePacketListener implements StanzaListener {

        @Override
        public void processStanza(final Stanza packet) {
            final Message msg = (Message) packet;
            if (msg.getBodies().size() > 0 && !Message.Type.error.equals(msg.getType())) {
                Log.i("connection: got message " + msg);
                final Post post = new Post(0,
                        packet.getFrom().asBareJid().toString(),
                        packet.getFrom().asBareJid().toString(),
                        packet.getStanzaId(),
                        "",
                        0,
                        System.currentTimeMillis(), /*TODO (ds): use actual time*/
                        Post.POST_STATE_INCOMING_PREPARING,
                        Post.POST_TYPE_TEXT,
                        msg.getBody(),
                        null,
                        null,
                        0,
                        0);
                observer.onIncomingPostReceived(post);
            } else {
                //This must be sth like delivery receipt or Chat state msg
                Log.i("connection: got message with empty body or error " + msg);
            }
        }
    }

    class MessageAckListener implements StanzaListener {

        @Override
        public void processStanza(Stanza packet) {
            if (packet instanceof Message) {
                observer.onOutgoingPostAcked(packet.getTo().toString(), packet.getStanzaId());
                Log.i("connection: post " + packet.getStanzaId() + " acked");
            } else {
                Log.i("connection: stanza " + packet.getStanzaId() + " acked");
            }
        }
    }

    class MessageReceiptsListener implements ReceiptReceivedListener {

        @Override
        public void onReceiptReceived(Jid fromJid, Jid toJid, String receiptId, Stanza receipt) {
            Log.i("connection: delivered to:" + toJid + ", from:" + fromJid + " , id:" + receiptId);
            observer.onOutgoingPostDelivered(fromJid.asBareJid().toString(), receiptId);
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

    // smack doesn't handle affiliation='publish-only' type
    public class HalloAffiliationProvider extends AffiliationProvider {

        @Override
        public Affiliation parse(XmlPullParser parser, int initialDepth)
                throws Exception {
            String node = parser.getAttributeValue(null, "node");
            BareJid jid = ParserUtils.getBareJidAttribute(parser);
            String namespaceString = parser.getNamespace();
            Affiliation.AffiliationNamespace namespace = Affiliation.AffiliationNamespace.fromXmlns(namespaceString);

            String affiliationString = parser.getAttributeValue(null, "affiliation");
            Affiliation.Type affiliationType = null;
            if (affiliationString != null && !"publish-only".equals(affiliationString)) {
                affiliationType = Affiliation.Type.valueOf(affiliationString);
            }
            Affiliation affiliation;
            if (node != null && jid == null) {
                // affiliationType may be empty
                affiliation = new Affiliation(node, affiliationType, namespace);
            }
            else if (node == null && jid != null) {
                affiliation = new Affiliation(jid, affiliationType, namespace);
            }
            else {
                throw new SmackException("Invalid affililation. Either one of 'node' or 'jid' must be set"
                        + ". Node: " + node
                        + ". Jid: " + jid
                        + '.');
            }
            return affiliation;
        }

    }
}
