package com.halloapp.crypto.keys;

import androidx.annotation.NonNull;

import com.google.android.gms.common.util.Hex;

public final class PublicXECKey extends XECKey {
    public PublicXECKey(byte[] key) {
        super(key);
    }

    @NonNull
    @Override
    public String toString() {
        return Hex.bytesToStringLowercase(getKeyMaterial());
    }
}
