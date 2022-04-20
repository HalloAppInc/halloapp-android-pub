package com.halloapp.ui.settings;

import android.os.Bundle;
import android.view.View;

import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.props.ServerProps;
import com.halloapp.util.Preconditions;

import androidx.appcompat.widget.SwitchCompat;

public class SettingsVoiceVideo extends HalloActivity {

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final Preferences preferences = Preferences.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_video);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        if (ServerProps.getInstance().getKrispNoiseSuppression()) {
            View useKrispNoiseSuppression = findViewById(R.id.use_krisp_noise_suppression);

            SwitchCompat useKrispNoiseSuppressionSwitch = findViewById(R.id.use_krisp_noise_suppression_switch);

            bgWorkers.execute(() -> {
                boolean useNoiseSuppression = preferences.getKrispNoiseSuppression();
                useKrispNoiseSuppressionSwitch.post(() -> {
                    useKrispNoiseSuppressionSwitch.setChecked(useNoiseSuppression);
                });
            });

            useKrispNoiseSuppression.setOnClickListener(v -> {
                boolean use = !useKrispNoiseSuppressionSwitch.isChecked();
                useKrispNoiseSuppressionSwitch.setChecked(use);
            });
            useKrispNoiseSuppressionSwitch.setOnCheckedChangeListener((v, checked) -> {
                bgWorkers.execute(() -> {
                    preferences.setKrispNoiseSuppression(checked);
                });
            });
        }
    }
}
