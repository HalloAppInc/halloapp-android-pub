package com.halloapp.crypto.keys;

import com.google.crypto.tink.subtle.X25519;
import com.halloapp.crypto.CryptoUtil;
import com.halloapp.util.Preconditions;

import java.security.InvalidKeyException;

public abstract class ECKey {
    private static final int KEY_SIZE_BYTES = 32;

    private final byte[] key;

    public static PrivateECKey generatePrivateKey() {
        return new PrivateECKey(X25519.generatePrivateKey());
    }

    public static PublicECKey publicFromPrivate(PrivateECKey privateECKey) throws InvalidKeyException {
        return new PublicECKey(X25519.publicFromPrivate(privateECKey.getKeyMaterial()));
    }

    public ECKey(byte[] key) {
        Preconditions.checkArgument(key.length == KEY_SIZE_BYTES);
        this.key = key;
    }

    public byte[] getKeyMaterial() {
        return key;
    }

    /**
     * Reduce the time the key material is in memory by nullifying the byte array when the
     * bytes are no longer needed. While the byte array will be passed by reference, we cannot
     * guarantee that the JVM did not move the storage address during a GC run. The old addresses
     * could still contain the key material, but we cannot access them. The current address
     * contains the key material and we can access it, so we delete it.
     */
    public void destroyKeyMaterial() {
        CryptoUtil.nullify(key);
    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        destroyKeyMaterial();
    }
}
