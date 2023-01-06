package com.halloapp.katchup.media;

import android.util.Size;

import androidx.annotation.NonNull;

import com.daasuu.mp4compose.FillModeCustomItem;
import com.daasuu.mp4compose.composer.Mp4Composer;
import com.halloapp.Constants;
import com.halloapp.media.MediaUtils;

import java.io.File;
import java.io.IOException;

public class TranscodeExternalShareVideoTask extends Mp4Composer {

    public TranscodeExternalShareVideoTask(@NonNull File videoSrc, @NonNull File selfie, @NonNull File dest) throws IOException {
        super(videoSrc.getAbsolutePath(), dest.getAbsolutePath());

        Size videoSize = MediaUtils.getVideoDimensions(videoSrc);

        float scale = (float) Constants.EXTERNAL_SHARE_VIDEO_WIDTH / videoSize.getWidth();
        float scaledHeight = scale * videoSize.getHeight();

        float remainingHeight = Math.max(Constants.EXTERNAL_SHARE_VIDEO_HEIGHT - scaledHeight, 0);
        float translateY = remainingHeight / 3f;
        FillModeCustomItem fillModeCustomItem = new FillModeCustomItem(
                1f,
                0,
                0,
                -translateY / Constants.EXTERNAL_SHARE_VIDEO_HEIGHT, // translation is a relative value, not absolute pixels e.g. shift 50%
                videoSize.getWidth(),
                videoSize.getHeight()
        );
        size(Constants.EXTERNAL_SHARE_VIDEO_WIDTH, Constants.EXTERNAL_SHARE_VIDEO_HEIGHT);
        customFillMode(fillModeCustomItem);
        trim(0, Constants.EXTERNAL_SHARE_MAX_VIDEO_DURATION_MS);

        VideoAndSelfieOverlayFilter filter = new VideoAndSelfieOverlayFilter(
                Mp4FrameExtractor.extractFrames(selfie.getAbsolutePath(), (int)(Constants.EXTERNAL_SHARE_VIDEO_WIDTH * 0.4)),
                Constants.EXTERNAL_SHARE_SELFIE_POS_X, Constants.EXTERNAL_SHARE_SELFIE_POS_Y,
                translateY,
                scaledHeight
                );

        filter(filter);
    }
}
