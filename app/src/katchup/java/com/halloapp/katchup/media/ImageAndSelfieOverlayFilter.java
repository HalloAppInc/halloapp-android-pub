package com.halloapp.katchup.media;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class ImageAndSelfieOverlayFilter extends SelfieOverlayFilter {

    private Bitmap image;

    public ImageAndSelfieOverlayFilter(Bitmap img, Mp4FrameExtractor.Frame[] frames, float x, float y) {
        super(frames, x, y);

        this.image = img;
    }

    private Rect dst = new Rect();

    @Override
    protected void drawCanvas(Canvas canvas) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        dst.right = canvasWidth;
        dst.bottom = canvasHeight;

        canvas.drawBitmap(image, null, dst, null);
        super.drawCanvas(canvas);
    }
}
