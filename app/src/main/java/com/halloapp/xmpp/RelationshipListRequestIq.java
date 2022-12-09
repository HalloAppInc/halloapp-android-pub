package com.halloapp.xmpp;

import com.halloapp.contacts.RelationshipInfo;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.RelationshipList;

public class RelationshipListRequestIq extends HalloIq {

    @RelationshipInfo.Type int relationshipType;

    public RelationshipListRequestIq(@RelationshipInfo.Type int relationshipType) {
        this.relationshipType = relationshipType;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setRelationshipList(RelationshipList.newBuilder().setType(RelationshipInfo.toProtoType(relationshipType)));
    }
}

