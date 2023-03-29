package com.daasuu.mp4compose.composer;

import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGL14;
import android.os.Build;
import android.util.Size;

import androidx.annotation.NonNull;

import com.daasuu.mp4compose.SampleType;
import com.daasuu.mp4compose.VideoFormatMimeType;
import com.daasuu.mp4compose.logger.AndroidLogger;
import com.halloapp.Constants;
import com.halloapp.katchup.media.ImageAndSelfieOverlayFilter;
import com.halloapp.katchup.media.Mp4FrameExtractor;
import com.halloapp.media.MediaUtils;

import java.io.File;
import java.io.IOException;

public class ImagePostShareGenerator {

    private static final int DRAIN_STATE_NONE = 0;
    private static final int DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY = 1;
    private static final int DRAIN_STATE_CONSUMED = 2;

    private final MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

    public static void generateExternalShareVideo(File image, File selfie, File dst, boolean isSharingMedia) throws IOException {
        // AVC && EXTERNAL_SHARE_BIT_RATE (4_600_000) are required to avoid WhatsApp trashing the quality
        generateVideo(
                Constants.EXTERNAL_SHARE_VIDEO_WIDTH,
                Constants.EXTERNAL_SHARE_VIDEO_HEIGHT,
                Constants.EXTERNAL_SHARE_IMAGE_VIDEO_DURATION_MS,
                Constants.EXTERNAL_SHARE_BIT_RATE,
                VideoFormatMimeType.AVC,
                image,
                selfie,
                dst,
                isSharingMedia);
    }

    public static void generateVideo(int width, int height, long durationMs, int bitrate, VideoFormatMimeType mimeType, File image, File selfie, File dst, boolean isSharingMedia) throws IOException {
        ImagePostShareGenerator generator = new ImagePostShareGenerator(image, selfie, dst);
        generator.setUp(width, height, bitrate, mimeType, isSharingMedia);
        generator.render(durationMs);
        generator.release();
    }

    public ImagePostShareGenerator(File image, File selfie, File dst) throws IOException {
        this.selfie = selfie;
        this.image = image;
        this.dst = dst;
    }

    private File image;
    private File selfie;
    private File dst;

    private MediaCodec encoder;
    private DecoderSurface renderSurface;
    private EncoderSurface encoderSurface;
    private MediaFormat outputFormat;
    private MediaMuxer mediaMuxer;
    private MuxRender muxRender;

    private Mp4FrameExtractor.Frame[] selfieFrames;
    private int frameIndex;

    private ImageAndSelfieOverlayFilter filter;

    private MediaFormat actualOutputFormat;

    private int width;
    private int height;

    private AndroidLogger logger;

    private long durationMs;
    private boolean encoderStarted;

    public void setUp(int width, int height, int bitrate, VideoFormatMimeType mimeType, boolean isSharingMedia) throws IOException {
        Bitmap img = MediaUtils.decodeImage(image, Constants.EXTERNAL_SHARE_VIDEO_WIDTH, Constants.EXTERNAL_SHARE_VIDEO_HEIGHT);
        selfieFrames = Mp4FrameExtractor.extractFrames(selfie.getAbsolutePath(), (int)(width * 0.4));

        filter = new ImageAndSelfieOverlayFilter(img, selfieFrames, Constants.EXTERNAL_SHARE_SELFIE_POS_X, Constants.EXTERNAL_SHARE_SELFIE_POS_Y, isSharingMedia);

        logger = new AndroidLogger();
        this.width = width;
        this.height = height;

        if (bitrate <= 0) {
            bitrate = calcBitRate(width, height);
        }
        outputFormat = createVideoOutputFormatWithAvailableEncoders(mimeType, bitrate, new Size(width, height));
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            // Only LOLLIPOP sets KEY_FRAME_RATE here.
            outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        }
        try {
            encoder = MediaCodec.createEncoderByType(this.outputFormat.getString(MediaFormat.KEY_MIME));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        encoder.configure(this.outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        encoderSurface = new EncoderSurface(encoder.createInputSurface(), EGL14.EGL_NO_CONTEXT);
        encoderSurface.makeCurrent();
        encoder.start();
        encoderStarted = true;

        renderSurface = new DecoderSurface(filter, logger);
        Size resolution = new Size(width, height);
        renderSurface.setInputResolution(resolution);
        renderSurface.setOutputResolution(resolution);
        renderSurface.completeParams();
        mediaMuxer = new MediaMuxer(dst.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        muxRender = new MuxRender(mediaMuxer, logger);
    }

    private long presentationTimeUs;

    public void render(long durationMs) {
        this.durationMs = durationMs;
        while (!isEncoderEOS) {
            stepPipeline();
        }
        mediaMuxer.stop();
    }

    public void release() {
        if (renderSurface != null) {
            renderSurface.release();
            renderSurface = null;
        }
        if (encoderSurface != null) {
            encoderSurface.release();
            encoderSurface = null;
        }
        if (encoder != null) {
            if (encoderStarted) encoder.stop();
            encoder.release();
            encoder = null;
        }
        if (muxRender != null) {
            muxRender = null;
        }
        if (mediaMuxer != null) {
            mediaMuxer.release();
            mediaMuxer = null;
        }
    }

    void stepPipeline() {
        boolean busy = false;
        int status;
        while (drainEncoder() != DRAIN_STATE_NONE) {
            busy = true;
        }
        stepRender();
    }

    private void stepRender() {
        if (isRenderEOS) return;
        renderToEncoder();
        frameIndex++;
        frameIndex = frameIndex % selfieFrames.length;
        if (frameIndex > 0) {
            presentationTimeUs += selfieFrames[frameIndex].presentationTimeUs - selfieFrames[frameIndex - 1].presentationTimeUs;
        }
        if (presentationTimeUs >= durationMs * 1000L) {
            encoder.signalEndOfInputStream();
            isRenderEOS = true;
        }
    }

    private boolean isRenderEOS;
    private boolean isEncoderEOS;

    private long writtenPresentationTimeUs;

    private void renderToEncoder() {
        if (filter != null) {
            filter.updatePresentationTimeUs(presentationTimeUs);
        }
        renderSurface.drawImage();
        encoderSurface.setPresentationTime(presentationTimeUs * 1000);
        encoderSurface.swapBuffers();
    }

    private int drainEncoder() {
        if (isEncoderEOS) return DRAIN_STATE_NONE;
        int result = encoder.dequeueOutputBuffer(bufferInfo, 0);
        switch (result) {
            case MediaCodec.INFO_TRY_AGAIN_LATER:
                return DRAIN_STATE_NONE;
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                if (actualOutputFormat != null) {
                    throw new RuntimeException("Video output format changed twice.");
                }
                actualOutputFormat = encoder.getOutputFormat();
                muxRender.setOutputFormat(SampleType.VIDEO, actualOutputFormat);
                muxRender.onSetOutputFormat();
                return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
        }
        if (actualOutputFormat == null) {
            throw new RuntimeException("Could not determine actual output format.");
        }

        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            isEncoderEOS = true;
            bufferInfo.set(0, 0, 0, bufferInfo.flags);
        }
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            // SPS or PPS, which should be passed by MediaFormat.
            encoder.releaseOutputBuffer(result, false);
            return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY;
        }
        muxRender.writeSampleData(SampleType.VIDEO, encoder.getOutputBuffer(result), bufferInfo);
        writtenPresentationTimeUs = bufferInfo.presentationTimeUs;
        encoder.releaseOutputBuffer(result, false);
        return DRAIN_STATE_CONSUMED;
    }

    private int calcBitRate(int width, int height) {
        return (int) (0.25 * 30 * width * height);
    }

    @NonNull
    private static MediaFormat createVideoOutputFormatWithAvailableEncoders(@NonNull final VideoFormatMimeType mimeType,
                                                                            final int bitrate,
                                                                            @NonNull final Size outputResolution) {
        final MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);

        if (mimeType != VideoFormatMimeType.AUTO) {
            final MediaFormat mediaFormat = createVideoFormat(mimeType.getFormat(), bitrate, outputResolution);
            if (mediaCodecList.findEncoderForFormat(mediaFormat) != null) {
                return mediaFormat;
            }
        }

        final MediaFormat hevcMediaFormat = createVideoFormat(VideoFormatMimeType.HEVC.getFormat(), bitrate, outputResolution);
        if (mediaCodecList.findEncoderForFormat(hevcMediaFormat) != null) {
            return hevcMediaFormat;
        }

        final MediaFormat avcMediaFormat = createVideoFormat(VideoFormatMimeType.AVC.getFormat(), bitrate, outputResolution);
        if (mediaCodecList.findEncoderForFormat(avcMediaFormat) != null) {
            return avcMediaFormat;
        }

        final MediaFormat mp4vesMediaFormat = createVideoFormat(VideoFormatMimeType.MPEG4.getFormat(), bitrate, outputResolution);
        if (mediaCodecList.findEncoderForFormat(mp4vesMediaFormat) != null) {
            return mp4vesMediaFormat;
        }

        return createVideoFormat(VideoFormatMimeType.H263.getFormat(), bitrate, outputResolution);
    }

    @NonNull
    private static MediaFormat createVideoFormat(@NonNull final String mimeType,
                                                 final int bitrate,
                                                 @NonNull final Size outputResolution) {
        final MediaFormat outputFormat =
                MediaFormat.createVideoFormat(mimeType,
                        outputResolution.getWidth(),
                        outputResolution.getHeight());

        outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        // On Build.VERSION_CODES.LOLLIPOP, format must not contain a MediaFormat#KEY_FRAME_RATE.
        // https://developer.android.com/reference/android/media/MediaCodecInfo.CodecCapabilities.html#isFormatSupported(android.media.MediaFormat)
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.LOLLIPOP) {
            // Required but ignored by the encoder
            outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        }
        outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        outputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

        return outputFormat;
    }
}
