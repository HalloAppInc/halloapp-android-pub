package com.halloapp.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Preconditions;

import com.halloapp.BuildConfig;
import com.halloapp.R;
import com.halloapp.util.Log;
import com.halloapp.widget.CenterToast;

public class ExpiredAppActivity extends AppCompatActivity {

    public static final String EXTRA_DAYS_LEFT = "days_left";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_expired);

        String text;
        int daysLeft = getIntent().getIntExtra(EXTRA_DAYS_LEFT, 10);
        if (daysLeft > 0) {
            text = getResources().getQuantityString(R.plurals.app_expiration_days_left, daysLeft, daysLeft);
            Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        } else {
            text = getResources().getString(R.string.app_expired_explanation);
        }

        TextView description = findViewById(R.id.description);
        description.setText(text);

        final Button downloadButton = findViewById(R.id.download_button);
        downloadButton.setOnClickListener(v -> {
            try {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID));
                intent.setPackage("com.android.vending");
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.i("ExpiredAppActivity Play Store Not Installed", e);
                CenterToast.show(ExpiredAppActivity.this, R.string.app_expiration_no_play_store);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
