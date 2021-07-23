package com.halloapp.crypto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Implementation of HKDF as defined in https://datatracker.ietf.org/doc/html/rfc5869
 */
public class Hkdf {

    public static Hkdf getHmacSha256() {
        return new Hkdf(HMAC_SHA_256, SHA_256_HASH_LEN);
    }

    public static Hkdf getHmacSha1() {
        return new Hkdf(HMAC_SHA_1, SHA_1_HASH_LEN);
    }

    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final int SHA_256_HASH_LEN = 32;

    private static final String HMAC_SHA_1 = "HmacSHA1";
    private static final int SHA_1_HASH_LEN = 20;

    private final String macName;
    private final int hashLen;

    private Hkdf(String mac, int hashLen) {
        this.macName = mac;
        this.hashLen = hashLen;
    }

    byte[] extract(@Nullable byte[] salt, @NonNull byte[] ikm) throws NoSuchAlgorithmException, InvalidKeyException {
        if (salt == null || salt.length == 0) {
            salt = new byte[hashLen];
            CryptoByteUtils.nullify(salt);
        }
        final Mac mac = Mac.getInstance(macName);
        final SecretKeySpec secretKeySpec = new SecretKeySpec(salt, macName);
        mac.init(secretKeySpec);
        return mac.doFinal(ikm);
    }

    byte[] expand(@NonNull byte[] prk, @Nullable byte[] info, int L) throws NoSuchAlgorithmException, InvalidKeyException {
        if (L > 255 * hashLen) {
            throw new IllegalArgumentException("Cannot expand to length " + L);
        }

        if (info == null) {
            info = new byte[0];
        }

        final Mac mac = Mac.getInstance(macName);
        final SecretKeySpec secretKeySpec = new SecretKeySpec(prk, macName);
        mac.init(secretKeySpec);

        int N = (int) Math.ceil((double) L / hashLen);

        byte[] T = new byte[0];
        byte[] prevT = T;
        for (int i=1; i<=N; i++) {
            prevT = mac.doFinal(CryptoByteUtils.concat(prevT, info, new byte[]{(byte) i}));
            T = CryptoByteUtils.concat(T, prevT);
        }

        return Arrays.copyOfRange(T, 0, L);
    }

    public byte[] compute(@NonNull byte[] ikm, @Nullable byte[] salt, @Nullable byte[] info, int L) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] prk = extract(salt, ikm);
        return expand(prk, info, L);
    }
}
