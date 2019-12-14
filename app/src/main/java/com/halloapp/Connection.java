package com.halloapp;

import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;

class Connection {

    private static Connection instance;

    private static final String XMPP_DOMAIN = "s.halloapp.net";
    private static final String HOST = "s.halloapp.net";
    private static final int PORT = 5222;
    private static final int CONNECTION_TIMEOUT = 20_000;
    private static final int REPLY_TIMEOUT = 20_000;

    private final Handler handler;
    private @Nullable AbstractXMPPConnection connection;

    static Connection getInstance() {
        if (instance == null) {
            synchronized(Connection.class) {
                if (instance == null) {
                    instance = new Connection();
                }
            }
        }
        return instance;
    }

    private Connection() {
        final HandlerThread handlerThread = new HandlerThread("ConnectionThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    void connect(final @NonNull String user, final @NonNull String password) {
        handler.post(() -> connectImpl(user, password));
    }

    void disconnect() {
        handler.post(this::disconnectImpl);
    }

    void sendMessage(final @NonNull String to, final @NonNull String text) {
        handler.post(() -> sendMessageImpl(to, text));
    }

    @WorkerThread
    private void connectImpl(final @NonNull String user, final @NonNull String password) {
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
            Log.e("cannot create connect", e);
            return;
        }
        try {
            connection.connect();
            connection.login();
            connection.addSyncStanzaListener(new MessagePacketListener(), new StanzaTypeFilter(Message.class));
        } catch (XMPPException | SmackException | IOException | InterruptedException e) {
            Log.e("cannot connect", e);
        }
    }

    @WorkerThread
    private void disconnectImpl() {
        if (connection == null) {
            Log.e("cannot disconnect, no connection");
            return;
        }
        connection.disconnect();
    }

    @WorkerThread
    private void sendMessageImpl(final @NonNull String to, final @NonNull String text) {
        if (connection == null) {
            Log.e("cannot send message, no connection");
            return;
        }
        try {
            connection.sendStanza(new Message(to, text));
        } catch (XmppStringprepException | SmackException.NotConnectedException | InterruptedException e) {
            Log.e("cannot send message", e);
        }
    }

    class MessagePacketListener implements StanzaListener {

        @Override
        public void processStanza(final Stanza packet) {
            final Message msg = (Message) packet;
            if (msg.getBodies().size() > 0 && !Message.Type.error.equals(msg.getType())) {
                //Text message
                Log.i("got message:" + msg);
            } else {
                //This must be sth like delivery receipt or Chat state msg
                Log.i("got message with empty body or error " + msg);
            }
        }
    }
}
