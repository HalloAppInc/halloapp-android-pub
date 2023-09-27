package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.contacts.FriendshipInfo;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.FriendshipResponse;
import com.halloapp.proto.server.HalloappUserProfile;
import com.halloapp.proto.server.Iq;

public class FriendshipResponseIq extends HalloIq {

    public boolean success;
    public HalloappUserProfile profile;
    public FriendshipInfo info;

    public FriendshipResponseIq(@NonNull FriendshipResponse friendshipResponse) {
        this.success = friendshipResponse.getResult().equals(FriendshipResponse.Result.OK);
        this.profile = friendshipResponse.getProfile();
        // Profiles are not set after rejecting a friend suggestion
        if (profile.getUid() != 0) {
            this.info = new FriendshipInfo(
                new UserId(Long.toString(profile.getUid())),
                profile.getUsername(),
                profile.getName(),
                profile.getAvatarId(),
                FriendshipInfo.fromProtoType(profile.getStatus(), profile.getBlocked()),
                System.currentTimeMillis()
            );
        }
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static FriendshipResponseIq fromProto(@NonNull FriendshipResponse friendshipResponse) {
        return new FriendshipResponseIq(friendshipResponse);
    }
}
