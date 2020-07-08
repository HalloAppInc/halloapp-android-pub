package com.halloapp;

import androidx.annotation.NonNull;

import com.halloapp.contacts.UserId;
import com.halloapp.content.Comment;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ContactInfo;
import com.halloapp.xmpp.WhisperKeysMessage;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConnectionObservers {

    private final Set<Connection.Observer> observers = new HashSet<>();

    private static ConnectionObservers instance;

    public static ConnectionObservers getInstance() {
        if (instance == null) {
            synchronized (ConnectionObservers.class) {
                if (instance == null) {
                    instance = new ConnectionObservers();
                }
            }
        }
        return instance;
    }

    private ConnectionObservers() {}

    public void addObserver(Connection.Observer observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }

    public void removeObserver(@NonNull Connection.Observer observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }

    public void notifyConnected() {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onConnected();
            }
        }
    }

    public void notifyDisconnected() {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onDisconnected();
            }
        }
    }

    public void notifyLoginFailed() {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onLoginFailed();
            }
        }
    }

    public void notifyClientVersionExpired() {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onClientVersionExpired();
            }
        }
    }

    public void notifyOutgoingPostSent(@NonNull String postId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onOutgoingPostSent(postId);
            }
        }
    }

    public void notifyIncomingFeedItemsReceived(@NonNull List<Post> posts, @NonNull List<Comment> comments, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onIncomingFeedItemsReceived(posts, comments, ackId);
            }
        }
    }

    public void notifyOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId, long timestamp, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onOutgoingPostSeen(seenByUserId, postId, timestamp, ackId);
            }
        }
    }

    public void notifyOutgoingCommentSent(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull String commentId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onOutgoingCommentSent(postSenderUserId, postId, commentId);
            }
        }
    }

    public void notifyIncomingPostSeenReceiptSent(@NonNull UserId senderUserId, @NonNull String postId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onIncomingPostSeenReceiptSent(senderUserId, postId);
            }
        }
    }

    public void notifyOutgoingMessageSent(@NonNull String chatId, @NonNull String messageId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onOutgoingMessageSent(chatId, messageId);
            }
        }
    }

    public void notifyOutgoingMessageDelivered(@NonNull String chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onOutgoingMessageDelivered(chatId, userId, id, timestamp, stanzaId);
            }
        }
    }

    public void notifyOutgoingMessageSeen(@NonNull String chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onOutgoingMessageSeen(chatId, userId, id, timestamp, stanzaId);
            }
        }
    }

    public void notifyIncomingMessageReceived(@NonNull Message message) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onIncomingMessageReceived(message);
            }
        }
    }

    public void notifyIncomingMessageSeenReceiptSent(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onIncomingMessageSeenReceiptSent(chatId, senderUserId, messageId);
            }
        }
    }

    public void notifyMessageRerequest(@NonNull UserId peerUserId, @NonNull String messageId, @NonNull String stanzaId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onMessageRerequest(peerUserId, messageId, stanzaId);
            }
        }
    }

    public void notifyContactsChanged(@NonNull List<ContactInfo> protocolContacts, @NonNull List<String> contactHashes, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onContactsChanged(protocolContacts, contactHashes, ackId);
            }
        }
    }

    public void notifyUserNamesReceived(@NonNull Map<UserId, String> names) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onUserNamesReceived(names);
            }
        }
    }

    public void notifyWhisperKeysMessage(WhisperKeysMessage message, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onWhisperKeysMessage(message, ackId);
            }
        }
    }

    public void notifyAvatarChangeMessageReceived(UserId userId, String avatarId, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onAvatarChangeMessageReceived(userId, avatarId, ackId);
            }
        }
    }

}
