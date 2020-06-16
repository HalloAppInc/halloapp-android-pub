package com.halloapp.crypto.keys;

import com.google.crypto.tink.subtle.X25519;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;

import java.security.InvalidKeyException;

public abstract class XECKey extends Key {
    private static final int KEY_SIZE_BYTES = 32;

    public static PrivateXECKey generatePrivateKey() {
        return new PrivateXECKey(X25519.generatePrivateKey());
    }

    public static PublicXECKey publicFromPrivate(PrivateXECKey privateECKey) throws InvalidKeyException {
        try {
            return new PublicXECKey(X25519.publicFromPrivate(privateECKey.getKeyMaterial()));
        } catch (InvalidKeyException e) {
            Log.e("Got invalid key during private to public XEC conversion");
            throw e;
        }
    }

    public XECKey(byte[] key) {
        super(key);
        Preconditions.checkArgument(key.length == KEY_SIZE_BYTES);
    }
}
