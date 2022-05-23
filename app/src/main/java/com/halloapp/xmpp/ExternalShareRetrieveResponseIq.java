package com.halloapp.xmpp;

import com.halloapp.proto.server.ExternalSharePostContainer;
import com.halloapp.proto.server.Iq;

public class ExternalShareRetrieveResponseIq extends HalloIq {

    public final byte[] blob;
    public final String name;

    private ExternalShareRetrieveResponseIq(ExternalSharePostContainer externalSharePostContainer) {
        this.blob = externalSharePostContainer.getBlob().toByteArray();
        this.name = externalSharePostContainer.getName();
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static ExternalShareRetrieveResponseIq fromProto(ExternalSharePostContainer externalSharePostContainer) {
        return new ExternalShareRetrieveResponseIq(externalSharePostContainer);
    }
}
