package com.halloapp.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.halloapp.R;
import com.halloapp.util.Rtl;

public class VoiceNoteRecorderControlView extends FrameLayout {

    private View voiceLock;
    private View voiceDelete;
    private VoiceVisualizerView voiceVisualizerView;

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
        void onLock();
    }

    public VoiceNoteRecorderControlView(@NonNull Context context) {
        super(context);
        init();
    }

    public VoiceNoteRecorderControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VoiceNoteRecorderControlView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private int state;

    private static final int STATE_DEFAULT = 0;
    private static final int STATE_LOCKING = 1;
    private static final int STATE_CANCELING = 2;
    private static final int STATE_LOCKED = 3;
    private static final int STATE_DONE = 4;

    private ValueAnimator enterAnimator;
    private ValueAnimator levitateAnimator;

    private float currentOffset;

    private boolean rtl;

    public void onTouch(MotionEvent event) {
        final int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            getLocationInWindow(pos);
            state = STATE_DEFAULT;
            fadeInElements();
        } else if (action == MotionEvent.ACTION_UP) {
            onDone();
            if (state != STATE_DONE && state != STATE_LOCKED) {
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
            if (state != STATE_DONE && state != STATE_LOCKED) {
                listener.onCancel();
                state = STATE_DONE;
                onDone();
            }
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
        voiceLock.setTranslationY(0);
        voiceDelete.setAlpha(0);
        voiceLock.setAlpha(0);
        voiceVisualizerView.setAlpha(1);

        enterAnimator = ValueAnimator.ofFloat(0.1f, 1.0f);
        enterAnimator.setDuration(150);
        enterAnimator.addUpdateListener(animation -> {
            float value = (Float) animation.getAnimatedValue();
            voiceVisualizerView.setScale(value);
            voiceLock.setAlpha(value);
            voiceDelete.setAlpha(value);
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
                voiceLock.setTranslationY(currentOffset);
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
        } else if (state == STATE_LOCKING) {
            if (dY < stateLockDistance) {
                state = STATE_DEFAULT;
            }
        }
        if (state == STATE_DEFAULT) {
            if (dX > stateLockDistance && dY > stateLockDistance) {
                if (dX > dY) {
                    state = STATE_CANCELING;
                } else {
                    state = STATE_LOCKING;
                }
            } else if (dX > stateLockDistance) {
                state = STATE_CANCELING;
            } else if (dY > stateLockDistance) {
                state = STATE_LOCKING;
            }
        }
        switch (state) {
            case STATE_LOCKING: {
                if (dY > swipeToLockDistance) {
                    state = STATE_LOCKED;
                    if (listener != null) {
                        listener.onLock();
                    }
                }
                updateButtonsLocking(x, y);
                break;
            }
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
            case STATE_LOCKED:
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
        voiceLock.setAlpha(0);
        voiceVisualizerView.setAlpha(0);
    }

    private void updateButtonsDefault() {
        voiceLock.setTranslationY(currentOffset);
        voiceDelete.setTranslationX(0);
        voiceDelete.setAlpha(1);
        voiceLock.setAlpha(1);
        voiceVisualizerView.setAlpha(1);
        voiceVisualizerView.setScale(1.0f);
    }

    private void updateButtonsCanceling(float x, float y) {
        x += stateLockDistance;
        float dX = getWidth() - x;
        voiceLock.setTranslationY(0);

        if (dX + stateLockDistance > swipeToCancelDistance - fadeDistance) {
            voiceDelete.setAlpha((swipeToCancelDistance - (dX + stateLockDistance) ) / fadeDistance);
        } else {
            voiceDelete.setAlpha(1.0f);
        }

        if (dX + stateLockDistance > 0) {
            float scale = Math.max(0.0f, (1.0f - dX / swipeToCancelDistance));
            voiceVisualizerView.setScale(scale);
            voiceVisualizerView.setAlpha(scale * scale);
        } else {
            voiceVisualizerView.setScale(1.0f);
            voiceVisualizerView.setAlpha(0);
        }

        if (dX > fadeDistance) {
            voiceLock.setAlpha(0);
        } else {
            voiceLock.setAlpha(1.0f - Math.max(0, dX  / fadeDistance));
        }
        voiceDelete.setTranslationX(rtl ? dX : -dX);
    }

    private void updateButtonsLocking(float x, float y) {
        y += stateLockDistance;
        float dY = getHeight() - y;
        voiceDelete.setTranslationX(0);

        if (dY + stateLockDistance > swipeToLockDistance - fadeDistance) {
            voiceLock.setAlpha((swipeToLockDistance - (dY + stateLockDistance) ) / fadeDistance);
        } else {
            voiceLock.setAlpha(1.0f);
        }

        if (dY + stateLockDistance > 0) {
            float scale = Math.max(0.0f, (1.0f - dY / swipeToLockDistance));
            voiceVisualizerView.setScale(scale);
            voiceVisualizerView.setAlpha(scale * scale);
        } else {
            voiceVisualizerView.setScale(1.0f);
            voiceVisualizerView.setAlpha(0);
        }

        if (dY > fadeDistance) {
            voiceDelete.setAlpha(0);
        } else {
            voiceDelete.setAlpha(1.0f - Math.max(0, dY  / fadeDistance));
        }
        voiceLock.setTranslationY(-dY);
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.voice_note_recording_ui, this, true);

        voiceLock = findViewById(R.id.voice_lock);
        voiceDelete = findViewById(R.id.voice_delete);

        voiceVisualizerView = findViewById(R.id.visualizer);

        rtl = Rtl.isRtl(getContext());

        swipeToCancelDistance = getContext().getResources().getDimensionPixelSize(R.dimen.voice_note_cancel_distance);
        swipeToLockDistance = getContext().getResources().getDimensionPixelSize(R.dimen.voice_note_lock_distance);

        fadeDistance = getContext().getResources().getDimensionPixelSize(R.dimen.voice_note_fade_distance);
        stateLockDistance = getContext().getResources().getDimensionPixelSize(R.dimen.voice_note_state_lock_distance);
        levitateDistance = getContext().getResources().getDimensionPixelSize(R.dimen.voice_note_levitate_distance);
    }

    public void updateAmplitude(Integer amplitude) {
        int amp = amplitude == null ? 0 : amplitude;
        voiceVisualizerView.updateAmplitude(amp);
    }

    public void bindAmplitude(@NonNull LifecycleOwner owner, @NonNull LiveData<Integer> amplitude) {
        amplitude.observe(owner, this::updateAmplitude);
    }
}
