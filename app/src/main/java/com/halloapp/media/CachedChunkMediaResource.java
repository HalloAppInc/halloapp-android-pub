package com.halloapp.media;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.content.ContentDb;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.FileUtils;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.BitSet;

public class CachedChunkMediaResource implements AutoCloseable {
    private final long mediaId;
    private final ChunkedMediaParameters chunkedParameters;
    private final BitSet chunkSet;
    private final ContentDb contentDb;
    private final RandomAccessFile raFile;
    private final FileChannel fileChannel;
    private boolean markedAsComplete = false;

    @WorkerThread
    public CachedChunkMediaResource(long mediaId, @NonNull ChunkedMediaParameters chunkedParameters, @NonNull File file) throws IOException {
        Log.i("CachedChunkMediaResource.CachedChunkMediaResource");
        this.mediaId = mediaId;
        this.chunkedParameters = chunkedParameters;
        contentDb = ContentDb.getInstance();
        chunkSet = new BitSet(chunkedParameters.getChunkCount());
        final BitSet initChunkSet = contentDb.getMediaChunkSet(mediaId);
        if (initChunkSet != null) {
            chunkSet.or(initChunkSet);
            checkChunkSetComplete();
        }
        raFile = new RandomAccessFile(file, "rw");
        fileChannel = raFile.getChannel();
    }

    public boolean isChunkCached(int chunkIndex) {
        Log.i("CachedChunkMediaResource.isChunkCached " + chunkSet.get(chunkIndex));
        return chunkSet.get(chunkIndex);
    }

    @WorkerThread
    public void readChunk(int chunkIndex, @NonNull ByteBuffer byteBuffer) throws IOException {
        Log.i("CachedChunkMediaResource.readChunk chunkIndex: " + chunkIndex);
        chunkedParameters.assertChunkIndexInBounds(chunkIndex);
        fileChannel.read(byteBuffer, chunkIndex * chunkedParameters.regularChunkPtSize);
    }

    @WorkerThread
    public void writeChunk(int chunkIndex, @NonNull ByteBuffer byteBuffer) throws IOException {
        Log.i("CachedChunkMediaResource.writeChunk chunkIndex: " + chunkIndex);
        chunkedParameters.assertChunkIndexInBounds(chunkIndex);
        fileChannel.write(byteBuffer, chunkIndex * chunkedParameters.regularChunkPtSize);
        fileChannel.force(false);
        chunkSet.set(chunkIndex);
        contentDb.updateMediaChunkSet(mediaId, chunkSet);
        checkChunkSetComplete();
    }

    @Override
    public void close() throws IOException {
        Log.i("CachedChunkMediaResource.readChunk close");
        FileUtils.closeSilently(fileChannel);
        raFile.close();
    }

    private void checkChunkSetComplete() {
        if (markedAsComplete) {
            return;
        }
        if (chunkSet.nextClearBit(0) >= chunkedParameters.getChunkCount()) {
            markedAsComplete = true;
            Log.i("CachedChunkMediaResource.checkChunkSetComplete mark media with id: " + mediaId + " as TRANSFERRED_YES.");
            BgWorkers.getInstance().execute(() -> contentDb.markChunkedMediaTransferComplete(mediaId));
        }
    }
}
