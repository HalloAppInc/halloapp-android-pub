package com.halloapp.xmpp;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.SetBioResult;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SetBioResponseIq extends HalloIq {

    public boolean success;
    public @Reason int reason;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Reason.UNKNOWN, Reason.TOO_LONG})
    public @interface Reason {
        int UNKNOWN = -1;
        int TOO_LONG = 0;
    }

    public SetBioResponseIq(@NonNull SetBioResult setBioResult) {
        this.success = setBioResult.getResult().equals(SetBioResult.Result.OK);
        this.reason = parseReason(setBioResult.getReason());
    }

    private static @Reason
    int parseReason(SetBioResult.Reason reason) {
        if (reason == SetBioResult.Reason.TOO_LONG) {
            return Reason.TOO_LONG;
        }

        return Reason.UNKNOWN;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static SetBioResponseIq fromProto(@NonNull SetBioResult setBioResult) {
        return new SetBioResponseIq(setBioResult);
    }
}
