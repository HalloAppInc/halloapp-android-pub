package com.halloapp.xmpp.privacy;

import org.jivesoftware.smack.packet.IQ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrivacyListsRequestIq extends IQ {

    public static final String ELEMENT = "privacy_lists";
    public static final String NAMESPACE = "halloapp:user:privacy";

    static final String ELEMENT_PRIVACY_LIST = "privacy_list";
    static final String ATTRIBUTE_TYPE = "type";

    private List<String> requestedTypes = new ArrayList<>();

    protected PrivacyListsRequestIq(@PrivacyList.Type String... types) {
        super(ELEMENT, NAMESPACE);
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
