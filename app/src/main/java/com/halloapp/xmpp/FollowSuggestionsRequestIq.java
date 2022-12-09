package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.FollowSuggestionsRequest;
import com.halloapp.proto.server.Iq;

public class FollowSuggestionsRequestIq extends HalloIq {

    private UserId rejectUserId;

    public FollowSuggestionsRequestIq() {
    }

    public FollowSuggestionsRequestIq(@NonNull UserId userId) {
        this.rejectUserId = userId;
    }

    @Override
    public Iq.Builder toProtoIq() {
        FollowSuggestionsRequest.Builder builder = FollowSuggestionsRequest.newBuilder();
        if (rejectUserId != null) {
            builder.setAction(FollowSuggestionsRequest.Action.REJECT);
            builder.addRejectedUids(rejectUserId.rawIdLong());
        } else {
            builder.setAction(FollowSuggestionsRequest.Action.GET);
        }
        return Iq.newBuilder()
                .setType(rejectUserId != null ? Iq.Type.SET : Iq.Type.GET)
                .setFollowSuggestionsRequest(builder);
    }
}

