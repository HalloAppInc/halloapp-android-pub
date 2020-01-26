package com.halloapp.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.halloapp.R;

public class PostImageView extends androidx.appcompat.widget.AppCompatImageView {

    private float maxAspectRatio = 1.25f;

    private int cornerColor;
    private float cornerRadius;
    private Path cornerPath = new Path();

    public PostImageView(Context context) {
        super(context);
        init(null, 0);
    }

    public PostImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PostImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PostImageView, defStyle, 0);

        cornerColor = a.getColor(R.styleable.PostImageView_pivCornerColor, 0);
        cornerRadius = a.getDimension(R.styleable.PostImageView_pivCornerRadius, 0);
        maxAspectRatio = a.getDimension(R.styleable.PostImageView_pivMaxAspectRatio, maxAspectRatio);

        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        Drawable drawable = getDrawable();
        if (drawable != null) {
            int height = (int) (width * Math.min(maxAspectRatio, 1f * drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth()));
            setMeasuredDimension(width, height);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (cornerRadius != 0 && cornerColor != 0) {
            // TODO (ds): think about doing it with Outline, like CardView does, there seems to be some issues with ViewPager during scroll
            cornerPath.reset();
            cornerPath.addRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius, Path.Direction.CW);
            cornerPath.setFillType(Path.FillType.INVERSE_EVEN_ODD);
            canvas.clipPath(cornerPath);
            canvas.drawColor(cornerColor);
        }
    }
}
