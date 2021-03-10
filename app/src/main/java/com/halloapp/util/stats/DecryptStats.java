package com.halloapp.util.stats;

import com.halloapp.proto.log_events.DecryptionReport;
import com.halloapp.proto.log_events.Platform;

public class DecryptStats {
    public final long rowId;
    public final String messageId;
    public final int rerequestCount;
    public final String failureReason;
    public final String version;
    public final String senderVersion;
    public final String senderPlatform;
    public final long originalTimestamp;
    public final long lastUpdatedTimestamp;

    public DecryptStats(
            long rowId,
            String messageId,
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
        this.rerequestCount = rerequestCount;
        this.failureReason = failureReason;
        this.version = version;
        this.senderVersion = senderVersion;
        this.senderPlatform = senderPlatform;
        this.originalTimestamp = originalTimestamp;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public DecryptionReport toDecryptionReport() {
        Platform senderPlatform = "android".equals(this.senderPlatform) ? Platform.ANDROID : "ios".equals(this.senderPlatform) ? Platform.IOS : Platform.UNKNOWN;
        long timeTakenS = (lastUpdatedTimestamp - originalTimestamp) / 1000L;
        DecryptionReport.Builder builder = DecryptionReport.newBuilder();
        if (failureReason != null) {
            builder.setReason(failureReason);
        }
        builder.setResult(failureReason == null ? DecryptionReport.Status.OK : DecryptionReport.Status.FAIL)
                .setMsgId(messageId)
                .setOriginalVersion(version)
                .setSenderVersion(senderVersion)
                .setSenderPlatform(senderPlatform)
                .setRerequestCount(rerequestCount)
                .setTimeTakenS((int)timeTakenS)
                .setIsSilent(false);
        return builder.build();
    }
}
