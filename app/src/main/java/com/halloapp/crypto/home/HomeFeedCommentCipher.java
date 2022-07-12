package com.halloapp.crypto.home;

import androidx.annotation.NonNull;

import com.goterl.lazysodium.interfaces.Sign;
import com.halloapp.Me;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.crypto.CryptoByteUtils;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.PrivateEdECKey;
import com.halloapp.id.UserId;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class HomeFeedCommentCipher {

    private static HomeFeedCommentCipher instance;

    private final Me me;
    private final ContentDb contentDb;

    public static HomeFeedCommentCipher getInstance() {
        if (instance == null) {
            synchronized (HomeFeedCommentCipher.class) {
                if (instance == null) {
                    instance = new HomeFeedCommentCipher(Me.getInstance(), ContentDb.getInstance());
                }
            }
        }
        return instance;
    }

    HomeFeedCommentCipher(Me me, ContentDb contentDb) {
        this.me = me;
        this.contentDb = contentDb;
    }

    byte[] convertForWire(byte[] payload, @NonNull String postId) throws CryptoException {
        Post post = contentDb.getPost(postId);
        if (post == null) {
            throw new CryptoException("post_not_found");
        }

        if (post.commentKey == null || post.commentKey.length < 64) {
            throw new CryptoException("missing_comment_key");
        }

        byte[] aesKey = Arrays.copyOfRange(post.commentKey, 0, 32);
        byte[] hmacKey = Arrays.copyOfRange(post.commentKey, 32, 64);

        try {
            byte[] hashInput = CryptoByteUtils.concat(CryptoUtils.sha256(payload), me.getUser().getBytes(StandardCharsets.UTF_8), UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
            byte[] iv = CryptoUtils.hkdf(hashInput, null, null, 16);

            Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding"); // NOTE: for AES same as PKCS5
            SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            c.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
            byte[] encryptedContents = c.doFinal(payload);

            byte[] hmac = CryptoUtils.hmac(hmacKey, encryptedContents);

            byte[] ret = CryptoByteUtils.concat(iv, encryptedContents, hmac);
            CryptoByteUtils.nullify(aesKey, hmacKey, iv, hmac);
            return ret;
        } catch (GeneralSecurityException e) {
            throw new CryptoException("cipher_enc_failure", e);
        }
    }

    byte[] convertFromWire(byte[] encPayload, @NonNull String postId) throws CryptoException {
        Post post = contentDb.getPost(postId);
        if (post == null) {
            throw new CryptoException("post_not_found");
        }

        if (post.commentKey == null || post.commentKey.length < 64) {
            throw new CryptoException("missing_comment_key");
        }

        byte[] iv = Arrays.copyOfRange(encPayload, 0, 16);
        byte[] ciphertext = Arrays.copyOfRange(encPayload, 16, encPayload.length - 32);
        byte[] receivedHmac = Arrays.copyOfRange(encPayload, encPayload.length - 32, encPayload.length);

        byte[] aesKey = Arrays.copyOfRange(post.commentKey, 0, 32);
        byte[] hmacKey = Arrays.copyOfRange(post.commentKey, 32, 64);

        byte[] calculatedHmac = CryptoUtils.hmac(hmacKey, ciphertext);
        if (!Arrays.equals(calculatedHmac, receivedHmac)) {
            Log.e("Expected HMAC " + StringUtils.bytesToHexString(receivedHmac) + " but calculated " + StringUtils.bytesToHexString(calculatedHmac));
            throw new CryptoException("home_hmac_mismatch");
        }

        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding"); // NOTE: for AES same as PKCS5
            SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            c.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
            byte[] decrypted = c.doFinal(ciphertext);

            CryptoByteUtils.nullify(iv, ciphertext, aesKey, hmacKey);

            return decrypted;
        } catch (GeneralSecurityException e) {
            Log.w("Decryption failed", e);
            throw new CryptoException("home_cipher_dec_failure");
        }
    }
}
