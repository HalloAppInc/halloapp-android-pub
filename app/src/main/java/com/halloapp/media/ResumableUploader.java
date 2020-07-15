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

public class ResumableUploader {
    final static int BUFFER_SIZE = 1024;

    public interface ResumableUploadListener {
        boolean onProgress(int percent);
    }

    public static class ResumableUploadException extends IOException {
        final int code;

        ResumableUploadException(int code) {
            this.code = code;
        }
    }

    @WorkerThread
    public static int sendHeadRequest(@NonNull String url) throws IOException {

        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        connection.setRequestMethod("HEAD");
        connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Expect", "100-Continue");
        connection.setRequestProperty("Tus-Resumable", "1.0.0");
        connection.setAllowUserInteraction(false);
        connection.setDoInput(true);
        connection.setConnectTimeout(30_000);
        connection.setReadTimeout(30_000);

        final int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return connection.getHeaderFieldInt("Upload-offset", 0);
        }
        return -1;
    }

    @WorkerThread
    public static String sendPatchRequest(File file, int offset, @NonNull String url, @Nullable ResumableUploadListener listener) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        connection.setChunkedStreamingMode(BUFFER_SIZE);
        connection.setRequestMethod("PATCH");
        connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Expect", "100-Continue");
        connection.setRequestProperty("Content-Type", "application/offset+octet-stream");
        connection.setRequestProperty("Tus-Resumable", "1.0.0");
        connection.setRequestProperty("Upload-offset", String.valueOf(offset));
        connection.setAllowUserInteraction(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setConnectTimeout(30_000);
        connection.setReadTimeout(30_000);

        OutputStream out = connection.getOutputStream();

        int outStreamSize = 0;
        final InputStream in = new FileInputStream(file);
        int inStreamSize = in.available();
        final byte[] bytes = new byte[BUFFER_SIZE];
        boolean cancelled = false;
        in.skip(offset);
        while (!cancelled) {
            final int count = in.read(bytes, 0, BUFFER_SIZE);
            if (count == -1) {
                break;
            }
            out.write(bytes, 0, count);
            outStreamSize += count;
            if (inStreamSize != 0 && listener != null) {
                Log.d("Resumable Uploader:" + ((outStreamSize + offset) * 100 / inStreamSize) + "%");
                cancelled = !listener.onProgress(outStreamSize * 100 / inStreamSize);
            }
        }
        in.close();
        out.close();

        final int responseCode = connection.getResponseCode();
        String downloadUrl = connection.getHeaderField("Download-Location");
        if (responseCode != 204 || downloadUrl == null) {
            throw new ResumableUploadException(responseCode);
        }
        return downloadUrl;
    }
}
