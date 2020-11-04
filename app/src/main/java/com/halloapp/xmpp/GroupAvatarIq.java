package com.halloapp.xmpp;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.halloapp.id.GroupId;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.UploadGroupAvatar;

public class GroupAvatarIq extends HalloIq {

    final GroupId groupId;
    final String base64;

    GroupAvatarIq(@NonNull GroupId groupId, @NonNull String base64) {
        this.groupId = groupId;
        this.base64 = base64;
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
