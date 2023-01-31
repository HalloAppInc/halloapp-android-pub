package com.halloapp.media;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.Constants;
import com.halloapp.content.Media;
import com.halloapp.crypto.CryptoByteUtils;
import com.halloapp.props.ServerProps;
import com.halloapp.util.FileUtils;
import com.halloapp.util.TailInputStream;
import com.halloapp.util.ThreadUtils;
import com.halloapp.util.logs.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.DigestInputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Downloader {

    public abstract static class DownloadListener {
        public abstract boolean onProgress(long bytes);
        public void onLogInfo(int contentLength, String cdnPop, String cdnId, String cdnCache) {}
    }

    public static class DownloadException extends IOException {
        final int code;

        DownloadException(int code) {
            super("http error code " + code);
            this.code = code;
        }
    }

    @WorkerThread
    private static void decrypt(@NonNull InputStream inStream, long contentLength, @NonNull File unencryptedFile, @Nullable byte[] mediaKey, @Nullable byte[] encSha256Hash, @Media.MediaType int type, @Nullable DownloadListener listener) throws IOException, GeneralSecurityException {
        Log.i("Downloader.decrypt using media key hash " + CryptoByteUtils.obfuscate(mediaKey));

        OutputStream outStream = null;
        try {
            outStream = new BufferedOutputStream(new FileOutputStream(unencryptedFile));
            MessageDigest digest = null;
            if (mediaKey != null) {
                if (encSha256Hash == null) {
                    throw new GeneralSecurityException("no received encSha256Hash");
                }
                try {
                    digest = MessageDigest.getInstance("SHA-256");
                    inStream = new TailInputStream(new DigestInputStream(inStream, digest), 32);
                } catch (NoSuchAlgorithmException e) {
                    throw new GeneralSecurityException(e);
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
                    cancelled = !listener.onProgress(byteWritten);
                }
            }
            inStream.close();
            outStream.close();
            if (mediaKey != null) {
                final byte [] receivedHmac = ((TailInputStream)inStream).getTail();
                final byte [] calculatedHmac = ((MediaDecryptOutputStream)outStream).getHmac();
                if (!Arrays.equals(receivedHmac, calculatedHmac)) {
                    throw new GeneralSecurityException("received hmac doesn't match calculated one");
                }
                if (!Arrays.equals(encSha256Hash, digest.digest())) {
                    throw new GeneralSecurityException("received encSha256Hash doesn't match calculated one");
                }
            }
        } finally {
            FileUtils.closeSilently(inStream);
            FileUtils.closeSilently(outStream);
        }
    }

    @WorkerThread
    private static void downloadPlaintext(@NonNull InputStream inStream, long contentLength, @NonNull File unencryptedFile, @Nullable DownloadListener listener) throws IOException, GeneralSecurityException {
        Log.i("Downloader.downloadPlaintext");
        OutputStream outStream = null;
        try {
            outStream = new BufferedOutputStream(new FileOutputStream(unencryptedFile));
            int byteRead;
            long byteWritten = 0;
            final byte[] buffer = new byte[1024];
            boolean cancelled = false;
            while (!cancelled && (byteRead = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, byteRead);
                byteWritten += byteRead;
                if (contentLength != 0 && listener != null) {
                    cancelled = !listener.onProgress(byteWritten);
                }
            }
            inStream.close();
            outStream.close();
        } finally {
            FileUtils.closeSilently(inStream);
            FileUtils.closeSilently(outStream);
        }
    }

    @WorkerThread
    public static void decryptChunkedFile(@NonNull ChunkedMediaParameters chunkedParameters, @NonNull InputStream inStream, long contentLength, @NonNull File unencryptedFile, @NonNull byte[] mediaKey, @NonNull byte[] encSha256hash, @Media.MediaType int type, @Nullable DownloadListener listener) throws IOException, GeneralSecurityException {
        Log.i("Downloader.decryptChunkedFile using media key hash " + CryptoByteUtils.obfuscate(mediaKey));

        OutputStream outStream = null;
        try {
            outStream = new BufferedOutputStream(new FileOutputStream(unencryptedFile));

            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-256");
                inStream = new DigestInputStream(inStream, digest);
            } catch (NoSuchAlgorithmException e) {
                throw new GeneralSecurityException(e);
            }

            long byteWritten = 0L;
            boolean cancelled = false;
            final byte[] buffer = new byte[1024];
            final ByteArrayOutputStream chunkBufferStream = new ByteArrayOutputStream(chunkedParameters.regularChunkPtSize);
            for (int i = 0; i <= chunkedParameters.regularChunkCount; ++i) {
                if (i == chunkedParameters.regularChunkCount && chunkedParameters.estimatedTrailingChunkPtSize == 0) {
                    break;
                }

                chunkBufferStream.reset();
                MediaDecryptOutputStream mediaDecryptOutputStream = null;
                byte[] receivedHmac = null;
                try {
                    mediaDecryptOutputStream = new MediaDecryptOutputStream(mediaKey, type, i, chunkBufferStream);
                    int chunkInputSize = i < chunkedParameters.regularChunkCount ?
                            chunkedParameters.chunkSize : chunkedParameters.trailingChunkSize;
                    int toCopySize = chunkInputSize - ChunkedMediaParameters.MAC_SIZE;
                    int byteRead;
                    while (!cancelled && toCopySize > 0 && (byteRead = inStream.read(buffer, 0, Math.min(buffer.length, toCopySize))) > 0) {
                        toCopySize -= byteRead;
                        mediaDecryptOutputStream.write(buffer, 0, byteRead);
                        byteWritten += byteRead;

                        if (contentLength != 0 && listener != null) {
                            cancelled = !listener.onProgress(byteWritten);
                        }
                    }

                    if (!cancelled) {
                        receivedHmac = new byte[ChunkedMediaParameters.MAC_SIZE];
                        toCopySize = ChunkedMediaParameters.MAC_SIZE;
                        while (toCopySize > 0 && (byteRead = inStream.read(receivedHmac, ChunkedMediaParameters.MAC_SIZE - toCopySize, toCopySize)) > 0) {
                            toCopySize -= byteRead;
                        }
                        if (toCopySize > 0) {
                            throw new IOException("stream ended before chunk hmac could be read");
                        }
                    }
                } finally {
                    FileUtils.closeSilently(mediaDecryptOutputStream);
                }

                byte[] calculatedHmac = mediaDecryptOutputStream.getHmac();
                if (!Arrays.equals(receivedHmac, calculatedHmac)) {
                    throw new GeneralSecurityException("received chunk hmac doesn't match calculated one");
                }

                outStream.write(chunkBufferStream.toByteArray());
                if (cancelled) {
                    break;
                }
            }

            if (!Arrays.equals(encSha256hash, digest.digest())) {
                throw new GeneralSecurityException("received encSha256hash doesn't match calculated one");
            }
        } finally {
            FileUtils.closeSilently(inStream);
            FileUtils.closeSilently(outStream);
        }
    }

    @WorkerThread
    private static void download(@NonNull InputStream inStream, long contentLength, @NonNull File localFile, @Nullable DownloadListener listener, @NonNull String mediaLogId) throws IOException {
        try (OutputStream outStream = new BufferedOutputStream(new FileOutputStream(localFile, localFile.exists()))) {
            int byteRead;
            long byteWritten = 0;
            final byte[] buffer = new byte[1024];
            boolean cancelled = false;
            int downloadPercent = 0;
            while (!cancelled && (byteRead = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, byteRead);
                byteWritten += byteRead;
                if (contentLength != 0 && listener != null) {
                    int newUploadPercent = (int)(byteWritten * 100 / contentLength);
                    if (newUploadPercent != downloadPercent) {
                        downloadPercent = newUploadPercent;
                        Log.i("Downloader progress for " + mediaLogId + ":" + downloadPercent + "%");
                    }
                    cancelled = !listener.onProgress(byteWritten);
                }
            }
        } finally {
            inStream.close();
        }
    }

    public static void run(@NonNull String remotePath, @Nullable byte [] mediaKey, @Nullable byte [] encSha256hash, @Media.MediaType int type, @Nullable File partialEnc, @NonNull File localFile, @Nullable DownloadListener listener, @NonNull String mediaLogId) throws IOException, GeneralSecurityException, ChunkedMediaParametersException, ForeignRemoteAuthorityException {
        run(remotePath, mediaKey, encSha256hash, type, Media.BLOB_VERSION_DEFAULT, 0, 0, partialEnc, localFile, listener, mediaLogId);
    }

    public static void run(@NonNull String remotePath, @Nullable byte [] mediaKey, @Nullable byte [] encSha256hash, @Media.MediaType int type, @Media.BlobVersion int blobVersion, int chunkSize, long blobSize, @Nullable File partialEnc, @NonNull File localFile, @Nullable DownloadListener listener, @NonNull String mediaLogId) throws IOException, GeneralSecurityException, ChunkedMediaParametersException, ForeignRemoteAuthorityException {
        ThreadUtils.setSocketTag();
        Log.i("Downloader starting download of " + mediaLogId + " from " + remotePath);
        InputStream inStream = null;
        HttpURLConnection connection = null;
        try {
            long existingBytes = 0;
            if (partialEnc != null && partialEnc.exists()) {
                existingBytes = partialEnc.length();
            }
            final URL url = new URL(remotePath);
            if (!url.getAuthority().endsWith(".halloapp.net") && !url.getAuthority().endsWith("halloapp.com") ) {
                Log.e("Attempted to download content from foreign authority " + url.getAuthority());
                throw new ForeignRemoteAuthorityException("Attempted to download from " + url.getAuthority());
            }
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
            Log.i("Downloader: content length for " + mediaLogId + ": " + contentLength);
            Log.i("Downloader: full headers for " + mediaLogId + ": " + connection.getHeaderFields());
            if (listener != null) {
                String cdnPop = connection.getHeaderField("x-amz-cf-pop");
                String cdnId = connection.getHeaderField("x-amz-cf-id");
                String cdnCache = connection.getHeaderField("x-cache");
                listener.onLogInfo(contentLength, cdnPop, cdnId, cdnCache);
            }
            if (partialEnc != null) {
                download(inStream, contentLength, partialEnc, listener, mediaLogId);
                inStream = new FileInputStream(partialEnc);
            }
            try {
                if (blobVersion == Media.BLOB_VERSION_DEFAULT) {
                    decrypt(inStream, connection.getContentLength(), localFile, mediaKey, encSha256hash, type, listener);
                } else if (blobVersion == Media.BLOB_VERSION_CHUNKED) {
                    if (mediaKey != null && encSha256hash != null) {
                        final ChunkedMediaParameters chunkedParameters = ChunkedMediaParameters.computeFromBlobSize(blobSize, chunkSize);
                        Log.d("Downloader chunkedParameters = " + chunkedParameters);
                        decryptChunkedFile(chunkedParameters, inStream, connection.getContentLength(), localFile, mediaKey, encSha256hash, type, listener);
                    } else {
                        throw new GeneralSecurityException("Downloader: cannot decrypt BLOB_VERSION_CHUNKED file when mediaKey or encSha256hash are null.");
                    }
                } else {
                    Log.e("Downloader: Unrecognized blob version for " + mediaLogId);
                    throw new GeneralSecurityException("Downloader: cannot process media with BLOB_VERSION_UNSPECIFIED.");
                }
            } catch (IOException e) {
                if (e.getCause() instanceof GeneralSecurityException) {
                    throw (GeneralSecurityException) e.getCause();
                }
                throw e;
            }
        } finally {
            FileUtils.closeSilently(inStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void runExternal(@NonNull String remotePath, @NonNull File localFile, @Nullable DownloadListener listener, @NonNull String mediaLogId) throws IOException, GeneralSecurityException, ChunkedMediaParametersException, ForeignRemoteAuthorityException {
        ThreadUtils.setSocketTag();
        Log.i("Downloader starting download of " + mediaLogId + " from " + remotePath);
        InputStream inStream = null;
        HttpURLConnection connection = null;
        try {
            long existingBytes = 0;
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
            Log.i("Downloader: content length for " + mediaLogId + ": " + contentLength);
            Log.i("Downloader: full headers for " + mediaLogId + ": " + connection.getHeaderFields());
            if (listener != null) {
                String cdnPop = connection.getHeaderField("x-amz-cf-pop");
                String cdnId = connection.getHeaderField("x-amz-cf-id");
                String cdnCache = connection.getHeaderField("x-cache");
                listener.onLogInfo(contentLength, cdnPop, cdnId, cdnCache);
            }
            downloadPlaintext(inStream, connection.getContentLength(), localFile, listener);
        } finally {
            FileUtils.closeSilently(inStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @WorkerThread
    public static void runForInitialChunks(long mediaId, @NonNull String remotePath, @NonNull byte[] mediaKey, int chunkSize, long blobSize, @NonNull File localFile, @Nullable DownloadListener downloadListener) throws IOException, ChunkedMediaParametersException, GeneralSecurityException {
        final ChunkedMediaParameters chunkedParameters = ChunkedMediaParameters.computeFromBlobSize(blobSize, chunkSize);
        final long roundedChunkCount = (((long) ServerProps.getInstance().getStreamingInitialDownloadSize()) + chunkedParameters.chunkSize - 1) / chunkedParameters.chunkSize;
        final int chunksToGet = (int) Math.min(roundedChunkCount, chunkedParameters.getChunkCount());
        final ByteBuffer byteBuffer = ByteBuffer.allocate(chunkedParameters.regularChunkPtSize);

        try (final RemoteChunkedMediaResource remoteResource = new RemoteChunkedMediaResource(remotePath, mediaKey, chunkedParameters, 0);
             final CachedChunkMediaResource localResource = new CachedChunkMediaResource(mediaId, chunkedParameters, localFile)) {
            for (int i = 0; i < chunksToGet; ++i) {
                byteBuffer.clear();
                remoteResource.readChunk(i, byteBuffer);
                byteBuffer.flip();
                localResource.writeChunk(i, byteBuffer);
                if (downloadListener != null) {
                    downloadListener.onProgress(byteBuffer.limit());
                }
            }
        }
    }
}
