package com.halloapp.ui.chat;

import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.media.AudioDurationLoader;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.util.StringUtils;

public class VoiceNoteMessageViewHolder extends MessageViewHolder {

    private AudioDurationLoader audioDurationLoader;

    private SeekBar seekBar;
    private ImageView controlButton;
    private ImageView avatarView;
    private TextView seekTime;
    private View loading;

    private VoiceNotePlayer voiceNotePlayer;

    private boolean playing;
    private boolean wasPlaying;

    private String audioPath;

    VoiceNoteMessageViewHolder(@NonNull View itemView, @NonNull MessageViewHolderParent parent) {
        super(itemView, parent);

        seekBar = itemView.findViewById(R.id.voice_note_seekbar);
        controlButton = itemView.findViewById(R.id.control_btn);
        avatarView = itemView.findViewById(R.id.ptt_avatar);
        seekTime = itemView.findViewById(R.id.seek_time);
        loading = itemView.findViewById(R.id.loading);

        voiceNotePlayer = parent.getVoiceNotePlayer();
        audioDurationLoader = parent.getAudioDurationLoader();

        controlButton.setOnClickListener(v -> {
            if (playing) {
                voiceNotePlayer.pause();
            } else if (audioPath != null) {
                voiceNotePlayer.playFile(audioPath, seekBar.getProgress());
            }
        });
    }

    private void startObservingPlayback() {
        parent.getVoiceNotePlayer().getPlaybackState().observe(this, state -> {
            if (state == null || audioPath == null || !audioPath.equals(state.playingTag)) {
                return;
            }
            playing = state.playing;
            if (state.playing) {
                controlButton.setImageResource(R.drawable.ic_pause);
                seekTime.setText(StringUtils.formatVoiceNoteDuration(seekTime.getContext(), state.seek));
            } else {
                controlButton.setImageResource(R.drawable.ic_play_arrow);
                seekTime.setText(StringUtils.formatVoiceNoteDuration(seekTime.getContext(), state.seekMax));
            }
            seekBar.setMax(state.seekMax);
            seekBar.setProgress(state.seek);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        });
    }

    @Override
    protected void fillView(@NonNull Message message, boolean changed) {
        if (changed) {
            this.audioPath = null;
            parent.getAvatarLoader().load(avatarView, message.senderUserId, false);
        }
        if (message.media != null && !message.media.isEmpty()) {
            Media media = message.media.get(0);
            if (media.transferred == Media.TRANSFERRED_YES) {
                if (media.file != null) {
                    String newPath = media.file.getAbsolutePath();
                    if (!newPath.equals(audioPath)) {
                        this.audioPath = media.file.getAbsolutePath();
                        audioDurationLoader.load(seekTime, media);
                        startObservingPlayback();
                    }
                }
                controlButton.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);
            } else {
                controlButton.setVisibility(View.INVISIBLE);
                loading.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void markRecycled() {
        super.markRecycled();
        audioPath = null;
    }
}
