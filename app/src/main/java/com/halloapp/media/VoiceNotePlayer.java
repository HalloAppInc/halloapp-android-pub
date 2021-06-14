package com.halloapp.media;

import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.util.logs.Log;

import java.io.IOException;

public class VoiceNotePlayer implements SensorEventListener {

    private static final int SEEKBAR_UPDATE_INTERVAL_MS = 100;

    private MediaPlayer mediaPlayer;

    private HandlerThread voiceNoteHandlerThread;

    private Handler voiceNoteHandler;

    private String currentAudio;

    private MutableLiveData<PlaybackState> playbackStateLiveData;

    private PlaybackState playbackState;

    private SensorManager sensorManager;
    private Sensor proximitySensor;

    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;


    public VoiceNotePlayer(@NonNull Application application) {
        sensorManager = (SensorManager) application.getSystemService(Context.SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        powerManager = (PowerManager) application.getSystemService(Context.POWER_SERVICE);

        voiceNoteHandlerThread = new HandlerThread("VoiceNoteHandlerThread");
        voiceNoteHandlerThread.start();

        voiceNoteHandler = new Handler(voiceNoteHandlerThread.getLooper());

        playbackStateLiveData = new MutableLiveData<>();
    }

    private void registerProximityListener() {
        if (proximitySensor == null) {
            return;
        }
        acquireWakeLock();
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_UI);
    }

    private void unregisterProximityListener() {
        sensorManager.unregisterListener(this);
        releaseWakeLock();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] > 5.0f || event.values[0] == event.sensor.getMaximumRange()) {
                onFarProximity();
            } else {
                onEarProximity();
            }
        }
    }

    private void onEarProximity() {
        voiceNoteHandler.post(() -> {
            switchAudioStream(AudioManager.STREAM_VOICE_CALL);
        });
    }

    private void switchAudioStream(int stream) {
        if (mediaPlayer != null && mediaPlayer.isPlaying())  {
            mediaPlayer.pause();
            int seek = mediaPlayer.getCurrentPosition() - 1000;
            seek = Math.max(seek, 0);
            mediaPlayer.stop();
            mediaPlayer.setAudioStreamType(stream);
            try {
                mediaPlayer.prepare();
                mediaPlayer.seekTo(seek);
                mediaPlayer.start();
            } catch (IOException e) {
                Log.e("VoiceNotePlayer/switchAudioStream failed", e);
            }
        }
    }

    private void onFarProximity() {
        voiceNoteHandler.post(() -> {
            switchAudioStream(AudioManager.STREAM_MUSIC);
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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

    private void acquireWakeLock() {
        if (wakeLock != null) {
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
        } else {
            wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "HalloApp:VoiceNoteWakeLock");
            wakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @AnyThread
    public void pause() {
        voiceNoteHandler.post(() -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playbackState.playing = false;
                playbackStateLiveData.postValue(playbackState);
                unregisterProximityListener();
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
                    unregisterProximityListener();
                    releaseWakeLock();
                }
            });
            playbackStateLiveData.postValue(playbackState);
            voiceNoteHandler.removeCallbacks(updatePlayback);

            schedulePlaybackUpdate();
            registerProximityListener();

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
        unregisterProximityListener();
        sensorManager = null;
        proximitySensor = null;
    }
}
