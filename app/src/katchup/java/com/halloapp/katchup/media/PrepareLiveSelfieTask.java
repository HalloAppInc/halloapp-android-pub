package com.halloapp.katchup.media;

import androidx.annotation.NonNull;

import com.daasuu.mp4compose.FillMode;
import com.daasuu.mp4compose.composer.Mp4Composer;

public class PrepareLiveSelfieTask extends Mp4Composer {

    public PrepareLiveSelfieTask(@NonNull String srcPath, @NonNull String destPath, boolean isMirrored) {
        super(srcPath, destPath);
        size(512, 512);
        fillMode(FillMode.PRESERVE_ASPECT_CROP);
        mute(true);
        flipHorizontal(isMirrored);
    }
}
