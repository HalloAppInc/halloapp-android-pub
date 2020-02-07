package com.halloapp.protocol.smack;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.provider.ItemProvider;
import org.jxmpp.jid.impl.JidCreate;
import org.xmlpull.v1.XmlPullParser;

public class HalloPubsubItemProvider extends ItemProvider {

    @Override
    public Item parse(XmlPullParser parser, int initialDepth) throws Exception {

        String id = parser.getAttributeValue(null, "id");
        String node = parser.getAttributeValue(null, "node");
        String timestamp = parser.getAttributeValue(null, "timestamp");
        String publisher = parser.getAttributeValue(null, "publisher");
        String xmlns = parser.getNamespace();
        Item.ItemNamespace itemNamespace = Item.ItemNamespace.fromXmlns(xmlns);

        int tag = parser.next();

        if (tag == XmlPullParser.END_TAG)  {
            return new Item(itemNamespace, id, node);
        }
        else {
            String payloadElemName = parser.getName();
            String payloadNS = parser.getNamespace();

            final ExtensionElementProvider<ExtensionElement> extensionProvider = ProviderManager.getExtensionProvider(payloadElemName, payloadNS);
            final HalloPubsubItem item;
            if (extensionProvider == null) {
                // TODO: Should we use StandardExtensionElement in this case? And probably remove SimplePayload all together.
                CharSequence payloadText = PacketParserUtils.parseElement(parser, true);
                item = new HalloPubsubItem(itemNamespace, id, node, new SimplePayload(payloadText.toString()));
            }
            else {
                item = new HalloPubsubItem(itemNamespace, id, node, (SimplePayload)extensionProvider.parse(parser));
            }
            if (publisher != null) {
                item.publisher = JidCreate.bareFrom(publisher);
            }
            if (timestamp != null) {
                item.timestamp = Long.parseLong(timestamp);
            }
            return item;
        }
    }
}
