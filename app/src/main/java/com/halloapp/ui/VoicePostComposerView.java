package com.halloapp.ui;

import android.Manifest;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;

import com.halloapp.R;
import com.halloapp.media.AudioDurationLoader;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.media.VoiceNoteRecorder;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.VoicePostVisualizerView;

import java.io.File;

import pub.devrel.easypermissions.EasyPermissions;

public class VoicePostComposerView extends ConstraintLayout {

    private View stopRecordingBtn;
    private View startRecordingBtn;

    private View tapToRecordText;
    private View stopRecordText;

    private View sendBtn;

    private View recordingContainer;
    private View recordingIndicator;
    private TextView recordingTime;

    private View composeTitle;

    private VoiceNotePlayer voiceNotePlayer;
    private VoiceNoteRecorder voiceNoteRecorder;

    private File audioDraft;

    private View draftContainer;
    private TextView draftSeekTime;
    private ImageView draftControlButton;
    private SeekBar draftSeekbar;
    private View draftDeleteButton;

    private View addMediaButton;

    private VoicePostVisualizerView visualizerView;

    private boolean playing;
    private boolean wasPlaying;

    public interface Host {
        void onStartRecording();
        void onStopRecording();
        void onSend();
        void onAttachMedia();
        void onDeleteRecording();
        void requestVoicePermissions();
    }

    private Host host;

    public VoicePostComposerView(@NonNull Context context) {
        super(context);

        init(null);
    }

    public VoicePostComposerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(attrs);
    }

    public VoicePostComposerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(attrs);
    }

    public void bindHost(@NonNull LifecycleOwner owner, @NonNull Host host, @NonNull VoiceNotePlayer voiceNotePlayer, @NonNull VoiceNoteRecorder voiceNoteRecorder) {
        this.host = host;
        bindVoicePlayer(owner, voiceNotePlayer);
        bindVoiceRecorder(owner, voiceNoteRecorder);
    }

    private void init(@Nullable AttributeSet attrs) {
        final Context context = getContext();

        inflate(context, R.layout.voice_post_composer, this);


        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.VoicePostComposerView, 0, 0);
        final boolean showSendButton = a.getBoolean(R.styleable.VoicePostComposerView_showSendButton, true);
        a.recycle();

        stopRecordingBtn = findViewById(R.id.stop_record);
        startRecordingBtn = findViewById(R.id.record_voice);

        tapToRecordText = findViewById(R.id.tap_to_record);
        stopRecordText = findViewById(R.id.stop_record_text);
        sendBtn = findViewById(R.id.composer_send);
        composeTitle = findViewById(R.id.compose_title);

        recordingContainer = findViewById(R.id.recording_container);
        recordingIndicator = findViewById(R.id.recording_indicator);
        recordingTime = findViewById(R.id.recording_time);

        draftContainer = findViewById(R.id.voice_draft);
        draftSeekTime = draftContainer.findViewById(R.id.seek_time);
        draftControlButton = draftContainer.findViewById(R.id.control_btn);
        draftSeekbar = findViewById(R.id.voice_note_seekbar);
        draftDeleteButton = findViewById(R.id.delete_btn);

        stopRecordingBtn.setVisibility(View.GONE);
        recordingContainer.setVisibility(View.GONE);

        visualizerView = findViewById(R.id.voice_visualizer);

        addMediaButton = findViewById(R.id.voice_add_media);

        startRecordingBtn.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (!EasyPermissions.hasPermissions(getContext(), Manifest.permission.RECORD_AUDIO)) {
                    if (host != null) {
                        host.requestVoicePermissions();
                    }
                    return false;
                }
                if (host != null) {
                    host.onStartRecording();
                }
                stopRecordingBtn.setKeepScreenOn(true);
                return true;
            }
            return false;
        });

        stopRecordText.setOnClickListener(v -> {
            if (host != null) {
                host.onStopRecording();
            }
            stopRecordingBtn.setKeepScreenOn(false);
        });
        stopRecordingBtn.setOnClickListener(v -> {
            if (host != null) {
                host.onStopRecording();
            }
            stopRecordingBtn.setKeepScreenOn(false);
        });

        sendBtn.setOnClickListener(v -> {
            if (host != null) {
                host.onSend();
            }
        });
        sendBtn.setVisibility(showSendButton ? VISIBLE : GONE);

        draftDeleteButton.setOnClickListener(v -> {
            if (playing) {
                voiceNotePlayer.pause();
            }
            if (host != null) {
                host.onDeleteRecording();
            }
        });

        draftControlButton.setOnClickListener(v -> {
            if (playing) {
                voiceNotePlayer.pause();
            } else if (audioDraft != null) {
                voiceNotePlayer.playFile(audioDraft.getAbsolutePath(), draftSeekbar.getProgress());
            }
        });
    }

    private void showStartRecordingState() {
        stopRecordingBtn.setVisibility(View.GONE);
        startRecordingBtn.setVisibility(View.VISIBLE);
        tapToRecordText.setVisibility(View.VISIBLE);
        stopRecordText.setVisibility(View.GONE);
        hideRecordingContainer();
        sendBtn.setEnabled(false);
        composeTitle.setVisibility(View.VISIBLE);
        draftContainer.setVisibility(View.GONE);
        addMediaButton.setVisibility(View.GONE);
    }

    private void showRecordingState() {
        stopRecordingBtn.setVisibility(View.VISIBLE);
        startRecordingBtn.setVisibility(View.GONE);
        tapToRecordText.setVisibility(View.GONE);
        stopRecordText.setVisibility(View.VISIBLE);
        recordingContainer.setVisibility(View.VISIBLE);
        sendBtn.setEnabled(false);
        composeTitle.setVisibility(View.GONE);
        draftContainer.setVisibility(View.GONE);
        visualizerView.setVisibility(View.VISIBLE);
        addMediaButton.setVisibility(View.GONE);
        if (recordingIndicator.getAnimation() == null) {
            Animation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(500);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            recordingIndicator.startAnimation(anim);
        }
    }

    private void showDraftState() {
        stopRecordingBtn.setVisibility(View.GONE);
        startRecordingBtn.setVisibility(View.GONE);
        tapToRecordText.setVisibility(View.GONE);
        stopRecordText.setVisibility(View.GONE);
        hideRecordingContainer();
        sendBtn.setEnabled(true);
        composeTitle.setVisibility(View.VISIBLE);
        draftContainer.setVisibility(View.VISIBLE);
        addMediaButton.setVisibility(View.VISIBLE);
    }

    private void hideRecordingContainer() {
        recordingContainer.setVisibility(View.GONE);
        if (recordingIndicator.getAnimation() != null) {
            recordingIndicator.clearAnimation();
        }
        visualizerView.setVisibility(View.GONE);
    }

    public void bindAudioDraft(AudioDurationLoader audioDurationLoader, File audioDraft) {
        if (this.audioDraft != audioDraft) {
            draftSeekbar.setProgress(0);
        }
        this.audioDraft = audioDraft;
        if (audioDraft != null) {
            audioDurationLoader.load(draftSeekTime, audioDraft);
            showDraftState();
        } else {
            showStartRecordingState();
        }
    }

    private void bindVoiceRecorder(@NonNull LifecycleOwner owner, @NonNull VoiceNoteRecorder voiceNoteRecorder) {
        this.voiceNoteRecorder = voiceNoteRecorder;
        voiceNoteRecorder.isRecording().observe(owner, isRecording -> {
            if (isRecording == null || !isRecording) {
                recordingContainer.setVisibility(View.GONE);
                if (audioDraft == null) {
                    showStartRecordingState();
                } else {
                    showDraftState();
                }
            } else {
                showRecordingState();
            }
        });

        voiceNoteRecorder.getRecordingTime().observe(owner, millis -> {
            if (millis == null) {
                return;
            }
            recordingTime.setText(StringUtils.formatVoiceNoteDuration(getContext(), millis));
        });

        voiceNoteRecorder.getRecordingAmplitude().observe(owner, visualizerView::updateAmplitude);
    }

    private void bindVoicePlayer(@NonNull LifecycleOwner owner, @NonNull VoiceNotePlayer voiceNotePlayer) {
        this.voiceNotePlayer = voiceNotePlayer;

        voiceNotePlayer.getPlaybackState().observe(owner, this::onDraftPlaybackState);
    }

    private void onDraftPlaybackState(@Nullable VoiceNotePlayer.PlaybackState state) {
        if (audioDraft == null) {
            return;
        }
        final String audioPath = audioDraft.getAbsolutePath();

        if (state == null || !audioPath.equals(state.playingTag)) {
            return;
        }
        playing = state.playing;
        if (state.playing) {
            draftControlButton.setImageResource(R.drawable.ic_pause);
            draftSeekTime.setText(StringUtils.formatVoiceNoteDuration(getContext(), state.seek));
        } else {
            draftControlButton.setImageResource(R.drawable.ic_play_arrow);
            draftSeekTime.setText(StringUtils.formatVoiceNoteDuration(draftSeekTime.getContext(), state.seekMax));
        }
        draftSeekbar.setMax(state.seekMax);
        draftSeekbar.setProgress(state.seek);
        draftSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                wasPlaying = playing;
                if (playing) {
                    voiceNotePlayer.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (wasPlaying) {
                    voiceNotePlayer.playFile(audioPath, seekBar.getProgress());
                }
            }
        });
    }
}
