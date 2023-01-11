package com.halloapp.xmpp;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.UsernameResponse;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class UsernameResponseIq extends HalloIq {

    public boolean success;
    public @Reason int reason;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Reason.UNKNOWN, Reason.TOO_SHORT, Reason.TOO_LONG, Reason.BAD_EXPRESSION, Reason.NOT_UNIQUE})
    public @interface Reason {
        int UNKNOWN = -1;
        int TOO_SHORT = 0;
        int TOO_LONG = 1;
        int BAD_EXPRESSION = 2;
        int NOT_UNIQUE = 3;
    }

    public UsernameResponseIq(@NonNull UsernameResponse usernameResponse) {
        this.success = usernameResponse.getResult().equals(UsernameResponse.Result.OK);
        this.reason = parseReason(usernameResponse.getReason());
    }

    private static @Reason
    int parseReason(UsernameResponse.Reason reason) {
        switch (reason) {
            case TOOSHORT:
                return Reason.TOO_SHORT;
            case TOOLONG:
                return Reason.TOO_LONG;
            case BADEXPR:
                return Reason.BAD_EXPRESSION;
            case NOTUNIQ:
                return Reason.NOT_UNIQUE;
            default:
                return Reason.UNKNOWN;
        }
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static UsernameResponseIq fromProto(@NonNull UsernameResponse usernameResponse) {
        return new UsernameResponseIq(usernameResponse);
    }
}
