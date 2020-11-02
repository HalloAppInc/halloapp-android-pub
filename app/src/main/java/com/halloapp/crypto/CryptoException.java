package com.halloapp.crypto;

public class CryptoException extends Exception {
    public CryptoException(String reason) {
        super(reason);
    }
    public CryptoException(String reason, Throwable cause) {
        super(reason, cause);
    }
}
