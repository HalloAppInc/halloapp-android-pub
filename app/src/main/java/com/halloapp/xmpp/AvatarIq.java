package com.halloapp.xmpp;

import com.google.protobuf.ByteString;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.Avatar;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.UploadAvatar;

public class AvatarIq extends HalloIq {

    final String avatarId;
    final UserId userId;
    final byte[] bytes;
    final byte[] largeBytes;

    AvatarIq(byte[] bytes, byte[] largeBytes) {
        this.avatarId = null;
        this.userId = null;
        this.bytes = bytes;
        this.largeBytes = largeBytes;
    }

    AvatarIq(UserId userId) {
        this.avatarId = null;
        this.userId = userId;
        this.bytes = null;
        this.largeBytes = null;
    }

    private AvatarIq(String avatarId) {
        this.avatarId = avatarId;
        this.userId = null;
        this.bytes = null;
        this.largeBytes = null;
    }

    @Override
    public Iq.Builder toProtoIq() {
        if (userId != null) {
            Avatar.Builder builder = Avatar.newBuilder();
            builder.setUid(Long.parseLong(userId.rawId()));
            return Iq.newBuilder()
                    .setType(Iq.Type.GET)
                    .setAvatar(builder);
        } else {
            UploadAvatar.Builder builder = UploadAvatar.newBuilder();
            builder.setData(ByteString.copyFrom(bytes));
            builder.setFullData(ByteString.copyFrom(largeBytes));
            return Iq.newBuilder()
                    .setType(Iq.Type.SET)
                    .setUploadAvatar(builder);
        }
    }

    public static AvatarIq fromProto(Avatar avatar) {
        return new AvatarIq(avatar.getId());
    }
}
