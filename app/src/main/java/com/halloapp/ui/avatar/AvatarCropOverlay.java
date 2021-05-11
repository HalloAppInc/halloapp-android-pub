package com.halloapp.ui.avatar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.halloapp.R;

public class AvatarCropOverlay extends View {
    public enum Form {
        CIRCLE, SQUARE
    }

    private final float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.avatar_crop_overlay_stroke), getResources().getDisplayMetrics());
    private final float cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.avatar_crop_overlay_corner_radius), getResources().getDisplayMetrics());
    private final int overlayColor = getResources().getColor(R.color.avatar_crop_overlay);
    private final Path path = new Path();
    private final Paint paint = new Paint();
    private final RectF rect = new RectF();
    private Form form = Form.CIRCLE;


    public AvatarCropOverlay(Context context) {
        super(context);
        init();
    }

    public AvatarCropOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AvatarCropOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
    }

    public void setForm(Form form) {
        this.form = form;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        path.reset();
        path.setFillType(Path.FillType.INVERSE_EVEN_ODD);

        int width = getWidth();
        int height = getHeight();
        int smaller = Math.min(width, height);
        int x = width/2;
        int y = height/2;
        int radius = smaller/2;
        int insetRadius = Math.max(0, Math.round(radius - strokeWidth/2));

        switch (form) {
            case CIRCLE:
                // Draw overlay
                path.addCircle(x, y, insetRadius, Path.Direction.CW);
                canvas.clipPath(path);
                canvas.drawColor(overlayColor);

                // Draw border
                canvas.drawCircle(x, y, insetRadius, paint);

                break;
            case SQUARE:
                rect.set(x - insetRadius, y - insetRadius, x + insetRadius, y + insetRadius);

                // Draw overlay
                path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW);
                canvas.clipPath(path);
                canvas.drawColor(overlayColor);

                // Draw border
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);

                break;
        }
    }
}
