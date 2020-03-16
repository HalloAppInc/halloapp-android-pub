package com.halloapp.xmpp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    private final String syncId;
    private final boolean lastBatch;
    private final Collection<String> phones;

    ContactsSyncRequestIq(@NonNull Jid to, @NonNull Collection<String> phones,
                          @ContactSyncRequest.Type String type, @Nullable String syncId, boolean lastBatch) {
        super(ELEMENT, NAMESPACE);
        setType(IQ.Type.set);
        setTo(to);
        this.phones = phones;
        this.type = type;
        this.syncId = syncId;
        this.lastBatch = lastBatch;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {

        final String phoneElement = ContactSyncRequest.DELETE.equals(type) ? ELEMENT_NORMALIZED : ELEMENT_RAW;
        xml.attribute("type", type);
        if (syncId != null) {
            xml.attribute("syncid", syncId);
            xml.attribute("last", lastBatch);
        }
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
