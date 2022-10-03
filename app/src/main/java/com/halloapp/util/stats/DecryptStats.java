package com.halloapp.util.stats;

import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.proto.log_events.DecryptionReport;
import com.halloapp.proto.log_events.GroupDecryptionReport;
import com.halloapp.proto.log_events.Platform;
import com.halloapp.util.Preconditions;

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
    public final boolean isSilent;
    public final ChatId chatId;

    public DecryptStats(
            long rowId,
            String messageId,
            int rerequestCount,
            String failureReason,
            String version,
            String senderVersion,
            String senderPlatform,
            long originalTimestamp,
            long lastUpdatedTimestamp,
            boolean isSilent,
            ChatId chatId
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
        this.isSilent = isSilent;
        this.chatId = chatId;
    }

    public DecryptionReport toDecryptionReport() {
        Platform senderPlatform = "android".equals(this.senderPlatform) ? Platform.ANDROID : "ios".equals(this.senderPlatform) ? Platform.IOS : Platform.UNKNOWN;
        long timeTakenS = (lastUpdatedTimestamp - originalTimestamp) / 1000L;
        DecryptionReport.Builder builder = DecryptionReport.newBuilder();
        if (failureReason != null) {
            builder.setReason(failureReason);
        }
        if (version != null) {
            builder.setOriginalVersion(version);
        }
        if (senderVersion != null) {
            builder.setSenderVersion(senderVersion);
        }
        builder.setResult(failureReason == null ? DecryptionReport.Status.OK : DecryptionReport.Status.FAIL)
                .setMsgId(messageId)
                .setSenderPlatform(senderPlatform)
                .setRerequestCount(rerequestCount)
                .setTimeTakenS((int)timeTakenS)
                .setIsSilent(isSilent);
        return builder.build();
    }

    public GroupDecryptionReport toGroupDecryptionReport() {
        Platform senderPlatform = "android".equals(this.senderPlatform) ? Platform.ANDROID : "ios".equals(this.senderPlatform) ? Platform.IOS : Platform.UNKNOWN;
        long timeTakenS = (lastUpdatedTimestamp - originalTimestamp) / 1000L;
        GroupDecryptionReport.Builder builder = GroupDecryptionReport.newBuilder();
        if (failureReason != null) {
            builder.setReason(failureReason);
        }
        if (version != null) {
            builder.setOriginalVersion(version);
        }
        Preconditions.checkState(chatId instanceof GroupId);
        builder.setGid(chatId.rawId());
        if (senderVersion != null) {
            builder.setSenderVersion(senderVersion);
        }
        builder.setResult(failureReason == null ? GroupDecryptionReport.Status.OK : GroupDecryptionReport.Status.FAIL)
                .setItemType(GroupDecryptionReport.ItemType.CHAT)
                .setContentId(messageId)
                .setSenderPlatform(senderPlatform)
                .setRerequestCount(rerequestCount)
                .setTimeTakenS((int)timeTakenS);
        return builder.build();
    }
}
