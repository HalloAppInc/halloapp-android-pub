package com.halloapp.protocol.smack;

import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jxmpp.jid.Jid;

public class HalloPubsubItem extends PayloadItem<SimplePayload> {

    long timestamp;
    Jid publisher;

    public HalloPubsubItem(SimplePayload payloadExt) {
        super(payloadExt);
    }

    public HalloPubsubItem(String itemId, SimplePayload payloadExt) {
        super(itemId, payloadExt);
    }

    public HalloPubsubItem(String itemId, String nodeId, SimplePayload payloadExt) {
        super(itemId, nodeId, payloadExt);
    }

    public HalloPubsubItem(ItemNamespace itemNamespace, String itemId, String nodeId, SimplePayload payloadExt) {
        super(itemNamespace, itemId, nodeId, payloadExt);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Jid getPublisher() {
        return publisher;
    }
}
