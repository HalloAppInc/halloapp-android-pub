package com.halloapp.xmpp;

import android.util.Base64;

import com.google.protobuf.ByteString;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.Avatar;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.UploadAvatar;

import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class AvatarIq extends HalloIq {

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
    final byte[] bytes;

    AvatarIq(byte[] bytes) {
        super(ELEMENT, NAMESPACE);
        this.base64 = null;
        this.numBytes = 0;
        this.height = 0;
        this.width = 0;
        this.avatarId = null;
        this.userId = null;
        this.bytes = bytes;
    }

    AvatarIq(String base64, long numBytes, int height, int width) {
        super(ELEMENT, NAMESPACE);
        setType(Type.set);
        this.base64 = base64;
        this.numBytes = numBytes;
        this.height = height;
        this.width = width;
        this.avatarId = null;
        this.userId = null;
        this.bytes = Base64.decode(base64, Base64.NO_WRAP);
    }

    AvatarIq(UserId userId) {
        super(ELEMENT, NAMESPACE);
        setType(Type.get);
        this.base64 = null;
        this.numBytes = 0;
        this.height = 0;
        this.width = 0;
        this.avatarId = null;
        this.userId = userId;
        this.bytes = null;
    }

    private AvatarIq(String avatarId) {
        super(ELEMENT, NAMESPACE);
        this.base64 = null;
        this.numBytes = 0;
        this.height = 0;
        this.width = 0;
        this.avatarId = avatarId;
        this.userId = null;
        this.bytes = null;
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

    @Override
    public Iq toProtoIq() {
        if (userId != null) {
            Avatar.Builder builder = Avatar.newBuilder();
            builder.setUid(Long.parseLong(userId.rawId()));
            return Iq.newBuilder().setType(Iq.Type.GET).setId(getStanzaId()).setAvatar(builder.build()).build();
        } else {
            UploadAvatar.Builder builder = UploadAvatar.newBuilder();
            builder.setData(ByteString.copyFrom(bytes));
            return Iq.newBuilder().setType(Iq.Type.SET).setId(getStanzaId()).setUploadAvatar(builder.build()).build();
        }
    }

    public static AvatarIq fromProto(Avatar avatar) {
        return new AvatarIq(avatar.getId());
    }

    public static class Provider extends IQProvider<AvatarIq> {

        @Override
        public AvatarIq parse(XmlPullParser parser, int initialDepth) throws IOException, XmlPullParserException {
            String avatarId = parser.getAttributeValue("", "id");
            return new AvatarIq(avatarId);
        }
    }
}
