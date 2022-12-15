package com.halloapp.katchup.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.halloapp.R;
import com.halloapp.util.Rtl;
import com.halloapp.widget.VoiceNoteRecorderControlView;
import com.halloapp.widget.VoiceVisualizerView;

public class VideoReactionRecordControlView extends FrameLayout {

    private View voiceDelete;
    private View arrowCancel;

    private final int[] pos = new int[2];

    private int swipeToCancelDistance;
    private int swipeToLockDistance;

    private int stateLockDistance;
    private int fadeDistance;

    private int levitateDistance;

    private RecordingListener listener;

    public interface RecordingListener {
        void onCancel();
        void onSend();
    }

    public VideoReactionRecordControlView(@NonNull Context context) {
        super(context);
        init();
    }

    public VideoReactionRecordControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoReactionRecordControlView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private int state;

    private static final int STATE_DEFAULT = 0;
    private static final int STATE_CANCELING = 2;
    private static final int STATE_DONE = 4;

    private ValueAnimator enterAnimator;
    private ValueAnimator levitateAnimator;

    private float currentOffset;

    private boolean rtl;

    private View recordingTime;

    public void onTouch(MotionEvent event) {
        final int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            getLocationInWindow(pos);
            state = STATE_DEFAULT;
            fadeInElements();
        } else if (action == MotionEvent.ACTION_UP) {
            onDone();
            if (state != STATE_DONE) {
                if (listener != null) {
                    listener.onSend();
                }
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            float adjustedX = event.getRawX() - pos[0];
            if (rtl) {
                adjustedX = getWidth() - adjustedX;
            }
            updateUI(adjustedX, event.getRawY() - pos[1]);
        } else if (action == MotionEvent.ACTION_CANCEL) {
            if (state != STATE_DONE) {
                listener.onCancel();
                state = STATE_DONE;
                onDone();
            }
        }
    }

    private void updatePadding() {
        if (recordingTime == null) {
            recordingTime = ((ViewGroup) getParent()).findViewById(R.id.recording_time);
        }
        if (recordingTime != null) {
            int paddingLeft;
            int paddingRight;
            switch(getLayoutDirection()) {
                case LAYOUT_DIRECTION_RTL:
                    paddingLeft = 0;
                    paddingRight = getWidth() - recordingTime.getLeft();
                    break;
                case LAYOUT_DIRECTION_LTR:
                default:
                    paddingLeft = recordingTime.getRight();
                    paddingRight = 0;
            }
            setPadding(paddingLeft, 0, paddingRight, 0);
        }
    }

    private void fadeInElements() {
        if (enterAnimator != null) {
            enterAnimator.cancel();
        }
        if (levitateAnimator != null) {
            levitateAnimator.cancel();
        }
        voiceDelete.setTranslationX(0);
        voiceDelete.setAlpha(0);

        enterAnimator = ValueAnimator.ofFloat(0.1f, 1.0f);
        enterAnimator.setDuration(150);
        enterAnimator.addUpdateListener(animation -> {
            float value = (Float) animation.getAnimatedValue();
            voiceDelete.setAlpha(value);
            setArrowAlpha(value);
        });
        enterAnimator.start();

        currentOffset = 0;
        levitateAnimator = ValueAnimator.ofFloat(-1.0f, 0.5f);
        levitateAnimator.setStartDelay(150);
        levitateAnimator.setDuration(600);
        levitateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        levitateAnimator.setRepeatMode(ValueAnimator.REVERSE);
        levitateAnimator.addUpdateListener(animation -> {
            float value = (Float) animation.getAnimatedValue();
            if (state == STATE_DEFAULT) {
                currentOffset = value * levitateDistance;
            }
        });
        levitateAnimator.start();
    }

    public void setRecordingListener(@Nullable RecordingListener listener) {
        this.listener = listener;
    }

    private void updateUI(float x, float y) {
        x = Math.min(getWidth(), x);
        y = Math.min(getHeight(), y);

        int width = getWidth();
        int height = getHeight();

        float dX = width - x;
        float dY = height - y;

        if (state == STATE_CANCELING) {
            if (dX < stateLockDistance) {
                state = STATE_DEFAULT;
            }
        }
        if (state == STATE_DEFAULT) {
            if (dX > stateLockDistance) {
                state = STATE_CANCELING;
                updatePadding();
            }
        }
        switch (state) {
            case STATE_CANCELING: {
                if (dX > swipeToCancelDistance) {
                    state = STATE_DONE;
                    if (listener != null) {
                        listener.onCancel();
                    }
                }
                updateButtonsCanceling(x, y);
                break;
            }
            case STATE_DONE: {
                onDone();
                break;
            }
            default:
            case STATE_DEFAULT: {
                updateButtonsDefault();
                break;
            }
        }
    }

    private void onDone() {
        if (enterAnimator != null) {
            enterAnimator.cancel();
            enterAnimator = null;
        }
        if (levitateAnimator != null) {
            levitateAnimator.cancel();
            levitateAnimator = null;
        }

        voiceDelete.setAlpha(0);
        setArrowAlpha(0);
    }

    private void updateButtonsDefault() {
        voiceDelete.setTranslationX(0);
        voiceDelete.setAlpha(1);
        setArrowAlpha(1);
    }

    private void updateButtonsCanceling(float x, float y) {
        x += stateLockDistance;
        float dX = getWidth() - x;

        if (dX + stateLockDistance > swipeToCancelDistance - fadeDistance) {
            voiceDelete.setAlpha((swipeToCancelDistance - (dX + stateLockDistance) ) / fadeDistance);
        } else {
            voiceDelete.setAlpha(1.0f);
        }

        if (dX + stateLockDistance > 0) {
            float scale = Math.max(0.0f, (1.0f - dX / swipeToCancelDistance));
            setArrowAlpha(scale*scale);
        } else {
            setArrowAlpha(0);
        }
        voiceDelete.setTranslationX(rtl ? dX : -dX);
    }

    private void setArrowAlpha(float alpha) {
        arrowCancel.setAlpha(alpha);
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.video_reaction_recording_ui, this, true);

        voiceDelete = findViewById(R.id.voice_delete);

        arrowCancel = findViewById(R.id.arrow_cancel);

        rtl = Rtl.isRtl(getContext());

        swipeToCancelDistance = getContext().getResources().getDimensionPixelSize(R.dimen.voice_note_cancel_distance);
        swipeToLockDistance = getContext().getResources().getDimensionPixelSize(R.dimen.voice_note_lock_distance);

        fadeDistance = getContext().getResources().getDimensionPixelSize(R.dimen.voice_note_fade_distance);
        stateLockDistance = getContext().getResources().getDimensionPixelSize(R.dimen.voice_note_state_lock_distance);
        levitateDistance = getContext().getResources().getDimensionPixelSize(R.dimen.voice_note_levitate_distance);
    }
}
