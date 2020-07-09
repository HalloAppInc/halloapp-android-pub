package com.halloapp.groups;

import com.halloapp.contacts.UserId;
import com.halloapp.xmpp.groups.MemberElement;

public class MemberInfo {
    public final UserId userId;
    public final @MemberElement.Type String type;
    public final String name;

    public MemberInfo(UserId userId, @MemberElement.Type String type, String name) {
        this.userId = userId;
        this.type = type;
        this.name = name;
    }
}
