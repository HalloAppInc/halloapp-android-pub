package com.halloapp.xmpp;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WhisperKeysResponseIq extends IQ {

    public static final String ELEMENT = "whisper_keys";
    public static final String NAMESPACE = "halloapp:whisper:keys";

    private static final String ELEMENT_KEY_COUNT = "otp_key_count";
    private static final String ELEMENT_IDENTITY_KEY_TOKEN = "identity_key";
    private static final String ELEMENT_SIGNED_PRE_KEY_TOKEN = "signed_key";
    private static final String ELEMENT_ONE_TIME_PRE_KEY_TOKEN = "one_time_key";

    public Integer count;
    public byte[] identityKey;
    public byte[] signedPreKey;
    public List<byte[]> oneTimePreKeys;

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        return xml;
    }

    private WhisperKeysResponseIq(@NonNull XmlPullParser parser) throws IOException, XmlPullParserException {
        super(ELEMENT, NAMESPACE);

        List<byte[]> list = new ArrayList<>();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            if (ELEMENT_KEY_COUNT.equals(name)) {
                count = Integer.parseInt(Xml.readText(parser));
            } else if (ELEMENT_IDENTITY_KEY_TOKEN.equals(name)) {
                identityKey = decode(Xml.readText(parser));
            } else if (ELEMENT_SIGNED_PRE_KEY_TOKEN.equals(name)) {
                signedPreKey = decode(Xml.readText(parser));
            } else if (ELEMENT_ONE_TIME_PRE_KEY_TOKEN.equals(name)) {
                list.add(decode(Xml.readText(parser)));
            } else {
                Xml.skip(parser);
            }
        }

        oneTimePreKeys = list;
    }

    public static class Provider extends IQProvider<WhisperKeysResponseIq> {

        @Override
        public WhisperKeysResponseIq parse(XmlPullParser parser, int initialDepth) throws IOException, XmlPullParserException {
            return new WhisperKeysResponseIq(parser);
        }
    }

    private static byte[] decode(String s) {
        return Base64.decode(s, Base64.NO_WRAP);
    }
}

