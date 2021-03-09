package com.halloapp.crypto.keys;

import com.halloapp.crypto.CryptoException;

public final class PrivateXECKey extends XECKey {
    public PrivateXECKey(byte[] key) throws CryptoException {
        super(key);
    }
}
