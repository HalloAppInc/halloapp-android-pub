package com.halloapp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.halloapp.props.ServerProps;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.logs.Log;

public class DebugActivity extends HalloActivity {

    private final Preferences preferences = Preferences.getInstance();
    private final ServerProps serverProps = ServerProps.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!serverProps.getIsInternalUser() && !BuildConfig.DEBUG) {
            finish();
        }

        setContentView(R.layout.activity_debug);

        EditText videoBitrateView = findViewById(R.id.video_bitrate);
        EditText audioBitrateView = findViewById(R.id.audio_bitrate);
        EditText h264ResView = findViewById(R.id.h264_res);
        EditText h265ResView = findViewById(R.id.h265_res);

        videoBitrateView.setText(Integer.toString(Constants.VIDEO_BITRATE));
        audioBitrateView.setText(Integer.toString(Constants.AUDIO_BITRATE));
        h264ResView.setText(Integer.toString(Constants.VIDEO_RESOLUTION_H264));
        h265ResView.setText(Integer.toString(Constants.VIDEO_RESOLUTION_H265));

        View saveBtn = findViewById(R.id.save);
        saveBtn.setOnClickListener(v -> {
            int videoBitrate = Constants.VIDEO_BITRATE;
            int audioBitrate = Constants.AUDIO_BITRATE;
            int h264Res = Constants.VIDEO_RESOLUTION_H264;
            int h265Res = Constants.VIDEO_RESOLUTION_H265;
            try {
                videoBitrate = Integer.parseInt(videoBitrateView.getText().toString());
            } catch (Exception e) {
                Log.e("Failed to update video bitrate");
            }
            try {
                audioBitrate = Integer.parseInt(audioBitrateView.getText().toString());
            } catch (Exception e) {
                Log.e("Failed to update audio bitrate");
            }
            try {
                h264Res = Integer.parseInt(h264ResView.getText().toString());
            } catch (Exception e) {
                Log.e("Failed to update h264 resolution");
            }
            try {
                h265Res = Integer.parseInt(h265ResView.getText().toString());
            } catch (Exception e) {
                Log.e("Failed to update h265 resolution");
            }
            Constants.VIDEO_BITRATE = videoBitrate;
            Constants.AUDIO_BITRATE = audioBitrate;
            Constants.VIDEO_RESOLUTION_H264 = h264Res;
            Constants.VIDEO_RESOLUTION_H265 = h265Res;
            preferences.saveVideoOverride();
            finish();
        });

        View resetButton = findViewById(R.id.reset);
        resetButton.setOnClickListener(v -> {
            Constants.VIDEO_BITRATE = 2000000;
            Constants.AUDIO_BITRATE = 96000;
            Constants.VIDEO_RESOLUTION_H264 = 360;
            Constants.VIDEO_RESOLUTION_H265 = 480;

            preferences.resetVideoOverride();
            finish();
        });
    }

}
