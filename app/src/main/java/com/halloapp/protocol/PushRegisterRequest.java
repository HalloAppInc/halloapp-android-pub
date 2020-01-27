package com.halloapp.protocol;

import androidx.annotation.NonNull;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;

public class PushRegisterRequest extends IQ {

    public final static String ELEMENT = "push_register";
    public final static String NAMESPACE = "halloapp:push:notifications";
    private static final String ELEMENT_TOKEN = "push_token";
    private String token;

    public PushRegisterRequest(@NonNull Jid to, @NonNull String token) {
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

