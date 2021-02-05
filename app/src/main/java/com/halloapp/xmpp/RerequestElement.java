package com.halloapp.xmpp;

import com.google.protobuf.ByteString;
import com.halloapp.crypto.keys.EncryptedKeyStore;
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

    public RerequestElement(String messageId, UserId originalSender, int rerequestCount) {
        this.id = RandomId.create();
        this.messageId = messageId;
        this.originalSender = originalSender;
        this.rerequestCount = rerequestCount;
    }

    public Msg toProto() {
        byte[] identityKey = null;
        try {
            identityKey = EncryptedKeyStore.getInstance().getMyPublicEd25519IdentityKey().getKeyMaterial();
        } catch (Exception e) {
            Log.w("Failed to get identity key bytes for rerequest", e);
        }
        
        Rerequest.Builder builder =  Rerequest.newBuilder();
        builder.setId(messageId);
        if (identityKey != null) {
            builder.setIdentityKey(ByteString.copyFrom(identityKey));
        }

        return Msg.newBuilder()
                .setId(id)
                .setToUid(Long.parseLong(originalSender.rawId()))
                .setRerequest(builder)
                .setRerequestCount(rerequestCount)
                .build();
    }
}
