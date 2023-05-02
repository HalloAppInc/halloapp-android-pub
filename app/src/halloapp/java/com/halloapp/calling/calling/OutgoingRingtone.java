package com.halloapp.calling.calling;

import android.media.AudioTrack;
import android.util.JsonReader;

import androidx.annotation.IntDef;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.halloapp.AppContext;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.util.ToneUtils;
import com.halloapp.util.logs.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class OutgoingRingtone {

    public class ToneSpec {
        public double[] freqHz;
        public double[] pattern;

        ToneSpec(double[] freqHz, double[] pattern) {
            this.freqHz = freqHz;
            this.pattern = pattern;
        }

        public String toString() {
            return "ToneSpec(freq=" + Arrays.toString(freqHz) + ", pattern:" + Arrays.toString(pattern);
        }
    }

    // Data comes from here: https://www.ietf.org/archive/id/draft-roach-voip-ringtone-00.txt
    // is stored as json here res/raw/tone_specs.json
    private static final Map<String, ToneSpec> ringtoneMap = new HashMap<>();

    @IntDef({Type.RINGING, Type.BUSY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        int RINGING = 0;
        int BUSY = 1;
    }


    private AudioTrack audioTrack;

    public OutgoingRingtone() {
    }

    private void initRingtoneMap() {
        if (!ringtoneMap.isEmpty()) {
            return;
        }
        Log.i("OutgoingRingtone.initRingtoneMap");
        InputStream is = AppContext.getInstance().get().getResources().openRawResource(R.raw.tone_specs);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            String jsonString = writer.toString();
            JsonObject obj = (JsonObject) JsonParser.parseString(jsonString);
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                JsonArray freqs = (JsonArray)((JsonObject)entry.getValue()).get("freq");
                double[] freqs2 = new double[freqs.size()];
                for (int i = 0; i < freqs.size(); i++) {
                    freqs2[i] = freqs.get(i).getAsDouble();
                }

                JsonArray pattern = (JsonArray)((JsonObject)entry.getValue()).get("pattern");
                double[] pattern2 = new double[pattern.size()];
                for (int i = 0; i < pattern.size(); i++) {
                    pattern2[i] = pattern.get(i).getAsDouble();
                }
                ringtoneMap.put(entry.getKey(), new ToneSpec(freqs2, pattern2));
            }
        } catch (IOException e) {
            Log.e("Failed to load json file", e);
        } finally {
            try {
                is.close();
                Log.i("RingtoneMap size: " + ringtoneMap.size());
            } catch (IOException e) {
                Log.e("Close failed", e);
            }
        }
    }

    public void start(@Type int type, String peerCC) {
        if (type != Type.RINGING) {
            Log.e("OutgoingRingtone.start Not implemented");
            return;
        }
        initRingtoneMap();

        ToneSpec ringtoneSpec = ringtoneMap.get(peerCC);
        if (ringtoneSpec == null) {
            ringtoneSpec = ringtoneMap.get("US");
        }

        Log.i("OutgoingRingtone: will play " + ringtoneSpec.toString());
        audioTrack = ToneUtils.generateTone(ringtoneSpec.freqHz, ringtoneSpec.pattern, Constants.CALL_RINGING_TIMEOUT_MS);
        audioTrack.play();
    }

    public void stop() {
        Log.i("OutgoingRingtone: stopping outgoing ringtone");
        if (audioTrack != null) {
            audioTrack.pause();
            audioTrack.flush();
            audioTrack.release();
            audioTrack = null;
        }
    }
}
