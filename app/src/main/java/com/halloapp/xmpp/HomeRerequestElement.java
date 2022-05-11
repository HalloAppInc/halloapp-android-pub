package com.halloapp.xmpp;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.HomeFeedRerequest;
import com.halloapp.proto.server.Msg;
import com.halloapp.util.RandomId;

public class HomeRerequestElement {

    public final String id;
    public final String contentId;
    public final UserId originalSender;
    public final boolean senderStateIssue;
    public final HomeFeedRerequest.ContentType contentType;
    public final int rerequestCount;

    public HomeRerequestElement(UserId originalSender, String contentId, boolean senderStateIssue, HomeFeedRerequest.ContentType contentType, int rerequestCount) {
        this.id = RandomId.create();
        this.contentId = contentId;
        this.originalSender = originalSender;
        this.senderStateIssue = senderStateIssue;
        this.contentType = contentType;
        this.rerequestCount = rerequestCount;
    }

    public Msg toProto() {
        HomeFeedRerequest.Builder builder = HomeFeedRerequest.newBuilder()
                .setId(contentId)
                .setRerequestType(senderStateIssue ? HomeFeedRerequest.RerequestType.SENDER_STATE : HomeFeedRerequest.RerequestType.PAYLOAD)
                .setContentType(contentType);

        return Msg.newBuilder()
                .setId(id)
                .setToUid(Long.parseLong(originalSender.rawId()))
                .setHomeFeedRerequest(builder)
                .setRerequestCount(rerequestCount)
                .build();
    }
}
