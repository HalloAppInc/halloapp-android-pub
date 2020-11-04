package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.PushRegister;
import com.halloapp.proto.server.PushToken;

import org.jivesoftware.smack.packet.IQ;

public class PushRegisterRequestIq extends HalloIq {

    private static final String ELEMENT = "push_register";
    private static final String NAMESPACE = "halloapp:push:notifications";

    private static final String ELEMENT_TOKEN = "push_token";

    private final String token;

    PushRegisterRequestIq(@NonNull String token) {
        super(ELEMENT, NAMESPACE);
        setType(IQ.Type.set);
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

    @Override
    public Iq toProtoIq() {
        return Iq.newBuilder()
                .setId(getStanzaId())
                .setType(Iq.Type.SET)
                .setPushRegister(
                        PushRegister.newBuilder()
                                .setPushToken(
                                        PushToken.newBuilder()
                                                .setOs(PushToken.Os.ANDROID)
                                                .setToken(token)
                                                .build())
                                .build())
                .build();
    }
}

