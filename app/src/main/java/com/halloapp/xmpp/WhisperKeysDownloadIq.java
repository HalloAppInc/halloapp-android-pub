package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;

public class WhisperKeysDownloadIq extends IQ {

    private static final String ELEMENT = "whisper_keys";
    private static final String NAMESPACE = "halloapp:whisper:keys";

    private static final String ATTRIBUTE_USERNAME = "username";

    private final String forUser;

    WhisperKeysDownloadIq(@NonNull Jid to, @NonNull String forUser) {
        super(ELEMENT, NAMESPACE);
        setType(Type.get);
        setTo(to);
        this.forUser = forUser;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {

        xml.attribute(ATTRIBUTE_USERNAME, forUser);
        xml.attribute("type", "get");

        xml.rightAngleBracket();
        return xml;
    }
}

