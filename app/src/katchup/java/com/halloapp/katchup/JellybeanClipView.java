package com.halloapp.katchup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.Constants;
import com.halloapp.R;

public class JellybeanClipView extends FrameLayout {

    private Paint paint = new Paint();
    private final Path path = new Path();
    private final Matrix matrix = new Matrix();

    private int lastWidth;
    private int lastHeight;

    private int rotateAngle = Constants.PROFILE_PHOTO_OVAL_DEG;
    private float outlineWidth = 0f;
    private float ovalRatio = Constants.PROFILE_PHOTO_OVAL_HEIGHT_RATIO;

    private float boxRatio;

    public JellybeanClipView(@NonNull Context context) {
        super(context);
        init(null, 0);
    }

    public JellybeanClipView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public JellybeanClipView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.UNSPECIFIED) {
            if (widthMode == MeasureSpec.EXACTLY) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Math.round(widthSize * boxRatio), MeasureSpec.EXACTLY));
            } else if (widthMode == MeasureSpec.AT_MOST) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Math.round(widthSize * boxRatio), MeasureSpec.AT_MOST));
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                int height = getMeasuredHeight();
                int width = getMeasuredWidth();
                if (height >= width * boxRatio) {
                    height = Math.round(width * boxRatio);
                } else {
                    width = Math.round(height / boxRatio);
                }
                super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            }
        } else if (heightMode == MeasureSpec.AT_MOST) {
            if (widthMode == MeasureSpec.UNSPECIFIED) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                int width = getMeasuredWidth();
                if (heightSize >= getMeasuredHeight() * boxRatio) {
                    setMeasuredDimension(width, Math.round(boxRatio * width));
                } else {
                    setMeasuredDimension(Math.round(heightSize / boxRatio), heightSize);
                }
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
            } else {
                if (heightSize >= widthSize * boxRatio) {
                    super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Math.round(boxRatio * widthSize), heightMode));
                } else {
                    super.onMeasure(MeasureSpec.makeMeasureSpec(Math.round(heightSize / boxRatio), widthMode), heightMeasureSpec);
                }
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private float computeRatio() {
        double radAngle = Math.toRadians(rotateAngle);
        double x = Math.sqrt(Math.pow(Math.cos(radAngle), 2) + (Math.pow(ovalRatio, 2) * Math.pow(Math.sin(radAngle), 2)));
        double y = Math.sqrt(Math.pow(Math.sin(radAngle), 2) + (Math.pow(ovalRatio, 2) * Math.pow(Math.cos(radAngle), 2)));

        return (float) (y / x);
    }

    private void init(@Nullable AttributeSet attrs, int defStyleAttr) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.JellybeanClipView, defStyleAttr, 0);
        rotateAngle = a.getInt(R.styleable.JellybeanClipView_jcvRotation, rotateAngle);
        outlineWidth = a.getDimension(R.styleable.JellybeanClipView_jcvOutlineWidth, outlineWidth);
        a.recycle();

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(outlineWidth);

        setWillNotDraw(false);

        boxRatio = computeRatio();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (lastWidth != w || lastHeight != h) {
            path.reset();
            matrix.reset();

            final float halfWidth = w / 2f;
            final float halfHeight = h / 2f;
            final float radius = Math.max(halfWidth - outlineWidth, halfHeight - ovalRatio * outlineWidth);

            path.addCircle(halfWidth, halfHeight, radius, Path.Direction.CCW);
            matrix.postScale(1, ovalRatio, halfWidth, halfHeight);
            matrix.postRotate(rotateAngle, halfWidth, halfHeight);
            path.transform(matrix);

            lastWidth = w;
            lastHeight = h;
        }
    }

    // Override dispatchDraw instead of onDraw so that we can draw the outline on top of the children
    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.clipPath(path);
        super.dispatchDraw(canvas);
        canvas.restore();
        if (outlineWidth > 0) {
            canvas.drawPath(path, paint);
        }
    }
}
