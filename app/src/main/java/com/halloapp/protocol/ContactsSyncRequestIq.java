package com.halloapp.protocol;

import androidx.annotation.NonNull;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;

import java.util.Collection;

public class ContactsSyncRequestIq extends IQ {

    public final static String ELEMENT = "contact_list";
    public final static String NAMESPACE = "ns:phonenumber:normalization";

    private static final String ELEMENT_CONTACT = "contact";
    private static final String ELEMENT_RAW = "raw";

    private final Collection<String> phones;

    public ContactsSyncRequestIq(@NonNull Jid to, @NonNull Collection<String> phones) {
        super(ELEMENT, NAMESPACE);
        setType(IQ.Type.get);
        setTo(to);
        this.phones = phones;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {

        xml.rightAngleBracket();
        for (String phone : phones) {
            xml.openElement(ELEMENT_CONTACT);
            xml.openElement(ELEMENT_RAW);
            xml.append(phone);
            xml.closeElement(ELEMENT_RAW);
            xml.closeElement(ELEMENT_CONTACT);
        }
        return xml;
    }
}
