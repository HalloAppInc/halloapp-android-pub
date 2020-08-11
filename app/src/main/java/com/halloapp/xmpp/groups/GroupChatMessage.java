package com.halloapp.xmpp.groups;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.Constants;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.crypto.EncryptedSessionManager;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.ChatMessage;
import com.halloapp.proto.Container;
import com.halloapp.proto.MediaType;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.Xml;
import com.halloapp.xmpp.ChatMessageElement;
import com.halloapp.xmpp.MessageElementHelper;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.EmbeddedExtensionProvider;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jxmpp.jid.Jid;
import org.xmlpull.v1.XmlPullParser;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

public class GroupChatMessage implements ExtensionElement {

    public static final String ELEMENT = "group_chat";
    public static final String NAMESPACE = "halloapp:groups";

    private static final String ELEMENT_PROTOBUF_STAGE_ONE = "s1";
    private static final String ELEMENT_ENCRYPTED = "enc";

    private static final String ATTRIBUTE_GROUP_ID = "gid";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_AVATAR_ID = "avatar";
    private static final String ATTRIBUTE_SENDER_ID = "sender";
    private static final String ATTRIBUTE_SENDER_NAME = "sender_name";

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

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public XmlStringBuilder toXML(String enclosingNamespace) {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.attribute(ATTRIBUTE_GROUP_ID, groupId.rawId());
        xml.rightAngleBracket();

        xml.openElement(ELEMENT_PROTOBUF_STAGE_ONE);
        xml.append(getEncodedEntryString());
        xml.closeElement(ELEMENT_PROTOBUF_STAGE_ONE);

        xml.closeElement(ELEMENT);

        return xml;
    }

    public Message getMessage(Jid from, String id) {
        final Message message = new Message(0,
                groupId,
                new UserId(from.getLocalpartOrNull().toString()),
                id,
                timestamp,
                Message.TYPE_CHAT,
                Message.USAGE_CHAT,
                chatMessage.getMediaCount() == 0 ? Message.STATE_INCOMING_RECEIVED : Message.STATE_INITIAL,
                chatMessage.getText(),
                chatMessage.getFeedPostId(),
                chatMessage.getFeedPostMediaIndex(),
                0);
        for (com.halloapp.proto.Media item : chatMessage.getMediaList()) {
            message.media.add(Media.createFromUrl(
                    MessageElementHelper.fromProtoMediaType(item.getType()),
                    item.getDownloadUrl(),
                    item.getEncryptionKey().toByteArray(),
                    item.getPlaintextHash().toByteArray(),
                    item.getWidth(),
                    item.getHeight()));
        }
        for (com.halloapp.proto.Mention item : chatMessage.getMentionsList()) {
            message.mentions.add(Mention.parseFromProto(item));
        }

        return message;
    }

    private byte[] getEncodedEntry() {
        Container.Builder containerBuilder = Container.newBuilder();
        containerBuilder.setChatMessage(chatMessage);
        return containerBuilder.build().toByteArray();
    }
    private String getEncodedEntryString() {
        return Base64.encodeToString(getEncodedEntry(), Base64.NO_WRAP);
    }

    public static class Provider extends ExtensionElementProvider<GroupChatMessage> {

        @Override
        public final GroupChatMessage parse(XmlPullParser parser, int initialDepth) throws Exception {
            final String timestampStr = parser.getAttributeValue(null, "timestamp");
            long timestamp = 0;
            if (timestampStr != null) {
                timestamp = Long.parseLong(timestampStr) * 1000L;
            }

            String rawGroupId = parser.getAttributeValue(null, ATTRIBUTE_GROUP_ID);
            String groupName = parser.getAttributeValue(null, ATTRIBUTE_NAME);
            String avatarId = parser.getAttributeValue(null, ATTRIBUTE_AVATAR_ID);
            String senderRawId = parser.getAttributeValue(null, ATTRIBUTE_SENDER_ID);
            String senderName = parser.getAttributeValue(null, ATTRIBUTE_SENDER_NAME);

            ChatMessage chatMessage = null;
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                final String name = Preconditions.checkNotNull(parser.getName());
                if (name.equals(ELEMENT_PROTOBUF_STAGE_ONE)) {
                    chatMessage = MessageElementHelper.readEncodedEntryString(Xml.readText(parser));
                } else {
                    Xml.skip(parser);
                }
            }
            return new GroupChatMessage(new GroupId(rawGroupId), groupName, avatarId, new UserId(senderRawId), senderName, chatMessage, timestamp);
        }
    }
}
