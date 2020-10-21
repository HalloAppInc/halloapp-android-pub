package com.halloapp.media;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.Constants;
import com.halloapp.content.Media;
import com.halloapp.util.ThreadUtils;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Uploader {

    public interface UploadListener {
        boolean onProgress(int percent);
    }

    public static class UploadException extends IOException {
        final int code;

        UploadException(int code) {
            this.code = code;
        }
    }

    @WorkerThread
    public static byte [] run(@NonNull File file, @Nullable byte [] mediaKey, @Media.MediaType int type, @NonNull String url, @Nullable UploadListener listener) throws IOException {
        ThreadUtils.setSocketTag();

        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        connection.setRequestMethod("PUT");
        connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Expect", "100-Continue");
        connection.setAllowUserInteraction(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setConnectTimeout(30_000);
        connection.setReadTimeout(30_000);

        MessageDigest digest = null;
        OutputStream out = connection.getOutputStream();
        if (mediaKey != null) {
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new IOException(e);
            }
            out = new MediaEncryptOutputStream(mediaKey, type, new DigestOutputStream(connection.getOutputStream(), digest));
        }
        int outStreamSize = 0;

        final InputStream in = new FileInputStream(file);
        int inStreamSize = in.available();
        final int bufferSize = 1024;
        final byte[] bytes = new byte[bufferSize];
        boolean cancelled = false;
        while (!cancelled) {
            final int count = in.read(bytes, 0, bufferSize);
            if (count == -1) {
                break;
            }
            out.write(bytes, 0, count);
            outStreamSize += count;
            if (inStreamSize != 0 && listener != null) {
                Log.i("Uploader:" + (outStreamSize * 100 / inStreamSize) + "%");
                cancelled = !listener.onProgress(outStreamSize * 100 / inStreamSize);
            }
        }
        out.close();
        in.close();

        final int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new UploadException(responseCode);
        }

        return digest == null ? null : digest.digest();
    }
}
