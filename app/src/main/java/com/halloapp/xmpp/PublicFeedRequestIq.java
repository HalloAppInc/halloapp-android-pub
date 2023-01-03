package com.halloapp.xmpp;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.FollowSuggestionsRequest;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.PublicFeedContentType;
import com.halloapp.proto.server.PublicFeedRequest;

public class PublicFeedRequestIq extends HalloIq {

    private String cursor;

    public PublicFeedRequestIq(@Nullable String cursor) {
        this.cursor = cursor;
    }

    @Override
    public Iq.Builder toProtoIq() {
        PublicFeedRequest.Builder builder = PublicFeedRequest.newBuilder();
        if (!TextUtils.isEmpty(cursor)) {
            builder.setCursor(cursor);
        }
        builder.setPublicFeedContentType(PublicFeedContentType.MOMENTS);
        // TODO(jack): Set GPS coordinates if available
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setPublicFeedRequest(builder);
    }
}

