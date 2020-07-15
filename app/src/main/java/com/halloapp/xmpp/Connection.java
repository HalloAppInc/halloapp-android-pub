package com.halloapp.xmpp;

import android.content.Context;
import android.os.HandlerThread;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;

import com.halloapp.BuildConfig;
import com.halloapp.ConnectionObservers;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.contacts.UserId;
import com.halloapp.content.Comment;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.crypto.EncryptedSessionManager;
import com.halloapp.crypto.SessionSetupInfo;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.xmpp.groups.GroupResponseIq;
import com.halloapp.xmpp.groups.GroupsListResponseIq;
import com.halloapp.xmpp.privacy.PrivacyListsResponseIq;
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
public class Connection {

    private static Connection instance;

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

    public static Connection getInstance() {
        if (instance == null) {
            synchronized(Connection.class) {
                if (instance == null) {
                    instance = new Connection(BgWorkers.getInstance(), ConnectionObservers.getInstance());
                }
            }
        }
        return instance;
    }

    public static abstract class Observer {
        public void onConnected() {}
        public void onDisconnected() {}
        public void onLoginFailed() {}
        public void onClientVersionExpired() {}
        public void onOutgoingPostSent(@NonNull String postId) {}
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId, long timestamp, @NonNull String ackId) {}
        public void onOutgoingCommentSent(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull String commentId) {}
        public void onIncomingFeedItemsReceived(@NonNull List<Post> posts, @NonNull List<Comment> comment, @NonNull String ackId) {}
        public void onIncomingPostSeenReceiptSent(@NonNull UserId senderUserId, @NonNull String postId) {}
        public void onOutgoingMessageSent(@NonNull String chatId, @NonNull String messageId) {}
        public void onOutgoingMessageDelivered(@NonNull String chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {}
        public void onOutgoingMessageSeen(@NonNull String chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {}
        public void onIncomingMessageReceived(@NonNull Message message) {}
        public void onIncomingMessageSeenReceiptSent(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {}
        public void onMessageRerequest(@NonNull UserId senderUserId, @NonNull String messageId, @NonNull String stanzaId) {}
        public void onContactsChanged(@NonNull List<ContactInfo> contacts, @NonNull List<String> contactHashes, @NonNull String ackId) {}
        public void onWhisperKeysMessage(@NonNull WhisperKeysMessage message, @NonNull String ackId) {}
        public void onAvatarChangeMessageReceived(UserId userId, String avatarId, @NonNull String ackId) {}
        public void onUserNamesReceived(@NonNull Map<UserId, String> names) {}
        public void onPresenceReceived(UserId user, Long lastSeen) {}
    }

    private Connection(BgWorkers bgWorkers, ConnectionObservers connectionObservers) {
        this.bgWorkers = bgWorkers;
        this.connectionObservers = connectionObservers;
        final HandlerThread handlerThread = new HandlerThread("ConnectionThread");
        handlerThread.start();
        SmackConfiguration.DEBUG = BuildConfig.DEBUG;
        Roster.setRosterLoadedAtLoginDefault(false);
    }

    public void connect(final @NonNull Context context) {
        executor.execute(() -> {
            this.me = Me.getInstance(context);
            this.preferences = Preferences.getInstance(context);
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
        ProviderManager.addExtensionProvider(WhisperKeysMessage.ELEMENT, WhisperKeysMessage.NAMESPACE, new WhisperKeysMessage.Provider());
        ProviderManager.addExtensionProvider(AvatarChangeMessage.ELEMENT, AvatarChangeMessage.NAMESPACE, new AvatarChangeMessage.Provider());
        ProviderManager.addExtensionProvider(RerequestElement.ELEMENT, RerequestElement.NAMESPACE, new RerequestElement.Provider());
        ProviderManager.addIQProvider(ContactsSyncResponseIq.ELEMENT, ContactsSyncResponseIq.NAMESPACE, new ContactsSyncResponseIq.Provider());
        ProviderManager.addIQProvider(PrivacyListsResponseIq.ELEMENT, PrivacyListsResponseIq.NAMESPACE, new PrivacyListsResponseIq.Provider());
        ProviderManager.addIQProvider(InvitesResponseIq.ELEMENT, InvitesResponseIq.NAMESPACE, new InvitesResponseIq.Provider());
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

        connectionObservers.notifyConnected();

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

    public Future<Integer> getAvailableInviteCount() {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: can't check invite count, no connection");
                return null;
            }
            final InvitesRequestIq requestIq = InvitesRequestIq.createGetInviteIq();
            requestIq.setTo(connection.getXMPPServiceDomain());
            requestIq.setFrom(connection.getUser());
            IQ responseIq = connection.createStanzaCollectorAndSend(requestIq).nextResultOrThrow();
            if (responseIq instanceof InvitesResponseIq) {
                return ((InvitesResponseIq) responseIq).invitesLeft;
            }
            return null;
        });
    }

    public Future<Integer> sendInvite(@NonNull String phoneNumber) {
        return executor.submit(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: can't invite, no connection");
                return null;
            }
            final InvitesRequestIq requestIq = InvitesRequestIq.createSendInviteIq(Collections.singleton(phoneNumber));
            requestIq.setTo(connection.getXMPPServiceDomain());
            InvitesResponseIq responseIq = connection.createStanzaCollectorAndSend(requestIq).nextResultOrThrow();
            if (!responseIq.successfulInvites.isEmpty()) {
                return InvitesResponseIq.Result.SUCCESS;
            } else {
                for (String phone : responseIq.failedInvites.keySet()) {
                    return responseIq.failedInvites.get(phone);
                }
            }
            return InvitesResponseIq.Result.UNKNOWN;
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
                final SimplePayload payload = new SimplePayload(entry.toXml());
                final PubSubItem item = new PubSubItem(PubSubItem.PUB_SUB_ITEM_TYPE_FEED_POST, post.id, payload);
                pubSubHelper.publishItem(getMyFeedNodeId(), item);
                // the {@link PubSubHelper#publishItem(String, Item)} waits for IQ reply, so we can report the post was acked here
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
                final SimplePayload payload = new SimplePayload(entry.toXml());
                final PubSubItem item = new PubSubItem(PubSubItem.PUB_SUB_ITEM_TYPE_COMMENT, comment.commentId, payload);
                pubSubHelper.publishItem(comment.postSenderUserId.isMe() ? getMyFeedNodeId() : getFeedNodeId(userIdToJid(comment.postSenderUserId)), item);
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
                connectionObservers.notifyOutgoingCommentSent(postSenderUserId, postId, commentId);
            } catch (SmackException.NotConnectedException | InterruptedException | SmackException.NoResponseException | XMPPException.XMPPErrorException e) {
                Log.e("connection: cannot retract comment", e);
            }
        });
    }

    public void sendMessage(final @NonNull Message message, final @Nullable SessionSetupInfo sessionSetupInfo) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot send message, no connection");
                return;
            }
            final Jid recipientJid = JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked(message.chatId), Domainpart.fromOrNull(XMPP_DOMAIN));
            final UserId recipientUserId = new UserId(message.chatId);
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

    public void sendRerequest(final @NonNull Jid originalSender, final @NonNull String messageId) {
        executor.execute(() -> {
            if (!reconnectIfNeeded() || connection == null) {
                Log.e("connection: cannot send message, no connection");
                return;
            }
            try {
                final org.jivesoftware.smack.packet.Message xmppMessage = new org.jivesoftware.smack.packet.Message(originalSender);
                String encodedIdentityKey = Base64.encodeToString(EncryptedSessionManager.getInstance().getPublicIdentityKey().getKeyMaterial(), Base64.NO_WRAP);
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
                message.addExtension(new SeenReceiptElement(senderUserId.rawId().equals(chatId) ? null : chatId, messageId));
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
                            entry.media.isEmpty() ? Post.TRANSFERRED_YES : Post.TRANSFERRED_NO,
                            Post.SEEN_NO,
                            entry.text
                    );
                    for (PublishedEntry.Media entryMedia : entry.media) {
                        post.media.add(Media.createFromUrl(PublishedEntry.getMediaType(entryMedia.type), entryMedia.url,
                                entryMedia.encKey, entryMedia.sha256hash,
                                entryMedia.width, entryMedia.height));
                    }
                    for (com.halloapp.proto.Mention mention : entry.mentions) {
                        post.mentions.add(Mention.parseFromProto(mention));
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
                    for (com.halloapp.proto.Mention mention : entry.mentions) {
                        comment.mentions.add(Mention.parseFromProto(mention));
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
                        }
                    }
                }
                if (!handled) {
                    final ChatMessageElement chatMessage = packet.getExtension(ChatMessageElement.ELEMENT, ChatMessageElement.NAMESPACE);
                    if (chatMessage != null) {
                        Log.i("connection: got chat message " + msg);
                        connectionObservers.notifyIncomingMessageReceived(chatMessage.getMessage(packet.getFrom(), packet.getStanzaId()));
                        handled = true;
                    }
                }
                if (!handled) {
                    final DeliveryReceiptElement deliveryReceipt = packet.getExtension(DeliveryReceiptElement.ELEMENT, DeliveryReceiptElement.NAMESPACE);
                    if (deliveryReceipt != null) {
                        Log.i("connection: got delivery receipt " + msg);
                        final String threadId = deliveryReceipt.getThreadId();
                        final UserId userId = getUserId(packet.getFrom());
                        connectionObservers.notifyOutgoingMessageDelivered(threadId == null ? userId.rawId() : threadId, userId, deliveryReceipt.getId(), deliveryReceipt.getTimestamp(), packet.getStanzaId());
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
                            connectionObservers.notifyOutgoingMessageSeen(threadId == null ? userId.rawId() : threadId, userId, seenReceipt.getId(), seenReceipt.getTimestamp(), packet.getStanzaId());
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
