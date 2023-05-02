package com.halloapp.ui.chat.chat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class KeyVerificationActivity extends HalloActivity {

    public static Intent openKeyVerification(@NonNull Context context, @NonNull UserId userId) {
        Preconditions.checkNotNull(userId);
        Intent intent = new Intent(context, KeyVerificationActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        return intent;
    }

    private static final String EXTRA_USER_ID = "user_id";
    private static final int SAFETY_NUMBER_SPACING_DP = 5;
    private static final int VIBRATION_TIME_MS = 500;

    private KeyVerificationViewModel viewModel;
    private ImageView qrCode;
    private ProgressDialog fetchingKeysDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_key_verification);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        UserId userId = getIntent().getParcelableExtra(EXTRA_USER_ID);
        if (userId == null) {
            Log.e("KeyVerificationActivity got null user id");
            finish();
            return;
        }

        fetchingKeysDialog = ProgressDialog.show(this, null, getString(R.string.key_verification_fetching_keys), true);
        fetchingKeysDialog.show();

        qrCode = findViewById(R.id.qr_code);
        View qrRegion = findViewById(R.id.qr_region);
        qrRegion.setOnClickListener(v -> {
            Intent intent = new IntentIntegrator(this)
                    .setPrompt("")
                    .setBeepEnabled(false)
                    .createScanIntent();
            intent.putExtra(Intents.Scan.MODE, Intents.Scan.QR_CODE_MODE);
            intent.putExtra(Intents.Scan.CHARACTER_SET, StandardCharsets.ISO_8859_1.name());
            startActivityForResult(intent, IntentIntegrator.REQUEST_CODE);
        });

        SwitchCompat verifiedSwitch = findViewById(R.id.verified_switch);
        verifiedSwitch.setOnClickListener(v -> {
            viewModel.markVerificationState(verifiedSwitch.isChecked());
        });

        viewModel = new ViewModelProvider(this,
                new KeyVerificationViewModel.Factory(getApplication(), userId)).get(KeyVerificationViewModel.class);

        viewModel.name.getLiveData().observe(this, name -> {
            TextView tv = toolbar.findViewById(R.id.subtitle);
            if (name == null) {
                tv.setVisibility(View.GONE);
            } else {
                tv.setVisibility(View.VISIBLE);
                tv.setText(name);

                TextView explanationText = findViewById(R.id.explanation);
                CharSequence text = Html.fromHtml(getString(R.string.key_verification_explanation, name));
                text = StringUtils.replaceLink(explanationText.getContext(), text, "learn-more", () -> {
                    IntentUtils.openOurWebsiteInBrowser(explanationText, Constants.ENCRYPTED_CHAT_BLOG_SUFFIX);
                });
                explanationText.setText(text);
                explanationText.setMovementMethod(LinkMovementMethod.getInstance());
            }
        });

        viewModel.verifiedSwitchState.getLiveData().observe(this, verifiedSwitch::setChecked);

        viewModel.keyVerificationData.getLiveData().observe(this, info -> {
            if (info == null) {
                SnackbarHelper.showWarning(this, R.string.key_verification_key_fetch_failed);
                return;
            }

            qrCode.setImageBitmap(info.qrCode);

            fetchingKeysDialog.dismiss();

            List<String> sn = info.safetyNumber;
            if (sn == null || sn.size() < 12) {
                Log.e("Received invalid safety number " + sn);
                return;
            }

            LinearLayout safetyNumber = findViewById(R.id.safety_number);
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, SAFETY_NUMBER_SPACING_DP, getResources().getDisplayMetrics());

            LinearLayout horizontal = null;
            for (int i=0; i<12; i++) {
                if (i % 4 == 0) {
                    horizontal = new LinearLayout(this);
                    horizontal.setGravity(Gravity.CENTER);
                    safetyNumber.addView(horizontal);
                }

                TextView tv = new TextView(this);
                tv.setText(sn.get(i));
                tv.setTypeface(Typeface.MONOSPACE);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(2 * px, px, 2 * px, px);
                tv.setLayoutParams(layoutParams);

                horizontal.addView(tv);
            }
        });

        viewModel.verificationResult.observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                SnackbarHelper.showInfo(this, R.string.key_verification_success);
            } else {
                SnackbarHelper.showWarning(this, R.string.key_verification_failure);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            String results = result.getContents();
            if (results != null) {
                vibrate();
                viewModel.verify(results);
            } else {
                Log.i("KeyVerification: null result content");
            }
        } else {
            Log.i("KeyVerification: null IntentResult from scanner");
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(VibrationEffect.createOneShot(VIBRATION_TIME_MS, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(VIBRATION_TIME_MS);
        }
    }
}
