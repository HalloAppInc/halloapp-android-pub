package com.halloapp.xmpp;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.WhisperKeys;

import java.util.List;

public class WhisperKeysUploadIq extends HalloIq {

    public static final String ELEMENT = "whisper_keys";
    public static final String NAMESPACE = "halloapp:whisper:keys";

    private static final String ELEMENT_IDENTITY_KEY_TOKEN = "identity_key";
    private static final String ELEMENT_SIGNED_PRE_KEY_TOKEN = "signed_key";
    private static final String ELEMENT_ONE_TIME_PRE_KEY_TOKEN = "one_time_key";

    public byte[] identityKey;
    public byte[] signedPreKey;
    public List<byte[]> oneTimePreKeys;

    WhisperKeysUploadIq(@Nullable byte[] identityKey, @Nullable byte[] signedPreKey, @NonNull List<byte[]> oneTimePreKeys) {
        super(ELEMENT, NAMESPACE);
        setType(Type.set);
        this.identityKey = identityKey;
        this.signedPreKey = signedPreKey;
        this.oneTimePreKeys = oneTimePreKeys;
    }

    private boolean isFullUpload() {
        return identityKey != null && signedPreKey != null;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {

        xml.attribute("type", isFullUpload() ? "set" : "add");

        xml.rightAngleBracket();

        if (isFullUpload()) {
            xml.openElement(ELEMENT_IDENTITY_KEY_TOKEN);
            xml.append(encode(identityKey));
            xml.closeElement(ELEMENT_IDENTITY_KEY_TOKEN);

            xml.openElement(ELEMENT_SIGNED_PRE_KEY_TOKEN);
            xml.append(encode(signedPreKey));
            xml.closeElement(ELEMENT_SIGNED_PRE_KEY_TOKEN);
        }

        for (byte[] oneTimePreKey : oneTimePreKeys) {
            xml.openElement(ELEMENT_ONE_TIME_PRE_KEY_TOKEN);
            xml.append(encode(oneTimePreKey));
            xml.closeElement(ELEMENT_ONE_TIME_PRE_KEY_TOKEN);
        }

        return xml;
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
        return Iq.newBuilder().setId(getStanzaId()).setType(Iq.Type.SET).setWhisperKeys(builder.build()).build();
    }

    private static String encode(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
}

