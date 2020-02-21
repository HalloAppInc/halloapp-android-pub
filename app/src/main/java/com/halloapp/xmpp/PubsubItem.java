package com.halloapp.xmpp;

import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jxmpp.jid.Jid;

public class PubsubItem extends PayloadItem<SimplePayload> {

    long timestamp;
    Jid publisher;

    PubsubItem(ItemNamespace itemNamespace, String itemId, String nodeId, SimplePayload payloadExt) {
        super(itemNamespace, itemId, nodeId, payloadExt);
    }

    public long getTimestamp() {
        return timestamp;
    }

    Jid getPublisher() {
        return publisher;
    }
}
