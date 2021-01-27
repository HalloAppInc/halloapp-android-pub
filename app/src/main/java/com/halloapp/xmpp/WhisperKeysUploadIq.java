package com.halloapp.xmpp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.WhisperKeys;

import java.util.List;

public class WhisperKeysUploadIq extends HalloIq {

    public List<byte[]> oneTimePreKeys;

    WhisperKeysUploadIq(@NonNull List<byte[]> oneTimePreKeys) {
        this.oneTimePreKeys = oneTimePreKeys;
    }

    @Override
    public Iq toProtoIq() {
        WhisperKeys.Builder builder = WhisperKeys.newBuilder();
        builder.setAction(WhisperKeys.Action.ADD);
        for (byte[] oneTimePreKey : oneTimePreKeys) {
            builder.addOneTimeKeys(ByteString.copyFrom(oneTimePreKey));
        }
        return Iq.newBuilder()
                .setId(getStanzaId())
                .setType(Iq.Type.SET)
                .setWhisperKeys(builder)
                .build();
    }
}

