package com.halloapp.ui.mediaedit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Objects;

public class EditImageView extends androidx.appcompat.widget.AppCompatImageView {
    @FunctionalInterface
    public interface StateUpdateListener {
        void onUpdate(@NonNull State state);
    }

    private enum ReverseAction {
        ROTATE, FLIP, REMOVE_DRAWING
    }

    private enum CropRegionSection {
        TOP_LEFT, TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT, INSIDE, NONE
    }

    public static final float MAX_SCALE = 10.0f;
    public static final float MIN_SCALE = 1.0f;

    private final float threshold = getResources().getDimension(R.dimen.media_crop_region_threshold);
    private final float outThreshold = getResources().getDimension(R.dimen.media_crop_region_out_threshold);
    private final float borderThickness = getResources().getDimension(R.dimen.media_crop_region_border);
    private final float borderRadius = getResources().getDimension(R.dimen.media_crop_region_radius);
    private final float drawingWidth = getResources().getDimension(R.dimen.media_edit_drawing_width);

    private final RectF imageRect = new RectF();
    private final RectF cropRect = new RectF();
    private final RectF borderRect = new RectF();
    private float maxAspectRatio = 0;
    private final Path path = new Path();
    private final Paint shadowPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private float offsetX = 0.0f;
    private float offsetY = 0.0f;
    private float scale = 1.0f;
    private int rotationCount = 0;
    private boolean hFlipped = false;
    private boolean vFlipped = false;
    private boolean isDrawing = false;
    private int drawingColor = Color.RED;
    private final Paint drawingPaint = new Paint();
    private final ArrayList<PointF> currentDrawingPath = new ArrayList<>();
    private final ArrayDeque<DrawingPath> drawingPaths = new ArrayDeque<>();
    private final ArrayDeque<ReverseAction> reverseActionStack = new ArrayDeque<>();

    private StateUpdateListener stateUpdateListener;

    public EditImageView(Context context) {
        super(context);
        init();
    }

    public EditImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setScaleType(ScaleType.MATRIX);

        drawingPaint.setColor(drawingColor);
        drawingPaint.setStyle(Paint.Style.STROKE);
        drawingPaint.setStrokeWidth(drawingWidth);
        drawingPaint.setStrokeCap(Paint.Cap.ROUND);
        drawingPaint.setStrokeJoin(Paint.Join.ROUND);
        shadowPaint.setColor(getResources().getColor(R.color.black_70));
        shadowPaint.setStyle(Paint.Style.FILL);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderThickness);

        setOnTouchListener(new GestureListener());
    }

    public void setMaxAspectRatio(float maxAspectRatio) {
        this.maxAspectRatio = maxAspectRatio;
    }

    public void setAsyncImageFile(@Nullable File file, @Nullable State state, @Nullable Runnable onLoad) {
        setState(null);
        setImageDrawable(null);

        if (file == null) {
            return;
        }

        BgWorkers.getInstance().execute(() -> {
            Bitmap bitmap;
            try {
                bitmap = MediaUtils.decodeImage(getContext(), file);
            } catch (IOException e) {
                Log.e("EditImageView: unable to get bitmap", e);
                return;
            }

            post(() -> {
                setImageBitmap(bitmap);
                setState(state);

                if (onLoad != null) {
                    onLoad.run();
                }
            });
        });
    }

    public void setStateUpdateListener(StateUpdateListener listener) {
        stateUpdateListener = listener;
    }

    private void notifyStateUpdated() {
        if (stateUpdateListener != null) {
            stateUpdateListener.onUpdate(getState());
        }
    }

    public State getState() {
        if (getDrawable() == null) {
            return new State();
        }

        final State state = new State();
        state.scale = scale;
        state.hFlipped = hFlipped;
        state.vFlipped = vFlipped;
        state.rotationCount = rotationCount;

        final float imageWidth = rotationCount % 2 == 0 ? getDrawable().getIntrinsicWidth() : getDrawable().getIntrinsicHeight();
        final float baseScale =  imageRect.width() / imageWidth;

        state.offsetX = offsetX / baseScale / scale;
        state.offsetY = offsetY / baseScale / scale;

        final float cropCenterX = (cropRect.left + cropRect.right) / 2;
        final float cropCenterY = (cropRect.top + cropRect.bottom) / 2;
        final float imageCenterX = imageRect.centerX() + offsetX;
        final float imageCenterY = imageRect.centerY() + offsetY;

        state.cropWidth = Math.round(cropRect.width() / baseScale);
        state.cropHeight = Math.round(cropRect.height() / baseScale);
        state.cropOffsetX = Math.round((cropCenterX - imageCenterX) / baseScale);
        state.cropOffsetY = Math.round((cropCenterY - imageCenterY) / baseScale);

        for (DrawingPath path : drawingPaths) {
            ArrayList<PointF> points = new ArrayList<>(path.points.size());
            for (PointF point : path.points) {
                float x = (point.x - imageRect.left) / baseScale;
                float y = (point.y - imageRect.top) / baseScale;
                points.add(new PointF(x, y));
            }

            state.drawingPaths.add(new DrawingPath(points, path.color, path.width / baseScale));
        }

        state.reverseActionStack = new ArrayList<>(reverseActionStack);

        return state;
    }

    public void setState(@Nullable State state) {
        if (getDrawable() == null) {
            return;
        }

        if (state == null) {
            offsetX = 0.0f;
            offsetY = 0.0f;
            scale = 1.0f;
            rotationCount = 0;
            hFlipped = false;
            vFlipped = false;
            drawingPaths.clear();
        } else {
            scale = state.scale;
            hFlipped = state.hFlipped;
            vFlipped = state.vFlipped;
            rotationCount = state.rotationCount;
            offsetX = state.offsetX;
            offsetY = state.offsetY;
            reverseActionStack.clear();
            reverseActionStack.addAll(state.reverseActionStack);
        }

        computeImageRect();

        if (state == null) {
            computeInitialCropRegion();
        } else {
            float imageWidth = rotationCount % 2 == 0 ? getDrawable().getIntrinsicWidth() : getDrawable().getIntrinsicHeight();
            float baseScale =  imageRect.width() / imageWidth;

            offsetX = state.offsetX * baseScale * scale;
            offsetY = state.offsetY * baseScale * scale;

            float cropWidth = state.cropWidth * baseScale;
            float cropHeight = state.cropHeight * baseScale;
            float cropX = state.cropOffsetX * baseScale + imageRect.centerX() + offsetX - cropWidth / 2;
            float cropY = state.cropOffsetY * baseScale + imageRect.centerY() + offsetY - cropHeight / 2;

            cropRect.set(
                Math.max(cropX, imageRect.left),
                Math.max(cropY, imageRect.top),
                Math.min(cropX + cropWidth, imageRect.right),
                Math.min(cropY + cropHeight, imageRect.bottom));

            drawingPaths.clear();
            for (DrawingPath path : state.drawingPaths) {
                ArrayList<PointF> points = new ArrayList<>(path.points.size());

                for (PointF point : path.points) {
                    float x = point.x * baseScale + imageRect.left;
                    float y = point.y * baseScale + imageRect.top;
                    points.add(new PointF(x, y));
                }

                drawingPaths.add(new DrawingPath(points, path.color, path.width * baseScale));
            }
        }

        updateImage();
        requestLayout();
    }

    public void setDrawing(boolean drawing) {
        isDrawing = drawing;
        invalidate();
    }

    public boolean isDrawing() {
        return isDrawing;
    }

    public void setDrawingColor(int color) {
        drawingColor = color;
    }

    public boolean canUndo() {
        return reverseActionStack.size() > 0;
    }

    private void computeImageRect() {
        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        final float w = getWidth();
        final float h = getHeight();
        final float cx = w / 2;
        final float cy = h / 2;
        final float dw = (rotationCount % 2) == 0 ? getDrawable().getIntrinsicWidth() : getDrawable().getIntrinsicHeight();
        final float dh = (rotationCount % 2) == 0 ? getDrawable().getIntrinsicHeight() : getDrawable().getIntrinsicWidth();

        final float baseScale = Math.min((w - borderThickness * 2)  / dw, (h - borderThickness * 2) / dh);
        final float iw = dw * baseScale;
        final float ih = dh * baseScale;

        imageRect.set(cx - iw / 2, cy - ih / 2, cx + iw / 2, cy + ih / 2);
    }

    public void computeInitialCropRegion() {
        cropRect.set(imageRect);
        keepCropWithinMaxRatio();
    }

    private void updateImage() {
        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        final float w = imageRect.width();
        final float h = imageRect.height();
        final float dw = drawable.getIntrinsicWidth();
        final float dh = getDrawable().getIntrinsicHeight();
        final float baseScale = (rotationCount % 2) == 0 ? Math.min(w / dw, h / dh) : Math.min(w / dh, h / dw);
        final float x = imageRect.centerX() - (dw * baseScale * scale) / 2;
        final float y = imageRect.centerY() - (dh * baseScale * scale) / 2;

        Matrix m = new Matrix();
        m.postScale(baseScale * scale, baseScale * scale);
        m.postTranslate(x, y);

        m.postRotate(-90 * rotationCount, imageRect.centerX(), imageRect.centerY());
        m.postScale(vFlipped ? -1 : 1, hFlipped ? -1 : 1, imageRect.centerX(), imageRect.centerY());
        m.postTranslate(offsetX, offsetY);

        setImageMatrix(m);

        notifyStateUpdated();
    }

    public void reset() {
        setState(null);
    }

    public void zoom(float scale, float zoomCenterX, float zoomCenterY) {
        if (scale < MIN_SCALE || scale > MAX_SCALE) {
            return;
        }

        float scaleBy = scale / this.scale;
        float translationX = (zoomCenterX - imageRect.centerX()) * (1 - scaleBy);
        float translationY = (zoomCenterY - imageRect.centerY()) * (1 - scaleBy);
        offsetX += translationX;
        offsetY += translationY;

        final float centerX = imageRect.centerX();
        final float centerY = imageRect.centerY();
        final float hsw = imageRect.width() * scale / 2;
        final float hsh = imageRect.height() * scale / 2;

        final float l = centerX - hsw + offsetX;
        final float t = centerY - hsh + offsetY;
        final float r = centerX + hsw + offsetX;
        final float b = centerY + hsh + offsetY;

        if (l > imageRect.left) {
            offsetX = imageRect.left - centerX + hsw;
        } else if (r < imageRect.right) {
            offsetX = imageRect.right - centerX - hsw;
        }

        if (t > imageRect.top) {
            offsetY = imageRect.top - centerY + hsh;
        } else if (b < imageRect.bottom) {
            offsetY = imageRect.bottom - centerY - hsh;
        }

        this.scale = scale;
        updateImage();
    }

    public void zoomBy(float scaleBy, float zoomCenterX, float zoomCenterY) {
        zoom(scale * scaleBy, zoomCenterX, zoomCenterY);
    }

    public void move(float offsetX, float offsetY) {
        final float centerX = imageRect.centerX();
        final float centerY = imageRect.centerY();
        final float hsw = imageRect.width() * scale / 2;
        final float hsh = imageRect.height() * scale / 2;

        final float l = centerX - hsw + offsetX;
        final float t = centerY - hsh + offsetY;
        final float r = centerX + hsw + offsetX;
        final float b = centerY + hsh + offsetY;

        if (l > imageRect.left) {
            return;
        } else if (t > imageRect.top) {
            return;
        } else if (r < imageRect.right) {
            return;
        } else if (b < imageRect.bottom) {
            return;
        }

        this.offsetX = offsetX;
        this.offsetY = offsetY;

        updateImage();
    }

    public void moveBy(float deltaX, float deltaY) {
        move(offsetX + deltaX, offsetY + deltaY);
    }

    public void flip() {
        flip(true);
    }

    private void flip(boolean shouldAddToReverseStack) {
        vFlipped = !vFlipped;

        final float cw = cropRect.width();
        cropRect.left = getWidth() - cw - cropRect.left;
        cropRect.right = cropRect.left + cw;

        offsetX = -offsetX;

        for (DrawingPath path : drawingPaths) {
            path.flip(getWidth());
        }

        if (shouldAddToReverseStack) {
            reverseActionStack.add(ReverseAction.FLIP);
        }

        updateImage();
    }

    public void rotate() {
        rotate(true);
    }

    private void rotate(boolean shouldAddToReverseStack) {
        rotationCount = (rotationCount + 1) % 4;

        boolean tmp = vFlipped;
        vFlipped = hFlipped;
        hFlipped = tmp;

        RectF before = new RectF(imageRect);
        computeImageRect();

        final float scale = imageRect.width() / before.height();

        final float ox = offsetX;
        offsetX = offsetY * scale;
        offsetY = -ox * scale;

        final float x = cropRect.left - before.left;
        final float y = cropRect.top - before.top;
        final float l = imageRect.left + y * scale;
        final float b = imageRect.bottom - x * scale;

        cropRect.set(l, b - cropRect.width() * scale, l + cropRect.height() * scale, b);
        keepCropWithinMaxRatio();

        for (DrawingPath path : drawingPaths) {
            path.rotate(before, imageRect);
        }

        if (shouldAddToReverseStack) {
            reverseActionStack.add(ReverseAction.ROTATE);
        }

        updateImage();
    }

    public void undo() {
        if (reverseActionStack.size() > 0) {
            switch (reverseActionStack.removeLast()) {
                case FLIP:
                    flip(false);
                    break;
                case ROTATE:
                    rotate(false);
                    rotate(false);
                    rotate(false);
                    break;
                case REMOVE_DRAWING:
                    drawingPaths.removeLast();
                    break;
            }

            invalidate();
            notifyStateUpdated();
        }
    }

    private CropRegionSection getCropSectionAt(float x, float y) {
        float vThreshold = Math.min(threshold, cropRect.height() / 3);
        float hThreshold = Math.min(threshold, cropRect.width() / 3);

        boolean isTop = (cropRect.top - outThreshold < y) && (y < (cropRect.top + vThreshold));
        boolean isBottom = ((cropRect.bottom - vThreshold) < y) && (y < cropRect.bottom + outThreshold);
        boolean isLeft = (cropRect.left - outThreshold < x) && (x < (cropRect.left + hThreshold));
        boolean isRight = ((cropRect.right - hThreshold) < x) && (x < cropRect.right + outThreshold);
        boolean isInsideVertical = (cropRect.top + vThreshold) < y && y < (cropRect.bottom - vThreshold);
        boolean isInsideHorizontal = (cropRect.left + hThreshold) < x && x < (cropRect.right - hThreshold);

        if (isTop && isLeft) {
            return CropRegionSection.TOP_LEFT;
        } else if (isTop && isRight) {
            return CropRegionSection.TOP_RIGHT;
        } else if (isBottom && isRight) {
            return CropRegionSection.BOTTOM_RIGHT;
        } else if (isBottom && isLeft) {
            return CropRegionSection.BOTTOM_LEFT;
        } else if (isTop) {
            return CropRegionSection.TOP;
        } else if (isBottom) {
            return CropRegionSection.BOTTOM;
        } else if (isLeft) {
            return CropRegionSection.LEFT;
        } else if (isRight) {
            return CropRegionSection.RIGHT;
        } else if (isInsideHorizontal && isInsideVertical) {
            return CropRegionSection.INSIDE;
        }

        return CropRegionSection.NONE;
    }

    private RectF computeCropRegion(CropRegionSection section, float deltaX, float deltaY) {
        RectF computed = new RectF(cropRect);

        switch (section) {
            case TOP:
            case TOP_LEFT:
            case TOP_RIGHT:
                computed.top += deltaY;
                break;
            case BOTTOM:
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
                computed.bottom += deltaY;
                break;
        }

        switch (section) {
            case LEFT:
            case BOTTOM_LEFT:
            case TOP_LEFT:
                computed.left += deltaX;
                break;
            case RIGHT:
            case BOTTOM_RIGHT:
            case TOP_RIGHT:
                computed.right += deltaX;
                break;
        }

        if (section == CropRegionSection.INSIDE) {
            computed.left += deltaX;
            computed.right += deltaX;
            computed.top += deltaY;
            computed.bottom += deltaY;
        }

        return computed;
    }

    private void keepCropWithinMaxRatio() {
        final float w = cropRect.width();
        final float h = cropRect.height();
        if (maxAspectRatio > 0 && h / w > maxAspectRatio) {
            final float newTop = cropRect.top + (h - maxAspectRatio * w) / 2;
            final float newHeight = w * maxAspectRatio;
            cropRect.top = newTop;
            cropRect.bottom = newTop + newHeight;
        }
    }

    private void keepWithinMaxRatio(RectF crop, CropRegionSection section) {
        if (maxAspectRatio > 0 && (crop.height() / crop.width()) > maxAspectRatio) {
            switch (section) {
                case LEFT:
                case RIGHT: {
                    final float d = (crop.height() - maxAspectRatio * crop.width()) / 2;
                    crop.top += d;
                    crop.bottom -= d;
                    break;
                }
                default: {
                    final float d = ((crop.height() / maxAspectRatio) - crop.width()) / 2;
                    crop.left -= d;
                    crop.right += d;
                    break;
                }
            }
        }
    }

    private boolean isCropRegionTooSmall(RectF crop) {
        return crop.width() < threshold || crop.height() < threshold;
    }

    private boolean isCropRegionValid(RectF crop) {
        return imageRect.contains(crop) && !isCropRegionTooSmall(crop);
    }

    private void updateCropRegionBy(CropRegionSection section, float deltaX, float deltaY) {
        RectF computedX = computeCropRegion(section, deltaX, 0);
        keepWithinMaxRatio(computedX, section);

        if (isCropRegionValid(computedX)) {
            cropRect.set(computedX);
        }

        RectF computedY = computeCropRegion(section, 0, deltaY);
        keepWithinMaxRatio(computedY, section);

        if (isCropRegionValid(computedY)) {
            cropRect.set(computedY);
        }

        invalidate();
        notifyStateUpdated();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.clipRect(imageRect);
        super.onDraw(canvas);

        for (DrawingPath path : drawingPaths) {
            DrawingPath scaled = path.scale(scale, offsetX, offsetY, getWidth(), getHeight());
            drawPath(canvas, scaled.points, scaled.color, scaled.width);
        }

        if (isDrawing && currentDrawingPath.size() > 1) {
            drawPath(canvas, currentDrawingPath, drawingColor, drawingWidth);
        }

        canvas.restore();

        drawShadow(canvas);

        if (!isDrawing) {
            drawBorder(canvas);
        }
    }

    private void drawPath(Canvas canvas, @NonNull ArrayList<PointF> points, int color, float width) {
        ArrayList<BezierCurve> curves = curves(sampleDrawingPoints(points));
        PointF start = points.get(0);

        drawingPaint.setColor(color);
        drawingPaint.setStrokeWidth(width);

        path.reset();
        path.moveTo(start.x, start.y);

        for (BezierCurve curve : curves) {
            path.cubicTo(curve.control1.x, curve.control1.y, curve.control2.x, curve.control2.y, curve.end.x, curve.end.y);
        }

        canvas.drawPath(path, drawingPaint);
    }

    private void drawShadow(Canvas canvas) {
        path.reset();
        path.addRect(imageRect, Path.Direction.CW);
        path.addRoundRect(cropRect, borderRadius, borderRadius, Path.Direction.CW);
        path.setFillType(Path.FillType.EVEN_ODD);
        canvas.drawPath(path, shadowPaint);
    }

    private void drawBorder(Canvas canvas) {
        borderRect.set(cropRect);
        borderRect.inset(-borderThickness / 2 + 1, -borderThickness / 2 + 1);

        path.reset();
        path.addRoundRect(borderRect, borderRadius, borderRadius, Path.Direction.CW);
        path.setFillType(Path.FillType.EVEN_ODD);
        canvas.drawPath(path, borderPaint);
    }

    // Hermite splines
    // https://spin.atomicobject.com/2014/05/28/ios-interpolating-points/
    public static ArrayList<BezierCurve> curves(ArrayList<PointF> points) {
        ArrayList<BezierCurve> curves = new ArrayList<>();
        if (points.size() < 2) {
            return curves;
        }

        PointF previous = new PointF();
        PointF current = points.get(0);
        PointF next = points.get(1);
        PointF end;
        float mx, my;
        int nextIdx;

        for (int i = 0; i < points.size() - 1; i++) {
            end = next;

            if (i > 0) {
                mx = (next.x - current.x) * 0.5f + (current.x - previous.x) * 0.5f;
                my = (next.y - current.y) * 0.5f + (current.y - previous.y) * 0.5f;
            } else {
                mx = (next.x - current.x) * 0.5f;
                my = (next.y - current.y) * 0.5f;
            }

            PointF ctrlPt1 = new PointF(current.x + mx / 3.0f, current.y + my / 3.0f);

            previous = current;
            current = next;

            nextIdx = i + 2;
            if (nextIdx < points.size()) {
                next = points.get(nextIdx);

                mx = (next.x - current.x) * 0.5f + (current.x - previous.x) * 0.5f;
                my = (next.y - current.y) * 0.5f + (current.y - previous.y) * 0.5f;
            } else {
                mx = (current.x - previous.x) * 0.5f;
                my = (current.y - previous.y) * 0.5f;
            }

            PointF ctrlPt2 = new PointF(current.x - mx / 3.0f, current.y - my / 3.0f);

            curves.add(new BezierCurve(ctrlPt1, ctrlPt2, end));

            if (nextIdx >= points.size()) {
                break;
            }
        }

        return curves;
    }

    public static ArrayList<PointF> sampleDrawingPoints(@NonNull ArrayList<PointF> points) {
        ArrayList<PointF> sampled = new ArrayList<>();

        if (points.size() < 2) {
            return sampled;
        }

        final float MIN_SQ_DISTANCE = 24 * 24;
        sampled.add(points.get(0));

        PointF current;
        for (PointF p : points) {
            current = sampled.get(sampled.size() - 1);

            float distance = (current.x - p.x) * (current.x - p.x) + (current.y - p.y) * (current.y - p.y);
            if (distance > MIN_SQ_DISTANCE) {
                sampled.add(p);
            }
        }

        PointF last = points.get(points.size() - 1);
        if (!last.equals(sampled.get(sampled.size() - 1))) {
            sampled.add(last);
        }

        return sampled;
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener implements OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {
        private final ScaleGestureDetector zoomDetector = new ScaleGestureDetector(getContext(), this);
        private final GestureDetector dragDetector = new GestureDetector(getContext(), this);
        private boolean isStarted = false;
        private boolean isMultiTouch = false;
        private CropRegionSection section = CropRegionSection.NONE;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            final int action = motionEvent.getAction() & MotionEvent.ACTION_MASK;

            switch (action) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (!isStarted) {
                        isMultiTouch = true;
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    if (!isStarted && motionEvent.getPointerCount() == 2) {
                        isMultiTouch = false;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isMultiTouch && motionEvent.getPointerCount() < 2) {
                        return true;
                    }
                    break;
            }

            zoomDetector.onTouchEvent(motionEvent);
            dragDetector.onTouchEvent(motionEvent);

            if (action == MotionEvent.ACTION_UP) {
                isStarted = false;
                isMultiTouch = false;
                section = CropRegionSection.NONE;

                if (isDrawing) {
                    if (currentDrawingPath.size() > 1) {
                        DrawingPath scaled = new DrawingPath(currentDrawingPath, drawingColor, drawingWidth);
                        DrawingPath path = scaled.scale(1 / scale, offsetX, offsetY, getWidth(), getHeight());
                        drawingPaths.add(path);

                        reverseActionStack.add(ReverseAction.REMOVE_DRAWING);
                    }

                    currentDrawingPath.clear();
                    invalidate();
                    notifyStateUpdated();
                }
            }

            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            if (isStarted && !isMultiTouch) {
                return false;
            }

            isStarted = true;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            zoomBy(scaleGestureDetector.getScaleFactor(), scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!isStarted) {
                isStarted = true;

                if (!isMultiTouch) {
                    section = getCropSectionAt(e1.getX(), e1.getY());
                }
            }

            if (isMultiTouch) {
                moveBy(-distanceX, -distanceY);
            } else if (isDrawing) {
                currentDrawingPath.add(new PointF(e2.getX(), e2.getY()));
                invalidate();
            } else {
                RectF valid = new RectF(borderRect);
                valid.inset(-2 * outThreshold, -2 * outThreshold);

                if (valid.contains(e2.getX(), e2.getY())) {
                    updateCropRegionBy(section, -distanceX, -distanceY);
                }
            }

            return true;
        }
    }

    public static class BezierCurve {
        public PointF control1, control2, end;

        public BezierCurve(PointF control1, PointF control2, PointF end) {
            this.control1 = new PointF(control1.x, control1.y);
            this.control2 = new PointF(control2.x, control2.y);
            this.end = new PointF(end.x, end.y);
        }
    }

    public static class DrawingPath implements Parcelable {
        public ArrayList<PointF> points = new ArrayList<>();
        public int color;
        public float width;

        public DrawingPath(@NonNull ArrayList<PointF> points, int color, float width) {
            this.points = new ArrayList<>(points);
            this.color = color;
            this.width = width;
        }

        public DrawingPath scale(float scale, float offsetX, float offsetY, float sizeWidth, float sizeHeight) {
            ArrayList<PointF> scaledPoints = new ArrayList<>(points.size());

            for (PointF point : points) {
                float x, y;
                if (scale >= 1) {
                    x = point.x * scale - (scale - 1) * sizeWidth / 2 + offsetX;
                    y = point.y * scale - (scale - 1) * sizeHeight / 2 + offsetY;
                } else {
                    x = (point.x + (1 / scale - 1) * sizeWidth / 2 - offsetX) * scale;
                    y = (point.y + (1 / scale - 1) * sizeHeight / 2 - offsetY) * scale;
                }

                scaledPoints.add(new PointF(x, y));
            }

            return new DrawingPath(scaledPoints, color, width * scale);
        }

        public void flip(int sizeWidth) {
            for (PointF point : points) {
                point.x = sizeWidth - point.x;
            }
        }

        public void rotate(RectF imageRectBefore, RectF imageRectAfter) {
            float scale = imageRectAfter.width() / imageRectBefore.height();

            for (PointF point : points) {
                float x = point.x - imageRectBefore.left;
                float y = point.y - imageRectBefore.top;
                point.x = imageRectAfter.left + scale * y;
                point.y = imageRectAfter.bottom - scale * x;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DrawingPath path = (DrawingPath) o;
            return color == path.color && Float.compare(path.width, width) == 0 && Objects.equals(points, path.points);
        }

        @Override
        public int hashCode() {
            return Objects.hash(points, color, width);
        }

        private DrawingPath(Parcel in) {
            color = in.readInt();
            width = in.readFloat();
            in.readTypedList(points, PointF.CREATOR);
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeInt(color);
            parcel.writeFloat(width);
            parcel.writeTypedList(points);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Parcelable.Creator<DrawingPath> CREATOR = new Parcelable.Creator<DrawingPath>() {
            public DrawingPath createFromParcel(Parcel in) {
                return new DrawingPath(in);
            }

            public DrawingPath[] newArray(int size) {
                return new DrawingPath[size];
            }
        };
    }

    public static class State implements Parcelable {
        public float offsetX;
        public float offsetY;
        public float scale;
        public int rotationCount;
        public boolean hFlipped;
        public boolean vFlipped;
        public int cropWidth;
        public int cropHeight;
        public int cropOffsetX;
        public int cropOffsetY;
        public ArrayList<DrawingPath> drawingPaths = new ArrayList<>();
        public ArrayList<ReverseAction> reverseActionStack = new ArrayList<>();

        @Override
        public int describeContents() {
            return 0;
        }

        public State() {}

        private State(Parcel in) {
            rotationCount = in.readInt();
            hFlipped = in.readInt() > 0; // Unable to use in.readBoolean due to API version
            vFlipped = in.readInt() > 0;
            scale = in.readFloat();
            offsetX = in.readFloat();
            offsetY = in.readFloat();
            cropWidth = in.readInt();
            cropHeight = in.readInt();
            cropOffsetX = in.readInt();
            cropOffsetY = in.readInt();
            in.readTypedList(drawingPaths, DrawingPath.CREATOR);

            ArrayList<String> undoStackStrings = new ArrayList<>();
            in.readStringList(undoStackStrings);
            for (String name : undoStackStrings) {
                reverseActionStack.add(ReverseAction.valueOf(name));
            }
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeInt(rotationCount);
            parcel.writeInt(hFlipped ? 1 : 0); // Unable to use parcel.writeBoolean due to API version
            parcel.writeInt(vFlipped ? 1 : 0);
            parcel.writeFloat(scale);
            parcel.writeFloat(offsetX);
            parcel.writeFloat(offsetY);
            parcel.writeInt(cropWidth);
            parcel.writeInt(cropHeight);
            parcel.writeInt(cropOffsetX);
            parcel.writeInt(cropOffsetY);
            parcel.writeTypedList(drawingPaths);

            ArrayList<String> undoStackStrings = new ArrayList<>(reverseActionStack.size());
            for (ReverseAction reverseAction : reverseActionStack) {
                undoStackStrings.add(reverseAction.name());
            }
            parcel.writeStringList(undoStackStrings);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (super.equals(obj)) {
                return true;
            }

            if (!(obj instanceof State)) {
                return false;
            }

            State state = (State) obj;

            return state.offsetX == offsetX && state.offsetY == offsetY && Objects.equals(state.reverseActionStack, reverseActionStack)
                    && state.scale == scale && state.rotationCount == rotationCount && state.hFlipped == hFlipped
                    && state.vFlipped == vFlipped && state.cropWidth == cropWidth && state.cropHeight == cropHeight
                    && state.cropOffsetX == cropOffsetX && state.cropOffsetY == cropOffsetY && Objects.equals(state.drawingPaths, drawingPaths);
        }

        public static final Parcelable.Creator<State> CREATOR = new Parcelable.Creator<State>() {
            public State createFromParcel(Parcel in) {
                return new State(in);
            }

            public State[] newArray(int size) {
                return new State[size];
            }
        };
    }
}
