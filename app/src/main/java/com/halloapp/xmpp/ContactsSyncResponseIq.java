package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContactsSyncResponseIq extends IQ {

    final static String ELEMENT = "contact_list";
    final static String NAMESPACE = "halloapp:user:contacts";

    private static final String ELEMENT_CONTACT = "contact";
    private static final String ELEMENT_USER_ID = "userid";
    private static final String ELEMENT_ROLE = "role";
    private static final String ELEMENT_RAW = "raw";
    private static final String ELEMENT_NORMALIZED = "normalized";

    final List<Contact> contactList = new ArrayList<>();

    private ContactsSyncResponseIq(@NonNull XmlPullParser parser) throws IOException, XmlPullParserException {
        super(ELEMENT, NAMESPACE);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            if (ELEMENT_CONTACT.equals(name)) {
                contactList.add(parseContact(parser));
            } else {
                Xml.skip(parser);
            }
        }
    }

    private Contact parseContact(XmlPullParser parser) throws IOException, XmlPullParserException {
        final Contact contact = new Contact();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            if (ELEMENT_RAW.equals(name)) {
                contact.phone = Xml.readText(parser);
            } else if (ELEMENT_NORMALIZED.equals(name)) {
                contact.normalizedPhone = Xml.readText(parser);
            } else if (ELEMENT_ROLE.equals(name)) {
                contact.role = Xml.readText(parser);
            } else if (ELEMENT_USER_ID.equals(name)) {
                contact.userId = Xml.readText(parser);
            } else {
                Xml.skip(parser);
            }
        }
        return contact;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        return null;
    }

    public static class Contact {
        public String userId;
        public String role;
        public String phone;
        public String normalizedPhone;
    }

    public static class Provider extends IQProvider<ContactsSyncResponseIq> {

        @Override
        public ContactsSyncResponseIq parse(XmlPullParser parser, int initialDepth) throws IOException, XmlPullParserException {
            return new ContactsSyncResponseIq(parser);
        }
    }
}
