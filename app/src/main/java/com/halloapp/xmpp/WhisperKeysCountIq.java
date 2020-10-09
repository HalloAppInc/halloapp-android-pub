package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.WhisperKeys;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;

public class WhisperKeysCountIq extends HalloIq {

    public static final String ELEMENT = "whisper_keys";
    public static final String NAMESPACE = "halloapp:whisper:keys";

    public Integer count;

    WhisperKeysCountIq(@NonNull Jid to) {
        super(ELEMENT, NAMESPACE);
        setType(Type.get);
        setTo(to);
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

