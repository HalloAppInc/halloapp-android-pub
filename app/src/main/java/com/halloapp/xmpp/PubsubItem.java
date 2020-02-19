package com.halloapp.xmpp;

import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jxmpp.jid.Jid;

public class PubsubItem extends PayloadItem<SimplePayload> {

    long timestamp;
    Jid publisher;

    public PubsubItem(SimplePayload payloadExt) {
        super(payloadExt);
    }

    public PubsubItem(String itemId, SimplePayload payloadExt) {
        super(itemId, payloadExt);
    }

    public PubsubItem(String itemId, String nodeId, SimplePayload payloadExt) {
        super(itemId, nodeId, payloadExt);
    }

    public PubsubItem(ItemNamespace itemNamespace, String itemId, String nodeId, SimplePayload payloadExt) {
        super(itemNamespace, itemId, nodeId, payloadExt);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Jid getPublisher() {
        return publisher;
    }
}
