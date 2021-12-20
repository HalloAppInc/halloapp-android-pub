package com.halloapp.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.media.VoiceNoteRecorder;

import java.util.ArrayList;
import java.util.List;

public class VoicePostVisualizerView extends View {

    private static final float OUTER_CIRCLE_VARIANCE = 10f;
    private static final float INNER_CIRCLE_VARIANCE = 6f;

    private static final float INNER_CIRCLE_SCALE = 0.4f;
    private static final float VERTEX_MAX_VELOCITY = 1f;

    private final Paint paint = new Paint();

    private int maxCircleRadius;
    private int minInnerCircleRadius;
    private int minOuterCircleRadius;

    private float scale = 1.0f;

    private int currentAmplitude;

    private ValueAnimator amplitudeAnimator;

    private List<Vertex> innerPoints = new ArrayList<>();
    private List<Vertex> outerPoints = new ArrayList<>();

    private Path outerPath;
    private Path innerPath;

    private long lastUpdate;
    private long lastVUpdate;

    private static class Vertex {
        public float length;
        public float velocity;
    }

    public VoicePostVisualizerView(Context context) {
        super(context);

        init();
    }

    public VoicePostVisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public VoicePostVisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        paint.setColor(getContext().getResources().getColor(R.color.voice_post_circle));

        minInnerCircleRadius = getResources().getDimensionPixelSize(R.dimen.voice_post_circle_min_inner_radius);
        minOuterCircleRadius = getResources().getDimensionPixelSize(R.dimen.voice_post_circle_min_outer_radius);
        maxCircleRadius = getResources().getDimensionPixelSize(R.dimen.voice_post_circle_max_radius);

        for (int i = 0; i < 5; i++) {
            outerPoints.add(new Vertex());
            innerPoints.add(new Vertex());
        }

        outerPath = new Path();
        innerPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float outerRadius = (maxCircleRadius - minOuterCircleRadius) * ((float) currentAmplitude / 10_000.0f);
        outerRadius += minOuterCircleRadius;
        outerRadius = Math.min(maxCircleRadius, outerRadius);
        outerRadius *= scale;

        float innerRadius = (maxCircleRadius - minInnerCircleRadius) * ((float) currentAmplitude / 10_000.0f) * INNER_CIRCLE_SCALE;
        innerRadius += minInnerCircleRadius;
        innerRadius = Math.min(maxCircleRadius, innerRadius);
        innerRadius *= scale;

        computeCircle(outerRadius, outerPath, outerPoints);
        computeCircle(innerRadius, innerPath, innerPoints);

        if (lastUpdate == 0) {
            lastUpdate = System.currentTimeMillis();
        }
        for (Vertex v : outerPoints) {
            v.length += (v.velocity);
            v.length = Math.min(v.length, OUTER_CIRCLE_VARIANCE);
            v.length = Math.max(v.length, -OUTER_CIRCLE_VARIANCE);
        }
        for (Vertex v : innerPoints) {
            v.length += (v.velocity);
            v.length = Math.min(v.length, INNER_CIRCLE_VARIANCE);
            v.length = Math.max(v.length, -INNER_CIRCLE_VARIANCE);
        }
        if (System.currentTimeMillis() - lastVUpdate > 150) {
            lastVUpdate = System.currentTimeMillis();
            for (Vertex rad : outerPoints) {
                rad.velocity = (float) (Math.random() * 2 * VERTEX_MAX_VELOCITY) - VERTEX_MAX_VELOCITY;
            }

            for (Vertex rad : innerPoints) {
                rad.velocity = (float) (Math.random() * 2 * VERTEX_MAX_VELOCITY) - VERTEX_MAX_VELOCITY * INNER_CIRCLE_SCALE;
            }
        }
        canvas.translate(getWidth() / 2.0f, getHeight() / 2.0f);
        canvas.drawPath(outerPath, paint);
        canvas.drawPath(innerPath, paint);
    }

    private void computeCircle(float radius, Path path, List<Vertex> points) {
        path.reset();
        int parts = points.size();

        float part = 360.0f / parts;
        float L = 4f * (float) Math.tan(Math.toRadians(part / 4f)) / 3f;
        float r = radius + points.get(parts - 1).length;
        float angle = 0;
        float x0 = (float)(r * Math.cos(Math.toRadians(angle)));
        float y0 = (float)(r * Math.sin(Math.toRadians(angle)));
        path.moveTo(x0, y0);
        for (int i = 0; i < parts; i++) {
            Vertex r1 = points.get(i);
            float rad1 = radius + r1.length;
            float x1 = (float)(r * L * Math.cos(Math.toRadians(angle + 90))) + x0;
            float y1 = (float)(r * L * Math.sin(Math.toRadians(angle + 90))) + y0;

            angle += part;

            float x3 = (float)(rad1 * Math.cos(Math.toRadians(angle)));
            float y3 = (float)(rad1 * Math.sin(Math.toRadians(angle)));
            float x2 = (float)(rad1 * L * Math.cos(Math.toRadians(angle - 90))) + x3;
            float y2 = (float)(rad1 * L * Math.sin(Math.toRadians(angle - 90))) + y3;

            x0 = x3;
            y0 = y3;

            path.cubicTo(x1, y1, x2, y2, x3, y3);
            r = rad1;
        }
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
