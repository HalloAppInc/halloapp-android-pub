package com.halloapp.xmpp;

import com.halloapp.proto.server.ExportData;
import com.halloapp.proto.server.Iq;

public class ExportDataRequestIq extends HalloIq {

    private Iq.Type requestType = Iq.Type.GET;

    public static ExportDataRequestIq requestExport() {
        ExportDataRequestIq requestIq = new ExportDataRequestIq();
        requestIq.requestType = Iq.Type.SET;

        return requestIq;
    }

    public static ExportDataRequestIq getRequestState() {
        ExportDataRequestIq requestIq = new ExportDataRequestIq();
        requestIq.requestType = Iq.Type.GET;

        return requestIq;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(requestType)
                .setExportData(ExportData.newBuilder().build());
    }
}

