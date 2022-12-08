package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.RelationshipList;
import com.halloapp.proto.server.RelationshipRequest;

public class RelationshipRequestIq extends HalloIq {

    private UserId userId;
    private RelationshipRequest.Action action;

    public RelationshipRequestIq(@NonNull UserId userId, @NonNull RelationshipRequest.Action action) {
        this.userId = userId;
        this.action = action;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setRelationshipRequest(RelationshipRequest.newBuilder()
                        .setAction(action)
                        .setUid(userId.rawIdLong()));
    }
}

