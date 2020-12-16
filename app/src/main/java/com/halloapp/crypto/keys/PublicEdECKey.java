package com.halloapp.crypto.keys;

import androidx.annotation.NonNull;

import com.google.android.gms.common.util.Hex;
import com.halloapp.util.Preconditions;

public final class PublicEdECKey extends Key {
    private static final int KEY_SIZE_BYTES = 32;

    public PublicEdECKey(byte[] key) {
        super(key);
        Preconditions.checkArgument(key.length == KEY_SIZE_BYTES);
    }

    @NonNull
    @Override
    public String toString() {
        return Hex.bytesToStringLowercase(getKeyMaterial());
    }
}
