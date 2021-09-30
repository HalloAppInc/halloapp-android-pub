package com.halloapp.xmpp;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.halloapp.id.GroupId;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.UploadGroupAvatar;

public class GroupAvatarIq extends HalloIq {

    final GroupId groupId;
    final byte[] bytes;

    GroupAvatarIq(@NonNull GroupId groupId, @NonNull byte[] bytes) {
        this.groupId = groupId;
        this.bytes = bytes;
    }

    @Override
    public Iq toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setId(getStanzaId())
                .setGroupAvatar(
                        UploadGroupAvatar.newBuilder()
                                .setGid(groupId.rawId())
                                .setData(ByteString.copyFrom(bytes))
                                .build())
                .build();
    }
}
