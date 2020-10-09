package com.halloapp.xmpp;

import com.halloapp.id.UserId;
import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.xmlpull.v1.XmlPullParser;

public class WhisperKeysMessage implements ExtensionElement {

    public static final String ELEMENT = "whisper_keys";
    public static final String NAMESPACE = "halloapp:whisper:keys";

    private static final String ONE_TIME_PRE_KEY_COUNT_ELEMENT = "otp_key_count";

    private static final String ATTRIBUTE_USER_ID = "uid";
    private static final String ATTRIBUTE_TYPE = "type";

    private static final String TYPE_NORMAL = "normal";
    private static final String TYPE_UPDATE = "update";

    public final Integer count;
    public final UserId userId;

    public WhisperKeysMessage(int count) {
        this.count = count;
        this.userId = null;
    }

    public WhisperKeysMessage(UserId userId) {
        this.count = null;
        this.userId = userId;
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

    public static class Provider extends ExtensionElementProvider<WhisperKeysMessage> {

        @Override
        public final WhisperKeysMessage parse(XmlPullParser parser, int initialDepth) throws Exception {
            String type = parser.getAttributeValue("", ATTRIBUTE_TYPE);
            String userId = parser.getAttributeValue("", ATTRIBUTE_USER_ID);
            if (TYPE_NORMAL.equals(type)) {
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
                return new WhisperKeysMessage(count);
            } else if (TYPE_UPDATE.equals(type)) {
                return new WhisperKeysMessage(new UserId(userId));
            }

            return null;
        }
    }
}
