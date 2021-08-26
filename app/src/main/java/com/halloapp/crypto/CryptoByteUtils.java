package com.halloapp.crypto;

import androidx.annotation.Nullable;

import com.google.android.gms.common.util.Hex;
import com.halloapp.util.logs.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

        if (bytes.length < 32) {
            return "TooShort" + bytes.length;
        }

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(bytes);
            String hex = Hex.bytesToStringLowercase(hash);
            return hex.substring(0, 4) + ":" + bytes.length;
        } catch (NoSuchAlgorithmException e) {
            Log.e("Failed to get sha256 for obfuscation", e);
            return "NoAlgo";
        }
    }
}
