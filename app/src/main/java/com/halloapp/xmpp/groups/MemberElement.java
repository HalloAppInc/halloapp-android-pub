package com.halloapp.xmpp.groups;

import androidx.annotation.StringDef;

import com.halloapp.Me;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.GroupMember;

import java.util.Locale;

public class MemberElement {

    public final UserId uid;
    public final @Type String type;
    public final String name;
    public final @Action String action;
    public final @Result String result;
    public final String reason;

    @StringDef({Type.ADMIN, Type.MEMBER, Type.INVALID})
    public @interface Type {
        String INVALID = "invalid";
        String ADMIN = "admin";
        String MEMBER = "member";
    }

    @StringDef({Action.ADD, Action.REMOVE, Action.LEAVE, Action.PROMOTE, Action.DEMOTE, Action.JOIN, Action.INVALID})
    public @interface Action {
        String INVALID = "invalid";
        String ADD = "add";
        String REMOVE = "remove";
        String LEAVE = "leave";
        String PROMOTE = "promote";
        String DEMOTE = "demote";
        String JOIN = "join";
    }

    @StringDef({Result.OK, Result.FAILED, Result.INVALID})
    public @interface Result {
        String INVALID = "invalid";
        String OK = "ok";
        String FAILED = "failed";
    }

    public MemberElement(GroupMember groupMember) {
        String rawUid = Long.toString(groupMember.getUid());
        boolean isMe = rawUid.equals(Me.getInstance().getUser());
        uid = isMe ? UserId.ME : new UserId(rawUid);
        type = groupMember.getType().name().toLowerCase(Locale.US);
        name = groupMember.getName();
        action = groupMember.getAction().name().toLowerCase(Locale.US);
        result = groupMember.getResult();
        reason = groupMember.getReason();
    }
}
