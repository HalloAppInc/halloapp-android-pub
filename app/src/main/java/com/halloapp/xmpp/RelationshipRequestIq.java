package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.RelationshipList;
import com.halloapp.proto.server.RelationshipRequest;

public class RelationshipRequestIq extends HalloIq {

    private UserId userId;

    public RelationshipRequestIq(@NonNull UserId userId) {
        this.userId = userId;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setRelationshipRequest(RelationshipRequest.newBuilder()
                        .setAction(RelationshipRequest.Action.FOLLOW)
                        .setUid(userId.rawIdLong()));
    }
}

