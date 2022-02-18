package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.GroupId;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.xmpp.HalloIq;

public class SetGroupInfoIq extends HalloIq {

    private final GroupId groupId;
    private final String name;
    private final String avatar;

    protected SetGroupInfoIq(@NonNull GroupId groupId, @Nullable String name, @Nullable String avatar) {
        this.groupId = groupId;
        this.name = name;
        this.avatar = avatar;
    }

    @Override
    public Iq.Builder toProtoIq() {
        GroupStanza.Builder builder = GroupStanza.newBuilder();
        builder.setGid(groupId.rawId());
        builder.setAction(GroupStanza.Action.SET_NAME);
        if (name != null) {
            builder.setName(name);
        }
        if (avatar != null) {
            builder.setAvatarId(avatar);
        }
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setId(getStanzaId())
                .setGroupStanza(builder);
    }
}
