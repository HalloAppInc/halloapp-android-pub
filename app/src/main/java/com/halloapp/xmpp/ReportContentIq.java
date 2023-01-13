package com.halloapp.xmpp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.ReportUserContent;

public class ReportContentIq extends HalloIq {

    private final UserId userId;
    private final String contentId;
    private final ReportUserContent.Reason reason;

    ReportContentIq(@NonNull UserId userId, @Nullable String contentId, @Nullable ReportUserContent.Reason reason) {
        this.userId = userId;
        this.contentId = contentId;
        this.reason = reason;
    }

    @Override
    public Iq.Builder toProtoIq() {
        ReportUserContent.Builder builder = ReportUserContent.newBuilder();
        builder.setType(contentId != null ? ReportUserContent.Type.POST : ReportUserContent.Type.USER);
        builder.setUid(Long.parseLong(userId.rawId()));
        if (contentId != null) {
            builder.setContentId(contentId);
        }
        if (reason != null) {
            builder.setReason(reason);
        }
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setReportUserContent(builder.build());
    }
}

