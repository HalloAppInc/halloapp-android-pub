package com.halloapp.xmpp.props;

import androidx.annotation.NonNull;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;

public class ServerPropsRequestIq extends IQ {

    public static final String ELEMENT = "props";
    public static final String NAMESPACE = "halloapp:props";

    public ServerPropsRequestIq() {
        super(ELEMENT, NAMESPACE);
        setType(Type.get);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.xmlnsAttribute(NAMESPACE);
        xml.setEmptyElement();
        return xml;
    }
}
