package com.halloapp.xmpp;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.FollowSuggestionsRequest;
import com.halloapp.proto.server.GpsLocation;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.PublicFeedContentType;
import com.halloapp.proto.server.PublicFeedRequest;

public class PublicFeedRequestIq extends HalloIq {

    private final String cursor;
    private final Double latitude;
    private final Double longitude;

    public PublicFeedRequestIq(@Nullable String cursor, @Nullable Double latitude, @Nullable Double longitude) {
        this.cursor = cursor;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public Iq.Builder toProtoIq() {
        PublicFeedRequest.Builder builder = PublicFeedRequest.newBuilder();
        if (!TextUtils.isEmpty(cursor)) {
            builder.setCursor(cursor);
        }
        builder.setPublicFeedContentType(PublicFeedContentType.MOMENTS);
        if (longitude != null && latitude != null) {
            final GpsLocation gpsLocation = GpsLocation.newBuilder().setLatitude(latitude).setLongitude(longitude).build();
            builder.setGpsLocation(gpsLocation);
        }
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setPublicFeedRequest(builder);
    }
}

