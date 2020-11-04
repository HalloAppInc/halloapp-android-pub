package com.halloapp.xmpp;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.halloapp.id.GroupId;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.UploadGroupAvatar;

public class GroupAvatarIq extends HalloIq {

    public static final String ELEMENT = "group_avatar";

    private static final String ATTRIBUTE_GROUP_ID = "gid";

    public static final String NAMESPACE = "halloapp:groups";

    final GroupId groupId;
    final String base64;

    GroupAvatarIq(@NonNull GroupId groupId, @NonNull String base64) {
        super(ELEMENT, NAMESPACE);
        setType(Type.set);
        this.groupId = groupId;
        this.base64 = base64;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.attribute(ATTRIBUTE_GROUP_ID, groupId.rawId());
        xml.rightAngleBracket();
        xml.append(base64);

        return xml;
    }

    @Override
    public Iq toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setId(getStanzaId())
                .setGroupAvatar(
                        UploadGroupAvatar.newBuilder()
                                .setGid(groupId.rawId())
                                .setData(ByteString.copyFrom(Base64.decode(base64, Base64.NO_WRAP)))
                                .build())
                .build();
    }
}
