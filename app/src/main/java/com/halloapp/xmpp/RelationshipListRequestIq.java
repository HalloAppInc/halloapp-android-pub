package com.halloapp.xmpp;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.proto.server.ExportData;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.RelationshipList;

public class RelationshipListRequestIq extends HalloIq {

    @ContactsDb.KatchupRelationshipInfo.RelationshipType int relationshipType;

    public RelationshipListRequestIq(@ContactsDb.KatchupRelationshipInfo.RelationshipType int relationshipType) {
        this.relationshipType = relationshipType;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setRelationshipList(RelationshipList.newBuilder().setType(ContactsDb.KatchupRelationshipInfo.toProtoType(relationshipType)));
    }
}

