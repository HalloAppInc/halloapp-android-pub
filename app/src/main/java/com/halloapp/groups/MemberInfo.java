package com.halloapp.groups;

import com.halloapp.Me;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.GroupMember;
import com.halloapp.xmpp.groups.MemberElement;

import java.util.Locale;
import java.util.Objects;

public class MemberInfo {
    public final long rowId;
    public final UserId userId;
    public final @MemberElement.Type String type;
    public final String name;

    public static MemberInfo fromGroupMember(GroupMember groupMember) {
        String rawUid = Long.toString(groupMember.getUid());
        boolean isMe = rawUid.equals(Me.getInstance().getUser());
        UserId uid = isMe ? UserId.ME : new UserId(rawUid);
        String type = groupMember.getType().name().toLowerCase(Locale.US);
        String name = groupMember.getName();
        return new MemberInfo(-1, uid, type, name);
    }

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

    public boolean isAdmin() {
        return MemberElement.Type.ADMIN.equals(type);
    }
}
