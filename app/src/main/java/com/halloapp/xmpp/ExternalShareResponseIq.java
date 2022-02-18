package com.halloapp.xmpp;

import com.halloapp.proto.server.ExternalSharePost;
import com.halloapp.proto.server.Iq;

public class ExternalShareResponseIq extends HalloIq {

    public final String blobId;

    private ExternalShareResponseIq(ExternalSharePost externalSharePost) {
        this.blobId = externalSharePost.getBlobId();
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static ExternalShareResponseIq fromProto(ExternalSharePost externalSharePost) {
        return new ExternalShareResponseIq(externalSharePost);
    }
}
