package com.halloapp;

import com.halloapp.media.MediaDecryptOutputStream;
import com.halloapp.media.MediaEncryptOutputStream;
import com.halloapp.content.Media;
import com.halloapp.util.TailInputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;

public class EncryptDecryptMediaTest {
    final File srcFile = new File("test.txt");
    final File encFile = new File("test.txt.enc");
    final File dstFile = new File("test.txt.enc.txt");

    @After
    public void clearFiles() {
        srcFile.delete();
        encFile.delete();
        dstFile.delete();
    }

    @Test
    public void test() throws IOException {

        final String CONTENT = "Testing shows the presence, not the absence of bugs.\n\n  Edsger W. Dijkstra";

        Files.write(Paths.get(srcFile.getAbsolutePath()), CONTENT.getBytes(), StandardOpenOption.CREATE);

        final SecureRandom random = new SecureRandom();
        final byte[] key = new byte[32];
        random.nextBytes(key);

        byte[] hmacBefore;

        // encrypt
        {
            final FileInputStream is = new FileInputStream(srcFile);
            final MediaEncryptOutputStream os = new MediaEncryptOutputStream(key, Media.MEDIA_TYPE_IMAGE, new FileOutputStream(encFile));
            copyStream(is, os);
            os.close();
            is.close();

            hmacBefore = os.getHmac();
        }

        // decrypt
        {
            final TailInputStream is = new TailInputStream(new FileInputStream(encFile), 32);
            final MediaDecryptOutputStream os = new MediaDecryptOutputStream(key, Media.MEDIA_TYPE_IMAGE, new FileOutputStream(dstFile));
            copyStream(is, os);
            os.close();
            is.close();

            final byte[] tail = is.getTail();
            final byte[] hmacAfter = os.getHmac();

            Assert.assertArrayEquals("hmacBefore != hmacAfter", hmacBefore, hmacAfter);
            Assert.assertArrayEquals("tail != hmac", tail, hmacAfter);
        }

        final String result = new String(Files.readAllBytes(Paths.get(dstFile.getAbsolutePath())));
        Assert.assertEquals(CONTENT, result);
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }
}
