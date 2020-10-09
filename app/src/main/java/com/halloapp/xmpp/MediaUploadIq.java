package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.MediaUrl;
import com.halloapp.proto.server.UploadMedia;
import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jxmpp.jid.Jid;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class MediaUploadIq extends HalloIq {

    final static String ELEMENT = "upload_media";
    final static String NAMESPACE = "ns:upload_media";
    final static String ATTRIBUTE_FILESIZE = "size";

    final Urls urls = new Urls();
    long fileSize = 0;

    MediaUploadIq(@NonNull Jid to, long fileSize) {
        super(ELEMENT, NAMESPACE);
        setType(IQ.Type.get);
        setTo(to);
        this.fileSize = fileSize;
    }

    private MediaUploadIq(@NonNull XmlPullParser parser) throws IOException, XmlPullParserException {
        super(ELEMENT, NAMESPACE);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            if ("media_urls".equals(name)) {
                if (parser.getAttributeName(0).equals("patch")) {
                    urls.patchUrl = parser.getAttributeValue(null, "patch");
                } else {
                    urls.putUrl = parser.getAttributeValue(null, "put");
                    urls.getUrl = parser.getAttributeValue(null, "get");
                }
            } else {
                Xml.skip(parser);
            }
        }
    }

    private MediaUploadIq(String patch, String put, String get) {
        super(ELEMENT, NAMESPACE);

        urls.patchUrl = patch;
        urls.putUrl = put;
        urls.getUrl = get;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.attribute(ATTRIBUTE_FILESIZE, Long.toString(fileSize));
        xml.rightAngleBracket();
        return xml;
    }

    @Override
    public Iq toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setUploadMedia(UploadMedia.newBuilder().setSize(fileSize))
                .build();
    }

    public static MediaUploadIq fromProto(UploadMedia uploadMedia) {
        MediaUrl mediaUrl = uploadMedia.getUrl();
        return new MediaUploadIq(mediaUrl.getPatch(), mediaUrl.getPut(), mediaUrl.getGet());
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
        public String patchUrl;
    }


}
