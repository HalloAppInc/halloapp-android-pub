package com.halloapp.xmpp;

import com.halloapp.proto.server.ExportData;
import com.halloapp.proto.server.Iq;

public class ExportDataResponseIq extends HalloIq {

    public final long dataReadyTs;
    public final ExportData.Status status;
    public final String dataUrl;

    private ExportDataResponseIq(ExportData exportData) {
        this.dataReadyTs = exportData.getDataReadyTs();
        this.status = exportData.getStatus();
        this.dataUrl = exportData.getDataUrl();
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static ExportDataResponseIq fromProto(ExportData exportData) {
        return new ExportDataResponseIq(exportData);
    }
}
