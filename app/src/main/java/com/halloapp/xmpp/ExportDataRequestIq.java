package com.halloapp.xmpp;

import com.halloapp.proto.server.ExportData;
import com.halloapp.proto.server.Iq;

public class ExportDataRequestIq extends HalloIq {

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setExportData(ExportData.newBuilder().build());
    }
}

