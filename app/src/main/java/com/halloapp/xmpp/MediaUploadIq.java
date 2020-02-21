package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jxmpp.jid.Jid;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class MediaUploadIq extends IQ {

    final static String ELEMENT = "upload_media";
    final static String NAMESPACE = "ns:upload_media";

    final Urls urls = new Urls();

    MediaUploadIq(@NonNull Jid to) {
        super(ELEMENT, NAMESPACE);
        setType(IQ.Type.get);
        setTo(to);
    }

    private MediaUploadIq(@NonNull XmlPullParser parser) throws IOException, XmlPullParserException {
        super(ELEMENT, NAMESPACE);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            if ("media_urls".equals(name)) {
                urls.putUrl = parser.getAttributeValue(null, "put");
                urls.getUrl = parser.getAttributeValue(null, "get");
            } else {
                Xml.skip(parser);
            }
        }
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        return xml;
    }

    public static class Provider extends IQProvider<MediaUploadIq> {

        @Override
        public MediaUploadIq parse(XmlPullParser parser, int initialDepth) throws IOException, XmlPullParserException {
            return new MediaUploadIq(parser);
        }
    }

    public static class Urls {
        public String putUrl;
        public String getUrl;
    }


}
