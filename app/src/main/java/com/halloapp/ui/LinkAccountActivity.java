package com.halloapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.io.BaseEncoding;
import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.halloapp.R;
import com.halloapp.crypto.web.WebClientManager;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class LinkAccountActivity extends HalloActivity {
    TextView connectButton;

    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_link_account);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        connectButton = findViewById(R.id.connect_button);
        WebClientManager webClientManager = WebClientManager.getInstance();

        // TODO(justin): changed hard-coded strings to strings.xml
        String connectedText = webClientManager.isConnectedToWebClient() ? "Connected" : "Disconnected";
        TextView isConnectedText = findViewById(R.id.connect_text);
        isConnectedText.setText(connectedText);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        WebClientManager webClientManager = WebClientManager.getInstance();
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            String qrCode = result.getContents();
            if (qrCode != null) {
                BgWorkers.getInstance().execute(() -> {
                    byte[] webClientStaticKey = getWebClientStaticKeyFromQrCode(qrCode);
                    webClientManager.connect(webClientStaticKey);
                    // TODO(justin): add disconnect functionality and update connect/disconnected strings accordingly
                });
            } else {
                Log.i("LinkAccount: null result content");
            }
        } else {
            Log.i("LinkAccount: null IntentResult from scanner");
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    private byte[] getWebClientStaticKeyFromQrCode(@NonNull String qrCode) {
        byte[] key = BaseEncoding.base64().decode(qrCode);
        return Arrays.copyOfRange(key , 1, 33);
    }
}
