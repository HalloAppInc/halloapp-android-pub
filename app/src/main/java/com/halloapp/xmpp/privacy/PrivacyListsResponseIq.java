package com.halloapp.xmpp.privacy;

import androidx.annotation.Nullable;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.PrivacyListResult;
import com.halloapp.proto.server.PrivacyLists;
import com.halloapp.util.Xml;
import com.halloapp.xmpp.HalloIq;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PrivacyListsResponseIq extends HalloIq {

    public static final String ELEMENT = "privacy_lists";
    public static final String NAMESPACE = "halloapp:user:privacy";

    static final String ELEMENT_PRIVACY_LIST = "privacy_list";

    public final @PrivacyList.Type String activeType;

    public Map<String, PrivacyList> resultMap = new HashMap<>();

    protected PrivacyListsResponseIq(XmlPullParser parser) throws IOException, XmlPullParserException {
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

    private PrivacyListsResponseIq(String activeType, Map<String, PrivacyList> resultMap) {
        super(ELEMENT, NAMESPACE);
        this.activeType = activeType;
        this.resultMap = resultMap;
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

    @Override
    public Iq toProtoIq() {
        return null;
    }

    public static PrivacyListsResponseIq fromProto(PrivacyLists privacyLists) {
        String activeType = privacyLists.getActiveType().name().toLowerCase(Locale.US);
        Map<String, PrivacyList> resultMap = new HashMap<>();

        for (com.halloapp.proto.server.PrivacyList privacyList : privacyLists.getListsList()) {
            PrivacyList list = new PrivacyList(privacyList);
            resultMap.put(list.type, list);
        }

        return new PrivacyListsResponseIq(activeType, resultMap);
    }

    public static class Provider extends IQProvider<PrivacyListsResponseIq> {

        @Override
        public PrivacyListsResponseIq parse(XmlPullParser parser, int initialDepth) throws IOException, XmlPullParserException {
            return new PrivacyListsResponseIq(parser);
        }
    }

}
