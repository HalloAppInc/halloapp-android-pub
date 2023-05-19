package com.halloapp.xmpp;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.ArchiveResult;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.Post;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class ArchiveResultIq extends HalloIq {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Reason.UNKNOWN_REASON, Reason.INVALID_USER})
    public @interface Reason {
        int UNKNOWN_REASON = 0;
        int INVALID_USER = 1;
    }

    public boolean success;
    public @Reason int reason;
    public UserId userId;
    public List<Post> posts;
    public String startDate;

    public ArchiveResultIq(@NonNull ArchiveResult archiveResult) {
        success = archiveResult.getResult().equals(ArchiveResult.Result.OK);
        if (archiveResult.getReason() == ArchiveResult.Reason.INVALID_USER) {
            reason = Reason.INVALID_USER;
        } else {
            reason = Reason.UNKNOWN_REASON;
        }
        userId = new UserId(Long.toString(archiveResult.getUid()));
        posts = archiveResult.getPostsList();
        startDate = archiveResult.getStartDate();
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static ArchiveResultIq fromProto(@NonNull ArchiveResult archiveResult) {
        return new ArchiveResultIq(archiveResult);
    }
}
