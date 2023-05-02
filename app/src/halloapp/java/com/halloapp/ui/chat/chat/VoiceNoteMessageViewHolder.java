package com.halloapp.ui.chat.chat;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.media.AudioDurationLoader;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.util.StringUtils;

public class VoiceNoteMessageViewHolder extends MessageViewHolder {

    private final AudioDurationLoader audioDurationLoader;

    private final SeekBar seekBar;
    private final ImageView controlButton;
    private final ImageView avatarView;
    private final ImageView playedStatus;
    private final TextView seekTime;
    private final View loading;

    private final VoiceNotePlayer voiceNotePlayer;

    private boolean playing;
    private boolean wasPlaying;
    private boolean played;

    private String audioPath;

    private final Observer<VoiceNotePlayer.PlaybackState> playbackStateObserver;

    VoiceNoteMessageViewHolder(@NonNull View itemView, @NonNull MessageViewHolderParent parent) {
        super(itemView, parent);

        seekBar = itemView.findViewById(R.id.voice_note_seekbar);
        controlButton = itemView.findViewById(R.id.control_btn);
        avatarView = itemView.findViewById(R.id.ptt_avatar);
        seekTime = itemView.findViewById(R.id.seek_time);
        loading = itemView.findViewById(R.id.loading);

        playedStatus = itemView.findViewById(R.id.played_status);

        voiceNotePlayer = parent.getVoiceNotePlayer();
        audioDurationLoader = parent.getAudioDurationLoader();

        controlButton.setOnClickListener(v -> {
            if (playing) {
                voiceNotePlayer.pause();
            } else if (audioPath != null) {
                voiceNotePlayer.playFile(audioPath, seekBar.getProgress());
            }
        });

        playbackStateObserver = state -> {
            if (state == null || audioPath == null || !audioPath.equals(state.playingTag)) {
                return;
            }
            if (playing != state.playing) {
                playing = state.playing;
                updateVoiceNoteTint(true);
            }
            if (state.playing) {
                controlButton.setImageResource(R.drawable.ic_pause);
                seekTime.setText(StringUtils.formatVoiceNoteDuration(seekTime.getContext(), state.seek));
                if (message.isIncoming() && message.state != Message.STATE_INCOMING_PLAYED) {
                    played = true;
                    updateVoiceNoteTint(true);
                    ContentDb.getInstance().setMessagePlayed(message.chatId, message.senderUserId, message.id);
                }
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
        };
    }

    private void startObservingPlayback() {
        parent.getVoiceNotePlayer().getPlaybackState().observe(this, playbackStateObserver);
    }

    private void stopObservingPlayback() {
        parent.getVoiceNotePlayer().getPlaybackState().removeObservers(this);
    }

    @Override
    protected void fillView(@NonNull Message message, boolean changed) {
        if (changed) {
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
                    }
                }
                controlButton.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);
            } else {
                controlButton.setVisibility(View.INVISIBLE);
                loading.setVisibility(View.VISIBLE);
            }
        }
        if (playedStatus != null) {
            @ColorRes int statusColor;
            if (message.state == Message.STATE_OUTGOING_PLAYED) {
                statusColor = R.color.message_state_read;
            } else {
                statusColor = R.color.message_state_sent;
            }
            playedStatus.setColorFilter(ContextCompat.getColor(playedStatus.getContext(), statusColor), PorterDuff.Mode.SRC_IN);
        }
        updateVoiceNoteTint(message.isOutgoing() || message.state == Message.STATE_INCOMING_PLAYED || played);
    }

    private void updateVoiceNoteTint(boolean wasPlayed) {
        @ColorInt int color;
        if (wasPlayed) {
            color = ContextCompat.getColor(controlButton.getContext(), R.color.voice_note_played);
        } else {
            color = ContextCompat.getColor(controlButton.getContext(), R.color.color_secondary);
        }
        controlButton.setImageTintList(ColorStateList.valueOf(color));
        if (playing) {
            seekBar.getThumb().clearColorFilter();
        } else {
            seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public void markAttach() {
        super.markAttach();
        startObservingPlayback();
    }

    @Override
    public void markDetach() {
        super.markDetach();
        stopObservingPlayback();
    }

    @Override
    public void markRecycled() {
        super.markRecycled();
        this.audioPath = null;
        playing = false;
        played = false;
        seekBar.setProgress(0);
        controlButton.setImageResource(R.drawable.ic_play_arrow);
    }
}
