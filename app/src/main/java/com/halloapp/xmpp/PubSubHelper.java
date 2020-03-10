package com.halloapp.xmpp;

import androidx.annotation.NonNull;
import androidx.core.util.Preconditions;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.pubsub.GetItemsRequest;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemsExtension;
import org.jivesoftware.smackx.pubsub.PubSubElementType;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishItem;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.packet.PubSub;
import org.jxmpp.jid.impl.JidCreate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PubSubHelper {

    private final XMPPConnection connection;
    private final PubSubManager pubSubManager;

    PubSubHelper(@NonNull XMPPConnection connection) {
        this.connection = connection;
        pubSubManager = PubSubManager.getInstance(connection, JidCreate.bareFromOrThrowUnchecked("pubsub." + connection.getXMPPServiceDomain()));
    }

    void publishItem(@NonNull String nodeId, @NonNull Item item) throws SmackException.NotConnectedException, InterruptedException, XMPPException.XMPPErrorException, SmackException.NoResponseException {
        Preconditions.checkNotNull(connection);
        final PubSub packet = PubSub.createPubsubPacket(pubSubManager.getServiceJid(), IQ.Type.set, new PublishItem<>(nodeId, Collections.singletonList(item)));
        connection.createStanzaCollectorAndSend(packet).nextResultOrThrow();
    }

    void retractItem(@NonNull String nodeId, @NonNull Item item) throws SmackException.NotConnectedException, InterruptedException, XMPPException.XMPPErrorException, SmackException.NoResponseException {
        Preconditions.checkNotNull(connection);
        final PubSub packet = PubSub.createPubsubPacket(pubSubManager.getServiceJid(), IQ.Type.set, new RetractItem<>(nodeId, Collections.singletonList(item)));
        // TODO (ds): uncomment when server implements 'retract'
        //connection.createStanzaCollectorAndSend(packet).nextResultOrThrow();
    }

    List<PubsubItem> getItems(@NonNull String nodeId) throws SmackException.NotConnectedException, InterruptedException, XMPPException.XMPPErrorException, SmackException.NoResponseException {
        Preconditions.checkNotNull(connection);
        final PubSub packet = PubSub.createPubsubPacket(pubSubManager.getServiceJid(), IQ.Type.get, new GetItemsRequest(nodeId));
        return getPubsubItems(connection.createStanzaCollectorAndSend(packet).nextResultOrThrow());
    }

    List<PubsubItem> getItems(@NonNull String nodeId, Collection<String> ids) throws SmackException.NotConnectedException, InterruptedException, XMPPException.XMPPErrorException, SmackException.NoResponseException {
        Preconditions.checkNotNull(connection);
        final List<Item> itemList = new ArrayList<>(ids.size());
        for (String id : ids) {
            itemList.add(new Item(id));
        }
        final PubSub packet = PubSub.createPubsubPacket(pubSubManager.getServiceJid(), IQ.Type.get, new ItemsExtension(ItemsExtension.ItemsElementType.items, nodeId, itemList));
        return getPubsubItems(connection.createStanzaCollectorAndSend(packet).nextResultOrThrow());
    }

    List<PubsubItem> getItems(@NonNull String nodeId, int maxItems) throws SmackException.NotConnectedException, InterruptedException, XMPPException.XMPPErrorException, SmackException.NoResponseException {
        Preconditions.checkNotNull(connection);
        final PubSub packet = PubSub.createPubsubPacket(pubSubManager.getServiceJid(), IQ.Type.get, new GetItemsRequest(nodeId, maxItems));
        return getPubsubItems(connection.createStanzaCollectorAndSend(packet).nextResultOrThrow());
    }

    List<Subscription> getSubscriptions() throws XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
        return pubSubManager.getSubscriptions();
    }

    private List<PubsubItem> getPubsubItems(PubSub result) {
        final ItemsExtension itemsElem = result.getExtension(PubSubElementType.ITEMS);
        return (List<PubsubItem>) itemsElem.getItems();
    }
}
