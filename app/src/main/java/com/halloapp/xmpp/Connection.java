package com.halloapp.xmpp;

import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;

import com.halloapp.BuildConfig;
import com.halloapp.Me;
import com.halloapp.contacts.UserId;
import com.halloapp.content.Comment;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
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
import org.jivesoftware.smack.packet.NamedElement;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.debugger.android.AndroidDebugger;
import org.jivesoftware.smackx.pubsub.EventElement;
import org.jivesoftware.smackx.pubsub.EventElementType;
import org.jivesoftware.smackx.pubsub.ItemsExtension;
import org.jivesoftware.smackx.pubsub.PayloadItem;
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
        void onClientVersionExpired();
        void onOutgoingPostSent(@NonNull String postId);
        void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId, long timestamp, @NonNull String ackId);
        void onOutgoingCommentSent(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull String commentId);
        void onIncomingFeedItemsReceived(@NonNull List<Post> posts, @NonNull List<Comment> comment, @NonNull String ackId);
        void onIncomingPostSeenReceiptSent(@NonNull UserId senderUserId, @NonNull String postId);
        void onOutgoingMessageSent(@NonNull String chatId, @NonNull String messageId);
        void onOutgoingMessageDelivered(@NonNull String chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId);
        void onOutgoingMessageSeen(@NonNull String chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId);
        void onIncomingMessageReceived(@NonNull Message message);
        void onIncomingMessageSeenReceiptSent(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId);
        void onContactsChanged(@NonNull List<ContactInfo> protocolContacts, @NonNull String ackId);
        void onAvatarMetadataReceived(@NonNull UserId metadataUserId, @NonNull PublishedAvatarMetadata pam, @NonNull String ackId);
        void onLowOneTimePreKeyCountReceived(int count);
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

        ProviderManager.addExtensionProvider("item", "http://jabber.org/protocol/pubsub", new PubSubItem.Provider()); // smack doesn't handle 'publisher' and 'timestamp' attributes
        ProviderManager.addExtensionProvider("item", "http://jabber.org/protocol/pubsub#event", new PubSubItem.Provider()); // smack doesn't handle 'publisher' and 'timestamp' attributes
        ProviderManager.addExtensionProvider("retract", "http://jabber.org/protocol/pubsub", new PubSubItem.Provider()); // smack doesn't handle 'publisher' and 'timestamp' attributes
        ProviderManager.addExtensionProvider("retract", "http://jabber.org/protocol/pubsub#event", new PubSubItem.Provider()); // smack doesn't handle 'publisher' and 'timestamp' attributes
        ProviderManager.addExtensionProvider(ChatMessageElement.ELEMENT, ChatMessageElement.NAMESPACE, new ChatMessageElement.Provider());
        ProviderManager.addExtensionProvider(DeliveryReceiptElement.ELEMENT, DeliveryReceiptElement.NAMESPACE, new DeliveryReceiptElement.Provider());
        ProviderManager.addExtensionProvider(SeenReceiptElement.ELEMENT, SeenReceiptElement.NAMESPACE, new SeenReceiptElement.Provider());
        ProviderManager.addExtensionProvider(ContactList.ELEMENT, ContactList.NAMESPACE, new ContactList.Provider());
        ProviderManager.addExtensionProvider(WhisperKeysLowCountMessage.ELEMENT, WhisperKeysLowCountMessage.NAMESPACE, new WhisperKeysLowCountMessage.Provider());
        ProviderManager.addIQProvider(ContactsSyncResponseIq.ELEMENT, ContactsSyncResponseIq.NAMESPACE, new ContactsSyncResponseIq.Provider());
        ProviderManager.addIQProvider(MediaUploadIq.ELEMENT, MediaUploadIq.NAMESPACE, new MediaUploadIq.Provider());
        ProviderManager.addIQProvider(SecondsToExpirationIq.ELEMENT, SecondsToExpirationIq.NAMESPACE, new SecondsToExpirationIq.Provider());
        ProviderManager.addIQProvider(WhisperKeysResponseIq.ELEMENT, WhisperKeysResponseIq.NAMESPACE, new WhisperKeysResponseIq.Provider());

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
            connection = new XMPPTCPConnection(config, () -> {
                clientExpired();
                observer.onClientVersionExpired();
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

    public void subscribePresence(UserId userId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (!reconnectIfNeeded() || connection == null) {
                    Log.e("connection: request presence subscription: no connection");
                    return;
                }
                try {
                    PresenceStanza stanza = new PresenceStanza(userIdToJid(userId), null, "subscribe", null);
                    connection.sendStanza(stanza);
                } catch (InterruptedException | SmackException.NotConnectedException e) {
                    Log.e("Failed to subscribe", e);
                }
            }
        });
    }

    public void updatePresence(boolean available) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (!reconnectIfNeeded() || connection == null) {
                    Log.e("connection: update presence: no connection");
                    return;
                }
                try {
                    PresenceStanza stanza = new PresenceStanza(connection.getXMPPServiceDomain(), null, available ? "available" : "away", null);
                    connection.sendStanza(stanza);
                } catch (InterruptedException | SmackException.NotConnectedException e) {
                    Log.e("Failed to update presence", e);
                }
            }
        });
    }

    public void uploadKeys(@Nullable byte[] identityKey, @Nullable byte[] signedPreKey, @NonNull List<byte[]> oneTimePreKeys) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: upload keys: no connection");
                return;
            }
            final WhisperKeysUploadIq uploadIq = new WhisperKeysUploadIq(connection.getXMPPServiceDomain(), identityKey, signedPreKey, oneTimePreKeys);
            try {
                final IQ response = connection.createStanzaCollectorAndSend(uploadIq).nextResultOrThrow();
                Log.d("connection: response after uploading keys " + response.toString());
            } catch (SmackException.NotConnectedException | InterruptedException | XMPPException.XMPPErrorException | SmackException.NoResponseException e) {
                Log.e("connection: cannot upload keys", e);
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
            final WhisperKeysDownloadIq downloadIq = new WhisperKeysDownloadIq(connection.getXMPPServiceDomain(), userId.rawId());
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

    public void publishAvatarMetadata(String hash, String url, int numBytes, int height, int width) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot update avatar metadata, no connection");
                return;
            }
            try {
                final PublishedAvatarMetadata metadata = new PublishedAvatarMetadata(hash, url, numBytes, height, width);
                final SimplePayload payload = new SimplePayload(metadata.toXml());
                final PayloadItem<SimplePayload> item = new PayloadItem<>(PublishedAvatarMetadata.AVATAR_ID, payload);
                pubSubHelper.publishItem(getMyAvatarMetadataNodeId(), item);
                // the {@link PubSubHelper#publishItem(String, Item)} waits for IQ reply, so we can report the post was acked here

                //observer.onOutgoingPostSent(post.postId);
            } catch (SmackException.NotConnectedException | InterruptedException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.w("connection: cannot update avatar metadata", e);
            }
        });
    }

    public Future<PubSubItem> getMostRecentAvatarMetadata(UserId userId) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot get avatar metadata, no connection");
                return null;
            }
            try {
                List<PubSubItem> items = pubSubHelper.getItems(userId.isMe() ? getMyAvatarMetadataNodeId() : getAvatarMetadataNodeId(userIdToJid(userId)), Collections.singleton(PublishedAvatarMetadata.AVATAR_ID));
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
                    entry.media.add(new PublishedEntry.Media(PublishedEntry.getMediaType(media.type), media.url, media.encKey, media.sha256hash, media.width, media.height));
                }
                final SimplePayload payload = new SimplePayload(entry.toXml());
                final PubSubItem item = new PubSubItem(PubSubItem.PUB_SUB_ITEM_TYPE_FEED_POST, post.id, payload);
                pubSubHelper.publishItem(getMyFeedNodeId(), item);
                // the {@link PubSubHelper#publishItem(String, Item)} waits for IQ reply, so we can report the post was acked here
                observer.onOutgoingPostSent(post.id);
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
                final PubSubItem item = new PubSubItem(PubSubItem.PUB_SUB_ITEM_TYPE_FEED_POST, postId, payload);
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
                final PubSubItem item = new PubSubItem(PubSubItem.PUB_SUB_ITEM_TYPE_COMMENT, comment.commentId, payload);
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
                final PubSubItem item = new PubSubItem(PubSubItem.PUB_SUB_ITEM_TYPE_COMMENT, commentId, payload);
                pubSubHelper.retractItem(postSenderUserId.isMe() ? getMyFeedNodeId() : getFeedNodeId(userIdToJid(postSenderUserId)), item);
                // the {@link PubSubHelper#retractItem(String, Item)} waits for IQ reply, so we can report the comment was acked here
                observer.onOutgoingCommentSent(postSenderUserId, postId, commentId);
            } catch (SmackException.NotConnectedException | InterruptedException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.e("connection: cannot retract comment", e);
            }
        });
    }

    public void sendMessage(final @NonNull Message message) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot send post, no connection");
                return;
            }
            try {
                final PublishedEntry entry = new PublishedEntry(
                        PublishedEntry.ENTRY_CHAT,
                        null,
                        message.timestamp,
                        connection.getUser().getLocalpart().toString(),
                        message.text,
                        null,
                        null);
                for (Media media : message.media) {
                    entry.media.add(new PublishedEntry.Media(PublishedEntry.getMediaType(media.type), media.url, media.encKey, media.sha256hash, media.width, media.height));
                }

                final Jid recipientJid = JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked(message.chatId), Domainpart.fromOrNull(XMPP_DOMAIN));
                final org.jivesoftware.smack.packet.Message xmppMessage = new org.jivesoftware.smack.packet.Message(recipientJid);
                xmppMessage.setStanzaId(message.id);
                xmppMessage.addExtension(new ChatMessageElement(entry));
                ackHandlers.put(xmppMessage.getStanzaId(), () -> observer.onOutgoingMessageSent(message.chatId, message.id));
                Log.i("connection: sending message " + message.id + " to " + recipientJid);
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
                message.addExtension(new SeenReceiptElement("feed", postId));
                ackHandlers.put(message.getStanzaId(), () -> observer.onIncomingPostSeenReceiptSent(senderUserId, postId));
                Log.i("connection: sending post seen receipt " + postId + " to " + recipientJid);
                connection.sendStanza(message);
            } catch (SmackException.NotConnectedException | InterruptedException e) {
                Log.e("connection: cannot send post seen receipt", e);
            }
        });
    }

    public void sendMessageSeenReceipt(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot send message seen receipt, no connection");
                return;
            }
            try {
                final Jid recipientJid = userIdToJid(senderUserId);
                final org.jivesoftware.smack.packet.Message message = new org.jivesoftware.smack.packet.Message(recipientJid);
                message.setStanzaId(RandomId.create());
                message.addExtension(new SeenReceiptElement(chatId, messageId));
                ackHandlers.put(message.getStanzaId(), () -> observer.onIncomingMessageSeenReceiptSent(chatId, senderUserId, messageId));
                Log.i("connection: sending message seen receipt " + messageId + " to " + recipientJid);
                connection.sendStanza(message);
            } catch (SmackException.NotConnectedException | InterruptedException e) {
                Log.e("connection: cannot send message seen receipt", e);
            }
        });
    }

    public void sendMessageDeliveryReceipt(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot send message delivery receipt, no connection");
                return;
            }
            try {
                final Jid recipientJid = userIdToJid(senderUserId);
                final org.jivesoftware.smack.packet.Message message = new org.jivesoftware.smack.packet.Message(recipientJid);
                message.setStanzaId(RandomId.create());
                message.addExtension(new DeliveryReceiptElement(chatId, messageId));
                ackHandlers.put(message.getStanzaId(), () -> {});
                Log.i("connection: sending message delivery receipt " + messageId + " to " + recipientJid);
                connection.sendStanza(message);
            } catch (SmackException.NotConnectedException | InterruptedException e) {
                Log.e("connection: cannot send message delivery receipt", e);
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
        final List<PublishedEntry> entries = PublishedEntry.getEntries(items);
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
                            entry.media.isEmpty() ? Post.TRANSFERRED_DESTINATION : Post.TRANSFERRED_NO,
                            Post.SEEN_NO,
                            entry.text
                    );
                    for (PublishedEntry.Media entryMedia : entry.media) {
                        post.media.add(Media.createFromUrl(PublishedEntry.getMediaType(entryMedia.type), entryMedia.url,
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

    private boolean processMetadataPubSubItems(@NonNull UserId metadataUserId, @NonNull List<? extends NamedElement> items, @NonNull String ackId) {
        Preconditions.checkNotNull(connection);
        final List<PublishedAvatarMetadata> pams = PublishedAvatarMetadata.getAvatarMetadatas(items);
        observer.onAvatarMetadataReceived(metadataUserId, pams.get(0), ackId);
        return true;
    }

    private void parseFeedHistoryItems(UserId feedUserId, List<PubSubItem> items, Collection<Post> posts, Collection<Comment> comments) {
        final List<PublishedEntry> entries = PublishedEntry.getEntries(items);
        for (PublishedEntry entry : entries) {
            if (entry.type == PublishedEntry.ENTRY_FEED) {
                final Post post = new Post(0,
                        getUserId(entry.user),
                        entry.id,
                        entry.timestamp,
                        (isMe(entry.user) || entry.media.isEmpty()) ? Post.TRANSFERRED_DESTINATION : Post.TRANSFERRED_NO,
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

    private static boolean isMetadataNodeId(@NonNull String nodeId) {
        return nodeId.startsWith("metadata-");
    }

    private UserId getFeedUserId(@NonNull String nodeId) {
        Preconditions.checkArgument(isFeedNodeId(nodeId));
        return getUserId(nodeId.substring("feed-".length()));
    }

    private UserId getMetadataUserId(@NonNull String nodeId) {
        Preconditions.checkArgument(isMetadataNodeId(nodeId));
        return getUserId(nodeId.substring("metadata-".length()));
    }

    private static String getNodeId(@NonNull String prefix, @NonNull Jid jid) {
        return prefix + "-" + jid.asEntityBareJidOrThrow().getLocalpart().toString();
    }

    private static Jid userIdToJid(@NonNull UserId userId) {
        return JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked(userId.rawId()), Domainpart.fromOrNull(XMPP_DOMAIN));
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
            PresenceLoader presenceLoader = PresenceLoader.getInstance(Connection.this);
            presenceLoader.reportPresence(getUserId(packet.getFrom()), ((PresenceStanza) packet).lastSeen);
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
                final EventElement event = packet.getExtension(PubSubNamespace.event.name(), PubSubNamespace.event.getXmlns());
                if (event != null && EventElementType.items.equals(event.getEventType())) {
                    final ItemsExtension itemsElem = (ItemsExtension) event.getEvent();
                    if (itemsElem != null) {
                        String node = itemsElem.getNode();
                        if (isFeedNodeId(node)) {
                            Log.i("connection: got feed pubsub " + msg);
                            handled = processFeedPubSubItems(getFeedUserId(node), itemsElem.getItems(), msg.getStanzaId());
                        } else if (isMetadataNodeId(node)) {
                            Log.i("connection: got metadata pubsub " + msg);
                            handled = processMetadataPubSubItems(getMetadataUserId(node), itemsElem.getItems(), msg.getStanzaId());
                        }
                    }
                }
                if (!handled) {
                    final ChatMessageElement chatMessage = packet.getExtension(ChatMessageElement.ELEMENT, ChatMessageElement.NAMESPACE);
                    if (chatMessage != null) {
                        Log.i("connection: got chat message " + msg);
                        observer.onIncomingMessageReceived(chatMessage.getMessage(packet.getFrom(), packet.getStanzaId()));
                        handled = true;
                    }
                }
                if (!handled) {
                    final DeliveryReceiptElement deliveryReceipt = packet.getExtension(DeliveryReceiptElement.ELEMENT, DeliveryReceiptElement.NAMESPACE);
                    if (deliveryReceipt != null) {
                        Log.i("connection: got delivery receipt " + msg);
                        final String threadId = deliveryReceipt.getThreadId();
                        final UserId userId = getUserId(packet.getFrom());
                        observer.onOutgoingMessageDelivered(threadId == null ? userId.rawId() : threadId, userId, deliveryReceipt.getId(), deliveryReceipt.getTimestamp(), packet.getStanzaId());
                        handled = true;
                    }
                }
                if (!handled) {
                    final SeenReceiptElement seenReceipt = packet.getExtension(SeenReceiptElement.ELEMENT, SeenReceiptElement.NAMESPACE);
                    if (seenReceipt != null) {
                        Log.i("connection: got seen receipt " + msg);
                        final String threadId = seenReceipt.getThreadId();
                        final UserId userId = getUserId(packet.getFrom());
                        // TODO (ds): uncomment and remove next line when supported by server
                        observer.onOutgoingPostSeen(userId, seenReceipt.getId(), seenReceipt.getTimestamp(), packet.getStanzaId());
                        /*
                        if ("feed".equals(threadId)) {
                            observer.onOutgoingPostSeen(userId, seenReceipt.getId(), seenReceipt.getTimestamp(), packet.getStanzaId());
                        } else {
                            observer.onOutgoingMessageSeen(threadId, userId, seenReceipt.getId(), seenReceipt.getTimestamp(), packet.getStanzaId());
                        }
                        */
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
                if (!handled) {
                    final WhisperKeysLowCountMessage whisperKeysLowCountMessage = packet.getExtension(WhisperKeysLowCountMessage.ELEMENT, WhisperKeysLowCountMessage.NAMESPACE);
                    if (whisperKeysLowCountMessage != null) {
                        Log.i("connection: got low otp count " + msg + " count: " + whisperKeysLowCountMessage.count);
                        observer.onLowOneTimePreKeyCountReceived(whisperKeysLowCountMessage.count);
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

    static class HalloConnectionListener implements ConnectionListener {

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
