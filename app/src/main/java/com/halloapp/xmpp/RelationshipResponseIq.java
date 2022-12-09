package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.RelationshipRequest;
import com.halloapp.proto.server.RelationshipResponse;
import com.halloapp.proto.server.UserProfile;

public class RelationshipResponseIq extends HalloIq {

    public boolean success;
    public UserId userId;
    public String username;
    public String name;
    public String avatarId;

    public RelationshipResponseIq(@NonNull RelationshipResponse relationshipResponse) {
        this.success = relationshipResponse.getResult().equals(RelationshipResponse.Result.OK);
        UserProfile userProfile = relationshipResponse.getProfile();
        this.userId = new UserId(Long.toString(userProfile.getUid()));
        this.username = userProfile.getUsername();
        this.name = userProfile.getName();
        this.avatarId = userProfile.getAvatarId();
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static RelationshipResponseIq fromProto(@NonNull RelationshipResponse relationshipResponse) {
        return new RelationshipResponseIq(relationshipResponse);
    }
}

