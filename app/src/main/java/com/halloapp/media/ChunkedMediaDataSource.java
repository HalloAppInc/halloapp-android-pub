package com.halloapp.media;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.BaseDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSourceException;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.halloapp.content.ContentDb;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

public class ChunkedMediaDataSource extends BaseDataSource {
    public static final class Factory implements DataSource.Factory {
        private final String remoteLocation;
        private final long mediaRowId;
        private final ChunkedMediaParameters chunkedParameters;
        private final File file;

        @Nullable private TransferListener transferListener;

        public Factory(long mediaRowId, String remoteLocation, ChunkedMediaParameters chunkedParameters, File file) {
            this.remoteLocation = remoteLocation;
            this.mediaRowId = mediaRowId;
            this.chunkedParameters = chunkedParameters;
            this.file = file;
        }

        public ChunkedMediaDataSource.Factory setTransferListener(@Nullable TransferListener transferListener) {
            this.transferListener = transferListener;
            return this;
        }

        @Override
        public DataSource createDataSource() {
            final ChunkedMediaDataSource dataSource = new ChunkedMediaDataSource(mediaRowId, remoteLocation, file, chunkedParameters);
            if (transferListener != null) {
                dataSource.addTransferListener(transferListener);
            }
            return dataSource;
        }
    }

    private final long mediaRowId;
    private final String remoteLocation;
    private final File file;
    private final ChunkedMediaParameters chunkedParameters;
    private int cachedChunkIndex = -1;

    private final ByteBuffer byteBuffer;
    private RemoteChunkedMediaResource remoteResource;
    private CachedChunkMediaResource localResource;

    private byte[] encKey;
    private Uri uri;
    private long position;
    private int initialOffset;
    private boolean opened;

    protected ChunkedMediaDataSource(long mediaRowId, String remoteLocation, @NonNull File file, ChunkedMediaParameters chunkedParameters) {
        super(true);
        this.remoteLocation = remoteLocation;
        this.mediaRowId = mediaRowId;
        this.file = file;
        this.chunkedParameters = chunkedParameters;
        this.byteBuffer = ByteBuffer.allocate(chunkedParameters.regularChunkPtSize);
    }

    @Override
    public long open(@NonNull DataSpec dataSpec) throws IOException {
        transferStarted(dataSpec);

        if (dataSpec.key == null) {
            throw new IOException("Data spec has no key set");
        }
        final long mediaId;
        try {
            mediaId = Long.parseLong(dataSpec.key);
        } catch (NumberFormatException e) {
            throw new IOException("Could not read mediaId from dataSpec", e);
        }
        if (!Uri.parse(remoteLocation).equals(dataSpec.uri)) {
            throw new IOException("Remote location address mismatch " + remoteLocation + " != " + dataSpec.uri);
        }
        uri = dataSpec.uri;

        position = dataSpec.position;
        if (position > chunkedParameters.estimatedPtSize) {
            throw new DataSourceException(DataSourceException.POSITION_OUT_OF_RANGE);
        }

        initialOffset = chunkedParameters.getChunkPtOffset(position);
        position -= initialOffset;

        encKey = ContentDb.getInstance().getMediaEncKey(mediaRowId);
        if (encKey == null) {
            throw new IOException("Could not obtain encryption key for remote resource");
        }

        localResource = new CachedChunkMediaResource(mediaId, chunkedParameters, file);

        opened = true;
        transferStarted(dataSpec);
        return dataSpec.length == C.LENGTH_UNSET ? chunkedParameters.estimatedPtSize : dataSpec.length;
    }

    @Nullable
    @Override
    public Uri getUri() {
        return uri;
    }

    @Override
    public void close() throws IOException {
        try {
            try {
                if (remoteResource != null) {
                    remoteResource.close();
                    remoteResource = null;
                }
            } finally {
                if (localResource != null) {
                    localResource.close();
                    localResource = null;
                }
            }
        } finally {
            encKey = null;
            if (opened) {
                opened = false;
                transferEnded();
            }
        }
    }

    @Override
    public int read(@NonNull byte[] target, int offset, int length) throws IOException {
        try {
            int bytesRead = 0;
            if (initialOffset > 0) {
                final byte[] tempBuffer = new byte[initialOffset];
                initialOffset = 0;
                read(tempBuffer, 0, tempBuffer.length);
            }
            while (length > 0) {
                final int chunkIndex = chunkedParameters.getChunkIndex(position);
                final int chunkPtSize = chunkedParameters.getChunkPtSize(chunkIndex);
                getChunkData(chunkIndex);
                if (byteBuffer.limit() == 0) {
                    break;
                }
                if (chunkIndex < chunkedParameters.regularChunkCount && byteBuffer.limit() != chunkPtSize) {
                    throw new IOException("Mismatch between expected chunk size " + chunkPtSize + " and actual size " + byteBuffer.limit() + " for chunk " + chunkIndex);
                }
                if (chunkIndex == chunkedParameters.regularChunkCount && (chunkPtSize < byteBuffer.limit() || chunkPtSize - byteBuffer.limit() > ChunkedMediaParameters.BLOCK_SIZE)) {
                    throw new IOException("Mismatch between estimated chunk size " + chunkPtSize + " and actual size " + byteBuffer.limit() + " for chunk " + chunkIndex);
                }
                final int ptOffset = chunkedParameters.getChunkPtOffset(position);
                if (chunkPtSize <= ptOffset) {
                    break;
                }
                final int toCopySize = Math.min(length, chunkPtSize - ptOffset);
                byteBuffer.position(ptOffset);
                byteBuffer.get(target, offset, toCopySize);
                bytesRead += toCopySize;
                position += toCopySize;
                offset += toCopySize;
                length -= toCopySize;
            }
            return bytesRead > 0 ? bytesRead : C.RESULT_END_OF_INPUT;
        } catch (ChunkedMediaParametersException | GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    private void getChunkData(int chunkIndex) throws GeneralSecurityException, IOException {
        if (cachedChunkIndex != chunkIndex) {
            if (cachedChunkIndex > chunkedParameters.getChunkCount()) {
                byteBuffer.limit(0);
            } else if (localResource.isChunkCached(chunkIndex)) {
                if (remoteResource != null) {
                    remoteResource.close();
                    remoteResource = null;
                }
                byteBuffer.clear();
                localResource.readChunk(chunkIndex, byteBuffer);
                byteBuffer.flip();
            } else {
                if (remoteResource == null) {
                    remoteResource = new RemoteChunkedMediaResource(remoteLocation, encKey, chunkedParameters, chunkIndex);
                }
                byteBuffer.clear();
                remoteResource.readChunk(chunkIndex, byteBuffer);
                byteBuffer.flip();
                localResource.writeChunk(chunkIndex, byteBuffer);
                byteBuffer.rewind();
            }
            cachedChunkIndex = chunkIndex;
        }
    }
}
