package com.halloapp.media;

import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.FileStore;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.IOException;

public class VoiceNoteRecorder {

    private MediaRecorder mediaRecorder;

    private final FileStore fileStore = FileStore.getInstance();

    private int state;

    private static final int VOICE_RECORDING_BIT_RATE = 64 * 1024;

    private static final int STATE_NOT_READY = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_READY_TO_RECORD = 2;
    private static final int STATE_RECORDING = 3;

    private boolean startRecordingOnPrepare = false;

    private File recordingLocation;

    private final MutableLiveData<Boolean> isRecording;
    private final MutableLiveData<Long> recordingDuration = new MutableLiveData<>();

    private Handler recorderHandler;
    private HandlerThread recorderThread;

    private long recordStartTime;

    public VoiceNoteRecorder() {
        isRecording = new MutableLiveData<>(false);

        recorderThread = new HandlerThread("VoiceNoteRecorder");
        recorderThread.start();

        recorderHandler = new Handler(recorderThread.getLooper());
    }

    private final Runnable updatePlayback = () -> {
        recordingDuration.postValue(System.currentTimeMillis() - recordStartTime);
        if (state == STATE_RECORDING) {
            schedulePlaybackUpdate();
        }
    };

    private void schedulePlaybackUpdate() {
        recorderHandler.postDelayed(updatePlayback, 100);
    }

    @UiThread
    private synchronized void initialize() {
        if (state != STATE_PREPARING) {
            state = STATE_PREPARING;
        } else {
            Log.w("VoiceNoteRecorder/initialize already initializing");
            return;
        }
        if (mediaRecorder != null) {
            mediaRecorder.reset();
        } else {
            mediaRecorder = new MediaRecorder();
        }
        recordingLocation = fileStore.getTmpFile(RandomId.create() + ".aac");
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
        mediaRecorder.setOutputFile(recordingLocation.getPath());
        mediaRecorder.setAudioEncodingBitRate(VOICE_RECORDING_BIT_RATE);
        recorderHandler.post(() -> {
            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                Log.e("VoiceNoteRecorder/record failed to prepare recorder", e);
                synchronized (VoiceNoteRecorder.this) {
                    state = STATE_NOT_READY;
                }
                return;
            }
            synchronized (VoiceNoteRecorder.this) {
                state = STATE_READY_TO_RECORD;
                if (startRecordingOnPrepare) {
                    startRecordingOnPrepare = false;
                    startRecording();
                }
            }
        });
    }

    private void startRecording() {
        mediaRecorder.start();
        recordStartTime = System.currentTimeMillis();
        updatePlayback.run();
        state = STATE_RECORDING;
        isRecording.postValue(true);
        schedulePlaybackUpdate();
    }

    public void record() {
        if (state != STATE_READY_TO_RECORD) {
            startRecordingOnPrepare = true;
            initialize();
            return;
        }
        startRecording();
    }

    public LiveData<Boolean> isRecording() {
        return isRecording;
    }

    public LiveData<Long> getRecordingTime() {
        return recordingDuration;
    }

    @Nullable
    public File finishRecording() {
        if (state != STATE_RECORDING) {
            Log.w("VoiceNoteRecording/finishRecording not currently recording");
            return null;
        }
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;
        state = STATE_NOT_READY;
        isRecording.postValue(false);
        recorderHandler.removeCallbacks(updatePlayback);
        return recordingLocation;
    }

    public void onCleared() {
        recorderThread.quit();
        recorderThread = null;
        recorderHandler = null;
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }
}
