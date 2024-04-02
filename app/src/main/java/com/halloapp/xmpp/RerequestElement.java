package com.halloapp.xmpp;

import com.google.protobuf.ByteString;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.XECKey;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.Msg;
import com.halloapp.proto.server.Rerequest;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;

public class RerequestElement {

    public final String id;
    public final String messageId;
    public final UserId originalSender;
    public final int rerequestCount;
    public final byte[] teardownKey;
    public final Rerequest.ContentType contentType;

    public RerequestElement(String messageId, UserId originalSender, int rerequestCount, byte[] teardownKey, Rerequest.ContentType contentType) {
        this.id = RandomId.create();
        this.messageId = messageId;
        this.originalSender = originalSender;
        this.rerequestCount = rerequestCount;
        this.teardownKey = teardownKey;
        this.contentType = contentType;
    }

    public Msg toProto() {
        EncryptedKeyStore encryptedKeyStore = EncryptedKeyStore.getInstance();

        byte[] identityKey = null;
        try {
            identityKey = encryptedKeyStore.getMyPublicEd25519IdentityKey().getKeyMaterial();
        } catch (Exception e) {
            Log.w("Failed to get identity key bytes for rerequest", e);
        }

        byte[] outboundEphemeralKey = null;
        try {
            outboundEphemeralKey = XECKey.publicFromPrivate(encryptedKeyStore.getOutboundEphemeralKey(originalSender)).getKeyMaterial();
        } catch (Exception e) {
            Log.w("Failed to get ephemeral key bytes for rerequest", e);
        }

        // TODO: signed pre-key id!
        Integer otpkId = encryptedKeyStore.getPeerOneTimePreKeyId(originalSender);
        
        Rerequest.Builder builder =  Rerequest.newBuilder();
        builder.setId(messageId);
        if (identityKey != null) {
            builder.setIdentityKey(ByteString.copyFrom(identityKey));
        }
        if (teardownKey != null) {
            builder.setMessageEphemeralKey(ByteString.copyFrom(teardownKey));
        }
        if (otpkId != null) {
            builder.setOneTimePreKeyId(otpkId);
        }
        if (outboundEphemeralKey != null) {
            builder.setSessionSetupEphemeralKey(ByteString.copyFrom(outboundEphemeralKey));
        }
        if (contentType != null) {
            builder.setContentType(contentType);
        }

        return Msg.newBuilder()
                .setId(id)
                .setToUid(Long.parseLong(originalSender.rawId()))
                .setRerequest(builder)
                .setRerequestCount(rerequestCount)
                .build();
    }
}
