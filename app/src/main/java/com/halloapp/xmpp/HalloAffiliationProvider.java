package com.halloapp.xmpp;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.util.ParserUtils;
import org.jivesoftware.smackx.pubsub.Affiliation;
import org.jivesoftware.smackx.pubsub.provider.AffiliationProvider;
import org.jxmpp.jid.BareJid;
import org.xmlpull.v1.XmlPullParser;

// smack doesn't handle affiliation='publish-only' type
public class HalloAffiliationProvider extends AffiliationProvider {

    @Override
    public Affiliation parse(XmlPullParser parser, int initialDepth) throws Exception {

        String node = parser.getAttributeValue(null, "node");
        BareJid jid = ParserUtils.getBareJidAttribute(parser);
        String namespaceString = parser.getNamespace();
        Affiliation.AffiliationNamespace namespace = Affiliation.AffiliationNamespace.fromXmlns(namespaceString);

        String affiliationString = parser.getAttributeValue(null, "affiliation");
        Affiliation.Type affiliationType = null;
        if (affiliationString != null && !"publish-only".equals(affiliationString)) {
            affiliationType = Affiliation.Type.valueOf(affiliationString);
        }
        Affiliation affiliation;
        if (node != null && jid == null) {
            // affiliationType may be empty
            affiliation = new Affiliation(node, affiliationType, namespace);
        }
        else if (node == null && jid != null) {
            affiliation = new Affiliation(jid, affiliationType, namespace);
        }
        else {
            throw new SmackException("Invalid affiliation. Either one of 'node' or 'jid' must be set"
                    + ". Node: " + node
                    + ". Jid: " + jid
                    + '.');
        }
        return affiliation;
    }
}
