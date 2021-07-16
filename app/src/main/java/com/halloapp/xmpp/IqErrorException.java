package com.halloapp.xmpp;

public class IqErrorException extends Exception {

    private String id;
    private String reason;

    public IqErrorException(String id, String reason) {
        super("Server returned error response for " + id + ": " + reason);

        this.id = id;
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public String getId() {
        return id;
    }
}
