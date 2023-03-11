package com.halloapp.katchup.media;

import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.core.content.res.ResourcesCompat;

import com.halloapp.AppContext;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.R;

public class VideoAndSelfieOverlayFilter extends SelfieOverlayFilter {

    public VideoAndSelfieOverlayFilter(Mp4FrameExtractor.Frame[] frames, float x, float y, float translateY, float videoHeight, boolean isSharingMedia) {
        super(frames, x, y);

        paint.setAntiAlias(true);
        paint.setColor(Constants.EXTERNAL_SHARE_FOOTER_COLOR);
        paint.setTextSize(Constants.EXTERNAL_SHARE_FOOTER_TEXT_SIZE);
        paint.setTypeface(ResourcesCompat.getFont(AppContext.getInstance().get(), R.font.krona_one));
        paint.setTextAlign(Paint.Align.CENTER);

        if (isSharingMedia) {
            url = "katchup.com/" + Me.getInstance().getUsername();
        } else {
            url = "";
        }

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
