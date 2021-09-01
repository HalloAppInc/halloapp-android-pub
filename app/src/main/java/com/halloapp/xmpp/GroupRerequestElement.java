package com.halloapp.xmpp;

import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.GroupFeedRerequest;
import com.halloapp.proto.server.Msg;
import com.halloapp.util.RandomId;

public class GroupRerequestElement {

    public final String id;
    public final String contentId;
    public final UserId originalSender;
    public final GroupId groupId;
    public final boolean senderStateIssue;
    public final int rerequestCount;

    public GroupRerequestElement(UserId originalSender, GroupId groupId, String contentId, boolean senderStateIssue, int rerequestCount) {
        this.id = RandomId.create();
        this.contentId = contentId;
        this.originalSender = originalSender;
        this.groupId = groupId;
        this.senderStateIssue = senderStateIssue;
        this.rerequestCount = rerequestCount;
    }

    public Msg toProto() {
        GroupFeedRerequest.Builder builder = GroupFeedRerequest.newBuilder()
                .setId(contentId)
                .setGid(groupId.rawId())
                .setRerequestType(senderStateIssue ? GroupFeedRerequest.RerequestType.SENDER_STATE : GroupFeedRerequest.RerequestType.PAYLOAD);

        return Msg.newBuilder()
                .setId(id)
                .setToUid(Long.parseLong(originalSender.rawId()))
                .setGroupFeedRerequest(builder)
                .setRerequestCount(rerequestCount)
                .build();
    }
}
