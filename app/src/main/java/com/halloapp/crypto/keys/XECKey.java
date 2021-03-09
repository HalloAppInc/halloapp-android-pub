package com.halloapp.crypto.keys;

import com.google.crypto.tink.subtle.X25519;
import com.halloapp.crypto.CryptoException;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;

import java.security.InvalidKeyException;

public abstract class XECKey extends Key {
    private static final int KEY_SIZE_BYTES = 32;

    public static PrivateXECKey generatePrivateKey() {
        try {
            return new PrivateXECKey(X25519.generatePrivateKey());
        } catch (CryptoException e) {
            throw new IllegalStateException("Generated key was invalid");
        }
    }

    public static PublicXECKey publicFromPrivate(PrivateXECKey privateECKey) throws InvalidKeyException, CryptoException {
        try {
            return new PublicXECKey(X25519.publicFromPrivate(privateECKey.getKeyMaterial()));
        } catch (InvalidKeyException e) {
            Log.e("Got invalid key during private to public XEC conversion");
            throw e;
        }
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
