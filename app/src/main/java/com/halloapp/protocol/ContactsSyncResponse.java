package com.halloapp.protocol;

import androidx.annotation.NonNull;

import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.IQ;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContactsSyncResponse extends IQ {

    public final static String ELEMENT = "contact_list";
    public final static String NAMESPACE = "ns:phonenumber:normalization";

    private static final String ELEMENT_CONTACT = "contact";
    private static final String ELEMENT_RAW = "raw";
    private static final String ELEMENT_NORMALIZED = "normalized";
    private static final String ELEMENT_ROLE = "role";

    public final List<Contact> contactList = new ArrayList<>();

    ContactsSyncResponse(@NonNull XmlPullParser parser) throws IOException, XmlPullParserException {
        super(ELEMENT, NAMESPACE);

        String tag = parser.getName();
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
        Contact contact = new Contact();
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

    static class Contact {
        String role;
        String phone;
        String normalizedPhone;
    }
}
