package com.halloapp.media;

import androidx.annotation.NonNull;

import com.halloapp.content.Media;
import com.halloapp.util.logs.Log;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MediaDecryptOutputStream extends FilterOutputStream {

    private final Cipher cipher;
    private final Mac mac;

    private final byte[] singleByteBuffer = new byte[1];

    private byte[] hmac;

    private boolean closed;

    public MediaDecryptOutputStream(@NonNull byte[] mediaKey, @Media.MediaType int type, @NonNull OutputStream os) throws IOException {
        this(mediaKey, type, -1, os);
    }

    public MediaDecryptOutputStream(@NonNull byte[] mediaKey, @Media.MediaType int type, int chunkNumber, @NonNull OutputStream os) throws IOException {
        super(os);

        final MediaKeys keys = new MediaKeys(mediaKey, type, chunkNumber);

        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            Log.e("MediaDecryptOutputStream: failed to create cipher", e);
            throw new IOException(e);
        }

        try {
            this.cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keys.aesKey, "AES"), new IvParameterSpec(keys.iv));
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            Log.e("MediaDecryptOutputStream: failed to init cipher", e);
            throw new IOException(e);
        }

        try {
            mac = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            Log.e("MediaDecryptOutputStream: failed to create mac", e);
            throw new IOException(e);
        }

        try {
            this.mac.init(new SecretKeySpec(keys.hmacKey, "HmacSHA256"));
        } catch (InvalidKeyException e) {
            Log.e("MediaDecryptOutputStream: failed to init mac", e);
            throw new IOException(e);
        }
    }

    public void write(int b) throws IOException {
        singleByteBuffer[0] = (byte) b;
        mac.update(singleByteBuffer);
        final byte[] buffer = cipher.update(singleByteBuffer, 0, 1);
        if (buffer != null) {
            out.write(buffer);
        }
    }

    public void write(@NonNull byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(@NonNull byte[] b, int off, int len) throws IOException {
        mac.update(b, off, len);
        final byte[] buffer = cipher.update(b, off, len);
        if (buffer != null) {
            out.write(buffer);
        }
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void close() throws IOException {
        if (closed) {
            return;
        }

        closed = true;
        try {
            final byte[] buffer = cipher.doFinal();
            if (buffer != null) {
                out.write(buffer);
            }
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IOException(e);
        }

        hmac = mac.doFinal();

        out.close();
    }

    public byte[] getHmac() {
        if (!closed) {
            throw new IllegalStateException("need to close stream before getting hmac");
        }
        return hmac;
    }
}
