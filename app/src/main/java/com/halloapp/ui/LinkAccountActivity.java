package com.halloapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

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

    private View connectButton;
    private View disconnectButton;
    private TextView connectedTextView;
    private WebClientManager webClientManager = WebClientManager.getInstance();
    private BgWorkers bgWorkers = BgWorkers.getInstance();
    private WebClientManager.WebClientObserver webClientObserver;
    private MutableLiveData<Boolean> isConnected = new MutableLiveData<>();

    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webClientObserver = new WebClientManager.WebClientObserver() {
            @Override
            public void onConnectedToWebClientChanged(boolean isConnected){
                LinkAccountActivity.this.isConnected.postValue(isConnected);
            }
        };

        webClientManager.addObserver(webClientObserver);

        setContentView(R.layout.activity_link_account);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        connectButton = findViewById(R.id.connect_button);
        disconnectButton = findViewById(R.id.disconnect_button);
        connectedTextView = findViewById(R.id.connect_text);

        bgWorkers.execute(() -> {
            isConnected.postValue(webClientManager.isConnectedToWebClient());
        });

        isConnected.observe(this, connected -> {
            connectButton.setVisibility(connected ? View.GONE : View.VISIBLE);
            disconnectButton.setVisibility(connected ? View.VISIBLE : View.GONE);
            connectedTextView.setText(connected ? R.string.connected_to_web_client : R.string.not_connected_to_web_client);
        });

        connectButton.setOnClickListener(v -> {
            Intent intent = new IntentIntegrator(this)
                    .setPrompt("")
                    .setBeepEnabled(false)
                    .createScanIntent();
            intent.putExtra(Intents.Scan.MODE, Intents.Scan.QR_CODE_MODE);
            intent.putExtra(Intents.Scan.CHARACTER_SET, StandardCharsets.ISO_8859_1.name());
            startActivityForResult(intent, IntentIntegrator.REQUEST_CODE);
        });

        disconnectButton.setOnClickListener(v -> {
            webClientManager.disconnect();
        });
    }

    @Override
    public void onDestroy() {
        webClientManager.removeObserver(webClientObserver);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            String qrCode = result.getContents();
            if (qrCode != null) {
                BgWorkers.getInstance().execute(() -> {
                    byte[] webClientStaticKey = getWebClientStaticKeyFromQrCode(qrCode);
                    webClientManager.connect(webClientStaticKey);
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
