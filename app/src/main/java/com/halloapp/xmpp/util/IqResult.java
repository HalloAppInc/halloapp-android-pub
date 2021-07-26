package com.halloapp.xmpp.util;

public class IqResult<T> {

    private boolean success;

    private T result;
    private String error;

    public IqResult() {
        this.error = null;
        success = false;
    }

    public IqResult(String error) {
        this.success = false;
        this.error = error;
    }

    public IqResult(T res) {
        this.result = res;
        this.success = true;
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
