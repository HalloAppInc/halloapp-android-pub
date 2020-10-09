package com.halloapp.xmpp.props;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.Props;
import com.halloapp.xmpp.HalloIq;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;

public class ServerPropsRequestIq extends HalloIq {

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

    @Override
    public Iq toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setId(getStanzaId())
                .setProps(Props.newBuilder().build())
                .build();
    }
}
