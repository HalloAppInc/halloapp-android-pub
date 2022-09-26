package com.halloapp.groups;

import com.halloapp.id.GroupId;
import com.halloapp.proto.clients.Background;
import com.halloapp.proto.server.ExpiryInfo;
import com.halloapp.proto.server.GroupStanza;

import java.util.List;

public class GroupInfo {
    public final GroupStanza.GroupType groupType;
    public final GroupId groupId;
    public final String name;
    public final String description;
    public final String avatar;
    public final Background background;
    public final ExpiryInfo expiryInfo;

    public final List<MemberInfo> members;

    public GroupInfo(GroupStanza.GroupType groupType, GroupId groupId, String name, String description, String avatar, Background background, List<MemberInfo> members, ExpiryInfo expiryInfo) {
        this.groupType = groupType;
        this.groupId = groupId;
        this.name = name;
        this.description = description;
        this.avatar = avatar;
        this.background = background;
        this.members = members;
        if (expiryInfo == null) {
            this.expiryInfo = ExpiryInfo.newBuilder()
                    .setExpiryType(ExpiryInfo.ExpiryType.EXPIRES_IN_SECONDS)
                    .setExpiresInSeconds(30 * 86400)
                    .build();
        } else {
            this.expiryInfo = expiryInfo;
        }
    }
}
