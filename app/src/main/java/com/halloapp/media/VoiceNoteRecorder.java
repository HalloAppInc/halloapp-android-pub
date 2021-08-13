package com.halloapp.media;

import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.AppContext;
import com.halloapp.FileStore;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.RandomId;
import com.halloapp.util.VibrationUtils;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.IOException;

public class VoiceNoteRecorder {

    private MediaRecorder mediaRecorder;

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final FileStore fileStore = FileStore.getInstance();
    private final AppContext appContext = AppContext.getInstance();

    private int state;

    private static final int VOICE_RECORDING_BIT_RATE = 64 * 1024;
    private static final int VOICE_RECORDING_SAMPLE_RATE = 48_000; //in cycles per second

    private static final int STATE_NOT_READY = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_READY_TO_RECORD = 2;
    private static final int STATE_RECORDING = 3;

    private boolean startRecordingOnPrepare = false;

    private File recordingLocation;

    private final MutableLiveData<Boolean> isRecording;
    private final MutableLiveData<Boolean> isLocked;
    private final MutableLiveData<Long> recordingDuration = new MutableLiveData<>();
    private final MutableLiveData<Integer> recordingAmplitude = new MutableLiveData<>();

    private Handler recorderHandler;
    private HandlerThread recorderThread;

    private long recordStartTime;

    public VoiceNoteRecorder() {
        isRecording = new MutableLiveData<>(false);
        isLocked = new MutableLiveData<>(false);

        recorderThread = new HandlerThread("VoiceNoteRecorder");
        recorderThread.start();

        recorderHandler = new Handler(recorderThread.getLooper());
    }

    private int skip = 0;

    private final Runnable updatePlayback = () -> {
        recordingDuration.postValue(System.currentTimeMillis() - recordStartTime);
        if (state == STATE_RECORDING) {
            synchronized (VoiceNoteRecorder.this) {
                if (state == STATE_RECORDING) {
                    skip++;
                    if (skip >= 2) {
                        int lastAmp = mediaRecorder.getMaxAmplitude();
                        skip = 0;
                        recordingAmplitude.postValue(lastAmp);
                    }
                }
            }
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
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(VOICE_RECORDING_BIT_RATE);
        mediaRecorder.setAudioSamplingRate(VOICE_RECORDING_SAMPLE_RATE);
        mediaRecorder.setOutputFile(recordingLocation.getPath());
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

    public void lockRecording() {
        isLocked.postValue(true);
    }

    private void startRecording() {
        VibrationUtils.quickVibration(appContext.get());
        state = STATE_RECORDING;
        mediaRecorder.start();
        recordStartTime = System.currentTimeMillis();
        updatePlayback.run();
        isRecording.postValue(true);
        isLocked.postValue(false);
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

    public LiveData<Boolean> isLocked() {
        return isLocked;
    }

    public LiveData<Long> getRecordingTime() {
        return recordingDuration;
    }

    public LiveData<Integer> getRecordingAmplitude() {
        return recordingAmplitude;
    }

    @Nullable
    public synchronized File finishRecording() {
        if (state != STATE_RECORDING) {
            startRecordingOnPrepare = false;
            if (state == STATE_PREPARING) {
                recorderHandler.post(this::finishRecording);
            }
            Log.w("VoiceNoteRecording/finishRecording not currently recording");
            return null;
        }
        state = STATE_NOT_READY;
        RuntimeException error = null;
        try {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
        } catch (RuntimeException e) {
            Log.e("VoiceNoteRecorder/finishRecording failed to finish");
            error = e;
        }
        VibrationUtils.quickVibration(appContext.get());
        mediaRecorder = null;
        isRecording.postValue(false);
        isLocked.postValue(false);
        recorderHandler.removeCallbacks(updatePlayback);
        if (error != null) {
            final File loc = recordingLocation;
            recordingLocation = null;
            bgWorkers.execute(() -> {
                if (loc == null) {
                    return;
                }
                if (loc.exists()) {
                    if (loc.delete()) {
                        Log.i("VoiceNoteRecorder/finishRecording tmp file successfully deleted");
                    } else {
                        Log.e("VoiceNoteRecorder/finishRecording tmp failed to delete");
                    }
                }
            });
        }
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
