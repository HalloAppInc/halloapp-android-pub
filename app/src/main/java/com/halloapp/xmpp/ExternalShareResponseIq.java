package com.halloapp.xmpp;

import com.halloapp.proto.server.ExternalSharePost;
import com.halloapp.proto.server.Iq;

public class ExternalShareResponseIq extends HalloIq {

    public final String blobId;
    public final byte[] blob;

    private ExternalShareResponseIq(ExternalSharePost externalSharePost) {
        this.blobId = externalSharePost.getBlobId();
        this.blob = externalSharePost.getBlob().toByteArray();
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static ExternalShareResponseIq fromProto(ExternalSharePost externalSharePost) {
        return new ExternalShareResponseIq(externalSharePost);
    }
}
