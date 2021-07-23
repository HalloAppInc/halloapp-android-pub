package com.halloapp.crypto.keys;

import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;

public abstract class XECKey extends Key {
    private static final int KEY_SIZE_BYTES = 32;

    public static PrivateXECKey generatePrivateKey() {
        try {
            return new PrivateXECKey(CryptoUtils.generateX25519PrivateKey());
        } catch (CryptoException e) {
            throw new IllegalStateException("Generated key was invalid");
        }
    }

    public static PublicXECKey publicFromPrivate(PrivateXECKey privateECKey) throws CryptoException {
        return new PublicXECKey(CryptoUtils.publicX25519FromPrivate(privateECKey.getKeyMaterial()));
    }

    public XECKey(byte[] key) throws CryptoException {
        super(key);
        if (key == null) {
            throw new CryptoException("xec_null_key");
        } else if (key.length != KEY_SIZE_BYTES) {
            throw new CryptoException("xec_wrong_key_size");
        }
    }
}
