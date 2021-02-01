package com.halloapp.ui.mediaedit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.BgWorkers;

import java.io.File;
import java.io.IOException;

public class EditImageView extends androidx.appcompat.widget.AppCompatImageView {
    @FunctionalInterface
    public interface ImageLoadedListener {
        void loaded(Bitmap originalBitmap, Bitmap croppedBitmap);
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

    private final RectF imageRect = new RectF();
    private final RectF cropRect = new RectF();
    private final RectF borderRect = new RectF();
    private float maxAspectRatio = Constants.MAX_IMAGE_ASPECT_RATIO;
    private final Path path = new Path();
    private final Paint shadowPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private float offsetX = 0.0f;
    private float offsetY = 0.0f;
    private float scale = 1.0f;
    private int numberOfRotations = 0;
    private boolean hFlipped = false;
    private boolean vFlipped = false;

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

        shadowPaint.setColor(getResources().getColor(R.color.black_70));
        shadowPaint.setStyle(Paint.Style.FILL);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderThickness);

        setOnTouchListener(new GestureListener());
    }

    @WorkerThread
    @Nullable
    private Bitmap loadBitmap(@Nullable File file) {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);

        int limit = Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels));

        try {
            return file != null ? MediaUtils.decodeImage(file, limit, limit) : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setMaxAspectRatio(float maxAspectRatio) {
        this.maxAspectRatio = maxAspectRatio;
    }

    public void setAsyncImageFile(@Nullable File originalFile, @Nullable File croppedFile, @Nullable State state, @Nullable ImageLoadedListener listener) {
        setImageDrawable(null);
        clearValues();

        if (originalFile == null) {
            return;
        }

        BgWorkers.getInstance().execute(() -> {
            final Bitmap bitmap = loadBitmap(originalFile);
            if (bitmap == null) {
                return;
            }

            final Bitmap cropped = state != null ? loadBitmap(croppedFile) : null;

            post(() -> {
                setImageBitmap(bitmap);

                if (state != null) {
                    setState(state);
                }

                computeImageRect();
                if (state == null) {
                    computeInitialCropRegion();
                }

                updateImage();

                if (listener != null) {
                    listener.loaded(bitmap, cropped);
                }
            });
        });
    }

    public State getState() {
        final State state = new State();
        state.cropRect = new RectF(cropRect);
        state.scale = scale;
        state.hFlipped = hFlipped;
        state.vFlipped = vFlipped;
        state.numberOfRotations = numberOfRotations;
        state.offsetX = offsetX;
        state.offsetY = offsetY;

        final float imageWidth = numberOfRotations % 2 == 0 ? getDrawable().getIntrinsicWidth() : getDrawable().getIntrinsicHeight();
        final float baseScale =  imageRect.width() / imageWidth;

        final float cropCenterX = (cropRect.left + cropRect.right) / 2;
        final float cropCenterY = (cropRect.top + cropRect.bottom) / 2;
        final float imageCenterX = imageRect.centerX() + offsetX;
        final float imageCenterY = imageRect.centerY() + offsetY;

        state.cropWidth = Math.round(cropRect.width() / baseScale);
        state.cropHeight = Math.round(cropRect.height() / baseScale);
        state.cropOffsetX = Math.round((cropCenterX - imageCenterX) / baseScale);
        state.cropOffsetY = Math.round((cropCenterY - imageCenterY) / baseScale);

        return state;
    }

    public void setState(@Nullable State state) {
        if (state == null) {
            return;
        }

        cropRect.set(state.cropRect);
        scale = state.scale;
        hFlipped = state.hFlipped;
        vFlipped = state.vFlipped;
        numberOfRotations = state.numberOfRotations;
        offsetX = state.offsetX;
        offsetY = state.offsetY;
    }

    public void clearValues() {
        offsetX = 0.0f;
        offsetY = 0.0f;
        scale = 1.0f;
        numberOfRotations = 0;
        hFlipped = false;
        vFlipped = false;
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
        final float dw = (numberOfRotations % 2) == 0 ? getDrawable().getIntrinsicWidth() : getDrawable().getIntrinsicHeight();
        final float dh = (numberOfRotations % 2) == 0 ? getDrawable().getIntrinsicHeight() : getDrawable().getIntrinsicWidth();

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
        final float baseScale = (numberOfRotations % 2) == 0 ? Math.min(w / dw, h / dh) : Math.min(w / dh, h / dw);
        final float x = imageRect.centerX() - (dw * baseScale * scale) / 2;
        final float y = imageRect.centerY() - (dh * baseScale * scale) / 2;

        Matrix m = new Matrix();
        m.postScale(baseScale * scale, baseScale * scale);
        m.postTranslate(x, y);

        m.postRotate(-90 * numberOfRotations, imageRect.centerX(), imageRect.centerY());
        m.postScale(vFlipped ? -1 : 1, hFlipped ? -1 : 1, imageRect.centerX(), imageRect.centerY());
        m.postTranslate(offsetX, offsetY);

        setImageMatrix(m);
    }

    public void reset() {
        clearValues();
        computeImageRect();
        computeInitialCropRegion();
        updateImage();
        requestLayout();
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
        vFlipped = !vFlipped;

        final float cw = cropRect.width();
        cropRect.left = getWidth() - cw - cropRect.left;
        cropRect.right = cropRect.left + cw;

        offsetX = -offsetX;

        updateImage();
    }

    public void rotate() {
        numberOfRotations = (numberOfRotations + 1) % 4;

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

        updateImage();
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.clipRect(imageRect);
        super.onDraw(canvas);
        canvas.restore();

        drawShadow(canvas);
        drawBorder(canvas);
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

    public static class State implements Parcelable {
        public RectF cropRect;
        public float offsetX;
        public float offsetY;
        public float scale;
        public int numberOfRotations;
        public boolean hFlipped;
        public boolean vFlipped;
        public int cropWidth;
        public int cropHeight;
        public int cropOffsetX;
        public int cropOffsetY;

        @Override
        public int describeContents() {
            return 0;
        }

        public State() {}

        private State(Parcel in) {
            numberOfRotations = in.readInt();
            hFlipped = in.readInt() > 0; // Unable to use in.readBoolean due to API version
            vFlipped = in.readInt() > 0;
            scale = in.readFloat();
            offsetX = in.readFloat();
            offsetY = in.readFloat();
            cropRect = in.readParcelable(RectF.class.getClassLoader());
            cropWidth = in.readInt();
            cropHeight = in.readInt();
            cropOffsetX = in.readInt();
            cropOffsetY = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeInt(numberOfRotations);
            parcel.writeInt(hFlipped ? 1 : 0); // Unable to use parcel.writeBoolean due to API version
            parcel.writeInt(vFlipped ? 1 : 0);
            parcel.writeFloat(scale);
            parcel.writeFloat(offsetX);
            parcel.writeFloat(offsetY);
            parcel.writeParcelable(cropRect, flags);
            parcel.writeInt(cropWidth);
            parcel.writeInt(cropHeight);
            parcel.writeInt(cropOffsetX);
            parcel.writeInt(cropOffsetY);
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
