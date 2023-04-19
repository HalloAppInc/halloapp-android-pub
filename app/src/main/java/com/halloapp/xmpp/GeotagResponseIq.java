package com.halloapp.xmpp;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.halloapp.proto.server.FeedItem;
import com.halloapp.proto.server.GeoTagResponse;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.PostSubscriptionResponse;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class GeotagResponseIq extends HalloIq {

    public boolean success;
    public @Reason int reason;
    public List<String> geotags;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Reason.UNKNOWN_REASON, Reason.INVALID_REQUEST})
    public @interface Reason {
        int UNKNOWN_REASON = -1;
        int INVALID_REQUEST = 0;
    }

    public GeotagResponseIq(@NonNull GeoTagResponse geoTagResponse) {
        this.success = geoTagResponse.getResult().equals(GeoTagResponse.Result.OK);
        this.reason = parseReason(geoTagResponse.getReason());
        this.geotags = geoTagResponse.getGeoTagsList();
    }

    private static @Reason int parseReason(GeoTagResponse.Reason reason) {
        if (reason == GeoTagResponse.Reason.INVALID_REQUEST) {
            return Reason.INVALID_REQUEST;
        }
        return Reason.UNKNOWN_REASON;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static GeotagResponseIq fromProto(@NonNull GeoTagResponse geotagResponse) {
        return new GeotagResponseIq(geotagResponse);
    }
}
