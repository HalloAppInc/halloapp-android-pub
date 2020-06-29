package com.halloapp.xmpp;

import androidx.annotation.Nullable;

import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PrivacyListsResponseIq extends IQ {

    public static final String ELEMENT = "privacy_lists";
    public static final String NAMESPACE = "halloapp:user:privacy";

    static final String ELEMENT_PRIVACY_LIST = "privacy_list";

    private @PrivacyList.Type String activeType;

    public Map<String, PrivacyList> resultMap = new HashMap<>();

    public PrivacyListsResponseIq(XmlPullParser parser) throws IOException, XmlPullParserException {
        super(ELEMENT, NAMESPACE);

        activeType = parser.getAttributeValue(null, "active_type");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            if (ELEMENT_PRIVACY_LIST.equals(name)) {
                PrivacyList list = new PrivacyList(parser);
                resultMap.put(list.type, list);
            } else {
                Xml.skip(parser);
            }
        }
    }

    @Nullable
    public PrivacyList getPrivacyList(@PrivacyList.Type String type) {
        if (resultMap.containsKey(type)) {
            return resultMap.get(type);
        }
        return null;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        return null;
    }

    public static class Provider extends IQProvider<PrivacyListsResponseIq> {

        @Override
        public PrivacyListsResponseIq parse(XmlPullParser parser, int initialDepth) throws IOException, XmlPullParserException {
            return new PrivacyListsResponseIq(parser);
        }
    }

}
