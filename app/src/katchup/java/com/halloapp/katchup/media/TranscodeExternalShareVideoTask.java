package com.halloapp.katchup.media;

import android.util.Size;

import androidx.annotation.NonNull;

import com.daasuu.mp4compose.FillModeCustomItem;
import com.daasuu.mp4compose.composer.Mp4Composer;
import com.halloapp.media.MediaUtils;

import java.io.File;
import java.io.IOException;

public class TranscodeExternalShareVideoTask extends Mp4Composer {

    public TranscodeExternalShareVideoTask(@NonNull File videoSrc, @NonNull File selfie, @NonNull File dest) throws IOException {
        super(videoSrc.getAbsolutePath(), dest.getAbsolutePath());

        Size videoSize = MediaUtils.getVideoDimensions(videoSrc);

        float scale = 720f / videoSize.getWidth();
        float scaledHeight = scale * videoSize.getHeight();

        float remainingHeight = Math.max(1280f - scaledHeight, 0);
        float translateY = remainingHeight / 3f;
        FillModeCustomItem fillModeCustomItem = new FillModeCustomItem(
                1f,
                0,
                0,
                (-remainingHeight / 3f) / 1280f,
                videoSize.getWidth(),
                videoSize.getHeight()
        );
        size(720, 1280);
        customFillMode(fillModeCustomItem);
        trim(0, 20_000);

        VideoAndSelfieOverlayFilter filter = new VideoAndSelfieOverlayFilter(
                Mp4FrameExtractor.extractFrames(selfie.getAbsolutePath(), (int)(720 * 0.4)),
                0.95f, 0.03f,
                translateY,
                scaledHeight
                );

        filter(filter);
    }
}
