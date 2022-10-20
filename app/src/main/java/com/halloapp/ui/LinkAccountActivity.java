package com.halloapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;
import com.halloapp.R;
import com.halloapp.util.Preconditions;

import java.nio.charset.StandardCharsets;

public class LinkAccountActivity extends HalloActivity {
    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_link_account);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        View connectButton = findViewById(R.id.connect_button);

        connectButton.setOnClickListener(v -> {
            Intent intent = new IntentIntegrator(this)
                    .setPrompt("")
                    .setBeepEnabled(false)
                    .createScanIntent();
            intent.putExtra(Intents.Scan.MODE, Intents.Scan.QR_CODE_MODE);
            intent.putExtra(Intents.Scan.CHARACTER_SET, StandardCharsets.ISO_8859_1.name());
            startActivityForResult(intent, IntentIntegrator.REQUEST_CODE);
        });
    }
}
