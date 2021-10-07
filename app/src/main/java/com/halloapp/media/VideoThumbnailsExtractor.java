package com.halloapp.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Build;
import android.util.Size;

import androidx.annotation.NonNull;

import com.dstukalov.videoconverter.OutputSurface;
import com.halloapp.util.logs.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// This code is based on the demo by Dmitri Stukalov
// https://github.com/dstukalov/VideoConverter/blob/master/demo/src/main/java/com/dstukalov/videoconverterdemo/VideoThumbnailsExtractor.java

public class VideoThumbnailsExtractor {
    private enum State { WAITING, DONE, SUCCESS }
    private static final int TIMEOUT_US = 10000;

    public interface Callback {
        void extracted(Bitmap thumbnail);
    }

    public static void extract(@NonNull Context context, @NonNull Uri uri, int count, int size, @NonNull Callback callback) {
        MediaExtractor extractor = new MediaExtractor();

        try {
            extractor.setDataSource(context, uri, null);
            extract(extractor, count, size, callback);
        } catch (IOException e) {
            Log.e("VideoThumbnailsExtractor.extract unable to set Uri " + uri + " as source", e);
        } finally {
            extractor.release();
        }
    }

    public static void extract(@NonNull String path, int count, int size, @NonNull Callback callback) {
        MediaExtractor extractor = new MediaExtractor();

        try {
            extractor.setDataSource(path);
            extract(extractor, count, size, callback);
        } catch (IOException e) {
            Log.e("VideoThumbnailsExtractor.extract unable to set path " + path + " as source", e);
        } finally {
            extractor.release();
        }
    }

    public static void extract(@NonNull MediaExtractor extractor, int count, int size, @NonNull Callback callback) {
        MediaCodec decoder = null;
        OutputSurface outputSurface = null;

        try {
            int trackIndex = getVideoTrackIndex(extractor);
            if (trackIndex == -1) {
                Log.e("VideoThumbnailsExtractor.extract missing video track");
                return;
            }

            extractor.selectTrack(trackIndex);
            MediaFormat format = extractor.getTrackFormat(trackIndex);

            String mime = format.getString(MediaFormat.KEY_MIME);
            long duration = format.getLong(MediaFormat.KEY_DURATION);

            Size outputSize = computeOutputSize(format, size);

            outputSurface = new OutputSurface(outputSize.getWidth(), outputSize.getHeight());
            decoder = MediaCodec.createDecoderByType(mime);
            decoder.configure(format, outputSurface.getSurface(), null, 0);
            decoder.start();

            extract(extractor, decoder, outputSurface, outputSize, duration, count, callback);
        } catch (IOException e) {
            Log.e("VideoThumbnailsExtractor.extract", e);
        } finally {
            extractor.release();

            if (outputSurface != null) {
                outputSurface.release();
            }

            if (decoder != null) {
                decoder.stop();
                decoder.release();
            }
        }
    }

    private static int getVideoTrackIndex(@NonNull MediaExtractor extractor) {
        for (int i = 0; i < extractor.getTrackCount(); ++i) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);

            if (mime != null && mime.startsWith("video/")) {
                return i;
            }
        }

        return -1;
    }

    @NonNull
    private static Size computeOutputSize(@NonNull MediaFormat format, int size) {
        int rotation = 0;
        if (Build.VERSION.SDK_INT >= 23) {
            rotation = format.containsKey(MediaFormat.KEY_ROTATION) ? format.getInteger(MediaFormat.KEY_ROTATION) : 0;
        }

        int width = (rotation % 180 == 90) ? format.getInteger(MediaFormat.KEY_HEIGHT) : format.getInteger(MediaFormat.KEY_WIDTH);
        int height = (rotation % 180 == 90) ? format.getInteger(MediaFormat.KEY_WIDTH) : format.getInteger(MediaFormat.KEY_HEIGHT);

        int outputWidth;
        int outputHeight;
        if (width < height) {
            outputWidth = size;
            outputHeight = height * size / width;
        } else {
            outputWidth = width * size / height;
            outputHeight = size;
        }

        Log.i("VideoThumbnailsExtractor original size: " + width + "x" + height + " " + rotation + " degrees");
        Log.i("VideoThumbnailsExtractor output size: " + outputWidth + "x" + outputHeight);

        return new Size(outputWidth, outputHeight);
    }

    private static void extract(@NonNull MediaExtractor extractor, @NonNull MediaCodec decoder, @NonNull OutputSurface outputSurface, @NonNull Size outputSize, long duration, int count, @NonNull Callback callback) {
        Log.i("VideoThumbnailsExtractor extracting started");

        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        ByteBuffer pixelBuf = ByteBuffer.allocateDirect(outputSize.getWidth() * outputSize.getHeight() * 4);
        pixelBuf.order(ByteOrder.LITTLE_ENDIAN);

        int samplesExtracted = 0;
        int thumbnailsCreated = 0;
        boolean inputDone = false;

        while (true) {
            if (!inputDone) {
                long timeUs = duration * samplesExtracted / count;

                State extractState = extractSample(extractor, decoder, timeUs, samplesExtracted == count - 1);
                if (extractState == State.SUCCESS) {
                    samplesExtracted++;
                }

                if (extractState == State.DONE || samplesExtracted >= count) {
                    inputDone = true;
                    Log.i("VideoThumbnailsExtractor sample extracting finished");
                }
            }

            State decodeState = decodeThumbnail(decoder, outputSurface, outputSize, info, pixelBuf, callback);
            if (decodeState == State.SUCCESS) {
                thumbnailsCreated++;
            }

            if (decodeState == State.DONE || thumbnailsCreated >= count) {
                break;
            }
        }

        Log.i("VideoThumbnailsExtractor extracting finished");
    }

    /**
     * @return number of extracted samples or -1 if no more samples can be extracted
     */
    private static State extractSample(@NonNull MediaExtractor extractor, @NonNull MediaCodec decoder, long timeUs, boolean isLast) {
        int bufferIndex = decoder.dequeueInputBuffer(TIMEOUT_US);
        if (bufferIndex < 0) {
            return State.WAITING;
        }

        ByteBuffer buffer = decoder.getInputBuffer(bufferIndex);
        if (buffer == null) {
            return State.WAITING;
        }

        extractor.seekTo(timeUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        Log.i("VideoThumbnailsExtractor.readAndQueueInputBuffer seek to " + timeUs + ", actual " + extractor.getSampleTime());

        int sampleSize = extractor.readSampleData(buffer, 0);

        if (sampleSize < 0) {
            decoder.queueInputBuffer(bufferIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            return State.DONE;
        } else {
            long presentationTimeUs = extractor.getSampleTime();
            decoder.queueInputBuffer(bufferIndex, 0, sampleSize, presentationTimeUs, isLast ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
            return State.SUCCESS;
        }
    }

    /**
     * @return number of decoded thumbnails or -1 if no more thumbnails can be decoded
     */
    private static State decodeThumbnail(@NonNull MediaCodec decoder, @NonNull OutputSurface outputSurface, @NonNull Size outputSize, @NonNull MediaCodec.BufferInfo info, ByteBuffer pixelBuf, @NonNull Callback callback) {
        int outputBufferIndex = decoder.dequeueOutputBuffer(info, TIMEOUT_US);
        if (outputBufferIndex < 0) {
            return State.WAITING;
        }

        if (info.size != 0) {
            decoder.releaseOutputBuffer(outputBufferIndex, true);
            outputSurface.awaitNewImage();
            outputSurface.drawImage();

            pixelBuf.rewind();
            GLES20.glReadPixels(0, 0, outputSize.getWidth(), outputSize.getHeight(), GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuf);

            final Bitmap bitmap = Bitmap.createBitmap(outputSize.getWidth(), outputSize.getHeight(), Bitmap.Config.ARGB_8888);
            pixelBuf.rewind();
            bitmap.copyPixelsFromBuffer(pixelBuf);

            Log.i("VideoThumbnailsExtractor.decodeThumbnail for frame at " + info.presentationTimeUs);

            // images are always returned flipped vertically, so flip them to the correct position
            Matrix matrix = new Matrix();
            matrix.postScale(1, -1, (float) outputSize.getWidth() / 2, (float) outputSize.getHeight() / 2);

            callback.extracted(Bitmap.createBitmap(bitmap, 0, 0, outputSize.getWidth(), outputSize.getHeight(), matrix, false));

            return State.SUCCESS;
        } else {
            decoder.releaseOutputBuffer(outputBufferIndex, false);
            return (info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0 ? State.DONE : State.WAITING;
        }
    }
}

