package com.halloapp.crypto.keys;

import androidx.annotation.NonNull;

import com.google.android.gms.common.util.Hex;
import com.halloapp.crypto.CryptoException;

public final class PublicXECKey extends XECKey {
    public PublicXECKey(byte[] key) throws CryptoException {
        super(key);
    }

    @NonNull
    @Override
    public String toString() {
        return Hex.bytesToStringLowercase(getKeyMaterial());
    }
}
