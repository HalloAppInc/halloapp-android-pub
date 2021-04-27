package com.halloapp.crypto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.util.Hex;
import com.google.crypto.tink.subtle.Hkdf;
import com.google.crypto.tink.subtle.X25519;
import com.goterl.lazysodium.LazySodiumAndroid;
import com.goterl.lazysodium.SodiumAndroid;
import com.goterl.lazysodium.interfaces.Auth;
import com.goterl.lazysodium.interfaces.Box;
import com.goterl.lazysodium.interfaces.Sign;
import com.halloapp.crypto.keys.PrivateEdECKey;
import com.halloapp.crypto.keys.PrivateXECKey;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.keys.PublicXECKey;
import com.halloapp.util.logs.Log;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.util.Arrays;

public class CryptoUtils {
    // not instantiable
    private CryptoUtils() {}

    private static final LazySodiumAndroid lazySodium = new LazySodiumAndroid(new SodiumAndroid(), StandardCharsets.UTF_8);
    private static final Sign.Native sign = lazySodium;
    private static final Auth.Native auth = lazySodium;

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

    public static byte[] hkdf(@NonNull byte[] ikm, @Nullable byte[] salt, @Nullable byte[] info, int len) throws GeneralSecurityException {
        try {
            return Hkdf.computeHkdf("HMACSHA256", ikm, salt, info, len);
        } catch (GeneralSecurityException e) {
            Log.e("Hkdf security exception during computation");
            throw e;
        }
    }

    public static byte[] ecdh(@NonNull PrivateXECKey a, @NonNull PublicXECKey b) throws InvalidKeyException {
        try {
            return X25519.computeSharedSecret(a.getKeyMaterial(), b.getKeyMaterial());
        } catch (InvalidKeyException e) {
            Log.e("Invalid key during ECDH");
            throw e;
        }
    }

    public static byte[] hmac(byte[] key, byte[] input) {
        byte[] ret = new byte[Auth.HMACSHA256_BYTES];
        auth.cryptoAuthHMACSha256(ret, input, input.length, key);
        return ret;
    }

    public static byte[] generateEd25519KeyPair() {
        byte[] publicKey = new byte[Sign.ED25519_PUBLICKEYBYTES];
        byte[] privateKey = new byte[Sign.ED25519_SECRETKEYBYTES];
        sign.cryptoSignKeypair(publicKey, privateKey);
        return CryptoUtils.concat(publicKey, privateKey);
    }

    public static PublicXECKey convertPublicEdToX(PublicEdECKey ed) throws CryptoException {
        byte[] ret = new byte[Box.PUBLICKEYBYTES];
        sign.convertPublicKeyEd25519ToCurve25519(ret, ed.getKeyMaterial());
        return new PublicXECKey(ret);
    }

    public static PrivateXECKey convertPrivateEdToX(PrivateEdECKey ed) throws CryptoException {
        byte[] ret = new byte[Box.SECRETKEYBYTES];
        sign.convertSecretKeyEd25519ToCurve25519(ret, ed.getKeyMaterial());
        return new PrivateXECKey(ret);
    }

    public static byte[] sign(byte[] message, PrivateEdECKey key) {
        byte[] ret = new byte[Sign.ED25519_BYTES + message.length];
        sign.cryptoSign(ret, message, message.length, key.getKeyMaterial());
        return ret;
    }

    public static byte[] verifyDetached(byte[] message, PrivateEdECKey key) {
        byte[] ret = new byte[Sign.ED25519_BYTES];
        sign.cryptoSignDetached(ret, message, message.length, key.getKeyMaterial());
        return ret;
    }

    public static void verify(byte[] signature, byte[] message, PublicEdECKey key) throws GeneralSecurityException {
        if (!sign.cryptoSignVerifyDetached(signature, message, message.length, key.getKeyMaterial())) {
            Log.w("Invalid Ed signature");
            throw new GeneralSecurityException("Invalid signature");
        }
    }

    public static boolean verifyOpen(byte[] msgBytes, byte[] cert, int certLen, byte[] publicKey) {
        return sign.cryptoSignOpen(msgBytes, cert, certLen, publicKey);
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
            if (i >= 2 && i < bytes.length - 2) {
                sb.append("*");
            } else {
                sb.append(Hex.bytesToStringLowercase(new byte[] {bytes[i]}));
            }
        }

        return sb.toString();
    }
}
