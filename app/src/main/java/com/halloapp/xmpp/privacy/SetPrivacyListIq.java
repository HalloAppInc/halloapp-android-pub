package com.halloapp.xmpp.privacy;

import androidx.annotation.Nullable;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.Packet;
import com.halloapp.proto.server.UidElement;
import com.halloapp.xmpp.HalloIq;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.util.XmlStringBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class SetPrivacyListIq extends HalloIq {

    private final @PrivacyList.Type String type;

    private final List<UserId> usersAdd = new ArrayList<>();
    private final List<UserId> usersDelete = new ArrayList<>();

    protected SetPrivacyListIq(@PrivacyList.Type String listType, @Nullable Collection<UserId> usersToAdd, @Nullable Collection<UserId> usersToDelete) {
        this.type = listType;
        if (usersToAdd != null) {
            this.usersAdd.addAll(usersToAdd);
        }
        if (usersToDelete != null) {
            this.usersDelete.addAll(usersToDelete);
        }
    }

    @Override
    public Iq toProtoIq() {
        com.halloapp.proto.server.PrivacyList.Builder builder = com.halloapp.proto.server.PrivacyList.newBuilder();
        builder.setType(com.halloapp.proto.server.PrivacyList.Type.valueOf(type.toUpperCase(Locale.US)));
        for (UserId userId : usersAdd) {
            addUid(builder, userId, true);
        }
        for (UserId userId : usersDelete) {
            addUid(builder, userId, false);
        }
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setId(getStanzaId())
                .setPrivacyList(builder)
                .build();
    }

    private void addUid(com.halloapp.proto.server.PrivacyList.Builder builder, UserId userId, boolean add) {
        UidElement uidElement = UidElement.newBuilder()
                .setAction(add ? UidElement.Action.ADD : UidElement.Action.DELETE)
                .setUid(Long.parseLong(userId.rawId()))
                .build();
        builder.addUidElements(uidElement);
    }
}
