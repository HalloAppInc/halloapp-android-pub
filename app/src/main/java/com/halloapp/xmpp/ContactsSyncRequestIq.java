package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;

import java.util.Collection;

public class ContactsSyncRequestIq extends IQ {

    private final static String ELEMENT = "contact_list";
    private final static String NAMESPACE = "halloapp:user:contacts";

    private static final String ELEMENT_CONTACT = "contact";
    private static final String ELEMENT_RAW = "raw";
    private static final String ELEMENT_NORMALIZED = "normalized";

    private final @ContactSyncRequest.Type String type;
    private final Collection<String> phones;

    ContactsSyncRequestIq(@NonNull Jid to, @NonNull Collection<String> phones, @ContactSyncRequest.Type String type) {
        super(ELEMENT, NAMESPACE);
        setType(IQ.Type.set);
        setTo(to);
        this.type = type;
        this.phones = phones;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {

        final String phoneElement = ContactSyncRequest.DELETE.equals(type) ? ELEMENT_NORMALIZED : ELEMENT_RAW;
        xml.attribute("type", type);
        xml.rightAngleBracket();
        for (String phone : phones) {
            xml.openElement(ELEMENT_CONTACT);
            xml.openElement(phoneElement);
            xml.append(phone);
            xml.closeElement(phoneElement);
            xml.closeElement(ELEMENT_CONTACT);
        }
        return xml;
    }
}
