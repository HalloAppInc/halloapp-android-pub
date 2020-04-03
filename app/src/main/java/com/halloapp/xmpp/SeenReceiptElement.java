package com.halloapp.xmpp;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.provider.EmbeddedExtensionProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;

import java.util.List;
import java.util.Map;

public class SeenReceiptElement implements ExtensionElement {

    static final String NAMESPACE = "urn:xmpp:receipts";
    static final String ELEMENT = "seen";

    private final String threadId;
    private final String id;
    private final long timestamp;

    SeenReceiptElement(String threadId, String id) {
        this(threadId, id, 0);
    }

    private SeenReceiptElement(String threadId, String id, long timestamp) {
        this.threadId = threadId;
        this.id = id;
        this.timestamp = timestamp;
    }

    public static SeenReceiptElement from(Message message) {
        return message.getExtension(ELEMENT, NAMESPACE);
    }

    public String getThreadId() {
        return threadId;
    }

    public String getId() {
        return id;
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
        xml.optAttribute("id", id);
        xml.optAttribute("threadid", threadId);
        xml.closeEmptyElement();
        return xml;
    }

    public static class Provider extends EmbeddedExtensionProvider<SeenReceiptElement> {

        @Override
        protected SeenReceiptElement createReturnExtension(String currentElement, String currentNamespace, Map<String, String> attributeMap, List<? extends ExtensionElement> content) {
            final String timestampStr = attributeMap.get("timestamp");
            long timestamp = 0;
            if (timestampStr != null) {
                timestamp = Long.parseLong(timestampStr);
            }
            return new SeenReceiptElement(attributeMap.get("threadid"), attributeMap.get("id"), timestamp * 1000L);
        }
    }
}
