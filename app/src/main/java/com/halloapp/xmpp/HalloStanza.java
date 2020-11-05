package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.util.RandomId;

public class HalloStanza {

    private String id;

    protected HalloStanza() {
        this.id = RandomId.create();
    }

    protected HalloStanza(@NonNull String id) {
        this.id = id;
    }

    protected void setStanzaId(String id) {
        this.id = id;
    }

    protected String getStanzaId() {
        return id;
    }

    protected void logCommonAttributes(StringBuilder sb) {
        sb.append("id=").append(id).append(',');
    }
}
