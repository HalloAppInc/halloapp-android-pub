package com.halloapp.xmpp;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrivacyListsRequestIq extends IQ {

    public static final String ELEMENT = "privacy_lists";
    public static final String NAMESPACE = "halloapp:user:privacy";

    static final String ELEMENT_PRIVACY_LIST = "privacy_list";
    static final String ATTRIBUTE_TYPE = "type";

    private List<String> requestedTypes = new ArrayList<>();

    public PrivacyListsRequestIq(Jid to, @PrivacyList.Type String... types) {
        super(ELEMENT, NAMESPACE);
        setTo(to);
        setType(Type.get);
        requestedTypes.addAll(Arrays.asList(types));
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.xmlnsAttribute(NAMESPACE);
        xml.rightAngleBracket();
        for (String type : requestedTypes) {
            xml.halfOpenElement(ELEMENT_PRIVACY_LIST);
            xml.xmlnsAttribute(NAMESPACE);
            xml.attribute(ATTRIBUTE_TYPE, type);
            xml.closeEmptyElement();
        }
        return xml;
    }
}
