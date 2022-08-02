package com.halloapp.util.stats;

import com.halloapp.id.GroupId;
import com.halloapp.proto.log_events.GroupHistoryReport;
import com.halloapp.util.logs.Log;

public class GroupHistoryDecryptStats {
    public final long rowId;
    public final GroupId groupId;
    public final long addedTimestamp;
    public final int expectedCount;
    public final int decryptedCount;
    public final int rerequestCount;
    public final long lastUpdateTimestamp;

    public GroupHistoryDecryptStats(
            long rowId,
            GroupId groupId,
            long addedTimestamp,
            int expectedCount,
            int decryptedCount,
            int rerequestCount,
            long lastUpdateTimestamp
    ) {
        this.rowId = rowId;
        this.groupId = groupId;
        this.addedTimestamp = addedTimestamp;
        this.expectedCount = expectedCount;
        this.decryptedCount = decryptedCount;
        this.rerequestCount = rerequestCount;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public GroupHistoryReport toDecryptionReport() {
        GroupHistoryReport.Builder builder = GroupHistoryReport.newBuilder();
        if (groupId != null) {
            builder.setGid(groupId.rawId());
        }
        builder.setNumExpected(expectedCount);
        builder.setNumDecrypted(decryptedCount);
        builder.setRerequestCount(rerequestCount);
        builder.setTimeTakenS((int)((lastUpdateTimestamp - addedTimestamp) / 1000));
        return builder.build();
    }
}
