package com.halloapp.katchup.media;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.core.content.res.ResourcesCompat;

import com.halloapp.AppContext;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.R;

public class ImageAndSelfieOverlayFilter extends SelfieOverlayFilter {

    private Bitmap image;

    private Paint paint = new Paint();

    private String url;

    public ImageAndSelfieOverlayFilter(Bitmap img, Mp4FrameExtractor.Frame[] frames, float x, float y) {
        super(frames, x, y);

        this.image = img;

        paint.setAntiAlias(true);
        paint.setColor(Constants.EXTERNAL_SHARE_FOOTER_COLOR);
        paint.setTextSize(Constants.EXTERNAL_SHARE_FOOTER_TEXT_SIZE);
        paint.setTypeface(ResourcesCompat.getFont(AppContext.getInstance().get(), R.font.krona_one));
        paint.setTextAlign(Paint.Align.CENTER);

        url = "katchup.com/" + Me.getInstance().getUsername();
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
