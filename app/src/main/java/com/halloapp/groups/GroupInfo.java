package com.halloapp.groups;

import com.halloapp.id.GroupId;
import com.halloapp.proto.clients.Background;

import java.util.List;

public class GroupInfo {
    public final GroupId groupId;
    public final String name;
    public final String description;
    public final String avatar;
    public final Background background;

    public final List<MemberInfo> members;

    public GroupInfo(GroupId groupId, String name, String description, String avatar, Background background, List<MemberInfo> members) {
        this.groupId = groupId;
        this.name = name;
        this.description = description;
        this.avatar = avatar;
        this.background = background;
        this.members = members;
    } 
}
