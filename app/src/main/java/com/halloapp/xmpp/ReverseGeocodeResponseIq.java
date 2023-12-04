package com.halloapp.xmpp;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.ReverseGeocodeLocation;
import com.halloapp.proto.server.ReverseGeocodeResult;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ReverseGeocodeResponseIq extends HalloIq {

    public boolean success;
    public @Reason int reason;
    public long backoff;
    public ReverseGeocodeLocation location;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Reason.UNKNOWN, Reason.TOO_SOON, Reason.INVALID_LAT_LONG})
    public @interface Reason {
        int UNKNOWN = -1;
        int TOO_SOON = 0;
        int INVALID_LAT_LONG = 1;
    }

    public ReverseGeocodeResponseIq(@NonNull ReverseGeocodeResult geocodeResult) {
        this.success = geocodeResult.getResult().equals(ReverseGeocodeResult.Result.OK);
        this.reason = parseReason(geocodeResult.getReason());
        this.backoff = geocodeResult.getBackoff();
        this.location = geocodeResult.getLocation();
    }

    private static @Reason int parseReason(ReverseGeocodeResult.Reason reason) {
        if (reason == ReverseGeocodeResult.Reason.TOO_SOON) {
            return Reason.TOO_SOON;
        } else if (reason == ReverseGeocodeResult.Reason.INVALID_LAT_LONG) {
            return Reason.INVALID_LAT_LONG;
        }
        return Reason.UNKNOWN;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static ReverseGeocodeResponseIq fromProto(@NonNull ReverseGeocodeResult geocodeResult) {
        return new ReverseGeocodeResponseIq(geocodeResult);
    }
}
