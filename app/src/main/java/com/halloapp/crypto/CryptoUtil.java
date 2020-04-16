package com.halloapp.crypto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.crypto.tink.subtle.Hkdf;
import com.google.crypto.tink.subtle.X25519;
import com.halloapp.crypto.keys.PrivateECKey;
import com.halloapp.crypto.keys.PublicECKey;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.util.Arrays;

public class CryptoUtil {
    // not instantiable
    private CryptoUtil() {}

    public static byte[] concat(byte[] first, byte[]... rest) {
        int len = first.length;
        for (byte[] arr : rest) {
            len += arr.length;
        }
        byte[] ret = Arrays.copyOf(first, len);
        int destOffset = first.length;
        for (byte[] arr : rest) {
            System.arraycopy(arr, 0, ret, destOffset, arr.length);
            destOffset += arr.length;
        }
        return ret;
    }

    public static byte[] hkdf(@NonNull byte[] ikm, @Nullable byte[] salt, int len) throws GeneralSecurityException {
        return Hkdf.computeHkdf("HMACSHA256", ikm, salt, getStringBytes("HalloApp"), len);
    }

    public static byte[] ecdh(@NonNull PrivateECKey a, @NonNull PublicECKey b) throws InvalidKeyException {
        return X25519.computeSharedSecret(a.getKeyMaterial(), b.getKeyMaterial());
    }

    private static byte[] getStringBytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }
}
