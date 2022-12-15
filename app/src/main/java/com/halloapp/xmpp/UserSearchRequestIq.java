package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.FollowSuggestionsRequest;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.SearchRequest;

public class UserSearchRequestIq extends HalloIq {

    private String text;

    public UserSearchRequestIq(@NonNull String text) {
        this.text = text;
    }

    @Override
    public Iq.Builder toProtoIq() {
        SearchRequest.Builder builder = SearchRequest.newBuilder();
        builder.setUsernameString(text);
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setSearchRequest(builder);
    }
}

