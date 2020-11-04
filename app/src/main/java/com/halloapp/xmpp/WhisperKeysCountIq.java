package com.halloapp.xmpp;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.WhisperKeys;

public class WhisperKeysCountIq extends HalloIq {

    public static final String ELEMENT = "whisper_keys";
    public static final String NAMESPACE = "halloapp:whisper:keys";

    public Integer count;

    WhisperKeysCountIq() {
        super(ELEMENT, NAMESPACE);
        setType(Type.get);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {

        xml.attribute("type", "count");

        xml.rightAngleBracket();
        return xml;
    }

    @Override
    public Iq toProtoIq() {
        return Iq.newBuilder()
                .setId(getStanzaId())
                .setType(Iq.Type.GET)
                .setWhisperKeys(WhisperKeys.newBuilder().setAction(WhisperKeys.Action.COUNT).build())
                .build();
    }
}

