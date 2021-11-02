package com.halloapp.calling;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.halloapp.R;
import com.halloapp.util.FileUtils;
import com.halloapp.util.logs.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class OutgoingRingtone {

    @IntDef({Type.RINGING, Type.BUSY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        int RINGING = 0;
        int BUSY = 1;
    }


    private final Context context;
    private MediaPlayer mediaPlayer;

    public OutgoingRingtone(@NonNull Context context) {
        this.context = context;
    }

    public void start(@Type int type) {
        int soundId = getSound(type);

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();

        Uri soundUri = FileUtils.getUriFromResource(context, soundId);

        try {
            mediaPlayer.setAudioAttributes(getAudioAttributes());
            mediaPlayer.setLooping(true);
            mediaPlayer.setDataSource(context, soundUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            Log.e("Failed to play outgoing ringtone" + e.toString());
        }
    }

    public void stop() {
        Log.i("stoping outgoing ringtone");
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private static int getSound(@Type int type) {
        if (type == Type.BUSY) {
            return R.raw.north_american_busy_signal;
        } else if (type == Type.RINGING) {
            return R.raw.us_ringback_tone;
        } else {
            throw new IllegalArgumentException("Invalid OutgoingRingtone.Type " + type);
        }
    }

    private static AudioAttributes getAudioAttributes() {
        return new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING)
                .build();
    }

}
