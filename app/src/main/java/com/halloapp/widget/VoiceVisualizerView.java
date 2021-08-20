package com.halloapp.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.media.VoiceNoteRecorder;
import com.halloapp.util.Rtl;

public class VoiceVisualizerView extends View {

    private final Paint paint = new Paint();

    private int maxCircleRadius;
    private int minCircleRadius;
    private int centerOffset;

    private float scale = 1.0f;

    private int currentAmplitude;

    private ValueAnimator amplitudeAnimator;

    private boolean rtl;

    public VoiceVisualizerView(Context context) {
        super(context);

        init();
    }

    public VoiceVisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public VoiceVisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        paint.setColor(getContext().getResources().getColor(R.color.voice_note_circle));

        minCircleRadius = getResources().getDimensionPixelSize(R.dimen.voice_note_circle_min_radius);
        maxCircleRadius = getResources().getDimensionPixelSize(R.dimen.voice_note_circle_max_radius);

        centerOffset = getResources().getDimensionPixelSize(R.dimen.voice_note_circle_offset);

        rtl = Rtl.isRtl(getContext());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float radius = (maxCircleRadius - minCircleRadius) * ((float) currentAmplitude / 10_000.0f);
        radius += minCircleRadius;
        radius = Math.min(maxCircleRadius, radius);
        radius *= scale;

        float centerX = rtl ? centerOffset : getWidth() - centerOffset;
        canvas.drawCircle(centerX, getHeight() - centerOffset, radius, paint);
    }

    public void updateAmplitude(int amplitude) {
        if (amplitudeAnimator != null) {
            amplitudeAnimator.cancel();
        }
        amplitudeAnimator = ValueAnimator.ofInt(currentAmplitude, amplitude);
        amplitudeAnimator.setDuration(VoiceNoteRecorder.PLAYBACK_UPDATE_TIME);
        amplitudeAnimator.addUpdateListener(animation -> {
            currentAmplitude = (Integer)animation.getAnimatedValue();
            invalidate();
        });
        amplitudeAnimator.start();
    }

    public void setScale(float scale) {
        if (this.scale != scale) {
            this.scale = scale;
        }
        invalidate();
    }
}
