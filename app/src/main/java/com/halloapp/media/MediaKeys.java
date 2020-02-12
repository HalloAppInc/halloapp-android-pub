package com.halloapp.media;

import androidx.annotation.NonNull;

import com.halloapp.posts.Media;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import at.favre.lib.crypto.HKDF;

class MediaKeys {

    final byte[] iv = new byte[16];
    final byte[] aesKey = new byte[32];
    final byte[] hmacKey = new byte[32];

    MediaKeys(@NonNull byte[] mediaKey, @Media.MediaType int type) {
        final HKDF hkdf = HKDF.fromHmacSha256();
        final byte[] pseudoRandomKey = hkdf.extract((SecretKey) null, mediaKey);
        final byte[] expandedKey = hkdf.expand(pseudoRandomKey, getHkdfInfo(type).getBytes(StandardCharsets.UTF_8), 80);

        System.arraycopy(expandedKey, 0, iv, 0, iv.length);
        System.arraycopy(expandedKey, iv.length, aesKey, 0, aesKey.length);
        System.arraycopy(expandedKey, iv.length + aesKey.length, hmacKey, 0, hmacKey.length);
    }

    private static String getHkdfInfo(@Media.MediaType int type) {
        switch (type) {
            case Media.MEDIA_TYPE_IMAGE: {
                return "HalloApp image";
            }
            case Media.MEDIA_TYPE_VIDEO: {
                return "HalloApp video";
            }
            case Media.MEDIA_TYPE_UNKNOWN:
            default: {
                return "";
            }
        }
    }
}
