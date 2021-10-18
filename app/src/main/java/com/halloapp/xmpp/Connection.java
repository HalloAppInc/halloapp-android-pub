package com.halloapp.xmpp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.ConnectionObservers;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.contacts.ContactSyncResult;
import com.halloapp.content.Comment;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.signal.SignalSessionSetupInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.log_events.EventData;
import com.halloapp.proto.server.Iq;
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

    public static abstract class Observer {
        public void onConnected() {}
        public void onDisconnected() {}
        public void onOfflineQueueComplete() {}
        public void onLoginFailed(boolean deleted) {}
        public void onClientVersionExpiringSoon(int daysLeft) {}
        public void onOutgoingPostSent(@NonNull String postId) {}
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId, long timestamp, @NonNull String ackId) {}
        public void onOutgoingCommentSent(@NonNull String postId, @NonNull String commentId) {}
        public void onAudienceHashMismatch(@NonNull Post post) {}
        public void onIncomingFeedItemsReceived(@NonNull List<Post> posts, @NonNull List<Comment> comment, @NonNull String ackId) {}
        public void onIncomingPostSeenReceiptSent(@NonNull UserId senderUserId, @NonNull String postId) {}
        public void onOutgoingMessageSent(@NonNull ChatId chatId, @NonNull String messageId) {}
        public void onOutgoingMessageDelivered(@NonNull ChatId chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {}
        public void onOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {}
        public void onOutgoingMessagePlayed(@NonNull ChatId chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {}
        public void onIncomingMessageReceived(@NonNull Message message) {}
        public void onIncomingMessageSeenReceiptSent(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {}
        public void onIncomingMessagePlayedReceiptSent(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {}
        public void onMessageRerequest(@NonNull UserId senderUserId, @NonNull String messageId, @NonNull PublicEdECKey peerIdentityKey, @Nullable Integer otpkId, @NonNull byte[] sessionSetupKey, @NonNull byte[] messageEphemeralKey, @NonNull String stanzaId) {}
        public void onGroupFeedRerequest(@NonNull UserId senderUserId, @NonNull GroupId groupId, @NonNull String contentId, boolean senderStateIssue, @NonNull String stanzaId) {}
        public void onContactsChanged(@NonNull List<ContactInfo> contacts, @NonNull List<String> contactHashes, @NonNull String ackId) {}
        public void onInvitesAccepted(@NonNull List<ContactInfo> contacts, @NonNull String ackId) {}
        public void onWhisperKeysMessage(@NonNull WhisperKeysMessage message, @NonNull String ackId) {}
        public void onAvatarChangeMessageReceived(UserId userId, String avatarId, @NonNull String ackId) {}
        public void onGroupCreated(@NonNull GroupId groupId, @NonNull String name, @Nullable String avatarId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {}
        public void onGroupMemberChangeReceived(@NonNull GroupId groupId, @Nullable String groupName, @Nullable String avatarId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {}
        public void onGroupMemberJoinReceived(@NonNull GroupId groupId, @Nullable String groupName, @Nullable String avatarId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {}
        public void onGroupMemberLeftReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull String ackId) {}
        public void onGroupAdminChangeReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {}
        public void onGroupNameChangeReceived(@NonNull GroupId groupId, @NonNull String name, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {}
        public void onGroupBackgroundChangeReceived(@NonNull GroupId groupId, int theme, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {}
        public void onGroupAvatarChangeReceived(@NonNull GroupId groupId, @NonNull String avatarId, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {}
        public void onGroupDescriptionChanged(@NonNull GroupId groupId, @NonNull String description, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {}
        public void onGroupAdminAutoPromoteReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull String ackId) {}
        public void onGroupDeleteReceived(@NonNull GroupId groupId, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {}
        public void onUserNamesReceived(@NonNull Map<UserId, String> names) {}
        public void onUserPhonesReceived(@NonNull Map<UserId, String> phones) {}
        public void onPresenceReceived(UserId user, Long lastSeen) {}
        public void onChatStateReceived(UserId user, ChatState chatState) {}
        public void onServerPropsReceived(@NonNull Map<String, String> props, @NonNull String hash) {}
        public void onPostRevoked(@NonNull UserId senderUserId, @NonNull String postId, GroupId groupId) {}
        public void onCommentRevoked(@NonNull String id, @NonNull UserId commentSenderId, @NonNull String postId, long timestamp) {}
        public void onMessageRevoked(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId, @NonNull String ackId) {}
    }

    public abstract Future<Boolean> connect();

    public abstract void clientExpired();

    public abstract void disconnect();

    public abstract void requestServerProps();

    public abstract Observable<Integer> requestSecondsToExpiration();

    public abstract Observable<MediaUploadIq.Urls> requestMediaUpload(long fileSize, @Nullable String downloadUrl);

    public abstract Observable<ContactSyncResult> syncContacts(@Nullable Collection<String> addPhones, @Nullable Collection<String> deletePhones, boolean fullSync, @Nullable String syncId, int index, boolean lastBatch);

    public abstract void sendPushToken(@NonNull final String pushToken, @NonNull String languageCode);

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

    public abstract void retractPost(final @NonNull String postId);

    public abstract void retractGroupPost(final @NonNull GroupId groupId, @NonNull String postId);

    public abstract void sendComment(final @NonNull Comment comment);

    public abstract void sendRerequestedGroupComment(@NonNull Comment comment, @NonNull UserId userId);

    public abstract void retractMessage(final @NonNull UserId chatUserId, final @NonNull String messageId);

    public abstract void retractComment(final @NonNull String postId, final @NonNull String commentId);

    public abstract void retractGroupComment(final @NonNull GroupId groupId, final @NonNull String postId, final @NonNull String commentId);

    public abstract void sendMessage(final @NonNull Message message, final @Nullable SignalSessionSetupInfo signalSessionSetupInfo);

    public abstract void sendGroupMessage(final @NonNull Message message, final @Nullable SignalSessionSetupInfo signalSessionSetupInfo);

    public abstract Observable<Iq> sendIqRequest(@NonNull HalloIq iq);

    public abstract <T extends HalloIq> Observable<T> sendRequestIq(@NonNull HalloIq iq);

    public abstract void sendRerequest(final @NonNull UserId senderUserId, final @NonNull String messageId, int rerequestCount, @Nullable byte[] teardownKey);

    public abstract void sendGroupPostRerequest(final @NonNull UserId senderUserId, final @NonNull GroupId groupId, final @NonNull String contentId, boolean senderStateIssue);

    public abstract void sendGroupCommentRerequest(final @NonNull UserId senderUserId, final @NonNull GroupId groupId, final @NonNull String contentId, boolean senderStateIssue);

    public abstract void sendAck(final @NonNull String id);

    public abstract void sendPostSeenReceipt(@NonNull UserId senderUserId, @NonNull String postId);

    public abstract void sendMessageSeenReceipt(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId);

    public abstract void sendMessagePlayedReceipt(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId);

    public abstract UserId getUserId(@NonNull String user);

    public abstract boolean getClientExpired();

    public abstract String getAndIncrementShortId();

    public abstract Observable<Iq> deleteAccount(@NonNull String phone);

    public abstract Observable<ExportDataResponseIq> requestAccountData();
}
