package com.halloapp.xmpp.props;

import androidx.annotation.Nullable;

import com.google.android.gms.common.util.Hex;
import com.halloapp.proto.server.Prop;
import com.halloapp.proto.server.Props;
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

    private Map<String, String> propMap = new HashMap<>();
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

    private ServerPropsResponseIq(Map<String, String> propMap, String hash) {
        super(ELEMENT, NAMESPACE);

        this.propMap = propMap;
        this.hash = hash;
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

    public static ServerPropsResponseIq fromProto(Props props) {
        final Map<String, String> propMap = new HashMap<>();
        final String hash = Hex.bytesToStringLowercase(props.getHash().toByteArray());
        for (Prop prop : props.getPropsList()) {
            propMap.put(prop.getName(), prop.getValue());
        }
        return new ServerPropsResponseIq(propMap, hash);
    }

    public static class Provider extends IQProvider<ServerPropsResponseIq> {

        @Override
        public ServerPropsResponseIq parse(XmlPullParser parser, int initialDepth) throws IOException, XmlPullParserException {
            return new ServerPropsResponseIq(parser);
        }
    }
}
