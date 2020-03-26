package com.halloapp.xmpp;

import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.xmlpull.v1.XmlPullParser;

public class WhisperKeysLowCountMessage implements ExtensionElement {

    public static final String ELEMENT = "whisper_keys";
    public static final String NAMESPACE = "halloapp:whisper:keys";

    private static final String ONE_TIME_PRE_KEY_COUNT_ELEMENT = "otp_key_count";

    public int count;

    public WhisperKeysLowCountMessage(Integer count) {
        this.count = count;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public CharSequence toXML(String enclosingNamespace) {
        return null;
    }

    public static class Provider extends ExtensionElementProvider<WhisperKeysLowCountMessage> {

        @Override
        public final WhisperKeysLowCountMessage parse(XmlPullParser parser, int initialDepth) throws Exception {
            int count = 10;
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                final String name = parser.getName();
                if (ONE_TIME_PRE_KEY_COUNT_ELEMENT.equals(name)) {
                    count = Integer.parseInt(Xml.readText(parser));
                } else {
                    Xml.skip(parser);
                }
            }
            return new WhisperKeysLowCountMessage(count);
        }
    }
}
