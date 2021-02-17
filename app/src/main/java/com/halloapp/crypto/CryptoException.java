package com.halloapp.crypto;

public class CryptoException extends Exception {
    public final boolean teardownKeyMatched;
    public final byte[] teardownKey;

    public CryptoException(String reason) {
        super(reason);
        teardownKeyMatched = false;
        teardownKey = null;
    }
    public CryptoException(String reason, Throwable cause) {
        super(reason, cause);
        teardownKeyMatched = false;
        teardownKey = null;
    }
    public CryptoException(String reason, boolean teardownKeyMatched, byte[] teardownKey) {
        super(reason);
        this.teardownKeyMatched = teardownKeyMatched;
        this.teardownKey = teardownKey;
    }
}
