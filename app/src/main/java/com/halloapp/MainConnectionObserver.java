package com.halloapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;

import com.halloapp.content.PostsManager;
import com.halloapp.groups.GroupInfo;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.content.TransferPendingItemsTask;
import com.halloapp.crypto.EncryptedSessionManager;
import com.halloapp.groups.GroupsSync;
import com.halloapp.privacy.BlockListManager;
import com.halloapp.privacy.FeedPrivacyManager;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.AppExpirationActivity;
import com.halloapp.ui.RegistrationRequestActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.xmpp.ChatState;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ContactInfo;
import com.halloapp.xmpp.PresenceLoader;
import com.halloapp.xmpp.WhisperKeysMessage;
import com.halloapp.xmpp.groups.MemberElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainConnectionObserver extends Connection.Observer {

    private static MainConnectionObserver instance;

    private final Context context;

    private final Me me;
    private final BgWorkers bgWorkers;
    private final ContentDb contentDb;
    private final Connection connection;
    private final ContactsDb contactsDb;
    private final GroupsSync groupsSync;
    private final ServerProps serverProps;
    private final AvatarLoader avatarLoader;
    private final PostsManager postsManager;
    private final Notifications notifications;
    private final ForegroundChat foregroundChat;
    private final PresenceLoader presenceLoader;
    private final BlockListManager blockListManager;
    private final FeedPrivacyManager feedPrivacyManager;
    private final ForegroundObserver foregroundObserver;
    private final EncryptedSessionManager encryptedSessionManager;

    public static MainConnectionObserver getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (MainConnectionObserver.class) {
                if (instance == null) {
                    instance = new MainConnectionObserver(
                            context,
                            Me.getInstance(),
                            BgWorkers.getInstance(),
                            ContentDb.getInstance(),
                            Connection.getInstance(),
                            ContactsDb.getInstance(),
                            GroupsSync.getInstance(context),
                            ServerProps.getInstance(),
                            AvatarLoader.getInstance(),
                            PostsManager.getInstance(),
                            Notifications.getInstance(context),
                            ForegroundChat.getInstance(),
                            PresenceLoader.getInstance(),
                            BlockListManager.getInstance(),
                            FeedPrivacyManager.getInstance(),
                            ForegroundObserver.getInstance(),
                            EncryptedSessionManager.getInstance());
                }
            }
        }
        return instance;
    }


    MainConnectionObserver(
            @NonNull Context context,
            @NonNull Me me,
            @NonNull BgWorkers bgWorkers,
            @NonNull ContentDb contentDb,
            @NonNull Connection connection,
            @NonNull ContactsDb contactsDb,
            @NonNull GroupsSync groupsSync,
            @NonNull ServerProps serverProps,
            @NonNull AvatarLoader avatarLoader,
            @NonNull PostsManager postsManager,
            @NonNull Notifications notifications,
            @NonNull ForegroundChat foregroundChat,
            @NonNull PresenceLoader presenceLoader,
            @NonNull BlockListManager blockListManager,
            @NonNull FeedPrivacyManager feedPrivacyManager,
            @NonNull ForegroundObserver foregroundObserver,
            @NonNull EncryptedSessionManager encryptedSessionManager) {
        this.context = context.getApplicationContext();

        this.me = me;
        this.bgWorkers = bgWorkers;
        this.contentDb = contentDb;
        this.connection = connection;
        this.contactsDb = contactsDb;
        this.groupsSync = groupsSync;
        this.serverProps = serverProps;
        this.avatarLoader = avatarLoader;
        this.postsManager = postsManager;
        this.notifications = notifications;
        this.foregroundChat = foregroundChat;
        this.presenceLoader = presenceLoader;
        this.blockListManager = blockListManager;
        this.feedPrivacyManager = feedPrivacyManager;
        this.foregroundObserver = foregroundObserver;
        this.encryptedSessionManager = encryptedSessionManager;
    }

    @Override
    public void onConnected() {
        bgWorkers.execute(() -> {
            try {
                encryptedSessionManager.ensureKeysUploaded();
            } catch (Exception e) {
                Log.e("Failed to ensure keys uploaded", e);
            }
        });
        bgWorkers.execute(blockListManager::fetchBlockList);
        bgWorkers.execute(feedPrivacyManager::fetchFeedPrivacy);
        bgWorkers.execute(postsManager::ensurePostsShared);

        connection.updatePresence(foregroundObserver.isInForeground());
        new TransferPendingItemsTask(context).execute();
        HalloApp.sendPushTokenFromFirebase();
        new RequestExpirationInfoTask(connection, context).execute();
        presenceLoader.onReconnect();
        groupsSync.startGroupsSync();
    }

    @Override
    public void onDisconnected() {
        presenceLoader.onDisconnect();
    }

    @Override
    public void onLoginFailed() {
        me.resetRegistration();
        blockListManager.onLoginFailed();
        if (foregroundObserver.isInForeground()) {
            RegistrationRequestActivity.reVerify(context);
        } else {
            notifications.showLoginFailedNotification();
        }
    }

    @Override
    public void onClientVersionExpired() {
        if (foregroundObserver.isInForeground()) {
            AppExpirationActivity.open(context, 0);
        } else {
            notifications.showExpirationNotification(0);
        }
    }

    @Override
    public void onOutgoingPostSent(@NonNull String postId) {
        contentDb.setPostTransferred(UserId.ME, postId);
    }

    @Override
    public void onIncomingFeedItemsReceived(@NonNull List<Post> posts, @NonNull List<Comment> comments, @NonNull String ackId) {
        contentDb.addFeedItems(posts, comments, () -> connection.sendAck(ackId));
    }

    @Override
    public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId, long timestamp, @NonNull String ackId) {
        contentDb.setOutgoingPostSeen(seenByUserId, postId, timestamp, () -> connection.sendAck(ackId));
    }

    @Override
    public void onOutgoingCommentSent(@NonNull String postId, @NonNull String commentId) {
        contentDb.setCommentTransferred(postId, UserId.ME, commentId);
    }

    @Override
    public void onIncomingPostSeenReceiptSent(@NonNull UserId senderUserId, @NonNull String postId) {
        contentDb.setPostSeenReceiptSent(senderUserId, postId);
    }

    @Override
    public void onOutgoingMessageSent(@NonNull ChatId chatId, @NonNull String messageId) {
        contentDb.setMessageTransferred(chatId, UserId.ME, messageId);
    }

    @Override
    public void onOutgoingMessageDelivered(@NonNull ChatId chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {
        contentDb.setOutgoingMessageDelivered(chatId, userId, id, timestamp, () -> connection.sendAck(stanzaId));
    }

    @Override
    public void onOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId userId, @NonNull String id, long timestamp, @NonNull String stanzaId) {
        contentDb.setOutgoingMessageSeen(chatId, userId, id, timestamp, () -> connection.sendAck(stanzaId));
    }

    @Override
    public void onIncomingMessageReceived(@NonNull Message message) {
        final boolean isMessageForForegroundChat = foregroundChat.isForegroundChatId(message.chatId);
        final Runnable completionRunnable = () -> {
            if (isMessageForForegroundChat) {
                connection.sendMessageSeenReceipt(message.chatId, message.senderUserId, message.id);
            }
            connection.sendAck(message.id);
        };
        if (message.isRetracted()) {
            contentDb.retractMessage(message, completionRunnable);
        } else {
            contentDb.addMessage(message, !isMessageForForegroundChat, completionRunnable);
        }
    }

    @Override
    public void onIncomingSilentMessageReceived(@NonNull Message message) {
        final Runnable completionRunnable = () -> {
            connection.sendAck(message.id);
        };
        contentDb.addSilentMessage(message, completionRunnable);
    }

    @Override
    public void onIncomingMessageSeenReceiptSent(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        contentDb.setMessageSeenReceiptSent(chatId, senderUserId, messageId);
    }

    @Override
    public void onMessageRerequest(@NonNull UserId peerUserId, @NonNull String messageId, @NonNull String stanzaId) {
        bgWorkers.execute(() -> {
            Message message = contentDb.getMessage(peerUserId, UserId.ME, messageId);
            if (message != null && message.rerequestCount < Constants.MAX_REREQUESTS_PER_MESSAGE) {
                contentDb.setMessageRerequestCount(peerUserId, UserId.ME, messageId, message.rerequestCount + 1);
                encryptedSessionManager.sendMessage(message, false);
            }
            connection.sendAck(stanzaId);
        });
    }

    @Override
    public void onContactsChanged(@NonNull List<ContactInfo> protocolContacts, @NonNull List<String> contactHashes, @NonNull String ackId) {
        final List<ContactsDb.NormalizedPhoneData> normalizedPhoneDataList = new ArrayList<>(protocolContacts.size());
        for (ContactInfo contact : protocolContacts) {
            normalizedPhoneDataList.add(new ContactsDb.NormalizedPhoneData(contact.normalizedPhone, new UserId(contact.userId), "friends".equals(contact.role), contact.avatarId));
        }
        bgWorkers.execute(() -> {
            try {
                contactsDb.updateNormalizedPhoneData(normalizedPhoneDataList).get();
                if (!contactHashes.isEmpty()) {
                    ContactsSync.getInstance(context).startContactSync(contactHashes);
                }
                connection.sendAck(ackId);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("ConnectionObserver.onContactsChanged", e);
            }
        });
    }

    @Override
    public void onInvitesAccepted(@NonNull List<ContactInfo> contacts, @NonNull String ackId) {
        bgWorkers.execute(() -> {
            for (ContactInfo contactInfo : contacts) {
                Contact contact = contactsDb.getContact(new UserId(contactInfo.userId));
                notifications.showInviteAcceptedNotification(contact);
            }
            connection.sendAck(ackId);
        });
    }

    @Override
    public void onUserNamesReceived(@NonNull Map<UserId, String> names) {
        contactsDb.updateUserNames(names);
    }

    @Override
    public void onPresenceReceived(UserId user, Long lastSeen) {
        presenceLoader.reportPresence(user, lastSeen);
    }

    @Override
    public void onChatStateReceived(UserId user, ChatState chatState) {
        presenceLoader.reportChatState(user, chatState);
    }

    @Override
    public void onPostRevoked(@NonNull UserId senderUserId, @NonNull String postId, @Nullable GroupId groupId) {
        Post post = new Post(0, senderUserId, postId, 0, Post.TRANSFERRED_NO, Post.SEEN_NO, null);
        if (groupId != null) {
            post.setParentGroup(groupId);
        }
        contentDb.retractPost(post);
    }

    @Override
    public void onCommentRevoked(@NonNull String id, @NonNull UserId commentSenderId, @NonNull String postId, long timestamp) {
        Comment comment = new Comment(0, postId, commentSenderId, id, null, timestamp, !commentSenderId.isMe(), true, null);
        contentDb.retractComment(comment);
    }

    @Override
    public void onWhisperKeysMessage(@NonNull WhisperKeysMessage message, @NonNull String ackId) {
        if (message.count != null) {
            int count = message.count;
            Log.i("OTPK count down to " + count + "; replenishing");
            List<byte[]> protoKeys = encryptedSessionManager.getFreshOneTimePreKeyProtos();
            connection.uploadMoreOneTimePreKeys(protoKeys);
            connection.sendAck(ackId);
        } else if (message.userId != null) {
            encryptedSessionManager.tearDownSession(message.userId);
            connection.sendAck(ackId);
        }
    }

    @Override
    public void onAvatarChangeMessageReceived(UserId userId, String avatarId, @NonNull String ackId) {
        avatarLoader.reportAvatarUpdate(userId, avatarId);
        connection.sendAck(ackId);
    }

    @Override
    public void onGroupCreated(@NonNull GroupId groupId, @NonNull String name, @Nullable String avatarId, @NonNull List<MemberElement> memberElements, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        List<MemberInfo> members = new ArrayList<>();
        for (MemberElement memberElement : memberElements) {
            members.add(new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name));
        }
        members.add(new MemberInfo(-1, sender, MemberElement.Type.ADMIN, senderName));

        contentDb.addGroupChat(new GroupInfo(groupId, name, null, avatarId, members), () -> {
            if (serverProps.getGroupFeedEnabled()) {
                addSystemPost(groupId, sender, Post.USAGE_CREATE_GROUP, null, () -> connection.sendAck(ackId));
            } else {
                addSystemMessage(groupId, sender, Message.USAGE_CREATE_GROUP, null, () -> connection.sendAck(ackId));
            }
        });
    }

    @Override
    public void onGroupMemberChangeReceived(@NonNull GroupId groupId, @Nullable String groupName, @Nullable String avatarId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        List<MemberInfo> added = new ArrayList<>();
        List<MemberInfo> removed = new ArrayList<>();
        for (MemberElement memberElement : members) {
            MemberInfo memberInfo = new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name);
            if (MemberElement.Action.ADD.equals(memberElement.action)) {
                added.add(memberInfo);
            } else if (MemberElement.Action.REMOVE.equals(memberElement.action)) {
                removed.add(memberInfo);
            }
        }

        contentDb.addRemoveGroupMembers(groupId, groupName, avatarId, added, removed, () -> {
            if (!added.isEmpty()) {
                String idList = toUserIdList(added);
                if (serverProps.getGroupFeedEnabled()) {
                    addSystemPost(groupId, sender, Post.USAGE_ADD_MEMBERS, idList, null);
                } else {
                    addSystemMessage(groupId, sender, Message.USAGE_ADD_MEMBERS, idList, null);
                }
            }

            if (!removed.isEmpty()) {
                String idList = toUserIdList(removed);
                if (serverProps.getGroupFeedEnabled()) {
                    addSystemPost(groupId, sender, Post.USAGE_REMOVE_MEMBER, idList, null);
                } else {
                    addSystemMessage(groupId, sender, Message.USAGE_REMOVE_MEMBER, idList, null);
                }
            }

            for (MemberInfo member : removed) {
                if (member.userId.rawId().equals(me.getUser())) {
                    contentDb.setGroupInactive(groupId, null);
                    break;
                }
            }

            connection.sendAck(ackId);
        });
    }

    @Override
    public void onGroupMemberLeftReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull String ackId) {
        List<MemberInfo> left = new ArrayList<>();
        for (MemberElement memberElement : members) {
            MemberInfo memberInfo = new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name);
            if (MemberElement.Action.LEAVE.equals(memberElement.action)) {
                left.add(memberInfo);
            }
        }

        contentDb.addRemoveGroupMembers(groupId, null, null, new ArrayList<>(), left, () -> {
            for (MemberInfo member : left) {
                if (serverProps.getGroupFeedEnabled()) {
                    addSystemPost(groupId, member.userId, Post.USAGE_MEMBER_LEFT, null, () -> {
                        if (member.userId.rawId().equals(me.getUser())) {
                            contentDb.setGroupInactive(groupId, null);
                        }
                    });
                } else {
                    addSystemMessage(groupId, member.userId, Message.USAGE_MEMBER_LEFT, null, () -> {
                        if (member.userId.rawId().equals(me.getUser())) {
                            contentDb.setGroupInactive(groupId, null);
                        }
                    });
                }
            }

            connection.sendAck(ackId);
        });
    }

    @Override
    public void onGroupAdminChangeReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        List<MemberInfo> promoted = new ArrayList<>();
        List<MemberInfo> demoted = new ArrayList<>();
        for (MemberElement memberElement : members) {
            MemberInfo memberInfo = new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name);
            if (MemberElement.Action.PROMOTE.equals(memberElement.action)) {
                promoted.add(memberInfo);
            } else if (MemberElement.Action.DEMOTE.equals(memberElement.action)) {
                demoted.add(memberInfo);
            }
        }

        contentDb.promoteDemoteGroupAdmins(groupId, promoted, demoted, () -> {
            if (!promoted.isEmpty()) {
                String idList = toUserIdList(promoted);
                if (serverProps.getGroupFeedEnabled()) {
                    addSystemPost(groupId, sender, Post.USAGE_PROMOTE, idList, null);
                } else {
                    addSystemMessage(groupId, sender, Message.USAGE_PROMOTE, idList, null);
                }
            }

            if (!demoted.isEmpty()) {
                String idList = toUserIdList(demoted);
                if (serverProps.getGroupFeedEnabled()) {
                    addSystemPost(groupId, sender, Post.USAGE_DEMOTE, idList, null);
                } else {
                    addSystemMessage(groupId, sender, Message.USAGE_DEMOTE, idList, null);
                }
            }

            connection.sendAck(ackId);
        });
    }

    @Override
    public void onGroupNameChangeReceived(@NonNull GroupId groupId, @NonNull String name, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        contentDb.setGroupName(groupId, name, () -> {
            if (serverProps.getGroupFeedEnabled()) {
                addSystemPost(groupId, sender, Post.USAGE_NAME_CHANGE, name, () -> connection.sendAck(ackId));
            } else {
                addSystemMessage(groupId, sender, Message.USAGE_NAME_CHANGE, name, () -> connection.sendAck(ackId));
            }
        });
    }

    @Override
    public void onGroupAvatarChangeReceived(@NonNull GroupId groupId, @NonNull String avatarId, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        contentDb.setGroupAvatar(groupId, avatarId, () -> {
            avatarLoader.reportAvatarUpdate(groupId, avatarId);
            if (serverProps.getGroupFeedEnabled()) {
                addSystemPost(groupId, sender, Post.USAGE_AVATAR_CHANGE, null, () -> connection.sendAck(ackId));
            } else {
                addSystemMessage(groupId, sender, Message.USAGE_AVATAR_CHANGE, null, () -> connection.sendAck(ackId));
            }
        });
    }

    @Override
    public void onGroupAdminAutoPromoteReceived(@NonNull GroupId groupId, @NonNull List<MemberElement> members, @NonNull String ackId) {
        List<MemberInfo> promoted = new ArrayList<>();
        for (MemberElement memberElement : members) {
            MemberInfo memberInfo = new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name);
            if (MemberElement.Action.PROMOTE.equals(memberElement.action)) {
                promoted.add(memberInfo);
            }
        }

        contentDb.promoteDemoteGroupAdmins(groupId, promoted, new ArrayList<>(), () -> {
            for (MemberInfo member : promoted) {
                if (serverProps.getGroupFeedEnabled()) {
                    addSystemPost(groupId, member.userId, Post.USAGE_AUTO_PROMOTE, null, null);
                } else {
                    addSystemMessage(groupId, member.userId, Message.USAGE_AUTO_PROMOTE, null, null);
                }
            }

            connection.sendAck(ackId);
        });
    }

    @Override
    public void onGroupDeleteReceived(@NonNull GroupId groupId, @NonNull UserId sender, @NonNull String senderName, @NonNull String ackId) {
        if (serverProps.getGroupFeedEnabled()) {
            addSystemPost(groupId, sender, Post.USAGE_GROUP_DELETED, null, () -> {
                contentDb.setGroupInactive(groupId, () -> {
                    connection.sendAck(ackId);
                });
            });
        } else {
            addSystemMessage(groupId, sender, Message.USAGE_GROUP_DELETED, null, () -> {
                contentDb.setGroupInactive(groupId, () -> {
                    connection.sendAck(ackId);
                });
            });
        }
    }

    private String toUserIdList(@NonNull List<MemberInfo> members) {
        Preconditions.checkArgument(!members.isEmpty());
        StringBuilder sb = new StringBuilder(userIdToString(members.get(0).userId));
        for (int i=1; i<members.size(); i++) {
            sb.append(",").append(userIdToString(members.get(i).userId));
        }
        return sb.toString();
    }

    private String userIdToString(@NonNull UserId userId) {
        return userId.isMe() ? me.getUser() : userId.rawId();
    }

    private void addSystemPost(@NonNull GroupId groupId, @NonNull UserId sender, @Post.Usage int usage, @Nullable String text, @Nullable Runnable completionRunnable) {
        Post systemPost = new Post(0,
                sender,
                RandomId.create(),
                System.currentTimeMillis(),
                Post.TRANSFERRED_YES,
                Post.SEEN_YES,
                Post.TYPE_SYSTEM,
                text);
        systemPost.usage = usage;
        systemPost.setParentGroup(groupId);
        contentDb.addPost(systemPost, completionRunnable);
    }

    private void addSystemMessage(@NonNull GroupId groupId, @NonNull UserId sender, @Message.Usage int usage, @Nullable String text, @Nullable Runnable completionRunnable) {
        Message promoteAdminsMessage = new Message(0,
                groupId,
                sender,
                RandomId.create(),
                System.currentTimeMillis(),
                Message.TYPE_SYSTEM,
                usage,
                Message.STATE_OUTGOING_DELIVERED,
                text,
                null,
                -1,
                null,
                -1,
                null,
                0);
        contentDb.addMessage(promoteAdminsMessage, false, completionRunnable);
    }
}
