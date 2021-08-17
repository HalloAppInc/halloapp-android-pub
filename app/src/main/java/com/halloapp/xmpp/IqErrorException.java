package com.halloapp.xmpp;

import androidx.annotation.Nullable;

import com.halloapp.proto.server.Iq;

public class IqErrorException extends Exception {

    private final String id;
    private final String reason;
    private final Iq errorIq;

    public IqErrorException(String id, String reason) {
        super("Server returned error response for " + id + ": " + reason);

        this.id = id;
        this.reason = reason;
        this.errorIq = null;
    }

    public IqErrorException(String id, Iq errorIq) {
        super ("Server returned error response for " + id);

        this.id = id;
        this.reason = null;
        this.errorIq = errorIq;
    }

    @Nullable
    public Iq getErrorIq() {
        return errorIq;
    }

    public String getReason() {
        return reason;
    }

    public String getId() {
        return id;
    }
}
