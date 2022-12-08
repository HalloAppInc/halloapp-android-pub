package com.halloapp.xmpp;

import com.halloapp.proto.server.ExportData;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.RelationshipList;

public class RelationshipListRequestIq extends HalloIq {

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setRelationshipList(RelationshipList.newBuilder().setType(RelationshipList.Type.FOLLOWING));
    }
}

