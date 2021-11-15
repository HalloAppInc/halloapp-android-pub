package com.halloapp.media;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.Constants;
import com.halloapp.content.Media;
import com.halloapp.util.FileUtils;
import com.halloapp.util.ThreadUtils;
import com.halloapp.util.logs.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class RemoteChunkedMediaResource implements AutoCloseable {
    private static final int COPY_BUFFER_SIZE = 1024;
    private static final int CONNECT_TIMEOUT = 30_000;
    private static final int READ_TIMEOUT = 30_000;

    private final byte[] encKey;
    private final ChunkedMediaParameters chunkedParameters;
    private final byte[] copyBuffer = new byte[COPY_BUFFER_SIZE];
    private final ByteArrayOutputStream chunkBufferStream;

    private HttpURLConnection connection;
    private InputStream inputStream;
    private int currentIndex;

    @WorkerThread
    public RemoteChunkedMediaResource(String remoteLocation, byte[] encKey, @NonNull ChunkedMediaParameters chunkedParameters, int initialChunkIndex) throws IOException {
        Log.d("RemoteChunkedMediaResource.RemoteChunkedMediaResource");
        chunkedParameters.assertChunkIndexInBounds(initialChunkIndex);
        this.encKey = encKey;
        this.chunkedParameters = chunkedParameters;
        this.chunkBufferStream = new ByteArrayOutputStream(chunkedParameters.regularChunkPtSize);
        this.currentIndex = initialChunkIndex;

        ThreadUtils.setSocketTag();

        try {
            final int rangeStart = initialChunkIndex * chunkedParameters.chunkSize;
            final URL url = new URL(remoteLocation);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
            connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setRequestProperty("Expires", "0");
            if (rangeStart > 0) {
                connection.setRequestProperty("Range", "bytes=" + rangeStart + "-");
            }
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK && connection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
                throw new Downloader.DownloadException(connection.getResponseCode());
            }

            inputStream = connection.getInputStream();
        } catch (IOException e) {
            FileUtils.closeSilently(inputStream);
            connection.disconnect();
            throw e;
        }
    }

    @WorkerThread
    public void readChunk(int chunkIndex, @NonNull ByteBuffer byteBuffer) throws GeneralSecurityException, IOException {
        Log.d("RemoteChunkedMediaResource.readChunk chunkIndex: " + chunkIndex);
        chunkedParameters.assertChunkIndexInBounds(chunkIndex);
        if (chunkIndex != currentIndex) {
            throw new IOException("The requested index " + chunkIndex + " is not the current one " + currentIndex);
        }

        chunkBufferStream.reset();
        MediaDecryptOutputStream mediaDecryptOutputStream = null;
        byte[] receivedHmac;
        try {
            mediaDecryptOutputStream = new MediaDecryptOutputStream(encKey, Media.MEDIA_TYPE_VIDEO, chunkIndex, chunkBufferStream);
            int chunkInputSize = chunkedParameters.getChunkSize(chunkIndex);
            int toCopySize = chunkInputSize - ChunkedMediaParameters.MAC_SIZE;
            int bytesRead;
            while (toCopySize > 0 && (bytesRead = inputStream.read(copyBuffer, 0, Math.min(copyBuffer.length, toCopySize))) > 0) {
                toCopySize -= bytesRead;
                mediaDecryptOutputStream.write(copyBuffer, 0, bytesRead);
            }

            receivedHmac = new byte[ChunkedMediaParameters.MAC_SIZE];
            toCopySize = ChunkedMediaParameters.MAC_SIZE;
            while (toCopySize > 0 && (bytesRead = inputStream.read(receivedHmac, ChunkedMediaParameters.MAC_SIZE - toCopySize, toCopySize)) > 0) {
                toCopySize -= bytesRead;
            }
            if (toCopySize > 0) {
                throw new IOException("stream ended before chunk hmac could be read");
            }
        } finally {
            FileUtils.closeSilently(mediaDecryptOutputStream);
        }
        currentIndex++;

        byte[] calculatedHmac = mediaDecryptOutputStream.getHmac();
        if (!Arrays.equals(receivedHmac, calculatedHmac)) {
            throw new GeneralSecurityException("received chunk hmac doesn't match calculated one");
        }

        byteBuffer.put(chunkBufferStream.toByteArray());
    }

    @Override
    public void close() throws IOException {
        Log.d("RemoteChunkedMediaResource.close");
        currentIndex = -1;
        FileUtils.closeSilently(inputStream);
        if (connection != null) {
            connection.disconnect();
        }
    }
}
