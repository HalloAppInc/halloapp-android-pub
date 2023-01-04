package com.halloapp.katchup.media;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;

import com.daasuu.mp4compose.filter.GlOverlayFilter;

import java.io.IOException;

public class SelfieOverlayFilter extends GlOverlayFilter {

    private static final float OVAL_HEIGHT = 0.75f; // assume this is always < 1
    private static final int OVAL_ROTATE_DEG = 12;

    private final float positionX;
    private final float positionY;

    private final Path path = new Path();

    private float width;
    private float height;

    private Mp4FrameExtractor.Frame[] frames;
    private int currentFrame = 0;

    public SelfieOverlayFilter(Mp4FrameExtractor.Frame[] frames, float x, float y) {
        this.positionX = x;
        this.positionY = y;

        this.frames = frames;
        if (frames != null && frames[0] != null) {

            this.width = frames[0].bitmap.getWidth();
            this.height = computeBoxRatio() * width;

            final float halfWidth = width / 2f;
            final float halfHeight = height / 2f;
            final float radius = halfWidth;

            path.addCircle(halfWidth, halfHeight, radius, Path.Direction.CCW);
            Matrix matrix = new Matrix();
            matrix.postScale(1, OVAL_HEIGHT, halfWidth, halfHeight);
            matrix.postRotate(OVAL_ROTATE_DEG, halfWidth, halfHeight);
            path.transform(matrix);
        }
    }

    private static float computeBoxRatio() {
        double radAngle = Math.toRadians(OVAL_ROTATE_DEG);
        double x = Math.sqrt(Math.pow(Math.cos(radAngle), 2) + (Math.pow(OVAL_HEIGHT, 2) * Math.pow(Math.sin(radAngle), 2)));
        double y = Math.sqrt(Math.pow(Math.sin(radAngle), 2) + (Math.pow(OVAL_HEIGHT, 2) * Math.pow(Math.cos(radAngle), 2)));

        return (float) (y / x);
    }

    @Override
    protected void drawCanvas(Canvas canvas) {
        Mp4FrameExtractor.Frame frame = frames[currentFrame];
        if (frame.bitmap != null && !frame.bitmap.isRecycled()) {
            int cW = canvas.getWidth();
            int cH = canvas.getHeight();

            float x = positionX * (cW - width);
            float y = positionY * (cH - height);

            canvas.save();
            canvas.translate(x, y);
            canvas.clipPath(path);
            if (frame.bitmap.getHeight() > height) {
                canvas.drawBitmap(frame.bitmap, 0, - (frame.bitmap.getHeight() - height) / 2, null);
            } else {
                canvas.drawBitmap(frame.bitmap, 0, 0, null);
            }
            canvas.restore();
        }
    }

    @Override
    public void updatePresentationTimeUs(long presentationTimeUs) {
        Mp4FrameExtractor.Frame frame = frames[currentFrame];
        int nextFrameIndex = (currentFrame + 1) % frames.length;
        Mp4FrameExtractor.Frame nextFrame = frames[nextFrameIndex];
        if (presentationTimeUs > nextFrame.presentationTimeUs || presentationTimeUs < frame.presentationTimeUs) {
            currentFrame = nextFrameIndex;
        }
    }
}
