package com.halloapp.crypto.keys;

import com.halloapp.crypto.CryptoUtils;

public abstract class Key {
    private final byte[] key;

    public Key(byte[] key) {
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
        CryptoUtils.nullify(key);
    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        destroyKeyMaterial();
    }
}
