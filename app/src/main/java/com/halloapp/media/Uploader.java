package com.halloapp.media;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.Constants;
import com.halloapp.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class Uploader {

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
    static void run(@NonNull File file, @NonNull String url, @Nullable UploadListener listener) throws IOException {

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

        final OutputStream out = connection.getOutputStream();
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

        final int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new UploadException(responseCode);
        }
    }
}