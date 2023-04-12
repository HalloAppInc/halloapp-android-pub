package com.halloapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.content.Comment;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.content.Reaction;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.AnswerCall;
import com.halloapp.proto.server.CallRinging;
import com.halloapp.proto.server.ContentMissing;
import com.halloapp.proto.server.EndCall;
import com.halloapp.proto.server.ExpiryInfo;
import com.halloapp.proto.server.GroupFeedRerequest;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.HistoryResend;
import com.halloapp.proto.server.HoldCall;
import com.halloapp.proto.server.HomeFeedRerequest;
import com.halloapp.proto.server.IceCandidate;
import com.halloapp.proto.server.IceRestartAnswer;
import com.halloapp.proto.server.IceRestartOffer;
import com.halloapp.proto.server.IncomingCall;
import com.halloapp.proto.server.MomentNotification;
import com.halloapp.proto.server.MuteCall;
import com.halloapp.proto.server.ProfileUpdate;
import com.halloapp.proto.server.PublicFeedUpdate;
import com.halloapp.proto.server.Rerequest;
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

    public void notifyOfflineQueueComplete(@NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onOfflineQueueComplete(ackId);
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

    public void notifyOutgoingPostSent(@NonNull String postId, @Nullable byte[] protoHash) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onOutgoingPostSent(postId, protoHash);
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

    public void notifyIncomingPublicFeedItemsReceived(@NonNull List<Comment> comments) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onIncomingPublicFeedItemsReceived(comments);
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

    public void notifyOutgoingCommentSent(@NonNull String postId, @NonNull String commentId, @Nullable byte[] protoHash) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onOutgoingCommentSent(postId, commentId, protoHash);
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

    public void notifyOutgoingMomentScreenshotted(@NonNull UserId seenByUserId, @NonNull String postId, long timestamp, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onOutgoingMomentScreenshotted(seenByUserId, postId, timestamp, ackId);
            }
        }
    }

    public void notifyIncomingMomentScreenshotReceiptSent(@NonNull UserId senderUserId, @NonNull String postId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onIncomingMomentScreenshotReceiptSent(senderUserId, postId);
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

    public void notifyMessageRerequest(@NonNull Rerequest.ContentType contentType, @NonNull UserId peerUserId, @NonNull String messageId, @NonNull PublicEdECKey peerIdentityKey, @Nullable Integer otpkId, @NonNull byte[] sessionSetupKey, @NonNull byte[] messageEphemeralKey, @NonNull String stanzaId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onMessageRerequest(contentType, peerUserId, messageId, peerIdentityKey, otpkId, sessionSetupKey, messageEphemeralKey, stanzaId);
            }
        }
    }

    public void notifyGroupFeedRerequest(@NonNull GroupFeedRerequest.ContentType contentType, @NonNull UserId peerUserId, @NonNull GroupId groupId, @NonNull String contentId, boolean senderStateIssue, @NonNull String stanzaId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupFeedRerequest(contentType, peerUserId, groupId, contentId, senderStateIssue, stanzaId);
            }
        }
    }

    public void notifyGroupFeedHistoryRerequest(@NonNull UserId peerUserId, @NonNull GroupId groupId, @NonNull String historyId, boolean senderStateIssue, @NonNull String stanzaId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupFeedHistoryRerequest(peerUserId, groupId, historyId, senderStateIssue, stanzaId);
            }
        }
    }

    public void notifyHomeFeedRerequest(@NonNull HomeFeedRerequest.ContentType contentType, @NonNull UserId peerUserId, @NonNull String contentId, boolean senderStateIssue, @NonNull String stanzaId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onHomeFeedRerequest(contentType, peerUserId, contentId, senderStateIssue, stanzaId);
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

    public void notifyPublicFeedUpdate(@NonNull PublicFeedUpdate publicFeedUpdate, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onPublicFeedUpdate(publicFeedUpdate, ackId);
            }
        }
    }

    public void notifyGroupFeedCreated(
            @NonNull GroupId groupId,
            @NonNull String name,
            @Nullable String avatarId,
            @NonNull List<MemberElement> members,
            @NonNull UserId sender,
            @NonNull String senderName,
            @Nullable ExpiryInfo expiryInfo,
            @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupFeedCreated(groupId, name, avatarId, members, sender, senderName, expiryInfo, ackId);
            }
        }
    }

    public void notifyGroupChatCreated(
            @NonNull GroupId groupId,
            @NonNull String name,
            @Nullable String avatarId,
            @NonNull List<MemberElement> members,
            @NonNull UserId sender,
            @NonNull String senderName,
            @Nullable ExpiryInfo expiryInfo,
            @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupChatCreated(groupId, name, avatarId, members, sender, senderName, expiryInfo, ackId);
            }
        }
    }

    public void notifyGroupMemberChangeReceived(@NonNull GroupId groupId, @Nullable String groupName, @Nullable String avatarId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @Nullable HistoryResend historyResend, GroupStanza.GroupType groupType, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupMemberChangeReceived(groupId, groupName, avatarId, members, sender, senderName, historyResend, groupType, ackId);
            }
        }
    }

    public void notifyGroupMemberJoinReceived(@NonNull GroupId groupId, @Nullable String groupName, @Nullable String avatarId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupMemberJoinReceived(groupId, groupName, avatarId, members, sender, senderName, groupType, ackId);
            }
        }
    }

    public void notifyGroupMemberLeftReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupMemberLeftReceived(groupId, members, groupType, ackId);
            }
        }
    }

    public void notifyGroupAdminChangeReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupAdminChangeReceived(groupId, members, sender, senderName, groupType, ackId);
            }
        }
    }

    public void notifyGroupNameChangeReceived(@NonNull GroupId groupId, @NonNull String name, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupNameChangeReceived(groupId, name, sender, senderName, groupType, ackId);
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

    public void notifyGroupAvatarChangeReceived(@NonNull GroupId groupId, @NonNull String avatarId, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupAvatarChangeReceived(groupId, avatarId, sender, senderName, groupType, ackId);
            }
        }
    }

    public void notifyGroupDescriptionChanged(@NonNull GroupId groupId, @NonNull String description, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupDescriptionChanged(groupId, description, sender, senderName, groupType, ackId);
            }
        }
    }

    public void notifyGroupExpiryChanged(@NonNull GroupId groupId, @NonNull ExpiryInfo expiryInfo, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupExpiryChanged(groupId, expiryInfo, sender, senderName, ackId);
            }
        }
    }

    public void notifyGroupAdminAutoPromoteReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupAdminAutoPromoteReceived(groupId, members, groupType, ackId);
            }
        }
    }

    public void notifyGroupDeleteReceived(@NonNull GroupId groupId, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onGroupDeleteReceived(groupId, sender, senderName, groupType, ackId);
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

    public void notifyPublicCommentRetracted(@NonNull String commentId, @NonNull String postId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onPublicCommentRevoked(commentId, postId);
            }
        }
    }

    public void notifyPostRetracted(@NonNull UserId postUid, @NonNull String postId, long timestamp) {
        notifyPostRetracted(postUid, null, postId, timestamp);
    }

    public void notifyPostRetracted(@NonNull UserId postUid, @Nullable GroupId groupId, @NonNull String postId, long timestamp) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onPostRevoked(postUid, postId, groupId, timestamp);
            }
        }
    }

    public void notifyPublicPostRetracted(@NonNull String postId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onPublicPostRevoked(postId);
            }
        }
    }

    public void notifyIncomingCall(@NonNull UserId peerUid, @NonNull IncomingCall incomingCall, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onIncomingCall(peerUid, incomingCall, ackId);
            }
        }
    }

    public void notifyCallRinging(@NonNull UserId peerUid, @NonNull CallRinging callRinging, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onCallRinging(peerUid, callRinging, ackId);
            }
        }
    }

    public void notifyAnswerCall(@NonNull UserId peerUid, @NonNull AnswerCall answerCall, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onAnswerCall(peerUid, answerCall, ackId);
            }
        }
    }

    public void notifyEndCall(@NonNull UserId peerUid, @NonNull EndCall endCall, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onEndCall(peerUid, endCall, ackId);
            }
        }
    }

    public void notifyIceCandidate(@NonNull UserId peerUid, @NonNull IceCandidate iceCandidate, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onIceCandidate(peerUid, iceCandidate, ackId);
            }
        }
    }

    public void notifyIceRestartOffer(@NonNull UserId peerUid, @NonNull IceRestartOffer iceRestartOffer, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onIceRestartOffer(peerUid, iceRestartOffer, ackId);
            }
        }
    }

    public void notifyIceRestartAnswer(@NonNull UserId peerUid, @NonNull IceRestartAnswer iceRestartAnswer, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onIceRestartAnswer(peerUid, iceRestartAnswer, ackId);
            }
        }
    }

    public void notifyHoldCall(@NonNull UserId peerUid, @NonNull HoldCall holdCall, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onHoldCall(peerUid, holdCall, ackId);
            }
        }
    }

    public void notifyMuteCall(@NonNull UserId peerUid, @NonNull MuteCall muteCall, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onMuteCall(peerUid, muteCall, ackId);
            }
        }
    }

    public void notifyHistoryResend(@NonNull HistoryResend historyResend, @NonNull UserId peerUserId, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onHistoryResend(historyResend, peerUserId, ackId);
            }
        }
    }

    public void notifyContentMissing(@NonNull ContentMissing.ContentType contentType, @NonNull UserId peerUserId, @NonNull String contentId, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onContentMissing(contentType, peerUserId, contentId, ackId);
            }
        }
    }

    public void notifyMomentNotificationReceived(MomentNotification momentNotification, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onMomentNotificationReceived(momentNotification, ackId);
            }
        }
    }

    public void notifyProfileUpdateReceived(@NonNull ProfileUpdate profileUpdate, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onProfileUpdateReceived(profileUpdate, ackId);
            }
        }
    }

    public void notifyPostExpired(@NonNull String postId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onPostExpired(postId);
            }
        }
    }

    public void notifyAiImageReceived(@NonNull String id, @Nullable byte[] bytes, @NonNull String ackId) {
        synchronized (observers) {
            for (Connection.Observer observer : observers) {
                observer.onAiImageReceived(id, bytes, ackId);
            }
        }
    }
}
