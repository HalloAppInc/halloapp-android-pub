package com.halloapp.xmpp;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.Constants;
import com.halloapp.contacts.UserId;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.crypto.SessionManager;
import com.halloapp.crypto.keys.PublicECKey;
import com.halloapp.proto.ChatMessage;
import com.halloapp.proto.Container;
import com.halloapp.proto.MediaType;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jxmpp.jid.Jid;
import org.xmlpull.v1.XmlPullParser;

public class ChatMessageElement implements ExtensionElement {

    static final String NAMESPACE = "halloapp:chat:messages";
    static final String ELEMENT = "chat";

    private static final String ELEMENT_PROTOBUF_STAGE_ONE = "s1";
    private static final String ELEMENT_ENCRYPTED = "enc";

    private static final String ATTRIBUTE_EPHEMERAL_KEY_ID = "id";
    private static final String ATTRIBUTE_EPHEMERAL_KEY = "ephemeral_key_bytes";
    private static final String ATTRIBUTE_IDENTITY_KEY = "identity_key";
    private static final String ATTRIBUTE_ONE_TIME_PRE_KEY_ID = "one_time_pre_key_id";
    private static final String ATTRIBUTE_SENDER_USER_ID = "sender_user_id";

    private final ChatMessage chatMessage;
    private final long timestamp;
    private final UserId recipientUserId;
    private final UserId senderUserId;
    private final PublicECKey ephemeralKey;
    private final Integer ephemeralKeyId;
    private final PublicECKey identityKey;
    private final Integer oneTimePreKeyId;

    ChatMessageElement(@NonNull Message message, UserId recipientUserId, UserId senderUserId, PublicECKey ephemeralKey, Integer ephemeralKeyId, PublicECKey identityKey, Integer oneTimePreKeyId) {
        this.chatMessage = messageToChatMessage(message);
        this.timestamp = 0;
        this.recipientUserId = recipientUserId;
        this.senderUserId = senderUserId;
        this.ephemeralKey = ephemeralKey;
        this.ephemeralKeyId = ephemeralKeyId;
        this.identityKey = identityKey;
        this.oneTimePreKeyId = oneTimePreKeyId;
    }

    private ChatMessageElement(@NonNull ChatMessage chatMessage, long timestamp) {
        this.chatMessage = chatMessage;
        this.timestamp = timestamp;
        this.recipientUserId = null;
        this.senderUserId = null;
        this.ephemeralKey = null;
        this.ephemeralKeyId = null;
        this.identityKey = null;
        this.oneTimePreKeyId = null;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public XmlStringBuilder toXML(String enclosingNamespace) {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.rightAngleBracket();

        xml.openElement(ELEMENT_PROTOBUF_STAGE_ONE);
        xml.append(getEncodedEntryString());
        xml.closeElement(ELEMENT_PROTOBUF_STAGE_ONE);

        if (Constants.ENCRYPTION_TURNED_ON) {
            xml.halfOpenElement(ELEMENT_ENCRYPTED);
            if (senderUserId != null) {
                xml.attribute(ATTRIBUTE_SENDER_USER_ID, senderUserId.rawId());
            }
            if (ephemeralKey != null && ephemeralKeyId != null) {
                xml.attribute(ATTRIBUTE_EPHEMERAL_KEY, Base64.encodeToString(ephemeralKey.getKeyMaterial(), Base64.NO_WRAP));
                xml.attribute(ATTRIBUTE_EPHEMERAL_KEY_ID, ephemeralKeyId.toString());
            }
            if (identityKey != null) {
                xml.attribute(ATTRIBUTE_IDENTITY_KEY, Base64.encodeToString(identityKey.getKeyMaterial(), Base64.NO_WRAP));
            }
            if (oneTimePreKeyId != null) {
                xml.attribute(ATTRIBUTE_ONE_TIME_PRE_KEY_ID, oneTimePreKeyId.toString());
            }
            xml.rightAngleBracket();
            xml.append(getEncryptedEntryString());
            xml.closeElement(ELEMENT_ENCRYPTED);
        }

        xml.closeElement(ELEMENT);
        return xml;
    }

    Message getMessage(Jid from, String id) {
        final Message message = new Message(0,
                from.getLocalpartOrNull().toString(),
                new UserId(from.getLocalpartOrNull().toString()),
                id,
                timestamp,
                chatMessage.getMediaCount() == 0 ? Message.STATE_INCOMING_RECEIVED : Message.STATE_INITIAL,
                chatMessage.getText());
        for (com.halloapp.proto.Media item : chatMessage.getMediaList()) {
            message.media.add(Media.createFromUrl(
                    fromProtoMediaType(item.getType()),
                    item.getDownloadUrl(),
                    item.getEncryptionKey().toByteArray(),
                    item.getPlaintextHash().toByteArray(),
                    item.getWidth(),
                    item.getHeight()));
        }

        return message;
    }

    private String getEncodedEntryString() {
        return Base64.encodeToString(getEncodedEntry(), Base64.NO_WRAP);
    }

    private String getEncryptedEntryString() {
        try {
            byte[] encodedEntry = getEncodedEntry();
            byte[] encryptedEntry = SessionManager.getInstance().encryptMessage(encodedEntry, recipientUserId);
            return Base64.encodeToString(encryptedEntry, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e("Failed to encrypt", e);
        }
        return "";
    }

    public static ChatMessageElement from(@NonNull org.jivesoftware.smack.packet.Message message) {
        return message.getExtension(ELEMENT, NAMESPACE);
    }

    private byte[] getEncodedEntry() {
        Container.Builder containerBuilder = Container.newBuilder();
        containerBuilder.setChatMessage(chatMessage);
        return containerBuilder.build().toByteArray();
    }

    private static ChatMessage messageToChatMessage(@NonNull Message message) {
        ChatMessage.Builder chatMessageBuilder = ChatMessage.newBuilder();
        for (Media media : message.media) {
            com.halloapp.proto.Media.Builder mediaBuilder = com.halloapp.proto.Media.newBuilder();
            mediaBuilder.setType(getProtoMediaType(media.type));
            mediaBuilder.setWidth(media.width);
            mediaBuilder.setHeight(media.height);
            mediaBuilder.setEncryptionKey(ByteString.copyFrom(media.encKey));
            mediaBuilder.setPlaintextHash(ByteString.copyFrom(media.sha256hash));
            mediaBuilder.setDownloadUrl(media.url);
            chatMessageBuilder.addMedia(mediaBuilder.build());
        }
        if (message.text != null) {
            chatMessageBuilder.setText(message.text);
        }
        return chatMessageBuilder.build();
    }

    private static @Media.MediaType int fromProtoMediaType(@NonNull MediaType type) {
        if (type == MediaType.MEDIA_TYPE_IMAGE) {
            return Media.MEDIA_TYPE_IMAGE;
        } else if (type == MediaType.MEDIA_TYPE_VIDEO) {
            return Media.MEDIA_TYPE_VIDEO;
        }
        Log.w("Unrecognized MediaType " + type);
        return Media.MEDIA_TYPE_UNKNOWN;
    }

    private static MediaType getProtoMediaType(@Media.MediaType int type) {
        if (type == Media.MEDIA_TYPE_IMAGE) {
            return MediaType.MEDIA_TYPE_IMAGE;
        } else if (type == Media.MEDIA_TYPE_VIDEO) {
            return MediaType.MEDIA_TYPE_VIDEO;
        }
        Log.w("Unrecognized media type " + type);
        return MediaType.MEDIA_TYPE_UNSPECIFIED;
    }

    private static ChatMessage readEncodedEntryString(String entry) {
        return readEncodedEntry(Base64.decode(entry, Base64.NO_WRAP));
    }

    private static ChatMessage readEncodedEntry(byte[] entry) {
        final Container container;
        try {
            container = Container.parseFrom(entry);
        } catch (InvalidProtocolBufferException e) {
            Log.w("Error reading encoded entry", e);
            return null;
        }
        if (container.hasChatMessage()) {
           return container.getChatMessage();
        } else {
            Log.i("Unknown encoded entry type");
        }
        return null;
    }

    private static ChatMessage readEncryptedEntry(@NonNull XmlPullParser parser) throws Exception {
        final String oneTimePreKeyIdString = parser.getAttributeValue(null, ATTRIBUTE_ONE_TIME_PRE_KEY_ID);
        final String identityKeyString = parser.getAttributeValue(null, ATTRIBUTE_IDENTITY_KEY);
        final String ephemeralKeyIdString = parser.getAttributeValue(null, ATTRIBUTE_EPHEMERAL_KEY_ID);
        final String ephemeralKeyString = parser.getAttributeValue(null, ATTRIBUTE_EPHEMERAL_KEY);
        final String rawUserId = parser.getAttributeValue(null, ATTRIBUTE_SENDER_USER_ID); // TODO(jack): handle this properly, ideally passed in

        Integer oneTimePreKeyId = null;
        try {
            oneTimePreKeyId = Integer.parseInt(oneTimePreKeyIdString);
        } catch (NumberFormatException e) {
            Log.w("Got invalid one time pre key id " + oneTimePreKeyIdString);
        }
        Integer ephemeralKeyId = null;
        try {
            ephemeralKeyId = Integer.parseInt(ephemeralKeyIdString);
        } catch (NumberFormatException e) {
            Log.w("Got invalid ephemeral key id " + ephemeralKeyIdString);
        }
        final PublicECKey ephemeralKey = new PublicECKey(Base64.decode(ephemeralKeyString, Base64.NO_WRAP));
        final PublicECKey identityKey = new PublicECKey(Base64.decode(identityKeyString, Base64.NO_WRAP));

        final String encryptedEntry = Xml.readText(parser);
        final byte[] bytes = Base64.decode(encryptedEntry, Base64.NO_WRAP);
        final byte[] dec = SessionManager.getInstance().decryptMessage(bytes, new UserId(rawUserId), identityKey, ephemeralKey, ephemeralKeyId, oneTimePreKeyId);

        return readEncodedEntry(dec);
    }

    public static class Provider extends ExtensionElementProvider<ChatMessageElement> {

        @Override
        public final ChatMessageElement parse(XmlPullParser parser, int initialDepth) throws Exception {
            final String timestampStr = parser.getAttributeValue(null, "timestamp");
            long timestamp = 0;
            if (timestampStr != null) {
                timestamp = Long.parseLong(timestampStr);
            }

            ChatMessage chatMessage = null;
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                final String name = Preconditions.checkNotNull(parser.getName());
                if (name.equals(ELEMENT_ENCRYPTED)) {
                    try {
                        if (Constants.ENCRYPTION_TURNED_ON) {
                            chatMessage = readEncryptedEntry(parser);
                        }
                    } catch (Exception e) {
                        Log.w("Failed to read encrypted entry", e);
                    }
                } else if (chatMessage == null && name.equals(ELEMENT_PROTOBUF_STAGE_ONE)) {
                    chatMessage = readEncodedEntryString(Xml.readText(parser));
                } else if (name.equals("chatmessage")) { // TODO (ds): remove
                    Xml.skip(parser);
                }
            }
            return chatMessage == null ? null : new ChatMessageElement(chatMessage, timestamp * 1000L);
        }
    }
}
