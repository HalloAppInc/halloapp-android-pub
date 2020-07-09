package com.halloapp.groups;

import java.util.List;

public class GroupInfo {
    public final String gid;
    public final String name;
    public final String description;
    public final String avatar;

    public final List<MemberInfo> members;

    public GroupInfo(String gid, String name, String description, String avatar, List<MemberInfo> members) {
        this.gid = gid;
        this.name = name;
        this.description = description;
        this.avatar = avatar;
        this.members = members;
    }
}
