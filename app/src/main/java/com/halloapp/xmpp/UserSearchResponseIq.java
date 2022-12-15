package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.FollowSuggestionsResponse;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.SearchRequest;
import com.halloapp.proto.server.SearchResponse;
import com.halloapp.proto.server.UserProfile;

import java.util.List;

public class UserSearchResponseIq extends HalloIq {

    public boolean success;
    // TODO(jack): Use Justin's data class for user profiles
    public List<UserProfile> profiles;

    public UserSearchResponseIq(@NonNull SearchResponse searchResponse) {
        success = searchResponse.getResult().equals(SearchResponse.Result.OK);
        profiles = searchResponse.getSearchResultList();
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static UserSearchResponseIq fromProto(@NonNull SearchResponse searchResponse) {
        return new UserSearchResponseIq(searchResponse);
    }
}

