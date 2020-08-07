package com.halloapp.util.crashlytics;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.NoSuchElementException;

/**
 * Implementation from Crashlytics
 *
 * This is the file format they use to store in progress logs
 */
public class QueueFile implements Closeable {
    private final RandomAccessFile raf;
    int fileLength;
    private int elementCount;
    private Element first;
    private QueueFile.Element last;
    private final byte[] buffer = new byte[16];

    public QueueFile(File file) throws IOException {
        if (!file.exists()) {
            initialize(file);
        }

        this.raf = open(file);
        this.readHeader();
    }

    private static void writeInt(byte[] buffer, int offset, int value) {
        buffer[offset] = (byte)(value >> 24);
        buffer[offset + 1] = (byte)(value >> 16);
        buffer[offset + 2] = (byte)(value >> 8);
        buffer[offset + 3] = (byte)value;
    }

    private static void writeInts(byte[] buffer, int... values) {
        int offset = 0;
        int[] var3 = values;
        int var4 = values.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            int value = var3[var5];
            writeInt(buffer, offset, value);
            offset += 4;
        }

    }

    private static int readInt(byte[] buffer, int offset) {
        return ((buffer[offset] & 255) << 24) + ((buffer[offset + 1] & 255) << 16) + ((buffer[offset + 2] & 255) << 8) + (buffer[offset + 3] & 255);
    }

    private void readHeader() throws IOException {
        this.raf.seek(0L);
        this.raf.readFully(this.buffer);
        this.fileLength = readInt(this.buffer, 0);
        if ((long)this.fileLength > this.raf.length()) {
            throw new IOException("File is truncated. Expected length: " + this.fileLength + ", Actual length: " + this.raf.length());
        } else {
            this.elementCount = readInt(this.buffer, 4);
            int firstOffset = readInt(this.buffer, 8);
            int lastOffset = readInt(this.buffer, 12);
            this.first = this.readElement(firstOffset);
            this.last = this.readElement(lastOffset);
        }
    }

    private void writeHeader(int fileLength, int elementCount, int firstPosition, int lastPosition) throws IOException {
        writeInts(this.buffer, fileLength, elementCount, firstPosition, lastPosition);
        this.raf.seek(0L);
        this.raf.write(this.buffer);
    }

    private QueueFile.Element readElement(int position) throws IOException {
        if (position == 0) {
            return QueueFile.Element.NULL;
        } else {
            this.raf.seek((long)position);
            return new QueueFile.Element(position, this.raf.readInt());
        }
    }

    private static void initialize(File file) throws IOException {
        File tempFile = new File(file.getPath() + ".tmp");
        RandomAccessFile raf = open(tempFile);

        try {
            raf.setLength(4096L);
            raf.seek(0L);
            byte[] headerBuffer = new byte[16];
            writeInts(headerBuffer, 4096, 0, 0, 0);
            raf.write(headerBuffer);
        } finally {
            raf.close();
        }

        if (!tempFile.renameTo(file)) {
            throw new IOException("Rename failed!");
        }
    }

    private static RandomAccessFile open(File file) throws FileNotFoundException {
        return new RandomAccessFile(file, "rwd");
    }

    private int wrapPosition(int position) {
        return position < this.fileLength ? position : 16 + position - this.fileLength;
    }

    private void ringWrite(int position, byte[] buffer, int offset, int count) throws IOException {
        position = this.wrapPosition(position);
        if (position + count <= this.fileLength) {
            this.raf.seek((long)position);
            this.raf.write(buffer, offset, count);
        } else {
            int beforeEof = this.fileLength - position;
            this.raf.seek((long)position);
            this.raf.write(buffer, offset, beforeEof);
            this.raf.seek(16L);
            this.raf.write(buffer, offset + beforeEof, count - beforeEof);
        }

    }

    private void ringRead(int position, byte[] buffer, int offset, int count) throws IOException {
        position = this.wrapPosition(position);
        if (position + count <= this.fileLength) {
            this.raf.seek((long)position);
            this.raf.readFully(buffer, offset, count);
        } else {
            int beforeEof = this.fileLength - position;
            this.raf.seek((long)position);
            this.raf.readFully(buffer, offset, beforeEof);
            this.raf.seek(16L);
            this.raf.readFully(buffer, offset + beforeEof, count - beforeEof);
        }

    }

    public void add(byte[] data) throws IOException {
        this.add(data, 0, data.length);
    }

    public synchronized void add(byte[] data, int offset, int count) throws IOException {
        nonNull(data, "buffer");
        if ((offset | count) >= 0 && count <= data.length - offset) {
            this.expandIfNecessary(count);
            boolean wasEmpty = this.isEmpty();
            int position = wasEmpty ? 16 : this.wrapPosition(this.last.position + 4 + this.last.length);
            QueueFile.Element newLast = new QueueFile.Element(position, count);
            writeInt(this.buffer, 0, count);
            this.ringWrite(newLast.position, this.buffer, 0, 4);
            this.ringWrite(newLast.position + 4, data, offset, count);
            int firstPosition = wasEmpty ? newLast.position : this.first.position;
            this.writeHeader(this.fileLength, this.elementCount + 1, firstPosition, newLast.position);
            this.last = newLast;
            ++this.elementCount;
            if (wasEmpty) {
                this.first = this.last;
            }

        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public int usedBytes() {
        if (this.elementCount == 0) {
            return 16;
        } else {
            return this.last.position >= this.first.position ? this.last.position - this.first.position + 4 + this.last.length + 16 : this.last.position + 4 + this.last.length + this.fileLength - this.first.position;
        }
    }

    private int remainingBytes() {
        return this.fileLength - this.usedBytes();
    }

    public synchronized boolean isEmpty() {
        return this.elementCount == 0;
    }

    private void expandIfNecessary(int dataLength) throws IOException {
        int elementLength = 4 + dataLength;
        int remainingBytes = this.remainingBytes();
        if (remainingBytes < elementLength) {
            int previousLength = this.fileLength;

            int newLength;
            do {
                remainingBytes += previousLength;
                newLength = previousLength << 1;
                previousLength = newLength;
            } while(remainingBytes < elementLength);

            this.setLength(newLength);
            int endOfLastElement = this.wrapPosition(this.last.position + 4 + this.last.length);
            if (endOfLastElement < this.first.position) {
                FileChannel channel = this.raf.getChannel();
                channel.position((long)this.fileLength);
                int count = endOfLastElement - 4;
                if (channel.transferTo(16L, (long)count, channel) != (long)count) {
                    throw new AssertionError("Copied insufficient number of bytes!");
                }
            }

            if (this.last.position < this.first.position) {
                int newLastPosition = this.fileLength + this.last.position - 16;
                this.writeHeader(newLength, this.elementCount, this.first.position, newLastPosition);
                this.last = new QueueFile.Element(newLastPosition, this.last.length);
            } else {
                this.writeHeader(newLength, this.elementCount, this.first.position, this.last.position);
            }

            this.fileLength = newLength;
        }
    }

    private void setLength(int newLength) throws IOException {
        this.raf.setLength((long)newLength);
        this.raf.getChannel().force(true);
    }

    public synchronized byte[] peek() throws IOException {
        if (this.isEmpty()) {
            return null;
        } else {
            int length = this.first.length;
            byte[] data = new byte[length];
            this.ringRead(this.first.position + 4, data, 0, length);
            return data;
        }
    }

    public synchronized void peek(QueueFile.ElementReader reader) throws IOException {
        if (this.elementCount > 0) {
            reader.read(new QueueFile.ElementInputStream(this.first), this.first.length);
        }

    }

    public synchronized void forEach(QueueFile.ElementReader reader) throws IOException {
        int position = this.first.position;

        for(int i = 0; i < this.elementCount; ++i) {
            QueueFile.Element current = this.readElement(position);
            reader.read(new QueueFile.ElementInputStream(current), current.length);
            position = this.wrapPosition(current.position + 4 + current.length);
        }

    }

    private static <T> T nonNull(T t, String name) {
        if (t == null) {
            throw new NullPointerException(name);
        } else {
            return t;
        }
    }

    public synchronized int size() {
        return this.elementCount;
    }

    public synchronized void remove() throws IOException {
        if (this.isEmpty()) {
            throw new NoSuchElementException();
        } else {
            if (this.elementCount == 1) {
                this.clear();
            } else {
                int newFirstPosition = this.wrapPosition(this.first.position + 4 + this.first.length);
                this.ringRead(newFirstPosition, this.buffer, 0, 4);
                int length = readInt(this.buffer, 0);
                this.writeHeader(this.fileLength, this.elementCount - 1, newFirstPosition, this.last.position);
                --this.elementCount;
                this.first = new QueueFile.Element(newFirstPosition, length);
            }

        }
    }

    public synchronized void clear() throws IOException {
        this.writeHeader(4096, 0, 0, 0);
        this.elementCount = 0;
        this.first = QueueFile.Element.NULL;
        this.last = QueueFile.Element.NULL;
        if (this.fileLength > 4096) {
            this.setLength(4096);
        }

        this.fileLength = 4096;
    }

    public synchronized void close() throws IOException {
        this.raf.close();
    }


    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName()).append('[');
        builder.append("fileLength=").append(this.fileLength);
        builder.append(", size=").append(this.elementCount);
        builder.append(", first=").append(this.first);
        builder.append(", last=").append(this.last);
        builder.append(", element lengths=[");

        try {
            this.forEach(new QueueFile.ElementReader() {
                boolean first = true;

                public void read(InputStream in, int length) throws IOException {
                    if (this.first) {
                        this.first = false;
                    } else {
                        builder.append(", ");
                    }

                    builder.append(length);
                }
            });
        } catch (IOException var3) {
        }

        builder.append("]]");
        return builder.toString();
    }

    public interface ElementReader {
        void read(InputStream var1, int var2) throws IOException;
    }

    static class Element {
        static final QueueFile.Element NULL = new QueueFile.Element(0, 0);
        final int position;
        final int length;

        Element(int position, int length) {
            this.position = position;
            this.length = length;
        }

        public String toString() {
            return this.getClass().getSimpleName() + "[position = " + this.position + ", length = " + this.length + "]";
        }
    }

    private final class ElementInputStream extends InputStream {
        private int position;
        private int remaining;

        private ElementInputStream(QueueFile.Element element) {
            this.position = QueueFile.this.wrapPosition(element.position + 4);
            this.remaining = element.length;
        }

        public int read(byte[] buffer, int offset, int length) throws IOException {
            QueueFile.nonNull(buffer, "buffer");
            if ((offset | length) >= 0 && length <= buffer.length - offset) {
                if (this.remaining > 0) {
                    if (length > this.remaining) {
                        length = this.remaining;
                    }

                    QueueFile.this.ringRead(this.position, buffer, offset, length);
                    this.position = QueueFile.this.wrapPosition(this.position + length);
                    this.remaining -= length;
                    return length;
                } else {
                    return -1;
                }
            } else {
                throw new ArrayIndexOutOfBoundsException();
            }
        }

        public int read() throws IOException {
            if (this.remaining == 0) {
                return -1;
            } else {
                QueueFile.this.raf.seek((long)this.position);
                int b = QueueFile.this.raf.read();
                this.position = QueueFile.this.wrapPosition(this.position + 1);
                --this.remaining;
                return b;
            }
        }
    }
}
