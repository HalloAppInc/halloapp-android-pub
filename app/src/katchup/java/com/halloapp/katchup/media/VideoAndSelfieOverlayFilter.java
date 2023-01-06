package com.halloapp.katchup.media;

import android.graphics.Canvas;
import android.graphics.Paint;

public class VideoAndSelfieOverlayFilter extends SelfieOverlayFilter {

    public VideoAndSelfieOverlayFilter(Mp4FrameExtractor.Frame[] frames, float x, float y, float translateY, float videoHeight) {
        super(frames, x, y);

        paint.setAntiAlias(true);
        paint.setColor(0xFFFED3D3);
        paint.setTextSize(30f);
        paint.setTextAlign(Paint.Align.CENTER);

        // TODO: get users url?
        url = "katchup.com";

        this.translateY = translateY;
        this.videoHeight = videoHeight;
    }

    private float translateY;
    private float videoHeight;

    private Paint paint = new Paint();

    private String url;

    @Override
    protected void drawCanvas(Canvas canvas) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        float remainingHeight = canvasHeight - videoHeight;

        canvas.save();
        canvas.translate(0, translateY);

        canvas.drawText(url, canvasWidth / 2f, videoHeight + (Math.max(remainingHeight, 0) / 6f), paint);
        super.drawCanvas(canvas);
        canvas.restore();
    }
}
