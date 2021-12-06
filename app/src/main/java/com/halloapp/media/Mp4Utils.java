package com.halloapp.media;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.FileStore;
import com.halloapp.util.FileUtils;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Mp4Utils {
    private static final long MAX_UNSIGNED_INT = unsignedIntToLong(-1);
    private static final int INT_BYTES = 4;
    private static final int LONG_BYTES = 8;

    private static final int MP4_ATOM_BUFFER_SIZE_LIMIT = 1 << 26;
    private static final int MP4_STCO_LEVEL_DEPTH = 5;
    private static final int MP4_META_UDTA_LEVEL_DEPTH = 3;

    private static final int MP4_ATOM_SIZE_LENGTH = INT_BYTES;
    private static final int MP4_ATOM_SIZE_TYPE = INT_BYTES;
    private static final int MP4_ATOM_SIZE_PREAMBLE = MP4_ATOM_SIZE_LENGTH + MP4_ATOM_SIZE_TYPE;
    private static final int MP4_ATOM_SIZE_EXTENDED_LENGTH = LONG_BYTES;
    private static final int MP4_ATOM_SIZE_VERSION_AND_FLAGS = INT_BYTES;
    private static final int MP4_ATOM_SIZE_CREATION_DATE = INT_BYTES;
    private static final int MP4_ATOM_SIZE_MODIFICATION_DATE = INT_BYTES;
    private static final int MP4_ATOM_SIZE_CHUNK_NUMBER_OF_ENTRIES = INT_BYTES;
    private static final int MP4_ATOM_SIZE_KEYS_ENTRY_COUNT = INT_BYTES;

    private static final int MP4_ATOM_FLAG_SIZE_TO_END = 0;
    private static final int MP4_ATOM_FLAG_SIZE_EXTENDED = 1;

    private static final int MP4_ATOM_FLAG_LIST_ZERO_TERMINATION = 0;

    private static final int MP4_ATOM_TYPE_FTYP = getMp4AtomTypeInt("ftyp");
    private static final int MP4_ATOM_TYPE_MOOV = getMp4AtomTypeInt("moov");
    private static final int MP4_ATOM_TYPE_MVHD = getMp4AtomTypeInt("mvhd");
    private static final int MP4_ATOM_TYPE_META = getMp4AtomTypeInt("meta");
    private static final int MP4_ATOM_TYPE_KEYS = getMp4AtomTypeInt("keys");
    private static final int MP4_ATOM_TYPE_UDTA = getMp4AtomTypeInt("udta");
    private static final int MP4_ATOM_TYPE_TRAK = getMp4AtomTypeInt("trak");
    private static final int MP4_ATOM_TYPE_TKHD = getMp4AtomTypeInt("tkhd");
    private static final int MP4_ATOM_TYPE_MDIA = getMp4AtomTypeInt("mdia");
    private static final int MP4_ATOM_TYPE_MDHD = getMp4AtomTypeInt("mdhd");
    private static final int MP4_ATOM_TYPE_MINF = getMp4AtomTypeInt("minf");
    private static final int MP4_ATOM_TYPE_STBL = getMp4AtomTypeInt("stbl");
    private static final int MP4_ATOM_TYPE_STCO = getMp4AtomTypeInt("stco");
    private static final int MP4_ATOM_TYPE_CO64 = getMp4AtomTypeInt("co64");
    private static final int MP4_ATOM_TYPE_FREE = getMp4AtomTypeInt("free");

    // Useful documentation about the user data list entry types:
    // https://developer.apple.com/library/archive/documentation/QuickTime/QTFF/QTFFChap2/qtff2.html#//apple_ref/doc/uid/TP40000939-CH204-TPXREF101
    // More about metadata in mp4 and the "©xyz" geolocation user data list entry type:
    // https://leo-van-stee.github.io/#_Toc48653963
    private static final int MP4_ATOM_TYPE_USER_XYZ = getMp4AtomTypeInt("©xyz");

    private static final Set<Integer> STCO_ATOM_CONTAINER_SET = new HashSet<>();
    private static final Set<Integer> META_ATOM_CONTAINER_SET = new HashSet<>();

    static {
        META_ATOM_CONTAINER_SET.add(MP4_ATOM_TYPE_MOOV);
        META_ATOM_CONTAINER_SET.add(MP4_ATOM_TYPE_TRAK);
        META_ATOM_CONTAINER_SET.add(MP4_ATOM_TYPE_MDIA);

        STCO_ATOM_CONTAINER_SET.add(MP4_ATOM_TYPE_MOOV);
        STCO_ATOM_CONTAINER_SET.add(MP4_ATOM_TYPE_TRAK);
        STCO_ATOM_CONTAINER_SET.add(MP4_ATOM_TYPE_MDIA);
        STCO_ATOM_CONTAINER_SET.add(MP4_ATOM_TYPE_MINF);
        STCO_ATOM_CONTAINER_SET.add(MP4_ATOM_TYPE_STBL);
    }

    public static class Mp4OperationException extends Exception {
        public Mp4OperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final class Mp4Atom {
        int type;
        long start;
        long size;
        int headerSize;

        public Mp4Atom(int type, long start, long size, int headerSize) {
            this.type = type;
            this.start = start;
            this.size = size;
            this.headerSize = headerSize;
        }

        @Override
        public String toString() {
            final ByteBuffer buffer = ByteBuffer.allocate(INT_BYTES).putInt(type);
            return "Mp4Atom {type:" + new String(buffer.array(), StandardCharsets.ISO_8859_1) + " start:" + start + " size:" + size + " headerSize: " + headerSize + "}";
        }
    }

    private static final class StcoCounter {
        int numberOfChunks = 0;
        boolean overflowDetected = false;
    }

    private static class Mp4FormatException extends Exception {
        public Mp4FormatException(String message) {
            super(message);
        }
    }

    private static class Mp4UnsupportedFormatException extends Mp4FormatException {
        private Mp4Atom mp4Atom;

        public Mp4UnsupportedFormatException(String message) {
            super(message);
        }

        public Mp4UnsupportedFormatException(Mp4Atom mp4Atom, String message) {
            super(mp4Atom + " " + message);
            this.mp4Atom = mp4Atom;
        }

        public Mp4Atom getMp4Atom() {
            return mp4Atom;
        }
    }

    private static class Mp4AtomFormatException extends Mp4FormatException {
        private final Mp4Atom mp4Atom;

        public Mp4AtomFormatException(Mp4Atom mp4Atom, String message) {
            super(mp4Atom + " " + message);
            this.mp4Atom = mp4Atom;
        }

        public Mp4Atom getMp4Atom() {
            return mp4Atom;
        }
    }

    private static long unsignedIntToLong(int unsignedInt) {
        return unsignedInt & 0xffffffffL;
    }

    private static int getMp4AtomTypeInt(@NonNull String atomTypeString) {
        return ByteBuffer.wrap(atomTypeString.getBytes(StandardCharsets.ISO_8859_1)).getInt();
    }

    private static List<Mp4Atom> filterAtomsByType(@NonNull List<Mp4Atom> atomList, int atomType) {
        ArrayList<Mp4Atom> filteredList = new ArrayList<>();
        for (Mp4Atom atom : atomList) {
            if (atom.type == atomType) {
                filteredList.add(atom);
            }
        }
        return filteredList;
    }

    private static ByteBuffer allocateAtomByteBuffer(long size) throws Mp4UnsupportedFormatException {
        if (size > MP4_ATOM_BUFFER_SIZE_LIMIT) {
            throw new Mp4UnsupportedFormatException("Will not allocate buffer of length " + size + " larger than the set limit " + MP4_ATOM_BUFFER_SIZE_LIMIT);
        }
        return ByteBuffer.allocate((int) size);
    }

    @WorkerThread
    private static List<Mp4Atom> getChildAtoms(@NonNull FileChannel fileChannel, @NonNull Mp4Atom parentAtom) throws IOException, Mp4FormatException {
        return getLevelAtoms(fileChannel, parentAtom.start + parentAtom.headerSize, parentAtom.size - parentAtom.headerSize);
    }

    @WorkerThread
    private static List<Mp4Atom> getUserDataList(@NonNull FileChannel fileChannel, @NonNull Mp4Atom udtaAtom) throws IOException, Mp4FormatException {
        if (udtaAtom.type != MP4_ATOM_TYPE_UDTA) {
            throw new Mp4AtomFormatException(udtaAtom, "Expected udta atom type");
        }
        return getLevelAtoms(fileChannel, udtaAtom.start + udtaAtom.headerSize, udtaAtom.size - udtaAtom.headerSize, true);
    }

    @WorkerThread
    private static List<Mp4Atom> getLevelAtoms(@NonNull FileChannel fileChannel, long start, long size) throws IOException, Mp4FormatException {
        return getLevelAtoms(fileChannel, start, size, false);
    }

    @WorkerThread
    private static List<Mp4Atom> getLevelAtoms(@NonNull FileChannel fileChannel, long start, long size, boolean allowZeroTermination) throws IOException, Mp4FormatException {
        // Useful documentation about the mp4 atom layout:
        // https://developer.apple.com/library/archive/documentation/QuickTime/QTFF/QTFFChap1/qtff1.html#//apple_ref/doc/uid/TP40000939-CH203-38190
        final List<Mp4Atom> atomBoxList = new ArrayList<>();
        final ByteBuffer preambleBuffer = ByteBuffer.allocate(MP4_ATOM_SIZE_PREAMBLE);
        final ByteBuffer extendedSizeBuffer = ByteBuffer.allocate(MP4_ATOM_SIZE_EXTENDED_LENGTH);

        int atomType, headerSize, bytesRead;
        long atomStart, atomSize;

        fileChannel.position(start);
        while (fileChannel.position() < start + size) {
            atomStart = fileChannel.position();
            bytesRead = fileChannel.read(preambleBuffer);
            if (allowZeroTermination && atomStart + MP4_ATOM_SIZE_LENGTH == start + size && bytesRead >= MP4_ATOM_SIZE_LENGTH && preambleBuffer.getInt(0) == MP4_ATOM_FLAG_LIST_ZERO_TERMINATION) {
                break;
            }
            if (atomStart + MP4_ATOM_SIZE_PREAMBLE > start + size) {
                throw new Mp4FormatException("Atom stretches past the encompassing limits");
            }
            if (bytesRead != preambleBuffer.capacity()) {
                throw new Mp4FormatException("End of file reached before atom preamble could be fully read");
            }

            preambleBuffer.flip();
            atomSize = unsignedIntToLong(preambleBuffer.getInt());
            atomType = preambleBuffer.getInt();
            headerSize = MP4_ATOM_SIZE_PREAMBLE;
            preambleBuffer.clear();

            if (atomSize == MP4_ATOM_FLAG_SIZE_TO_END) {
                atomSize = fileChannel.size() - atomStart;
            } else if (atomSize == MP4_ATOM_FLAG_SIZE_EXTENDED) {
                if (fileChannel.read(extendedSizeBuffer) != extendedSizeBuffer.capacity()) {
                    throw new Mp4FormatException("End of file reached before atom extended size could be fully read");
                }
                extendedSizeBuffer.flip();
                atomSize = extendedSizeBuffer.getLong();
                extendedSizeBuffer.clear();
                headerSize = MP4_ATOM_SIZE_PREAMBLE + MP4_ATOM_SIZE_EXTENDED_LENGTH;
            }

            final Mp4Atom atom = new Mp4Atom(atomType, atomStart, atomSize, headerSize);
            atomBoxList.add(atom);
            if (atomStart + atomSize >= fileChannel.position()) {
                fileChannel.position(atomStart + atomSize);
            } else {
                throw new Mp4AtomFormatException(atom, "Atom size is less than " + atom.headerSize + " bytes");
            }
        }

        return atomBoxList;
    }

    @WorkerThread
    private static ByteBuffer createAtomHeaderBuffer(@NonNull Mp4Atom atom) throws Mp4AtomFormatException, Mp4UnsupportedFormatException {
        final ByteBuffer headerBuffer = allocateAtomByteBuffer(atom.headerSize);
        if (atom.headerSize == MP4_ATOM_SIZE_PREAMBLE) {
            if (atom.size > MAX_UNSIGNED_INT) {
                throw new Mp4UnsupportedFormatException(atom, "Atom size is too big to fit in unsigned int");
            }
            headerBuffer.putInt((int) atom.size).putInt(atom.type);
        } else if (atom.headerSize == MP4_ATOM_SIZE_PREAMBLE + MP4_ATOM_SIZE_EXTENDED_LENGTH) {
            headerBuffer.putInt(MP4_ATOM_FLAG_SIZE_EXTENDED).putInt(atom.type).putLong(atom.size);
        } else {
            throw new Mp4AtomFormatException(atom, "Unexpected atom header size");
        }
        headerBuffer.flip();
        return headerBuffer;
    }

    @WorkerThread
    private static void overwriteAtomSize(FileChannel outFileChannel, Mp4Atom mp4Atom, long newStart, long newSize) throws IOException, Mp4AtomFormatException, Mp4UnsupportedFormatException {
        if (newSize < mp4Atom.headerSize) {
            throw new Mp4AtomFormatException(mp4Atom, "Attempt to update atom size with too small value " + newSize);
        }
        if (mp4Atom.headerSize == MP4_ATOM_SIZE_PREAMBLE) {
            if (newSize > MAX_UNSIGNED_INT) {
                throw new Mp4UnsupportedFormatException(mp4Atom, "Overflow when changing atom size");
            }
            final ByteBuffer buffer = ByteBuffer.allocate(INT_BYTES);
            buffer.putInt((int) newSize);
            buffer.flip();
            outFileChannel.write(buffer, newStart);
        } else if (mp4Atom.headerSize == MP4_ATOM_SIZE_PREAMBLE + MP4_ATOM_SIZE_EXTENDED_LENGTH) {
            final ByteBuffer buffer = ByteBuffer.allocate(MP4_ATOM_SIZE_EXTENDED_LENGTH);
            buffer.putLong(newSize);
            buffer.flip();
            outFileChannel.write(buffer, newStart + MP4_ATOM_SIZE_PREAMBLE);
        } else {
            throw new Mp4AtomFormatException(mp4Atom, "Unexpected atom header size");
        }
    }

    @WorkerThread
    private static void zeroHeaderAtomTimestamp(@NonNull FileChannel fileChannel, @NonNull Mp4Atom atom) throws IOException, Mp4AtomFormatException {
        if (atom.size < atom.headerSize + MP4_ATOM_SIZE_VERSION_AND_FLAGS + MP4_ATOM_SIZE_CREATION_DATE + MP4_ATOM_SIZE_MODIFICATION_DATE) {
            throw new Mp4AtomFormatException(atom, "Atom is too short to attempt to zero timestamps");
        }
        final ByteBuffer zeroBuffer = ByteBuffer.wrap(new byte[MP4_ATOM_SIZE_CREATION_DATE + MP4_ATOM_SIZE_MODIFICATION_DATE]);
        fileChannel.write(zeroBuffer, atom.start + atom.headerSize + MP4_ATOM_SIZE_VERSION_AND_FLAGS);
    }

    @WorkerThread
    private static void clearAtomTimestamps(@NonNull FileChannel fileChannel) throws IOException, Mp4FormatException {
        // Useful documentation about the mp4 hierarchy:
        // https://openmp4file.com/format.html
        // https://developer.apple.com/library/archive/documentation/QuickTime/QTFF/QTFFChap2/qtff2.html#//apple_ref/doc/uid/TP40000939-CH204-55265

        final List<Mp4Atom> moovAtomList = filterAtomsByType(getLevelAtoms(fileChannel, 0, fileChannel.size()), MP4_ATOM_TYPE_MOOV);
        if (moovAtomList.size() > 0) {
            final List<Mp4Atom> moovChildAtomList = getChildAtoms(fileChannel, moovAtomList.get(0));

            final List<Mp4Atom> mvhdAtomList = filterAtomsByType(moovChildAtomList, MP4_ATOM_TYPE_MVHD);
            if (mvhdAtomList.size() > 0) {
                zeroHeaderAtomTimestamp(fileChannel, mvhdAtomList.get(0));
            }

            final List<Mp4Atom> trakAtomList = filterAtomsByType(moovChildAtomList, MP4_ATOM_TYPE_TRAK);
            for (Mp4Atom trakAtom : trakAtomList) {
                final List<Mp4Atom> trakChildAtomList = getChildAtoms(fileChannel, trakAtom);

                final List<Mp4Atom> tkhdAtomList = filterAtomsByType(trakChildAtomList, MP4_ATOM_TYPE_TKHD);
                if (tkhdAtomList.size() > 0) {
                    zeroHeaderAtomTimestamp(fileChannel, tkhdAtomList.get(0));
                }

                final List<Mp4Atom> mdiaAtomList = filterAtomsByType(trakChildAtomList, MP4_ATOM_TYPE_MDIA);
                if (mdiaAtomList.size() > 0) {
                    final List<Mp4Atom> mdhdAtomList = filterAtomsByType(getChildAtoms(fileChannel, mdiaAtomList.get(0)), MP4_ATOM_TYPE_MDHD);
                    if (mdhdAtomList.size() > 0) {
                        zeroHeaderAtomTimestamp(fileChannel, mdhdAtomList.get(0));
                    }
                }
            }
        }
    }

    @WorkerThread
    private static void updateAndCopyStcoAtom(FileChannel inFileChannel, FileChannel outFileChannel, Mp4Atom atom, long offsetDiff, StcoCounter stcoCounter) throws Mp4FormatException, IOException {
        if (atom.type != MP4_ATOM_TYPE_STCO) {
            throw new Mp4AtomFormatException(atom, "Expected stco atom type");
        }
        if (atom.size < atom.headerSize + MP4_ATOM_SIZE_VERSION_AND_FLAGS + MP4_ATOM_SIZE_CHUNK_NUMBER_OF_ENTRIES) {
            throw new Mp4AtomFormatException(atom, "Atom is too short to attempt to read number of entries");
        }

        final ByteBuffer atomBuffer = allocateAtomByteBuffer(atom.size);
        inFileChannel.read(atomBuffer, atom.start);
        atomBuffer.flip();

        atomBuffer.position(atom.headerSize + MP4_ATOM_SIZE_VERSION_AND_FLAGS);
        int numberOfEntries = atomBuffer.getInt();
        final long expectedSize = atom.headerSize + MP4_ATOM_SIZE_VERSION_AND_FLAGS + MP4_ATOM_SIZE_CHUNK_NUMBER_OF_ENTRIES + numberOfEntries * INT_BYTES;
        if (atom.size != expectedSize) {
            throw new Mp4AtomFormatException(atom, "Size does not match expected content size for number of chunks " + numberOfEntries);
        }
        stcoCounter.numberOfChunks += numberOfEntries;

        for (int i = 0; i < numberOfEntries; ++i) {
            long chunkOffset = unsignedIntToLong(atomBuffer.getInt(atomBuffer.position()));
            if (chunkOffset + offsetDiff > MAX_UNSIGNED_INT) {
                stcoCounter.overflowDetected = true;
            }
            atomBuffer.putInt((int) (chunkOffset + offsetDiff));
        }

        atomBuffer.flip();
        outFileChannel.write(atomBuffer);
    }

    @WorkerThread
    private static void updateAndCopyCo64Atom(FileChannel inFileChannel, FileChannel outFileChannel, Mp4Atom atom, long offsetDiff) throws Mp4FormatException, IOException {
        if (atom.type != MP4_ATOM_TYPE_CO64) {
            throw new Mp4AtomFormatException(atom, "Expected co64 atom type");
        }
        if (atom.size < atom.headerSize + MP4_ATOM_SIZE_VERSION_AND_FLAGS + MP4_ATOM_SIZE_CHUNK_NUMBER_OF_ENTRIES) {
            throw new Mp4AtomFormatException(atom, "Atom is too short to attempt to read number of entries");
        }

        final ByteBuffer atomBuffer = allocateAtomByteBuffer(atom.size);
        inFileChannel.read(atomBuffer, atom.start);
        atomBuffer.flip();

        atomBuffer.position(atom.headerSize + MP4_ATOM_SIZE_VERSION_AND_FLAGS);
        final int numberOfEntries = atomBuffer.getInt();
        final long expectedSize = atom.headerSize + MP4_ATOM_SIZE_VERSION_AND_FLAGS + MP4_ATOM_SIZE_CHUNK_NUMBER_OF_ENTRIES + numberOfEntries * LONG_BYTES;
        if (atom.size != expectedSize) {
            throw new Mp4AtomFormatException(atom, "Size does not match expected content size for number of chunks " + numberOfEntries);
        }

        for (int i = 0; i < numberOfEntries; ++i) {
            long chunkOffset = atomBuffer.getLong(atomBuffer.position());
            atomBuffer.putLong(chunkOffset + offsetDiff);
        }

        atomBuffer.flip();
        outFileChannel.write(atomBuffer);
    }

    @WorkerThread
    private static Mp4Atom upgradeAndCopyStcoAtom(FileChannel inFileChannel, FileChannel outFileChannel, Mp4Atom atom, long offsetDiff) throws Mp4FormatException, IOException {
        if (atom.type != MP4_ATOM_TYPE_STCO) {
            throw new Mp4AtomFormatException(atom, "Expected stco atom type");
        }
        if (atom.size < atom.headerSize + MP4_ATOM_SIZE_VERSION_AND_FLAGS + MP4_ATOM_SIZE_CHUNK_NUMBER_OF_ENTRIES) {
            throw new Mp4AtomFormatException(atom, "Atom is too short to attempt to read number of entries");
        }

        final ByteBuffer inBuffer = allocateAtomByteBuffer(atom.size);
        inFileChannel.read(inBuffer, atom.start);
        inBuffer.flip();

        inBuffer.position(atom.headerSize);
        final int versionAndFlags = inBuffer.getInt();
        final int numberOfEntries = inBuffer.getInt();
        final long expectedSize = atom.headerSize + MP4_ATOM_SIZE_VERSION_AND_FLAGS + MP4_ATOM_SIZE_CHUNK_NUMBER_OF_ENTRIES + numberOfEntries * INT_BYTES;
        if (atom.size != expectedSize) {
            throw new Mp4AtomFormatException(atom, "Size does not match expected content size for number of chunks " + numberOfEntries);
        }

        final long newSize = atom.headerSize + MP4_ATOM_SIZE_VERSION_AND_FLAGS + MP4_ATOM_SIZE_CHUNK_NUMBER_OF_ENTRIES + numberOfEntries * LONG_BYTES;
        final Mp4Atom co64Atom = new Mp4Atom(MP4_ATOM_TYPE_CO64, atom.start, newSize, atom.headerSize);
        final ByteBuffer outBuffer = allocateAtomByteBuffer(co64Atom.size);

        if (co64Atom.headerSize == MP4_ATOM_SIZE_PREAMBLE) {
            if (newSize > MAX_UNSIGNED_INT) {
                throw new Mp4UnsupportedFormatException(co64Atom, "Overflow atom size when upgrading from stco to co64");
            }
            outBuffer.putInt((int) co64Atom.size).putInt(co64Atom.type);
        } else if (co64Atom.headerSize == MP4_ATOM_SIZE_PREAMBLE + MP4_ATOM_SIZE_EXTENDED_LENGTH) {
            outBuffer.putInt(MP4_ATOM_FLAG_SIZE_EXTENDED).putInt(co64Atom.type).putLong(co64Atom.size);
        } else {
            throw new Mp4AtomFormatException(co64Atom, "Unexpected atom header size");
        }
        outBuffer.putInt(versionAndFlags).putInt(numberOfEntries);

        for (int i = 0; i < numberOfEntries; ++i) {
            long chunkOffset = unsignedIntToLong(inBuffer.getInt());
            outBuffer.putLong(chunkOffset + offsetDiff);
        }

        outBuffer.flip();
        outFileChannel.write(outBuffer);

        return co64Atom;
    }

    @WorkerThread
    private static void clearUdtaLocation(@NonNull FileChannel fileChannel, @NonNull Mp4Atom udtaAtom) throws Mp4FormatException, IOException {
        if (udtaAtom.type != MP4_ATOM_TYPE_UDTA) {
            throw new Mp4AtomFormatException(udtaAtom, "Expected udta atom type");
        }
        final List<Mp4Atom> xyzDataList = filterAtomsByType(getUserDataList(fileChannel, udtaAtom), MP4_ATOM_TYPE_USER_XYZ);
        for (Mp4Atom xyzData : xyzDataList) {
            final Mp4Atom freeData = new Mp4Atom(MP4_ATOM_TYPE_FREE, xyzData.start, xyzData.size, xyzData.headerSize);
            final ByteBuffer headerBuffer = createAtomHeaderBuffer(freeData);
            final ByteBuffer contentBuffer = allocateAtomByteBuffer(freeData.size - freeData.headerSize);
            fileChannel.write(headerBuffer, freeData.start);
            fileChannel.write(contentBuffer, freeData.start + freeData.headerSize);
        }
    }

    @WorkerThread
    private static void traverseUpdateStcoAtoms(FileChannel inFileChannel, FileChannel outFileChannel, Mp4Atom atom, long offsetDiff, StcoCounter stcoCounter, int levelDepth) throws IOException, Mp4FormatException {
        if (levelDepth > MP4_STCO_LEVEL_DEPTH) {
            throw new Mp4FormatException("While traversing the atom tree for stco update, reached too deep level " + levelDepth);
        }
        if (atom.type == MP4_ATOM_TYPE_STCO) {
            updateAndCopyStcoAtom(inFileChannel, outFileChannel, atom, offsetDiff, stcoCounter);
        } else if (atom.type == MP4_ATOM_TYPE_CO64) {
            updateAndCopyCo64Atom(inFileChannel, outFileChannel, atom, offsetDiff);
        } else if (STCO_ATOM_CONTAINER_SET.contains(atom.type)) {
            List<Mp4Atom> childAtomList = getChildAtoms(inFileChannel, atom);
            inFileChannel.transferTo(atom.start, atom.headerSize, outFileChannel);
            for (Mp4Atom childAtom : childAtomList) {
                traverseUpdateStcoAtoms(inFileChannel, outFileChannel, childAtom, offsetDiff, stcoCounter, levelDepth + 1);
            }
        } else {
            inFileChannel.transferTo(atom.start, atom.size, outFileChannel);
        }
    }

    @WorkerThread
    private static long traverseUpgradeStcoAtoms(FileChannel inFileChannel, FileChannel outFileChannel, Mp4Atom atom, long offsetDiff, int levelDepth) throws IOException, Mp4FormatException {
        if (levelDepth > MP4_STCO_LEVEL_DEPTH) {
            throw new Mp4FormatException("While traversing the atom tree for stco upgrade, reached too deep level " + levelDepth);
        }
        if (atom.type == MP4_ATOM_TYPE_STCO) {
            final Mp4Atom co64Atom = upgradeAndCopyStcoAtom(inFileChannel, outFileChannel, atom, offsetDiff);
            return co64Atom.size - atom.size;
        } else if (atom.type == MP4_ATOM_TYPE_CO64) {
            updateAndCopyCo64Atom(inFileChannel, outFileChannel, atom, offsetDiff);
            return 0;
        } else if (STCO_ATOM_CONTAINER_SET.contains(atom.type)) {
            List<Mp4Atom> childAtomList = getChildAtoms(inFileChannel, atom);
            final long newAtomStart = outFileChannel.position();
            long sizeDiff = 0;
            inFileChannel.transferTo(atom.start, atom.headerSize, outFileChannel);
            for (Mp4Atom childAtom : childAtomList) {
                sizeDiff += traverseUpgradeStcoAtoms(inFileChannel, outFileChannel, childAtom, offsetDiff, levelDepth + 1);
            }
            if (sizeDiff > 0) {
                overwriteAtomSize(outFileChannel, atom, newAtomStart, atom.size + sizeDiff);
            }
            return sizeDiff;
        } else {
            inFileChannel.transferTo(atom.start, atom.size, outFileChannel);
            return 0;
        }
    }

    @WorkerThread
    private static void traverseClearLocation(@NonNull FileChannel fileChannel, @NonNull Mp4Atom atom, int levelDepth) throws IOException, Mp4FormatException {
        if (levelDepth > MP4_META_UDTA_LEVEL_DEPTH) {
            throw new Mp4FormatException("While traversing the atom tree for udta update, reached too deep level " + levelDepth);
        }
        if (atom.type == MP4_ATOM_TYPE_UDTA) {
            clearUdtaLocation(fileChannel, atom);
        } else if (META_ATOM_CONTAINER_SET.contains(atom.type)) {
            final List<Mp4Atom> childAtomList = getChildAtoms(fileChannel, atom);
            for (Mp4Atom childAtom : childAtomList) {
                traverseClearLocation(fileChannel, childAtom, levelDepth + 1);
            }
        }
    }

    @WorkerThread
    private static boolean putMoovAtomAtStart(FileChannel inFileChannel, FileChannel outFileChannel) throws IOException, Mp4FormatException {
        List<Mp4Atom> mp4AtomList = getLevelAtoms(inFileChannel, 0, inFileChannel.size());
        if (mp4AtomList.size() > 0) {
            long ftypSize = 0;
            final Mp4Atom firstAtom = mp4AtomList.get(0);
            if (firstAtom.type == MP4_ATOM_TYPE_FTYP) {
                ftypSize = firstAtom.size;
            }

            final Mp4Atom lastAtom = mp4AtomList.get(mp4AtomList.size() - 1);
            if (lastAtom.type == MP4_ATOM_TYPE_MOOV) {
                Log.i("Mp4Utils.putMoovAtomAtStart: Last atom in mp4 was moov, will put it at the start.");
                if (ftypSize > 0) {
                    inFileChannel.transferTo(0, ftypSize, outFileChannel);
                }

                final StcoCounter stcoCounter = new StcoCounter();
                traverseUpdateStcoAtoms(inFileChannel, outFileChannel, lastAtom, lastAtom.size, stcoCounter, 0);

                if (stcoCounter.overflowDetected) {
                    final long newMoovSize = lastAtom.size + stcoCounter.numberOfChunks * INT_BYTES;
                    outFileChannel.position(ftypSize);
                    traverseUpgradeStcoAtoms(inFileChannel, outFileChannel, lastAtom, newMoovSize, 0);
                }

                inFileChannel.transferTo(ftypSize, lastAtom.start - ftypSize, outFileChannel);
                return true;
            } else {
                final int moovIndex = ftypSize > 0 ? 1 : 0;
                if (moovIndex >= mp4AtomList.size() || mp4AtomList.get(moovIndex).type != MP4_ATOM_TYPE_MOOV) {
                    throw new Mp4FormatException("Could not find moov atom at the beginning or at the end of the file");
                }
                Log.i("Mp4Utils.putMoovAtomAtStart: Last atom in mp4 was not moov, nothing more to do.");
            }
        } else {
            throw new Mp4FormatException("No top-level atoms found");
        }
        return false;
    }

    @WorkerThread
    private static void removeLocationData(@NonNull FileChannel fileChannel) throws IOException, Mp4FormatException {
        List<Mp4Atom> mp4AtomList = getLevelAtoms(fileChannel, 0, fileChannel.size());
        if (mp4AtomList.size() > 0) {
            final List<Mp4Atom> moovAtomList = filterAtomsByType(mp4AtomList, MP4_ATOM_TYPE_MOOV);
            if (moovAtomList.size() > 0) {
                traverseClearLocation(fileChannel, moovAtomList.get(0), 0);
            } else {
                throw new Mp4FormatException("No moov atom found");
            }
        } else {
            throw new Mp4FormatException("No top-level atoms found");
        }
    }

    @WorkerThread
    public static void zeroMp4Timestamps(@NonNull File mp4File) throws IOException {
        Log.d("Mp4Utils.zeroMp4Timestamps start size " + mp4File.length());
        final File tempFile = FileStore.getInstance().getTmpFile(RandomId.create());
        FileUtils.copyFile(mp4File, tempFile);

        try (final RandomAccessFile raFile = new RandomAccessFile(tempFile, "rw");
             final FileChannel fileChannel = raFile.getChannel()) {
            clearAtomTimestamps(fileChannel);
            fileChannel.force(false);
            if (!mp4File.delete()) {
                Log.e("MediaUtils.zeroMp4Timestamps: failed to delete " + mp4File.getAbsolutePath());
            }
            if (!tempFile.renameTo(mp4File)) {
                Log.e("MediaUtils.zeroMp4Timestamps: failed to rename " + tempFile.getAbsolutePath() + " to " + mp4File.getAbsolutePath());
            }
        } catch (Mp4FormatException e) {
            Log.e("MediaUtils.zeroMp4Timestamps: " + e);
        } finally {
            tempFile.delete();
        }
        Log.d("Mp4Utils.zeroMp4Timestamps end size " + mp4File.length());
    }

    @WorkerThread
    public static void makeMp4Streamable(@NonNull File mp4File) throws IOException, Mp4OperationException {
        Log.d("Mp4Utils.makeMp4Streamable start size " + mp4File.length());
        final File tempFile = FileStore.getInstance().getTmpFile(RandomId.create());

        try (final FileInputStream inputStream = new FileInputStream(mp4File);
             final FileChannel inFileChannel = inputStream.getChannel();
             final FileOutputStream outputStream = new FileOutputStream(tempFile);
             final FileChannel outFileChannel = outputStream.getChannel()) {
            if (putMoovAtomAtStart(inFileChannel, outFileChannel)) {
                outFileChannel.force(false);
                if (!mp4File.delete()) {
                    Log.e("MediaUtils.makeMp4Streamable: failed to delete " + mp4File.getAbsolutePath());
                }
                if (!tempFile.renameTo(mp4File)) {
                    Log.e("MediaUtils.makeMp4Streamable: failed to rename " + tempFile.getAbsolutePath() + " to " + mp4File.getAbsolutePath());
                }
            }
        } catch (Mp4FormatException e) {
            Log.e("MediaUtils.makeMp4Streamable: " + e);
            throw new Mp4OperationException("Failed to make mp4 streamable", e);
        } finally {
            tempFile.delete();
        }
        Log.d("Mp4Utils.makeMp4Streamable end size " + mp4File.length());
    }

    @WorkerThread
    public static void removeMp4Location(@NonNull File mp4File) throws IOException {
        Log.d("Mp4Utils.removeMp4Location start size " + mp4File.length());
        final File tempFile = FileStore.getInstance().getTmpFile(RandomId.create());
        FileUtils.copyFile(mp4File, tempFile);

        try (final RandomAccessFile raFile = new RandomAccessFile(tempFile, "rw");
             final FileChannel fileChannel = raFile.getChannel()) {
            removeLocationData(fileChannel);
            fileChannel.force(false);
            if (!mp4File.delete()) {
                Log.e("MediaUtils.removeMp4Location: failed to delete " + mp4File.getAbsolutePath());
            }
            if (!tempFile.renameTo(mp4File)) {
                Log.e("MediaUtils.removeMp4Location: failed to rename " + tempFile.getAbsolutePath() + " to " + mp4File.getAbsolutePath());
            }
        } catch (Mp4FormatException e) {
            Log.e("MediaUtils.removeMp4Location: " + e);
        } finally {
            tempFile.delete();
        }
        Log.d("Mp4Utils.removeMp4Location end size " + mp4File.length());
    }
}
