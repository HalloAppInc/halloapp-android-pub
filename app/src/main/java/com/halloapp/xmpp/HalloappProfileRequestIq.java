
package com.halloapp.xmpp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.HalloappProfileRequest;
import com.halloapp.proto.server.Iq;

public class HalloappProfileRequestIq extends HalloIq {

    private final UserId userId;
    private final String username;

    public HalloappProfileRequestIq(@Nullable UserId userId, @Nullable String username) {
        this.userId = userId;
        this.username = username;
    }

    @Override
    public Iq.Builder toProtoIq() {
        HalloappProfileRequest.Builder request = HalloappProfileRequest.newBuilder();
        if (userId != null) {
            request.setUid(userId.rawIdLong());
        }
        if (username != null) {
            request.setUsername(username);
        }
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setHalloappProfileRequest(request);
    }
}
