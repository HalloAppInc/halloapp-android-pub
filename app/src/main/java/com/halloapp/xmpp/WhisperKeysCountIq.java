package com.halloapp.xmpp;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.WhisperKeys;

public class WhisperKeysCountIq extends HalloIq {

    public Integer count;

    @Override
    public Iq toProtoIq() {
        return Iq.newBuilder()
                .setId(getStanzaId())
                .setType(Iq.Type.GET)
                .setWhisperKeys(WhisperKeys.newBuilder().setAction(WhisperKeys.Action.COUNT))
                .build();
    }
}

