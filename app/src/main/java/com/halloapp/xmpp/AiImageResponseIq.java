package com.halloapp.xmpp;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.halloapp.proto.server.AiImageResult;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.SetLinkResult;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AiImageResponseIq extends HalloIq {

    public boolean success;
    public @Reason int reason;
    public String id;
    public long backoff;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Reason.UNKNOWN, Reason.TOO_SOON, Reason.TOO_MANY_ATTEMPTS})
    public @interface Reason {
        int UNKNOWN = -1;
        int TOO_SOON = 0;
        int TOO_MANY_ATTEMPTS = 1;
    }

    public AiImageResponseIq(@NonNull AiImageResult aiImageResult) {
        this.success = aiImageResult.getResult().equals(AiImageResult.Result.PENDING);
        this.reason = parseReason(aiImageResult.getReason());
        this.id = aiImageResult.getId();
        this.backoff = aiImageResult.getBackoff();
    }

    private static @Reason int parseReason(AiImageResult.Reason reason) {
        if (reason == AiImageResult.Reason.TOO_SOON) {
            return Reason.TOO_SOON;
        } else if (reason == AiImageResult.Reason.TOO_MANY_ATTEMPTS) {
            return Reason.TOO_MANY_ATTEMPTS;
        }

        return Reason.UNKNOWN;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static AiImageResponseIq fromProto(@NonNull AiImageResult aiImageResult) {
        return new AiImageResponseIq(aiImageResult);
    }
}
