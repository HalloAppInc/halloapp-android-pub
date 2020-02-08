package com.halloapp.media;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.Constants;
import com.halloapp.posts.Media;
import com.halloapp.util.FileUtils;
import com.halloapp.util.TailInputStream;

import java.io.BufferedOutputStream;
import java.io.File;
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

class Downloader {

    public interface DownloadListener {
        boolean onProgress(int percent);
    }

    public static class DownloadException extends IOException {
        final int code;

        DownloadException(int code) {
            this.code = code;
        }
    }

    @WorkerThread
    static void run(@NonNull String remotePath, byte [] mediaKey, byte [] sha256hash, @Media.MediaType int type, @NonNull File localFile, @Nullable DownloadListener listener) throws IOException {
        InputStream inStream = null;
        OutputStream outStream = null;
        HttpURLConnection connection = null;
        try {
            final URL url = new URL(remotePath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
            connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setRequestProperty("Expires", "0");
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(30_000);
            connection.setReadTimeout(30_000);
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new DownloadException(connection.getResponseCode());
            }
            inStream = connection.getInputStream();
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
            final int totalSize = connection.getContentLength();
            int byteRead;
            int byteWritten = 0;
            final byte[] buffer = new byte[1024];
            boolean cancelled = false;
            while (!cancelled && (byteRead = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, byteRead);
                byteWritten += byteRead;
                if (totalSize != 0 && listener != null) {
                    cancelled = !listener.onProgress(byteWritten * 100 / totalSize);
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
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
