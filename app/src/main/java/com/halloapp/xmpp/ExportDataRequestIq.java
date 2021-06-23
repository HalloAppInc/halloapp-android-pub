package com.halloapp.xmpp;

import com.halloapp.proto.server.ExportData;
import com.halloapp.proto.server.Iq;

public class ExportDataRequestIq extends HalloIq {

    @Override
    public Iq toProtoIq() {
        return Iq.newBuilder()
                .setId(getStanzaId())
                .setType(Iq.Type.SET)
                .setExportData(ExportData.newBuilder().build())
                .build();
    }
}

