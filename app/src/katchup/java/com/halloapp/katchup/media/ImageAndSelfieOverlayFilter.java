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

    public ImageAndSelfieOverlayFilter(Bitmap img, Mp4FrameExtractor.Frame[] frames, float x, float y, boolean isSharingMedia) {
        super(frames, x, y);

        this.image = img;

        paint.setAntiAlias(true);
        paint.setColor(Constants.EXTERNAL_SHARE_FOOTER_TEXT_COLOR);
        paint.setTextSize(Constants.EXTERNAL_SHARE_FOOTER_TEXT_SIZE);
        paint.setTypeface(ResourcesCompat.getFont(AppContext.getInstance().get(), R.font.krona_one));
        paint.setTextAlign(Paint.Align.RIGHT);

        if (isSharingMedia) {
            url = "katchup.com/" + Me.getInstance().getUsername();
        } else {
            url = "";
        }
    }

    private Rect dst = new Rect();

    @Override
    protected void drawCanvas(Canvas canvas) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        int bitmapWidth = image.getWidth();
        int bitmapHeight = image.getHeight();

        int scaledHeight = (int)(canvasWidth * ((float)bitmapHeight / (float)bitmapWidth));
        float textPaddingBottom = Constants.EXTERNAL_SHARE_FOOTER_TEXT_SIZE / 2;
        float textPaddingRight = Constants.EXTERNAL_SHARE_FOOTER_TEXT_SIZE;

        dst.right = canvasWidth;
        int remainingHeight = canvasHeight - scaledHeight;
        dst.bottom = scaledHeight;

        canvas.save();
        canvas.translate(0, Math.max(remainingHeight / 3, 0));

        canvas.drawBitmap(image, null, dst, null);

        canvas.drawText(url, canvasWidth - textPaddingRight, scaledHeight - textPaddingBottom, paint);
        super.drawCanvas(canvas);
        canvas.restore();
    }
}
