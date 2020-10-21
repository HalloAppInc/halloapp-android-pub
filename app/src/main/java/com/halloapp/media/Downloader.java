package com.halloapp.media;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.Constants;
import com.halloapp.content.Media;
import com.halloapp.util.FileUtils;
import com.halloapp.util.TailInputStream;
import com.halloapp.util.ThreadUtils;
import com.halloapp.util.logs.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Downloader {

    public interface DownloadListener {
        boolean onProgress(int percent);
    }

    public static class DownloadException extends IOException {
        final int code;

        DownloadException(int code) {
            super("http error code " + code);
            this.code = code;
        }
    }

    @WorkerThread
    private static void decrypt(@NonNull InputStream inStream, long contentLength, @Nullable byte [] mediaKey, @Nullable byte [] sha256hash, @Media.MediaType int type, @NonNull File localFile, @Nullable DownloadListener listener) throws IOException {
        OutputStream outStream = null;
        try {
            outStream = new BufferedOutputStream(new FileOutputStream(localFile));
            MessageDigest digest = null;
            if (mediaKey != null) {
                if (sha256hash == null) {
                    throw new IOException("no received sha256hash");
                }
                try {
                    digest = MessageDigest.getInstance("SHA-256");
                    inStream = new TailInputStream(new DigestInputStream(inStream, digest), 32);
                } catch (NoSuchAlgorithmException e) {
                    throw new IOException(e);
                }
                outStream = new MediaDecryptOutputStream(mediaKey, type, outStream);
            }
            int byteRead;
            long byteWritten = 0;
            final byte[] buffer = new byte[1024];
            boolean cancelled = false;
            while (!cancelled && (byteRead = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, byteRead);
                byteWritten += byteRead;
                if (contentLength != 0 && listener != null) {
                    cancelled = !listener.onProgress((int) (byteWritten * 100 / contentLength));
                }
            }
            inStream.close();
            outStream.close();
            if (mediaKey != null) {
                final byte [] receivedHmac = ((TailInputStream)inStream).getTail();
                final byte [] calculatedHmac = ((MediaDecryptOutputStream)outStream).getHmac();
                if (!Arrays.equals(receivedHmac, calculatedHmac)) {
                    throw new IOException("received hmac doesn't match calculated one");
                }
                if (!Arrays.equals(sha256hash, digest.digest())) {
                    throw new IOException("received sha256hash doesn't match calculated one");
                }
            }
        } finally {
            FileUtils.closeSilently(inStream);
            FileUtils.closeSilently(outStream);
        }
    }

    @WorkerThread
    private static void download(@NonNull InputStream inStream, long contentLength, @NonNull File localFile, @Nullable DownloadListener listener) throws IOException {
        try (OutputStream outStream = new BufferedOutputStream(new FileOutputStream(localFile, localFile.exists()))) {
            int byteRead;
            long byteWritten = 0;
            final byte[] buffer = new byte[1024];
            boolean cancelled = false;
            while (!cancelled && (byteRead = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, byteRead);
                byteWritten += byteRead;
                if (contentLength != 0 && listener != null) {
                    cancelled = !listener.onProgress((int) (byteWritten * 100 / contentLength));
                }
            }
        } finally {
            inStream.close();
        }
    }

    @WorkerThread
    public static void run(@NonNull String remotePath, @Nullable byte [] mediaKey, @Nullable byte [] sha256hash, @Media.MediaType int type, @Nullable File partialEnc, @NonNull File localFile, @Nullable DownloadListener listener) throws IOException {
        ThreadUtils.setSocketTag();
        InputStream inStream = null;
        HttpURLConnection connection = null;
        try {
            long existingBytes = 0;
            if (partialEnc != null && partialEnc.exists()) {
                existingBytes = partialEnc.length();
            }
            final URL url = new URL(remotePath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
            connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setRequestProperty("Expires", "0");
            if (existingBytes > 0) {
                connection.setRequestProperty("Range", "bytes=" + existingBytes + "-");
            }
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(30_000);
            connection.setReadTimeout(30_000);
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK && connection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
                throw new DownloadException(connection.getResponseCode());
            }
            inStream = connection.getInputStream();

            int contentLength = connection.getContentLength();
            Log.i("Downloader: content length: " + contentLength);
            if (partialEnc != null) {
                download(inStream, contentLength, partialEnc, listener);
                inStream = new FileInputStream(partialEnc);
            }
            decrypt(inStream, connection.getContentLength(), mediaKey, sha256hash, type, localFile, listener);
        } finally {
            FileUtils.closeSilently(inStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
