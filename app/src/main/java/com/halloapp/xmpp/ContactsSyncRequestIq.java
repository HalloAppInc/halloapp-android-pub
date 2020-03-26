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

    private final boolean isFullSync;
    private final String syncId;
    private final int index;
    private final boolean lastBatch;
    private final Collection<String> addPhones;
    private final Collection<String> deletePhones;

    ContactsSyncRequestIq(@NonNull Jid to, @Nullable Collection<String> addPhones, @Nullable Collection<String> deletePhones,
                          boolean isFullSync, @Nullable String syncId, int index, boolean lastBatch) {
        super(ELEMENT, NAMESPACE);
        setType(IQ.Type.set);
        setTo(to);
        this.addPhones = addPhones;
        this.deletePhones = deletePhones;
        this.isFullSync = isFullSync;
        this.index = index;
        this.syncId = syncId;
        this.lastBatch = lastBatch;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {

        xml.attribute("type", isFullSync ? "full" : "delta");
        if (syncId != null) {
            xml.attribute("syncid", syncId);
            xml.attribute("index", index);
            xml.attribute("last", lastBatch);
        }
        xml.rightAngleBracket();
        if (deletePhones != null && !deletePhones.isEmpty()) {
            for (String phone : deletePhones) {
                xml.halfOpenElement(ELEMENT_CONTACT);
                xml.attribute("type", "delete");
                xml.rightAngleBracket();
                xml.openElement(ELEMENT_NORMALIZED);
                xml.append(phone);
                xml.closeElement(ELEMENT_NORMALIZED);
                xml.closeElement(ELEMENT_CONTACT);
            }
        }
        if (addPhones != null && !addPhones.isEmpty()) {
            for (String phone : addPhones) {
                xml.halfOpenElement(ELEMENT_CONTACT);
                xml.attribute("type", "add");
                xml.rightAngleBracket();
                xml.openElement(ELEMENT_RAW);
                xml.append(phone);
                xml.closeElement(ELEMENT_RAW);
                xml.closeElement(ELEMENT_CONTACT);
            }
        }
        return xml;
    }
}
