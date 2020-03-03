package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class ContactsSyncResponseIq extends IQ {

    final static String ELEMENT = "contact_list";
    final static String NAMESPACE = "halloapp:user:contacts";

    final ContactList contactList;

    private ContactsSyncResponseIq(@NonNull XmlPullParser parser) throws IOException, XmlPullParserException {
        super(ELEMENT, NAMESPACE);
        contactList = new ContactList(parser);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        return null;
    }

    public static class Provider extends IQProvider<ContactsSyncResponseIq> {

        @Override
        public ContactsSyncResponseIq parse(XmlPullParser parser, int initialDepth) throws IOException, XmlPullParserException {
            return new ContactsSyncResponseIq(parser);
        }
    }
}
