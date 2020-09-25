package com.halloapp.xmpp;

import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.crypto.EncryptedSessionManager;
import com.halloapp.crypto.SessionSetupInfo;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;
import com.halloapp.proto.ChatMessage;
import com.halloapp.proto.Container;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.stats.Stats;
import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jxmpp.jid.Jid;
import org.xmlpull.v1.XmlPullParser;

import java.security.GeneralSecurityException;

public class ChatMessageElement implements ExtensionElement {

    static final String NAMESPACE = "halloapp:chat:messages";
    static final String ELEMENT = "chat";

    private static final String ELEMENT_PROTOBUF_STAGE_ONE = "s1";
    private static final String ELEMENT_ENCRYPTED = "enc";

    private static final String ATTRIBUTE_IDENTITY_KEY = "identity_key";
    private static final String ATTRIBUTE_ONE_TIME_PRE_KEY_ID = "one_time_pre_key_id";

    private ChatMessage chatMessage;
    private final long timestamp;
    private final UserId recipientUserId;
    private final SessionSetupInfo sessionSetupInfo;
    private final byte[] encryptedBytes;
    private ChatMessage plaintextChatMessage = null; // TODO(jack): Remove before removing s1 XML tag

    private Stats stats = Stats.getInstance();

    ChatMessageElement(@NonNull Message message, UserId recipientUserId, @Nullable SessionSetupInfo sessionSetupInfo) {
        this.chatMessage = MessageElementHelper.messageToChatMessage(message);
        this.timestamp = 0;
        this.recipientUserId = recipientUserId;
        this.sessionSetupInfo = sessionSetupInfo;
        this.encryptedBytes = null;
    }

    private ChatMessageElement(byte[] encryptedBytes, SessionSetupInfo sessionSetupInfo, long timestamp) {
        this.chatMessage = null;
        this.timestamp = timestamp;
        this.recipientUserId = null;
        this.sessionSetupInfo = sessionSetupInfo;
        this.encryptedBytes = encryptedBytes;
    }

    private ChatMessageElement(@NonNull ChatMessage chatMessage, long timestamp) {
        this.chatMessage = chatMessage;
        this.timestamp = timestamp;
        this.recipientUserId = null;
        this.sessionSetupInfo = null;
        this.encryptedBytes = null;
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

            if (sessionSetupInfo != null) {
                xml.attribute(ATTRIBUTE_IDENTITY_KEY, Base64.encodeToString(sessionSetupInfo.identityKey.getKeyMaterial(), Base64.NO_WRAP));
                if (sessionSetupInfo.oneTimePreKeyId != null) {
                    xml.attribute(ATTRIBUTE_ONE_TIME_PRE_KEY_ID, sessionSetupInfo.oneTimePreKeyId.toString());
                }
            }

            xml.rightAngleBracket();
            xml.append(getEncryptedEntryString());
            xml.closeElement(ELEMENT_ENCRYPTED);
        }

        xml.closeElement(ELEMENT);
        return xml;
    }

    Message getMessage(Jid from, String id) {
        if (Constants.ENCRYPTION_TURNED_ON && encryptedBytes != null) {
            try {
                UserId userId = new UserId(from.getLocalpartOrThrow().asUnescapedString());
                final byte[] dec = EncryptedSessionManager.getInstance().decryptMessage(this.encryptedBytes, userId, sessionSetupInfo);
                chatMessage = MessageElementHelper.readEncodedEntry(dec);
                if (plaintextChatMessage != null && !plaintextChatMessage.equals(chatMessage)) {
                    Log.sendErrorReport("Decrypted message does not match plaintext");
                    stats.reportDecryptError("plaintext mismatch");
                } else {
                    stats.reportDecryptSuccess();
                }
            } catch (GeneralSecurityException | ArrayIndexOutOfBoundsException e) {
                Log.e("Failed to decrypt message, falling back to plaintext", e);
                Log.sendErrorReport("Decryption failure");
                stats.reportDecryptError("decryption failure");
                chatMessage = plaintextChatMessage;

                if (Constants.REREQUEST_SEND_ENABLED) {
                    Log.i("Rerequesting message " + id);
                    EncryptedSessionManager.getInstance().sendRerequest(from, id);
                }
            }
        }
        String rawReplyMessageId = chatMessage.getChatReplyMessageId();
        String rawSenderId = chatMessage.getChatReplyMessageSenderId();
        final Message message = new Message(0,
                ChatId.fromString(from.getLocalpartOrNull().toString()),
                new UserId(from.getLocalpartOrNull().toString()),
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

    private String getEncodedEntryString() {
        return Base64.encodeToString(getEncodedEntry(), Base64.NO_WRAP);
    }

    private String getEncryptedEntryString() {
        try {
            byte[] encodedEntry = getEncodedEntry();
            byte[] encryptedEntry = EncryptedSessionManager.getInstance().encryptMessage(encodedEntry, recipientUserId);
            stats.reportEncryptSuccess();
            return Base64.encodeToString(encryptedEntry, Base64.NO_WRAP);
        } catch (GeneralSecurityException e) {
            Log.e("Failed to encrypt", e);
            Log.sendErrorReport("Encryption failure");
            stats.reportEncryptError("encryption failed");
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

    private static ChatMessageElement readEncryptedEntry(@NonNull XmlPullParser parser, long timestamp) throws Exception {
        final String oneTimePreKeyIdString = parser.getAttributeValue(null, ATTRIBUTE_ONE_TIME_PRE_KEY_ID);
        final String identityKeyString = parser.getAttributeValue(null, ATTRIBUTE_IDENTITY_KEY);

        Integer oneTimePreKeyId = null;
        try {
            oneTimePreKeyId = Integer.parseInt(oneTimePreKeyIdString);
        } catch (NumberFormatException e) {
            Log.w("Got invalid one time pre key id " + oneTimePreKeyIdString);
        }

        PublicEdECKey identityKey = null;
        if (identityKeyString != null) {
            identityKey = new PublicEdECKey(Base64.decode(identityKeyString, Base64.NO_WRAP));
        }

        final String encryptedEntry = Xml.readText(parser);
        final byte[] bytes = Base64.decode(encryptedEntry, Base64.NO_WRAP);

        return new ChatMessageElement(bytes, new SessionSetupInfo(identityKey, oneTimePreKeyId), timestamp);
    }

    public static class Provider extends ExtensionElementProvider<ChatMessageElement> {

        @Override
        public final ChatMessageElement parse(XmlPullParser parser, int initialDepth) throws Exception {
            final String timestampStr = parser.getAttributeValue(null, "timestamp");
            long timestamp = 0;
            if (timestampStr != null) {
                timestamp = Long.parseLong(timestampStr) * 1000L;
            }

            ChatMessage plaintextChatMessage = null;
            ChatMessageElement chatMessageElement = null;
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                final String name = Preconditions.checkNotNull(parser.getName());
                if (name.equals(ELEMENT_ENCRYPTED)) {
                    try {
                        if (Constants.ENCRYPTION_TURNED_ON) {
                            chatMessageElement = readEncryptedEntry(parser, timestamp);
                            chatMessageElement.plaintextChatMessage = plaintextChatMessage;
                        }
                    } catch (Exception e) {
                        Log.w("Failed to read encrypted entry", e);
                    }
                } else if (name.equals(ELEMENT_PROTOBUF_STAGE_ONE)) {
                    plaintextChatMessage = MessageElementHelper.readEncodedEntryString(Xml.readText(parser));
                    if (chatMessageElement == null) {
                        chatMessageElement = new ChatMessageElement(plaintextChatMessage, timestamp);
                    } else {
                        chatMessageElement.plaintextChatMessage = plaintextChatMessage;
                    }
                } else if (name.equals("chatmessage")) { // TODO (ds): remove
                    Xml.skip(parser);
                }
            }
            return chatMessageElement;
        }
    }
}
