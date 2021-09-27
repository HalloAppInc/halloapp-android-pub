package com.halloapp.crypto.keys;

import androidx.annotation.NonNull;

import com.halloapp.crypto.CryptoByteUtils;

public abstract class Key {
    private final byte[] key;

    public Key(byte[] key) {
        this.key = key;
    }

    public byte[] getKeyMaterial() {
        return key;
    }

    @NonNull
    @Override
    public String toString() {
        return CryptoByteUtils.obfuscate(key);
    }
}
