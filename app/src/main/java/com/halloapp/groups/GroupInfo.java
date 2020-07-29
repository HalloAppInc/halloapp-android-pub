package com.halloapp.groups;

import com.halloapp.id.GroupId;

import java.util.List;

public class GroupInfo {
    public final GroupId groupId;
    public final String name;
    public final String description;
    public final String avatar;

    public final List<MemberInfo> members;

    public GroupInfo(GroupId groupId, String name, String description, String avatar, List<MemberInfo> members) {
        this.groupId = groupId;
        this.name = name;
        this.description = description;
        this.avatar = avatar;
        this.members = members;
    } 
}
