package com.halloapp.xmpp.util;

public interface ResponseHandler<T> {
    void handleResponse(T response);
}
