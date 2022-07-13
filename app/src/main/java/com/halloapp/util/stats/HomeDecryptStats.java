package com.halloapp.util.stats;

import com.halloapp.proto.log_events.HomeDecryptionReport;
import com.halloapp.proto.log_events.Platform;

public class HomeDecryptStats {
    public final long rowId;
    public final String messageId;
    public final boolean isComment;
    public final int rerequestCount;
    public final String failureReason;
    public final String version;
    public final String senderVersion;
    public final String senderPlatform;
    public final long originalTimestamp;
    public final long lastUpdatedTimestamp;

    public HomeDecryptStats(
            long rowId,
            String messageId,
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
        this.isComment = isComment;
        this.rerequestCount = rerequestCount;
        this.failureReason = failureReason;
        this.version = version;
        this.senderVersion = senderVersion;
        this.senderPlatform = senderPlatform;
        this.originalTimestamp = originalTimestamp;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public HomeDecryptionReport toDecryptionReport() {
        Platform senderPlatform = "android".equals(this.senderPlatform) ? Platform.ANDROID : "ios".equals(this.senderPlatform) ? Platform.IOS : Platform.UNKNOWN;
        long timeTakenS = (lastUpdatedTimestamp - originalTimestamp) / 1000L;
        HomeDecryptionReport.Builder builder = HomeDecryptionReport.newBuilder();
        if (failureReason != null) {
            builder.setReason(failureReason);
        }
        if (version != null) {
            builder.setOriginalVersion(version);
        }
        if (senderVersion != null) {
            builder.setSenderVersion(senderVersion);
        }
        builder.setResult(failureReason == null ? HomeDecryptionReport.Status.OK : HomeDecryptionReport.Status.FAIL)
                .setItemType(isComment ? HomeDecryptionReport.ItemType.COMMENT : HomeDecryptionReport.ItemType.POST)
                .setContentId(messageId)
                .setSenderPlatform(senderPlatform)
                .setRerequestCount(rerequestCount)
                .setTimeTakenS((int)timeTakenS);
        return builder.build();
    }
}
