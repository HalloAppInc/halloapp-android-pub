package com.halloapp.widget;

import android.Manifest;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.halloapp.R;
import com.halloapp.media.AudioDurationLoader;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.media.VoiceNoteRecorder;
import com.halloapp.ui.UrlPreviewTextWatcher;
import com.halloapp.util.StringUtils;

import java.io.File;

import pub.devrel.easypermissions.EasyPermissions;

public class PostEntryView extends FrameLayout {
    private ImageView recordBtn;
    private TextView recordingTime;
    private View deleteVoiceNote;

    private View deleteVoiceDraft;
    private View voiceDraftInfo;
    private View recordingIndicator;

    private TextView draftSeekTime;
    private ImageView draftControlButton;
    private SeekBar draftSeekbar;

    private VoicePostRecorderControlView controlView;

    private MentionableEntry editText;

    private File audioDraft;

    private boolean allowVoiceNoteRecording;

    private boolean canSend;

    private InputParent inputParent;

    private VoiceNoteRecorder voiceNoteRecorder;
    private VoiceNotePlayer voiceNotePlayer;

    private boolean playing;
    private boolean wasPlaying;

    public View getRecordingTimeView() {
        return recordingTime;
    }

    public interface InputParent {
        void onSendVoiceNote();
        void onDeleteVoiceDraft();
        void requestVoicePermissions();
        void onUrl(String url);
    }

    public PostEntryView(Context context) {
        super(context);
        init();
    }

    public PostEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PostEntryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setVoiceNoteControlView(@NonNull VoicePostRecorderControlView controlView) {
        if (this.controlView != null) {
            throw new RuntimeException("You can only set the control view once");
        }
        if (this.voiceNoteRecorder != null) {
            throw new RuntimeException("You must set the control view before you bind the voice note recorder");
        }
        this.controlView = controlView;
        controlView.setRecordingListener(new VoicePostRecorderControlView.RecordingListener() {
            @Override
            public void onCancel() {
                voiceNoteRecorder.finishRecording();
                controlView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onSend() {
                if (inputParent != null) {
                    inputParent.onSendVoiceNote();
                    controlView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onLock() {
                voiceNoteRecorder.lockRecording();
                controlView.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void setInputParent(@Nullable InputParent inputParent) {
        this.inputParent = inputParent;
    }

    private void init() {
        inflate(getContext(), R.layout.post_input_layout, this);
        editText = findViewById(R.id.entry_bottom);

        recordBtn = findViewById(R.id.record_voice);
        recordingTime = findViewById(R.id.recording_time);
        deleteVoiceNote = findViewById(R.id.cancel_voice_note);

        draftControlButton = findViewById(R.id.control_btn);
        draftSeekbar = findViewById(R.id.voice_note_seekbar);
        draftSeekTime = findViewById(R.id.seek_time);
        deleteVoiceDraft = findViewById(R.id.delete_voice_draft);
        voiceDraftInfo = findViewById(R.id.voice_draft_info);
        recordingIndicator = findViewById(R.id.recording_indicator);

        recordBtn.setOnTouchListener((v, event) -> {
            final int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                if (!EasyPermissions.hasPermissions(getContext(), Manifest.permission.RECORD_AUDIO)) {
                    if (inputParent != null) {
                        inputParent.requestVoicePermissions();
                    }
                    return false;
                }
                controlView.setVisibility(View.VISIBLE);
                voiceNoteRecorder.record();
            }
            controlView.onTouch(event);
            return true;
        });

        deleteVoiceNote.setOnClickListener(v -> {
            if (inputParent != null) {
                inputParent.onSendVoiceNote();
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateEntry();
            }
        });

        editText.addTextChangedListener(new UrlPreviewTextWatcher(url -> {
            if (inputParent != null) {
                inputParent.onUrl(url);
            }
        }));

        deleteVoiceDraft.setOnClickListener(v -> {
            inputParent.onDeleteVoiceDraft();
            if (playing) {
                voiceNotePlayer.pause();
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

    public void setAllowVoiceNoteRecording(boolean allow) {
        if (this.allowVoiceNoteRecording != allow) {
            this.allowVoiceNoteRecording = allow;
            updateEntry();
        }
    }
    
    public void setCanSend(boolean canSend) {
        this.canSend = canSend;

        updateEntry();
    }

    private void updateEntry() {
        boolean isRecording = false;
        boolean isLocked = false;

        if (voiceNoteRecorder != null) {
            isRecording = Boolean.TRUE.equals(voiceNoteRecorder.isRecording().getValue());
            isLocked = isRecording && Boolean.TRUE.equals(voiceNoteRecorder.isLocked().getValue());
        }

        if (audioDraft != null) {
            showAudioDraft();
        } else if (isRecording) {
            if (isLocked) {
                showRecordingLock();
            } else {
                showRecording();
            }
        } else {
            if (!canSend) {
                showEmptyEntry();
            } else {
                showTextEntry();
            }
        }
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

    public void bindAudioDraft(AudioDurationLoader audioDurationLoader, File audioDraft) {
        this.audioDraft = audioDraft;
        if (audioDraft != null) {
            audioDurationLoader.load(draftSeekTime, audioDraft);
        }
        updateEntry();
    }

    private void showAudioDraft() {
        voiceDraftInfo.setVisibility(View.VISIBLE);
        deleteVoiceDraft.setVisibility(View.VISIBLE);
        recordBtn.setVisibility(View.INVISIBLE);
        deleteVoiceNote.setVisibility(View.GONE);
        editText.setVisibility(View.INVISIBLE);
    }

    private void showRecordingLock() {
        voiceDraftInfo.setVisibility(View.GONE);
        deleteVoiceDraft.setVisibility(View.GONE);
        recordBtn.setVisibility(View.INVISIBLE);
        deleteVoiceNote.setVisibility(View.VISIBLE);
        editText.setVisibility(View.INVISIBLE);
    }

    private void showEmptyEntry() {
        voiceDraftInfo.setVisibility(View.GONE);
        deleteVoiceDraft.setVisibility(View.GONE);
        deleteVoiceNote.setVisibility(View.GONE);
        editText.setVisibility(View.VISIBLE);

        if (allowVoiceNoteRecording) {
            recordBtn.setVisibility(View.VISIBLE);
        } else {
            recordBtn.setVisibility(View.GONE);
        }
    }

    public File getAudioDraft() {
        if (voiceNoteRecorder == null) {
            return null;
        }
        File draft = voiceNoteRecorder.finishRecording();
        if (draft != null) {
            return draft;
        }
        return audioDraft;
    }

    private void showTextEntry() {
        editText.setVisibility(View.VISIBLE);
        voiceDraftInfo.setVisibility(View.GONE);
        deleteVoiceDraft.setVisibility(View.GONE);
        deleteVoiceNote.setVisibility(View.GONE);

        if (allowVoiceNoteRecording) {
            recordBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void showRecording() {
        voiceDraftInfo.setVisibility(View.GONE);
        deleteVoiceDraft.setVisibility(View.GONE);
        recordBtn.setVisibility(View.VISIBLE);
        deleteVoiceNote.setVisibility(View.GONE);
        editText.setVisibility(View.INVISIBLE);
    }

    public void bindVoicePlayer(@NonNull LifecycleOwner owner, @NonNull VoiceNotePlayer voiceNotePlayer) {
        this.voiceNotePlayer = voiceNotePlayer;

        voiceNotePlayer.getPlaybackState().observe(owner, this::onDraftPlaybackState);
    }

    public void bindVoiceRecorder(@NonNull LifecycleOwner owner, @NonNull VoiceNoteRecorder voiceNoteRecorder) {
        this.voiceNoteRecorder = voiceNoteRecorder;
        voiceNoteRecorder.isRecording().observe(owner, isRecording -> {
            if (getVisibility() != View.VISIBLE) {
                return;
            }
            if (isRecording == null || !isRecording) {
                recordingTime.setVisibility(View.GONE);
                recordingIndicator.setVisibility(View.GONE);
                if (recordingIndicator.getAnimation() != null) {
                    recordingIndicator.clearAnimation();
                }
                editText.setVisibility(View.VISIBLE);
            } else {
                editText.setVisibility(View.INVISIBLE);
                recordingTime.setVisibility(View.VISIBLE);
                recordingIndicator.setVisibility(View.VISIBLE);
                if (recordingIndicator.getAnimation() == null) {
                    Animation anim = new AlphaAnimation(0.0f, 1.0f);
                    anim.setDuration(500);
                    anim.setRepeatMode(Animation.REVERSE);
                    anim.setRepeatCount(Animation.INFINITE);
                    recordingIndicator.startAnimation(anim);
                }
                controlView.setVisibility(View.VISIBLE);
            }
            updateEntry();
        });

        voiceNoteRecorder.isLocked().observe(owner, isLocked -> {
            updateEntry();
        });

        voiceNoteRecorder.getRecordingTime().observe(owner, millis -> {
            if (millis == null) {
                return;
            }
            recordingTime.setText(StringUtils.formatVoiceNoteDuration(getContext(), millis));
        });

        controlView.bindAmplitude(owner, voiceNoteRecorder.getRecordingAmplitude());
    }
}
