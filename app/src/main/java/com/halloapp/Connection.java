package com.halloapp;

import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.posts.Post;
import com.halloapp.util.Log;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;

public class Connection {

    private static Connection instance;

    private static final String XMPP_DOMAIN = "s.halloapp.net";
    private static final String HOST = "s.halloapp.net";
    private static final int PORT = 5222;
    private static final int CONNECTION_TIMEOUT = 20_000;
    private static final int REPLY_TIMEOUT = 20_000;

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
            try {
                final XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                        .setUsernameAndPassword(user, password)
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
                return;
            }

            ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(connection);
            if (!sdm.includesFeature(DeliveryReceipt.NAMESPACE)) {
                sdm.addFeature(DeliveryReceipt.NAMESPACE);
            }

            try {
                connection.connect();
                connection.login();

                final DeliveryReceiptManager deliveryReceiptManager = DeliveryReceiptManager.getInstanceFor(connection);
                deliveryReceiptManager.addReceiptReceivedListener(new MessageReceiptsListener());
                connection.addSyncStanzaListener(new MessagePacketListener(), new StanzaTypeFilter(Message.class));
                connection.addStanzaAcknowledgedListener(new MessageAckListener());
            } catch (XMPPException | SmackException | IOException | InterruptedException e) {
                Log.e("connection: cannot connect", e);
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
        });
    }

    public void sendPost(final @NonNull Post post) {
        handler.post(() -> {
            if (connection == null) {
                Log.e("connection: cannot send message, no connection");
                return;
            }
            try {
                final Message message = new Message(post.chatJid, post.text);
                message.setStanzaId(post.postId);
                message.addExtension(new DeliveryReceiptRequest());
                Log.i("connection: sending message " + post.postId + " to " + post.chatJid);
                connection.sendStanza(message);
            } catch (XmppStringprepException | SmackException.NotConnectedException | InterruptedException e) {
                Log.e("connection: cannot send message", e);
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
                        Post.POST_TYPE_IMAGE,
                        msg.getBody(),
                        "");
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
