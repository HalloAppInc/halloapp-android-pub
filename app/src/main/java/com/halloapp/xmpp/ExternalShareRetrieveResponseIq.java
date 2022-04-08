package com.halloapp.xmpp;

import com.halloapp.proto.server.ExternalSharePostContainer;
import com.halloapp.proto.server.Iq;

public class ExternalShareRetrieveResponseIq extends HalloIq {

    public final byte[] blob;

    private ExternalShareRetrieveResponseIq(ExternalSharePostContainer externalSharePostContainer) {
        this.blob = externalSharePostContainer.getBlob().toByteArray();
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static ExternalShareRetrieveResponseIq fromProto(ExternalSharePostContainer externalSharePostContainer) {
        return new ExternalShareRetrieveResponseIq(externalSharePostContainer);
    }
}
