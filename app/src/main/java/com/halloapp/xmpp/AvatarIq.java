package com.halloapp.xmpp;

import com.halloapp.id.UserId;
import com.halloapp.util.Log;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jxmpp.jid.Jid;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class AvatarIq extends IQ {

    public static final String ELEMENT = "avatar";

    private static final String ATTRIBUTE_WIDTH = "width";
    private static final String ATTRIBUTE_HEIGHT = "height";
    private static final String ATTRIBUTE_BYTES = "bytes";
    private static final String ATTRIBUTE_USER_ID = "userid";

    public static final String NAMESPACE = "halloapp:user:avatar";

    final String base64;
    final long numBytes;
    final int height;
    final int width;
    final String avatarId;
    final UserId userId;

    AvatarIq(Jid to, String base64, long numBytes, int height, int width) {
        super(ELEMENT, NAMESPACE);
        setType(Type.set);
        setTo(to);
        this.base64 = base64;
        this.numBytes = numBytes;
        this.height = height;
        this.width = width;
        this.avatarId = null;
        this.userId = null;
    }

    AvatarIq(Jid to, UserId userId) {
        super(ELEMENT, NAMESPACE);
        setType(Type.get);
        setTo(to);
        this.base64 = null;
        this.numBytes = 0;
        this.height = 0;
        this.width = 0;
        this.avatarId = null;
        this.userId = userId;
    }

    private AvatarIq(String avatarId) {
        super(ELEMENT, NAMESPACE);
        this.base64 = null;
        this.numBytes = 0;
        this.height = 0;
        this.width = 0;
        this.avatarId = avatarId;
        this.userId = null;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        if (userId != null) {
            xml.attribute(ATTRIBUTE_USER_ID, userId.rawId());
            xml.rightAngleBracket();
        } else {
            xml.attribute(ATTRIBUTE_BYTES, Long.toString(numBytes));
            xml.attribute(ATTRIBUTE_WIDTH, Integer.toString(width));
            xml.attribute(ATTRIBUTE_HEIGHT, Integer.toString(height));
            xml.rightAngleBracket();
            xml.append(base64);
        }

        return xml;
    }

    public static class Provider extends IQProvider<AvatarIq> {

        @Override
        public AvatarIq parse(XmlPullParser parser, int initialDepth) throws IOException, XmlPullParserException {
            String avatarId = parser.getAttributeValue("", "id");
            return new AvatarIq(avatarId);
        }
    }
}
