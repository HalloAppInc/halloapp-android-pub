package com.halloapp.xmpp;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.SetLinkResult;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SetLinkResponseIq extends HalloIq {

    public boolean success;
    public @Reason int reason;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Reason.UNKNOWN, Reason.BAD_TYPE})
    public @interface Reason {
        int UNKNOWN = -1;
        int BAD_TYPE = 0;
    }

    public SetLinkResponseIq(@NonNull SetLinkResult setLinkResult) {
        this.success = setLinkResult.getResult().equals(SetLinkResult.Result.OK);
        this.reason = parseReason(setLinkResult.getReason());
    }

    private static @Reason
    int parseReason(SetLinkResult.Reason reason) {
        if (reason == SetLinkResult.Reason.BAD_TYPE) {
            return Reason.BAD_TYPE;
        }

        return Reason.UNKNOWN;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static SetLinkResponseIq fromProto(@NonNull SetLinkResult setLinkResult) {
        return new SetLinkResponseIq(setLinkResult);
    }
}
