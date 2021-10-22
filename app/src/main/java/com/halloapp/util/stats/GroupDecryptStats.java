package com.halloapp.util.stats;

import com.halloapp.id.GroupId;
import com.halloapp.proto.log_events.GroupDecryptionReport;
import com.halloapp.proto.log_events.Platform;

public class GroupDecryptStats {
    public final long rowId;
    public final String messageId;
    public final GroupId groupId;
    public final boolean isComment;
    public final int rerequestCount;
    public final String failureReason;
    public final String version;
    public final String senderVersion;
    public final String senderPlatform;
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
            String senderVersion,
            String senderPlatform,
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
        this.senderVersion = senderVersion;
        this.senderPlatform = senderPlatform;
        this.originalTimestamp = originalTimestamp;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public GroupDecryptionReport toDecryptionReport() {
        Platform senderPlatform = "android".equals(this.senderPlatform) ? Platform.ANDROID : "ios".equals(this.senderPlatform) ? Platform.IOS : Platform.UNKNOWN;
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
        if (senderVersion != null) {
            builder.setSenderVersion(senderVersion);
        }
        builder.setResult(failureReason == null ? GroupDecryptionReport.Status.OK : GroupDecryptionReport.Status.FAIL)
                .setItemType(isComment ? GroupDecryptionReport.ItemType.COMMENT : GroupDecryptionReport.ItemType.POST)
                .setContentId(messageId)
                .setSenderPlatform(senderPlatform)
                .setRerequestCount(rerequestCount)
                .setTimeTakenS((int)timeTakenS);
        return builder.build();
    }
}
