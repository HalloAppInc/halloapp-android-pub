package com.halloapp.util;

import androidx.annotation.NonNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/*
 * Similar to <code>PushbackInputStream</code>, but pushes back by default on every read <code>tailSize</code> bytes;
 * finishes read at tail
 * */
public class TailInputStream extends FilterInputStream {

    private byte[] buffer = new byte[1024];
    private int offset = 0;

    private byte[] tail;

    private boolean done = false;

    public TailInputStream(InputStream is, int tailSize) {
        super(is);
        tail = new byte[tailSize];
    }

    public byte[] getTail() {
        if (!done) {
            throw new IllegalStateException("too early to call");
        }
        return tail;
    }

    public int read() {
        throw new RuntimeException("not implemented");
    }

    public int read(@NonNull byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(@NonNull byte[] b, int off, int len) throws IOException {
        if (!done) {
            if (buffer.length + tail.length < len) {
                byte[] tmp = new byte[len];
                System.arraycopy(buffer, 0, tmp, 0, buffer.length);
                buffer = tmp;
            }
            int readSize = 0;
            while (readSize >= 0 && offset < buffer.length) {
                readSize = super.read(buffer, offset, buffer.length - offset);
                if (readSize >= 0) {
                    offset += readSize;
                }
            }
            if (readSize < 0) {
                done = true;
                if (offset < tail.length) {
                    throw new IOException("Stream is too short");
                }
                System.arraycopy(buffer, offset - tail.length, tail, 0, tail.length);
            }
        }
        int returnLen = Math.min(len, offset - tail.length);
        if (returnLen == 0) {
            return -1;
        }
        System.arraycopy(buffer, 0, b, off, returnLen);
        System.arraycopy(buffer, returnLen, buffer, 0, buffer.length - returnLen);
        offset -= returnLen;
        return returnLen;

    }

    public long skip(long n) {
        throw new RuntimeException("not implemented");
    }

    public int available() throws IOException {
        return super.available() - tail.length;
    }

    public void close() throws IOException {
        super.close();
    }

    public boolean markSupported() {
        return false;
    }
}
