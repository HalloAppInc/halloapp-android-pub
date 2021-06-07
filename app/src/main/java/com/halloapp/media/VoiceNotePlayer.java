package com.halloapp.media;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.util.logs.Log;

import java.io.IOException;

public class VoiceNotePlayer {

    private static final int SEEKBAR_UPDATE_INTERVAL_MS = 100;

    private MediaPlayer mediaPlayer;

    private HandlerThread voiceNoteHandlerThread;

    private Handler voiceNoteHandler;

    private String currentAudio;

    private MutableLiveData<PlaybackState> playbackStateLiveData;

    private PlaybackState playbackState;


    public VoiceNotePlayer() {
        voiceNoteHandlerThread = new HandlerThread("VoiceNoteHandlerThread");
        voiceNoteHandlerThread.start();

        voiceNoteHandler = new Handler(voiceNoteHandlerThread.getLooper());

        playbackStateLiveData = new MutableLiveData<>();
    }

    public static class PlaybackState {
        public boolean playing;
        public String playingTag;
        public int seek;
        public int seekMax;
    }

    public LiveData<PlaybackState> getPlaybackState() {
        return playbackStateLiveData;
    }

    private final Runnable updatePlayback = () -> {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            playbackState.seek = mediaPlayer.getCurrentPosition();
            playbackStateLiveData.postValue(playbackState);
            schedulePlaybackUpdate();
        }
    };

    private void schedulePlaybackUpdate() {
        voiceNoteHandler.postDelayed(updatePlayback, SEEKBAR_UPDATE_INTERVAL_MS);
    }

    @AnyThread
    public void pause() {
        voiceNoteHandler.post(() -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playbackState.playing = false;
                playbackStateLiveData.postValue(playbackState);
            }
        });
    }

    @AnyThread
    public void playFile(final @NonNull String absFilePath, int seekTo) {
        voiceNoteHandler.post(() -> {
            if (playbackState == null) {
                playbackState = new PlaybackState();
            }
            if (!absFilePath.equals(currentAudio)) {
                if (mediaPlayer != null) {
                    mediaPlayer.reset();
                    playbackState.playing = false;
                    playbackState.seek = seekTo;
                    playbackStateLiveData.postValue(playbackState);
                } else {
                    mediaPlayer = new MediaPlayer();
                }
                try {
                    mediaPlayer.setDataSource(absFilePath);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    Log.e("VoiceNotePlayer/playFile failed to prepare " + absFilePath, e);
                    return;
                }
                playbackState.seek = seekTo;
            } else {
                playbackState.seek = seekTo;
            }
            playbackState.seekMax = mediaPlayer.getDuration();
            playbackState.playing = true;
            playbackState.playingTag = absFilePath;
            mediaPlayer.seekTo(seekTo);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> {
                if (absFilePath.equals(currentAudio)) {
                    playbackState.playing = false;
                    playbackState.seek = 0;
                    playbackStateLiveData.postValue(playbackState);
                }
            });
            playbackStateLiveData.postValue(playbackState);
            voiceNoteHandler.removeCallbacks(updatePlayback);

            schedulePlaybackUpdate();

            currentAudio = absFilePath;
        });
    }

    public void onCleared() {
        voiceNoteHandlerThread.quit();
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
