package com.halloapp;

import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Preconditions;

import com.halloapp.posts.Post;
import com.halloapp.protocol.PublishedEntry;
import com.halloapp.util.Log;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.StanzaError;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.Affiliation;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubException;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.jivesoftware.smackx.pubsub.provider.AffiliationsProvider;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        void onOutgoingPostAcked(@NonNull String chatJid, @NonNull String postId);
        void onOutgoingPostDelivered(@NonNull String chatJid, @NonNull String postId);
        void onIncomingPostReceived(@NonNull Post post);
    }

    private Connection(@NonNull Observer observer) {
        this.observer = observer;
        final HandlerThread handlerThread = new HandlerThread("ConnectionThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    void connect(final @NonNull String user, final @NonNull String password) {
        handler.post(() -> {

            ProviderManager.addExtensionProvider("affiliations", "http://jabber.org/protocol/pubsub#owner", new AffiliationsProvider()); // looks like a bug in smack -- this provider is not registered by default, so getAffiliationsAsOwner crashes with ClassCastException

            try {
                final XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                        .setUsernameAndPassword(user, password)
                        .setResource("android")
                        .setXmppDomain(XMPP_DOMAIN)
                        .setHost(HOST)
                        .setConnectTimeout(CONNECTION_TIMEOUT)
                        .setSendPresence(true)
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                        .setPort(PORT)
                        .build();
                connection = new XMPPTCPConnection(config);
                connection.setReplyTimeout(REPLY_TIMEOUT);
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
                connection.login();
            } catch (XMPPException | SmackException | IOException | InterruptedException e) {
                Log.e("connection: cannot connect", e);
                disconnect();
                return;
            }

            final DeliveryReceiptManager deliveryReceiptManager = DeliveryReceiptManager.getInstanceFor(connection);
            deliveryReceiptManager.addReceiptReceivedListener(new MessageReceiptsListener());
            connection.addSyncStanzaListener(new MessagePacketListener(), new StanzaTypeFilter(Message.class));
            connection.addStanzaAcknowledgedListener(new MessageAckListener());

            final PubSubManager pubSubManager = PubSubManager.getInstance(connection);
            Node node = null;
            try {
                try {
                    node = pubSubManager.getNode("feed-" + user);
                } catch (XMPPException.XMPPErrorException e) {
                    if (e.getStanzaError().getCondition() == StanzaError.Condition.item_not_found) {
                        final ConfigureForm configureForm = new ConfigureForm(DataForm.Type.submit);
                        configureForm.setAccessModel(AccessModel.whitelist);
                        configureForm.setPublishModel(PublishModel.open);
                        configureForm.setMaxItems(10);
                        node = pubSubManager.createNode("feed-" + user, configureForm);
                    }
                }
                if (node == null) {
                    Log.e("connection: cannot create node");
                    disconnect();
                    return;
                }
                node.addItemEventListener((ItemEventListener<PayloadItem<SimplePayload>>) items -> {
                    Log.i("connection: got " + items.getItems().size() + " items on my feed, " + items.getPublishedDate());
                    processPublishedItems(items);
                });

                node.subscribe(connection.getUser().asBareJid().toString());

            } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException | InterruptedException | PubSubException.NotAPubSubNodeException e) {
                Log.e("connection: cannot subscribe to pubsub", e);
                disconnect();
            }
        });
    }

    void disconnect() {
        handler.post(() -> {
            if (connection == null) {
                Log.e("connection: cannot disconnect, no connection");
                return;
            }
            connection.disconnect();
            connection = null;
        });
    }

    public void syncPubSub(@NonNull final List<Jid> jids) {
        handler.post(() -> {
            if (connection == null) {
                Log.e("connection: sync pubsub: no connection");
                return;
            }
            final Jid selfJid = connection.getUser().asEntityBareJid();

            final PubSubManager pubSubManager = PubSubManager.getInstance(connection);

            try {
                final Node myFeedNode = pubSubManager.getNode(getMyFeedId());
                final List<Affiliation> currentAffiliations = myFeedNode.getAffiliationsAsOwner();
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
                    myFeedNode.modifyAffiliationAsOwner(modifyAffiliations);
                }
            } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException | InterruptedException | PubSubException.NotAPubSubNodeException e) {
                Log.e("connection: sync pubsub", e);
            } catch (XmppStringprepException e) {
                Log.e("connection: sync pubsub: invalid jid", e);
            }

            try {
                final List<Subscription> subscriptions = pubSubManager.getSubscriptions();
                final List<String> addFeeds = new ArrayList<>(jids.size());
                final List<String> subscribedFeeds = new ArrayList<>(jids.size());
                for (Jid jid : jids) {
                    addFeeds.add(getFeedId(jid));
                }
                for (Subscription subscription : subscriptions) {
                    if (subscription.getJid() == null) {
                        continue;
                    }
                    final String feed = subscription.getNode();
                    if (!addFeeds.remove(feed)) {
                        try {
                            final Node node = pubSubManager.getNode(feed);
                            node.unsubscribe(subscription.getJid().toString());
                        } catch (PubSubException.NotAPubSubNodeException | XMPPException.XMPPErrorException e) {
                            Log.e("connection: sync pubsub: cannot unsubscribe, no such node", e);
                        }
                    } else if (!feed.equals(getMyFeedId())){
                        subscribedFeeds.add(feed);
                    }
                }
                for (String addFeed : addFeeds) {
                    try {
                        final Node node = pubSubManager.getNode(addFeed);
                        node.subscribe(selfJid.asBareJid().toString());
                    } catch (PubSubException.NotAPubSubNodeException | XMPPException.XMPPErrorException e) {
                        Log.e("connection: sync pubsub: cannot subscribe, no such node", e);
                        subscribedFeeds.remove(addFeed);
                    }
                }
                for (String subscribedFeed : subscribedFeeds) {
                    final Node node;
                    try {
                        node = pubSubManager.getNode(subscribedFeed);
                        node.addItemEventListener((ItemEventListener<PayloadItem<SimplePayload>>) items -> {
                            Log.i("connection: got " + items.getItems().size() + " items on " + subscribedFeed + " feed, " + items.getPublishedDate());
                            processPublishedItems(items);
                        });
                    } catch (PubSubException.NotAPubSubNodeException e) {
                        Log.e("connection: sync pubsub: cannot listen, no such node", e);
                    }
                }
                
            } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException | InterruptedException e) {
                Log.e("connection: sync pubsub", e);
            }
        });
    }

    private void processPublishedItems(ItemPublishEvent<PayloadItem<SimplePayload>> items) {
        Preconditions.checkNotNull(connection);
        final List<PublishedEntry> entries = PublishedEntry.getPublishedItems(items);
        for (PublishedEntry entry : entries) {
            if (entry.user.equals(connection.getUser().getLocalpart().toString())) {
                observer.onOutgoingPostAcked(FEED_JID.toString(), entry.id);
            } else {
                Post post = new Post(0,
                        FEED_JID.toString(),
                        JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked(entry.user), Domainpart.fromOrThrowUnchecked(XMPP_DOMAIN)).toString(),
                        entry.id,
                        null,
                        0,
                        System.currentTimeMillis(),
                        Post.POST_STATE_RECEIVED,
                        entry.url == null ? Post.POST_TYPE_TEXT : Post.POST_TYPE_IMAGE,
                        entry.text,
                        entry.url
                        );
                observer.onIncomingPostReceived(post);
            }
        }

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
                    final LeafNode myFeedNode = pubSubManager.getNode(getMyFeedId());
                    final SimplePayload payload = new SimplePayload(new PublishedEntry(null, connection.getUser().getLocalpart().toString(), post.text, post.url).toXml());
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

    private String getMyFeedId() {
        return getFeedId(Preconditions.checkNotNull(connection).getUser());
    }

    private static String getFeedId(@NonNull Jid jid) {
        return "feed-" + jid.asEntityBareJidOrThrow().getLocalpart().toString();
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
                        Post.POST_STATE_RECEIVED,
                        Post.POST_TYPE_TEXT,
                        msg.getBody(),
                        null);
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
}
