package com.halloapp.xmpp;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.ArchiveRequest;
import com.halloapp.proto.server.Iq;

public class ArchiveRequestIq extends HalloIq {
    private UserId userId;

    public ArchiveRequestIq(UserId userId) {
        this.userId = userId;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setArchiveRequest(ArchiveRequest.newBuilder().setUid(userId.rawIdLong()).build());
    }
}
