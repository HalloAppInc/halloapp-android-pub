package com.halloapp;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.halloapp.props.ServerProps;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ThreadUtils;
import com.halloapp.util.logs.Log;

@SuppressWarnings("SetTextI18n")
public class DebugActivity extends HalloActivity {

    private final Preferences preferences = Preferences.getInstance();
    private final ServerProps serverProps = ServerProps.getInstance();
    private final BgWorkers bgWorkers = BgWorkers.getInstance();

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
        TextView supportsWideColor = findViewById(R.id.supports_wide_color);
        SwitchCompat switchForceCompactShare = findViewById(R.id.force_compact_share_switch);

        bgWorkers.execute(() -> {
            final boolean forceCompactShare = preferences.getForceCompactShare();
            runOnUiThread(() -> switchForceCompactShare.setChecked(forceCompactShare));
        });

        videoBitrateView.setText(Integer.toString(Constants.VIDEO_BITRATE_OVERRIDE));
        audioBitrateView.setText(Integer.toString(Constants.AUDIO_BITRATE));
        h264ResView.setText(Integer.toString(Constants.VIDEO_RESOLUTION_H264));

        if (Build.VERSION.SDK_INT < 26) {
            supportsWideColor.setText("Wide color not supported: SDK < 26");
        } else if (!getResources().getConfiguration().isScreenWideColorGamut()) {
            supportsWideColor.setText("Wide color not supported: screen not wide color");
        } else if (!getWindowManager().getDefaultDisplay().isWideColorGamut()) {
            supportsWideColor.setText("Wide color not supported: default display not wide color");
        } else {
            getWindow().setColorMode(ActivityInfo.COLOR_MODE_WIDE_COLOR_GAMUT);
            if (getWindow().getColorMode() != ActivityInfo.COLOR_MODE_WIDE_COLOR_GAMUT) {
                supportsWideColor.setText("Failed to set window color mode to wide color");
            } else if (Build.VERSION.SDK_INT >= 27 && !getWindow().isWideColorGamut()) {
                supportsWideColor.setText("Wide color mode set but device does not support wide color");
            } else {
                supportsWideColor.setText("Supports wide color");
            }
        }

        View saveBtn = findViewById(R.id.save);
        saveBtn.setOnClickListener(v -> {
            bgWorkers.execute(() -> {
                int videoBitrate = Constants.VIDEO_BITRATE_OVERRIDE;
                int audioBitrate = Constants.AUDIO_BITRATE;
                int h264Res = Constants.VIDEO_RESOLUTION_H264;
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
                Constants.VIDEO_BITRATE_OVERRIDE = videoBitrate;
                Constants.AUDIO_BITRATE = audioBitrate;
                Constants.VIDEO_RESOLUTION_H264 = h264Res;
                preferences.saveVideoOverride();
                preferences.setForceCompactShare(switchForceCompactShare.isChecked());
                finish();
            });
        });

        View resetButton = findViewById(R.id.reset);
        resetButton.setOnClickListener(v -> {
            bgWorkers.execute(() -> {
                Constants.VIDEO_BITRATE_OVERRIDE = 0;
                Constants.AUDIO_BITRATE = 96000;
                Constants.VIDEO_RESOLUTION_H264 = 360;
                preferences.resetVideoOverride();
                preferences.setForceCompactShare(false);
                finish();
            });
        });
    }

}
