package com.halloapp.xmpp.groups;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.halloapp.Me;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.clients.ChatMessage;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.server.GroupChat;
import com.halloapp.xmpp.MessageElementHelper;

public class GroupChatMessage {

    public GroupId groupId;
    public String name;
    public String avatarId;
    public UserId sender;
    public String senderName;
    public String payload;

    private final long timestamp;
    private final ChatMessage chatMessage;

    public GroupChatMessage(GroupId groupId, Message message) {
        this.groupId = groupId;
        this.payload = "payload";
        this.timestamp = message.timestamp;
        this.chatMessage = MessageElementHelper.messageToChatMessage(message);
    }

    private GroupChatMessage(GroupId groupId, String name, String avatarId, UserId sender, String senderName, ChatMessage chatMessage, long timestamp) {
        this.groupId = groupId;
        this.name = name;
        this.avatarId = avatarId;
        this.sender = sender;
        this.senderName = senderName;
        this.chatMessage = chatMessage;
        this.timestamp = timestamp;
    }

    public Message getMessage(UserId senderUserId, String id) {
        String rawReplyMessageId = chatMessage.getChatReplyMessageId();
        String rawSenderId = chatMessage.getChatReplyMessageSenderId();
        final Message message = new Message(0,
                groupId,
                senderUserId,
                id,
                timestamp,
                Message.TYPE_CHAT,
                Message.USAGE_CHAT,
                chatMessage.getMediaCount() == 0 ? Message.STATE_INCOMING_RECEIVED : Message.STATE_INITIAL,
                chatMessage.getText(),
                chatMessage.getFeedPostId(),
                chatMessage.getFeedPostMediaIndex(),
                TextUtils.isEmpty(rawReplyMessageId) ? null : rawReplyMessageId,
                chatMessage.getChatReplyMessageMediaIndex(),
                rawSenderId.equals(Me.getInstance().getUser()) ? UserId.ME : new UserId(rawSenderId),
                0);
        for (com.halloapp.proto.clients.Media item : chatMessage.getMediaList()) {
            message.media.add(Media.createFromUrl(
                    MessageElementHelper.fromProtoMediaType(item.getType()),
                    item.getDownloadUrl(),
                    item.getEncryptionKey().toByteArray(),
                    item.getPlaintextHash().toByteArray(),
                    item.getWidth(),
                    item.getHeight()));
        }
        for (com.halloapp.proto.clients.Mention item : chatMessage.getMentionsList()) {
            message.mentions.add(Mention.parseFromProto(item));
        }

        return message;
    }

    private byte[] getEncodedEntry() {
        Container.Builder containerBuilder = Container.newBuilder();
        containerBuilder.setChatMessage(chatMessage);
        return containerBuilder.build().toByteArray();
    }

    public GroupChat toProto() {
        GroupChat.Builder builder = GroupChat.newBuilder();
        builder.setGid(groupId.rawId());
        builder.setPayload(ByteString.copyFrom(getEncodedEntry()));

        return builder.build();
    }

    public static GroupChatMessage fromProto(@NonNull GroupChat groupChat) {
        long timestamp = groupChat.getTimestamp() * 1000L;

        String rawGroupId = groupChat.getGid();
        String groupName = groupChat.getName();
        String avatarId = groupChat.getAvatarId();
        String senderRawId = Long.toString(groupChat.getSenderUid());
        String senderName = groupChat.getSenderName();

        ByteString plaintext = groupChat.getPayload();
        ChatMessage chatMessage =  MessageElementHelper.readEncodedEntry(plaintext.toByteArray());

        return new GroupChatMessage(new GroupId(rawGroupId), groupName, avatarId, new UserId(senderRawId), senderName, chatMessage, timestamp);
    }
}
