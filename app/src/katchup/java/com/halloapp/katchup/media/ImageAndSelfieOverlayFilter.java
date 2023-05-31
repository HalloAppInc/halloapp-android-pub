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

    private final Bitmap image;

    private final Paint paint = new Paint();

    private final String url;
    private final boolean isTextInside;

    public ImageAndSelfieOverlayFilter(Bitmap img, Mp4FrameExtractor.Frame[] frames, float x, float y, boolean isSharingMedia, boolean isTextInside) {
        super(frames, x, y);

        this.image = img;
        this.isTextInside = isTextInside;

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
        int remainingHeight = canvasHeight - scaledHeight;
        float textPaddingBottom = Constants.EXTERNAL_SHARE_FOOTER_TEXT_SIZE / 2;
        float textPaddingRight = Constants.EXTERNAL_SHARE_FOOTER_TEXT_SIZE;
        float textY = isTextInside ? (scaledHeight - textPaddingBottom) : (scaledHeight + (Math.max(remainingHeight, 0) / 6f));

        dst.right = canvasWidth;
        dst.bottom = scaledHeight;

        canvas.save();
        canvas.translate(0, Math.max(remainingHeight / 3, 0));

        canvas.drawBitmap(image, null, dst, null);
        canvas.drawText(url, canvasWidth - textPaddingRight, textY, paint);

        super.drawCanvas(canvas);
        canvas.restore();
    }
}
