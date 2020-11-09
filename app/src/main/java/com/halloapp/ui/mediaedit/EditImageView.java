package com.halloapp.ui.mediaedit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
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

    public static final float MAX_ASPECT_RATIO = 1.25f;
    public static final float MAX_SCALE = 10.0f;
    public static final float MIN_SCALE = 1.0f;

    // 'THRESHOLD_SCALE' is used together with 'threshold' to compute the minimum crop region size.
    // 'threshold' is the recommended size for a tapable region. Both vertically and horizontally there
    // are 3 different tapable regions (two borders and inside), thus 'THRESHOLD_SCALE' >= 3 is required.
    // Current value of 4 is chosen to give more space for easier tapping on different parts of the crop region.
    private static final float THRESHOLD_SCALE = 4;

    private final float initialOffset = getResources().getDimension(R.dimen.media_crop_region_init_offset);
    private final float threshold = getResources().getDimension(R.dimen.media_crop_region_threshold);
    private final float borderThickness = getResources().getDimension(R.dimen.media_crop_region_border);
    private final float borderRadius = getResources().getDimension(R.dimen.media_crop_region_radius);

    private final RectF imageRect = new RectF();
    private RectF cropRect = new RectF();
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

                computeClipBounds();
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
        final float imageCenterX = ((float)getWidth() / 2) + offsetX;
        final float imageCenterY = ((float)getHeight() / 2) + offsetY;

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

    private void computeClipBounds() {
        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        final float w = getWidth();
        final float h = getHeight();
        final float dw = (numberOfRotations % 2) == 0 ? getDrawable().getIntrinsicWidth() : getDrawable().getIntrinsicHeight();
        final float dh = (numberOfRotations % 2) == 0 ? getDrawable().getIntrinsicHeight() : getDrawable().getIntrinsicWidth();

        final float baseScale = Math.min(w / dw, h / dh);

        final int l = (int) Math.floor((w - dw * baseScale) / 2);
        final int t = (int) Math.floor((h - dh * baseScale) / 2);
        final int r = Math.round((w + dw * baseScale) / 2);
        final int b = Math.round((h + dh * baseScale) / 2);

        imageRect.set(l, t, r, b);
        setClipBounds(new Rect(l, t, r, b));
    }

    public void computeInitialCropRegion() {
        cropRect.set(imageRect);
        cropRect.inset(borderThickness / 2, borderThickness / 2);

        final float w = cropRect.width();
        final float h = cropRect.height();
        final float nh = w * Math.min(MAX_ASPECT_RATIO, h / w);

        cropRect.bottom += nh - h;
    }

    private void updateImage() {
        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        final float w = getWidth();
        final float h = getHeight();
        final float dw = drawable.getIntrinsicWidth();
        final float dh = getDrawable().getIntrinsicHeight();
        final float baseScale = (numberOfRotations % 2) == 0 ? Math.min(w / dw, h / dh) : Math.min(w / dh, h / dw);
        final float x = (w - dw * baseScale * scale) / 2;
        final float y = (h - dh * baseScale * scale) / 2;

        Matrix m = new Matrix();
        m.postScale(baseScale * scale, baseScale * scale);
        m.postTranslate(x, y);

        m.postRotate(-90 * numberOfRotations, w / 2, h / 2);
        m.postScale(vFlipped ? -1 : 1, hFlipped ? -1 : 1, w / 2, h / 2);
        m.postTranslate(offsetX, offsetY);

        setImageMatrix(m);
    }

    public void reset() {
        clearValues();
        computeClipBounds();
        computeInitialCropRegion();
        updateImage();
        requestLayout();
    }

    public void zoom(float scale) {
        if (scale < MIN_SCALE || scale > MAX_SCALE) {
            return;
        }

        final float centerX = (float)getWidth() / 2.0f;
        final float centerY = (float)getHeight() / 2.0f;
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

    public void zoomBy(float scaleBy) {
        zoom(scale * scaleBy);
    }

    public void move(float offsetX, float offsetY) {
        final float centerX = (float)getWidth() / 2.0f;
        final float centerY = (float)getHeight() / 2.0f;
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
        computeClipBounds();

        final float scale = imageRect.width() / before.height();

        final float ox = offsetX;
        offsetX = offsetY * scale;
        offsetY = -ox * scale;

        final float x = cropRect.left - before.left;
        final float y = cropRect.top - before.top;
        final float l = imageRect.left + y * scale;
        final float b = imageRect.bottom - x * scale;

        cropRect.set(l, b - cropRect.width() * scale, l + cropRect.height() * scale, b);

        updateImage();
    }

    private CropRegionSection getCropSectionAt(float x, float y) {
        boolean isTop = (cropRect.top < y) && (y < (cropRect.top + threshold));
        boolean isBottom = ((cropRect.bottom - threshold) < y) && (y < cropRect.bottom);
        boolean isLeft = (cropRect.left < x) && (x < (cropRect.left + threshold));
        boolean isRight = ((cropRect.right - threshold) < x) && (x < cropRect.right);
        boolean isInsideVertical = (cropRect.top + threshold) < y && y < (cropRect.bottom - threshold);
        boolean isInsideHorizontal = (cropRect.left + threshold) < x && x < (cropRect.right - threshold);

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

    private void keepWithinMaxRatio(RectF crop, CropRegionSection section) {
        if ((crop.height() / crop.width()) > MAX_ASPECT_RATIO) {
            switch (section) {
                case LEFT:
                case RIGHT: {
                    final float d = (crop.height() - MAX_ASPECT_RATIO * crop.width()) / 2;
                    crop.top += d;
                    crop.bottom -= d;
                    break;
                }
                default: {
                    final float d = ((crop.height() / MAX_ASPECT_RATIO) - crop.width()) / 2;
                    crop.left -= d;
                    crop.right += d;
                    break;
                }
            }
        }
    }

    private boolean isCropRegionWithinImage(RectF crop) {
        final float offset = borderThickness / 2;
        final RectF limit = new RectF(imageRect);
        limit.inset(offset, offset);

        return limit.contains(crop);
    }

    private boolean isCropRegionTooSmall(RectF crop) {
        return (crop.width() < threshold * THRESHOLD_SCALE) || (crop.height() < threshold * THRESHOLD_SCALE);
    }

    private boolean isCropRegionValid(RectF crop) {
        return isCropRegionWithinImage(crop) && !isCropRegionTooSmall(crop);
    }

    private void updateCropRegionBy(CropRegionSection section, float deltaX, float deltaY) {
        RectF computed = computeCropRegion(section, deltaX, deltaY);
        keepWithinMaxRatio(computed, section);

        if (isCropRegionValid(computed)) {
            cropRect = computed;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawShadow(canvas);
        drawBorder(canvas);
    }

    private void drawShadow(Canvas canvas) {
        path.reset();
        path.addRect(imageRect, Path.Direction.CW);
        path.addRoundRect(cropRect,borderRadius, borderRadius, Path.Direction.CW);
        path.setFillType(Path.FillType.EVEN_ODD);
        canvas.drawPath(path, shadowPaint);
    }

    private void drawBorder(Canvas canvas) {
        path.reset();
        path.addRoundRect(cropRect, borderRadius, borderRadius, Path.Direction.CW);
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
            zoomBy(scaleGestureDetector.getScaleFactor());
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
                updateCropRegionBy(section, -distanceX, -distanceY);
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
