package com.halloapp.katchup.media;

import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.core.content.res.ResourcesCompat;

import com.halloapp.AppContext;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.R;

public class VideoAndSelfieOverlayFilter extends SelfieOverlayFilter {

    private final boolean isTextInside;

    public VideoAndSelfieOverlayFilter(Mp4FrameExtractor.Frame[] frames, float x, float y, float translateY, float videoHeight, boolean isSharingMedia, boolean isTextInside) {
        super(frames, x, y);

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

        this.translateY = translateY;
        this.videoHeight = videoHeight;
    }

    private final float translateY;
    private final float videoHeight;

    private final Paint paint = new Paint();

    private final String url;

    @Override
    protected void drawCanvas(Canvas canvas) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        float textPaddingBottom = Constants.EXTERNAL_SHARE_FOOTER_TEXT_SIZE / 2;
        float textPaddingRight = Constants.EXTERNAL_SHARE_FOOTER_TEXT_SIZE;
        float remainingHeight = canvasHeight - videoHeight;
        float textY = isTextInside ? (videoHeight - textPaddingBottom) : (videoHeight + (Math.max(remainingHeight, 0) / 6f));

        canvas.save();
        canvas.translate(0, translateY);

        canvas.drawText(url, canvasWidth - textPaddingRight, textY, paint);
        super.drawCanvas(canvas);
        canvas.restore();
    }
}
