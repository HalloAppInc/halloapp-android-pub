package com.halloapp;

import androidx.annotation.NonNull;

import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.content.Comment;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ContactInfo;
import com.halloapp.xmpp.WhisperKeysMessage;
import com.halloapp.xmpp.groups.MemberElement;

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

    public void notifyGroupMemberChangeReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupMemberChangeReceived(groupId, members, sender, senderName, ackId);
            }
        }
    }

    public void notifyGroupMemberLeftReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupMemberLeftReceived(groupId, members, ackId);
            }
        }
    }

    public void notifyGroupAdminChangeReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupAdminChangeReceived(groupId, members, sender, senderName, ackId);
            }
        }
    }

    public void notifyGroupNameChangeReceived(@NonNull GroupId groupId, @NonNull String name, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupNameChangeReceived(groupId, name, sender, senderName, ackId);
            }
        }
    }

    public void notifyGroupAvatarChangeReceived(@NonNull GroupId groupId, @NonNull String avatarId, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupAvatarChangeReceived(groupId, avatarId, sender, senderName, ackId);
            }
        }
    }

    public void notifyGroupAdminAutoPromoteReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupAdminAutoPromoteReceived(groupId, members, ackId);
            }
        }
    }

    public void notifyPresenceReceived(UserId userId, Long lastSeen) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onPresenceReceived(userId, lastSeen);
            }
        }
    }

}
