package com.halloapp.xmpp;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.ExportData;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.RelationshipList;
import com.halloapp.proto.server.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class RelationshipListResponseIq extends HalloIq {

    public final List<ContactsDb.KatchupRelationshipInfo> relationshipList;
    public final String cursor;

    private RelationshipListResponseIq(RelationshipList relationshipList) {
        List<ContactsDb.KatchupRelationshipInfo> infos = new ArrayList<>();
        @ContactsDb.KatchupRelationshipInfo.RelationshipType int type = ContactsDb.KatchupRelationshipInfo.fromProtoType(relationshipList.getType());
        for (UserProfile userProfile : relationshipList.getUsersList()) {
            infos.add(new ContactsDb.KatchupRelationshipInfo(
                    new UserId(Long.toString(userProfile.getUid())),
                    userProfile.getUsername(),
                    userProfile.getName(),
                    userProfile.getAvatarId(),
                    type
            ));
        }
        this.relationshipList = infos;
        this.cursor = relationshipList.getCursor();
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static RelationshipListResponseIq fromProto(RelationshipList relationshipList) {
        return new RelationshipListResponseIq(relationshipList);
    }
}

