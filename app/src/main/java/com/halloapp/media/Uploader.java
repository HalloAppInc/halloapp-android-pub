package com.halloapp.media;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.Constants;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Uploader {

    public interface UploadListener {
        boolean onProgress(int percent);
    }

    public static class UploadException extends IOException {
        public final int code;

        UploadException(int code) {
            this.code = code;
        }
    }

    @WorkerThread
    static public String run(@NonNull File file, @Nullable UploadListener listener) throws IOException {
        final String hash = FileUtils.getFileMD5(file);

        final String boundary = "----WebKitFormBoundaryZJvxBSLQp0sE6qAB";
        final String endLine = "\r\n";

        final URL url = new URL("https://www.stukalov.com/halloapp/upload/upload.php");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        connection.setRequestProperty("Authorization", "Basic aGFsbG9hcHA6Zjc2VWlBWHlrRzNkWTU0");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Expect", "100-Continue");
        connection.setAllowUserInteraction(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setConnectTimeout(30_000);
        connection.setReadTimeout(30_000);

        final OutputStream out = connection.getOutputStream();
        out.write(("--" + boundary + endLine).getBytes());
        out.write(("Content-Disposition: form-data; name=\"hash\"" + endLine + endLine).getBytes());
        out.write((hash + endLine).getBytes());
        out.write(("--" + boundary + endLine).getBytes());
        out.write(("Content-Disposition: form-data; name=\"media\"; filename=\"" + file.getName() + "\"" + endLine).getBytes());
        out.write(("Content-Type: application/octet-stream" + endLine + endLine).getBytes());
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
        out.write((endLine + "--" + boundary + "--" + endLine).getBytes());
        out.close();

        if (cancelled) {
            return "";
        }

        final int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new UploadException(responseCode);
        }

        final StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(connection.getInputStream()))) {
            final char[] buffer = new char[0x2000];
            int read;
            do {
                read = reader.read(buffer, 0, buffer.length);
                if (read > 0) {
                    response.append(buffer, 0, read);
                }
            }
            while (read >= 0);
        }
        return response.toString();
    }
}