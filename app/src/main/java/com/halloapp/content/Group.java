package com.halloapp.content;

import com.halloapp.id.GroupId;
import com.halloapp.proto.server.ExpiryInfo;

public class Group {
    public final long rowId;
    public final GroupId groupId;
    public final long timestamp;

    public final boolean isActive;
    public final String groupDescription;
    public final String groupAvatarId;
    public final int theme;

    public String name;
    public String inviteToken;

    public ExpiryInfo expiryInfo;

    public Group(
            long rowId,
            GroupId groupId,
            long timestamp,
            String name,
            String groupDescription,
            String groupAvatarId,
            boolean isActive,
            int theme,
            ExpiryInfo expiryInfo) {
        this.rowId = rowId;
        this.groupId = groupId;
        this.timestamp = timestamp;
        this.name = name;
        this.groupDescription = groupDescription;
        this.groupAvatarId = groupAvatarId;
        this.isActive = isActive;
        this.theme = theme;
        this.expiryInfo = expiryInfo;
    }
}
