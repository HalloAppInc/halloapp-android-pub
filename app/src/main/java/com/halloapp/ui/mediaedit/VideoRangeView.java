package com.halloapp.ui.mediaedit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.halloapp.R;

public class VideoRangeView extends View {
    @FunctionalInterface
    public interface RangeChangedListener {
        /**
         * @param start range start will be between 0 - 1
         * @param end range end will be between 0 - 1 and bigger than range start
         * @param region is START, END or NONE and is also called on completion
         */
        void onRangeChanged(float start, float end, DragRegion region);
    }

    public enum DragRegion {
        START, END, NONE
    }

    private final float threshold = getResources().getDimension(R.dimen.video_edit_range_threshold);
    private final float borderThickness = getResources().getDimension(R.dimen.video_edit_range_border);
    private final float handleRadius = getResources().getDimension(R.dimen.video_edit_range_handle_radius);

    private final Path path = new Path();
    private final Paint shadowPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final Paint handlePaint = new Paint();

    private DragRegion dragRegion = DragRegion.NONE;
    private float start = 0;
    private float end = 1;
    private RangeChangedListener listener;

    public VideoRangeView(Context context) {
        super(context);
        init();
    }

    public VideoRangeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoRangeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        shadowPaint.setColor(getResources().getColor(R.color.black_70));
        shadowPaint.setStyle(Paint.Style.FILL);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderThickness);
        handlePaint.setColor(Color.WHITE);
        handlePaint.setStyle(Paint.Style.FILL);

        initDragHandling();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initDragHandling() {
        setOnTouchListener((View view, MotionEvent event) -> {
            float width = getWidth();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    float startDistance = Math.abs(width * start - event.getX());
                    float endDistance = Math.abs(width * end - event.getX());

                    if (startDistance < threshold && endDistance > threshold) {
                        dragRegion = DragRegion.START;
                    } else if (startDistance > threshold && endDistance < threshold) {
                        dragRegion = DragRegion.END;
                    } else if (startDistance < threshold && endDistance < threshold) {
                        if (startDistance < endDistance) {
                            dragRegion = DragRegion.START;
                        } else {
                            dragRegion = DragRegion.END;
                        }
                    }

                    break;
                case MotionEvent.ACTION_MOVE:
                    float minimumInterval = 2 * (borderThickness + handleRadius) / width;

                    switch (dragRegion) {
                        case START:
                            start = Math.max(0, Math.min(event.getX() / width, end - minimumInterval));
                            invalidate();
                            notifyRangeChanged();
                            break;
                        case END:
                            end = Math.min(1, Math.max(event.getX() / width, start + minimumInterval));
                            invalidate();
                            notifyRangeChanged();
                            break;
                    }

                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    dragRegion = DragRegion.NONE;
                    notifyRangeChanged();
                    break;
            }
            return true;
        });
    }

    /**
     * @param start range start should be between 0 - 1
     * @param end range end should be between 0 - 1 and bigger than range start
     */
    public void setRange(float start, float end) {
        if (start <= end) {
            this.start = Math.max(0, start);
            this.end = Math.min(1, end);
            invalidate();
        }
    }

    public void setRangeChangedListener(RangeChangedListener listener) {
        this.listener = listener;
    }

    public void notifyRangeChanged() {
        if (listener != null) {
            listener.onRangeChanged(start, end, dragRegion);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawShadow(canvas);
        drawBorder(canvas);
    }

    private void drawShadow(Canvas canvas) {
        int offsetHorizontal = (int) (borderThickness / 2 + handleRadius);
        int left = (int) (getWidth() * start) + offsetHorizontal;
        int right = (int) (getWidth() * end) - offsetHorizontal;

        path.reset();
        path.addRect(0, 0, getWidth(), getHeight(), Path.Direction.CW);
        path.addRect(left, 0, right, getHeight(), Path.Direction.CCW);
        path.setFillType(Path.FillType.WINDING);
        canvas.drawPath(path, shadowPaint);
    }

    private void drawBorder(Canvas canvas) {
        int offsetHorizontal = (int) (borderThickness / 2 + handleRadius);
        int offsetVertical = (int) (borderThickness / 2);
        int left = (int) (getWidth() * start) + offsetHorizontal;
        int right = (int) (getWidth() * end) - offsetHorizontal;

        path.reset();
        path.addRect(left, offsetVertical, right, getHeight() - offsetVertical, Path.Direction.CW);
        path.setFillType(Path.FillType.WINDING);
        canvas.drawPath(path, borderPaint);

        canvas.drawCircle(left, (float)getHeight() / 2, handleRadius, handlePaint);
        canvas.drawCircle(right, (float)getHeight() / 2, handleRadius, handlePaint);
    }
}
