package com.halloapp.crypto;

import androidx.annotation.Nullable;

import com.google.android.gms.common.util.Hex;

import java.util.Arrays;

public class CryptoByteUtils {
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

    public static void nullify(@Nullable byte[] first, byte[]... rest) {
        nullify(first);
        for (byte[] bytes : rest) {
            nullify(bytes);
        }
    }

    private static void nullify(@Nullable byte[] bytes) {
        if (bytes != null) {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    public static String obfuscate(@Nullable byte[] bytes) {
        if (bytes == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();

        if (bytes.length < 32) {
            for (int i=0; i<bytes.length; i++) {
                sb.append("*");
            }
            return sb.toString();
        }

        for (int i=0; i<bytes.length; i++) {
            if (i >= 1 && i < bytes.length - 1) {
                sb.append("*");
            } else {
                sb.append(Hex.bytesToStringLowercase(new byte[] {bytes[i]}));
            }
        }

        return sb.toString();
    }
}
