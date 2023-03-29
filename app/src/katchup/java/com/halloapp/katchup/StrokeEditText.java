package com.halloapp.katchup;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;

import com.halloapp.util.Preconditions;

/**
 * This class uses two separate draw functions in order to draw a stroke on the text, which
 * is not supported by default. The stroke and fill only match up if the gravity is set
 * to top, so vertical centering must be performed by a parent. The reason gravity must be
 * set to top is because prior to drawing the text, the canvas is translated by the sum of
 * extendedTopPadding and the result of getVerticalOffset(), but the latter is not available
 * to us (@UnsupportedAppUsage).
 */
public class StrokeEditText extends androidx.appcompat.widget.AppCompatEditText {

    public StrokeEditText(Context context) {
        super(context);
        init();
    }

    public StrokeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StrokeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Preconditions.checkState((getGravity() & Gravity.TOP) != 0, "StrokeEditText must have gravity set to top");
        setBackground(null);
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        getPaint().setColor(Color.BLACK);
        getPaint().setStrokeWidth(14f);
        getPaint().setStyle(Paint.Style.STROKE);
        getLayout().draw(canvas);

        getPaint().setStrokeWidth(0f);
        getPaint().setStyle(Paint.Style.FILL);
        super.onDraw(canvas);
    }
}
