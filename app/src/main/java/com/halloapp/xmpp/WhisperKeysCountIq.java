package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jxmpp.jid.Jid;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WhisperKeysCountIq extends IQ {

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
}

