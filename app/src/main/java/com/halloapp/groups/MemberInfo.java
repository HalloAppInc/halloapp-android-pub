package com.halloapp.groups;

import com.halloapp.id.UserId;
import com.halloapp.xmpp.groups.MemberElement;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        final MemberInfo member = (MemberInfo) o;
        return rowId == member.rowId &&
                Objects.equals(userId, member.userId) &&
                Objects.equals(type, member.type) &&
                Objects.equals(name, member.name);
    }
}
