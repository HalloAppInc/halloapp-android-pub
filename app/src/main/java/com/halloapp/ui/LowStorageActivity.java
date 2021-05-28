package com.halloapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import com.halloapp.R;

public class LowStorageActivity extends HalloActivity {

    public static final int MINIMUM_STORAGE_BYTES = 50000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_low_storage);

        Button manageLowStorageBtn = findViewById(R.id.low_storage_btn);
        manageLowStorageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
                startActivity(intent);
            }
        });
    }
}
