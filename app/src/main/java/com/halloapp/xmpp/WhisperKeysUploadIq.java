package com.halloapp.xmpp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.WhisperKeys;

import java.util.List;

public class WhisperKeysUploadIq extends HalloIq {

    public byte[] identityKey;
    public byte[] signedPreKey;
    public List<byte[]> oneTimePreKeys;

    WhisperKeysUploadIq(@Nullable byte[] identityKey, @Nullable byte[] signedPreKey, @NonNull List<byte[]> oneTimePreKeys) {
        this.identityKey = identityKey;
        this.signedPreKey = signedPreKey;
        this.oneTimePreKeys = oneTimePreKeys;
    }

    private boolean isFullUpload() {
        return identityKey != null && signedPreKey != null;
    }

    @Override
    public Iq toProtoIq() {
        WhisperKeys.Builder builder = WhisperKeys.newBuilder();
        builder.setAction(isFullUpload() ? WhisperKeys.Action.SET : WhisperKeys.Action.ADD);
        if (isFullUpload()) {
            builder.setIdentityKey(ByteString.copyFrom(identityKey));
            builder.setSignedKey(ByteString.copyFrom(signedPreKey));
        }
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

