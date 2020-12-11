package com.halloapp.xmpp.util;

import androidx.annotation.Nullable;

public interface ResponseHandler<T> {
    void handleResponse(@Nullable T response);
}
