package com.halloapp.xmpp;

import android.util.Base64;

import com.google.protobuf.ByteString;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.Avatar;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.UploadAvatar;

public class AvatarIq extends HalloIq {

    final String base64;
    final long numBytes;
    final int height;
    final int width;
    final String avatarId;
    final UserId userId;
    final byte[] bytes;

    AvatarIq(String base64, long numBytes, int height, int width) {
        this.base64 = base64;
        this.numBytes = numBytes;
        this.height = height;
        this.width = width;
        this.avatarId = null;
        this.userId = null;
        this.bytes = Base64.decode(base64, Base64.NO_WRAP);
    }

    AvatarIq(UserId userId) {
        this.base64 = null;
        this.numBytes = 0;
        this.height = 0;
        this.width = 0;
        this.avatarId = null;
        this.userId = userId;
        this.bytes = null;
    }

    private AvatarIq(String avatarId) {
        this.base64 = null;
        this.numBytes = 0;
        this.height = 0;
        this.width = 0;
        this.avatarId = avatarId;
        this.userId = null;
        this.bytes = null;
    }

    @Override
    public Iq toProtoIq() {
        if (userId != null) {
            Avatar.Builder builder = Avatar.newBuilder();
            builder.setUid(Long.parseLong(userId.rawId()));
            return Iq.newBuilder()
                    .setType(Iq.Type.GET)
                    .setId(getStanzaId())
                    .setAvatar(builder)
                    .build();
        } else {
            UploadAvatar.Builder builder = UploadAvatar.newBuilder();
            builder.setData(ByteString.copyFrom(bytes));
            return Iq.newBuilder()
                    .setType(Iq.Type.SET)
                    .setId(getStanzaId())
                    .setUploadAvatar(builder)
                    .build();
        }
    }

    public static AvatarIq fromProto(Avatar avatar) {
        return new AvatarIq(avatar.getId());
    }
}
