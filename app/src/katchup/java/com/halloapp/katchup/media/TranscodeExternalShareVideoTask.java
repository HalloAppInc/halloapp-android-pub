package com.halloapp.katchup.media;

import android.util.Size;

import androidx.annotation.NonNull;

import com.daasuu.mp4compose.FillMode;
import com.daasuu.mp4compose.FillModeCustomItem;
import com.daasuu.mp4compose.VideoFormatMimeType;
import com.daasuu.mp4compose.composer.Mp4Composer;
import com.halloapp.Constants;
import com.halloapp.media.MediaUtils;

import java.io.File;
import java.io.IOException;

public class TranscodeExternalShareVideoTask extends Mp4Composer {

    public TranscodeExternalShareVideoTask(@NonNull File videoSrc, @NonNull File selfie, @NonNull File dest, boolean isSharingMedia, boolean isCenterCrop) throws IOException {
        super(videoSrc.getAbsolutePath(), dest.getAbsolutePath());

        // AVC && EXTERNAL_SHARE_BIT_RATE (4_600_000) are required to avoid WhatsApp trashing the quality
        videoFormatMimeType(VideoFormatMimeType.AVC);
        videoBitrate(Constants.EXTERNAL_SHARE_BIT_RATE);

        int externalVideoHeight = isSharingMedia ? Constants.EXTERNAL_SHARE_VIDEO_HEIGHT : Constants.EXTERNAL_SAVE_VIDEO_HEIGHT;

        size(Constants.EXTERNAL_SHARE_VIDEO_WIDTH, externalVideoHeight);

        VideoAndSelfieOverlayFilter filter;

        if (isCenterCrop) {
            fillMode(FillMode.PRESERVE_ASPECT_CROP);

            filter = new VideoAndSelfieOverlayFilter(
                    Mp4FrameExtractor.extractFrames(selfie.getAbsolutePath(), (int)(Constants.EXTERNAL_SHARE_VIDEO_WIDTH * 0.4)),
                    Constants.EXTERNAL_SHARE_SELFIE_POS_X, Constants.EXTERNAL_SHARE_SELFIE_POS_Y,
                    0,
                    Constants.EXTERNAL_SHARE_VIDEO_HEIGHT,
                    isSharingMedia,
                    true
            );
        } else {
            Size videoSize = MediaUtils.getVideoDimensions(videoSrc);
            float scale = (float) Constants.EXTERNAL_SHARE_VIDEO_WIDTH / videoSize.getWidth();
            float scaledHeight = scale * videoSize.getHeight();

            float remainingHeight = Math.max(externalVideoHeight - scaledHeight, 0);
            float translateY = remainingHeight / 3f;

            FillModeCustomItem fillModeCustomItem = new FillModeCustomItem(
                    1f,
                    0,
                    0,
                    -translateY / externalVideoHeight, // translation is a relative value, not absolute pixels e.g. shift 50%
                    videoSize.getWidth(),
                    videoSize.getHeight()
            );
            customFillMode(fillModeCustomItem);

            filter = new VideoAndSelfieOverlayFilter(
                    Mp4FrameExtractor.extractFrames(selfie.getAbsolutePath(), (int)(Constants.EXTERNAL_SHARE_VIDEO_WIDTH * 0.4)),
                    Constants.EXTERNAL_SHARE_SELFIE_POS_X, Constants.EXTERNAL_SHARE_SELFIE_POS_Y,
                    translateY,
                    scaledHeight,
                    isSharingMedia,
                    false
            );
        }

        trim(0, Constants.EXTERNAL_SHARE_MAX_VIDEO_DURATION_MS);

        filter(filter);
    }
}
