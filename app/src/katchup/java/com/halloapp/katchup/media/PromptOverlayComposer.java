package com.halloapp.katchup.media;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.NonNull;

import com.daasuu.mp4compose.composer.Mp4Composer;
import com.daasuu.mp4compose.filter.GlOverlayFilter;

import java.io.File;

public class PromptOverlayComposer extends Mp4Composer {

    public PromptOverlayComposer(@NonNull File src, @NonNull File dest, @NonNull Bitmap prompt, float bottomMarginFraction) {
        super(src.getAbsolutePath(), dest.getAbsolutePath());

        GlOverlayFilter videoOverlayFilter = new PromptOverlayFilter(prompt, bottomMarginFraction);
        filter(videoOverlayFilter);
    }

    static class PromptOverlayFilter extends GlOverlayFilter {
        Paint paint = new Paint();
        Bitmap prompt;
        float bottomMarginFraction;

        PromptOverlayFilter(@NonNull Bitmap prompt, float bottomMarginFraction) {
            this.prompt = prompt;
            this.bottomMarginFraction = bottomMarginFraction;
            paint.setAntiAlias(true);
        }

        @Override
        protected void drawCanvas(Canvas canvas) {
            canvas.drawBitmap(prompt, (canvas.getWidth() - prompt.getWidth()) / 2f, canvas.getHeight() * bottomMarginFraction, paint);
        }
    }
}
