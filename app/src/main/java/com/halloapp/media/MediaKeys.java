package com.halloapp.media;

import androidx.annotation.NonNull;

import com.halloapp.content.Media;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import at.favre.lib.crypto.HKDF;

class MediaKeys {

    final byte[] iv = new byte[16];
    final byte[] aesKey = new byte[32];
    final byte[] hmacKey = new byte[32];

    MediaKeys(@NonNull byte[] mediaKey, @Media.MediaType int type, int chunkNumber) {
        final HKDF hkdf = HKDF.fromHmacSha256();
        final byte[] pseudoRandomKey = hkdf.extract((SecretKey) null, mediaKey);
        final byte[] expandedKey = hkdf.expand(pseudoRandomKey, getHkdfInfo(type, chunkNumber).getBytes(StandardCharsets.UTF_8), 80);

        System.arraycopy(expandedKey, 0, iv, 0, iv.length);
        System.arraycopy(expandedKey, iv.length, aesKey, 0, aesKey.length);
        System.arraycopy(expandedKey, iv.length + aesKey.length, hmacKey, 0, hmacKey.length);
    }

    private static String getHkdfInfo(@Media.MediaType int type, int chunkNumber) {
        if (chunkNumber >= 0 && type != Media.MEDIA_TYPE_VIDEO) {
            throw new IllegalArgumentException("chunkNumber works only with Media.MEDIA_TYPE_VIDEO.");
        }
        switch (type) {
            case Media.MEDIA_TYPE_IMAGE: {
                return "HalloApp image";
            }
            case Media.MEDIA_TYPE_VIDEO: {
                return chunkNumber >= 0 ? "HalloApp video " + chunkNumber : "HalloApp video";
            }
            case Media.MEDIA_TYPE_AUDIO: {
                return "HalloApp audio";
            }
            case Media.MEDIA_TYPE_DOCUMENT: {
                return "HalloApp files";
            }
            case Media.MEDIA_TYPE_UNKNOWN:
            default: {
                return "";
            }
        }
    }
}
