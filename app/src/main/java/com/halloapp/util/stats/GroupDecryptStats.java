package com.halloapp.util.stats;

import com.halloapp.id.GroupId;
import com.halloapp.proto.log_events.GroupDecryptionReport;

public class GroupDecryptStats {
    public final long rowId;
    public final String messageId;
    public final GroupId groupId;
    public final boolean isComment;
    public final int rerequestCount;
    public final String failureReason;
    public final String version;
    public final long originalTimestamp;
    public final long lastUpdatedTimestamp;

    public GroupDecryptStats(
            long rowId,
            String messageId,
            GroupId groupId,
            boolean isComment,
            int rerequestCount,
            String failureReason,
            String version,
            long originalTimestamp,
            long lastUpdatedTimestamp
    ) {
        this.rowId = rowId;
        this.messageId = messageId;
        this.groupId = groupId;
        this.isComment = isComment;
        this.rerequestCount = rerequestCount;
        this.failureReason = failureReason;
        this.version = version;
        this.originalTimestamp = originalTimestamp;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public GroupDecryptionReport toDecryptionReport() {
        long timeTakenS = (lastUpdatedTimestamp - originalTimestamp) / 1000L;
        GroupDecryptionReport.Builder builder = GroupDecryptionReport.newBuilder();
        if (failureReason != null) {
            builder.setReason(failureReason);
        }
        if (version != null) {
            builder.setOriginalVersion(version);
        }
        if (groupId != null) {
            builder.setGid(groupId.rawId());
        }
        builder.setResult(failureReason == null ? GroupDecryptionReport.Status.OK : GroupDecryptionReport.Status.FAIL)
                .setItemType(isComment ? GroupDecryptionReport.ItemType.COMMENT : GroupDecryptionReport.ItemType.POST)
                .setMsgId(messageId)
                .setRerequestCount(rerequestCount)
                .setTimeTakenS((int)timeTakenS);
        return builder.build();
    }
}
