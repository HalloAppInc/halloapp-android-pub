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

    private static final String ATTRIBUTE_THREAD_ID = "thread_id";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_TIMESTAMP = "timestamp";

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

    @Nullable String getThreadId() {
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
        xml.optAttribute(ATTRIBUTE_ID, getId());
        xml.optAttribute(ATTRIBUTE_THREAD_ID, threadId);
        xml.closeEmptyElement();
        return xml;
    }

    public static class Provider extends EmbeddedExtensionProvider<DeliveryReceiptElement> {

        @Override
        protected DeliveryReceiptElement createReturnExtension(String currentElement, String currentNamespace, Map<String, String> attributeMap, List<? extends ExtensionElement> content) {
            final String timestampStr = attributeMap.get(ATTRIBUTE_TIMESTAMP);
            long timestamp = 0;
            if (timestampStr != null) {
                timestamp = Long.parseLong(timestampStr);
            }
            return new DeliveryReceiptElement(attributeMap.get(ATTRIBUTE_THREAD_ID), attributeMap.get(ATTRIBUTE_ID), timestamp * 1000L);
        }
    }
}
