package com.halloapp.katchup.media;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class ImageAndSelfieOverlayFilter extends SelfieOverlayFilter {

    private Bitmap image;

    private Paint paint = new Paint();

    private String url;

    public ImageAndSelfieOverlayFilter(Bitmap img, Mp4FrameExtractor.Frame[] frames, float x, float y) {
        super(frames, x, y);

        this.image = img;

        paint.setAntiAlias(true);
        paint.setColor(0xFFFED3D3);
        paint.setTextSize(30f);
        paint.setTextAlign(Paint.Align.CENTER);

        // TODO: get users url?
        url = "katchup.com";
    }

    private Rect dst = new Rect();

    @Override
    protected void drawCanvas(Canvas canvas) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        int bitmapWidth = image.getWidth();
        int bitmapHeight = image.getHeight();

        int scaledHeight = (int)(canvasWidth * ((float)bitmapHeight / (float)bitmapWidth));

        dst.right = canvasWidth;
        int remainingHeight = canvasHeight - scaledHeight;
        dst.bottom = scaledHeight;

        canvas.save();
        canvas.translate(0, Math.max(remainingHeight / 3, 0));

        canvas.drawBitmap(image, null, dst, null);

        canvas.drawText(url, canvasWidth / 2f, scaledHeight + (Math.max(remainingHeight, 0) / 6f), paint);
        super.drawCanvas(canvas);
        canvas.restore();
    }
}
