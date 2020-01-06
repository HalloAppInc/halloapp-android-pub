package com.halloapp.media;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.Constants;
import com.halloapp.util.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader {

    public interface DownloadListener {
        boolean onProgress(int percent);
    }

    public static class DownloadException extends IOException {
        public final int code;

        DownloadException(int code) {
            this.code = code;
        }
    }

    public static void run(@NonNull String remotePath, @NonNull File localFile, @Nullable DownloadListener listener) throws IOException {
        InputStream inStream = null;
        BufferedOutputStream outStream = null;
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
            final int totalSize = connection.getContentLength();
            outStream = new BufferedOutputStream(new FileOutputStream(localFile));
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
        } finally {
            FileUtils.closeSilently(inStream);
            FileUtils.closeSilently(outStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
