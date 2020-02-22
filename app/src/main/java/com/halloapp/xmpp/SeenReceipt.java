package com.halloapp.xmpp;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.provider.EmbeddedExtensionProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;

import java.util.List;
import java.util.Map;

public class SeenReceipt implements ExtensionElement {

    static final String NAMESPACE = "urn:xmpp:receipts";
    static final String ELEMENT = "seen";

    /**
     * original ID of the delivered message
     */
    private final String id;
    private final long timestamp;

    SeenReceipt(String id) {
        this(id, 0);
    }

    SeenReceipt(String id, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    /**
     * Get the id of the message that has been delivered.
     *
     * @return id of the delivered message or {@code null}.
     */
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
        xml.closeEmptyElement();
        return xml;
    }

    /**
     * Get the {@link org.jivesoftware.smackx.receipts.DeliveryReceipt} extension of the packet, if any.
     *
     * @param p the packet
     * @return the {@link org.jivesoftware.smackx.receipts.DeliveryReceipt} extension or {@code null}
     * @deprecated use {@link #from(Message)} instead
     */
    @Deprecated
    public static org.jivesoftware.smackx.receipts.DeliveryReceipt getFrom(Message p) {
        return from(p);
    }

    /**
     * Get the {@link org.jivesoftware.smackx.receipts.DeliveryReceipt} extension of the message, if any.
     *
     * @param message the message.
     * @return the {@link org.jivesoftware.smackx.receipts.DeliveryReceipt} extension or {@code null}
     */
    public static org.jivesoftware.smackx.receipts.DeliveryReceipt from(Message message) {
        return message.getExtension(ELEMENT, NAMESPACE);
    }

    /**
     * This Provider parses and returns DeliveryReceipt packets.
     */
    public static class Provider extends EmbeddedExtensionProvider<SeenReceipt> {

        @Override
        protected SeenReceipt createReturnExtension(String currentElement, String currentNamespace,
                Map<String, String> attributeMap, List<? extends ExtensionElement> content) {
            final String timestampStr = attributeMap.get("timestamp");
            long timestamp = 0;
            if (timestampStr != null) {
                timestamp = Long.parseLong(timestampStr);
            }
            return new SeenReceipt(attributeMap.get("id"), timestamp * 1000L);
        }

    }
}
