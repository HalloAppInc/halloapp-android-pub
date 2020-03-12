package com.halloapp.xmpp;

import androidx.annotation.StringDef;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.provider.ItemProvider;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.xmlpull.v1.XmlPullParser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// org.jivesoftware.smackx.pubsub.provider.ItemProvider doesn't handle 'publisher' and 'timestamp' attributes
public class PubSubItem extends PayloadItem<SimplePayload> {

    private long timestamp;
    private Jid publisher;
    private @ItemType String type;

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            PUB_SUB_ITEM_TYPE_FEED_POST,
            PUB_SUB_ITEM_TYPE_COMMENT
    })
    @interface ItemType {}
    static final String PUB_SUB_ITEM_TYPE_FEED_POST = "feedpost";
    static final String PUB_SUB_ITEM_TYPE_COMMENT = "comment";

    PubSubItem(@ItemType String type, String itemId, SimplePayload payloadExt) {
        super(itemId, payloadExt);
        this.type = type;
    }

    private PubSubItem(ItemNamespace itemNamespace, String itemId, String nodeId, SimplePayload payloadExt) {
        super(itemNamespace, itemId, nodeId, payloadExt);
    }

    long getTimestamp() {
        return timestamp;
    }

    Jid getPublisher() {
        return publisher;
    }

    @Override
    public XmlStringBuilder toXML(String enclosingNamespace) {
        XmlStringBuilder xml = getCommonXml();
        xml.optAttribute("type", type);
        xml.rightAngleBracket();
        xml.append(getPayload().toXML(null));
        xml.closeElement(this);

        return xml;
    }

    public static class Provider extends ItemProvider {

        @Override
        public Item parse(XmlPullParser parser, int initialDepth) throws Exception {

            String id = parser.getAttributeValue(null, "id");
            String node = parser.getAttributeValue(null, "node");
            String timestamp = parser.getAttributeValue(null, "timestamp");
            String publisher = parser.getAttributeValue(null, "publisher");
            String xmlns = parser.getNamespace();
            Item.ItemNamespace itemNamespace = Item.ItemNamespace.fromXmlns(xmlns);

            int tag = parser.next();

            final PubSubItem item;
            if (tag == XmlPullParser.END_TAG)  {
                item = new PubSubItem(itemNamespace, id, node, new SimplePayload("<entry xmlns='http://halloapp.com/published-entry'><feedpost/></entry>")); // TODO (ds): come up with better solution
            }
            else {
                String payloadElemName = parser.getName();
                String payloadNS = parser.getNamespace();

                final ExtensionElementProvider<ExtensionElement> extensionProvider = ProviderManager.getExtensionProvider(payloadElemName, payloadNS);
                if (extensionProvider == null) {
                    // TODO: Should we use StandardExtensionElement in this case? And probably remove SimplePayload all together.
                    CharSequence payloadText = PacketParserUtils.parseElement(parser, true);
                    item = new PubSubItem(itemNamespace, id, node, new SimplePayload(payloadText.toString()));
                }
                else {
                    item = new PubSubItem(itemNamespace, id, node, (SimplePayload)extensionProvider.parse(parser));
                }
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
