package com.halloapp.xmpp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.ReportUserContent;

public class ReportContentIq extends HalloIq {

    private final UserId userId;
    private final String contentId;

    ReportContentIq(@NonNull UserId userId, @Nullable String contentId) {
        this.userId = userId;
        this.contentId = contentId;
    }

    @Override
    public Iq.Builder toProtoIq() {
        ReportUserContent.Builder builder = ReportUserContent.newBuilder();
        builder.setType(contentId != null ? ReportUserContent.Type.POST : ReportUserContent.Type.USER);
        builder.setUid(Long.parseLong(userId.rawId()));
        if (contentId != null) {
            builder.setContentId(contentId);
        }
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setReportUserContent(builder.build());
    }
}

