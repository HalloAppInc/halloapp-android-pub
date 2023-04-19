package com.halloapp.xmpp;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.proto.server.GeoTagRequest;
import com.halloapp.proto.server.GpsLocation;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.PostSubscriptionRequest;

public class GeotagRequestIq extends HalloIq {

    public String geotag;
    public Location location;
    public GeoTagRequest.Action action;

    public GeotagRequestIq(@Nullable String geotag, @Nullable Location location, @NonNull GeoTagRequest.Action action) {
        this.geotag = geotag;
        this.location = location;
        this.action = action;
    }

    @Override
    public Iq.Builder toProtoIq() {
        GeoTagRequest.Builder builder = GeoTagRequest.newBuilder()
                .setAction(action);
        if (geotag != null) {
            builder.setGeoTag(geotag);
        }
        if (location != null) {
            builder.setGpsLocation(GpsLocation.newBuilder()
                    .setLatitude(location.getLatitude())
                    .setLongitude(location.getLongitude())
                    .build());
        }
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setGeoTagRequest(builder);
    }
}
