package com.halloapp.xmpp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.ConnectionObservers;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.contacts.ContactSyncResult;
import com.halloapp.contacts.RelationshipInfo;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.content.Reaction;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.signal.SignalSessionSetupInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.log_events.EventData;
import com.halloapp.proto.server.AnswerCall;
import com.halloapp.proto.server.CallRinging;
import com.halloapp.proto.server.ContentMissing;
import com.halloapp.proto.server.EndCall;
import com.halloapp.proto.server.ExpiryInfo;
import com.halloapp.proto.server.GroupFeedHistory;
import com.halloapp.proto.server.GroupFeedRerequest;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.HistoryResend;
import com.halloapp.proto.server.HoldCall;
import com.halloapp.proto.server.HomeFeedRerequest;
import com.halloapp.proto.server.IceCandidate;
import com.halloapp.proto.server.IceRestartAnswer;
import com.halloapp.proto.server.IceRestartOffer;
import com.halloapp.proto.server.IncomingCall;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.MomentNotification;
import com.halloapp.proto.server.Msg;
import com.halloapp.proto.server.MuteCall;
import com.halloapp.proto.server.NoiseMessage;
import com.halloapp.proto.server.ProfileUpdate;
import com.halloapp.proto.server.ReportUserContent;
import com.halloapp.proto.server.Rerequest;
import com.halloapp.proto.server.UploadMedia;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.stats.Counter;
import com.halloapp.xmpp.groups.MemberElement;
import com.halloapp.xmpp.util.Observable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@SuppressWarnings("WeakerAccess")
public abstract class Connection {

    private static Connection instance;

    public static Connection getInstance() {
        if (instance == null) {
            synchronized(Connection.class) {
                if (instance == null) {
                    instance = new ConnectionImpl(
                            Me.getInstance(),
                            BgWorkers.getInstance(),
                            Preferences.getInstance(),
                            ConnectionObservers.getInstance());
                }
            }
        }
        return instance;
    }

    public abstract static class MsgCallback {
        public void onAck() {}
        public void onTimeout() {}
    }

    public static abstract class Observer {
        public void onConnected() {}
        public void onDisconnected() {}
        public void onOfflineQueueComplete(@NonNull String ackId) {}
        public void onLoginFailed(boolean deleted) {}
        public void onClientVersionExpiringSoon(int daysLeft) {}
        public void onOutgoingPostSent(@NonNull String postId, @Nullable byte[] protoHash) {}
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId, long timestamp, @NonNull String ackId) {}
        public void onOutgoingCommentSent(@NonNull String postId, @NonNull String commentId, @Nullable byte[] protoHash) {}
        public void onAudienceHashMismatch(@NonNull ContentItem contentItem) {}
        public void onIncomingFeedItemsReceived(@NonNull List<Post> posts, @NonNull List<Comment> comment, @NonNull String ackId) {}
        public void onIncomingPostSeenReceiptSent(@NonNull UserId senderUserId, @NonNull String postId) {}
        public void onIncomingMomentScreenshotReceiptSent(@NonNull UserId senderUserId, @NonNull String postId) {}
        public void onOutgoingMomentScreenshotted(@NonNull UserId seenByUserId, @NonNull String postId, long timestamp, @NonNull String ackId) {}
        public void onOutgoingMessageSent(@NonNull ChatId chatId, @NonNull String messageId) {}
        public void onOutgoingMessageDelivered(@NonNull ChatId chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {}
        public void onOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {}
        public void onOutgoingMessagePlayed(@NonNull ChatId chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {}
        public void onIncomingMessageReceived(@NonNull Message message) {}
        public void onIncomingMessageSeenReceiptSent(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {}
        public void onIncomingMessagePlayedReceiptSent(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {}
        public void onMessageRerequest(@NonNull Rerequest.ContentType contentType, @NonNull UserId senderUserId, @NonNull String messageId, @NonNull PublicEdECKey peerIdentityKey, @Nullable Integer otpkId, @NonNull byte[] sessionSetupKey, @NonNull byte[] messageEphemeralKey, @NonNull String stanzaId) {}
        public void onGroupFeedRerequest(@NonNull GroupFeedRerequest.ContentType contentType, @NonNull UserId senderUserId, @NonNull GroupId groupId, @NonNull String contentId, boolean senderStateIssue, @NonNull String stanzaId) {}
        public void onGroupFeedHistoryRerequest(@NonNull UserId senderUserId, @NonNull GroupId groupId, @NonNull String historyId, boolean senderStateIssue, @NonNull String stanzaId) {}
        public void onHomeFeedRerequest(@NonNull HomeFeedRerequest.ContentType contentType, @NonNull UserId senderUserId, @NonNull String contentId, boolean senderStateIssue, @NonNull String stanzaId) {}
        public void onContactsChanged(@NonNull List<ContactInfo> contacts, @NonNull List<String> contactHashes, @NonNull String ackId) {}
        public void onInvitesAccepted(@NonNull List<ContactInfo> contacts, @NonNull String ackId) {}
        public void onWhisperKeysMessage(@NonNull WhisperKeysMessage message, @NonNull String ackId) {}
        public void onAvatarChangeMessageReceived(UserId userId, String avatarId, @NonNull String ackId) {}
        public void onGroupFeedCreated(@NonNull GroupId groupId, @NonNull String name, @Nullable String avatarId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @Nullable ExpiryInfo expiryInfo, @NonNull String ackId) {}
        public void onGroupChatCreated(@NonNull GroupId groupId, @NonNull String name, @Nullable String avatarId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @Nullable ExpiryInfo expiryInfo, @NonNull String ackId) {}
        public void onGroupMemberChangeReceived(@NonNull GroupId groupId, @Nullable String groupName, @Nullable String avatarId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @Nullable HistoryResend historyResend, GroupStanza.GroupType groupType, @NonNull String ackId) {}
        public void onGroupMemberJoinReceived(@NonNull GroupId groupId, @Nullable String groupName, @Nullable String avatarId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {}
        public void onGroupMemberLeftReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {}
        public void onGroupAdminChangeReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {}
        public void onGroupNameChangeReceived(@NonNull GroupId groupId, @NonNull String name, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {}
        public void onGroupBackgroundChangeReceived(@NonNull GroupId groupId, int theme, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {}
        public void onGroupAvatarChangeReceived(@NonNull GroupId groupId, @NonNull String avatarId, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {}
        public void onGroupDescriptionChanged(@NonNull GroupId groupId, @NonNull String description, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {}
        public void onGroupExpiryChanged(@NonNull GroupId groupId, @NonNull ExpiryInfo expiryInfo, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {}
        public void onGroupAdminAutoPromoteReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {}
        public void onGroupDeleteReceived(@NonNull GroupId groupId, @NonNull UserId sender, @NonNull String senderName, @NonNull GroupStanza.GroupType groupType, @NonNull String ackId) {}
        public void onUserNamesReceived(@NonNull Map<UserId, String> names) {}
        public void onUserPhonesReceived(@NonNull Map<UserId, String> phones) {}
        public void onPresenceReceived(UserId user, Long lastSeen) {}
        public void onChatStateReceived(UserId user, ChatState chatState) {}
        public void onServerPropsReceived(@NonNull Map<String, String> props, @NonNull String hash) {}
        public void onPostRevoked(@NonNull UserId senderUserId, @NonNull String postId, GroupId groupId, long timestamp) {}
        public void onCommentRevoked(@NonNull String id, @NonNull UserId commentSenderId, @NonNull String postId, long timestamp) {}
        public void onMessageRevoked(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId, @NonNull String ackId) {}
        public void onIncomingCall(@NonNull UserId peerUid, @NonNull IncomingCall incomingCall, @NonNull String ackId) {}
        public void onCallRinging(@NonNull UserId peerUid, @NonNull CallRinging callRinging, @NonNull String ackId) {}
        public void onAnswerCall(@NonNull UserId peerUid, @NonNull AnswerCall answerCall, @NonNull String ackId) {}
        public void onEndCall(@NonNull UserId peerUid, @NonNull EndCall endCall, @NonNull String ackId) {}
        public void onIceCandidate(@NonNull UserId peerUid, @NonNull IceCandidate iceCandidate, @NonNull String ackId) {}
        public void onIceRestartOffer(@NonNull UserId peerUid, @NonNull IceRestartOffer iceRestartOffer, @NonNull String ackId) {}
        public void onIceRestartAnswer(@NonNull UserId peerUid, @NonNull IceRestartAnswer iceRestartAnswer, @NonNull String ackId) {}
        public void onHoldCall(@NonNull UserId peerUid, @NonNull HoldCall holdCall, @NonNull String ackId) {}
        public void onMuteCall(@NonNull UserId peerUid, @NonNull MuteCall muteCall, @NonNull String ackId) {}
        public void onHistoryResend(@NonNull HistoryResend historyResend, @NonNull UserId peerUserId, @NonNull String ackId) {}
        public void onContentMissing(@NonNull ContentMissing.ContentType contentType, @NonNull UserId peerUserId, @NonNull String contentId, @NonNull String ackId) {}
        public void onMomentNotificationReceived(MomentNotification momentNotification, @NonNull String ackId) {}
        public void onProfileUpdateReceived(@NonNull ProfileUpdate profileUpdate, @NonNull String ackId) {}
    }

    public abstract Future<Boolean> connect();

    public abstract void resetConnectionBackoff();

    public abstract void clientExpired();

    public abstract void disconnect();

    public abstract void requestServerProps();

    public abstract Observable<Integer> requestSecondsToExpiration();

    public abstract Observable<MediaUploadIq.Urls> requestMediaUpload(long fileSize, @Nullable String downloadUrl, @Nullable UploadMedia.Type type);

    public abstract Observable<ContactSyncResult> syncContacts(@Nullable Collection<String> addPhones, @Nullable Collection<String> deletePhones, boolean fullSync, @Nullable String syncId, int index, boolean lastBatch);

    public abstract void sendPushToken(@NonNull final String pushToken, @NonNull String languageCode, long timeZoneOffset);

    public abstract void sendHuaweiPushToken(@NonNull final String pushToken, @NonNull String languageCode, long timeZoneOffset);

    public abstract Observable<Void> sendName(@NonNull final String name);

    public abstract void subscribePresence(UserId userId);

    public abstract void updatePresence(boolean available);

    public abstract void updateChatState(@NonNull ChatId chat, @ChatState.Type int state);

    public abstract void uploadMoreOneTimePreKeys(@NonNull List<byte[]> oneTimePreKeys);

    public abstract Observable<WhisperKeysResponseIq> downloadKeys(@NonNull UserId userId);

    public abstract void sendStats(List<Counter> counters);

    public abstract Observable<Void> sendEvents(Collection<EventData> events);

    public abstract Observable<String> setAvatar(byte[] bytes, byte[] largeBytes);

    public abstract Observable<String> removeAvatar();

    public abstract Observable<String> setGroupAvatar(GroupId groupId, byte[] bytes, byte[] largeBytes);

    public abstract Observable<String> removeGroupAvatar(GroupId groupId);

    public abstract Observable<String> getAvatarId(UserId userId);

    public abstract Observable<String> getMyAvatarId();

    public abstract Observable<Void> sharePosts(final Map<UserId, Collection<Post>> shareMap);

    public abstract void sendPost(final @NonNull Post post);

    public abstract void sendRerequestedGroupPost(@NonNull Post post, @NonNull UserId userId);

    public abstract void sendRerequestedHistoryResend(@NonNull HistoryResend.Builder historyResend, @NonNull UserId userId);

    public abstract void sendRerequestedHomePost(@NonNull Post post, @NonNull UserId userId);

    public abstract void sendRerequestedHomeComment(@NonNull Comment comment, @NonNull UserId userId);

    public abstract void retractPost(final @NonNull String postId);

    public abstract void retractRerequestedPost(final @NonNull String postId, @NonNull UserId peerUserId);

    public abstract void retractGroupPost(final @NonNull GroupId groupId, @NonNull String postId);

    public abstract void retractRerequestedGroupPost(final @NonNull GroupId groupId, @NonNull String postId, @NonNull UserId peerUserId);

    public abstract void sendComment(final @NonNull Comment comment);

    public abstract void sendRerequestedGroupComment(@NonNull Comment comment, @NonNull UserId userId);

    public abstract void sendRerequestedGroupMessage(@NonNull Message message, @NonNull UserId userId);

    public abstract void sendGroupHistory(@NonNull GroupFeedHistory groupFeedHistory, @NonNull UserId userId);

    public abstract void sendMissingContentNotice(ContentMissing.ContentType contentType, @NonNull String contentId, @NonNull UserId userId);

    public abstract void retractMessage(final @NonNull UserId chatUserId, final @NonNull String messageId);

    public abstract void retractComment(final @NonNull String postId, final @NonNull String commentId);

    public abstract void retractRerequestedComment(final @NonNull String postId, final @NonNull String commentId, @NonNull UserId peerUserId);

    public abstract void retractGroupComment(final @NonNull GroupId groupId, final @NonNull String postId, final @NonNull String commentId);

    public abstract void retractRerequestedGroupComment(final @NonNull GroupId groupId, final @NonNull String postId, final @NonNull String commentId, @NonNull UserId peerUserId);

    public abstract void retractGroupMessage(final @NonNull GroupId groupId, final @NonNull String messageId);

    public abstract void sendMessage(final @NonNull Message message, final @Nullable SignalSessionSetupInfo signalSessionSetupInfo);

    public abstract void sendNoiseMessageToWebClient(final @NonNull byte[] connectionInfo, @NonNull NoiseMessage.MessageType type, @NonNull PublicEdECKey webClientStaticKey, @NonNull int msgLength);

    public abstract void sendMessageToWebClient(@NonNull byte[] content, @NonNull PublicEdECKey webClientStaticKey, String msgId);

    public abstract void sendGroupMessage(final @NonNull Message message);

    public abstract void sendChatReaction(final @NonNull Reaction reaction, final @NonNull Message message, final @Nullable SignalSessionSetupInfo signalSessionSetupInfo);

    public abstract void sendGroupChatReaction(final @NonNull Reaction reaction, final @NonNull Message message);

    public abstract Observable<ExternalShareRetrieveResponseIq> getSharedPost(@NonNull String shareId);

    public abstract Observable<HalloIq> revokeSharedPost(@NonNull String shareId);

    public abstract Observable<Iq> sendIqRequest(@NonNull HalloIq iq);

    public abstract Observable<Iq> sendIqRequest(@NonNull Iq.Builder iq);

    public abstract <T extends HalloIq> Observable<T> sendRequestIq(@NonNull HalloIq iq);

    public abstract <T extends HalloIq> Observable<T> sendRequestIq(@NonNull HalloIq iq, boolean resendable);

    public abstract void sendRerequest(final @NonNull UserId senderUserId, final @NonNull String messageId, final boolean isReaction, int rerequestCount, @Nullable byte[] teardownKey);

    public abstract void sendGroupPostRerequest(final @NonNull UserId senderUserId, final @NonNull GroupId groupId, final @NonNull String contentId, int rerequestCount, boolean senderStateIssue);

    public abstract void sendGroupCommentRerequest(final @NonNull UserId senderUserId, final @NonNull GroupId groupId, final @NonNull String contentId, int rerequestCount, boolean senderStateIssue, @NonNull com.halloapp.proto.server.Comment.CommentType commentType);

    public abstract void sendGroupMessageRerequest(final @NonNull UserId senderUserId, final @NonNull GroupId groupId, final @NonNull String contentId, int rerequestCount, boolean senderStateIssue);

    public abstract void sendGroupFeedHistoryRerequest(@NonNull UserId senderUserId, @NonNull GroupId groupId, @NonNull String historyId, boolean senderStateIssue);

    public abstract void sendGroupHistoryPayloadRerequest(final @NonNull UserId senderUserId, final @NonNull String contentId, @Nullable byte[] teardownKey);

    public abstract void sendHomePostRerequest(final @NonNull UserId senderUserId, boolean favorites, final @NonNull String contentId, int rerequestCount, boolean senderStateIssue);

    public abstract void sendHomeCommentRerequest(@NonNull UserId postSenderUserId, @NonNull UserId commentSenderUserId, int rerequestCount, @NonNull String contentId, @NonNull com.halloapp.proto.server.Comment.CommentType commentType);

    public abstract void sendAck(final @NonNull String id);

    public abstract void sendPostSeenReceipt(@NonNull UserId senderUserId, @NonNull String postId);

    public abstract void sendMomentScreenshotReceipt(@NonNull UserId senderUserId, @NonNull String postId);

    public abstract void sendMessageSeenReceipt(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId);

    public abstract void sendMessagePlayedReceipt(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId);

    public abstract void sendMsg(@NonNull Msg msg, @Nullable MsgCallback callback, long timeout, boolean resendable);

    public abstract UserId getUserId(@NonNull String user);

    public abstract Observable<Iq> getKatchupUserProfileInfo(@Nullable UserId userId, @Nullable String username);

    public abstract boolean getClientExpired();

    public abstract String getAndIncrementShortId();

    public abstract Observable<Iq> deleteAccount(@NonNull String phone, @Nullable String reason);

    public abstract Observable<Iq> reportUserContent(@NonNull UserId userId, @Nullable String contentId, @Nullable ReportUserContent.Reason reason);

    public abstract Observable<ExportDataResponseIq> requestAccountData();

    public abstract Observable<ExportDataResponseIq> getAccountDataRequestState();

    public abstract Observable<RelationshipListResponseIq> requestRelationshipList(@RelationshipInfo.Type int relationshipType);

    public abstract Observable<RelationshipResponseIq> requestFollowUser(@NonNull UserId userId);

    public abstract Observable<RelationshipResponseIq> requestUnfollowUser(@NonNull UserId userId);

    public abstract Observable<RelationshipResponseIq> requestRemoveFollower(@NonNull UserId userId);

    public abstract Observable<RelationshipResponseIq> requestBlockUser(@NonNull UserId userId);

    public abstract Observable<RelationshipResponseIq> requestUnblockUser(@NonNull UserId userId);

    public abstract Observable<UsernameResponseIq> sendUsername(@NonNull String username);

    public abstract Observable<UsernameResponseIq> checkUsernameIsAvailable(@NonNull String username);

    public abstract Observable<FollowSuggestionsResponseIq> requestFollowSuggestions();

    public abstract Observable<UserSearchResponseIq> searchForUser(@NonNull String text);

    public abstract Observable<Iq> rejectFollowSuggestion(@NonNull UserId userId);

    public abstract Observable<PublicFeedResponseIq> requestPublicFeed(@Nullable String cursor, @Nullable Double latitude, @Nullable Double longitude, boolean showDevContent);

    public abstract Observable<SetBioResponseIq> sendBio(@NonNull String bio);

    public abstract Observable<SetLinkResponseIq> sendUserDefinedLink(@NonNull String text);

    public abstract Observable<SetLinkResponseIq> sendTikTokLink(@NonNull String text);

    public abstract Observable<SetLinkResponseIq> sendInstagramLink(@NonNull String text);

    public abstract Observable<SetLinkResponseIq> sendSnapchatLink(@NonNull String text);
}
