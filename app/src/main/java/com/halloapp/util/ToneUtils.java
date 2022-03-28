package com.halloapp.util;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.halloapp.Constants;

public class ToneUtils {

    public static final int SAMPLE_RATE = 44100;
    public static final short SHORT_MAX = 0x7FFF;

    public static AudioTrack generateTone(double freqHz, double[] pattern) {
        return generateTone(new double[]{freqHz}, pattern);
    }

    public static AudioTrack generateTone(double[] freqHz, double[] pattern) {
        return generateTone(freqHz, pattern, 0);
    }

    public static AudioTrack generateTone(double[] freqHz, double[] pattern, int durationMs) {
        double totalDuration = 0;
        for (double p : pattern) {
            totalDuration += p;
        }

        int count = (int)(SAMPLE_RATE * totalDuration);
        short[] samples = new short[count];
        boolean on = true;
        int i = 0;
        short sample;
        for (double duration : pattern) {
            int samplesInDuration = (int)(duration * SAMPLE_RATE);
            for (int j = 0; j < samplesInDuration; j++) {
                sample = 0;
                if (on) {
                    for (double f : freqHz) {
                        double wave = Math.sin(2 * Math.PI * j / (((double) SAMPLE_RATE) / f));
                        short scaledWave = ((short) (wave * SHORT_MAX / freqHz.length));
                        sample += scaledWave;
                    }
                }
                samples[i++] = sample;
            }
            on = !on;
        }
        AudioTrack track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                count * (Short.SIZE / 8), AudioTrack.MODE_STATIC);
        track.write(samples, 0, count);
        int loops;
        if (durationMs < 0) {
            loops = -1;
        } else if (durationMs == 0) {
            loops = 0;
        } else {
            loops = (1 + durationMs) / (int)(totalDuration * 1000);
        }
        track.setLoopPoints(0, count, loops);
        return track;
    }
}
