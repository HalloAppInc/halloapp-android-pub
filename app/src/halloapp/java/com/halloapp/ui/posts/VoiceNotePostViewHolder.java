package com.halloapp.ui.posts;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.util.StringUtils;

public class VoiceNotePostViewHolder extends PostViewHolder {

    private final SeekBar seekBar;
    private final ImageView controlButton;
    private final TextView seekTime;
    private final ProgressBar loading;

    private boolean playing;

    private String audioPath;
    private boolean wasPlaying;

    private final Observer<VoiceNotePlayer.PlaybackState> playbackStateObserver;


    public VoiceNotePostViewHolder(@NonNull View itemView, @NonNull PostViewHolderParent parent) {
        super(itemView, parent);

        seekBar = itemView.findViewById(R.id.voice_note_seekbar);
        controlButton = itemView.findViewById(R.id.control_btn);
        seekTime = itemView.findViewById(R.id.seek_time);
        loading = itemView.findViewById(R.id.loading);

        controlButton.setOnClickListener(v -> {
            if (playing) {
                parent.getVoiceNotePlayer().pause();
            } else if (audioPath != null) {
                parent.getVoiceNotePlayer().playFile(audioPath, seekBar.getProgress());
            }
        });

        playbackStateObserver = state -> {
            if (state == null || audioPath == null || !audioPath.equals(state.playingTag)) {
                return;
            }
            if (playing != state.playing) {
                playing = state.playing;
            }
            if (playing) {
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
                        parent.getVoiceNotePlayer().pause();
                    }
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (wasPlaying) {
                        parent.getVoiceNotePlayer().playFile(audioPath, seekBar.getProgress());
                    }
                }
            });
        };
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
        audioPath = null;
        playing = false;
        seekBar.setProgress(0);
        controlButton.setImageResource(R.drawable.ic_play_arrow);
    }

    @Override
    public void bindTo(@NonNull Post post) {
        super.bindTo(post);

        if (post.media.size() > 1)  {
            mediaPagerView.setVisibility(View.VISIBLE);
        } else {
            mediaPagerIndicator.setVisibility(View.GONE);
            mediaPagerView.setVisibility(View.GONE);
        }

        if (!post.media.isEmpty()) {
            Media media = post.media.get(0);
            if (media.transferred == Media.TRANSFERRED_YES) {
                if (media.file != null) {
                    String newPath = media.file.getAbsolutePath();
                    if (!newPath.equals(audioPath)) {
                        this.audioPath = media.file.getAbsolutePath();
                        parent.getAudioDurationLoader().load(seekTime, media);
                    }
                }
                loading.setVisibility(View.GONE);
                controlButton.setVisibility(View.VISIBLE);
            } else {
                loading.setVisibility(View.VISIBLE);
                controlButton.setVisibility(View.INVISIBLE);
            }
        }
    }


    private void startObservingPlayback() {
        parent.getVoiceNotePlayer().getPlaybackState().observe(this, playbackStateObserver);
    }

    private void stopObservingPlayback() {
        parent.getVoiceNotePlayer().getPlaybackState().removeObservers(this);
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
        loading.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
}
