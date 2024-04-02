package com.halloapp.crypto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.goterl.lazysodium.LazySodiumAndroid;
import com.goterl.lazysodium.SodiumAndroid;
import com.goterl.lazysodium.interfaces.Auth;
import com.goterl.lazysodium.interfaces.Box;
import com.goterl.lazysodium.interfaces.DiffieHellman;
import com.goterl.lazysodium.interfaces.Sign;
import com.halloapp.crypto.keys.PrivateEdECKey;
import com.halloapp.crypto.keys.PrivateXECKey;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.keys.PublicXECKey;
import com.halloapp.util.logs.Log;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptoUtils {
    // not instantiable
    private CryptoUtils() {}

    private static final LazySodiumAndroid lazySodium = new LazySodiumAndroid(new SodiumAndroid(), StandardCharsets.UTF_8);
    private static final Sign.Native sign = lazySodium;
    private static final Auth.Native auth = lazySodium;
    private static final Box.Native box = lazySodium;
    private static final DiffieHellman.Native dh = lazySodium;

    public static byte[] hkdf(@NonNull byte[] ikm, @Nullable byte[] salt, @Nullable byte[] info, int len) throws GeneralSecurityException {
        return Hkdf.getHmacSha256().compute(ikm, salt, info, len);
    }

    public static byte[] ecdh(@NonNull PrivateXECKey a, @NonNull PublicXECKey b) {
        byte[] sharedSecret = new byte[DiffieHellman.SCALARMULT_CURVE25519_BYTES];
        dh.cryptoScalarMult(sharedSecret, a.getKeyMaterial(), b.getKeyMaterial());
        return sharedSecret;
    }

    public static byte[] hmac(byte[] key, byte[] input) {
        byte[] ret = new byte[Auth.HMACSHA256_BYTES];
        auth.cryptoAuthHMACSha256(ret, input, input.length, key);
        return ret;
    }

    @Nullable
    public static byte[] sha256(byte[] bytes) {
        byte[] hash = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            hash = messageDigest.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            Log.e("Failed to get sha256 digest", e);
        }
        return hash;
    }

    public static byte[] generateEd25519KeyPair() {
        byte[] publicKey = new byte[Sign.ED25519_PUBLICKEYBYTES];
        byte[] privateKey = new byte[Sign.ED25519_SECRETKEYBYTES];
        sign.cryptoSignKeypair(publicKey, privateKey);
        return CryptoByteUtils.concat(publicKey, privateKey);
    }

    public static byte[] generateX25519PrivateKey() {
        byte[] x25519PublicKey = new byte[Box.CURVE25519XSALSA20POLY1305_PUBLICKEYBYTES];
        byte[] x25519PrivateKey = new byte[Box.CURVE25519XSALSA20POLY1305_SECRETKEYBYTES];
        box.cryptoBoxKeypair(x25519PublicKey, x25519PrivateKey);
        return x25519PrivateKey;
    }

    public static byte[] publicX25519FromPrivate(byte[] privateKey) {
        byte[] publicKey = new byte[DiffieHellman.SCALARMULT_CURVE25519_BYTES];
        dh.cryptoScalarMultBase(publicKey, privateKey);
        return publicKey;
    }

    public static PublicEdECKey publicEdECKeyFromPrivate(PrivateEdECKey key) {
        byte[] publicKeyBytes = new byte[Sign.ED25519_PUBLICKEYBYTES];
        sign.cryptoSignEd25519SkToPk(publicKeyBytes, key.getKeyMaterial());
        return new PublicEdECKey(publicKeyBytes);
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

    // TODO: Rename this
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
}
