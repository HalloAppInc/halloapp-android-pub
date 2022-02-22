package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.WhisperKeys;

public class WhisperKeysDownloadIq extends HalloIq {

    private final UserId userId;

    WhisperKeysDownloadIq(@NonNull String forUser, @NonNull UserId userId) {
        this.userId = userId;
    }

    @Override
    public Iq toProtoIq() {
        return Iq.newBuilder()
                .setId(getStanzaId())
                .setWhisperKeys(
                        WhisperKeys.newBuilder()
                                .setAction(WhisperKeys.Action.GET)
                                .setUid(Long.parseLong(userId.rawId())))
                .build();
    }
}

