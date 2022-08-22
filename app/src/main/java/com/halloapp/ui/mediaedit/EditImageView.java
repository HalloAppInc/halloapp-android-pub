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
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.ShapeAppearancePathProvider;
import com.halloapp.R;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Objects;

public class EditImageView extends AppCompatImageView {
    @FunctionalInterface
    public interface StateUpdateListener {
        void onUpdate(@NonNull State state);
    }

    public interface AnnotationListener {
        void onTap(@NonNull Annotation annotation);
        void onDrag(@NonNull Annotation annotation, float x, float y);
        boolean onDragEnd(@NonNull Annotation annotation, float x, float y);
    }

    private enum CropRegionSection {
        TOP_LEFT, TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT, INSIDE, NONE
    }

    private final ShapeAppearancePathProvider pathProvider = new ShapeAppearancePathProvider();
    private ShapeAppearanceModel shapeAppearanceModel;

    public static final float MAX_SCALE = 10.0f;
    public static final float MIN_SCALE = 1.0f;

    private final float threshold = getResources().getDimension(R.dimen.media_crop_region_threshold);
    private final float outThreshold = getResources().getDimension(R.dimen.media_crop_region_out_threshold);
    private final float borderThickness = getResources().getDimension(R.dimen.media_crop_region_border);
    private final float borderGridThickness = getResources().getDimension(R.dimen.media_crop_region_grid);
    private final float borderRadius = getResources().getDimension(R.dimen.media_crop_region_radius);
    private final float borderHandleSize = getResources().getDimension(R.dimen.media_crop_region_handle);
    private final float drawingWidth = getResources().getDimension(R.dimen.media_edit_drawing_width);
    private final float annotationTextSize = getResources().getDimension(R.dimen.media_edit_annotation_text_size);
    private final float handleThickness = getResources().getDimension(R.dimen.media_crop_handle_stroke_width);

    private @MediaEditActivity.EditPurpose int editPurpose = MediaEditActivity.EDIT_PURPOSE_CROP;
    private final RectF imageRect = new RectF();
    private final RectF cropRect = new RectF();
    private final RectF borderRect = new RectF();
    private float maxAspectRatio = 0;
    private final Path path = new Path();
    private final Path outlinePath = new Path();
    private final Path maskPath = new Path();
    private final Paint shadowPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final Paint handlePaint = new Paint();
    private final Paint clearPaint = new Paint();
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
    private final ArrayList<Layer> layers = new ArrayList<>();
    private final ArrayDeque<ReverseAction> reverseActionStack = new ArrayDeque<>();
    private final GestureListener gestureListener = new GestureListener();

    private StateUpdateListener stateUpdateListener;
    private AnnotationListener annotationListener;

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
        borderPaint.setColor(getResources().getColor(R.color.image_edit_border));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderThickness);
        handlePaint.setColor(getResources().getColor(R.color.image_edit_border));
        handlePaint.setStyle(Paint.Style.STROKE);
        handlePaint.setStrokeWidth(handleThickness);
        clearPaint.setAntiAlias(true);
        clearPaint.setColor(ContextCompat.getColor(getContext(), R.color.window_background));
        clearPaint.setStyle(Paint.Style.FILL);

        shapeAppearanceModel = ShapeAppearanceModel.builder().setAllCorners(CornerFamily.ROUNDED, borderRadius).build();

        setOnTouchListener(gestureListener);
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
                invalidate();

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

    public void setAnnotationListener(AnnotationListener listener) {
        annotationListener = listener;
    }

    private void notifyAnnotationTapped(@NonNull Annotation annotation) {
        if (annotationListener != null) {
            annotationListener.onTap(annotation);
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

        for (Layer layer : layers) {
            state.layers.add(layer.fromViewToImage(imageRect, baseScale));
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
            layers.clear();
            reverseActionStack.clear();
        } else {
            scale = state.scale;
            hFlipped = state.hFlipped;
            vFlipped = state.vFlipped;
            rotationCount = state.rotationCount;
            offsetX = state.offsetX;
            offsetY = state.offsetY;
            layers.clear();
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

            for (Layer layer : state.layers) {
                layers.add(layer.fromImageToView(imageRect, baseScale));
            }
        }

        updateImage();
        requestLayout();
        updateShapeMask(getWidth(), getHeight());
    }

    public void setDrawing(boolean drawing) {
        isDrawing = drawing;
        invalidate();
    }

    public void setEditPurpose(@MediaEditActivity.EditPurpose int editPurpose) {
        this.editPurpose = editPurpose;
    }

    public boolean isDrawing() {
        return isDrawing;
    }

    public void setDrawingColor(int color) {
        drawingColor = color;
    }

    public void addAnnotation(String text) {
        Annotation annotation = new Annotation(text, annotationTextSize, drawingColor, new PointF(imageRect.centerX(), imageRect.centerY()), 0);
        annotation.scale(1 / scale, offsetX, offsetY, getWidth(), getHeight());
        layers.add(annotation);
        reverseActionStack.add(ReverseAction.remove());

        invalidate();
        notifyStateUpdated();
    }

    public void updateAnnotation(Annotation annotation, String text) {
        int idx = layers.indexOf(annotation);

        if (idx >= 0) {
            reverseActionStack.add(ReverseAction.restore(idx, annotation.copy()));
            annotation.setText(text);
            annotation.setColor(drawingColor);

            invalidate();
            notifyStateUpdated();
        }
    }

    public void removeAnnotation(Annotation annotation) {
        int idx = layers.indexOf(annotation);
        layers.remove(annotation);
        reverseActionStack.add(ReverseAction.insert(idx, annotation));

        invalidate();
        notifyStateUpdated();
    }

    private void addDrawingPath() {
        if (currentDrawingPath.size() > 1) {
            DrawingPath path = new DrawingPath(currentDrawingPath, drawingColor, drawingWidth);
            path.scale(1 / scale, offsetX, offsetY, getWidth(), getHeight());
            layers.add(path);
            reverseActionStack.add(ReverseAction.remove());
        }

        currentDrawingPath.clear();
        invalidate();
        notifyStateUpdated();
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

        for (Layer layer : layers) {
            layer.flip(getWidth());
        }

        if (shouldAddToReverseStack) {
            reverseActionStack.add(ReverseAction.flip());
        }

        updateImage();
        updateShapeMask(getWidth(), getHeight());
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

        for (Layer layer: layers) {
            layer.rotate(before, imageRect);
        }

        if (shouldAddToReverseStack) {
            reverseActionStack.add(ReverseAction.rotate());
        }

        updateImage();
        updateShapeMask(getWidth(), getHeight());
    }

    public void undo() {
        if (reverseActionStack.size() > 0) {
            ReverseAction action = reverseActionStack.removeLast();

            switch (action.type) {
                case FLIP:
                    flip(false);
                    break;
                case ROTATE:
                    rotate(false);
                    rotate(false);
                    rotate(false);
                    break;
                case REMOVE:
                    layers.remove(layers.size() - 1);
                    break;
                case RESTORE:
                    layers.set(action.index, action.layer);
                    break;
                case INSERT:
                    layers.add(action.index, action.layer);
                    break;
            }

            invalidate();
            notifyStateUpdated();
        }
    }

    @Nullable
    private Annotation getAnnotationAt(float x, float y) {
        for (Layer layer : layers) {
            if (layer instanceof Annotation) {
                Annotation annotation = (Annotation) layer;
                Annotation scaled = (Annotation) annotation.copy();
                scaled.scale(scale, offsetX, offsetY, getWidth(), getHeight());

                float minX = scaled.center.x - (float) scaled.layout.getWidth() / 2;
                float maxX = scaled.center.x + (float) scaled.layout.getWidth() / 2;
                float minY = scaled.center.y - (float) scaled.layout.getHeight() / 2;
                float maxY = scaled.center.y + (float) scaled.layout.getHeight() / 2;

                if (minX <= x && x <= maxX && minY <= y && y <= maxY) {
                    return annotation;
                }
            }
        }

        return null;
    }

    private void moveBy(Annotation annotation, float x, float y) {
        annotation.scale(scale, offsetX, offsetY, getWidth(), getHeight());
        annotation.center.x += x;
        annotation.center.y += y;
        annotation.scale(1 / scale, offsetX, offsetY, getWidth(), getHeight());

        invalidate();
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
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        updateShapeMask(width, height);
    }

    private void updateShapeMask(int width, int height) {
        pathProvider.calculatePath(shapeAppearanceModel, 1f , imageRect, outlinePath);
        maskPath.rewind();
        maskPath.addPath(outlinePath);
        final RectF maskRect = new RectF(0, 0, width, height);
        maskPath.addRect(maskRect, Path.Direction.CCW);
        maskPath.setFillType(Path.FillType.EVEN_ODD);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.clipRect(imageRect);
        super.onDraw(canvas);

        canvas.drawPath(maskPath, clearPaint);

        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.scale(scale, scale, (float) getWidth() / 2, (float) getHeight() / 2);

        for (Layer layer : layers) {
            layer.draw(canvas);
        }

        canvas.restore();

        if (isDrawing && currentDrawingPath.size() > 1) {
            drawPath(canvas, currentDrawingPath, drawingColor, drawingWidth);
        }

        canvas.restore();

        drawShadow(canvas);

        if (editPurpose == MediaEditActivity.EDIT_PURPOSE_CROP) {
            borderRect.set(cropRect);
            borderRect.inset(-borderThickness / 2 + 1, -borderThickness / 2 + 1);
            drawBorder(canvas);

            final float insetDiff = handleThickness - borderThickness;
            borderRect.inset(-insetDiff / 2 + 1, -insetDiff / 2 + 1);
            drawHandles(canvas);

            if (gestureListener.isDraggingCropRegion()) {
                drawGrid(canvas);
            }
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
        path.addPath(outlinePath);
        path.addRoundRect(cropRect, borderRadius, borderRadius, Path.Direction.CW);
        path.setFillType(Path.FillType.EVEN_ODD);
        canvas.drawPath(path, shadowPaint);
    }

    private void drawBorder(Canvas canvas) {
        path.reset();
        path.addRoundRect(borderRect, borderRadius, borderRadius, Path.Direction.CW);
        path.setFillType(Path.FillType.EVEN_ODD);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderThickness);
        canvas.drawPath(path, borderPaint);
    }

    private void drawGrid(Canvas canvas) {
        path.reset();

        path.moveTo(borderRect.left, borderRect.top + borderRect.height() / 3);
        path.lineTo(borderRect.right, borderRect.top + borderRect.height() / 3);

        path.moveTo(borderRect.left, borderRect.top + borderRect.height() * 2 / 3);
        path.lineTo(borderRect.right, borderRect.top + borderRect.height() * 2 / 3);

        path.moveTo(borderRect.left + borderRect.width() / 3, borderRect.top);
        path.lineTo(borderRect.left + borderRect.width() / 3, borderRect.bottom);

        path.moveTo(borderRect.left + borderRect.width() * 2 / 3, borderRect.top);
        path.lineTo(borderRect.left + borderRect.width() * 2 / 3, borderRect.bottom);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderGridThickness);
        canvas.drawPath(path, borderPaint);
    }

    private void drawHandles(Canvas canvas) {
        final float arcBound = 2 * borderRadius;
        final float sweepAngle = 90;

        path.reset();
        path.addArc(borderRect.left, borderRect.top, borderRect.left + arcBound, borderRect.top + arcBound, 180, sweepAngle);
        path.addArc(borderRect.right - arcBound, borderRect.top, borderRect.right, borderRect.top + arcBound, 270, sweepAngle);
        path.addArc(borderRect.right - arcBound, borderRect.bottom - arcBound, borderRect.right, borderRect.bottom, 0, sweepAngle);
        path.addArc(borderRect.left, borderRect.bottom - arcBound, borderRect.left + arcBound, borderRect.bottom, 90, sweepAngle);

        handlePaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, handlePaint);
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
        private Annotation annotation;
        private Annotation originalAnnotation;
        private MotionEvent recentEvent;
        private float recentAngle;

        public boolean isDraggingCropRegion() {
            return isStarted && section != CropRegionSection.NONE && annotation == null;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            recentEvent = motionEvent;
            final int action = motionEvent.getAction() & MotionEvent.ACTION_MASK;

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    break;
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
                if (editPurpose == MediaEditActivity.EDIT_PURPOSE_ANNOTATE) {
                    handleAnnotationOnEnd(motionEvent);
                }

                isStarted = false;
                isMultiTouch = false;
                section = CropRegionSection.NONE;
                recentEvent = null;
                annotation = null;

                if (isDrawing) {
                    addDrawingPath();
                }
            }

            return true;
        }

        private void handleAnnotationOnEnd(MotionEvent event) {
            if (annotation != null && annotation.center != null && !annotation.equals(originalAnnotation)) {
                if (!annotation.center.equals(originalAnnotation.center)) {
                    if (annotationListener!= null && annotationListener.onDragEnd(annotation, event.getX(), event.getY())) {
                        int idx = layers.indexOf(annotation);
                        layers.remove(annotation);
                        reverseActionStack.add(ReverseAction.insert(idx, originalAnnotation));
                    }
                }

                int idx = layers.indexOf(annotation);
                if (!annotation.equals(originalAnnotation) && idx >= 0) {
                    reverseActionStack.add(ReverseAction.restore(idx, originalAnnotation));
                }
            }
        }

        private float getRecentAngle(ScaleGestureDetector scaleGestureDetector) {
            float angleX = recentEvent.getX() - scaleGestureDetector.getFocusX();
            float angleY = recentEvent.getY() - scaleGestureDetector.getFocusY();
            return (float) Math.toDegrees(Math.atan2(angleY, angleX));
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            if (isStarted && !isMultiTouch) {
                return false;
            }

            isStarted = true;
            if (editPurpose == MediaEditActivity.EDIT_PURPOSE_ANNOTATE) {
                annotation = getAnnotationAt(recentEvent.getX(), recentEvent.getY());
                originalAnnotation = annotation != null ? (Annotation) annotation.copy() : null;
            }
            recentAngle = getRecentAngle(scaleGestureDetector);

            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            if (editPurpose == MediaEditActivity.EDIT_PURPOSE_CROP) {
                zoomBy(scaleGestureDetector.getScaleFactor(), scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
            } else if (editPurpose == MediaEditActivity.EDIT_PURPOSE_ANNOTATE && annotation != null) {
                annotation.zoomBy(scaleGestureDetector.getScaleFactor());

                float angle = getRecentAngle(scaleGestureDetector);
                annotation.rotation -= recentAngle - angle;
                recentAngle = angle;

                invalidate();
            }

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
                    switch (editPurpose) {
                        case MediaEditActivity.EDIT_PURPOSE_CROP:
                            section = getCropSectionAt(e1.getX(), e1.getY());
                            break;

                        case MediaEditActivity.EDIT_PURPOSE_ANNOTATE:
                            annotation = getAnnotationAt(e1.getX(), e1.getY());
                            originalAnnotation = annotation != null ? (Annotation) annotation.copy() : null;
                            break;
                    }
                }
            }

            if (isMultiTouch) {
                if (editPurpose == MediaEditActivity.EDIT_PURPOSE_CROP) {
                    moveBy(-distanceX, -distanceY);
                }
            } else if (isDrawing) {
                currentDrawingPath.add(new PointF(e2.getX(), e2.getY()));
                invalidate();
            } else if (editPurpose == MediaEditActivity.EDIT_PURPOSE_ANNOTATE && annotation != null) {
                moveBy(annotation, -distanceX, -distanceY);

                if (annotationListener != null) {
                    annotationListener.onDrag(annotation, e2.getX(), e2.getY());
                }
            } else if (editPurpose == MediaEditActivity.EDIT_PURPOSE_CROP) {
                RectF valid = new RectF(borderRect);
                valid.inset(-2 * outThreshold, -2 * outThreshold);

                if (valid.contains(e2.getX(), e2.getY())) {
                    updateCropRegionBy(section, -distanceX, -distanceY);
                }
            }

            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (editPurpose == MediaEditActivity.EDIT_PURPOSE_ANNOTATE) {
                Annotation annotation = getAnnotationAt(e.getX(), e.getY());
                if (annotation != null) {
                    notifyAnnotationTapped(annotation);
                    return true;
                }
            }

            return false;
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

    private enum ReverseActionType {
        ROTATE, FLIP, REMOVE, RESTORE, INSERT
    }

    public static class ReverseAction implements Parcelable {
        ReverseActionType type;
        int index;
        Layer layer;

        protected ReverseAction() {
        }

        public static ReverseAction rotate() {
            ReverseAction action = new ReverseAction();
            action.type = ReverseActionType.ROTATE;

            return action;
        }

        public static ReverseAction flip() {
            ReverseAction action = new ReverseAction();
            action.type = ReverseActionType.FLIP;

            return action;
        }

        public static ReverseAction remove() {
            ReverseAction action = new ReverseAction();
            action.type = ReverseActionType.REMOVE;

            return action;
        }

        public static ReverseAction restore(int index, Layer layer) {
            ReverseAction action = new ReverseAction();
            action.type = ReverseActionType.RESTORE;
            action.index = index;
            action.layer = layer.copy();

            return action;
        }

        public static ReverseAction insert(int index, Layer layer) {
            ReverseAction action = new ReverseAction();
            action.type = ReverseActionType.INSERT;
            action.index = index;
            action.layer = layer.copy();

            return action;
        }

        private ReverseAction(Parcel in) {
            type = ReverseActionType.valueOf(in.readString());
            index = in.readInt();
            layer = in.readParcelable(Layer.class.getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(type.name());
            parcel.writeInt(index);
            parcel.writeParcelable(layer, flags);
        }

        public static final Creator<ReverseAction> CREATOR = new Creator<ReverseAction>() {
            @Override
            public ReverseAction createFromParcel(Parcel in) {
                return new ReverseAction(in);
            }

            @Override
            public ReverseAction[] newArray(int size) {
                return new ReverseAction[size];
            }
        };
    }

    public interface Layer extends Parcelable {
        @NonNull
        Layer copy();

        @NonNull
        Layer fromViewToImage(RectF imageRect, float baseScale);

        @NonNull
        Layer fromImageToView(RectF imageRect, float baseScale);

        void flip(int sizeWidth);

        void rotate(RectF imageRectBefore, RectF imageRectAfter);

        void scale(float scale, float offsetX, float offsetY, float sizeWidth, float sizeHeight);

        void draw(@NonNull Canvas canvas);
    }

    public static class DrawingPath implements Layer {
        private ArrayList<PointF> points = new ArrayList<>();
        private final int color;
        private float width;

        private final Path path = new Path();
        private final Paint paint = new Paint();
        private ArrayList<BezierCurve> curves = new ArrayList<>();

        public DrawingPath(@NonNull ArrayList<PointF> points, int color, float width) {
            this.points = new ArrayList<>(points);
            this.color = color;
            this.width = width;

            computeLayout();
        }

        @NonNull
        @Override
        public Layer copy() {
            return new DrawingPath(new ArrayList<>(points), color, width);
        }

        @NonNull
        @Override
        public Layer fromViewToImage(RectF imageRect, float baseScale) {
            ArrayList<PointF> scaled = new ArrayList<>(points.size());

            for (PointF point : points) {
                float x = (point.x - imageRect.left) / baseScale;
                float y = (point.y - imageRect.top) / baseScale;
                scaled.add(new PointF(x, y));
            }

            return new DrawingPath(scaled, color, width / baseScale);
        }

        @NonNull
        @Override
        public Layer fromImageToView(RectF imageRect, float baseScale) {
            ArrayList<PointF> scaled = new ArrayList<>(points.size());

            for (PointF point : points) {
                float x = point.x * baseScale + imageRect.left;
                float y = point.y * baseScale + imageRect.top;
                scaled.add(new PointF(x, y));
            }

            return new DrawingPath(scaled, color, width * baseScale);
        }

        @Override
        public void flip(int sizeWidth) {
            for (PointF point : points) {
                point.x = sizeWidth - point.x;
            }

            computeLayout();
        }

        @Override
        public void rotate(RectF imageRectBefore, RectF imageRectAfter) {
            float scale = imageRectAfter.width() / imageRectBefore.height();

            for (PointF point : points) {
                float x = point.x - imageRectBefore.left;
                float y = point.y - imageRectBefore.top;
                point.x = imageRectAfter.left + scale * y;
                point.y = imageRectAfter.bottom - scale * x;
            }

            computeLayout();
        }

        @Override
        public void scale(float scale, float offsetX, float offsetY, float sizeWidth, float sizeHeight) {
            ArrayList<PointF> scaled = new ArrayList<>(points.size());

            for (PointF point : points) {
                float x, y;
                if (scale >= 1) {
                    x = point.x * scale - (scale - 1) * sizeWidth / 2 + offsetX;
                    y = point.y * scale - (scale - 1) * sizeHeight / 2 + offsetY;
                } else {
                    x = (point.x + (1 / scale - 1) * sizeWidth / 2 - offsetX) * scale;
                    y = (point.y + (1 / scale - 1) * sizeHeight / 2 - offsetY) * scale;
                }

                scaled.add(new PointF(x, y));
            }

            points = scaled;
            width *= scale;

            computeLayout();
        }

        private void computeLayout() {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(color);
            paint.setStrokeWidth(width);

            curves = curves(sampleDrawingPoints(points));
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            PointF start = points.get(0);

            path.reset();
            path.moveTo(start.x, start.y);

            for (BezierCurve curve : curves) {
                path.cubicTo(curve.control1.x, curve.control1.y, curve.control2.x, curve.control2.y, curve.end.x, curve.end.y);
            }

            canvas.drawPath(path, paint);
        }

        protected DrawingPath(Parcel in) {
            color = in.readInt();
            width = in.readFloat();
            in.readTypedList(points, PointF.CREATOR);

            computeLayout();
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
    }

    public static class Annotation implements Layer {
        private String text;
        private float textSize;
        private int color;
        private final PointF center;
        private float rotation;

        private StaticLayout layout;

        public Annotation(String text, float textSize, int color, PointF center, float rotation) {
            this.text = text;
            this.textSize = textSize;
            this.color = color;
            this.center = center;
            this.rotation = rotation;

            computeLayout();
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
            computeLayout();
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
            computeLayout();
        }

        @NonNull
        @Override
        public Layer copy() {
            return new Annotation(text, textSize, color, new PointF(center.x, center.y), rotation);
        }

        protected Annotation(Parcel in) {
            text = in.readString();
            textSize = in.readFloat();
            color = in.readInt();
            center = in.readParcelable(PointF.class.getClassLoader());
            rotation = in.readFloat();

            computeLayout();
        }

        private void computeLayout() {
            TextPaint textPaint = new TextPaint();
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(textSize);
            textPaint.setColor(color);

            int width = (int) textPaint.measureText(text);
            layout = new StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
        }

        @NonNull
        @Override
        public Layer fromViewToImage(RectF imageRect, float baseScale) {
            PointF scaledCenter = new PointF((center.x - imageRect.left) / baseScale, (center.y - imageRect.top) / baseScale);
            return new Annotation(text, textSize / baseScale, color, scaledCenter, rotation);
        }

        @NonNull
        @Override
        public Layer fromImageToView(RectF imageRect, float baseScale) {
            PointF scaledCenter = new PointF(center.x * baseScale + imageRect.left, center.y * baseScale + imageRect.top);
            return new Annotation(text, textSize * baseScale, color, scaledCenter, rotation);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            canvas.save();

            canvas.rotate(rotation, center.x, center.y);
            canvas.translate(center.x - (float)layout.getWidth() / 2, center.y - (float)layout.getHeight() / 2);
            layout.draw(canvas);

            canvas.restore();
        }

        @Override
        public void flip(int sizeWidth) {
            center.x = sizeWidth - center.x;
        }

        @Override
        public void rotate(RectF imageRectBefore, RectF imageRectAfter) {
            float scale = imageRectAfter.width() / imageRectBefore.height();

            float x = center.x - imageRectBefore.left;
            float y = center.y - imageRectBefore.top;
            center.x = imageRectAfter.left + scale * y;
            center.y = imageRectAfter.bottom - scale * x;

            rotation -= 90;
            textSize *= scale;

            computeLayout();
        }

        @Override
        public void scale(float scale, float offsetX, float offsetY, float sizeWidth, float sizeHeight) {
            if (scale >= 1) {
                center.x = center.x * scale - (scale - 1) * sizeWidth / 2 + offsetX;
                center.y = center.y * scale - (scale - 1) * sizeHeight / 2 + offsetY;
            } else {
                center.x = (center.x + (1 / scale - 1) * sizeWidth / 2 - offsetX) * scale;
                center.y = (center.y + (1 / scale - 1) * sizeHeight / 2 - offsetY) * scale;
            }

            textSize *= scale;

            computeLayout();
        }

        public void zoomBy(float scale) {
            textSize *= scale;
            computeLayout();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(text);
            parcel.writeFloat(textSize);
            parcel.writeInt(color);
            parcel.writeParcelable(center, flags);
            parcel.writeFloat(rotation);
        }

        public static final Parcelable.Creator<Annotation> CREATOR = new Parcelable.Creator<Annotation>() {
            public Annotation createFromParcel(Parcel in) {
                return new Annotation(in);
            }

            public Annotation[] newArray(int size) {
                return new Annotation[size];
            }
        };

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Annotation that = (Annotation) o;
            return Float.compare(that.textSize, textSize) == 0 && color == that.color && Float.compare(that.rotation, rotation) == 0 && Objects.equals(text, that.text) && Objects.equals(center, that.center);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, textSize, color, center, rotation);
        }
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
        public ArrayList<Layer> layers = new ArrayList<>();
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
            in.readList(layers, Layer.class.getClassLoader());
            in.readList(reverseActionStack, ReverseAction.class.getClassLoader());
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
            parcel.writeList(layers);
            parcel.writeList(reverseActionStack);
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

            return Float.compare(state.offsetX, offsetX) == 0 && Float.compare(state.offsetY,offsetY) == 0 && Objects.equals(state.reverseActionStack, reverseActionStack)
                    && Float.compare(state.scale, scale) == 0 && state.rotationCount == rotationCount && state.hFlipped == hFlipped
                    && state.vFlipped == vFlipped && state.cropWidth == cropWidth && state.cropHeight == cropHeight
                    && state.cropOffsetX == cropOffsetX && state.cropOffsetY == cropOffsetY && Objects.equals(state.layers, layers);
        }

        public boolean isInDefaultState() {
            return Float.compare(offsetX, 0f) == 0 &&
                    Float.compare(offsetY, 0f) == 0 &&
                    Float.compare(scale, 1f) == 0 &&
                    rotationCount == 0 &&
                    !hFlipped &&
                    !vFlipped &&
                    cropOffsetX == 0 &&
                    cropOffsetY == 0 &&
                    layers.size() == 0 &&
                    reverseActionStack.size() == 0;
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
