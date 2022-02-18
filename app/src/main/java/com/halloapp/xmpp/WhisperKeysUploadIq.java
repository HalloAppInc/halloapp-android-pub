package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.WhisperKeys;

import java.util.List;

public class WhisperKeysUploadIq extends HalloIq {

    public final List<byte[]> oneTimePreKeys;

    WhisperKeysUploadIq(@NonNull List<byte[]> oneTimePreKeys) {
        this.oneTimePreKeys = oneTimePreKeys;
    }

    @Override
    public Iq.Builder toProtoIq() {
        WhisperKeys.Builder builder = WhisperKeys.newBuilder();
        builder.setAction(WhisperKeys.Action.ADD);
        for (byte[] oneTimePreKey : oneTimePreKeys) {
            builder.addOneTimeKeys(ByteString.copyFrom(oneTimePreKey));
        }
        return Iq.newBuilder()
                .setId(getStanzaId())
                .setType(Iq.Type.SET)
                .setWhisperKeys(builder);
    }
}

