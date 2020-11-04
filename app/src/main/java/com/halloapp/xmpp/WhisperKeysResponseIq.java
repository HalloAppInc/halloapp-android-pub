package com.halloapp.xmpp;

import com.google.protobuf.ByteString;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.WhisperKeys;

import java.util.ArrayList;
import java.util.List;

public class WhisperKeysResponseIq extends HalloIq {

    public Integer count;
    public byte[] identityKey;
    public byte[] signedPreKey;
    public List<byte[]> oneTimePreKeys;

    private WhisperKeysResponseIq(Integer count, byte[] identityKey, byte[] signedPreKey, List<byte[]> oneTimePreKeys) {
        this.count = count;
        this.identityKey = identityKey;
        this.signedPreKey = signedPreKey;
        this.oneTimePreKeys = oneTimePreKeys;
    }

    @Override
    public Iq toProtoIq() {
        return null;
    }

    public static WhisperKeysResponseIq fromProto(WhisperKeys whisperKeys) {
        Integer count = whisperKeys.getOtpKeyCount();
        byte[] identityKey = whisperKeys.getIdentityKey().toByteArray();
        byte[] signedPreKey = whisperKeys.getSignedKey().toByteArray();
        List<byte[]> oneTimePreKeys = new ArrayList<>();
        for (ByteString byteString : whisperKeys.getOneTimeKeysList()) {
            oneTimePreKeys.add(byteString.toByteArray());
        }
        return new WhisperKeysResponseIq(count, identityKey, signedPreKey, oneTimePreKeys);
    }
}

