package com.halloapp.xmpp;

import com.halloapp.contacts.RelationshipInfo;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.BasicUserProfile;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.RelationshipList;
import com.halloapp.proto.server.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class RelationshipListResponseIq extends HalloIq {

    public final List<RelationshipInfo> relationshipList;
    public final String cursor;

    private RelationshipListResponseIq(RelationshipList relationshipList) {
        List<RelationshipInfo> infos = new ArrayList<>();
        @RelationshipInfo.Type int type = RelationshipInfo.fromProtoType(relationshipList.getType());
        for (BasicUserProfile userProfile : relationshipList.getUsersList()) {
            infos.add(new RelationshipInfo(
                    new UserId(Long.toString(userProfile.getUid())),
                    userProfile.getUsername(),
                    userProfile.getName(),
                    userProfile.getAvatarId(),
                    type,
                    System.currentTimeMillis()
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

