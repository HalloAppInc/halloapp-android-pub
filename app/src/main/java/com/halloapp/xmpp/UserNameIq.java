package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.Name;

import org.jivesoftware.smack.packet.IQ;

public class UserNameIq extends HalloIq {

    private static final String ELEMENT = "name";
    private static final String NAMESPACE = "halloapp:users:name";

    private static final String ELEMENT_TOKEN = "push_token";

    private final String name;

    UserNameIq(@NonNull String name) {
        super(ELEMENT, NAMESPACE);
        setType(IQ.Type.set);
        this.name = name;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.append(name);
        return xml;
    }

    @Override
    public Iq toProtoIq() {
        return Iq.newBuilder()
                .setId(getStanzaId())
                .setType(Iq.Type.SET)
                .setName(Name.newBuilder().setName(name).build())
                .build();
    }
}
