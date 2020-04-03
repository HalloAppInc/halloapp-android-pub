package com.halloapp.xmpp;

import androidx.annotation.Nullable;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.provider.EmbeddedExtensionProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;

import java.util.List;
import java.util.Map;

public class DeliveryReceiptElement extends DeliveryReceipt {
    static final String NAMESPACE = "urn:xmpp:receipts";
    static final String ELEMENT = "received";

    private final String threadId;
    private final long timestamp;

    DeliveryReceiptElement(String threadId, String id) {
        this(threadId, id, 0);
    }

    private DeliveryReceiptElement(String threadId, String id, long timestamp) {
        super(id);
        this.threadId = threadId;
        this.timestamp = timestamp;
    }

    public static DeliveryReceiptElement from(Message message) {
        return message.getExtension(ELEMENT, NAMESPACE);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public @Nullable String getThreadId() {
        return threadId;
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
        xml.optAttribute("id", getId());
        xml.optAttribute("threadid", threadId);
        xml.closeEmptyElement();
        return xml;
    }

    public static class Provider extends EmbeddedExtensionProvider<DeliveryReceiptElement> {

        @Override
        protected DeliveryReceiptElement createReturnExtension(String currentElement, String currentNamespace, Map<String, String> attributeMap, List<? extends ExtensionElement> content) {
            final String timestampStr = attributeMap.get("timestamp");
            long timestamp = 0;
            if (timestampStr != null) {
                timestamp = Long.parseLong(timestampStr);
            }
            return new DeliveryReceiptElement(attributeMap.get("threadid"), attributeMap.get("id"), timestamp * 1000L);
        }
    }
}
