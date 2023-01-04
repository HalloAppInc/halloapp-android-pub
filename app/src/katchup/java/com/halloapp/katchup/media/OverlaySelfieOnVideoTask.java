package com.halloapp.katchup.media;

import androidx.annotation.NonNull;

import com.daasuu.mp4compose.composer.Mp4Composer;
import com.halloapp.content.Media;

import java.io.File;
import java.io.IOException;

public class OverlaySelfieOnVideoTask extends Mp4Composer {

    private File dest;

    public OverlaySelfieOnVideoTask(@NonNull Media content, @NonNull File selfie, float selfieX, float selfieY, @NonNull File dest) throws IOException {
        super(content.file.getAbsolutePath(), dest.getAbsolutePath());
        filter(new SelfieOverlayFilter(Mp4FrameExtractor.extractFrames(selfie.getAbsolutePath(), 240), selfieX, selfieY));
        this.dest = dest;
    }
}
