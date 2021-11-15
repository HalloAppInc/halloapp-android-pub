package com.halloapp.media;

public class ChunkedMediaParameters {
    public static final int DEFAULT_CHUNK_SIZE = 65536;
    public static final long DEFAULT_INITIAL_FILE_SIZE = 5L * 1024 * 1024;
    public static final int BLOCK_SIZE = 16;
    public static final int MAC_SIZE = 32;

    public final long estimatedPtSize;
    public final long blobSize;
    public final int chunkSize;
    public final int regularChunkPtSize;
    public final int regularChunkCount;
    public final int estimatedTrailingChunkPtSize;
    public final int trailingChunkSize;

    private ChunkedMediaParameters(long estimatedPtSize, long blobSize, int chunkSize, int regularChunkPtSize, int regularChunkCount, int estimatedTrailingChunkPtSize, int trailingChunkSize) {
        this.estimatedPtSize = estimatedPtSize;
        this.blobSize = blobSize;
        this.chunkSize = chunkSize;
        this.regularChunkPtSize = regularChunkPtSize;
        this.regularChunkCount = regularChunkCount;
        this.estimatedTrailingChunkPtSize = estimatedTrailingChunkPtSize;
        this.trailingChunkSize = trailingChunkSize;
    }

    public static ChunkedMediaParameters computeFromPlaintextSize(long plaintextSize, int chunkSize) throws ChunkedMediaParametersException {
        if ((chunkSize - MAC_SIZE) % BLOCK_SIZE != 0) {
            throw new ChunkedMediaParametersException("chunkSize - MAC_SIZE must be divisible by BLOCK_SIZE");
        }
        if (chunkSize <= MAC_SIZE + BLOCK_SIZE) {
            throw new ChunkedMediaParametersException("chunkSize is too small, must be larger than MAC_SIZE + BLOCK_SIZE");
        }
        if (plaintextSize / (chunkSize - MAC_SIZE - BLOCK_SIZE)  > Integer.MAX_VALUE) {
            throw new ChunkedMediaParametersException("plaintextSize divided by (chunkSize - MAC_SIZE - BLOCK_SIZE) exceeds Integer.MAX_VALUE");
        }

        final int regularChunkPlaintextSize = chunkSize - MAC_SIZE - BLOCK_SIZE;
        final int regularChunkCount = (int) (plaintextSize / regularChunkPlaintextSize);
        final int trailingChunkPlaintextSize = (int) (plaintextSize % regularChunkPlaintextSize);
        final int trailingChunkSize = trailingChunkPlaintextSize > 0 ?
                (trailingChunkPlaintextSize + (BLOCK_SIZE - trailingChunkPlaintextSize % BLOCK_SIZE) + MAC_SIZE) :
                0;
        final long blobSize = ((long) regularChunkCount) * chunkSize + trailingChunkSize;
        return new ChunkedMediaParameters(plaintextSize, blobSize, chunkSize, regularChunkPlaintextSize, regularChunkCount, trailingChunkPlaintextSize, trailingChunkSize);
    }

    public static ChunkedMediaParameters computeFromBlobSize(long blobSize, int chunkSize) throws ChunkedMediaParametersException {
        if ((chunkSize - MAC_SIZE) % BLOCK_SIZE != 0) {
            throw new ChunkedMediaParametersException("chunkSize - MAC_SIZE must be divisible by BLOCK_SIZE");
        }
        if (chunkSize <= MAC_SIZE + BLOCK_SIZE) {
            throw new ChunkedMediaParametersException("chunkSize is too small, must be larger than MAC_SIZE + BLOCK_SIZE");
        }
        if (blobSize / chunkSize > Integer.MAX_VALUE) {
            throw new ChunkedMediaParametersException("blobSize divided by chunkSize exceeds Integer.MAX_VALUE");
        }

        final int regularChunkPlaintextSize = chunkSize - MAC_SIZE - BLOCK_SIZE;
        final int regularChunkCount = (int) (blobSize / chunkSize);
        final int trailingChunkSize = (int) (blobSize % chunkSize);

        if ((trailingChunkSize - MAC_SIZE) % BLOCK_SIZE != 0) {
            throw new ChunkedMediaParametersException("trailingChunkSize - MAC_SIZE must be divisible by BLOCK_SIZE");
        }
        if (0 < trailingChunkSize && trailingChunkSize <= MAC_SIZE + BLOCK_SIZE) {
            throw new ChunkedMediaParametersException("trailingChunkSize is too small, must be larger than MAC_SIZE + BLOCK_SIZE");
        }

        // Data size can be at most BLOCK_SIZE bigger because we don't know how big the padding is.
        // We don't know the actual trailing chunk data size before decrypting it.
        final int estimatedTrailingChunkPlaintextSize = trailingChunkSize > 0 ?
                (trailingChunkSize - MAC_SIZE) :
                0;
        final long estimatedPlaintextSize = ((long) regularChunkCount) * regularChunkPlaintextSize + estimatedTrailingChunkPlaintextSize;
        return new ChunkedMediaParameters(estimatedPlaintextSize, blobSize, chunkSize, regularChunkPlaintextSize, regularChunkCount, estimatedTrailingChunkPlaintextSize, trailingChunkSize);
    }

    public int getChunkCount() {
        return regularChunkCount + (trailingChunkSize != 0 ? 1 : 0);
    }

    public int getChunkSize(int chunkIndex) {
        return chunkIndex < regularChunkCount ? chunkSize : trailingChunkSize;
    }

    public int getChunkPtSize(int chunkIndex) {
        return chunkIndex < regularChunkCount ? regularChunkPtSize : estimatedTrailingChunkPtSize;
    }

    public int getChunkIndex(long ptPosition) throws ChunkedMediaParametersException {
        if (ptPosition / regularChunkPtSize > Integer.MAX_VALUE) {
            throw new ChunkedMediaParametersException("ptPosition divided by regularChunkPtSize exceeds Integer.MAX_VALUE");
        }
        return (int) ptPosition / regularChunkPtSize;
    }

    public int getChunkPtOffset(long ptPosition) {
        return (int) ptPosition % regularChunkPtSize;
    }

    public void assertChunkIndexInBounds(int chunkIndex) {
        if (chunkIndex < 0 || chunkIndex > getChunkCount()) {
            throw new IndexOutOfBoundsException("Chunk index " + chunkIndex + " is out of range");
        }
    }

    @Override
    public String toString() {
        return "ChunkedMediaParameters {chunkSize:" + chunkSize + " estimatedPtSize:" + estimatedPtSize + " blobSize:" + blobSize + " regularChunkPtSize:" + regularChunkPtSize + " regularChunkCount:" + regularChunkCount + " estimatedTrailingChunkPtSize:" + estimatedTrailingChunkPtSize + " trailingChunkSize:" + trailingChunkSize + "}";
    }
}
