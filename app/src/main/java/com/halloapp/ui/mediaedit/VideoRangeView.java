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
         */
        void onRangeChanged(float start, float end);
    }

    private enum DragRegion {
        START, END, NONE
    }

    private final float threshold = getResources().getDimension(R.dimen.video_edit_range_threshold);
    private final float borderThickness = getResources().getDimension(R.dimen.video_edit_range_border);
    private final float handleRadius = getResources().getDimension(R.dimen.video_edit_range_handle_radius);

    private final Path path = new Path();
    private final Paint shadowPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final Paint handlePaint = new Paint();

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

    private void initDragHandling() {
        setOnTouchListener(new OnTouchListener() {
            DragRegion dragRegion = DragRegion.NONE;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent event) {
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
                        switch (dragRegion) {
                            case START:
                                start = Math.max(0, Math.min(event.getX() / width, end));
                                invalidate();
                                notifyRangeChanged();
                                break;
                            case END:
                                end = Math.min(1, Math.max(event.getX() / width, start));
                                invalidate();
                                notifyRangeChanged();
                                break;
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        dragRegion = DragRegion.NONE;
                        break;
                }
                return true;
            }
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
            listener.onRangeChanged(start, end);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawShadow(canvas);
        drawBorder(canvas);
    }

    private void drawShadow(Canvas canvas) {
        int left = (int) (getWidth() * start);
        int right = (int) (getWidth() * end);

        path.reset();
        path.addRect(0, 0, getWidth(), getHeight(), Path.Direction.CW);
        path.addRect(left, 0, right, getHeight(), Path.Direction.CCW);
        path.setFillType(Path.FillType.WINDING);
        canvas.drawPath(path, shadowPaint);
    }

    private void drawBorder(Canvas canvas) {
        int offset = (int) (borderThickness / 2);
        int left = (int) (getWidth() * start) + offset;
        int right = (int) (getWidth() * end) - offset;

        if (Math.abs(left - right) < borderThickness) {
            left -= borderThickness / 2;
            right += borderThickness / 2;
        }

        path.reset();
        path.addRect(left, offset, right, getHeight() - offset, Path.Direction.CW);
        path.setFillType(Path.FillType.WINDING);
        canvas.drawPath(path, borderPaint);

        canvas.drawCircle(left, (float)getHeight() / 2, handleRadius, handlePaint);
        canvas.drawCircle(right, (float)getHeight() / 2, handleRadius, handlePaint);
    }
}
