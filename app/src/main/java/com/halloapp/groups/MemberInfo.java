package com.halloapp.groups;

import com.halloapp.id.UserId;
import com.halloapp.xmpp.groups.MemberElement;

public class MemberInfo {
    public final long rowId;
    public final UserId userId;
    public final @MemberElement.Type String type;
    public final String name;

    public MemberInfo(long rowId, UserId userId, @MemberElement.Type String type, String name) {
        this.rowId = rowId;
        this.userId = userId;
        this.type = type;
        this.name = name;
    }
}
