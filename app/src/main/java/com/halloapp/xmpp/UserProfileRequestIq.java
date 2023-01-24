package com.halloapp.xmpp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.UserProfileRequest;

public class UserProfileRequestIq extends HalloIq {

    UserId userId;
    String username;

    public UserProfileRequestIq(@NonNull UserId userId, @Nullable String username) {
        this.userId = userId;
        this.username = username;
    }

    @Override
    public Iq.Builder toProtoIq() {
        UserProfileRequest.Builder request = UserProfileRequest.newBuilder();
        if (userId != null) {
            request.setUid(userId.rawIdLong());
        }
        if (username != null) {
            request.setUsername(username);
        }
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setUserProfileRequest(request);
    }
}
