package com.halloapp.media;

import com.halloapp.content.Media;
import com.halloapp.media.ChunkedMediaParameters;
import com.halloapp.media.ChunkedMediaParametersException;
import com.halloapp.media.Downloader;
import com.halloapp.media.UploadMediaTask;
import com.halloapp.util.FileUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

public class EncryptDecryptChunkedMediaTest {
    final File srcFile = new File("test.txt");
    final File encFile = new File("test.txt.enc");
    final File dstFile = new File("test.txt.enc.txt");

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @After
    public void clearFiles() {
        srcFile.delete();
        encFile.delete();
        dstFile.delete();
    }

    private void testChunkParameters(int chunkSz, long ptSz, int expRegularChunkPtSz, int expRegularChunkCount, int expTrailingChunkSz, int expTrailingChunkPtSz, long  expBlobSize, int estTrailingChunkPtSz, long estPtSize) throws ChunkedMediaParametersException {
        final ChunkedMediaParameters inputChunkedParameters = ChunkedMediaParameters.computeFromPlaintextSize(ptSz, chunkSz);
        Assert.assertEquals(ptSz, inputChunkedParameters.estimatedPtSize);
        Assert.assertEquals(chunkSz, inputChunkedParameters.chunkSize);
        Assert.assertEquals(expRegularChunkPtSz, inputChunkedParameters.regularChunkPtSize);
        Assert.assertEquals(expRegularChunkCount, inputChunkedParameters.regularChunkCount);
        Assert.assertEquals(expTrailingChunkSz, inputChunkedParameters.trailingChunkSize);
        Assert.assertEquals(expTrailingChunkPtSz, inputChunkedParameters.estimatedTrailingChunkPtSize);
        Assert.assertEquals(expBlobSize, inputChunkedParameters.blobSize);

        final ChunkedMediaParameters outputChunkedParameters = ChunkedMediaParameters.computeFromBlobSize(inputChunkedParameters.blobSize, chunkSz);
        Assert.assertEquals(inputChunkedParameters.blobSize, outputChunkedParameters.blobSize);
        Assert.assertEquals(inputChunkedParameters.chunkSize, outputChunkedParameters.chunkSize);
        Assert.assertEquals(inputChunkedParameters.regularChunkPtSize, outputChunkedParameters.regularChunkPtSize);
        Assert.assertEquals(inputChunkedParameters.regularChunkCount, outputChunkedParameters.regularChunkCount);
        Assert.assertEquals(inputChunkedParameters.trailingChunkSize, outputChunkedParameters.trailingChunkSize);
        Assert.assertEquals(estTrailingChunkPtSz, outputChunkedParameters.estimatedTrailingChunkPtSize);
        Assert.assertEquals(estPtSize, outputChunkedParameters.estimatedPtSize);
    }

    @Test
    public void testZeroTrailingChunkParameters() throws ChunkedMediaParametersException {
        testChunkParameters(
                128,
                480,
                80,
                6,
                0,
                0,
                768,
                0,
                480);
    }

    @Test
    public void testLastChunkFullPadParameters() throws ChunkedMediaParametersException {
        testChunkParameters(
                128,
                464,
                80,
                5,
                112,
                64,
                752,
                80,
                480);
    }

    @Test
    public void testLastChunkPartialPadParameters() throws ChunkedMediaParametersException {
        testChunkParameters(
                128,
                461,
                80,
                5,
                96,
                61,
                736,
                64,
                464);
    }

    @Test
    public void testNearPtSizeLimit() throws ChunkedMediaParametersException {
        testChunkParameters(
                65536,
                140634409078736L,
                65488,
                2147483647,
                4048,
                4000,
                140737488293840L,
                4016,
                140634409078752L);
    }

    @Test
    public void test20BytesLessThanPtSizeLimit() throws ChunkedMediaParametersException {
        testChunkParameters(
                65536,
                140634409140204L,
                65488,
                2147483647,
                65504,
                65468,
                140737488355296L,
                65472,
                140634409140208L);
    }

    @Test
    public void test1ByteLessThanPtSizeLimit() throws ChunkedMediaParametersException {
        testChunkParameters(
                65536,
                140634409140223L,
                65488,
                2147483647,
                65520,
                65487,
                140737488355312L,
                65488,
                140634409140224L);
    }

    @Test
    public void testPtSizeLimitThrows() throws ChunkedMediaParametersException {
        exception.expect(ChunkedMediaParametersException.class);
        exception.expectMessage("plaintextSize divided by (chunkSize - MAC_SIZE - BLOCK_SIZE) exceeds Integer.MAX_VALUE");
        ChunkedMediaParameters.computeFromPlaintextSize(140634409140224L, 65536);
    }

    @Test
    public void testBlobSizeLimitThrows() throws ChunkedMediaParametersException {
        exception.expect(ChunkedMediaParametersException.class);
        exception.expectMessage("blobSize divided by chunkSize exceeds Integer.MAX_VALUE");
        ChunkedMediaParameters.computeFromBlobSize(140737488355328L, 65536);
    }

    @Test
    public void testChunkSizeTooSmallThrows() throws ChunkedMediaParametersException {
        exception.expect(ChunkedMediaParametersException.class);
        exception.expectMessage("chunkSize is too small, must be larger than MAC_SIZE + BLOCK_SIZE");
        ChunkedMediaParameters.computeFromPlaintextSize(16384, 48);
        exception.expect(ChunkedMediaParametersException.class);
        exception.expectMessage("chunkSize is too small, must be larger than MAC_SIZE + BLOCK_SIZE");
        ChunkedMediaParameters.computeFromBlobSize(16384, 48);
    }

    @Test
    public void testChunkSizeUnalignedThrows() throws ChunkedMediaParametersException {
        exception.expect(ChunkedMediaParametersException.class);
        exception.expectMessage("chunkSize - MAC_SIZE must be divisible by BLOCK_SIZE");
        ChunkedMediaParameters.computeFromPlaintextSize(16384, 65536 + 1);
        exception.expect(ChunkedMediaParametersException.class);
        exception.expectMessage("chunkSize - MAC_SIZE must be divisible by BLOCK_SIZE");
        ChunkedMediaParameters.computeFromBlobSize(16384, 65536 + 1);
    }

    @Test
    public void testTrailingChunkSizeUnalignedThrows() throws ChunkedMediaParametersException {
        exception.expect(ChunkedMediaParametersException.class);
        exception.expectMessage("trailingChunkSize - MAC_SIZE must be divisible by BLOCK_SIZE");
        ChunkedMediaParameters.computeFromBlobSize(65536 + 1, 65536);
    }

    @Test
    public void testTrailingChunkSizeTooSmallThrows() throws ChunkedMediaParametersException {
        exception.expect(ChunkedMediaParametersException.class);
        exception.expectMessage("trailingChunkSize is too small, must be larger than MAC_SIZE + BLOCK_SIZE");
        ChunkedMediaParameters.computeFromBlobSize(65536 + 32, 65536);
    }

    @Test
    public void test() throws IOException, GeneralSecurityException, ChunkedMediaParametersException {
        final int CHUNK_SIZE = 128;
        final String CONTENT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

        Files.write(Paths.get(srcFile.getAbsolutePath()), CONTENT.getBytes(), StandardOpenOption.CREATE);

        final SecureRandom random = new SecureRandom();
        final byte[] key = new byte[32];
        random.nextBytes(key);

        // encrypt
        final ChunkedMediaParameters inputChunkedParameters = ChunkedMediaParameters.computeFromPlaintextSize(srcFile.length(), CHUNK_SIZE);
        Assert.assertEquals(445, inputChunkedParameters.estimatedPtSize);
        Assert.assertEquals(CHUNK_SIZE, inputChunkedParameters.chunkSize);
        Assert.assertEquals(80, inputChunkedParameters.regularChunkPtSize);
        Assert.assertEquals(5, inputChunkedParameters.regularChunkCount);
        Assert.assertEquals(80, inputChunkedParameters.trailingChunkSize);
        Assert.assertEquals(45, inputChunkedParameters.estimatedTrailingChunkPtSize);
        Assert.assertEquals(720, inputChunkedParameters.blobSize);

        UploadMediaTask.encryptChunkedFile(inputChunkedParameters, srcFile, encFile, key, Media.MEDIA_TYPE_VIDEO);

        // decrypt
        final ChunkedMediaParameters outputChunkedParameters = ChunkedMediaParameters.computeFromBlobSize(encFile.length(), CHUNK_SIZE);
        Assert.assertEquals(inputChunkedParameters.blobSize, outputChunkedParameters.blobSize);
        Assert.assertEquals(inputChunkedParameters.chunkSize, outputChunkedParameters.chunkSize);
        Assert.assertEquals(inputChunkedParameters.regularChunkPtSize, outputChunkedParameters.regularChunkPtSize);
        Assert.assertEquals(inputChunkedParameters.regularChunkCount, outputChunkedParameters.regularChunkCount);
        Assert.assertEquals(inputChunkedParameters.trailingChunkSize, outputChunkedParameters.trailingChunkSize);
        Assert.assertEquals(48, outputChunkedParameters.estimatedTrailingChunkPtSize);
        Assert.assertEquals(448, outputChunkedParameters.estimatedPtSize);

        Downloader.decryptChunkedFile(outputChunkedParameters, new FileInputStream(encFile), encFile.length(), dstFile, key, FileUtils.getFileSha256(encFile), Media.MEDIA_TYPE_VIDEO, null);

        final String result = new String(Files.readAllBytes(Paths.get(dstFile.getAbsolutePath())));
        Assert.assertEquals(CONTENT, result);
    }
}
