package com.halloapp.widget;

import android.Manifest;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.halloapp.ContentDraftManager;
import com.halloapp.R;
import com.halloapp.id.ChatId;
import com.halloapp.media.AudioDurationLoader;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.media.VoiceNoteRecorder;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.util.StringUtils;

import java.io.File;

import pub.devrel.easypermissions.EasyPermissions;

public class ChatInputView extends ConstraintLayout {
    private ImageView recordBtn;
    private ImageView sendButton;
    private TextView recordingTime;
    private View deleteVoiceNote;
    private ImageView media;

    private View deleteVoiceDraft;
    private View voiceDraftInfo;

    private TextView draftSeekTime;
    private ImageView draftControlButton;
    private SeekBar draftSeekbar;

    private VoiceNoteRecorderControlView controlView;

    private MentionableEntry editText;

    private File audioDraft;

    private boolean allowVoiceNoteRecording;
    private boolean allowMedia;

    private boolean canSend;

    private InputParent inputParent;

    private VoiceNoteRecorder voiceNoteRecorder;
    private VoiceNotePlayer voiceNotePlayer;

    private boolean playing;
    private boolean wasPlaying;

    public interface InputParent {
        void onSendText();
        void onSendVoiceNote();
        void onSendVoiceDraft(File draft);
        void onChooseMedia();
        void requestVoicePermissions();
    }

    public ChatInputView(Context context) {
        super(context);
        init();
    }

    public ChatInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChatInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setVoiceNoteControlView(@NonNull VoiceNoteRecorderControlView controlView) {
        if (this.controlView != null) {
            throw new RuntimeException("You can only set the control view once");
        }
        if (this.voiceNoteRecorder != null) {
            throw new RuntimeException("You must set the control view before you bind the voice note recorder");
        }
        this.controlView = controlView;
        controlView.setRecordingListener(new VoiceNoteRecorderControlView.RecordingListener() {
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
        inflate(getContext(), R.layout.input_layout, this);
        editText = findViewById(R.id.entry_card);

        recordBtn = findViewById(R.id.record_voice);
        sendButton = findViewById(R.id.send);
        recordingTime = findViewById(R.id.recording_time);
        deleteVoiceNote = findViewById(R.id.cancel_voice_note);

        media = findViewById(R.id.media);

        draftControlButton = findViewById(R.id.control_btn);
        draftSeekbar = findViewById(R.id.voice_note_seekbar);
        draftSeekTime = findViewById(R.id.seek_time);
        deleteVoiceDraft = findViewById(R.id.delete_voice_draft);
        voiceDraftInfo = findViewById(R.id.voice_draft_info);

        sendButton.setOnClickListener(v -> {
            if (inputParent == null) {
                return;
            }
            if (audioDraft != null) {
                inputParent.onSendVoiceDraft(audioDraft);
                audioDraft = null;
            } else if (Boolean.TRUE.equals(voiceNoteRecorder.isLocked().getValue())) {
                inputParent.onSendVoiceNote();
            } else {
                inputParent.onSendText();
            }
            updateEntry();
        });

        recordBtn.setOnTouchListener((v, event) -> {
            final int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                controlView.setVisibility(View.VISIBLE);
                if (!EasyPermissions.hasPermissions(getContext(), Manifest.permission.RECORD_AUDIO)) {
                    if (inputParent != null) {
                        inputParent.requestVoicePermissions();
                    }
                    return false;
                }
                voiceNoteRecorder.record();
            }
            controlView.onTouch(event);
            return true;
        });

        deleteVoiceNote.setOnClickListener(v -> {
            voiceNoteRecorder.finishRecording();
        });

        media.setOnClickListener(v -> {
            if (inputParent != null) {
                inputParent.onChooseMedia();
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

        deleteVoiceDraft.setOnClickListener(v -> {
            audioDraft = null;
            updateEntry();
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
        this.allowVoiceNoteRecording = allow;

        updateEntry();
    }

    public void setAllowMedia(boolean allowMedia) {
        this.allowMedia = allowMedia;

        updateEntry();
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
        sendButton.setVisibility(View.VISIBLE);
        recordBtn.setVisibility(View.INVISIBLE);
        deleteVoiceNote.setVisibility(View.GONE);
        editText.setVisibility(View.INVISIBLE);
        media.setVisibility(View.GONE);
        sendButton.setColorFilter(ContextCompat.getColor(sendButton.getContext(), R.color.color_secondary));
    }

    private void showRecordingLock() {
        voiceDraftInfo.setVisibility(View.GONE);
        deleteVoiceDraft.setVisibility(View.GONE);
        sendButton.setVisibility(View.VISIBLE);
        recordBtn.setVisibility(View.INVISIBLE);
        deleteVoiceNote.setVisibility(View.VISIBLE);
        media.setVisibility(View.GONE);
        sendButton.setColorFilter(ContextCompat.getColor(sendButton.getContext(), R.color.color_secondary));
        editText.setVisibility(View.INVISIBLE);
    }

    private void showEmptyEntry() {
        voiceDraftInfo.setVisibility(View.GONE);
        deleteVoiceDraft.setVisibility(View.GONE);
        deleteVoiceNote.setVisibility(View.GONE);
        editText.setVisibility(View.VISIBLE);

        if (allowVoiceNoteRecording) {
            recordBtn.setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.INVISIBLE);
        } else {
            recordBtn.setVisibility(View.GONE);
            sendButton.setVisibility(View.VISIBLE);
            sendButton.clearColorFilter();
        }
        if (allowMedia) {
            media.setVisibility(View.VISIBLE);
            if (!allowVoiceNoteRecording) {
                media.clearColorFilter();
            } else {
                media.setColorFilter(ContextCompat.getColor(media.getContext(), R.color.color_secondary));
            }
        } else {
            media.setVisibility(View.GONE);
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
        media.setVisibility(View.VISIBLE);
        editText.setVisibility(View.VISIBLE);
        voiceDraftInfo.setVisibility(View.GONE);
        deleteVoiceDraft.setVisibility(View.GONE);
        deleteVoiceNote.setVisibility(View.GONE);

        if (allowVoiceNoteRecording) {
            recordBtn.setVisibility(View.INVISIBLE);
        }
        sendButton.setColorFilter(ContextCompat.getColor(sendButton.getContext(), R.color.color_secondary));
        sendButton.setVisibility(View.VISIBLE);
        if (allowMedia) {
            media.setVisibility(View.VISIBLE);
            media.setColorFilter(ContextCompat.getColor(media.getContext(), R.color.color_secondary));
        } else {
            media.setVisibility(View.GONE);
        }
    }

    private void showRecording() {
        voiceDraftInfo.setVisibility(View.GONE);
        deleteVoiceDraft.setVisibility(View.GONE);
        sendButton.setVisibility(View.INVISIBLE);
        recordBtn.setVisibility(View.VISIBLE);
        deleteVoiceNote.setVisibility(View.GONE);
        media.setVisibility(View.GONE);
        editText.setVisibility(View.INVISIBLE);
    }

    public void bindVoicePlayer(@NonNull LifecycleOwner owner, @NonNull VoiceNotePlayer voiceNotePlayer) {
        this.voiceNotePlayer = voiceNotePlayer;

        voiceNotePlayer.getPlaybackState().observe(owner, this::onDraftPlaybackState);
    }

    public void bindVoiceRecorder(@NonNull LifecycleOwner owner, @NonNull VoiceNoteRecorder voiceNoteRecorder) {
        this.voiceNoteRecorder = voiceNoteRecorder;
        voiceNoteRecorder.isRecording().observe(owner, isRecording -> {
            if (isRecording == null || !isRecording) {
                recordingTime.setVisibility(View.GONE);
                media.setVisibility(View.VISIBLE);
                editText.setVisibility(View.VISIBLE);
            } else {
                editText.setVisibility(View.INVISIBLE);
                recordingTime.setVisibility(View.VISIBLE);
                media.setVisibility(View.GONE);

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
