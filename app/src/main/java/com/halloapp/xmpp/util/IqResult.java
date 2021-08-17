package com.halloapp.xmpp.util;

public class IqResult<T> {

    private final boolean success;
    private final T result;
    private final String error;

    public IqResult() {
        this.success = false;
        this.result = null;
        this.error = null;
    }

    public IqResult(String error) {
        this.success = false;
        this.result = null;
        this.error = error;
    }

    public IqResult(T res) {
        this.success = true;
        this.result = res;
        this.error = null;
    }

    public String getError() {
        return error;
    }

    public T getResult() {
        return result;
    }

    public boolean isSuccess() {
        return success;
    }
}
