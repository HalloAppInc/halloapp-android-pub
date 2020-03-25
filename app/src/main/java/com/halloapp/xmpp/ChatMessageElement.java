package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.contacts.UserId;
import com.halloapp.content.Media;
import com.halloapp.content.Message;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jxmpp.jid.Jid;
import org.xmlpull.v1.XmlPullParser;

public class ChatMessageElement implements ExtensionElement {

    static final String NAMESPACE = "halloapp:chat:messages";
    static final String ELEMENT = "chat";

    private final PublishedEntry entry;
    private final long timestamp;

    ChatMessageElement(@NonNull PublishedEntry entry) {
        this(entry, 0);
    }

    private ChatMessageElement(@NonNull PublishedEntry entry, long timestamp) {
        this.entry = entry;
        this.timestamp = timestamp;
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
        xml.append(entry.toXml());
        xml.closeElement(ELEMENT);
        return xml;
    }

    public static ChatMessageElement from(org.jivesoftware.smack.packet.Message message) {
        return message.getExtension(ELEMENT, NAMESPACE);
    }

    Message getMessage(Jid from, String id) {
        final Message message = new Message(0,
                from.getLocalpartOrNull().toString(),
                new UserId(from.getLocalpartOrNull().toString()),
                id,
                timestamp,
                false,
                Message.SEEN_NO,
                entry.text);
        for (PublishedEntry.Media entryMedia : entry.media) {
            message.media.add(Media.createFromUrl(PublishedEntry.getMediaType(entryMedia.type), entryMedia.url,
                    entryMedia.encKey, entryMedia.sha256hash,
                    entryMedia.width, entryMedia.height));
        }

        return message;
    }

    public static class Provider extends ExtensionElementProvider<ChatMessageElement> {

        @Override
        public final ChatMessageElement parse(XmlPullParser parser, int initialDepth) throws Exception {
            final String timestampStr = parser.getAttributeValue(null, "timestamp");
            long timestamp = 0;
            if (timestampStr != null) {
                timestamp = Long.parseLong(timestampStr);
            }
            parser.nextTag();
            final PublishedEntry.Builder entryBuilder = PublishedEntry.readEntry(parser);
            entryBuilder.timestamp(timestamp * 1000L);
            return new ChatMessageElement(entryBuilder.build(), timestamp * 1000L);
        }
    }
}
