package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.halloapp.crypto.group.GroupSetupInfo;
import com.halloapp.id.GroupId;
import com.halloapp.proto.server.HistoryResend;
import com.halloapp.proto.server.Iq;
import com.halloapp.util.RandomId;
import com.halloapp.xmpp.HalloIq;

public class GroupsHistoryResendIq extends HalloIq {

    private final GroupId groupId;
    private final GroupSetupInfo groupSetupInfo;
    private final byte[] encPayload;

    protected GroupsHistoryResendIq(@NonNull GroupId groupId, @NonNull GroupSetupInfo groupSetupInfo, byte[] encPayload) {
        this.groupId = groupId;
        this.groupSetupInfo = groupSetupInfo;
        this.encPayload = encPayload;
    }

    @Override
    public Iq.Builder toProtoIq() {
        HistoryResend historyResend = HistoryResend.newBuilder()
                .setAudienceHash(ByteString.copyFrom(groupSetupInfo.audienceHash))
                .addAllSenderStateBundles(groupSetupInfo.senderStateBundles)
                .setEncPayload(ByteString.copyFrom(encPayload))
                .setGid(groupId.rawId())
                .setId(RandomId.create())
                .build();
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setId(getStanzaId())
                .setHistoryResend(historyResend);
    }
}
