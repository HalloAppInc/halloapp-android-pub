package com.halloapp.crypto.keys;

import com.halloapp.util.Preconditions;

public final class PrivateEdECKey extends Key {
    private static final int KEY_SIZE_BYTES = 64;

    public PrivateEdECKey(byte[] key) {
        super(key);
        Preconditions.checkArgument(key.length == KEY_SIZE_BYTES);
    }
}
