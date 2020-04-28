package com.halloapp.ui.avatar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.halloapp.R;
import com.halloapp.util.Log;

public class AvatarCropOverlay extends View {
    private Path path = new Path();
    private Paint paint = new Paint();

    public AvatarCropOverlay(Context context) {
        super(context);
    }

    public AvatarCropOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AvatarCropOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        path.reset();

        int width = getWidth();
        int height = getHeight();
        int smaller = Math.min(width, height);
        int x = width/2;
        int y = height/2;
        int radius = smaller/2;

        Resources resources = getResources();
        float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, resources.getDimension(R.dimen.avatar_crop_overlay_stroke), resources.getDisplayMetrics());
        int insetRadius = Math.max(0, Math.round(radius - strokeWidth/2));

        path.addCircle(x, y, insetRadius, Path.Direction.CW);
        path.setFillType(Path.FillType.INVERSE_EVEN_ODD);
        canvas.clipPath(path);
        canvas.drawColor(resources.getColor(R.color.avatar_crop_overlay));

        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(x, y, insetRadius, paint);
    }
}
