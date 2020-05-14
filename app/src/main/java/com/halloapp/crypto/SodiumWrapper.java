package com.halloapp.crypto;

import com.goterl.lazycode.lazysodium.LazySodiumAndroid;
import com.goterl.lazycode.lazysodium.SodiumAndroid;
import com.goterl.lazycode.lazysodium.interfaces.Auth;
import com.goterl.lazycode.lazysodium.interfaces.Box;
import com.goterl.lazycode.lazysodium.interfaces.Sign;
import com.halloapp.crypto.keys.PrivateEdECKey;
import com.halloapp.crypto.keys.PrivateXECKey;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.keys.PublicXECKey;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public class SodiumWrapper {
    private static SodiumWrapper instance;

    public static SodiumWrapper getInstance() {
        if (instance == null) {
            synchronized (SodiumWrapper.class) {
                if (instance == null) {
                    instance = new SodiumWrapper();
                }
            }
        }
        return instance;
    }

    private final SodiumAndroid sodium;
    private final LazySodiumAndroid lazySodium;
    private final Sign.Native sign;
    private final Auth.Native auth;

    private SodiumWrapper() {
        sodium = new SodiumAndroid();
        lazySodium = new LazySodiumAndroid(sodium, StandardCharsets.UTF_8);
        sign = (Sign.Native) lazySodium;
        auth = lazySodium;
    }

    public byte[] generateEd25519KeyPair() {
        byte[] publicKey = new byte[Sign.ED25519_PUBLICKEYBYTES];
        byte[] privateKey = new byte[Sign.ED25519_SECRETKEYBYTES];
        sign.cryptoSignKeypair(publicKey, privateKey);
        return CryptoUtil.concat(publicKey, privateKey);
    }

    public PublicXECKey convertPublicEdToX(PublicEdECKey ed) {
        byte[] ret = new byte[Box.PUBLICKEYBYTES];
        sign.convertPublicKeyEd25519ToCurve25519(ret, ed.getKeyMaterial());
        return new PublicXECKey(ret);
    }

    public PrivateXECKey convertPrivateEdToX(PrivateEdECKey ed) {
        byte[] ret = new byte[Box.SECRETKEYBYTES];
        sign.convertSecretKeyEd25519ToCurve25519(ret, ed.getKeyMaterial());
        return new PrivateXECKey(ret);
    }

    public byte[] sign(byte[] message, PrivateEdECKey key) {
        byte[] ret = new byte[Sign.ED25519_BYTES];
        sign.cryptoSignDetached(ret, message, message.length, key.getKeyMaterial());
        return ret;
    }

    public void verify(byte[] signature, byte[] message, PublicEdECKey key) throws GeneralSecurityException {
        if (!sign.cryptoSignVerifyDetached(signature, message, message.length, key.getKeyMaterial())) {
            throw new GeneralSecurityException("Invalid signature");
        }
    }

    public byte[] hmac(byte[] key, byte[] input) {
        byte[] ret = new byte[Auth.HMACSHA256_BYTES];
        auth.cryptoAuthHMACSha256(ret, input, input.length, key);
        return ret;
    }
}
