package com.halloapp.katchup.media;

import androidx.annotation.NonNull;

import com.daasuu.mp4compose.FillMode;
import com.daasuu.mp4compose.composer.Mp4Composer;

public class PrepareVideoReactionTask extends Mp4Composer {
    public PrepareVideoReactionTask(@NonNull String srcPath, @NonNull String destPath) {
        super(srcPath, destPath);
        size(512, 512);
        fillMode(FillMode.PRESERVE_ASPECT_CROP);
    }
}
