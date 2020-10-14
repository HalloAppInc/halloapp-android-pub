package com.halloapp.xmpp;

import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContactList implements ExtensionElement {

    static final String NAMESPACE = "halloapp:user:contacts";
    static final String ELEMENT = "contact_list";

    private static final String ELEMENT_CONTACT = "contact";
    private static final String ELEMENT_CONTACT_HASH = "contact_hash";

    final List<ContactInfo> contacts = new ArrayList<>();
    final List<String> contactHashes = new ArrayList<>();

    ContactList(XmlPullParser parser) throws IOException, XmlPullParserException {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            if (ELEMENT_CONTACT.equals(name)) {
                contacts.add(new ContactInfo(parser));
            } else if (ELEMENT_CONTACT_HASH.equals(name)) {
                String hash = Xml.readText(parser);
                contactHashes.add(hash);
            } else {
                Xml.skip(parser);
            }
        }
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public CharSequence toXML(String enclosingNamespace) {
        return null;
    }

    public static class Provider extends ExtensionElementProvider<ContactList> {

        @Override
        public final ContactList parse(XmlPullParser parser, int initialDepth) throws IOException, XmlPullParserException {
            return new ContactList(parser);
        }
    }
}
