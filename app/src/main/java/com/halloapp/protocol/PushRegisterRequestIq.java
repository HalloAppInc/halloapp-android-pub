package com.halloapp.protocol;

import androidx.annotation.NonNull;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;

public class PushRegisterRequestIq extends IQ {

    private static final String ELEMENT = "push_register";
    private static final String NAMESPACE = "halloapp:push:notifications";

    private static final String ELEMENT_TOKEN = "push_token";

    private final String token;

    public PushRegisterRequestIq(@NonNull Jid to, @NonNull String token) {
        super(ELEMENT, NAMESPACE);
        setType(IQ.Type.set);
        setTo(to);
        this.token = token;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {

        xml.rightAngleBracket();
        xml.halfOpenElement(ELEMENT_TOKEN);
        xml.attribute("os", "android");
        xml.rightAngleBracket();
        xml.append(token);
        xml.closeElement(ELEMENT_TOKEN);
        return xml;
    }
}

