package com.halloapp.katchup.media;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.daasuu.mp4compose.filter.GlOverlayFilter;
import com.halloapp.util.logs.Log;

public class ImageDumpOverlayFilter extends GlOverlayFilter {

    private final Rect src = new Rect();
    private final Rect dst = new Rect();
    private final Mp4FrameExtractor.Frame[] frames;
    private int currentFrameIndex = 0;

    public ImageDumpOverlayFilter(Mp4FrameExtractor.Frame[] frames) {
        this.frames = frames;
    }

    protected void drawCanvas(Canvas canvas) {
        Mp4FrameExtractor.Frame frame = frames[currentFrameIndex];

        if (frame.bitmap != null && !frame.bitmap.isRecycled()) {
            float canvasWidth = canvas.getWidth();
            float canvasHeight = canvas.getHeight();
            float bitmapWidth = frame.bitmap.getWidth();
            float bitmapHeight = frame.bitmap.getHeight();
            float scale = Math.min(bitmapWidth / canvasWidth,  bitmapHeight / canvasHeight);
            float cropWidth = canvasWidth * scale;
            float cropHeight = canvasHeight * scale;

            src.left = (int)((bitmapWidth - cropWidth) / 2);
            src.right = (int)((bitmapWidth + cropWidth) / 2);
            src.top = (int)((bitmapHeight - cropHeight) / 2);
            src.bottom = (int)((bitmapHeight + cropHeight) / 2);

            dst.right = canvas.getWidth();
            dst.bottom = canvas.getHeight();

            canvas.save();
            canvas.drawBitmap(frame.bitmap, src, dst, null);
            canvas.restore();
        }
    }

    @Override
    public void updatePresentationTimeUs(long presentationTimeUs) {
        Mp4FrameExtractor.Frame frame = frames[currentFrameIndex];
        int nextFrameIndex = (currentFrameIndex + 1) % frames.length;
        Mp4FrameExtractor.Frame nextFrame = frames[nextFrameIndex];

        if (presentationTimeUs >= nextFrame.presentationTimeUs || presentationTimeUs < frame.presentationTimeUs) {
            currentFrameIndex = nextFrameIndex;
        }
    }
}
