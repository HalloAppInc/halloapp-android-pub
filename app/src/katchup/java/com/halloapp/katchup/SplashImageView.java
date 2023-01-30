package com.halloapp.katchup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.halloapp.R;
import com.halloapp.util.Preconditions;

public class SplashImageView extends View {
    private int layoutWidth;
    private int layoutHeight;
    private int tileWidth;
    private int tileHeight;
    private BitmapDrawable splashTile;

    private final Rect drawRect = new Rect();

    public SplashImageView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SplashImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SplashImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public SplashImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(@NonNull Context context) {
        splashTile = (BitmapDrawable) Preconditions.checkNotNull(AppCompatResources.getDrawable(context, R.drawable.splash_tile));
        tileWidth = splashTile.getIntrinsicWidth();
        tileHeight = splashTile.getIntrinsicHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (layoutWidth > 0 && layoutHeight > 0) {
            final Bitmap splashBitmap = splashTile.getBitmap();
            final int drawHeight = tileHeight * layoutWidth / tileWidth;
            drawRect.set(0, 0, layoutWidth, drawHeight);

            for (int h = 0; h < layoutHeight; h += drawHeight) {
                drawRect.top = h;
                drawRect.bottom = h + drawHeight;
                canvas.drawBitmap(splashBitmap, null, drawRect, null);
            }
        }

        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            layoutWidth = right - left;
            layoutHeight = bottom - top;
        }
    }
}
