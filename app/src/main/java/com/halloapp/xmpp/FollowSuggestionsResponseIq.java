package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.contacts.RelationshipInfo;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.BasicUserProfile;
import com.halloapp.proto.server.FollowSuggestionsRequest;
import com.halloapp.proto.server.FollowSuggestionsResponse;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.SuggestedProfile;
import com.halloapp.proto.server.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class FollowSuggestionsResponseIq extends HalloIq {

    public boolean success;
    public List<Suggestion> suggestions = new ArrayList<>();

    public FollowSuggestionsResponseIq(@NonNull FollowSuggestionsResponse followSuggestionsResponse) {
        this.success = followSuggestionsResponse.getResult().equals(FollowSuggestionsResponse.Result.OK);
        for (SuggestedProfile suggestedProfile : followSuggestionsResponse.getSuggestedProfilesList()) {
            BasicUserProfile userProfile = suggestedProfile.getUserProfile();
            RelationshipInfo info = new RelationshipInfo(
                    new UserId(Long.toString(userProfile.getUid())),
                    userProfile.getUsername(),
                    userProfile.getName(),
                    userProfile.getAvatarId(),
                    RelationshipInfo.Type.BLOCKED,
                    System.currentTimeMillis()
            );
            suggestions.add(new Suggestion(Suggestion.fromProto(suggestedProfile.getReason()), suggestedProfile.getRank(), info, suggestedProfile.getUserProfile().getNumMutualFollowing()));
        }
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static FollowSuggestionsResponseIq fromProto(@NonNull FollowSuggestionsResponse followSuggestionsResponse) {
        return new FollowSuggestionsResponseIq(followSuggestionsResponse);
    }

    public static class Suggestion {
        public enum Type {
            Contact,
            Fof,
            Campus,
        }

        public static Type fromProto(SuggestedProfile.Reason reason) {
            switch (reason) {
                case DIRECT_CONTACT: return Type.Contact;
                case FOF: return Type.Fof;
                case CAMPUS: return Type.Campus;
            }
            throw new IllegalArgumentException("Unexpected reason " + reason);
        }

        public Type type;
        public int rank;
        public RelationshipInfo info;
        public int mutuals;

        public Suggestion(Type type, int rank, RelationshipInfo info, int mutuals) {
            this.type = type;
            this.rank = rank;
            this.info = info;
            this.mutuals = mutuals;
        }
    }
}

