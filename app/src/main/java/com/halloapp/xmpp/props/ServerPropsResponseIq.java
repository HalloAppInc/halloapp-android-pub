package com.halloapp.xmpp.props;

import androidx.annotation.Nullable;

import com.halloapp.util.Log;
import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerPropsResponseIq extends IQ {

    public static final String ELEMENT = "props";
    public static final String NAMESPACE = "halloapp:props";

    private static final String ELEMENT_PROP = "prop";

    private final Map<String, String> propMap = new HashMap<>();
    private String hash;

    protected ServerPropsResponseIq(XmlPullParser parser) throws IOException, XmlPullParserException {
        super(ELEMENT, NAMESPACE);

        hash = parser.getAttributeValue(null, "hash");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            if (ELEMENT_PROP.equals(name)) {
                String propName = parser.getAttributeValue(null, "name");
                String propValue = Xml.readText(parser);
                propMap.put(propName, propValue);
            } else {
                Xml.skip(parser);
            }
        }
    }

    public Map<String, String> getProps() {
        return propMap;
    }

    public String getHash() {
        return hash;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        return null;
    }

    public static class Provider extends IQProvider<ServerPropsResponseIq> {

        @Override
        public ServerPropsResponseIq parse(XmlPullParser parser, int initialDepth) throws IOException, XmlPullParserException {
            return new ServerPropsResponseIq(parser);
        }
    }
}
