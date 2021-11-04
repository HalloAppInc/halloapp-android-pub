package com.halloapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.content.Comment;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.xmpp.ChatState;
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

    public void notifyServerPropsReceived(@NonNull Map<String, String> props, @NonNull String hash) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onServerPropsReceived(props, hash);
            }
        }
    }

    public void notifyLoginFailed(boolean deleted) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onLoginFailed(deleted);
            }
        }
    }

    public void notifyOfflineQueueComplete() {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onOfflineQueueComplete();
            }
        }
    }

    public void notifyClientVersionExpiringSoon(int daysLeft) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onClientVersionExpiringSoon(daysLeft);
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

    public void notifyOutgoingCommentSent(@NonNull String postId, @NonNull String commentId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onOutgoingCommentSent(postId, commentId);
            }
        }
    }

    public void notifyAudienceHashMismatch(@NonNull ContentItem contentItem) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onAudienceHashMismatch(contentItem);
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

    public void notifyOutgoingMessageSent(@NonNull ChatId chatId, @NonNull String messageId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onOutgoingMessageSent(chatId, messageId);
            }
        }
    }

    public void notifyOutgoingMessageDelivered(@NonNull ChatId chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onOutgoingMessageDelivered(chatId, userId, id, timestamp, stanzaId);
            }
        }
    }

    public void notifyOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onOutgoingMessageSeen(chatId, userId, id, timestamp, stanzaId);
            }
        }
    }

    public void notifyOutgoingMessagePlayed(@NonNull ChatId chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onOutgoingMessagePlayed(chatId, userId, id, timestamp, stanzaId);
            }
        }
    }

    public void notifyMessageRetracted(@NonNull ChatId chatId, @NonNull UserId userId, @NonNull String msgId, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onMessageRevoked(chatId, userId, msgId, ackId);
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

    public void notifyIncomingMessageSeenReceiptSent(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onIncomingMessageSeenReceiptSent(chatId, senderUserId, messageId);
            }
        }
    }

    public void notifyIncomingMessagePlayedReceiptSent(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onIncomingMessagePlayedReceiptSent(chatId, senderUserId, messageId);
            }
        }
    }

    public void notifyMessageRerequest(@NonNull UserId peerUserId, @NonNull String messageId, @NonNull PublicEdECKey peerIdentityKey, @Nullable Integer otpkId, @NonNull byte[] sessionSetupKey, @NonNull byte[] messageEphemeralKey, @NonNull String stanzaId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onMessageRerequest(peerUserId, messageId, peerIdentityKey, otpkId, sessionSetupKey, messageEphemeralKey, stanzaId);
            }
        }
    }

    public void notifyGroupFeedRerequest(@NonNull UserId peerUserId, @NonNull GroupId groupId, @NonNull String contentId, boolean senderStateIssue, @NonNull String stanzaId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupFeedRerequest(peerUserId, groupId, contentId, senderStateIssue, stanzaId);
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

    public void notifyInvitesAccepted(@NonNull List<ContactInfo> contacts, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onInvitesAccepted(contacts, ackId);
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

    public void notifyUserPhonesReceived(@NonNull Map<UserId, String> phones) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onUserPhonesReceived(phones);
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

    public void notifyGroupCreated(@NonNull GroupId groupId, @NonNull String name, @Nullable String avatarId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupCreated(groupId, name, avatarId, members, sender, senderName, ackId);
            }
        }
    }

    public void notifyGroupMemberChangeReceived(@NonNull GroupId groupId, @Nullable String groupName, @Nullable String avatarId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupMemberChangeReceived(groupId, groupName, avatarId, members, sender, senderName, ackId);
            }
        }
    }

    public void notifyGroupMemberJoinReceived(@NonNull GroupId groupId, @Nullable String groupName, @Nullable String avatarId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupMemberJoinReceived(groupId, groupName, avatarId, members, sender, senderName, ackId);
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

    public void notifyGroupBackgroundChangeReceived(@NonNull GroupId groupId, int theme, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupBackgroundChangeReceived(groupId, theme, sender, senderName, ackId);
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

    public void notifyGroupDescriptionChanged(@NonNull GroupId groupId, @NonNull String description, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupDescriptionChanged(groupId, description, sender, senderName, ackId);
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

    public void notifyGroupDeleteReceived(@NonNull GroupId groupId, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupDeleteReceived(groupId, sender, senderName, ackId);
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

    public void notifyChatStateReceived(UserId userId, ChatState chatState) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onChatStateReceived(userId, chatState);
            }
        }
    }

    public void notifyCommentRetracted(@NonNull String commentId, @NonNull UserId commentUid, @NonNull String postId, long timestamp) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onCommentRevoked(commentId, commentUid, postId, timestamp);
            }
        }
    }

    public void notifyPostRetracted(@NonNull UserId postUid, @NonNull String postId) {
        notifyPostRetracted(postUid, null, postId);
    }

    public void notifyPostRetracted(@NonNull UserId postUid, @Nullable GroupId groupId, @NonNull String postId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onPostRevoked(postUid, postId, groupId);
            }
        }
    }

}
