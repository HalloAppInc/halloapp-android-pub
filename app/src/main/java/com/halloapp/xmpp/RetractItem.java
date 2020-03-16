package com.halloapp.xmpp;

import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.NodeExtension;
import org.jivesoftware.smackx.pubsub.PubSubElementType;

import java.util.ArrayList;
import java.util.Collection;

public class RetractItem<T extends Item> extends NodeExtension {

    protected final Collection<T> items;

    /**
     * Construct a request to publish an item to a node.
     *
     * @param nodeId The node to publish to
     * @param toPublish The {@link Item} to publish
     */
    public RetractItem(String nodeId, T toPublish) {
        super(PubSubElementType.RETRACT, nodeId);
        items = new ArrayList<>(1);
        items.add(toPublish);
    }

    /**
     * Construct a request to publish multiple items to a node.
     *
     * @param nodeId The node to publish to
     * @param toPublish The list of {@link Item} to publish
     */
    public RetractItem(String nodeId, Collection<T> toPublish) {
        super(PubSubElementType.RETRACT, nodeId);
        items = toPublish;
    }

    @Override
    public String toXML(String enclosingNamespace) {
        StringBuilder builder = new StringBuilder("<");
        builder.append(getElementName());
        builder.append(" node='");
        builder.append(getNode());
        builder.append("'");
        //builder.append(" notify='true'");
        builder.append(">");

        for (Item item : items) {
            builder.append(item.toXML(null));
        }
        builder.append("</retract>");

        return builder.toString();
    }
}

